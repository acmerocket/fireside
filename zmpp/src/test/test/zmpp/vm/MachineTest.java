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
package test.zmpp.vm;

import org.jmock.Mock;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.instructions.DefaultInstructionDecoder;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Input;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vm.MemoryOutputStream;
import org.zmpp.vm.Output;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;
import org.zmpp.vm.StoryFileHeader.Attribute;

public class MachineTest extends MemoryMapSetup {

  private MachineImpl machine;
  private Mock mockStatusLine, mockScreen;
  private Mock mockOutputStream1, mockOutputStream2, mockOutputStream3;
  private Mock mockInputStream1, mockInputStream0;
  private OutputStream outputStream1, outputStream2, outputStream3;
  private InputStream inputStream1, inputStream0;
  private StatusLine statusLine;
  private ScreenModel screen;
  private Mock mockDataStore;
  private SaveGameDataStore datastore;
  private Output output;
  private Input input;
  
  protected void setUp() throws Exception {

    super.setUp();
    machine = new MachineImpl();
    machine.initialize(config, new DefaultInstructionDecoder());
    mockStatusLine = mock(StatusLine.class);
    statusLine = (StatusLine) mockStatusLine.proxy();
    mockScreen = mock(ScreenModel.class);
    screen = (ScreenModel) mockScreen.proxy();
    mockOutputStream1 = mock(OutputStream.class);
    mockOutputStream2 = mock(OutputStream.class);
    mockOutputStream3 = mock(OutputStream.class);
    outputStream1 = (OutputStream) mockOutputStream1.proxy();
    outputStream2 = (OutputStream) mockOutputStream2.proxy();
    outputStream3 = (OutputStream) mockOutputStream3.proxy();

    mockInputStream0 = mock(InputStream.class);
    inputStream0 = (InputStream) mockInputStream0.proxy();
    mockInputStream1 = mock(InputStream.class);
    inputStream1 = (InputStream) mockInputStream1.proxy();
    
    machine.setScreen(screen);
    
    output = machine.getOutput();    
    output.setOutputStream(Output.OUTPUTSTREAM_SCREEN, outputStream1);
    output.setOutputStream(Output.OUTPUTSTREAM_TRANSCRIPT, outputStream2);
    output.setOutputStream(Output.OUTPUTSTREAM_MEMORY, outputStream3);
    
    input = machine.getInput();
    input.setInputStream(Input.INPUTSTREAM_KEYBOARD, inputStream0);
    input.setInputStream(Input.INPUTSTREAM_FILE, inputStream1);
    
    mockDataStore = mock(SaveGameDataStore.class);
    datastore = (SaveGameDataStore) mockDataStore.proxy();
  }
  
  public void testInitialState() {
    
    assertEquals(fileheader, machine.getGameData().getStoryFileHeader());
    assertEquals(minizorkmap, machine.getGameData().getMemoryAccess());
    assertTrue(machine.getGameData().hasValidChecksum());
  }
  
  public void testSetOutputStream() {
    
    mockOutputStream1.expects(once()).method("select").with(eq(true));
    mockOutputStream2.expects(once()).method("select").with(eq(false));
    mockOutputStream1.expects(atLeastOnce()).method("isSelected").will(returnValue(true));
    mockOutputStream2.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream3.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream1.expects(atLeastOnce()).method("print").withAnyArguments();
    output.selectOutputStream(1, true);
    
    output.print(new ZsciiString("test"));
  }
  
  public void testSelectOutputStream() {
    
    mockOutputStream1.expects(once()).method("select").with(eq(true));
    output.selectOutputStream(1, true);
  }
  
  public void testInputStream1() {
    
    mockScreen.expects(once()).method("setPaging").with(eq(false));
    
    input.setInputStream(Input.INPUTSTREAM_KEYBOARD, inputStream0);
    input.setInputStream(Input.INPUTSTREAM_FILE, inputStream1);
    
    input.selectInputStream(Input.INPUTSTREAM_FILE);
    assertEquals(inputStream1, input.getSelectedInputStream());
  }

