/*
 * $Id$
 * 
 * Created on 10/03/2005
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

import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.toUnsigned16;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;

/**
 * This class represents the state of the Z machine in an external format,
 * so it can be exchanged using the Quetzal IFF format.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class PortableGameState {

  /** The return variable value for discard result. */
  public static final int DISCARD_RESULT = -1;
  
  /**
   * This class represents a stack frame in the portable game state model.
   */
  public static class StackFrame {
  
    /** The return program counter. */
    int pc;
    
    /** The return variable. */
    int returnVariable;
    
    /** The local variables. */
    char[] locals;
    
    /** The evaluation stack. */
    char[] evalStack;
    
    /** The arguments. */
    int[] args;
    
    public int getProgramCounter() { return pc; }    
    public int getReturnVariable() { return returnVariable; }
    public char[] getEvalStack() { return evalStack; }
    public char[] getLocals() { return locals; }
    public int[] getArgs() { return args; }
    
    public void setProgramCounter(final int pc) { this.pc = pc; }
    public void setReturnVariable(final int varnum) {
      this.returnVariable = varnum;
    }
    public void setEvalStack(final char[] stack) { this.evalStack = stack; }
    public void setLocals(final char[] locals) { this.locals = locals; }
    public void setArgs(final int[] args) { this.args = args; }
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
    
    super();
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
  public List<StackFrame> getStackFrames() { return stackFrames; }
  
  /**
   * Returns the delta bytes. This is the changes in dynamic memory, where
   * 0 represents no change.
   * 
   * @return the delta bytes
   */
  public byte[] getDeltaBytes() { return delta; }
  
  /**
   * Returns the current dump of dynamic memory captured from a Machine object.
   * 
   * @return the dynamic memory dump
   */
  public byte[] getDynamicMemoryDump() { return dynamicMem; }
  
  public void setRelease(final int release) {
    
    this.release = release;
  }
  
  public void setChecksum(final int checksum) { this.checksum = checksum; }
  
  public void setSerialNumber(final String serial) {
    this.serialBytes = serial.getBytes();
  }
  
  public void setProgramCounter(final int pc) { this.pc = pc; }
  
  public void setDynamicMem(final byte[] memdata) { this.dynamicMem = memdata; }
  
  // **********************************************************************
  // ***** Reading the state from a file
  // *******************************************
  /**
   * Initialize the state from an IFF form.
   * 
   * @param formChunk the IFF form
   * @return false if there was a consistency problem during the read
   */
  public boolean readSaveGame(final FormChunk formChunk) {
    stackFrames.clear();
    if (formChunk != null
        && (new String(formChunk.getSubId())).equals("IFZS")) {
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
  private void readIfhdChunk(final FormChunk formChunk) {
    final Chunk ifhdChunk = formChunk.getSubChunk("IFhd".getBytes());
    final Memory chunkMem = ifhdChunk.getMemory();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    
    // read release number
    release = chunkMem.readUnsigned16(offset);
    offset += 2;
    
    // read serial number
    chunkMem.copyBytesToArray(serialBytes, 0, offset, 6);
    offset += 6;
    
    // read check sum
    checksum = chunkMem.readUnsigned16(offset);
    offset += 2;

    // read pc
    pc = decodePcBytes(chunkMem.readUnsigned8(offset),
                       chunkMem.readUnsigned8(offset + 1),
                       chunkMem.readUnsigned8(offset + 2));
  }
  
  /**
   * Evaluate the contents of the Stks chunk.
   * 
   * @param formChunk the FORM chunk
   */
  private void readStacksChunk(final FormChunk formChunk) {
    final Chunk stksChunk = formChunk.getSubChunk("Stks".getBytes());
    final Memory chunkMem = stksChunk.getMemory();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    final int chunksize = stksChunk.getSize() + Chunk.CHUNK_HEADER_LENGTH;
    
    while (offset < chunksize) {
      final StackFrame stackFrame = new StackFrame();
      offset = readStackFrame(stackFrame, chunkMem, offset);
      stackFrames.add(stackFrame);
    }    
  }

  /**
   * Reads a stack frame from the specified chunk at the specified
   * offset.
   * @param stackFrame the stack frame to set the data into
   * @param chunkMem the Stks chunk to read from
   * @param offset the offset to read the stack
   * @return the offset after reading the stack frame
   */
  public int readStackFrame(final StackFrame stackFrame,
                            final Memory chunkMem,
                            final int offset) {
    int tmpoff = offset;
    stackFrame.pc = decodePcBytes(chunkMem.readUnsigned8(tmpoff),
                                  chunkMem.readUnsigned8(tmpoff + 1),
                                  chunkMem.readUnsigned8(tmpoff + 2));
    tmpoff += 3;
  
    final byte pvFlags = (byte) (chunkMem.readUnsigned8(tmpoff++) & 0xff);
    final int numLocals = pvFlags & 0x0f;
    final boolean discardResult = (pvFlags & 0x10) > 0;
    stackFrame.locals = new char[numLocals];
  
    // Read the return variable, ignore the result if DISCARD_RESULT
    final int returnVar = chunkMem.readUnsigned8(tmpoff++);
    stackFrame.returnVariable = discardResult ? DISCARD_RESULT :
                                                returnVar;
    final byte argSpec = (byte) (chunkMem.readUnsigned8(tmpoff++) & 0xff);
    stackFrame.args = getArgs(argSpec);
    final int evalStackSize = chunkMem.readUnsigned16(tmpoff);
    stackFrame.evalStack = new char[evalStackSize];
    tmpoff += 2;
  
    // Read local variables
    for (int i = 0; i < numLocals; i++) {
      stackFrame.locals[i] = chunkMem.readUnsigned16(tmpoff);
      tmpoff += 2;
    }
  
    // Read evaluation stack values
    for (int i = 0; i < evalStackSize; i++) {
      stackFrame.evalStack[i] = chunkMem.readUnsigned16(tmpoff);
      tmpoff += 2;
    }
    return tmpoff;
  }
  
  
  /**
   * Evaluate the contents of the Cmem and the UMem chunks.
   * 
   * @param formChunk the FORM chunk
   */
  private void readMemoryChunk(final FormChunk formChunk) {
    final Chunk cmemChunk = formChunk.getSubChunk("CMem".getBytes());
    final Chunk umemChunk = formChunk.getSubChunk("UMem".getBytes());
    if (cmemChunk != null) { readCMemChunk(cmemChunk); }
    if (umemChunk != null) { readUMemChunk(umemChunk); }
  }
  
  /**
   * Decompresses and reads the dynamic memory state.
   * @param cmemChunk the CMem chunk
   */
  private void readCMemChunk(final Chunk cmemChunk) {    
    final Memory chunkMem = cmemChunk.getMemory();
    int offset = Chunk.CHUNK_HEADER_LENGTH;
    final int chunksize = cmemChunk.getSize() + Chunk.CHUNK_HEADER_LENGTH;
    final List<Byte> byteBuffer = new ArrayList<Byte>();
    char b;
    
    while (offset < chunksize) {
      b = chunkMem.readUnsigned8(offset++);
      if (b == 0) {
        final char runlength = chunkMem.readUnsigned8(offset++);
        for (int r = 0; r <= runlength; r++) { // (runlength + 1) iterations
          byteBuffer.add((byte) 0);
        }
      } else {
        byteBuffer.add((byte) (b & 0xff));
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
   * @param umemChunk the UMem chunk
   */
  private void readUMemChunk(final Chunk umemChunk) {
    final Memory chunkMem = umemChunk.getMemory();
    final int datasize = umemChunk.getSize();
    dynamicMem = new byte[datasize];
    chunkMem.copyBytesToArray(dynamicMem, 0, Chunk.CHUNK_HEADER_LENGTH,
                              datasize);
  }
  
  // **********************************************************************
  // ***** Reading the state from a Machine
  // *******************************************
  
  /**
   * Makes a snapshot of the current machine state. The savePc argument
   * is taken as the restore program counter.
   * @param machine a Machine
   * @param savePc the program counter restore value
   */
  public void captureMachineState(final Machine machine, final int savePc) {
    final StoryFileHeader fileheader = machine.getFileHeader();
    release = fileheader.getRelease();
    checksum = fileheader.getChecksum();
    serialBytes = fileheader.getSerialNumber().getBytes();
    pc = savePc;
    
    // capture dynamic memory which ends at address(staticsMem) - 1
    // uncompressed
    final int staticMemStart = fileheader.getStaticsAddress();
    dynamicMem = new byte[staticMemStart];
    // Save the state of dynamic memory
    machine.copyBytesToArray(dynamicMem, 0, 0, staticMemStart);
    captureStackFrames(machine);
  }
  
  /**
   * Read the list of RoutineContexts in Machine, convert them to StackFrames,
   * prepending a dummy stack frame.
   * @param machine the machine object
   */
  private void captureStackFrames(final Machine machine) {    
    final List<RoutineContext> contexts = machine.getRoutineContexts();
    // Put in initial dummy stack frame
    final StackFrame dummyFrame = new StackFrame();
    dummyFrame.args = new int[0];
    dummyFrame.locals = new char[0];
    int numElements = calculateNumStackElements(machine, contexts, 0, 0);
    dummyFrame.evalStack = new char[numElements];
    for (int i = 0; i < numElements; i++) {
      dummyFrame.evalStack[i] = machine.getStackElement(i);
    }
    stackFrames.add(dummyFrame);
    
    // Write out stack frames
    for (int c = 0; c < contexts.size(); c++) {
      final RoutineContext context = contexts.get(c);
      
      final StackFrame stackFrame = new StackFrame();
      stackFrame.pc = context.getReturnAddress();
      stackFrame.returnVariable = context.getReturnVariable();
      
      // Copy local variables
      stackFrame.locals = new char[context.getNumLocalVariables()];
      for (int i = 0; i < stackFrame.locals.length; i++) {
        stackFrame.locals[i] = context.getLocalVariable(i);
      }
      
      // Create argument array
      stackFrame.args = new int[context.getNumArguments()];
      for (int i = 0; i < stackFrame.args.length; i++) {
        stackFrame.args[i] = i;
      }
      
      // Transfer evaluation stack
      final int localStackStart = context.getInvocationStackPointer();
      numElements = calculateNumStackElements(machine, contexts, c + 1,
          localStackStart);
      stackFrame.evalStack = new char[numElements];
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
  private int calculateNumStackElements(final Machine machine,
      final List<RoutineContext> contexts, final int contextIndex,
      final int localStackStart) {
    
    if (contextIndex < contexts.size()) {
      final RoutineContext context = contexts.get(contextIndex);
      return context.getInvocationStackPointer() - localStackStart;
    } else {
      return machine.getSP() - localStackStart; 
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
    final byte[] id = "IFZS".getBytes();
    final WritableFormChunk formChunk = new WritableFormChunk(id);
    formChunk.addChunk(createIfhdChunk());
    formChunk.addChunk(createUMemChunk());
    formChunk.addChunk(createStksChunk());
    
    return formChunk;
  }
  
  private Chunk createIfhdChunk() {
    final byte[] id = "IFhd".getBytes();
    final byte[] data = new byte[13];
    final Chunk chunk = new DefaultChunk(id, data);    
    final Memory chunkmem = chunk.getMemory();
    
    // Write release number
    chunkmem.writeUnsigned16(8, toUnsigned16(release));

    // Copy serial bytes
    chunkmem.copyBytesFromArray(serialBytes, 0, 10, serialBytes.length);
    chunkmem.writeUnsigned16(16, toUnsigned16(checksum));
    chunkmem.writeUnsigned8(18, (char) ((pc >>> 16) & 0xff));
    chunkmem.writeUnsigned8(19, (char) ((pc >>> 8) & 0xff));
    chunkmem.writeUnsigned8(20, (char) (pc & 0xff));
    
    return chunk;
  }
  
  private Chunk createUMemChunk() {
    final byte[] id = "UMem".getBytes();
    return new DefaultChunk(id, dynamicMem);
  }
  
  private Chunk createStksChunk() {    
    final byte[] id = "Stks".getBytes();
    final List<Byte> byteBuffer = new ArrayList<Byte>();
    
    for (StackFrame stackFrame : stackFrames) {
      writeStackFrameToByteBuffer(byteBuffer, stackFrame);
    }
    final byte[] data = new byte[byteBuffer.size()];
    for (int i = 0; i < data.length; i++) {
      data[i] = byteBuffer.get(i);
    }    
    return new DefaultChunk(id, data);
  }
  
  /**
   * Writes the specified stackframe to the given byte buffer.
   * 
   * @param byteBuffer a byte buffer
   * @param stackFrame the stack frame
   */
  public void writeStackFrameToByteBuffer(final List<Byte> byteBuffer,
                                          final StackFrame stackFrame) {    
    final int returnPC = stackFrame.pc;
    byteBuffer.add((byte) ((returnPC >>> 16) & 0xff));
    byteBuffer.add((byte) ((returnPC >>> 8) & 0xff));
    byteBuffer.add((byte) (returnPC & 0xff));
    
    // locals flag, is simply the number of local variables
    final boolean discardResult = stackFrame.returnVariable == DISCARD_RESULT;
    byte pvFlag = (byte) (stackFrame.locals.length & 0x0f);
    if (discardResult) { pvFlag |= 0x10; }
    byteBuffer.add(pvFlag);
    
    // returnvar
    byteBuffer.add((byte) (discardResult ? 0 : stackFrame.returnVariable));
    
    // argspec
    byteBuffer.add(createArgSpecByte(stackFrame.args));
    
    // eval stack size
    final int stacksize = stackFrame.evalStack.length;
    addUnsignedShortToByteBuffer(byteBuffer, stacksize);
    
    // local variables
    for (char local : stackFrame.locals) {
      addUnsigned16ToByteBuffer(byteBuffer, local);
    }
    
    // stack values
    for (char stackValue : stackFrame.evalStack) {
      addUnsigned16ToByteBuffer(byteBuffer, stackValue);
    }
  }
  
  private void addUnsignedShortToByteBuffer(final List<Byte> buffer,
      final int value) {
    buffer.add((byte) ((value & 0xff00) >> 8));
    buffer.add((byte) (value & 0xff));
  }
  
  private void addUnsigned16ToByteBuffer(final List<Byte> buffer,
      final char value) {
    buffer.add((byte) ((value & 0xff00) >>> 8));
    buffer.add((byte) (value & 0xff));
  }
  
  private byte createArgSpecByte(final int[] args) {
    byte result = 0;
    for (int arg : args) { result |= (1 << arg); }
    return result;
  }
  
  // ***********************************************************************
  // ******* Transfer to Machine object
  // *****************************************
  
  /**
   * Transfers the current object state to the specified Machine object.
   * The machine needs to be in a reset state in order to function correctly.
   * 
   * @param machine a Machine object
   */
  public void transferStateToMachine(final Machine machine) {
    // Copy dynamic memory
    machine.copyBytesFromArray(dynamicMem, 0, 0, dynamicMem.length);
    
    // Stack frames
    final List<RoutineContext> contexts = new ArrayList<RoutineContext>();
            
    // Dummy frame, only the stack is interesting
    if (stackFrames.size() > 0) {
      final StackFrame dummyFrame = stackFrames.get(0);
      
      // Stack
      for (int s = 0; s < dummyFrame.getEvalStack().length; s++) {
        machine.setVariable(0, dummyFrame.getEvalStack()[s]);
      }
    }
    
    // Now iterate through all real stack frames
    for (int i = 1; i < stackFrames.size(); i++) {
    
      final StackFrame stackFrame = stackFrames.get(i);
      // ignore the start address
      final RoutineContext context =
        new RoutineContext(0, stackFrame.locals.length);
      
      context.setReturnVariable(stackFrame.returnVariable);
      context.setReturnAddress(stackFrame.pc);
      context.setNumArguments(stackFrame.args.length);
      
      // local variables
      for (int l = 0; l < stackFrame.locals.length; l++) {
        
        context.setLocalVariable(l, stackFrame.locals[l]);
      }
      
      // Stack
      for (int s = 0; s < stackFrame.evalStack.length; s++) {
        
        machine.setVariable(0, stackFrame.evalStack[s]);
      }
      contexts.add(context);      
    }    
    machine.setRoutineContexts(contexts);

    // Prepare the machine continue
    int resumePc = getProgramCounter();
    if (machine.getVersion() <= 3) {
      // In version 3 this is a branch target that needs to be read
      // Execution is continued at the first instruction after the branch offset
      resumePc += getBranchOffsetLength(machine, resumePc);
    } else if (machine.getVersion() >= 4) {
      // in version 4 and later, this is always 1
      resumePc++;
    }
    machine.setPC(resumePc);
  }
  
  /**
   * For versions >= 4. Returns the store variable
   * 
   * @param machine the machine
   * @return the store variable
   */
  public int getStoreVariable(final Machine machine) {
    final int storeVarAddress = getProgramCounter();
    return machine.readUnsigned8(storeVarAddress);
  }

  /**
   * Determine if the branch offset is one or two bytes long.
   * 
   * @param memory the Memory object of the current story
   * @param offsetAddress the branch offset address
   * @return 1 or 2, depending on the value of the branch offset
   */
  private static int getBranchOffsetLength(final Memory memory,
      final int offsetAddress) {
    final char offsetByte1 = memory.readUnsigned8(offsetAddress);
      
    // Bit 6 set -> only one byte needs to be read
    return ((offsetByte1 & 0x40) > 0) ? 1 : 2;
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
  private int[] getArgs(final byte argspec) {
    int andBit;
    final List<Integer> result = new ArrayList<Integer>();
    
    for (int i = 0; i < 7; i++) {
      andBit = 1 << i;
      if ((andBit & argspec) > 0) {
        result.add(i);
      }      
    }
    final int[] intArray = new int[result.size()];
    for (int i = 0; i < result.size(); i++) {
      intArray[i] = result.get(i);
    }
    return intArray;
  }
  
  /**
   * Joins three bytes to a program counter value.
   * 
   * @param b0 byte 0
   * @param b1 byte 1
   * @param b2 byte 2
   * @return the resulting program counter
   */
  private int decodePcBytes(final char b0, final char b1, final char b2) {    
    return ((b0 & 0xff) << 16) | ((b1 & 0xff) << 8) | (b2 & 0xff);
  }
}
