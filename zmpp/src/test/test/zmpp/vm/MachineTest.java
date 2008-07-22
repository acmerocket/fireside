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
package test.zmpp.vm;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.zmpp.iff.WritableFormChunk;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Input;
import org.zmpp.vm.MachineRunState;
import org.zmpp.vm.MemoryOutputStream;
import org.zmpp.vm.Output;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;
import org.zmpp.vm.StoryFileHeader.Attribute;

/**
 * Tests the external i/o of the machine.
 * @author Wei-ju Wu
 * @version 1.0
 */
@RunWith(JMock.class)
public class MachineTest extends MiniZorkSetup {
  Mockery context = new JUnit4Mockery();
  private OutputStream outputStream1, outputStream2, outputStream3;
  private InputStream inputStream1, inputStream0;
  private StatusLine statusLine;
  private ScreenModel screen;
  private SaveGameDataStore datastore;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    statusLine = context.mock(StatusLine.class);
    screen = context.mock(ScreenModel.class);
    outputStream1 = context.mock(OutputStream.class, "outputStream1");
    outputStream2 = context.mock(OutputStream.class, "outputStream2");
    outputStream3 = context.mock(OutputStream.class, "outputStream3");

    inputStream0 = context.mock(InputStream.class, "inputStream0");
    inputStream1 = context.mock(InputStream.class, "inputStrean1");
    
    machine.setScreen(screen);
    
    machine.setOutputStream(Output.OUTPUTSTREAM_SCREEN, outputStream1);
    machine.setOutputStream(Output.OUTPUTSTREAM_TRANSCRIPT, outputStream2);
    machine.setOutputStream(Output.OUTPUTSTREAM_MEMORY, outputStream3);
    
    machine.setInputStream(Input.INPUTSTREAM_KEYBOARD, inputStream0);
    machine.setInputStream(Input.INPUTSTREAM_FILE, inputStream1);
    
