/*
 * $Id$
 * 
 * Created on 03.10.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zmpp.base.MemoryAccess;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.vm.StoryFileHeader.Attribute;
import org.zmpp.vmutil.PredictableRandomGenerator;
import org.zmpp.vmutil.RandomGenerator;
import org.zmpp.vmutil.UnpredictableRandomGenerator;
import org.zmpp.vmutil.ZString;
import org.zmpp.vmutil.ZsciiEncoding;

/**
 * This class implements the state and some services of a Z-machine, version 3.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MachineImpl implements Machine {

  /**
   * The configuration object.
   */
  private MachineConfig config;
  
  /**
   * The memory access object.
   */
  private MemoryAccess memaccess;
  
  /**
   * This machine's current program counter.
   */
  private int programCounter;
  
  /**
   * This machine's global stack.
   */
  private List<Short> stack;  
  
  /**
   * The routine info.
   */
  private List<RoutineContext> routineContextStack;
  
  /**
   * The start of global variables.
   */
  private int globalsAddress;
  
  /**
   * The object tree.
   */
  private ObjectTree objectTree;
  
  /**
   * The dictionary.
   */
  private Dictionary dictionary;
  
  /**
   * This is the array of output streams.
   */
  private OutputStream[] outputStream;
  
  /**
   * This is the array of input streams.
   */
  private InputStream[] inputStream;
  
  /**
   * The selected input stream.
   */
  private int selectedInputStreamIndex;
  
  /**
   * This flag indicates the run status.
   */
  private boolean running;
  
  /**
   * The random generator.
   */
  private RandomGenerator random;
  
  /**
   * The instruction decoder.
   */
  private InstructionDecoder decoder;
  
  /**
   * The status line.
   */
  private StatusLine statusLine;
  
  /**
   * The screen model.
   */
  private ScreenModel screenModel;
  
  /**
   * Returns the checksum verification status of the story file.
   */
  private boolean hasValidChecksum;
  
  /**
   * The file header.
   */
  private StoryFileHeader fileHeader;
  
  /**
   * The save game data store.
   */
  private SaveGameDataStore datastore;
  
  /**
   * The undo game state.
   */
  private PortableGameState undoGameState;
  
  /**
   * Constructor.
   */
  public MachineImpl() {

  }
  
  /**
   * {@inheritDoc}
   */
  public void initialize(MachineConfig config, InstructionDecoder decoder) {
  
    this.config = config;
    this.decoder = decoder;
    this.outputStream = new OutputStream[3];
    this.inputStream = new InputStream[2];    
    this.selectedInputStreamIndex = 0;
    this.random = new UnpredictableRandomGenerator();
    this.running = true;
    
    resetState();
  }

  /**
   * Resets all state to initial values, using the configuration object.
   */
  private void resetState() {
    
    this.memaccess = config.getMemoryAccess();
    this.fileHeader = config.getFileHeader();
    this.dictionary = config.getDictionary();
    this.objectTree = config.getObjectTree();
    
    this.stack = new ArrayList<Short>();
    this.routineContextStack = new ArrayList<RoutineContext>();
    this.programCounter = fileHeader.getProgramStart();
    this.globalsAddress = fileHeader.getGlobalsAddress();
    this.decoder.initialize(this, memaccess);
    int checksum = calculateChecksum(fileHeader);
    hasValidChecksum = fileHeader.getChecksum() == checksum;
    
    if (fileHeader.getVersion() >= 4) {
            
      fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, false);
      fileHeader.setInterpreterNumber(6); // IBM PC
      fileHeader.setInterpreterVersion(1);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public StoryFileHeader getStoryFileHeader() {
    
    return fileHeader;
  }
    
  /**
   * Calculates the checksum of the file.
   * 
   * @param fileheader the file header
   * @return the check sum
   */
  private int calculateChecksum(StoryFileHeader fileheader) {
    
    int filelen = fileheader.getFileLength();
    int sum = 0;
    
    for (int i = 0x40; i < filelen; i++) {
    
      sum += memaccess.readUnsignedByte(i);
    }
    return (sum & 0xffff);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasValidChecksum() {
    
    return this.hasValidChecksum;
  }
  
  /**
   * {@inheritDoc}
   */
  public MemoryAccess getMemoryAccess() {
    
    return memaccess;
  }
  
  /**
   * {@inheritDoc}
   */
  public Dictionary getDictionary() {
    
    return dictionary;
  }
  
  public ObjectTree getObjectTree() {
    
    return objectTree;
  }

  /**
   * {@inheritDoc}
   */
  public int getStackPointer() {
    
    return stack.size();
  }
  
  /**
   * Sets the global stack pointer to the specified value. This might pop off
   * several values from the stack.
   * 
   * @param stackpointer the new stack pointer value
   */
  private void setStackPointer(int stackpointer) {

    // remove the last diff elements
    int diff = getStackPointer() - stackpointer;
    for (int i = 0; i < diff; i++) {
     
      stack.remove(stack.size() - 1);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public short getStackTopElement() {
    
    if (stack.size() > 0) {
      
      return stack.get(stack.size() - 1);
    }
    return -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setStackTopElement(short value) {
    
    stack.set(stack.size() - 1, value);
  }
  
  /**
   * {@inheritDoc}
   */
  public short getStackElement(int index) {
    
    return stack.get(index);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getProgramCounter() {
    
    return programCounter;
  }

  /**
   * {@inheritDoc}
   */
  public void setProgramCounter(int address) {

    this.programCounter = address;
  }

  /**
   * {@inheritDoc}
   */
  public short getVariable(int variableNumber) {

    VariableType varType = getVariableType(variableNumber);
    if (varType == VariableType.STACK) {
      
      if (stack.size() == 0) {
        
        throw new IllegalStateException("stack is empty");
        
      } else {
   
        return stack.remove(stack.size() - 1);
      }
      
    } else if (varType == VariableType.LOCAL) {
      
      int localVarNumber = getLocalVariableNumber(variableNumber);
      checkLocalVariableAccess(localVarNumber);
      return getCurrentRoutineContext().getLocalVariable(localVarNumber);
      
    } else { // GLOBAL
      
      return memaccess.readShort(globalsAddress
          + (getGlobalVariableNumber(variableNumber) * 2));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setVariable(int variableNumber, short value) {

    VariableType varType = getVariableType(variableNumber);
    if (varType == VariableType.STACK) {
      
      stack.add(value);
      
    } else if (varType == VariableType.LOCAL) {
      
      int localVarNumber = getLocalVariableNumber(variableNumber);
      checkLocalVariableAccess(localVarNumber);
      getCurrentRoutineContext().setLocalVariable(localVarNumber, value);
      
    } else {
      
      memaccess.writeShort(globalsAddress
          + (getGlobalVariableNumber(variableNumber) * 2), value);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void pushRoutineContext(RoutineContext routineContext) {

    routineContext.setInvocationStackPointer(getStackPointer());
    routineContextStack.add(routineContext);
  }
  
  /**
   * {@inheritDoc}
   */
  public void popRoutineContext(short returnValue) {
    
    if (routineContextStack.size() > 0) {

      RoutineContext popped =
        routineContextStack.remove(routineContextStack.size() - 1);
    
      // Restore stack pointer and pc
      setStackPointer(popped.getInvocationStackPointer());
      programCounter = popped.getReturnAddress();
      int returnVariable = popped.getReturnVariable();
      if (returnVariable != RoutineContext.DISCARD_RESULT) {
        
        setVariable(returnVariable, returnValue);
      }
    } else {
      
      throw new IllegalStateException("no routine context active");
    }
  }

  /**
   * {@inheritDoc}
   */
  public RoutineContext getCurrentRoutineContext() {
    
    if (routineContextStack.size() == 0) return null;
    return routineContextStack.get(routineContextStack.size() - 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<RoutineContext> getRoutineContexts() {
    
    return Collections.unmodifiableList(routineContextStack);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setRoutineContexts(List<RoutineContext> contexts) {

    routineContextStack.clear();
    for (RoutineContext context : contexts) {
      
      routineContextStack.add(context);
    }
  }
  
  /**
   * This function is basically exposed to the debug application.
   * 
   * @return the current routine stack pointer
   */
  public int getRoutineStackPointer() {
    
    return routineContextStack.size();
  }
  
  /**
   * Returns the variable type for the given variable number.
   * 
   * @param variableNumber the variable number
   * @return STACK if stack variable, LOCAL if local variable, GLOBAL if global
   */
  public static VariableType getVariableType(int variableNumber) {
    
    if (variableNumber == 0) return VariableType.STACK;
    else if (variableNumber < 0x10) return VariableType.LOCAL;
    else return VariableType.GLOBAL;
  }

  /**
   * {@inheritDoc}
   */
  public short random(short range) {
    
    if (range < 0) {
      
      random = new PredictableRandomGenerator(-range);
      return 0;
      
    } else if (range == 0) {
      
      random = new UnpredictableRandomGenerator();
      return 0;
    }
    return (short) ((random.next() % range) + 1);
  }

  // **********************************************************************
  // ****** Streams
  // ****************************
  
  /**
   * {@inheritDoc}
   */
  public void setOutputStream(int streamnumber, OutputStream stream) {
    
    outputStream[streamnumber - 1] = stream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void printZString(int address) {
    
    print(new ZString(memaccess, address).toString());
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(String str) {

    printZsciiChars(ZsciiEncoding.getInstance().convertToZscii(str));
  }
  
  /**
   * {@inheritDoc}
   */
  public void newline() {
    
    printZsciiChar(ZsciiEncoding.NEWLINE);
  }
  
  private short[] zchars = new short[1];
  
  /**
   * {@inheritDoc}
   */
  public void printZsciiChar(short zchar) {
    
    zchars[0] = zchar;
    printZsciiChars(zchars);
  }

  /**
   * Prints the specified array of ZSCII characters. This is the only function
   * that communicates with the output streams directly.
   * 
   * @param zchars the array of ZSCII characters.
   */
  private void printZsciiChars(short[] zchars) {
    
    checkTranscriptFlag();
    
    if (outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      for (short zchar : zchars) {
        
        outputStream[OUTPUTSTREAM_MEMORY - 1].print(zchar);
      }
      
    } else {
    
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          for (short zchar : zchars) {
          
            outputStream[i].print(zchar);
          }
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void printNumber(short number) {
    
    print(String.valueOf(number));
  }

  /**
   * Checks the fileheader if the transcript flag was set by the game
   * bypassing output_stream, e.g. with a storeb to the fileheader flags
   * address. Enable the transcript depending on the status of that flag.
   */
  private void checkTranscriptFlag() {
    
    if (outputStream[OUTPUTSTREAM_TRANSCRIPT - 1] != null) {
        
      outputStream[OUTPUTSTREAM_TRANSCRIPT - 1].select(
          fileHeader.isEnabled(Attribute.TRANSCRIPTING));
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectOutputStream(int streamnumber, boolean flag) {
    
    outputStream[streamnumber - 1].select(flag);
    
    // Sets the tranxdQscript flag if the transcipt is specified
    if (streamnumber == OUTPUTSTREAM_TRANSCRIPT) {
      
      //System.out.println("ENABLE_TRANSCRIPT_STREAM: " + flag);
      fileHeader.setEnabled(Attribute.TRANSCRIPTING, flag);
      
    } else if (streamnumber == OUTPUTSTREAM_MEMORY && flag) {
      
      halt("invalid selection of memory stream");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectOutputStream3(int tableAddress) {

    ((MemoryOutputStream) outputStream[OUTPUTSTREAM_MEMORY - 1]).select(
        tableAddress);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setInputStream(int streamnumber, InputStream stream) {
    
    inputStream[streamnumber] = stream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectInputStream(int streamnumber) {
    
    selectedInputStreamIndex = streamnumber;    
    screenModel.setPaging(streamnumber != Machine.INPUTSTREAM_FILE);
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream getSelectedInputStream() {
    
    return inputStream[selectedInputStreamIndex];
  }
  
  /**
   * {@inheritDoc}
   */
  public void readLine(int address, int bufferlen) {
    
    short zsciiChar;
    boolean isAtLeastV5 = getStoryFileHeader().getVersion() >= 5;
    
    // From V5, the first byte contains the number of characters typed
    int pointerstart = isAtLeastV5 ? 1 : 0;
    int pointer = pointerstart;
    
    if (isAtLeastV5) {
      
      // The clunky feature to include previous input into the current input
      // Simply adjust the pointer, the differencing at the end of the
      // function will then calculate the total
      int numCharactersTyped = memaccess.readByte(address);
      if (numCharactersTyped < 0) numCharactersTyped = 0;
      if (numCharactersTyped > 0) System.out.println("leftover input: " + numCharactersTyped);
      pointer += numCharactersTyped;
    }

    do {
      
      zsciiChar = inputStream[selectedInputStreamIndex].readZsciiChar();
      
      // Decrement the buffer pointer
      if (zsciiChar == ZsciiEncoding.DELETE) {
        
        if (pointer > pointerstart) pointer--;
        
      } else if (zsciiChar != ZsciiEncoding.NEWLINE) {
        
        // Do not include the terminator in the buffer
        memaccess.writeByte(address + pointer, (byte) zsciiChar);        
        pointer++;
      }      
      printZsciiChar(zsciiChar);
      
    } while (zsciiChar != ZsciiEncoding.NEWLINE && pointer < bufferlen - 1);

    if (isAtLeastV5) {
    
      // Write the number of characters typed in byte 1
      memaccess.writeUnsignedByte(address, (byte) (pointer - 1));
      
    } else {
      
      // Terminate with 0 byte in versions < 5      
      memaccess.writeByte(address + pointer, (byte) 0);
    }
    
    // Echo a newline into the streams
    printZsciiChar(ZsciiEncoding.NEWLINE);
    
    // debug output:
    /*
    StringBuilder outputbuffer = new StringBuilder();
    if (isAtLeastV5) {
      
      int numCharacters = memaccess.readUnsignedByte(address);
      for (int i = 0; i < numCharacters; i++) {

        outputbuffer.append((char) memaccess.readUnsignedByte(address + i + 1));
      }
      System.out.printf("# chars typed: %d: '%s'\n", numCharacters, outputbuffer.toString());
    }*/
  }
  
  /**
   * {@inheritDoc}
   */
  public short readChar() {
    
    return inputStream[selectedInputStreamIndex].getZsciiChar();
  }
  
  /**
   * {@inheritDoc}
   */
  public int translatePackedAddress(int packedAddress, boolean isCall) {
  
    // Version specific packed address translation
    switch (getStoryFileHeader().getVersion()) {
    
      case 1: case 2: case 3:  
        return packedAddress * 2;
      case 4:
      case 5:
        return packedAddress * 4;
      case 6:
      case 7:
        return packedAddress * 4 + 8 *
          (isCall ? getStoryFileHeader().getRoutineOffset() :
                    getStoryFileHeader().getStaticStringOffset());
      case 8:
      default:
        return packedAddress * 8;
    }
  }
  
  /**
   * {@inheritDoc} 
   */
  public int computeBranchTarget(short offset, int instructionLength) {
        
    return getProgramCounter() + instructionLength + offset - 2;
  }
  
  // ************************************************************************
  // ****** Control functions
  // ************************************************

  /**
   * {@inheritDoc}
   */
  public void playSoundEffect(int soundnum, int effect, int volume,
                              int routine) {
    
    //System.out.println("sound_effect is not implemented yet");
  }
  
  /**
   * {@inheritDoc}
   */
  public void updateStatusLine() {
  
    if (fileHeader.getVersion() <= 3 && statusLine != null) {
      
      int objNum = getVariable(0x10);    
      ZObject obj = getObjectTree().getObject(objNum);      
      String objectName = (new ZString(getMemoryAccess(),
            obj.getPropertiesDescriptionAddress())).toString();
      
      int global2 = getVariable(0x11);
      int global3 = getVariable(0x12);
      if (fileHeader.isEnabled(Attribute.SCORE_GAME)) {
        
        statusLine.updateStatusScore(objectName, global2, global3);
      } else {
        
        statusLine.updateStatusTime(objectName, global2, global3);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setStatusLine(StatusLine statusLine) {
    
    this.statusLine = statusLine;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreen(ScreenModel screen) {
   
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
  public void halt(String errormsg) {
  
    print(errormsg);
    running = false;
  }
  
  /**
   * {@inheritDoc}
   */
  public void warn(String msg) {
    
    System.err.println("WARNING: " + msg);
  }
  
  /**
   * {@inheritDoc} 
   */
  public boolean save(int savepc) {
    
    if (datastore != null) {
      
      PortableGameState gamestate = new PortableGameState();
      gamestate.captureMachineState(this, savepc);
      WritableFormChunk formChunk = gamestate.exportToFormChunk();
      return datastore.saveFormChunk(formChunk);
    }
    
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean save_undo(int savepc) {
    
    undoGameState = new PortableGameState();
    undoGameState.captureMachineState(this, savepc);
    return true;
  }

  /**
   * {@inheritDoc} 
   */
  public PortableGameState restore() {
    
    if (datastore != null) {
      
      PortableGameState gamestate = new PortableGameState();
      FormChunk formchunk = datastore.retrieveFormChunk();
      gamestate.readSaveGame(formchunk);
      
      // verification has to be here
      if (verifySaveGame(gamestate)) {
        
        // do not reset screen model, since e.g. AMFV simply picks up the
        // current window state
        restart(false);
        gamestate.transferStateToMachine(this);
        System.out.printf("restore(), pc is: %4x running: %b\n", getProgramCounter(), isRunning());
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
    if (undoGameState != null) {
      restart(false);
      undoGameState.transferStateToMachine(this);
      System.out.printf("restore(), pc is: %4x\n", getProgramCounter());
      return undoGameState;
    }
    return null;
  }
  
  private boolean verifySaveGame(PortableGameState gamestate) {
    
    // Verify the game according to the standard
    int checksum = fileHeader.getChecksum();
    if (checksum == 0) checksum = calculateChecksum(fileHeader);
    return gamestate.getRelease() == fileHeader.getRelease()
      && gamestate.getChecksum() == checksum
      && gamestate.getSerialNumber().equals(fileHeader.getSerialNumber());
  }
  
  /**
   * {@inheritDoc} 
   */
  public void restart() {

    restart(true);
  }
  
  private void restart(boolean resetScreenModel) {
    
    // Transcripting and fixed font bits survive the restart
    boolean fixedFontForced = fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT);
    boolean transcripting = fileHeader.isEnabled(Attribute.TRANSCRIPTING);
    config.reset();
    resetState();
    if (resetScreenModel) screenModel.reset();    
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, transcripting);
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, fixedFontForced);
  }
  
  /**
   * {@inheritDoc} 
   */
  public void quit() {
    
    running = false;
    
    // On quit, close the streams
    closeStreams();
  }
  
  /**
   * Close the streams.
   */
  private void closeStreams() {
    
    if (inputStream != null) {

      for (int i = 0; i < inputStream.length; i++) {
        
        if (inputStream[i] != null) inputStream[i].close();
      }
    }
    
    if (outputStream != null) {

      for (int i = 0; i < outputStream.length; i++) {
        
        if (outputStream[i] != null) outputStream[i].close();
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isRunning() {
    
    return running;
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    
    running = true;
  }
  
  /**
   * {@inheritDoc}
   */
  public Instruction nextStep() {
    
    Instruction instruction = decoder.decodeInstruction(getProgramCounter());
    return instruction;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setSaveGameDataStore(SaveGameDataStore datastore) {
    
    this.datastore = datastore;
  }
  
  // ************************************************************************
  // ****** Private functions
  // ************************************************
  /**
   * Returns the local variable number for a specified variable number.
   * 
   * @param variableNumber the variable number in an operand (0x01-0x0f)
   * @return the local variable number
   */
  private int getLocalVariableNumber(int variableNumber) {
    
    return variableNumber - 1;
  }
  
  /**
   * Returns the global variable for the specified variable number.
   * 
   * @param variableNumber a variable number (0x10-0xff)
   * @return the global variable number
   */
  private int getGlobalVariableNumber(int variableNumber) {
    
    return variableNumber - 0x10;
  }
  
  /**
   * This function throws an exception if a non-existing local variable
   * is accessed on the current routine context or no current routine context
   * is set.
   * 
   * @param localVariableNumber the local variable number
   */
  private void checkLocalVariableAccess(int localVariableNumber) {
    
    if (routineContextStack.size() == 0) {
      
      throw new IllegalStateException("no routine context set");
    }
    
    if (localVariableNumber >= getCurrentRoutineContext().getNumLocalVariables()) {
      
      throw new IllegalStateException("access to non-existent local variable: "
                                      + localVariableNumber);
    }
  }
}
