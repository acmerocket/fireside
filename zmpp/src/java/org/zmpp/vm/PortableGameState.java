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
import org.zmpp.iff.FormChunk;

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
  
    int pc;
    int returnVariable;
    short[] locals;
    short[] evalStack;
    int[] args;
    
    public int getProgramCounter() { return pc; }    
    public int getReturnVariable() { return returnVariable; }
    public short[] getEvalStack() { return evalStack; }
    public short[] getLocals() { return locals; }
    public int[] getArgs() { return args; }
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
  
  public void captureMachineState(Machine machine) {
    
    StoryFileHeader fileheader = machine.getStoryFileHeader();
    release = fileheader.getRelease();
    checksum = fileheader.getChecksum();
    serialBytes = fileheader.getSerialNumber().getBytes();
    pc = machine.getProgramCounter();
    
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
  
  private void captureStackFrames(Machine machine) {
    
    // TODO: Write out stack frames
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
