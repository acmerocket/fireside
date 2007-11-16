/*
 * $Id$
 *
 * Copyright 2005-2007 by Wei-ju Wu
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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.io.InputStream;
import org.zmpp.vm.GameData;
import org.zmpp.vm.Input;
import org.zmpp.vm.InputFunctions;
import org.zmpp.vm.Machine;
import org.zmpp.vm.Output;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.InputFunctions.InterruptThread;

/**
 * This class tests the functions in InputFunctionsImpl. Normally this would
 * only be readChar() and readLine(). readLine() is a pretty complex function
 * so we break it up into its parts and test them separately. This has the
 * disadvantage of exposing routines that would normally be private, but
 * we get to test them as separate unit which facilitates testing.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class InputFunctionsTest extends MockObjectTestCase {

  private Mock mockFileHeader, mockMachine, mockMemory, mockInputStream,
               mockScreen, mockGameData, mockOutput, mockInput;
  
  private Machine machine;
  private GameData gamedata;
  private StoryFileHeader fileheader;
  private Memory memory;
  private InputStream inputstream;
  private ScreenModel screen;
  private ZsciiEncoding encoding;
  private Output output;
  private Input input;
  
  private InputFunctions inputFunctions;
  private int textbuffer;
  private List<Short> inputbuffer;

  protected void setUp() throws Exception {

    mockMachine = mock(Machine.class);
    mockFileHeader = mock(StoryFileHeader.class);
    mockMemory = mock(Memory.class);
    mockInputStream = mock(InputStream.class);
    mockScreen = mock(ScreenModel.class);
    mockGameData = mock(GameData.class);
    mockOutput = mock(Output.class);
    mockInput = mock(Input.class);

    machine = (Machine) mockMachine.proxy();
    fileheader = (StoryFileHeader) mockFileHeader.proxy();
    memory = (Memory) mockMemory.proxy();
    inputstream = (InputStream) mockInputStream.proxy();
    screen = (ScreenModel) mockScreen.proxy();
    gamedata = (GameData) mockGameData.proxy();
    output = (Output) mockOutput.proxy();
    input = (Input) mockInput.proxy();
    
    encoding = new ZsciiEncoding(new DefaultAccentTable());
    
    inputFunctions = new InputFunctions(machine);
    textbuffer = 4711;
    inputbuffer = new ArrayList<Short>();
  }

  // *********************************************************************
  // **** Testing the "previous" input function
  // ***************************************************
  /**
   * We want to check for the simple case that nothing happens. This is the
   * case when the version number is < 5.
   */
  public void testCheckForPreviousInputV3() {

    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    
    int pointer = inputFunctions.checkForPreviousInput(textbuffer, inputbuffer);
    assertEquals(1, pointer);
    assertEquals(0, inputbuffer.size());
  }

  /**
   * We check for the case that input exists in version 5. We simulate
   * that there is the text "Start" already in the buffer.
   */
  public void testCheckForPreviousInputV5() {

    //ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(5));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    //mockServices.expects(once()).method("getZsciiEncoding").will(returnValue(encoding));
    
    // Previous input
    mockMemory.expects(once()).method("readByte").with(eq(textbuffer + 1)).will(returnValue((byte) 5));
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer + 2)).will(returnValue((short) 'S'));
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer + 3)).will(returnValue((short) 't'));
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer + 4)).will(returnValue((short) 'a'));
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer + 5)).will(returnValue((short) 'r'));
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer + 6)).will(returnValue((short) 't'));
        
    int pointer = inputFunctions.checkForPreviousInput(textbuffer, inputbuffer);
    
    List<Short> compareBuffer = new ArrayList<Short>();
    compareBuffer.add((short)'S');
    compareBuffer.add((short)'t');
    compareBuffer.add((short)'a');
    compareBuffer.add((short)'r');
    compareBuffer.add((short)'t');
    assertEquals(7, pointer);
    assertEquals(5, inputbuffer.size());
    assertEquals(compareBuffer, inputbuffer);
  }

  /**
   * Now we are in version 5, but no previous input is available. In this
   * case we expect, that the size byte is skipped.
   */
  public void testCheckForPreviousInputV5Empty() {

    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(5));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    
    // Previous input
    mockMemory.expects(once()).method("readByte").with(eq(4712)).will(returnValue((byte) 0));
        
    int pointer = inputFunctions.checkForPreviousInput(textbuffer, inputbuffer);
    
    assertEquals(2, pointer); // the length byte is skipped of course
    assertEquals(0, inputbuffer.size());
  }

  // *********************************************************************
  // **** Testing the startInputThread() function, which implements
  // **** the timed input behaviour
  // ***************************************************
  
  /**
   * V3, time and routineAddress are 0, so there will be no thread.
   */
  public void testStartInputThreadV3() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    int routineAddress = 0;
    int time = 0;
    
    InterruptThread thread = inputFunctions.startInterruptThread(routineAddress, time, inputbuffer);
    assertNull(thread);
  }

  /**
   * The version number is sufficient, but either routineAddress, time or
   * both are 0, so there will be no thread.
   */
  public void testStartInputThreadV4NoCreate() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    
    int routineAddress = 0;
    int time = 1000;    
    InterruptThread thread = inputFunctions.startInterruptThread(routineAddress, time, inputbuffer);
    assertNull(thread);
    
    routineAddress = 4711;
    time = 0;
    thread = inputFunctions.startInterruptThread(routineAddress, time, inputbuffer);
    assertNull(thread);
  
    routineAddress = 0;
    time = 0;
    thread = inputFunctions.startInterruptThread(routineAddress, time, inputbuffer);
    assertNull(thread);
  }
  
  /**
   * Now every condition for thread creation is fulfilled.
   */
  /*
  public void testStartInputThreadV4DoCreate() {
    
    mockMachine.expects(atLeastOnce()).method("getServices").will(returnValue(services));
    mockServices.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    
    int routineAddress = 4711;
    int time = 100;    
    InterruptThread thread = inputFunctions.startInterruptThread(routineAddress, time, inputbuffer);
    assertNotNull(thread);
  }*/
  
  // *********************************************************************
  // **** Testing the terminateInputThread() function
  // ***************************************************

  /**
   * Check if the functions does not choke on null.
   */
  public void testTerminateInputThreadNull() {
    
    inputFunctions.terminateInterruptThread(null);
  }
  
  /*
  public void testTerminateInputThread() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("getRoutineContexts").will(returnValue(contextStack));
    
    int routineAddress = 4711;
    int time = 100;    
    InterruptThread thread = inputFunctions.startInterruptThread(routineAddress, time, inputbuffer);
    inputFunctions.terminateInterruptThread(thread);
    assertFalse(thread.isRunning());
    assertFalse(thread.isAlive());
  }*/

  // *********************************************************************
  // **** Testing the handleTerminateChar() function
  // ***************************************************

  /**
   * Simple case: NULL as terminator.
   */
  public void testHandleTerminateCharNull() {
    
    short result = inputFunctions.handleTerminateChar(ZsciiEncoding.NULL);
    assertEquals(ZsciiEncoding.NULL, result);
  }

  /**
   * Simple case: Newline as terminator.
   */
  public void testHandleTerminateCharNewline() {
    
    mockMachine.expects(once()).method("getOutput").will(returnValue(output));
    mockOutput.expects(once()).method("printZsciiChar").with(eq((short)ZsciiEncoding.NEWLINE), eq(false));
    short result = inputFunctions.handleTerminateChar(ZsciiEncoding.NEWLINE);
    assertEquals(ZsciiEncoding.NEWLINE, result);
  }

  /**
   * Some other function key.
   */
  public void testHandleTerminateSomeFunctionKey() {
    
    short result = inputFunctions.handleTerminateChar((short) 130);
    assertEquals((short) 130, result);
  }
  
  // *********************************************************************
  // **** Testing the checkTermination() function
  // ***************************************************

  /**
   * Terminated with null, so it was aborted, and input is empty.
   */
  public void testCheckTerminationV4Null() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeByte").with(eq(textbuffer), eq((byte) 0));
    
    int textpointer = 6;
    inputFunctions.checkTermination(ZsciiEncoding.NULL, textbuffer, textpointer);
  }

  /**
   * Terminated with newline, so 0 is appended to the input.
   */
  public void testCheckTerminationV4Newline() {

    int textpointer = 6;
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeByte").with(eq(textbuffer + textpointer), eq((byte) 0));
    
    inputFunctions.checkTermination(ZsciiEncoding.NEWLINE, textbuffer, textpointer);
  }

  /**
   * Version 5 and the last character is null, print 0 to the beginning of the text buffer.
   */
  public void testCheckTerminationV5Null() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 1), eq((short) 0));
    
    int textpointer = 6;
    inputFunctions.checkTermination(ZsciiEncoding.NULL, textbuffer, textpointer);
  }

  /**
   * Version 5 and the last character is newline, print 5 to byte 1 of the text buffer.
   */
  public void testCheckTerminationV5Newline() {
    
    int textpointer = 6;
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 1),
        eq((short) (textpointer - 2)));
    
    inputFunctions.checkTermination(ZsciiEncoding.NEWLINE, textbuffer, textpointer);
  }

  // *********************************************************************
  // **** Testing the doInputLoop() function
  // ***************************************************

  /**
   * This simulates a typical V5 situation, without previous input. pointer
   * and pointerstart are equal.
   */
  public void testDoInputLoopV5() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockGameData.expects(atLeastOnce()).method("getZsciiEncoding").will(returnValue(encoding));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockFileHeader.expects(atLeastOnce()).method("getTerminatorsAddress").will(returnValue(0));
    mockGameData.expects(atLeastOnce()).method("getMemory").will(returnValue(memory));
    mockMachine.expects(atLeastOnce()).method("getOutput").will(returnValue(output));
    mockMachine.expects(atLeastOnce()).method("getInput").will(returnValue(input));
    
    // The maximum number of characters
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer)).will(returnValue((short) 200));
    mockInput.expects(atLeastOnce()).method("getSelectedInputStream").will(returnValue(inputstream));
    mockInputStream.expects(atLeastOnce()).method("getZsciiChar").will(onConsecutiveCalls(
        returnValue((short) 'h'), returnValue((short) 'i'), returnValue(ZsciiEncoding.NEWLINE)));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 2), eq((short) 'h'));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 3), eq((short) 'i'));
    
    // echo
    mockOutput.expects(once()).method("printZsciiChar").with(eq((short) 'h'), eq(true));
    mockOutput.expects(once()).method("printZsciiChar").with(eq((short) 'i'), eq(true));
    
    // screen set cursor expected
    mockMachine.expects(atLeastOnce()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(atLeastOnce()).method("displayCursor").withAnyArguments();
    mockScreen.expects(atLeastOnce()).method("redraw");
    
    // We set them to the standard value 2 (1 available byte + 1 typed byte)
    int pointer = 2;
    
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 1), eq((short) 2));
    
    inputFunctions.doInputLoop(textbuffer, pointer, inputbuffer);
  }
  
  // TODO: Simulate a situation with a terminators table
  public void testDoInputLoopV5WithTerminators() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockGameData.expects(atLeastOnce()).method("getZsciiEncoding").will(returnValue(encoding));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockFileHeader.expects(atLeastOnce()).method("getTerminatorsAddress").will(returnValue(1234));
    mockGameData.expects(atLeastOnce()).method("getMemory").will(returnValue(memory));
    mockMachine.expects(atLeastOnce()).method("getOutput").will(returnValue(output));
    mockMachine.expects(atLeastOnce()).method("getInput").will(returnValue(input));
    
    // The maximum number of characters
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(textbuffer)).will(returnValue((short) 200));
    mockInput.expects(atLeastOnce()).method("getSelectedInputStream").will(returnValue(inputstream));
    mockInputStream.expects(atLeastOnce()).method("getZsciiChar").will(onConsecutiveCalls(
        returnValue((short) 'h'), returnValue((short) 'i'), returnValue((short) 130)));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 2), eq((short) 'h'));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 3), eq((short) 'i'));
    
    // Read the terminating characters
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(1234)).will(returnValue((short) 130));
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(1235)).will(returnValue((short) 0));
    
    // echo
    mockOutput.expects(once()).method("printZsciiChar").with(eq((short) 'h'), eq(true));
    mockOutput.expects(once()).method("printZsciiChar").with(eq((short) 'i'), eq(true));
    
    // screen set cursor expected
    mockMachine.expects(atLeastOnce()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(atLeastOnce()).method("displayCursor").withAnyArguments();
    mockScreen.expects(atLeastOnce()).method("redraw");
    
    // We set them to the standard value 2 (1 available byte + 1 typed byte)
    int pointer = 2;
    
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 1), eq((short) 2));
    
    inputFunctions.doInputLoop(textbuffer, pointer, inputbuffer);
  }  
}
