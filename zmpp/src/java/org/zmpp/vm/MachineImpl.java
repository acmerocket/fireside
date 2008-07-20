/*
 * $Id$
 * 
 * Created on 2005/10/03
 * Copyright 2005-2008 by Wei-ju Wu
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.vm;

import java.util.List;
import java.util.logging.Logger;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.blorb.BlorbImage;
import org.zmpp.encoding.AccentTable;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV1;
import org.zmpp.encoding.AlphabetTableV2;
import org.zmpp.encoding.CustomAccentTable;
import org.zmpp.encoding.CustomAlphabetTable;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharDecoder;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharEncoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.encoding.ZsciiStringBuilder;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.media.MediaCollection;
import org.zmpp.media.PictureManager;
import org.zmpp.media.PictureManagerImpl;
import org.zmpp.media.Resources;
import org.zmpp.media.SoundEffect;
import org.zmpp.media.SoundSystem;
import org.zmpp.media.SoundSystemImpl;
import org.zmpp.vm.StoryFileHeader.Attribute;
import org.zmpp.vmutil.PredictableRandomGenerator;
import org.zmpp.vmutil.RandomGenerator;
import org.zmpp.vmutil.RingBuffer;
import org.zmpp.vmutil.UnpredictableRandomGenerator;

/**
 * This class implements the state and some services of a Z-machine, version 3.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MachineImpl implements Machine {

  private static final Logger LOG = Logger.getLogger("MachineImpl");

  /** Number of undo steps. */
  private static final int NUM_UNDO = 5;
  
  private MachineRunState runstate;
  private RandomGenerator random;
  private StatusLine statusLine;
  private ScreenModel screenModel;
  private SaveGameDataStore datastore;
  private RingBuffer<PortableGameState> undostates;
  private InputFunctions inputFunctions;
  private SoundSystem soundSystem;
  private PictureManager pictureManager;  
  private Cpu cpu;
  private OutputImpl output;
  private InputImpl input;
  
  // Formerly GameData
  private StoryFileHeader fileheader;
  private Memory memory;
  private Dictionary dictionary;
  private ObjectTree objectTree;
  private ZsciiEncoding encoding;
  private ZCharDecoder decoder;
  private ZCharEncoder encoder;  
  private AlphabetTable alphabetTable;  
  private byte[] storyfileData;
  private int checksum;
  
  /**
   * Constructor.
   */
  public MachineImpl() {
    this.inputFunctions = new InputFunctions(this);
  }
  
  // **********************************************************************
  // ***** Initialization
  // **************************************
  /**
   * {@inheritDoc}
   */
  public void initialize(final byte[] data, Resources resources) {
    this.storyfileData = data;
    this.random = new UnpredictableRandomGenerator();
    this.undostates = new RingBuffer<PortableGameState>(NUM_UNDO);
    
    cpu = new CpuImpl(this);
    output = new OutputImpl(this);
    input = new InputImpl();
    
    MediaCollection<SoundEffect> sounds = null;
    MediaCollection<BlorbImage> pictures = null;
    int resourceRelease = 0;
    
    if (resources != null) {
      sounds = resources.getSounds();
      pictures = resources.getImages();
      resourceRelease = resources.getRelease();
    }
    
    soundSystem = new SoundSystemImpl(sounds);
    pictureManager = new PictureManagerImpl(resourceRelease, this, pictures);
    
    resetState();
  }

  /**
   * Resets the data.
   */
  public final void resetGameData() {
    // Make a copy and initialize from the copy
    final byte[] data = new byte[storyfileData.length];
    System.arraycopy(storyfileData, 0, data, 0, storyfileData.length);
    
    memory = new DefaultMemory(data);
    fileheader = new DefaultStoryFileHeader(memory);
    checksum = calculateChecksum();
    
    // Install the whole character code system here
    initEncodingSystem();
    
    // The object tree and dictionaries depend on the code system
    if (fileheader.getVersion() <= 3) {
      objectTree = new ClassicObjectTree(memory,
          fileheader.getObjectTableAddress());
    } else {
      objectTree = new ModernObjectTree(memory,
          fileheader.getObjectTableAddress());
    }
    final DictionarySizes sizes = (fileheader.getVersion() <= 3) ?
        new DictionarySizesV1ToV3() : new DictionarySizesV4ToV8();
    dictionary = new DefaultDictionary(memory,
        fileheader.getDictionaryAddress(), decoder, sizes);
  }
  
  private void initEncodingSystem() {
    final AccentTable accentTable = (fileheader.getCustomAccentTable() == 0) ?
        new DefaultAccentTable() :
        new CustomAccentTable(memory, fileheader.getCustomAccentTable());
    encoding = new ZsciiEncoding(accentTable);

    // Configure the alphabet table
    if (fileheader.getCustomAlphabetTable() == 0) {
      if (fileheader.getVersion() == 1) {
        alphabetTable = new AlphabetTableV1();
      } else if (fileheader.getVersion() == 2) {
        alphabetTable = new AlphabetTableV2();
      } else {
        alphabetTable = new DefaultAlphabetTable();
      }
    } else {
      alphabetTable = new CustomAlphabetTable(memory,
          fileheader.getCustomAlphabetTable());
    }
    
    final ZCharTranslator translator =
      new DefaultZCharTranslator(alphabetTable);
        
    final Abbreviations abbreviations = new Abbreviations(memory,
        fileheader.getAbbreviationsAddress());
    decoder = new DefaultZCharDecoder(encoding, translator, abbreviations);
    encoder = new ZCharEncoder(translator);
    ZsciiString.initialize(encoding);
  }
  
  /**
   * Calculates the checksum of the file.
   * @param fileheader the file header
   * @return the check sum
   */
  private int calculateChecksum() {
    final int filelen = fileheader.getFileLength();
    int sum = 0;
    for (int i = 0x40; i < filelen; i++) {
      sum += getMemory().readUnsigned8(i);
    }
    return (sum & 0xffff);
  }
 
  /**
   * {@inheritDoc}
   */
  public int getVersion() {
    return getFileHeader().getVersion();
  }
  
  public boolean hasValidChecksum() {
    return this.checksum == getFileHeader().getChecksum();
  }
  
  /**
   * {@inheritDoc}
   */
  public StoryFileHeader getFileHeader() { return fileheader; }

  // **********************************************************************
  // ***** Memory interface functionality
  // **********************************************************************
  private Memory getMemory() { return memory; }
  public long readUnsigned32(int address) {
    return getMemory().readUnsigned32(address);
  }
  public char readUnsigned16(int address) {
    return getMemory().readUnsigned16(address);
  }
  public short readSigned16(int address) {
    return getMemory().readSigned16(address);
  }
  public short readUnsigned8(int address) {
    return getMemory().readUnsigned8(address);
  }
  public byte readSigned8(int address) {
    return getMemory().readSigned8(address);
  }
  public void writeUnsigned16(int address, char value) {
    getMemory().writeUnsigned16(address, value);
  }
  public void writeSigned16(int address, short value) {
    getMemory().writeSigned16(address, value);
  }
  public void writeUnsigned8(int address, short value) {
    getMemory().writeUnsigned8(address, value);
  }
  public void writeSigned8(int address, byte value) {
    getMemory().writeSigned8(address, value);
  }
  public void writeUnsigned32(int address, long value) {
    getMemory().writeUnsigned32(address, value);
  }
  
  // **********************************************************************
  // ***** Cpu interface functionality
  // **********************************************************************
  private Cpu getCpu() { return cpu; }
  public short getVariable(int varnum) { return getCpu().getVariable(varnum); }
  public void setVariable(int varnum, short value) {
    getCpu().setVariable(varnum, value);
  }
  public short getStackTop() { return getCpu().getStackTop(); }
  public short getStackElement(int index) {
    return getCpu().getStackElement(index);
  }
  public void setStackTop(short value) {
    getCpu().setStackTop(value);
  }
  public void incrementPC(int length) {
    getCpu().incrementPC(length);
  }
  public void setPC(int address) {
    getCpu().setPC(address);
  }
  public int getPC() { return getCpu().getPC(); }
  public int getSP() { return getCpu().getSP(); }
  public short popStack(int userstackAddress) {
    return getCpu().popStack(userstackAddress);
  }
  public boolean pushStack(int stack, short value) {
    return getCpu().pushStack(stack, value);
  }
  public List<RoutineContext> getRoutineContexts() {
    return getCpu().getRoutineContexts();
  }
  public void setRoutineContexts(List<RoutineContext> routineContexts) {
    getCpu().setRoutineContexts(routineContexts);
  }
  public void returnWith(short returnValue) {
    getCpu().returnWith(returnValue);
  }
  public RoutineContext getCurrentRoutineContext() {
    return getCpu().getCurrentRoutineContext();
  }

  public int unpackStringAddress(int packedAddress) {
    return getCpu().unpackStringAddress(packedAddress);
  }
  public RoutineContext call(int packedAddress, int returnAddress, short[] args,
                             int returnVar) {
    return getCpu().call(packedAddress, returnAddress, args, returnVar);
  }
  
  public void doBranch(short branchOffset, int instructionLength) {
    getCpu().doBranch(branchOffset, instructionLength);
  }
  
  // **********************************************************************
  // ***** Dictionary functionality
  // **********************************************************************
  private static final ZsciiString WHITESPACE =
    new ZsciiString(new char[] { ' ', '\n', '\t', '\r' });
  
  private Dictionary getDictionary() { return dictionary; }

  public int lookupToken(int dictionaryAddress, ZsciiString token) {
    if (dictionaryAddress == 0) {
      return getDictionary().lookup(token);
    }
    return new UserDictionary(getMemory(), dictionaryAddress,
                              getZCharDecoder()).lookup(token);
  }
  
  public ZsciiString getDictionaryDelimiters() {
    // Retrieve the defined separators
    final ZsciiStringBuilder separators = new ZsciiStringBuilder();
    separators.append(WHITESPACE);    
    for (int i = 0, n = getDictionary().getNumberOfSeparators(); i < n; i++) {
      separators.append(getZCharDecoder().decodeZChar((char)
              getDictionary().getSeparator(i)));
    }
    // The tokenizer will also return the delimiters
    return separators.toZsciiString();
  }

  // **********************************************************************
  // ***** Encoding functionality
  // **********************************************************************
  private ZCharDecoder getZCharDecoder() { return decoder; }
  private ZCharEncoder getZCharEncoder() { return encoder; }
  public String convertToZscii(String str) {
    return encoding.convertToZscii(str);
  }
  
  public void encode(int source, int length, int destination) {
    getZCharEncoder().encode(getMemory(), source, length, destination);
  }

  public ZsciiString decode2Zscii(int address, int length) {
    return getZCharDecoder().decode2Zscii(getMemory(), address, length);
  }
  
  public char getUnicodeChar(char zsciiChar) {
    return encoding.getUnicodeChar(zsciiChar);
  }

  // **********************************************************************
  // ***** Output stream management, implemented by the OutputImpl object
  // **********************************************************************
  /**  
   * Sets the output stream to the specified number.
   * @param streamnumber the stream number
   * @param stream the output stream
   */
  public void setOutputStream(int streamnumber, OutputStream stream) {
    output.setOutputStream(streamnumber, stream);
  }

  public void selectOutputStream(int streamnumber, boolean flag) {
    output.selectOutputStream(streamnumber, flag);
  }

  public void selectOutputStream3(int tableAddress, int tableWidth) {
    output.selectOutputStream3(tableAddress, tableWidth);
  }

  public void printZString(int stringAddress) {
    output.printZString(stringAddress);
  }

  public void print(ZsciiString str) {
    output.print(str);
  }

  public void newline() {
    output.newline();
  }

  public void printZsciiChar(char zchar) {
    output.printZsciiChar(zchar);
  }

  public void printNumber(short num) {
    output.printNumber(num);
  }

  public void flushOutput() {
    output.flushOutput();
  }

  public void reset() {
    output.reset();
  }
  
  // **********************************************************************
  // ***** Input stream management, implemented by the InputImpl object
  // ********************************************************************
  /**
   * Sets an input stream to the specified number.
   * @param streamnumber the input stream number
   * @param stream the input stream to set
   */
  public void setInputStream(int streamNumber, InputStream stream) {
    input.setInputStream(streamNumber, stream);
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream getSelectedInputStream() {
    return input.getSelectedInputStream();
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectInputStream(int streamNumber) {
    input.selectInputStream(streamNumber);
  }
  
  /**
   * {@inheritDoc}
   */
  public short random(final short range) {
    if (range < 0) {
      random = new PredictableRandomGenerator(-range);
      return 0;
    } else if (range == 0) {
      random = new UnpredictableRandomGenerator();
      return 0;
    }
    return (short) ((random.next() % range) + 1);
  }

  // ************************************************************************
  // ****** Control functions
  // ************************************************

  /**
   * {@inheritDoc}
   */
  public MachineRunState getRunState() {
    return runstate;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setRunState(MachineRunState runstate) {
    this.runstate = runstate;
  }

  /**
   * {@inheritDoc}
   */
  public void halt(final String errormsg) {
    print(new ZsciiString(errormsg));
    runstate = MachineRunState.STOPPED;
  }  

  /**
   * {@inheritDoc}
   */
  public void warn(final String msg) {
    LOG.warning("WARNING: " + msg);
  }
  
  /**
   * {@inheritDoc} 
   */
  public void restart() { restart(true); }
  
  /**
   * {@inheritDoc} 
   */
  public void quit() {
    runstate = MachineRunState.STOPPED;    
    // On quit, close the streams
    output.print(new ZsciiString("*Game ended*"));
    closeStreams();
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    runstate = MachineRunState.RUNNING;
  }
  
  // ************************************************************************
  // ****** Machine services
  // ************************************************

  /**
   * {@inheritDoc}
   */
  public void tokenize(final int textbuffer, final int parsebuffer,
      final int dictionaryAddress, final boolean flag) {
    inputFunctions.tokenize(textbuffer, parsebuffer, dictionaryAddress, flag);
  }
  
  /**
   * {@inheritDoc}
   */
  public char readLine(final int textbuffer) {
    return inputFunctions.readLine(textbuffer);
  }
  
  /**
   * {@inheritDoc}
   */
  public char readChar() {
    return inputFunctions.readChar();
  }
  
  /**
   * {@inheritDoc}
   */
  public SoundSystem getSoundSystem() { return soundSystem; }

  /**
   * {@inheritDoc}
   */
  public PictureManager getPictureManager() { return pictureManager; }
  
  /**
   * {@inheritDoc}
   */
  public void setSaveGameDataStore(final SaveGameDataStore datastore) {
    this.datastore = datastore;
  }

  /**
   * {@inheritDoc}
   */
  public void updateStatusLine() {
    if (getFileHeader().getVersion() <= 3 && statusLine != null) {
      final int objNum = cpu.getVariable(0x10);    
      final String objectName = getZCharDecoder().decode2Zscii(getMemory(),
        getObjectTree().getPropertiesDescriptionAddress(objNum), 0).toString();      
      final int global2 = cpu.getVariable(0x11);
      final int global3 = cpu.getVariable(0x12);
      if (getFileHeader().isEnabled(Attribute.SCORE_GAME)) {
        statusLine.updateStatusScore(objectName, global2, global3);
      } else {
        statusLine.updateStatusTime(objectName, global2, global3);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setStatusLine(final StatusLine statusLine) {
    this.statusLine = statusLine;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreen(final ScreenModel screen) {
    this.screenModel = screen;
  }
  
  /**
   * {@inheritDoc}
   */
  public ScreenModel getScreen() { return screenModel; }
  
  /**
   * {@inheritDoc}
   */
  public ScreenModel6 getScreen6() {
    return (ScreenModel6) screenModel;
  }
  
  /**
   * {@inheritDoc} 
   */
  public boolean save(final int savepc) {
    if (datastore != null) {
      final PortableGameState gamestate = new PortableGameState();
      gamestate.captureMachineState(this, savepc);
      final WritableFormChunk formChunk = gamestate.exportToFormChunk();
      return datastore.saveFormChunk(formChunk);
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean save_undo(final int savepc) {
    final PortableGameState undoGameState = new PortableGameState();
    undoGameState.captureMachineState(this, savepc);
    undostates.add(undoGameState);
    return true;
  }

  /**
   * {@inheritDoc} 
   */
  public PortableGameState restore() {
    if (datastore != null) {
      final PortableGameState gamestate = new PortableGameState();
      final FormChunk formchunk = datastore.retrieveFormChunk();
      gamestate.readSaveGame(formchunk);
      
      // verification has to be here
      if (verifySaveGame(gamestate)) {
        // do not reset screen model, since e.g. AMFV simply picks up the
        // current window state
        restart(false);
        gamestate.transferStateToMachine(this);
        return gamestate;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public PortableGameState restore_undo() {
    // do not reset screen model, since e.g. AMFV simply picks up the
    // current window state
    if (undostates.size() > 0) {
      final PortableGameState undoGameState =
        undostates.remove(undostates.size() - 1);      
      restart(false);
      undoGameState.transferStateToMachine(this);
      LOG.info(String.format("restore(), pc is: %4x\n", cpu.getPC()));
      return undoGameState;
    }
    return null;
  }
  
  // ***********************************************************************
  // ***** Private methods
  // **************************************
  
  private boolean verifySaveGame(final PortableGameState gamestate) {
    // Verify the game according to the standard
    int saveGameChecksum = getFileHeader().getChecksum();
    if (saveGameChecksum == 0) {
      saveGameChecksum = this.checksum;
    }
    return gamestate.getRelease() == getFileHeader().getRelease()
      && gamestate.getChecksum() == checksum
      && gamestate.getSerialNumber().equals(getFileHeader().getSerialNumber());
  }  

  /**
   * Close the streams.
   */
  private void closeStreams() {
    input.close();
    output.close();
  }
  
  /**
   * Resets all state to initial values, using the configuration object.
   */
  private void resetState() {
    resetGameData();
    output.reset();
    soundSystem.reset();
    cpu.reset();
    getFileHeader().setStandardRevision(1, 0);
    if (getFileHeader().getVersion() >= 4) {
      getFileHeader().setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
      getFileHeader().setInterpreterNumber(6); // IBM PC
      getFileHeader().setInterpreterVersion(1);
    }
  }
  
  private void restart(final boolean resetScreenModel) {
    // Transcripting and fixed font bits survive the restart
    final StoryFileHeader fileHeader = getFileHeader();
    final boolean fixedFontForced =
      fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT);
    final boolean transcripting = fileHeader.isEnabled(Attribute.TRANSCRIPTING);
    
    resetState();
    
    if (resetScreenModel) {
      screenModel.reset();    
    }
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, transcripting);
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, fixedFontForced);
  }

  // ***********************************************************************
  // ***** Object accesss
  // ************************************
  
  private ObjectTree getObjectTree() { return objectTree; }

  /**
   * {@inheritDoc}
   */
  public void insertObject(int parentNum, int objectNum) {
    getObjectTree().insertObject(parentNum, objectNum);
  }

  /**
   * {@inheritDoc}
   */
  public void removeObject(int objectNum) {
    getObjectTree().removeObject(objectNum);
  }

  /**
   * {@inheritDoc}
   */
  public void clearAttribute(int objectNum, int attributeNum) {
    getObjectTree().clearAttribute(objectNum, attributeNum);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAttributeSet(int objectNum, int attributeNum) {
    return getObjectTree().isAttributeSet(objectNum, attributeNum);
  }

  /**
   * {@inheritDoc}
   */
  public void setAttribute(int objectNum, int attributeNum) {
    getObjectTree().setAttribute(objectNum, attributeNum);
  }

  /**
   * {@inheritDoc}
   */
  public int getParent(int objectNum) {
    return getObjectTree().getParent(objectNum);
  }

  /**
   * {@inheritDoc}
   */
  public void setParent(int objectNum, int parent) {
    getObjectTree().setParent(objectNum, parent);
  }

  /**
   * {@inheritDoc}
   */
  public int getChild(int objectNum) {
    return getObjectTree().getChild(objectNum);
  }

  /**
   * {@inheritDoc}
   */
  public void setChild(int objectNum, int child) {
    getObjectTree().setChild(objectNum, child);
  }

  /**
   * {@inheritDoc}
   */
  public int getSibling(int objectNum) {
    return getObjectTree().getSibling(objectNum);
  }

  /**
   * {@inheritDoc}
   */
  public void setSibling(int objectNum, int sibling) {
    getObjectTree().setSibling(objectNum, sibling);
  }  

  /**
   * {@inheritDoc}
   */
  public int getPropertiesDescriptionAddress(int objectNum) {
    return getObjectTree().getPropertiesDescriptionAddress(objectNum);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getPropertyAddress(int objectNum, int property) {
    return getObjectTree().getPropertyAddress(objectNum, property);
  }

  /**
   * {@inheritDoc}
   */
  public int getPropertyLength(int propertyAddress) {
    return getObjectTree().getPropertyLength(propertyAddress);
  }

  /**
   * {@inheritDoc}
   */
  public int getProperty(int objectNum, int property) {
    return getObjectTree().getProperty(objectNum, property);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setProperty(int objectNum, int property, int value) {
    getObjectTree().setProperty(objectNum, property, value);
  }

  /**
   * {@inheritDoc}
   */
  public int getNextProperty(int objectNum, int property) {
    return getObjectTree().getNextProperty(objectNum, property);
  }
}
