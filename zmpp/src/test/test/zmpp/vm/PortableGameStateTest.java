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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.PortableGameState.StackFrame;
import static test.zmpp.testutil.ZmppTestUtil.*;

/**
 * This tests simply analyzes a given Quetzal file.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class PortableGameStateTest {
  private Mockery context = new JUnit4Mockery();
  private PortableGameState gameState;
  private FormChunk formChunk;
  private Machine machine;
  private StoryFileHeader fileheader;

  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    fileheader = context.mock(StoryFileHeader.class);
    
    File testSaveFile = createLocalFile("testfiles/leathersave.ifzs");
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
  
  @Test
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
  
  @Test
  public void testReadSaveGameFormChunkIsNull() {
    assertFalse(gameState.readSaveGame(null));
  }
  
  @Test
  public void testGetStackFrameStatusVars() {
    StackFrame stackFrame = new StackFrame();
    stackFrame.setProgramCounter(4711);
    assertEquals(4711, stackFrame.getProgramCounter());
    stackFrame.setReturnVariable(5);
    assertEquals(5, stackFrame.getReturnVariable());
  }
  
  @Test
  public void testCaptureMachineState() {
    final List<RoutineContext> emptyContexts = new ArrayList<RoutineContext>();
    
    // Expectations
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getFileHeader(); will(returnValue(fileheader));
      one (machine).getRoutineContexts(); will(returnValue(emptyContexts));
      one (machine).getSP(); will(returnValue(4));
      allowing (machine).getStackElement(with(any(int.class))); will(returnValue((short) 42));
      one (fileheader).getRelease(); will(returnValue(42));
      one (fileheader).getChecksum(); will(returnValue(4712));
      one (fileheader).getSerialNumber(); will(returnValue("850101"));
      one (fileheader).getStaticsAddress(); will(returnValue(12345));
      allowing (machine).readByte(with(any(int.class))); will(returnValue((byte) 0));
    }});    
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
  
  @Test
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
    WritableFormChunk exportFormChunk = gameState.exportToFormChunk();
    assertTrue(Arrays.equals("FORM".getBytes(), exportFormChunk.getId()));
    assertEquals(156, exportFormChunk.getSize());
    assertTrue(Arrays.equals("IFZS".getBytes(), exportFormChunk.getSubId()));
    assertNotNull(exportFormChunk.getSubChunk("IFhd".getBytes()));    
    assertNotNull(exportFormChunk.getSubChunk("UMem".getBytes()));
    assertNotNull(exportFormChunk.getSubChunk("Stks".getBytes()));
    
    // Read IFhd information
    Chunk ifhdChunk = exportFormChunk.getSubChunk("IFhd".getBytes());
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
    Chunk umemChunk = exportFormChunk.getSubChunk("UMem".getBytes());
    memaccess = umemChunk.getMemory();
    assertEquals(dynamicMem.length, umemChunk.getSize());
    for (int i = 0; i < dynamicMem.length; i++) {
      
      assertEquals(dynamicMem[i], memaccess.readByte(8 + i));
    }
    
    // Read the Stks information
    Chunk stksChunk = exportFormChunk.getSubChunk("Stks".getBytes());
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
    gameState2.readSaveGame(exportFormChunk);
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
    byte[] data = exportFormChunk.getBytes();
    FormChunk formChunk2 = new DefaultFormChunk(new DefaultMemory(data));
    assertTrue(Arrays.equals(formChunk2.getId(), "FORM".getBytes()));
    assertTrue(Arrays.equals(formChunk2.getSubId(), "IFZS".getBytes()));
    assertEquals(exportFormChunk.getSize(), formChunk2.getSize());

    // IFhd chunk
    Chunk ifhd2 = formChunk2.getSubChunk("IFhd".getBytes());
    assertEquals(13, ifhd2.getSize());
    Memory ifhd1mem = exportFormChunk.getSubChunk("IFhd".getBytes()).getMemory();
    Memory ifhd2mem = ifhd2.getMemory();
    for (int i = 0; i < 21; i++) {
      
      assertEquals(ifhd2mem.readByte(i), ifhd1mem.readByte(i));
    }

    // UMem chunk
    Chunk umem2 = formChunk2.getSubChunk("UMem".getBytes());
    assertEquals(dynamicMem.length, umem2.getSize());
    Memory umem1mem = exportFormChunk.getSubChunk("UMem".getBytes()).getMemory();
    Memory umem2mem = umem2.getMemory();
    for (int i = 0; i < umem2.getSize() + Chunk.CHUNK_HEADER_LENGTH; i++) {
      
      assertEquals(umem2mem.readByte(i), umem1mem.readByte(i));
    }
    
    // Stks chunk
    Chunk stks2 = formChunk2.getSubChunk("Stks".getBytes());
    assertEquals(14, stks2.getSize());
    Memory stks1mem = exportFormChunk.getSubChunk("Stks".getBytes()).getMemory();
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
  /*
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
   
    mockMachine.expects(atLeastOnce()).method("getFileHeader").will(returnValue(fileheader));
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
    Machine tmpMachine = new MachineImpl();
    tmpMachine.initialize(dynMem, null, new DefaultInstructionDecoder());
    gamestate.transferStateToMachine(tmpMachine);
  }*/
  
  @Test
  public void testReadStackFrameFromChunkDiscardResult() {
    context.checking(new Expectations() {{
      // PC
      one (machine).readByte(0); will(returnValue((byte) 0x00));
      one (machine).readByte(1); will(returnValue((byte) 0x12));
      one (machine).readByte(2); will(returnValue((byte) 0x20));
      // Return variable/locals flag: discard result/3 locals (0x13)
      one (machine).readByte(3); will(returnValue((byte) 0x13));
      // return variable is always 0 if discard result
      one (machine).readByte(4); will(returnValue((byte) 0x00));
      // supplied arguments, we define a and b
      one (machine).readByte(5); will(returnValue((byte) 0x03));
      // stack size, we define 2
      one (machine).readUnsignedShort(6); will(returnValue(2));
      // local variables
      for (int i = 0; i < 3; i++) {
        one (machine).readShort(8 + i * 2); will(returnValue((short) i));
      }
      // stack variables
      for (int i = 0; i < 2; i++) {  
        one (machine).readShort(8 + 6 + i * 2); will(returnValue((short) i));
      }
    }});
    StackFrame stackFrame = new StackFrame();
    PortableGameState gamestate = new PortableGameState();
    gamestate.readStackFrame(stackFrame, machine, 0);
    assertEquals(0x1220, stackFrame.getProgramCounter());
    assertEquals(PortableGameState.DISCARD_RESULT, stackFrame.getReturnVariable());
    assertEquals(3, stackFrame.getLocals().length);
    assertEquals(2, stackFrame.getEvalStack().length);
    assertEquals(2, stackFrame.getArgs().length);
  }

  @Test
  public void testReadStackFrameFromChunkWithReturnVar() {
    context.checking(new Expectations() {{
      // PC
      one (machine).readByte(0); will(returnValue((byte) 0x00));
      one (machine).readByte(1); will(returnValue((byte) 0x12));
      one (machine).readByte(2); will(returnValue((byte) 0x21));
      // Return variable/locals flag: has return value/2 locals (0x02)
      one (machine).readByte(3); will(returnValue((byte) 0x02));
      // return variable is 5
      one (machine).readByte(4); will(returnValue((byte) 0x05));
      // supplied arguments, we define a, b and c
      one (machine).readByte(5); will(returnValue((byte) 0x07));
      // stack size, we define 3
      one (machine).readUnsignedShort(6); will(returnValue(3));
      // local variables
      for (int i = 0; i < 2; i++) {
        one (machine).readShort(8 + i * 2); will(returnValue((short) i));
      }
      // stack variables
      for (int i = 0; i < 3; i++) {  
        one (machine).readShort(8 + 4 + i * 2); will(returnValue((short) i));
      }
    }});
    StackFrame stackFrame = new StackFrame();
    PortableGameState gamestate = new PortableGameState();
    gamestate.readStackFrame(stackFrame, machine, 0);
    assertEquals(0x1221, stackFrame.getProgramCounter());
    assertEquals(5, stackFrame.getReturnVariable());
    assertEquals(2, stackFrame.getLocals().length);
    assertEquals(3, stackFrame.getEvalStack().length);
    assertEquals(3, stackFrame.getArgs().length);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testWriteStackFrameToChunkDiscardResult() {
    final List<Byte> byteBuffer = (List<Byte>) context.mock(List.class);
    context.checking(new Expectations() {{
      // pc
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x12); will(returnValue(true));
      one (byteBuffer).add((byte) 0x20); will(returnValue(true));
      // pvflag
      one (byteBuffer).add((byte) 0x11); will(returnValue(true));
      // return var
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      // argspec
      one (byteBuffer).add((byte) 0x03); will(returnValue(true));
      // stack size
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x02); will(returnValue(true));
      // locals
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x01); will(returnValue(true));
      // stack
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x05); will(returnValue(true));
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x06); will(returnValue(true));
    }});
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
  @Test
  public void testWriteStackFrameToChunkWithReturnVar() {
    final List<Byte> byteBuffer = (List<Byte>) context.mock(List.class);
    context.checking(new Expectations() {{
      // pc
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x12); will(returnValue(true));
      one (byteBuffer).add((byte) 0x21); will(returnValue(true));
      // pvflag
      one (byteBuffer).add((byte) 0x01); will(returnValue(true));
      // return var
      one (byteBuffer).add((byte) 0x06); will(returnValue(true));
      // argspec
      one (byteBuffer).add((byte) 0x03); will(returnValue(true));
      // stack size
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x02); will(returnValue(true));
      // locals
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x01); will(returnValue(true));
      // stack
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x05); will(returnValue(true));
      one (byteBuffer).add((byte) 0x00); will(returnValue(true));
      one (byteBuffer).add((byte) 0x06); will(returnValue(true));
    }});
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