  public void testInputStream0() {
    
    mockScreen.expects(once()).method("setPaging").with(eq(true));
    
    input.setInputStream(Input.INPUTSTREAM_KEYBOARD, inputStream0);
    input.setInputStream(Input.INPUTSTREAM_FILE, inputStream1);
    
    input.selectInputStream(Input.INPUTSTREAM_KEYBOARD);
    assertEquals(inputStream0, input.getSelectedInputStream());
  }
  
  public void testRandom() {
    
    short random1 = machine.random((short) 23);
    assertTrue(0 < random1 && random1 <= 23);    
    assertEquals(0, machine.random((short) 0));
    
    short random2 = machine.random((short) 23);    
    assertTrue(0 < random2 && random2 <= 23);
    assertEquals(0, machine.random((short) -23));
    
    short random3 = machine.random((short) 23);
    assertTrue(0 < random3 && random3 <= 23);
  }
  
  public void testRandom1() {
    
    short value;
    for (int i = 0; i < 10; i++) {
      
      value = machine.random((short) 1);
      assertEquals(value, 1);
    }
  }

  public void testRandom2() {
    
    short value;
    boolean contains1 = false;
    boolean contains2 = false;
    for (int i = 0; i < 10; i++) {
      
      value = machine.random((short) 2);
      assertTrue(0 < value && value <= 2);
      if (value == 1) contains1 = true;
      if (value == 2) contains2 = true;
    }
    assertTrue(contains1);
    assertTrue(contains2);
  }
  
  public void testStartQuit() {
    
    mockOutputStream2.expects(once()).method("select").with(eq(false));
    mockOutputStream1.expects(atLeastOnce()).method("isSelected").will(returnValue(true));
    mockOutputStream2.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream3.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream1.expects(atLeastOnce()).method("print").withAnyArguments();
    
    mockScreen.expects(once()).method("redraw");
    
    mockOutputStream1.expects(atLeastOnce()).method("flush");
    mockOutputStream1.expects(once()).method("close");
    mockOutputStream2.expects(atLeastOnce()).method("flush");
    mockOutputStream2.expects(once()).method("close");
    mockOutputStream3.expects(atLeastOnce()).method("flush");
    mockOutputStream3.expects(once()).method("close");
    
    mockInputStream0.expects(once()).method("close");
    mockInputStream1.expects(once()).method("close");
    
    machine.start();
    assertTrue(machine.getCpu().isRunning());
    machine.quit();
    assertFalse(machine.getCpu().isRunning());
  }
  
  public void testStatusLineScore() {
    
    machine.getCpu().setVariable(0x10, (short) 2);
    mockStatusLine.expects(once()).method("updateStatusScore");
    machine.setStatusLine(statusLine);
    machine.updateStatusLine();
  }
  
  public void testStatusLineTime() {
    
    machine.getCpu().setVariable(0x10, (short) 2);
    mockStatusLine.expects(once()).method("updateStatusTime");
    machine.setStatusLine(statusLine); // set the "time" flag
    machine.getGameData().getMemoryAccess().writeByte(1, (byte) 2);
    machine.updateStatusLine();
  }
  
  public void testGetSetScreen() {

    machine.setScreen(screen);
    assertTrue(screen == machine.getScreen());
  }
  
  public void testHalt() {
    
    mockOutputStream2.expects(once()).method("select").with(eq(false));
    mockOutputStream1.expects(atLeastOnce()).method("isSelected").will(returnValue(true));
    mockOutputStream2.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream3.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream1.expects(atLeastOnce()).method("print").with(eq((short) 'e'), eq(false));
    mockOutputStream1.expects(atLeastOnce()).method("print").with(eq((short) 'r'), eq(false));
    mockOutputStream1.expects(atLeastOnce()).method("print").with(eq((short) 'o'), eq(false));
    
    machine.start();
    
    assertTrue(machine.getCpu().isRunning());
    machine.getCpu().halt("error");
    assertFalse(machine.getCpu().isRunning());
  }
  
