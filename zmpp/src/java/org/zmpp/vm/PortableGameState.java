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
import java.util.List;

import org.zmpp.base.MemoryAccess;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;

/**
 * This class represents the state of the Z machine in an external format,
 * so it can be exchanged using the Quetzal IFF format.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class PortableGameState {

  /**
   * This class represents a stack frame in the portable game state model.
   */
  public static class StackFrame {
  
    /**
     * The return program counter.
     */
    int pc;
    
    /**
     * The return variable.
     */
    int returnVariable;
    
    /**
     * The local variables.
     */
    short[] locals;
    
    /**
     * The evaluation stack.
     */
    short[] evalStack;
    
    /**
     * The arguments.
     */
    int[] args;
    
    public int getProgramCounter() { return pc; }    
    public int getReturnVariable() { return returnVariable; }
    public short[] getEvalStack() { return evalStack; }
    public short[] getLocals() { return locals; }
    public int[] getArgs() { return args; }
    
    public void setProgramCounter(int pc) { this.pc = pc; }
    public void setReturnVariable(int varnum) { this.returnVariable = varnum; }
    public void setEvalStack(short[] stack) { this.evalStack = stack; }
    public void setLocals(short[] locals) { this.locals = locals; }
    public void setArgs(int[] args) { this.args = args; }
  }

  /**
   * The release number.
   */
  private int release;
  
  /**
   * The story file checksum.
   */
  private int checksum;
  
  /**
   * The serial number.
   */
  private byte[] serialBytes;
  
  /**
   * The program counter.
   */
  private int pc;
  
  /**
   * The uncompressed dynamic memory.
   */
  private byte[] dynamicMem;
  
  /**
   * The delta.
   */
  private byte[] delta;
  
  /**
   * The list of stack frames in this game state, from oldest to latest.
   */
  private List<StackFrame> stackFrames;
  
  /**
   * Constructor.
   */
  public PortableGameState() {
    
    serialBytes = new byte[6];
    stackFrames = new ArrayList<StackFrame>();
  }
  
  // **********************************************************************
  // ***** Accessing the state
  // *******************************************
  
  /**
   * Returns the game release number.
   * 
   * @return the release number
   */
  public int getRelease() { return release; }
  
  /**
   * Returns the game checksum.
   * 
   * @return the checksum
   */
  public int getChecksum() { return checksum; }
  
  /**
   * Returns the game serial number.
   * 
   * @return the serial number
   */
  public String getSerialNumber() { return new String(serialBytes); }
  
  /**
   * Returns the program counter.
   * 
   * @return the program counter
   */
  public int getProgramCounter() { return pc; }
  
  /**
   * Returns the list of stack frames.
   * 
   * @return the stack frames
   */
  public List<StackFrame> getStackFrames() {
    
    return stackFrames;
  }
  
  /**
   * Returns the delta bytes. This is the changes in dynamic memory, where
   * 0 represents no change.
   * 
   * @return the delta bytes
   */
  public byte[] getDeltaBytes() {
    
    return delta;
  }
  
  /**
   * Returns the current dump of dynamic memory captured from a Machine object.
   * 
   * @return the dynamic memory dump
   */
  public byte[] getDynamicMemoryDump() {
    
    return dynamicMem;
  }
  
  public void setRelease(int release) {
    
    this.release = release;
  }
  
  public void setChecksum(int checksum) {
    
    this.checksum = checksum;
  }
  
  public void setSerialNumber(String serial) {
    
    this.serialBytes = serial.getBytes();
  }
  
  public void setProgramCounter(int pc) {
    
    this.pc = pc;
  }
  
  public void setDynamicMem(byte[] memdata) {
    
    this.dynamicMem = memdata;
  }
  
  // **********************************************************************
  // ***** Reading the state from a file
  // *******************************************
  /**
   * Initialize the state from an IFF form.
   * 
   * @param formChunk the IFF form
   * @return false if there was a consistency problem during the read
   */
  public boolean readSaveGame(FormChunk formChunk) {
    
    stackFrames.clear();
    
    if ((new String(formChunk.getSubId())).equals("IFZS")) {
      
      readIfhdChunk(formChunk);
      readStacksChunk(formChunk);
      readMemoryChunk(formChunk);
      
      return true;
    }
    return false;
  }
  
  /**
   * Evaluate the contents of the IFhd chunk.
   * 
   * @param formChunk the FORM chunk
   */
  private void readIfhdChunk(FormChunk formChunk) {
    
    Chunk ifhdChunk = formChunk.getSubChunk("IFhd".getBytes());
    MemoryAccess chunkMem = ifhdChunk.getMemoryAccess();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    
    // read release number
    release = chunkMem.readUnsignedShort(offset);
    offset += 2;
    
    // read serial number
    for (int i = 0; i < 6; i++) {
      
      serialBytes[i] = chunkMem.readByte(offset + i);
    }
    offset += 6;
    
    // read check sum
    checksum = chunkMem.readUnsignedShort(offset);
    offset += 2;

    // read pc
    pc = decodePcBytes(chunkMem.readByte(offset), chunkMem.readByte(offset + 1),
        chunkMem.readByte(offset + 2));
  }
  
  /**
   * Evalutate the contents of the Stks chunk.
   * 
   * @param formChunk the FORM chunk
   */
  private void readStacksChunk(FormChunk formChunk) {
    
    Chunk stksChunk = formChunk.getSubChunk("Stks".getBytes());
    MemoryAccess chunkMem = stksChunk.getMemoryAccess();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    int chunksize = stksChunk.getSize() + Chunk.CHUNK_HEADER_LENGTH;
    
    while (offset < chunksize) {
      
      StackFrame stackFrame = new StackFrame();
      stackFrame.pc = decodePcBytes(chunkMem.readByte(offset),
        chunkMem.readByte(offset + 1), chunkMem.readByte(offset + 2));
      offset += 3;
    
      byte pvFlags = chunkMem.readByte(offset++);
      int numLocals = getNumLocals(pvFlags);
      stackFrame.locals = new short[numLocals];
    
      stackFrame.returnVariable = chunkMem.readByte(offset++);
    
      byte argSpec = chunkMem.readByte(offset++);
      stackFrame.args = getArgs(argSpec);
    
      int evalStackSize = chunkMem.readUnsignedShort(offset);
      /*
      System.out.println("pc: " + stackFrame.pc + " numLocals: " + numLocals + ", evalStackSize: " + evalStackSize
          + " retvar: " + stackFrame.returnVariable);
          */
      stackFrame.evalStack = new short[evalStackSize];
      offset += 2;
    
      // Read local variables
      for (int i = 0; i < numLocals; i++) {
      
        stackFrame.locals[i] = chunkMem.readShort(offset);
        offset += 2;
      }
    
      // Read evaluation stack values
      for (int i = 0; i < evalStackSize; i++) {
      
        stackFrame.evalStack[i] = chunkMem.readShort(offset);
        offset += 2;
      }
      stackFrames.add(stackFrame);
    }    
  }    
  
  /**
   * Evaluate the contents of the Cmem and the UMem chunks.
   * 
   * @param formChunk the FORM chunk
   */
  private void readMemoryChunk(FormChunk formChunk) {
    
    Chunk cmemChunk = formChunk.getSubChunk("CMem".getBytes());
    Chunk umemChunk = formChunk.getSubChunk("UMem".getBytes());
    if (cmemChunk != null) {
     
      readCMemChunk(cmemChunk);
      
    } else if (umemChunk != null) {
     
      readUMemChunk(umemChunk);
    }
  }
  
  /**
   * Decompresses and reads the dynamic memory state.
   * 
   * @param cmemChunk the CMem chunk
   */
  private void readCMemChunk(Chunk cmemChunk) {
    
    MemoryAccess chunkMem = cmemChunk.getMemoryAccess();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    int chunksize = cmemChunk.getSize() + Chunk.CHUNK_HEADER_LENGTH;
    List<Byte> byteBuffer = new ArrayList<Byte>();
    
    byte b;
    
    while (offset < chunksize) {
      
      b = chunkMem.readByte(offset++);
      if (b == 0) {
        
        short runlength = chunkMem.readUnsignedByte(offset++);
        
        for (int r = 0; r <= runlength; r++) { // (runlength + 1) iterations
          
          byteBuffer.add((byte) 0);
        }
      } else {
        
        byteBuffer.add(b);
      }
    }
    
    // Copy the results to the delta array
    delta = new byte[byteBuffer.size()];
    for (int i = 0; i < delta.length; i++) {
      
      delta[i] = byteBuffer.get(i);
    }    
  }
  
  /**
   * Reads the uncompressed dynamic memory state.
   * 
   * @param umemChunk the UMem chunk
   */
  private void readUMemChunk(Chunk umemChunk) {
    
    MemoryAccess chunkMem = umemChunk.getMemoryAccess();
    int datasize = umemChunk.getSize();
    
    dynamicMem = new byte[datasize];
    for (int i = 0; i < datasize; i++) {
     
      dynamicMem[i] = chunkMem.readByte(i + Chunk.CHUNK_HEADER_LENGTH); 
    }    
  }
  
  // **********************************************************************
  // ***** Reading the state from a Machine
  // *******************************************
  
  /**
   * Makes a snapshot of the current machine state. The savePc argument
   * is taken as the restore program counter.
   * 
   * @param machine a Machine
   * @param savePc the program counter restore value
   */
  public void captureMachineState(Machine machine, int savePc) {
    
    StoryFileHeader fileheader = machine.getStoryFileHeader();
    release = fileheader.getRelease();
    checksum = fileheader.getChecksum();
    serialBytes = fileheader.getSerialNumber().getBytes();
    pc = savePc;
    
    // capture dynamic memory which ends at address(staticsMem) - 1
    // uncompressed
    MemoryAccess memaccess = machine.getMemoryAccess();
    int staticMemStart = fileheader.getStaticsAddress();
    dynamicMem = new byte[staticMemStart];
    
    for (int i = 0; i < staticMemStart; i++) {
      
      dynamicMem[i] = memaccess.readByte(i);
    }

    captureStackFrames(machine);
  }
  
  /**
   * Read the list of RoutineContexts in Machine, convert them to StackFrames,
   * prepending a dummy stack frame.
   * 
   * @param machine the machine object
   */
  private void captureStackFrames(Machine machine) {
    
    List<RoutineContext> contexts = machine.getRoutineContexts();

    // Put in initial dummy stack frame
    StackFrame dummyFrame = new StackFrame();
    dummyFrame.args = new int[0];
    dummyFrame.locals = new short[0];
    int numElements = calculateNumStackElements(machine, contexts, 0, 0);
    dummyFrame.evalStack = new short[numElements];
    for (int i = 0; i < numElements; i++) {
      
      dummyFrame.evalStack[i] = machine.getStackElement(i);
    }
    stackFrames.add(dummyFrame);
    
    // Write out stack frames
    for (int c = 0; c < contexts.size(); c++) {

      RoutineContext context = contexts.get(c);
      
      StackFrame stackFrame = new StackFrame();
      stackFrame.pc = context.getReturnAddress();
      stackFrame.returnVariable = context.getReturnVariable();
      
      // Copy local variables
      stackFrame.locals = new short[context.getNumLocalVariables()];
      for (int i = 0; i < stackFrame.locals.length; i++) {
        
        stackFrame.locals[i] = context.getLocalVariable(i);
      }
      
      // Create argument array
      stackFrame.args = new int[context.getNumArguments()];
      for (int i = 0; i < stackFrame.args.length; i++) {
        
        stackFrame.args[i] = i;
      }
      
      // Transfer evaluation stack
      int localStackStart = context.getInvocationStackPointer();
      numElements = calculateNumStackElements(machine, contexts, c + 1,
          localStackStart);
      stackFrame.evalStack = new short[numElements];
      for (int i = 0; i < numElements; i++) {
        
        stackFrame.evalStack[i] = machine.getStackElement(localStackStart + i);
      }
      
      stackFrames.add(stackFrame);
    }
  }
  
  /**
   * Determines the number of stack elements between localStackStart and
   * the invocation stack pointer of the specified routine context.
   * If contextIndex is greater than the size of the List contexts, the
   * functions assumes this is the top routine context and therefore
   * calculates the difference between the current stack pointer and
   * localStackStart.
   *  
   * @param machine the Machine object
   * @param contexts a list of RoutineContext
   * @param contextIndex the index of the context to calculate the difference
   * @param localStackStart the local stack start pointer
   * @return the number of stack elements in the specified stack frame
   */
  private int calculateNumStackElements(Machine machine,
      List<RoutineContext> contexts, int contextIndex, int localStackStart) {
    
    if (contextIndex < contexts.size()) {
      
      RoutineContext context = contexts.get(contextIndex);
      return context.getInvocationStackPointer() - localStackStart;
      
    } else {
      
      return machine.getStackPointer() - localStackStart; 
    }
  }
  
  // ***********************************************************************
  // ******* Export to an IFF FORM chunk
  // *****************************************
  
  /**
   * Exports the current object state to a FormChunk.
   * 
   * @return the state as a FormChunk
   */
  public WritableFormChunk exportToFormChunk() {
    
    byte[] id = "IFZS".getBytes();
    WritableFormChunk formChunk = new WritableFormChunk(id);
    formChunk.addChunk(createIfhdChunk());
    formChunk.addChunk(createUMemChunk());
    formChunk.addChunk(createStksChunk());
    
    return formChunk;
  }
  
  private Chunk createIfhdChunk() {

    byte[] id = "IFhd".getBytes();
    byte[] data = new byte[13];
    Chunk chunk = new DefaultChunk(id, data);    
    MemoryAccess chunkmem = chunk.getMemoryAccess();
    
    // Write release number
    chunkmem.writeUnsignedShort(8, (short) release);
    
    for (int i = 0; i < serialBytes.length; i++) {
      
      chunkmem.writeByte(10 + i, serialBytes[i]);
    }
    chunkmem.writeUnsignedShort(16, checksum);

    chunkmem.writeByte(18, (byte) ((pc >>> 16) & 0xff));
    chunkmem.writeByte(19, (byte) ((pc >>> 8) & 0xff));
    chunkmem.writeByte(20, (byte) (pc & 0xff));
    
    return chunk;
  }
  
  private Chunk createUMemChunk() {
    
    byte[] id = "UMem".getBytes();
    return new DefaultChunk(id, dynamicMem);
  }
  
  private Chunk createStksChunk() {
    
    byte[] id = "Stks".getBytes();
    List<Byte> byteBuffer = new ArrayList<Byte>();
    
    for (StackFrame stackFrame : stackFrames) {
     
      // returnpc
      int pc = stackFrame.pc;
      byteBuffer.add((byte) ((pc >>> 16) & 0xff));
      byteBuffer.add((byte) ((pc >>> 8) & 0xff));
      byteBuffer.add((byte) (pc & 0xff));
      
      // locals flag, is simply the number of local variables
      byteBuffer.add((byte) (stackFrame.locals.length & 0x0f));
      
      // returnvar
      byteBuffer.add((byte) stackFrame.returnVariable);
      
      // argspec
      byteBuffer.add(createArgSpecByte(stackFrame.args));
      
      // eval stack size
      int stacksize = stackFrame.evalStack.length;
      addUnsignedShortToByteBuffer(byteBuffer, stacksize);
      
      // local variables
      for (short local : stackFrame.locals) {
        
        addShortToByteBuffer(byteBuffer, local);
      }
      
      // stack values
      for (short stackValue : stackFrame.evalStack) {
        
        addShortToByteBuffer(byteBuffer, stackValue);
      }
    }
    
    byte[] data = new byte[byteBuffer.size()];
    for (int i = 0; i < data.length; i++) {
      
      data[i] = byteBuffer.get(i);
    }    
    return new DefaultChunk(id, data);
  }
  
  private void addUnsignedShortToByteBuffer(List<Byte> buffer, int value) {
    
    buffer.add((byte) ((value & 0xff00) >> 8));
    buffer.add((byte) (value & 0xff));
  }
  
  private void addShortToByteBuffer(List<Byte> buffer, short value) {
    
    buffer.add((byte) ((value & 0xff00) >>> 8));
    buffer.add((byte) (value & 0xff));
  }
  
  private byte createArgSpecByte(int[] args) {
    
    byte result = 0;
    for (int arg : args) {
      
      result |= (1 << arg);
    }
    return result;
  }
  
  // ***********************************************************************
  // ******* Transfer to Machine object
  // *****************************************
  
  /**
   * Transfers the current object state to the specified Machine object.
   * 
   * @param machine a Machine object
   */
  public void transferStateToMachine(Machine machine) {
    
    // TODO
  }
  
  // ***********************************************************************
  // ******* Helpers
  // *****************************************
  
  /**
   * There is no apparent reason at the moment to implement getArgs().
   *  
   * @param argspec the argspec byte
   * @return the specified arguments
   */
  private int[] getArgs(byte argspec) {
    
    //System.out.println("argSpec: " + argspec);
    int andBit;
    List<Integer> result = new ArrayList<Integer>();
    
    for (int i = 0; i < 7; i++) {
      
      andBit = 1 << i;
      if ((andBit & argspec) > 0) result.add(i);
      
    }
    int[] intArray = new int[result.size()];
    for (int i = 0; i < result.size(); i++) {
      
      intArray[i] = result.get(i);
    }
    return intArray;
  }
  
  /**
   * Mask out the lower 4 bits if Bit 5 is cleared.
   * 
   * @param pvFlags a bit mask of the form xxxpvvvv
   * @return 0000vvvv if p is cleared, 0 otherwise
   */
  private int getNumLocals(byte pvFlags) {
   
    if ((pvFlags & 0x10) == 0) {
     
      return pvFlags & 0x0f;
    }
    return 0;
  }

  /**
   * Joins three bytes to a program counter value.
   * 
   * @param b0 byte 0
   * @param b1 byte 1
   * @param b2 byte 2
   * @return the resulting program counter
   */
  private int decodePcBytes(byte b0, byte b1, byte b2) {
    
    return ((b0 & 0xff) << 16) | ((b1 & 0xff) << 8) | (b2 & 0xff);
  }
}
