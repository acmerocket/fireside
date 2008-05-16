/*
 * $Id$
 *
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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.io.InputStream;
import org.zmpp.vm.InputFunctions;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StoryFileHeader;

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
               mockScreen;
  
  private Machine machine;
  private StoryFileHeader fileheader;
  private Memory memory;
  private InputStream inputstream;
  private ScreenModel screen;
  private ZsciiEncoding encoding;
  
  private InputFunctions inputFunctions;
  private int textbuffer;
  private List<Character> inputbuffer;

  @Override
  protected void setUp() throws Exception {
    mockMachine = mock(Machine.class);
    mockFileHeader = mock(StoryFileHeader.class);
    mockMemory = mock(Memory.class);
    mockInputStream = mock(InputStream.class);
    mockScreen = mock(ScreenModel.class);

    machine = (Machine) mockMachine.proxy();
    fileheader = (StoryFileHeader) mockFileHeader.proxy();
    memory = (Memory) mockMemory.proxy();
    inputstream = (InputStream) mockInputStream.proxy();
    screen = (ScreenModel) mockScreen.proxy();
    
    encoding = new ZsciiEncoding(new DefaultAccentTable());
    
    inputFunctions = new InputFunctions(machine);
    textbuffer = 4711;
    inputbuffer = new ArrayList<Character>();
  }

  /**
   * Simple case: NULL as terminator.
   */
  public void testHandleTerminateCharNull() {
    char result = inputFunctions.handleTerminateChar(ZsciiEncoding.NULL);
    assertEquals(ZsciiEncoding.NULL, result);
  }

  /**
   * Simple case: Newline as terminator.
   */
  public void testHandleTerminateCharNewline() {
    mockMachine.expects(once()).method("printZsciiChar").with(eq((char)ZsciiEncoding.NEWLINE), eq(false));
    char result = inputFunctions.handleTerminateChar(ZsciiEncoding.NEWLINE);
    assertEquals(ZsciiEncoding.NEWLINE, result);
  }

  /**
   * Some other function key.
   */
  public void testHandleTerminateSomeFunctionKey() {
    char result = inputFunctions.handleTerminateChar((char) 130);
    assertEquals((short) 130, result);
  }
  
  // *********************************************************************
  // **** Testing the checkTermination() function
  // ***************************************************

  /**
   * Terminated with null, so it was aborted, and input is empty.
   */
  public void testCheckTerminationV4Null() {
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeByte").with(eq(textbuffer), eq((byte) 0));
    
    int textpointer = 6;
    inputFunctions.checkTermination(ZsciiEncoding.NULL, textbuffer, textpointer);
  }

  /**
   * Terminated with newline, so 0 is appended to the input.
   */
  public void testCheckTerminationV4Newline() {
    int textpointer = 6;
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeByte").with(eq(textbuffer + textpointer), eq((byte) 0));    
    inputFunctions.checkTermination(ZsciiEncoding.NEWLINE, textbuffer, textpointer);
  }

  /**
   * Version 5 and the last character is null, print 0 to the beginning of the text buffer.
   */
  public void testCheckTerminationV5Null() {
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockMachine.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 1), eq((short) 0));
    
    int textpointer = 6;
    inputFunctions.checkTermination(ZsciiEncoding.NULL, textbuffer, textpointer);
  }

  /**
   * Version 5 and the last character is newline, print 5 to byte 1 of the text buffer.
   */
  public void testCheckTerminationV5Newline() {
    int textpointer = 6;
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockMachine.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(textbuffer + 1),
        eq((short) (textpointer - 2)));
    inputFunctions.checkTermination(ZsciiEncoding.NEWLINE, textbuffer, textpointer);
  }
}
