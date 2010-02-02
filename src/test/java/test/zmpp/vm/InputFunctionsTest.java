/*
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