    datastore = context.mock(SaveGameDataStore.class);
  }
  
  @Test
  public void testInitialState() {    
    assertEquals(fileheader, machine.getFileHeader());
    assertTrue(machine.hasValidChecksum());
  }

  @Test
  public void testSetOutputStream() {
    context.checking(new Expectations() {{
      one (outputStream1).select(true);
      one (outputStream2).select(false);
      atLeast(1).of (outputStream1).isSelected(); will(returnValue(true));
      atLeast(1).of (outputStream2).isSelected(); will(returnValue(false));
      atLeast(1).of (outputStream3).isSelected(); will(returnValue(false));
      exactly(2).of (outputStream1).print('t');
      one (outputStream1).print('e');
      one (outputStream1).print('s');
    }});    
    machine.selectOutputStream(1, true);    
    machine.print("test");
  }
  
  @Test
  public void testSelectOutputStream() {
    context.checking(new Expectations() {{
      one (outputStream1).select(true);
    }});
    machine.selectOutputStream(1, true);
  }
  
  @Test
  public void testInputStream1() {
    machine.setInputStream(Input.INPUTSTREAM_KEYBOARD, inputStream0);
    machine.setInputStream(Input.INPUTSTREAM_FILE, inputStream1);    
    machine.selectInputStream(Input.INPUTSTREAM_FILE);
    assertEquals(inputStream1, machine.getSelectedInputStream());
  }

  @Test
  public void testInputStream0() {    
    machine.setInputStream(Input.INPUTSTREAM_KEYBOARD, inputStream0);
    machine.setInputStream(Input.INPUTSTREAM_FILE, inputStream1);
    machine.selectInputStream(Input.INPUTSTREAM_KEYBOARD);
    assertEquals(inputStream0, machine.getSelectedInputStream());
  }
  
  @Test
  public void testRandom() {
    char random1 = machine.random((short) 23);
    assertTrue(0 < random1 && random1 <= 23);    
    assertEquals(0, machine.random((short) 0));
    
    char random2 = machine.random((short) 23);    
    assertTrue(0 < random2 && random2 <= 23);
    assertEquals(0, machine.random((short) -23));
    
    char random3 = machine.random((short) 23);
    assertTrue(0 < random3 && random3 <= 23);
  }
  
  @Test
  public void testRandom1() {
    char value;
    for (int i = 0; i < 10; i++) {
      value = machine.random((short) 1);
      assertEquals(value, 1);
    }
  }

  @Test
  public void testRandom2() { 
    char value;
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

  @Test
  public void testStartQuit() {
    context.checking(new Expectations() {{
      one (outputStream2).select(false);
      atLeast(1).of (outputStream1).isSelected(); will(returnValue(true));
      atLeast(1).of (outputStream2).isSelected(); will(returnValue(false));
      atLeast(1).of (outputStream3).isSelected(); will(returnValue(false));
      atLeast(1).of (outputStream1).print(with(any(char.class)));
      atLeast(1).of (outputStream1).flush();
      one (outputStream1).close();
      atLeast(1).of (outputStream2).flush();
      one (outputStream2).close();
      atLeast(1).of (outputStream3).flush();
      one (outputStream3).close();
      one (inputStream0).close();
      one (inputStream1).close();
    }});    
    machine.start();
    assertEquals(MachineRunState.RUNNING, machine.getRunState());
    machine.quit();
    assertEquals(MachineRunState.STOPPED, machine.getRunState());
  }
  
  @Test
  public void testStatusLineScore() {    
    context.checking(new Expectations() {{
      one (statusLine).updateStatusScore(with(any(String.class)),
        with(any(int.class)), with(any(int.class)));
    }});
    machine.setVariable((char) 0x10, (char) 2);
    machine.setStatusLine(statusLine);
    machine.updateStatusLine();
  }
  
  @Test
  public void testStatusLineTime() {
    context.checking(new Expectations() {{
      one (statusLine).updateStatusTime(with(any(String.class)),
        with(any(int.class)), with(any(int.class)));
    }});
    machine.setVariable((char) 0x10, (char) 2);
    machine.setStatusLine(statusLine); // set the "time" flag
    machine.writeUnsigned8(1, (char) 2);
    machine.updateStatusLine();
  }
  
  @Test
  public void testGetSetScreen() {
    machine.setScreen(screen);
    assertTrue(screen == machine.getScreen());
  }

  @Test
  public void testHalt() {
    context.checking(new Expectations() {{
      one (outputStream2).select(false);
      atLeast(1).of (outputStream1).isSelected(); will(returnValue(true));
      atLeast(1).of (outputStream2).isSelected(); will(returnValue(false));
      atLeast(1).of (outputStream3).isSelected(); will(returnValue(false));
      allowing (outputStream1).print(with(any(char.class)));
    }});
    machine.start();    
    assertEquals(MachineRunState.RUNNING, machine.getRunState());
    machine.halt("error");
    assertEquals(MachineRunState.STOPPED, machine.getRunState());
  }
  
  @Test
  public void testRestart() {
    context.checking(new Expectations() {{
      one (outputStream1).flush();
      one (outputStream2).flush();
      one (outputStream3).flush();
      one (screen).reset();
    }});
    machine.restart();
  }
  
  @Test
  public void testSave() {
    context.checking(new Expectations() {{
      one (datastore).saveFormChunk(with(any(WritableFormChunk.class)));
      will(returnValue(true));
    }});
    machine.setSaveGameDataStore(datastore);
    assertTrue(machine.save((char) 4711));
  }
  
  @Test
  public void testSelectTranscriptOutputStream() {
    context.checking(new Expectations() {{
      one (outputStream2).select(true);
    }});
    machine.selectOutputStream(Output.OUTPUTSTREAM_TRANSCRIPT, true);
    assertTrue(machine.getFileHeader().isEnabled(Attribute.TRANSCRIPTING));    
  }
  
  @Test
  public void testSelectMemoryOutputStreamWithoutTable() {
    context.checking(new Expectations() {{
      atLeast(1).of (outputStream2).select(false);
      one (outputStream3).select(true);
      one (outputStream1).isSelected(); will(returnValue(true));
      atLeast(1).of (outputStream2).isSelected(); will(returnValue(false));
      atLeast(1).of (outputStream3).isSelected(); will(returnValue(false));
      // error message
      allowing (outputStream1).print(with(any(char.class)));
    }});
    machine.selectOutputStream(Output.OUTPUTSTREAM_MEMORY, true);
  }
  
  private int tableAddress;
  
  @Test
  public void testSelectMemoryOutputStreamWithTable() {  
    MemoryOutputStream memstream = new MemoryOutputStream(machine) {
      
      @Override
      public void select(int table, int tableWidth) {
        tableAddress = table;
      }
    };
    machine.setOutputStream(Output.OUTPUTSTREAM_MEMORY, memstream);
    machine.selectOutputStream3(4711, 0);
    
    assertEquals(4711, tableAddress);
  }
}
