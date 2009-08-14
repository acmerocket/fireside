/*
 * Copyright 2005-2009 by Wei-ju Wu
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
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.*;

import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.vm.InputFunctions;
import org.zmpp.vm.Machine;

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
@RunWith(JMock.class)
public class InputFunctionsTest {
  Mockery context = new JUnit4Mockery();
  private Machine machine;
  
  private InputFunctions inputFunctions;
  private int textbuffer;

  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);    
    inputFunctions = new InputFunctions(machine);
    textbuffer = 4711;
  }

  /**
   * Simple case: NULL as terminator.
   */
  @Test
  public void testHandleTerminateCharNull() {
    char result = inputFunctions.handleTerminateChar(ZsciiEncoding.NULL);
    assertEquals(ZsciiEncoding.NULL, result);
  }

  /**
   * Simple case: Newline as terminator.
   */
  @Test
  public void testHandleTerminateCharNewline() {
    context.checking(new Expectations() {{
      one (machine).printZsciiChar(ZsciiEncoding.NEWLINE);
    }});
    char result = inputFunctions.handleTerminateChar(ZsciiEncoding.NEWLINE);
    assertEquals(ZsciiEncoding.NEWLINE, result);
  }

  /**
   * Some other function key.
   */
  @Test
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
  @Test
  public void testCheckTerminationV4Null() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(4));
      one (machine).writeUnsigned8(textbuffer, (char) 0);
    }});
    int textpointer = 6;
    inputFunctions.checkTermination(ZsciiEncoding.NULL, textbuffer,
                                    textpointer);
  }

  @Test
  public void testTokenize() {
    final int parsebuffer = 123;
    final int dictionaryAddress = 456;
    final boolean tokenize = true;
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(3));
      // reading input
      one (machine).readUnsigned8(textbuffer); will(returnValue((char) 4));
      one (machine).readUnsigned8(textbuffer + 1); will(returnValue('w'));
      one (machine).readUnsigned8(textbuffer + 2); will(returnValue('a'));
      one (machine).readUnsigned8(textbuffer + 3); will(returnValue('i'));
      one (machine).readUnsigned8(textbuffer + 4); will(returnValue('t'));
      one (machine).getDictionaryDelimiters(); will(returnValue(", \t\n"));
      // filling parse buffer
      one (machine).readUnsigned8(parsebuffer); will(returnValue((char) 10));
      one (machine).writeUnsigned8(parsebuffer + 1, (char) 1);
      // lookup
      one (machine).lookupToken(dictionaryAddress, "wait");
      will(returnValue(987));
      // write parse buffer
      one (machine).writeUnsigned16(parsebuffer + 2, (char) 987);
      one (machine).writeUnsigned8(parsebuffer + 4, (char) 4);
      one (machine).writeUnsigned8(parsebuffer + 5, (char) 1);
    }});
    inputFunctions.tokenize(textbuffer, parsebuffer, dictionaryAddress,
                            tokenize);
  }

  /**
   * Terminated with newline, so 0 is appended to the input.
   */
  @Test
  public void testCheckTerminationV4Newline() {
    final int textpointer = 6;
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(4));
      one (machine).writeUnsigned8(textbuffer + textpointer, (char) 0);
    }});
    inputFunctions.checkTermination(ZsciiEncoding.NEWLINE, textbuffer,
                                    textpointer);
  }

  /**
   * Version 5 and the last character is null, print 0 to the beginning of the
   * text buffer.
   */
  @Test
  public void testCheckTerminationV5Null() {
    final int textpointer = 6;
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(5));
      one (machine).writeUnsigned8(textbuffer + 1, (char) 0);
    }});
    inputFunctions.checkTermination(ZsciiEncoding.NULL, textbuffer,
                                    textpointer);
  }

  /**
   * Version 5 and the last character is newline, print 5 to byte 1 of the
   * text buffer.
   */
  @Test
  public void testCheckTerminationV5Newline() {
    final int textpointer = 6;
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(5));
      one (machine).writeUnsigned8(textbuffer + 1, (char) (textpointer - 2));
    }});
    inputFunctions.checkTermination(ZsciiEncoding.NEWLINE, textbuffer,
                                    textpointer);
  }
}
