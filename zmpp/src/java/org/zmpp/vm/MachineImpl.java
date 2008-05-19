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
import org.zmpp.base.Memory;
import org.zmpp.blorb.BlorbImage;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharEncoder;
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
 * @version 1.0
 */
public class MachineImpl implements Machine {

  /**
   * Number of undo steps.
   */
  private static final int NUM_UNDO = 5;
  
  private GameData gamedata;
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
  
  /**
   * Constructor.
   */
  public MachineImpl() {
    this.inputFunctions = new InputFunctions(this);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getVersion() {
    return gamedata.getStoryFileHeader().getVersion();
  }
  
  public boolean hasValidChecksum() {
    return gamedata.hasValidChecksum();
  }
  
  /**
   * {@inheritDoc}
   */
  public StoryFileHeader getFileHeader() {
    return gamedata.getStoryFileHeader();
  }

  // **********************************************************************
  // ***** Memory interface functionality
  // **********************************************************************
  private Memory getMemory() { return gamedata.getMemory(); }
  public long readUnsigned32(int address) {
    return getMemory().readUnsigned32(address);
  }
  public int readUnsignedShort(int address) {
    return getMemory().readUnsignedShort(address);
  }
  public short readShort(int address) {
    return getMemory().readShort(address);
  }
  public short readUnsignedByte(int address) {
    return getMemory().readUnsignedByte(address);
  }
  public byte readByte(int address) {
    return getMemory().readByte(address);
  }
  public void writeUnsignedShort(int address, int value) {
    getMemory().writeUnsignedShort(address, value);
  }
  public void writeShort(int address, short value) {
    getMemory().writeShort(address, value);
  }
  public void writeUnsignedByte(int address, short value) {
    getMemory().writeUnsignedByte(address, value);
  }
  public void writeByte(int address, byte value) {
    getMemory().writeByte(address, value);
  }
  public void writeUnsigned32(int address, long value) {
    getMemory().writeUnsigned32(address, value);
  }
  
  // **********************************************************************
  // ***** Cpu interface functionality
  // **********************************************************************
  private Cpu getCpu() { return cpu; }
  public Instruction nextInstruction() { return getCpu().nextInstruction(); }
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
  
  /**
   */
  public short callInterrupt(int routineAddress) {
    return cpu.callInterrupt(routineAddress);
  }

  public boolean interruptDidOutput() {
    return cpu.interruptDidOutput();
  } 

  // **********************************************************************
  // ***** Dictionary functionality
  // **********************************************************************
  private static final ZsciiString WHITESPACE =
    new ZsciiString(new char[] { ' ', '\n', '\t', '\r' });
  
  private Dictionary getDictionary() { return gamedata.getDictionary(); }

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
    final ZCharDecoder decoder = getZCharDecoder();
    for (int i = 0, n = getDictionary().getNumberOfSeparators(); i < n; i++) {
      separators.append(decoder.decodeZChar((char)
              getDictionary().getSeparator(i)));
    }
    // The tokenizer will also return the delimiters
    return separators.toZsciiString();
  }

  // **********************************************************************
  // ***** Encoding functionality
  // **********************************************************************
  private ZCharDecoder getZCharDecoder() { return gamedata.getZCharDecoder(); }
  private ZCharEncoder getZCharEncoder() { return gamedata.getZCharEncoder(); }
  public char[] convertToZscii(String str) {
    return gamedata.getZsciiEncoding().convertToZscii(str);
  }
  
  public void encode(int source, int length, int destination) {
    getZCharEncoder().encode(getMemory(), source, length, destination);
  }

  public ZsciiString decode2Zscii(int address, int length) {
    return getZCharDecoder().decode2Zscii(getMemory(), address, length);
  }
  
