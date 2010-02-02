/*
 * Created on 09/24/2005
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
import org.zmpp.base.StoryFileHeader;
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
  private byte[] savedata;

  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    fileheader = context.mock(StoryFileHeader.class);
    
    File testSaveFile = createLocalFile("testfiles/leathersave.ifzs");
    RandomAccessFile saveFile = new RandomAccessFile(testSaveFile, "r");
    int length = (int) saveFile.length();
    savedata = new byte[length];
    saveFile.readFully(savedata);
    Memory memaccess = new DefaultMemory(savedata);
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
    stackFrame.setProgramCounter((char) 4711);
    assertEquals(4711, stackFrame.getProgramCounter());
    stackFrame.setReturnVariable((char) 5);
    assertEquals(5, stackFrame.getReturnVariable());
  }
  
  @Test
  public void testCaptureMachineState() {
    final List<RoutineContext> emptyContexts = new ArrayList<RoutineContext>();
    
    // Expectations
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getFileHeader(); will(returnValue(fileheader));
      one (machine).getRoutineContexts(); will(returnValue(emptyContexts));
      one (machine).getSP(); will(returnValue((char) 4));
      allowing (machine).getStackElement(with(any(int.class))); will(returnValue((char) 42));
      one (machine).getRelease(); will(returnValue(42));
      one (machine).readUnsigned16(StoryFileHeader.CHECKSUM); will(returnValue((char) 4712));
      one (machine).readUnsigned16(StoryFileHeader.STATIC_MEM); will(returnValue((char) 12345));
      one (fileheader).getSerialNumber(); will(returnValue("850101"));
      allowing (machine).copyBytesToArray(with(any(savedata.getClass())), with(0), with(0), with(12345));
      allowing (machine).readUnsigned8(with(any(int.class))); will(returnValue((short) 0));
    }});
    gameState.captureMachineState(machine, (char) 4711);
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
    
    char[] dummyStack = { (short) 1, (short) 2, (short) 3 };
    StackFrame dummyFrame = new StackFrame();
    dummyFrame.setArgs(new char[0]);
    dummyFrame.setEvalStack(dummyStack);
    dummyFrame.setLocals(new char[0]);
    
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
    gameState.setProgramCounter((char) 4711);
    gameState.getStackFrames().add(dummyFrame);
    
    // Export our mock machine to a FormChunk verify some basic information
    WritableFormChunk exportFormChunk = gameState.exportToFormChunk();
    assertEquals("FORM", exportFormChunk.getId());
    assertEquals(156, exportFormChunk.getSize());
    assertEquals("IFZS", exportFormChunk.getSubId());
    assertNotNull(exportFormChunk.getSubChunk("IFhd"));    
    assertNotNull(exportFormChunk.getSubChunk("UMem"));
    assertNotNull(exportFormChunk.getSubChunk("Stks"));
    
    // Read IFhd information
    Chunk ifhdChunk = exportFormChunk.getSubChunk("IFhd");
    Memory memaccess = ifhdChunk.getMemory();
    assertEquals(13, ifhdChunk.getSize());
    assertEquals(gameState.getRelease(), memaccess.readUnsigned16(8));
    byte[] serial = new byte[6];
    memaccess.copyBytesToArray(serial, 0, 10, 6);
    assertTrue(Arrays.equals(gameState.getSerialNumber().getBytes(), serial));
    assertEquals(gameState.getChecksum(), memaccess.readUnsigned16(16));
    assertEquals(gameState.getProgramCounter(),
                 decodePcBytes(memaccess.readUnsigned8(18),
                               memaccess.readUnsigned8(19),
                               memaccess.readUnsigned8(20)));
    
    // Read the UMem information
    Chunk umemChunk = exportFormChunk.getSubChunk("UMem");
    memaccess = umemChunk.getMemory();
    assertEquals(dynamicMem.length, umemChunk.getSize());
    for (int i = 0; i < dynamicMem.length; i++) {
      assertEquals(dynamicMem[i], memaccess.readUnsigned8(8 + i));
    }
    
    // Read the Stks information
    Chunk stksChunk = exportFormChunk.getSubChunk("Stks");
    memaccess = stksChunk.getMemory();
    
    // There is only one frame at the moment
    assertEquals(14, stksChunk.getSize());
    int retpc0 = decodePcBytes(memaccess.readUnsigned8(8),
                               memaccess.readUnsigned8(9),
                               memaccess.readUnsigned8(10));
    assertEquals(0, retpc0);
    assertEquals(0, memaccess.readUnsigned8(11)); // pv flags
    assertEquals(0, memaccess.readUnsigned8(12)); // retvar
    assertEquals(0, memaccess.readUnsigned8(13)); // argspec
    assertEquals(3, memaccess.readUnsigned16(14)); // stack size
    assertEquals(1, memaccess.readUnsigned16(16)); // stack val 0
    assertEquals(2, memaccess.readUnsigned16(18)); // stack val 1
    assertEquals(3, memaccess.readUnsigned16(20)); // stack val 2
    
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
    assertEquals("FORM", formChunk2.getId());
    assertEquals("IFZS", formChunk2.getSubId());
    assertEquals(exportFormChunk.getSize(), formChunk2.getSize());

    // IFhd chunk
    Chunk ifhd2 = formChunk2.getSubChunk("IFhd");
    assertEquals(13, ifhd2.getSize());
    Memory ifhd1mem = exportFormChunk.getSubChunk("IFhd").getMemory();
    Memory ifhd2mem = ifhd2.getMemory();
    for (int i = 0; i < 21; i++) {
      assertEquals(ifhd2mem.readUnsigned8(i), ifhd1mem.readUnsigned8(i));
    }

    // UMem chunk
    Chunk umem2 = formChunk2.getSubChunk("UMem");
    assertEquals(dynamicMem.length, umem2.getSize());
    Memory umem1mem = exportFormChunk.getSubChunk("UMem").getMemory();
    Memory umem2mem = umem2.getMemory();
    for (int i = 0; i < umem2.getSize() + Chunk.CHUNK_HEADER_LENGTH; i++) {      
      assertEquals(umem2mem.readUnsigned8(i), umem1mem.readUnsigned8(i));
    }
    
    // Stks chunk
    Chunk stks2 = formChunk2.getSubChunk("Stks");
    assertEquals(14, stks2.getSize());
    Memory stks1mem = exportFormChunk.getSubChunk("Stks").getMemory();
    Memory stks2mem = stks2.getMemory();
    for (int i = 0; i < stks2.getSize() + Chunk.CHUNK_HEADER_LENGTH; i++) {      
      assertEquals(stks2mem.readUnsigned8(i), stks1mem.readUnsigned8(i));
    }    
  }
  
  private int decodePcBytes(char b0, char b1, char b2) {
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
      one (machine).readUnsigned8(0); will(returnValue((char) 0x00));
      one (machine).readUnsigned8(1); will(returnValue((char) 0x12));
      one (machine).readUnsigned8(2); will(returnValue((char) 0x20));
      // Return variable/locals flag: discard result/3 locals (0x13)
      one (machine).readUnsigned8(3); will(returnValue((char) 0x13));
      // return variable is always 0 if discard result
      one (machine).readUnsigned8(4); will(returnValue((char) 0x00));
      // supplied arguments, we define a and b
      one (machine).readUnsigned8(5); will(returnValue((char) 0x03));
      // stack size, we define 2
      one (machine).readUnsigned16(6); will(returnValue((char) 2));
      // local variables
      for (int i = 0; i < 3; i++) {
        one (machine).readUnsigned16(8 + i * 2); will(returnValue((char) i));
      }
      // stack variables
      for (int i = 0; i < 2; i++) {  
        one (machine).readUnsigned16(8 + 6 + i * 2); will(returnValue((char) i));
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
      one (machine).readUnsigned8(0); will(returnValue((char) 0x00));
      one (machine).readUnsigned8(1); will(returnValue((char) 0x12));
      one (machine).readUnsigned8(2); will(returnValue((char) 0x21));
      // Return variable/locals flag: has return value/2 locals (0x02)
      one (machine).readUnsigned8(3); will(returnValue((char) 0x02));
      // return variable is 5
      one (machine).readUnsigned8(4); will(returnValue((char) 0x05));
      // supplied arguments, we define a, b and c
      one (machine).readUnsigned8(5); will(returnValue((char) 0x07));
      // stack size, we define 3
      one (machine).readUnsigned16(6); will(returnValue((char) 3));
      // local variables
      for (int i = 0; i < 2; i++) {
        one (machine).readUnsigned16(8 + i * 2); will(returnValue((char) i));
      }
      // stack variables
      for (int i = 0; i < 3; i++) {  
        one (machine).readUnsigned16(8 + 4 + i * 2); will(returnValue((char) i));
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
    char[] args = { 0, 1 };
    char[] locals = { (short) 1 };
    char[] stack = { (short) 5, (short) 6 };
    
    StackFrame stackFrame = new StackFrame();
    stackFrame.setProgramCounter((char) 0x1220);
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
    char[] args = { 0, 1 };
    char[] locals = { (short) 1 };
    char[] stack = { (short) 5, (short) 6 };
    
    StackFrame stackFrame = new StackFrame();
    stackFrame.setProgramCounter((char) 0x1221);
    stackFrame.setReturnVariable((char) 6);
    stackFrame.setArgs(args);
    stackFrame.setLocals(locals);
    stackFrame.setEvalStack(stack);
    
    PortableGameState gamestate = new PortableGameState();
    gamestate.writeStackFrameToByteBuffer(byteBuffer, stackFrame);
  }
}
