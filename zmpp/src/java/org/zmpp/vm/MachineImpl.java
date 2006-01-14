/*
 * $Id$
 * 
 * Created on 10/03/2005
 * Copyright 2005-2006 by Wei-ju Wu
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
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.StoryFileHeader.Attribute;
import org.zmpp.vmutil.PredictableRandomGenerator;
import org.zmpp.vmutil.RandomGenerator;
import org.zmpp.vmutil.UnpredictableRandomGenerator;
import org.zmpp.vmutil.ZCharDecoder;
import org.zmpp.vmutil.ZCharEncoder;
import org.zmpp.vmutil.ZsciiEncoding;

/**
 * This class implements the state and some services of a Z-machine, version 3.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MachineImpl implements Machine, MachineServices {

  /**
   * The configuration object.
   */
  private MachineConfig config;
  
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
   * The save game data store.
   */
  private SaveGameDataStore datastore;
  
  /**
   * The undo game state.
   */
  private PortableGameState undoGameState;
  
  /**
   * The input functions object.
   */
  private InputFunctions inputFunctions;
  
  /**
   * Constructor.
   */
  public MachineImpl() {

    this.inputFunctions = new InputFunctionsImpl(this);
  }
  
  /**
   * {@inheritDoc}
   */
  public MachineServices getServices() {
    
    return this;
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
    
    this.stack = new ArrayList<Short>();
    this.routineContextStack = new ArrayList<RoutineContext>();
    this.programCounter = getStoryFileHeader().getProgramStart();
    this.globalsAddress = getStoryFileHeader().getGlobalsAddress();
    this.decoder.initialize(this, getMemoryAccess());
    int checksum = calculateChecksum(getStoryFileHeader());
    hasValidChecksum = getStoryFileHeader().getChecksum() == checksum;
    getStoryFileHeader().setStandardRevision(1, 0);
    
    if (getStoryFileHeader().getVersion() >= 4) {
            
      getStoryFileHeader().setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
      getStoryFileHeader().setInterpreterNumber(6); // IBM PC
      getStoryFileHeader().setInterpreterVersion(1);
    }
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
    
      sum += getMemoryAccess().readUnsignedByte(i);
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
      
      return getMemoryAccess().readShort(globalsAddress
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
      
      getMemoryAccess().writeShort(globalsAddress
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
      popped.setReturnValue(returnValue);
    
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
    
    print(config.getZCharDecoder().decode2Unicode(getMemoryAccess(),
        address));
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(String str) {

    //System.out.println("print: '" + str + "'");
    printZsciiChars(ZsciiEncoding.getInstance().convertToZscii(str), false);
  }
  
  /**
   * {@inheritDoc}
   */
  public void newline() {
    
    printZsciiChar(ZsciiEncoding.NEWLINE, false);
  }
  
  private short[] zchars = new short[1];
  
  /**
   * {@inheritDoc}
   */
  public void printZsciiChar(short zchar, boolean isInput) {
    
    //System.out.println("printZsciiChar: '" + (char) zchar + "'");
    zchars[0] = zchar;
    printZsciiChars(zchars, isInput);
  }
  
  /**
   * {@inheritDoc}
   */
  public void deletePreviousZsciiChar(short zchar) {
    
    if (!outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
          
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          outputStream[i].deletePrevious(zchar);
        }
      }
    }
  }

  /**
   * Prints the specified array of ZSCII characters. This is the only function
   * that communicates with the output streams directly.
   * 
   * @param zchars the array of ZSCII characters.
   */
  private void printZsciiChars(short[] zchars, boolean isInput) {
    
    checkTranscriptFlag();
    
    if (outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      for (short zchar : zchars) {
        
        outputStream[OUTPUTSTREAM_MEMORY - 1].print(zchar, isInput);
      }
      
    } else {
    
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          for (short zchar : zchars) {
          
            outputStream[i].print(zchar, isInput);
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
  
  public void flushOutput() {
    
    // At the moment flushing only makes sense for screen
    if (!outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          outputStream[i].flush();
        }
      }
    }
  }

  /**
   * Checks the fileheader if the transcript flag was set by the game
   * bypassing output_stream, e.g. with a storeb to the fileheader flags
   * address. Enable the transcript depending on the status of that flag.
   */
  private void checkTranscriptFlag() {
    
    if (outputStream[OUTPUTSTREAM_TRANSCRIPT - 1] != null) {
        
      outputStream[OUTPUTSTREAM_TRANSCRIPT - 1].select(
          getStoryFileHeader().isEnabled(Attribute.TRANSCRIPTING));
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
      getStoryFileHeader().setEnabled(Attribute.TRANSCRIPTING, flag);
      
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
  public InputFunctions getInputFunctions() {
    
    return inputFunctions;
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
  
    if (getStoryFileHeader().getVersion() <= 3 && statusLine != null) {
      
      int objNum = getVariable(0x10);    
      ZObject obj = getObjectTree().getObject(objNum);
      String objectName = config.getZCharDecoder().decode2Unicode(
          getMemoryAccess(), obj.getPropertiesDescriptionAddress());      
      int global2 = getVariable(0x11);
      int global3 = getVariable(0x12);
      if (getStoryFileHeader().isEnabled(Attribute.SCORE_GAME)) {
        
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
    StoryFileHeader fileHeader = getStoryFileHeader();
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
    StoryFileHeader fileHeader = getStoryFileHeader();
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
    print("*Game ended*");
    closeStreams();
    screenModel.redraw();
  }
  
  /**
   * Close the streams.
   */
  private void closeStreams() {
    
    if (inputStream != null) {

      for (int i = 0; i < inputStream.length; i++) {
        
        if (inputStream[i] != null) {
          
          inputStream[i].close();
        }
      }
    }
    
    if (outputStream != null) {

      for (int i = 0; i < outputStream.length; i++) {
        
        if (outputStream[i] != null) {
          
          outputStream[i].flush();
          outputStream[i].close();
        }
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
  
  public RoutineContext call(int packedRoutineAddress, int returnAddress,
      short[] args, short returnVariable) {
    
    int routineAddress =
      translatePackedAddress(packedRoutineAddress, true);
    int numArgs = args != null ? args.length : 0;
    
    RoutineContext routineContext = decodeRoutine(routineAddress);
    
    // Sets the number of arguments
    routineContext.setNumArguments(numArgs);
    
    // Save return parameters
    routineContext.setReturnAddress(returnAddress);
    
    // Only if this instruction stores a result
    if (returnVariable != RoutineContext.DISCARD_RESULT) {
      
      routineContext.setReturnVariable(returnVariable);
      
    } else {
      
      routineContext.setReturnVariable(RoutineContext.DISCARD_RESULT);
    }      
    
    // Set call parameters into the local variables
    // if there are more parameters than local variables,
    // those are thrown away
    int numToCopy = Math.min(routineContext.getNumLocalVariables(),
        numArgs);
    
    for (int i = 0; i < numToCopy; i++) {
      
      routineContext.setLocalVariable(i, args[i]);
    }
    
    // save invocation stack pointer
    routineContext.setInvocationStackPointer(getStackPointer());
    
    // Pushes the routine context onto the routine stack
    pushRoutineContext(routineContext);
    
    // Jump to the address
    setProgramCounter(routineContext.getStartAddress());
    return routineContext;
  }

  // ************************************************************************
  // ****** Machine services
  // ************************************************
  
  /**
   * {@inheritDoc}
   */
  public MemoryAccess getMemoryAccess() {
    
    return config.getMemoryAccess();
  }
  
  /**
   * {@inheritDoc}
   */
  public Dictionary getDictionary() {
    
    return config.getDictionary();
  }
  
  public ObjectTree getObjectTree() {
    
    return config.getObjectTree();
  }

  /**
   * {@inheritDoc}
   */
  public StoryFileHeader getStoryFileHeader() {
    
    return config.getStoryFileHeader();
  }
  
  /**
   * {@inheritDoc}
   */
  public ZCharDecoder getZCharDecoder() {
    
    return config.getZCharDecoder();
  }
  
  /**
   * {@inheritDoc}
   */
  public ZCharEncoder getZCharEncoder() {
    
    return config.getZCharEncoder();
  }    
  
  // ************************************************************************
  // ****** Private functions
  // ************************************************
  
  /**
   * Decodes the routine at the specified address.
   * 
   * @param routineAddress the routine address
   * @return a RoutineContext object
   */
  private RoutineContext decodeRoutine(int routineAddress) {

    MemoryAccess memaccess = getMemoryAccess();    
    int numLocals = memaccess.readUnsignedByte(routineAddress);
    short[] locals = new short[numLocals];
    int currentAddress = routineAddress + 1;
    
    if (getStoryFileHeader().getVersion() <= 4) {
      
      // Only story files <= 4 actually store default values here,
      // after V5 they are assumed as being 0 (standard document 1.0, S.5.2.1) 
      for (int i = 0; i < numLocals; i++) {
      
        locals[i] = memaccess.readShort(currentAddress);
        currentAddress += 2;
      }
    }
    //System.out.printf("setting routine start to: %x\n", currentAddress);
    
    RoutineContext info = new RoutineContext(currentAddress, numLocals);
    
    for (int i = 0; i < numLocals; i++) {
      
      info.setLocalVariable(i, locals[i]);
    }
    return info;
  }
    
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