  public char getUnicodeChar(char zsciiChar) {
    return gamedata.getZsciiEncoding().getUnicodeChar(zsciiChar);
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

  public void printZsciiChar(char zchar, boolean isInput) {
    output.printZsciiChar(zchar, isInput);
  }

  public void deletePreviousZsciiChar(char zchar) {
    output.deletePreviousZsciiChar(zchar);
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
  
  // **********************************************************************
  // ***** Initialization
  // **************************************
  /**
   * {@inheritDoc}
   */
  public void initialize(final GameData gamedata,
      final InstructionDecoder decoder) {
    this.gamedata = gamedata;
    this.random = new UnpredictableRandomGenerator();
    this.undostates = new RingBuffer<PortableGameState>(NUM_UNDO);
    
    cpu = new CpuImpl(this, decoder);
    output = new OutputImpl(this);
    input = new InputImpl(this);
    
    MediaCollection<SoundEffect> sounds = null;
    MediaCollection<BlorbImage> pictures = null;
    int resourceRelease = 0;
    
    if (gamedata.getResources() != null) {
      sounds = gamedata.getResources().getSounds();
      pictures = gamedata.getResources().getImages();
      resourceRelease = gamedata.getResources().getRelease();
    }
    
    soundSystem = new SoundSystemImpl(sounds);
    pictureManager = new PictureManagerImpl(resourceRelease, this, pictures);
    
    resetState();
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
    System.err.println("WARNING: " + msg);
  }
  
  /**
   * {@inheritDoc} 
   */
  public void restart() {
    restart(true);
  }
  
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
  public SoundSystem getSoundSystem() {
    return soundSystem;
  }

  /**
   * {@inheritDoc}
   */
  public PictureManager getPictureManager() {
    return pictureManager;
  }
  
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
    if (gamedata.getStoryFileHeader().getVersion() <= 3 && statusLine != null) {
      final int objNum = cpu.getVariable(0x10);    
      final String objectName = gamedata.getZCharDecoder().decode2Zscii(
          gamedata.getMemory(),
          gamedata.getObjectTree().getPropertiesDescriptionAddress(objNum), 0)
          	.toString();      
      final int global2 = cpu.getVariable(0x11);
      final int global3 = cpu.getVariable(0x12);
      if (gamedata.getStoryFileHeader().isEnabled(Attribute.SCORE_GAME)) {
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
  public ScreenModel getScreen() {
    return screenModel;
  }
  
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
        //System.out.printf("restore(), pc is: %4x running: %b\n", getProgramCounter(), isRunning());
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
      System.out.printf("restore(), pc is: %4x\n", cpu.getPC());
      return undoGameState;
    }
    return null;
  }
  
  // ***********************************************************************
  // ***** Private methods
  // **************************************
  
  private boolean verifySaveGame(final PortableGameState gamestate) {
    // Verify the game according to the standard
    final StoryFileHeader fileHeader = gamedata.getStoryFileHeader();
    int checksum = fileHeader.getChecksum();
    if (checksum == 0) {
      checksum = gamedata.getCalculatedChecksum();
    }
    return gamestate.getRelease() == fileHeader.getRelease()
      && gamestate.getChecksum() == checksum
      && gamestate.getSerialNumber().equals(fileHeader.getSerialNumber());
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
    output.reset();
    soundSystem.reset();
    cpu.reset();
    //gamedata.getStoryFileHeader().setStandardRevision(1, 0);
    if (gamedata.getStoryFileHeader().getVersion() >= 4) {
      gamedata.getStoryFileHeader().setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
      //gamedata.getStoryFileHeader().setInterpreterNumber(4); // Amiga
      gamedata.getStoryFileHeader().setInterpreterNumber(6); // IBM PC
      gamedata.getStoryFileHeader().setInterpreterVersion(1);
    }
  }
  
  private void restart(final boolean resetScreenModel) {
    // Transcripting and fixed font bits survive the restart
    final StoryFileHeader fileHeader = gamedata.getStoryFileHeader();
    final boolean fixedFontForced =
      fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT);
    final boolean transcripting = fileHeader.isEnabled(Attribute.TRANSCRIPTING);
    
    gamedata.reset();
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
  
  private ObjectTree getObjectTree() { return gamedata.getObjectTree(); }

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
