/*
 * $Id$
 * 
 * Created on 09/24/2005
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
package test.zmpp.vm;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.instructions.DefaultInstructionDecoder;
import org.zmpp.vm.GameData;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.PortableGameState.StackFrame;

/**
 * This tests simply analyzes a given Quetzal file.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class PortableGameStateTest extends MockObjectTestCase {

  private PortableGameState gameState;
  private FormChunk formChunk;
  private Mock mockMachine, mockFileheader, mockMemory, mockGamedata;
  private Machine machine;
  private GameData gamedata;
  private StoryFileHeader fileheader;
  private Memory memory;

  @Override
  protected void setUp() throws Exception {
    mockMachine = mock(Machine.class);
    machine = (Machine) mockMachine.proxy();
    mockFileheader = mock(StoryFileHeader.class);
    fileheader = (StoryFileHeader) mockFileheader.proxy();
    mockMemory = mock(Memory.class);
    memory = (Memory) mockMemory.proxy();
    mockGamedata = mock(GameData.class);
    gamedata = (GameData) mockGamedata.proxy();
    
    File testSaveFile = new File("testfiles/leathersave.ifzs");
    RandomAccessFile saveFile = new RandomAccessFile(testSaveFile, "r");
    int length = (int) saveFile.length();
    byte[] data = new byte[length];
    saveFile.readFully(data);
    Memory memaccess = new DefaultMemory(data);
    formChunk = new DefaultFormChunk(memaccess);
    gameState = new PortableGameState();
    
    saveFile.close();
  }
  
  int[] pcs = { 0, 25108, 25132, 25377, 26137, 26457, 26499 };
  int[] retvars = { 0, 0, 1, 7, 0, 4, 0 };
  int[] localLengths = { 0, 1, 11, 2, 7, 4, 0 };
  int[] stackSizes = { 4, 0, 0, 0, 2, 0, 0 };
  int[] numArgs = { 0, 1, 4, 2, 4, 4, 0 };
  
  public void testReadSaveGame() {
    
    assertTrue(gameState.readSaveGame(formChunk));
    assertEquals(59, gameState.getRelease());
    assertEquals("860730", gameState.getSerialNumber());
    assertEquals(53360, gameState.getChecksum());
    assertEquals(35298, gameState.getProgramCounter());
    
    assertEquals(7, gameState.getStackFrames().size());
  
    for (int i = 0; i < gameState.getStackFrames().size(); i++) {
    
      StackFrame sfi = gameState.getStackFrames().get(i);
      assertEquals(pcs[i], sfi.getProgramCounter());
      assertEquals(retvars[i], sfi.getReturnVariable());
      assertEquals(localLengths[i], sfi.getLocals().length);
      assertEquals(stackSizes[i], sfi.getEvalStack().length);
      assertEquals(numArgs[i], sfi.getArgs().length);
    }
    assertEquals(10030, gameState.getDeltaBytes().length);
  }
  
  public void testReadSaveGameFormChunkIsNull() {

    assertFalse(gameState.readSaveGame(null));
  }
  
  public void testGetStackFrameStatusVars() {
    
    StackFrame stackFrame = new StackFrame();
    stackFrame.setProgramCounter(4711);
    assertEquals(4711, stackFrame.getProgramCounter());
    stackFrame.setReturnVariable(5);
    assertEquals(5, stackFrame.getReturnVariable());
  }
  
  public void testCaptureMachineState() {

    List<RoutineContext> emptyContexts = new ArrayList<RoutineContext>();
    
    // Expectations
    mockMachine.expects(atLeastOnce()).method("getFileHeader").will(returnValue(fileheader));
    mockMachine.expects(once()).method("getMemory").will(returnValue(memory));
    mockMachine.expects(once()).method("getRoutineContexts").will(returnValue(emptyContexts));
    mockMachine.expects(once()).method("getSP").will(returnValue(4));
    mockMachine.expects(atLeastOnce()).method("getStackElement").will(returnValue((short) 42));
    
    mockFileheader.expects(once()).method("getRelease").will(returnValue(42));
    mockFileheader.expects(once()).method("getChecksum").will(returnValue(4712));
    mockFileheader.expects(once()).method("getSerialNumber").will(returnValue("850101"));
    mockFileheader.expects(once()).method("getStaticsAddress").will(returnValue(12345));
    mockMemory.expects(atLeastOnce()).method("readByte").withAnyArguments().will(returnValue((byte) 0));
    
    gameState.captureMachineState(machine, 4711);
    assertEquals(4711, gameState.getProgramCounter());
    assertEquals(42, gameState.getRelease());
    assertEquals(4712, gameState.getChecksum());
    assertEquals("850101", gameState.getSerialNumber());
    assertEquals(12345, gameState.getDynamicMemoryDump().length);
    assertEquals(1, gameState.getStackFrames().size());
    StackFrame stackFrame = gameState.getStackFrames().get(0);
    assertEquals(4, stackFrame.getEvalStack().length);
  }
  
  public void testExportToFormChunk() throws Exception {
    
    short[] dummyStack = { (short) 1, (short) 2, (short) 3 };
    StackFrame dummyFrame = new StackFrame();
    dummyFrame.setArgs(new int[0]);
    dummyFrame.setEvalStack(dummyStack);
    dummyFrame.setLocals(new short[0]);
    
    byte[] dynamicMem = new byte[99];
    dynamicMem[35] = (byte) 12;
    dynamicMem[49] = (byte) 13;
    dynamicMem[51] = (byte) 21;
    dynamicMem[72] = (byte) 72;
    dynamicMem[98] = (byte) 1;

    gameState.setRelease(42);
    gameState.setChecksum(4712);
    gameState.setSerialNumber("850101");
    gameState.setDynamicMem(dynamicMem);
    gameState.setProgramCounter(4711);
    gameState.getStackFrames().add(dummyFrame);
    
    // Export our mock machine to a FormChunk verify some basic information
    WritableFormChunk formChunk = gameState.exportToFormChunk();
    assertTrue(Arrays.equals("FORM".getBytes(), formChunk.getId()));
    assertEquals(156, formChunk.getSize());
    assertTrue(Arrays.equals("IFZS".getBytes(), formChunk.getSubId()));
    assertNotNull(formChunk.getSubChunk("IFhd".getBytes()));    
    assertNotNull(formChunk.getSubChunk("UMem".getBytes()));
    assertNotNull(formChunk.getSubChunk("Stks".getBytes()));
    
    // Read IFhd information
    Chunk ifhdChunk = formChunk.getSubChunk("IFhd".getBytes());
    Memory memaccess = ifhdChunk.getMemory();
    assertEquals(13, ifhdChunk.getSize());
    assertEquals(gameState.getRelease(), memaccess.readUnsignedShort(8));
    byte[] serial = new byte[6];
    for (int i = 0; i < 6; i++) serial[i] = memaccess.readByte(10 + i);
    assertTrue(Arrays.equals(gameState.getSerialNumber().getBytes(), serial));
    assertEquals(gameState.getChecksum(), memaccess.readUnsignedShort(16));
    assertEquals(gameState.getProgramCounter(),
        decodePcBytes(memaccess.readByte(18), memaccess.readByte(19), memaccess.readByte(20)));
    
    // Read the UMem information
    Chunk umemChunk = formChunk.getSubChunk("UMem".getBytes());
    memaccess = umemChunk.getMemory();
    assertEquals(dynamicMem.length, umemChunk.getSize());
    for (int i = 0; i < dynamicMem.length; i++) {
      
      assertEquals(dynamicMem[i], memaccess.readByte(8 + i));
    }
    
    // Read the Stks information
    Chunk stksChunk = formChunk.getSubChunk("Stks".getBytes());
    memaccess = stksChunk.getMemory();
    
    // There is only one frame at the moment
    assertEquals(14, stksChunk.getSize());
    int retpc0 = decodePcBytes(memaccess.readByte(8), memaccess.readByte(9), memaccess.readByte(10));
    assertEquals(0, retpc0);
    assertEquals(0, memaccess.readByte(11)); // pv flags
    assertEquals(0, memaccess.readByte(12)); // retvar
    assertEquals(0, memaccess.readByte(13)); // argspec
    assertEquals(3, memaccess.readUnsignedShort(14)); // stack size
    assertEquals(1, memaccess.readShort(16)); // stack val 0
    assertEquals(2, memaccess.readShort(18)); // stack val 1
    assertEquals(3, memaccess.readShort(20)); // stack val 2
    
    // Now read the form chunk into another gamestate and compare
    PortableGameState gameState2 = new PortableGameState();
    gameState2.readSaveGame(formChunk);
    assertEquals(gameState.getRelease(), gameState2.getRelease());
    assertEquals(gameState.getChecksum(), gameState2.getChecksum());
    assertEquals(gameState.getSerialNumber(), gameState2.getSerialNumber());
    assertEquals(gameState.getProgramCounter(), gameState2.getProgramCounter());
    assertEquals(gameState.getStackFrames().size(), gameState2.getStackFrames().size());
    StackFrame dummyFrame1 = gameState.getStackFrames().get(0);
    StackFrame dummyFrame2 = gameState2.getStackFrames().get(0);
    assertEquals(dummyFrame1.getProgramCounter(), dummyFrame2.getProgramCounter());
    assertEquals(dummyFrame1.getReturnVariable(), dummyFrame2.getReturnVariable());
    assertEquals(0, dummyFrame2.getArgs().length);
    assertEquals(0, dummyFrame2.getLocals().length);
    assertEquals(3, dummyFrame2.getEvalStack().length);
        
    // Convert to byte array and reconstruct
    // This is in fact a test for WritableFormChunk and should be put
    // in a separate test
    byte[] data = formChunk.getBytes();
    FormChunk formChunk2 = new DefaultFormChunk(new DefaultMemory(data));
    assertTrue(Arrays.equals(formChunk2.getId(), "FORM".getBytes()));
    assertTrue(Arrays.equals(formChunk2.getSubId(), "IFZS".getBytes()));
    assertEquals(formChunk.getSize(), formChunk2.getSize());

    // IFhd chunk
    Chunk ifhd2 = formChunk2.getSubChunk("IFhd".getBytes());
    assertEquals(13, ifhd2.getSize());
    Memory ifhd1mem = formChunk.getSubChunk("IFhd".getBytes()).getMemory();
    Memory ifhd2mem = ifhd2.getMemory();
    for (int i = 0; i < 21; i++) {
      
      assertEquals(ifhd2mem.readByte(i), ifhd1mem.readByte(i));
    }

    // UMem chunk
    Chunk umem2 = formChunk2.getSubChunk("UMem".getBytes());
    assertEquals(dynamicMem.length, umem2.getSize());
    Memory umem1mem = formChunk.getSubChunk("UMem".getBytes()).getMemory();
    Memory umem2mem = umem2.getMemory();
    for (int i = 0; i < umem2.getSize() + Chunk.CHUNK_HEADER_LENGTH; i++) {
      
      assertEquals(umem2mem.readByte(i), umem1mem.readByte(i));
    }
    
    // Stks chunk
    Chunk stks2 = formChunk2.getSubChunk("Stks".getBytes());
    assertEquals(14, stks2.getSize());
    Memory stks1mem = formChunk.getSubChunk("Stks".getBytes()).getMemory();
    Memory stks2mem = stks2.getMemory();
    for (int i = 0; i < stks2.getSize() + Chunk.CHUNK_HEADER_LENGTH; i++) {
      
      assertEquals(stks2mem.readByte(i), stks1mem.readByte(i));
    }    
  }
  
  private int decodePcBytes(byte b0, byte b1, byte b2) {
    
    return ((b0 & 0xff) << 16) | ((b1 & 0xff) << 8) | (b2 & 0xff);
  }
  
  // ******************************************************************
  // ****
  public void testTransferState() {

    byte[] dynMem = {
        (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13  
    };
    PortableGameState gamestate = new PortableGameState();
    List<StackFrame> stackframes = gamestate.getStackFrames();
    
    StackFrame stackFrame0 = new StackFrame();
    int[] args0 = { 1, 2 };
    short[] locals0 = { (short) 1, (short) 2 };
    short[] stack0 = { (short) 11, (short) 21 };
    stackFrame0.setProgramCounter(4711);
    
    // what do we do with "N" calls which do not have return variables ?
    stackFrame0.setReturnVariable(0x20);
    stackFrame0.setLocals(locals0);
    stackFrame0.setEvalStack(stack0);
    stackFrame0.setArgs(args0);
    stackframes.add(stackFrame0);
    
    gamestate.setDynamicMem(dynMem);
   
    mockGamedata.expects(atLeastOnce()).method("getMemory").will(returnValue(memory));
    mockGamedata.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockGamedata.expects(atLeastOnce()).method("getResources").will(returnValue(null));
    mockFileheader.expects(once()).method("getProgramStart").will(returnValue(4711));
    mockFileheader.expects(once()).method("getGlobalsAddress").will(returnValue(5711));
    mockFileheader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockFileheader.expects(once()).method("setEnabled").withAnyArguments();
    mockFileheader.expects(once()).method("setInterpreterNumber").withAnyArguments();
    mockFileheader.expects(once()).method("setInterpreterVersion").withAnyArguments();
    //mockFileheader.expects(once()).method("setStandardRevision").with(eq(1), eq(0));
    
    for (int i = 0; i < dynMem.length; i++) {
      mockMemory.expects(once()).method("writeByte").with(eq(i), eq((byte) dynMem[i]));
    }
    
    // Tests if the dynamic memory and the stack frames are
    // completely copied
    Machine machine = new MachineImpl();
    machine.initialize(gamedata, new DefaultInstructionDecoder());
    gamestate.transferStateToMachine(machine);
  }
  
  public void testReadStackFrameFromChunkDiscardResult() {
    
    // PC
    mockMemory.expects(once()).method("readByte").with(eq(0)).will(returnValue((byte) 0));
    mockMemory.expects(once()).method("readByte").with(eq(1)).will(returnValue((byte) 0x12));
    mockMemory.expects(once()).method("readByte").with(eq(2)).will(returnValue((byte) 0x20));
    
    // Return variable/locals flag: discard result/3 locals (0x13)
    mockMemory.expects(once()).method("readByte").with(eq(3)).will(returnValue((byte) 0x13));

    // return variable is always 0 if discard result
    mockMemory.expects(once()).method("readByte").with(eq(4)).will(returnValue((byte) 0));
    
    // supplied arguments, we define a and b
    mockMemory.expects(once()).method("readByte").with(eq(5)).will(returnValue((byte) 3));

    // stack size, we define 2
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(6)).will(returnValue(2));
    
    // local variables
    for (int i = 0; i < 3; i++) {
      
      mockMemory.expects(once()).method("readShort").with(eq(8 + i * 2)).will(returnValue((short) i));
    }

    // stack variables
    for (int i = 0; i < 2; i++) {
      
      mockMemory.expects(once()).method("readShort").with(eq(8 + 6 + i * 2)).will(returnValue((short) i));
    }
    
    StackFrame stackFrame = new StackFrame();
    PortableGameState gamestate = new PortableGameState();
    gamestate.readStackFrame(stackFrame, memory, 0);
    assertEquals(0x1220, stackFrame.getProgramCounter());
    assertEquals(PortableGameState.DISCARD_RESULT, stackFrame.getReturnVariable());
    assertEquals(3, stackFrame.getLocals().length);
    assertEquals(2, stackFrame.getEvalStack().length);
    assertEquals(2, stackFrame.getArgs().length);
  }

  public void testReadStackFrameFromChunkWithReturnVar() {
    
    // PC
    mockMemory.expects(once()).method("readByte").with(eq(0)).will(returnValue((byte) 0));
    mockMemory.expects(once()).method("readByte").with(eq(1)).will(returnValue((byte) 0x12));
    mockMemory.expects(once()).method("readByte").with(eq(2)).will(returnValue((byte) 0x21));
    
    // Return variable/locals flag: has return value/2 locals (0x13)
    mockMemory.expects(once()).method("readByte").with(eq(3)).will(returnValue((byte) 0x02));

    // return variable is 5
    mockMemory.expects(once()).method("readByte").with(eq(4)).will(returnValue((byte) 5));
    
    // supplied arguments, we define a, b and c
    mockMemory.expects(once()).method("readByte").with(eq(5)).will(returnValue((byte) 7));

    // stack size, we define 3
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(6)).will(returnValue(3));
    
    // local variables
    for (int i = 0; i < 2; i++) {
      
      mockMemory.expects(once()).method("readShort").with(eq(8 + i * 2)).will(returnValue((short) i));
    }

    // stack variables
    for (int i = 0; i < 3; i++) {
      
      mockMemory.expects(once()).method("readShort").with(eq(8 + 4 + i * 2)).will(returnValue((short) i));
    }
    
    StackFrame stackFrame = new StackFrame();
    PortableGameState gamestate = new PortableGameState();
    gamestate.readStackFrame(stackFrame, memory, 0);
    assertEquals(0x1221, stackFrame.getProgramCounter());
    assertEquals(5, stackFrame.getReturnVariable());
    assertEquals(2, stackFrame.getLocals().length);
    assertEquals(3, stackFrame.getEvalStack().length);
    assertEquals(3, stackFrame.getArgs().length);
  }

  @SuppressWarnings("unchecked")
  public void testWriteStackFrameToChunkDiscardResult() {
    Mock mockByteBuffer = mock(List.class);
    List<Byte> byteBuffer = (List<Byte>) mockByteBuffer.proxy();
    
    // pc
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x12)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x20)).will(returnValue(true));
    
    // pvflag
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x11)).will(returnValue(true));

    // return var
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    
    // argspec
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x03)).will(returnValue(true));

    // stack size
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x02)).will(returnValue(true));
   
    // locals
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x01)).will(returnValue(true));
    
    // stack
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x05)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x06)).will(returnValue(true));
    
    int[] args = { 0, 1 };
    short[] locals = { (short) 1 };
    short[] stack = { (short) 5, (short) 6 };
    
    StackFrame stackFrame = new StackFrame();
    stackFrame.setProgramCounter(0x1220);
    stackFrame.setReturnVariable(PortableGameState.DISCARD_RESULT);
    stackFrame.setArgs(args);
    stackFrame.setLocals(locals);
    stackFrame.setEvalStack(stack);
    
    PortableGameState gamestate = new PortableGameState();
    gamestate.writeStackFrameToByteBuffer(byteBuffer, stackFrame);
  }
 
  @SuppressWarnings("unchecked")
  public void testWriteStackFrameToChunkWithReturnVar() {
    Mock mockByteBuffer = mock(List.class);
    List<Byte> byteBuffer = (List<Byte>) mockByteBuffer.proxy();
    
    // pc
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x12)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x21)).will(returnValue(true));
    
    // pvflag
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x01)).will(returnValue(true));

    // return var
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x06)).will(returnValue(true));
    
    // argspec
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x03)).will(returnValue(true));

    // stack size
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x02)).will(returnValue(true));
   
    // locals
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x01)).will(returnValue(true));
    
    // stack
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x05)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x00)).will(returnValue(true));
    mockByteBuffer.expects(once()).method("add").with(eq((byte) 0x06)).will(returnValue(true));
    
    int[] args = { 0, 1 };
    short[] locals = { (short) 1 };
    short[] stack = { (short) 5, (short) 6 };
    
    StackFrame stackFrame = new StackFrame();
    stackFrame.setProgramCounter(0x1221);
    stackFrame.setReturnVariable(6);
    stackFrame.setArgs(args);
    stackFrame.setLocals(locals);
    stackFrame.setEvalStack(stack);
    
    PortableGameState gamestate = new PortableGameState();
    gamestate.writeStackFrameToByteBuffer(byteBuffer, stackFrame);
  }
}
