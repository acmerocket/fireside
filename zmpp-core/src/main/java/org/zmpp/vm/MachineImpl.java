/*
 * Created on 2005/10/03
 * Copyright 2005-2009 by Wei-ju Wu
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

import org.zmpp.base.DefaultStoryFileHeader;
import org.zmpp.windowing.StatusLine;
import org.zmpp.media.Resolution;
import org.zmpp.windowing.ScreenModel6;
import org.zmpp.windowing.ScreenModel;
import java.util.List;
import java.util.logging.Logger;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.base.StoryFileHeader;
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
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.media.DrawingArea;
import org.zmpp.media.MediaCollection;
import org.zmpp.media.PictureManager;
import org.zmpp.media.PictureManagerImpl;
import org.zmpp.media.Resources;
import org.zmpp.media.SoundEffect;
import org.zmpp.media.SoundSystem;
import org.zmpp.media.SoundSystemImpl;
import org.zmpp.media.ZmppImage;
import org.zmpp.base.StoryFileHeader.Attribute;
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
public class MachineImpl implements Machine, DrawingArea {

  private static final Logger LOG = Logger.getLogger("org.zmpp");

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
  private Resources resources;
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
  /** {@inheritDoc} */
  public void initialize(final byte[] data, Resources aResources) {
    this.storyfileData = data;
    this.resources = aResources;
    this.random = new UnpredictableRandomGenerator();
    this.undostates = new RingBuffer<PortableGameState>(NUM_UNDO);

    cpu = new CpuImpl(this);
    output = new OutputImpl(this);
    input = new InputImpl();

    MediaCollection<SoundEffect> sounds = null;
    MediaCollection<? extends ZmppImage> pictures = null;
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

    final DictionarySizes dictionarySizes = (fileheader.getVersion() <= 3) ?
        new DictionarySizesV1ToV3() : new DictionarySizesV4ToV8();
    // Install the whole character code system here
    initEncodingSystem(dictionarySizes);

    // The object tree and dictionaries depend on the code system
    if (fileheader.getVersion() <= 3) {
      objectTree = new ClassicObjectTree(memory,
          memory.readUnsigned16(StoryFileHeader.OBJECT_TABLE));
    } else {
      objectTree = new ModernObjectTree(memory,
          memory.readUnsigned16(StoryFileHeader.OBJECT_TABLE));
    }
    // CAUTION: the current implementation of DefaultDictionary reads in all
    // entries into a hash table, so it will break when moving this statement
    // to a different position
    dictionary = new DefaultDictionary(memory,
        memory.readUnsigned16(StoryFileHeader.DICTIONARY), decoder, encoder,
                              dictionarySizes);
  }

  /**
   * Initializes the encoding system.
   * @param dictionarySizes the DictionarySizes
   */
  private void initEncodingSystem(DictionarySizes dictionarySizes) {
    final AccentTable accentTable = (fileheader.getCustomAccentTable() == 0) ?
        new DefaultAccentTable() :
        new CustomAccentTable(memory, fileheader.getCustomAccentTable());
    encoding = new ZsciiEncoding(accentTable);

    // Configure the alphabet table
    char customAlphabetTable =
        memory.readUnsigned16(StoryFileHeader.CUSTOM_ALPHABET);
    if (customAlphabetTable == 0) {
      if (fileheader.getVersion() == 1) {
        alphabetTable = new AlphabetTableV1();
      } else if (fileheader.getVersion() == 2) {
        alphabetTable = new AlphabetTableV2();
      } else {
        alphabetTable = new DefaultAlphabetTable();
      }
    } else {
      alphabetTable = new CustomAlphabetTable(memory, customAlphabetTable);
    }

    final ZCharTranslator translator =
      new DefaultZCharTranslator(alphabetTable);

    final Abbreviations abbreviations = new Abbreviations(memory,
        memory.readUnsigned16(StoryFileHeader.ABBREVIATIONS));
    decoder = new DefaultZCharDecoder(encoding, translator, abbreviations);
    encoder = new ZCharEncoder(translator, dictionarySizes);
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

  /** {@inheritDoc} */
  public int getVersion() {
    return getFileHeader().getVersion();
  }
  /** {@inheritDoc} */
  public int getRelease() {
    return getMemory().readUnsigned16(StoryFileHeader.RELEASE);
  }

  /** {@inheritDoc} */
  public boolean hasValidChecksum() {
    return this.checksum == getChecksum();
  }

  /** {@inheritDoc} */
  public StoryFileHeader getFileHeader() { return fileheader; }

  /** {@inheritDoc} */
  public Resources getResources() { return resources; }

  // **********************************************************************
  // ***** Memory interface functionality
  // **********************************************************************
  private Memory getMemory() { return memory; }
  /** {@inheritDoc} */
  public char readUnsigned16(int address) {
    return getMemory().readUnsigned16(address);
  }
  /** {@inheritDoc} */
  public char readUnsigned8(int address) {
    return getMemory().readUnsigned8(address);
  }
  /** {@inheritDoc} */
  public void writeUnsigned16(int address, char value) {
    getMemory().writeUnsigned16(address, value);
  }
  /** {@inheritDoc} */
  public void writeUnsigned8(int address, char value) {
    getMemory().writeUnsigned8(address, value);
  }
  /** {@inheritDoc} */
  public void copyBytesToArray(byte[] dstData, int dstOffset,
                               int srcOffset, int numBytes) {
    getMemory().copyBytesToArray(dstData, dstOffset, srcOffset,
                                 numBytes);
  }
  /** {@inheritDoc} */
  public void copyBytesFromArray(byte[] srcData, int srcOffset, int dstOffset,
                                 int numBytes) {
    getMemory().copyBytesFromArray(srcData, srcOffset, dstOffset, numBytes);
  }
  /** {@inheritDoc} */
  public void copyBytesFromMemory(Memory srcMem, int srcOffset, int dstOffset,
                                  int numBytes) {
    getMemory().copyBytesFromMemory(srcMem, srcOffset, dstOffset, numBytes);
  }
  /** {@inheritDoc} */
  public void copyArea(int src, int dst, int numBytes) {
    getMemory().copyArea(src, dst, numBytes);
  }
  // **********************************************************************
  // ***** Cpu interface functionality
  // **********************************************************************
  private Cpu getCpu() { return cpu; }
  /** {@inheritDoc} */
  public char getVariable(char varnum) { return getCpu().getVariable(varnum); }
  /** {@inheritDoc} */
  public void setVariable(char varnum, char value) {
    getCpu().setVariable(varnum, value);
  }
  /** {@inheritDoc} */
  public char getStackTop() { return getCpu().getStackTop(); }
  /** {@inheritDoc} */
  public char getStackElement(int index) {
    return getCpu().getStackElement(index);
  }
  /** {@inheritDoc} */
  public void setStackTop(char value) { getCpu().setStackTop(value); }
  /** {@inheritDoc} */
  public void incrementPC(int length) { getCpu().incrementPC(length); }
  /** {@inheritDoc} */
  public void setPC(int address) { getCpu().setPC(address); }
  /** {@inheritDoc} */
  public int getPC() { return getCpu().getPC(); }
  /** {@inheritDoc} */
  public char getSP() { return getCpu().getSP(); }
  /** {@inheritDoc} */
  public char popStack(char userstackAddress) {
    return getCpu().popStack(userstackAddress);
  }
  /** {@inheritDoc} */
  public boolean pushStack(char stack, char value) {
    return getCpu().pushStack(stack, value);
  }
  /** {@inheritDoc} */
  public List<RoutineContext> getRoutineContexts() {
    return getCpu().getRoutineContexts();
  }
  /** {@inheritDoc} */
  public void setRoutineContexts(List<RoutineContext> routineContexts) {
    getCpu().setRoutineContexts(routineContexts);
  }
  /** {@inheritDoc} */
  public void returnWith(char returnValue) {
    getCpu().returnWith(returnValue);
  }
  /** {@inheritDoc} */
  public RoutineContext getCurrentRoutineContext() {
    return getCpu().getCurrentRoutineContext();
  }
  /** {@inheritDoc} */
  public int unpackStringAddress(char packedAddress) {
    return getCpu().unpackStringAddress(packedAddress);
  }
  /** {@inheritDoc} */
  public RoutineContext call(char packedAddress, int returnAddress,
                             char[] args, char returnVar) {
    return getCpu().call(packedAddress, returnAddress, args, returnVar);
  }
  /** {@inheritDoc} */
  public void doBranch(short branchOffset, int instructionLength) {
    getCpu().doBranch(branchOffset, instructionLength);
  }

  // **********************************************************************
  // ***** Dictionary functionality
  // **********************************************************************
  private static final String WHITESPACE = " \n\t\r";

  private Dictionary getDictionary() { return dictionary; }
  /** {@inheritDoc} */
  public int lookupToken(int dictionaryAddress, String token) {
    if (dictionaryAddress == 0) {
      return getDictionary().lookup(token);
    }
    return new UserDictionary(getMemory(), dictionaryAddress,
                              getZCharDecoder(), encoder).lookup(token);
  }
  /** {@inheritDoc} */
  public String getDictionaryDelimiters() {
    // Retrieve the defined separators
    final StringBuilder separators = new StringBuilder();
    separators.append(WHITESPACE);
    for (int i = 0, n = getDictionary().getNumberOfSeparators(); i < n; i++) {
      separators.append(getZCharDecoder().decodeZChar((char)
              getDictionary().getSeparator(i)));
    }
    // The tokenizer will also return the delimiters
    return separators.toString();
  }

  // **********************************************************************
  // ***** Encoding functionality
  // **********************************************************************
  private ZCharDecoder getZCharDecoder() { return decoder; }
  private ZCharEncoder getZCharEncoder() { return encoder; }
  /** {@inheritDoc} */
  public String convertToZscii(String str) {
    return encoding.convertToZscii(str);
  }
  /** {@inheritDoc} */
  public void encode(int source, int length, int destination) {
    getZCharEncoder().encode(getMemory(), source, length, destination);
  }
  /** {@inheritDoc} */
  public int getNumZEncodedBytes(int address) {
    return getZCharDecoder().getNumZEncodedBytes(getMemory(), address);
  }
  /** {@inheritDoc} */
  public String decode2Zscii(int address, int length) {
    return getZCharDecoder().decode2Zscii(getMemory(), address, length);
  }
  /** {@inheritDoc} */
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

  public void print(String str) {
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
  /** {@inheritDoc} */
  public InputStream getSelectedInputStream() {
    return input.getSelectedInputStream();
  }
  /** {@inheritDoc} */
  public void selectInputStream(int streamNumber) {
    input.selectInputStream(streamNumber);
  }
  /** {@inheritDoc} */
  public char random(final short range) {
    if (range < 0) {
      random = new PredictableRandomGenerator(-range);
      return 0;
    } else if (range == 0) {
      random = new UnpredictableRandomGenerator();
      return 0;
    }
    return (char) ((random.next() % range) + 1);
  }

  // ************************************************************************
  // ****** Control functions
  // ************************************************
  /** {@inheritDoc} */
  public MachineRunState getRunState() { return runstate; }

  /** {@inheritDoc} */
  public void setRunState(MachineRunState aRunstate) {
    this.runstate = aRunstate;
    if (runstate != null && runstate.isWaitingForInput()) {
      updateStatusLine();
      flushOutput();
    }
  }

  /** {@inheritDoc} */
  public void halt(final String errormsg) {
    print(errormsg);
    runstate = MachineRunState.STOPPED;
  }
  /** {@inheritDoc} */
  public void warn(final String msg) {
    LOG.warning("WARNING: " + msg);
  }
  /** {@inheritDoc} */
  public void restart() { restart(true); }
  /** {@inheritDoc} */
  public void quit() {
    runstate = MachineRunState.STOPPED;
    // On quit, close the streams
    output.print("*Game ended*");
    closeStreams();
  }
  /** {@inheritDoc} */
  public void start() { runstate = MachineRunState.RUNNING; }

  // ************************************************************************
  // ****** Machine services
  // ************************************************

  /** {@inheritDoc} */
  public void tokenize(final int textbuffer, final int parsebuffer,
      final int dictionaryAddress, final boolean flag) {
    inputFunctions.tokenize(textbuffer, parsebuffer, dictionaryAddress, flag);
  }
  /** {@inheritDoc} */
  public char readLine(final int textbuffer) {
    return inputFunctions.readLine(textbuffer);
  }
  /** {@inheritDoc} */
  public char readChar() { return inputFunctions.readChar(); }
  /** {@inheritDoc} */
  public SoundSystem getSoundSystem() { return soundSystem; }
  /** {@inheritDoc} */
  public PictureManager getPictureManager() { return pictureManager; }
  /** {@inheritDoc} */
  public void setSaveGameDataStore(final SaveGameDataStore aDatastore) {
    this.datastore = aDatastore;
  }
  /** {@inheritDoc} */
  public void updateStatusLine() {
    if (getFileHeader().getVersion() <= 3 && statusLine != null) {
      final int objNum = cpu.getVariable((char) 0x10);
      final String objectName = getZCharDecoder().decode2Zscii(getMemory(),
        getObjectTree().getPropertiesDescriptionAddress(objNum), 0);
      final int global2 = cpu.getVariable((char) 0x11);
      final int global3 = cpu.getVariable((char) 0x12);
      if (getFileHeader().isEnabled(Attribute.SCORE_GAME)) {
        statusLine.updateStatusScore(objectName, global2, global3);
      } else {
        statusLine.updateStatusTime(objectName, global2, global3);
      }
    }
  }
  /** {@inheritDoc} */
  public void setStatusLine(final StatusLine statusLine) {
    this.statusLine = statusLine;
  }
  /** {@inheritDoc} */
  public void setScreen(final ScreenModel screen) {
    this.screenModel = screen;
  }
  /** {@inheritDoc} */
  public ScreenModel getScreen() { return screenModel; }
  /** {@inheritDoc} */
  public ScreenModel6 getScreen6() { return (ScreenModel6) screenModel; }
  /** {@inheritDoc} */
  public boolean save(final int savepc) {
    if (datastore != null) {
      final PortableGameState gamestate = new PortableGameState();
      gamestate.captureMachineState(this, savepc);
      final WritableFormChunk formChunk = gamestate.exportToFormChunk();
      return datastore.saveFormChunk(formChunk);
    }
    return false;
  }
  /** {@inheritDoc} */
  public boolean save_undo(final int savepc) {
    final PortableGameState undoGameState = new PortableGameState();
    undoGameState.captureMachineState(this, savepc);
    undostates.add(undoGameState);
    return true;
  }
  /** {@inheritDoc} */
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
  /** {@inheritDoc} */
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
    int saveGameChecksum = getChecksum();
    if (saveGameChecksum == 0) {
      saveGameChecksum = this.checksum;
    }
    return gamestate.getRelease() == getRelease()
      && gamestate.getChecksum() == checksum
      && gamestate.getSerialNumber().equals(getFileHeader().getSerialNumber());
  }

  private int getChecksum() {
    return memory.readUnsigned16(StoryFileHeader.CHECKSUM);
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
    setStandardRevision(1, 0);
    if (getFileHeader().getVersion() >= 4) {
      getFileHeader().setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
      // IBM PC
      getMemory().writeUnsigned8(StoryFileHeader.INTERPRETER_NUMBER, (char) 6);
      getFileHeader().setInterpreterVersion(1);
    }
  }

  private void setStandardRevision(int major, int minor) {
    memory.writeUnsigned8(StoryFileHeader.STD_REVISION_MAJOR, (char) major);
    memory.writeUnsigned8(StoryFileHeader.STD_REVISION_MINOR, (char) minor);
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
  /** {@inheritDoc} */
  public void insertObject(int parentNum, int objectNum) {
    getObjectTree().insertObject(parentNum, objectNum);
  }
  /** {@inheritDoc} */
  public void removeObject(int objectNum) {
    getObjectTree().removeObject(objectNum);
  }
  /** {@inheritDoc} */
  public void clearAttribute(int objectNum, int attributeNum) {
    getObjectTree().clearAttribute(objectNum, attributeNum);
  }
  /** {@inheritDoc} */
  public boolean isAttributeSet(int objectNum, int attributeNum) {
    return getObjectTree().isAttributeSet(objectNum, attributeNum);
  }
  /** {@inheritDoc} */
  public void setAttribute(int objectNum, int attributeNum) {
    getObjectTree().setAttribute(objectNum, attributeNum);
  }
  /** {@inheritDoc} */
  public int getParent(int objectNum) {
    return getObjectTree().getParent(objectNum);
  }
  /** {@inheritDoc} */
  public void setParent(int objectNum, int parent) {
    getObjectTree().setParent(objectNum, parent);
  }
  /** {@inheritDoc} */
  public int getChild(int objectNum) {
    return getObjectTree().getChild(objectNum);
  }
  /** {@inheritDoc} */
  public void setChild(int objectNum, int child) {
    getObjectTree().setChild(objectNum, child);
  }
  /** {@inheritDoc} */
  public int getSibling(int objectNum) {
    return getObjectTree().getSibling(objectNum);
  }
  /** {@inheritDoc} */
  public void setSibling(int objectNum, int sibling) {
    getObjectTree().setSibling(objectNum, sibling);
  }
  /** {@inheritDoc} */
  public int getPropertiesDescriptionAddress(int objectNum) {
    return getObjectTree().getPropertiesDescriptionAddress(objectNum);
  }
  /** {@inheritDoc} */
  public int getPropertyAddress(int objectNum, int property) {
    return getObjectTree().getPropertyAddress(objectNum, property);
  }
  /** {@inheritDoc} */
  public int getPropertyLength(int propertyAddress) {
    return getObjectTree().getPropertyLength(propertyAddress);
  }
  /** {@inheritDoc} */
  public char getProperty(int objectNum, int property) {
    return getObjectTree().getProperty(objectNum, property);
  }
  /** {@inheritDoc} */
  public void setProperty(int objectNum, int property, char value) {
    getObjectTree().setProperty(objectNum, property, value);
  }
  /** {@inheritDoc} */
  public int getNextProperty(int objectNum, int property) {
    return getObjectTree().getNextProperty(objectNum, property);
  }
  /** {@inheritDoc} */
  public Resolution getResolution() {
    return getScreen6().getResolution();
  }
}