  public void testRestart() {
    
    mockScreen.expects(once()).method("reset");
    machine.restart();
  }
  
  public void testSave() {
    
    mockDataStore.expects(once()).method("saveFormChunk").withAnyArguments().will(returnValue(true));
    machine.setSaveGameDataStore(datastore);
    assertTrue(machine.save(4711));
  }
  
  public void testGetDictionary() {
    
    assertNotNull(machine.getGameData().getDictionary());
  }
  
  public void testSelectTranscriptOutputStream() {
    
    mockOutputStream2.expects(once()).method("select").with(eq(true));

    output.selectOutputStream(Output.OUTPUTSTREAM_TRANSCRIPT, true);
    assertTrue(machine.getGameData().getStoryFileHeader().isEnabled(Attribute.TRANSCRIPTING));    
  }
  
  public void testSelectMemoryOutputStreamWithoutTable() {
    
    mockOutputStream2.expects(atLeastOnce()).method("select").with(eq(false));
    mockOutputStream3.expects(once()).method("select").with(eq(true));
    
    mockOutputStream1.expects(once()).method("isSelected").will(returnValue(true));
    mockOutputStream2.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream3.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    
    // for the error message
    mockOutputStream1.expects(atLeastOnce()).method("print").withAnyArguments();
    
    output.selectOutputStream(Output.OUTPUTSTREAM_MEMORY, true);
  }
  
  private int tableAddress;
  
  public void testSelectMemoryOutputStreamWithTable() {
    
    MemoryOutputStream memstream = new MemoryOutputStream(machine) {
      
      public void select(int table) {
        
        tableAddress = table;
      }
    };
    output.setOutputStream(Output.OUTPUTSTREAM_MEMORY, memstream);
    output.selectOutputStream3(4711);
    
    assertEquals(4711, tableAddress);
  }
  
  public void testReadLine() {
    
    mockScreen.expects(once()).method("setPaging").with(eq(true));
    mockScreen.expects(atLeastOnce()).method("displayCursor").with(eq(true));
    mockScreen.expects(atLeastOnce()).method("displayCursor").with(eq(false));
    mockScreen.expects(atLeastOnce()).method("redraw");
    mockInputStream0.expects(atLeastOnce()).method("getZsciiChar").will(
        onConsecutiveCalls( returnValue((short) 'H'), returnValue((short) 'i'), returnValue((short) ZsciiEncoding.NEWLINE) ));
    
    mockOutputStream2.expects(atLeastOnce()).method("select").with(eq(false));
    
    mockOutputStream1.expects(atLeastOnce()).method("isSelected").will(returnValue(true));
    mockOutputStream2.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream3.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream1.expects(once()).method("flush");

    mockOutputStream1.expects(atLeastOnce()).method("print").with(eq((short) 'H'), eq(true));
    mockOutputStream1.expects(atLeastOnce()).method("print").with(eq((short) 'i'), eq(true));
    mockOutputStream1.expects(atLeastOnce()).method("print").with(eq((short) ZsciiEncoding.NEWLINE), eq(false));
    
    input.selectInputStream(0);
    machine.getInputFunctions().readLine(4711, 0, 0);
  }
  
  public void testReadChar() {

    mockScreen.expects(atLeastOnce()).method("displayCursor").with(eq(true));
    mockScreen.expects(atLeastOnce()).method("displayCursor").with(eq(false));
    mockScreen.expects(atLeastOnce()).method("redraw");
    
    mockOutputStream1.expects(once()).method("isSelected").will(returnValue(true));
    mockOutputStream2.expects(once()).method("isSelected").will(returnValue(false));
    mockOutputStream3.expects(atLeastOnce()).method("isSelected").will(returnValue(false));
    mockOutputStream1.expects(once()).method("flush");
    
    mockInputStream0.expects(once()).method("getZsciiChar").will(returnValue((short) 'L'));
    
    input.setInputStream(0, inputStream0);
    input.setInputStream(1, inputStream1);
    short zchar = machine.getInputFunctions().readChar(0, 0);
    assertEquals((short) 'L', zchar);
  }
}
