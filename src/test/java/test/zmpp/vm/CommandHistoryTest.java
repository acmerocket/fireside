/*
 * Created on 03/10/2006
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.vm.CommandHistory;
import org.zmpp.vm.InputLine;

/**
 * Test class for CommandHistory.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class CommandHistoryTest implements InputLine {

  private CommandHistory history;
  
  @Before
  public void setUp() throws Exception {
    history = new CommandHistory(this);
  }

  // *********************************************************************
  // ***** Input line functions
  // **************************************

  public int deletePreviousChar(List<Character> inputbuffer, int pointer) {
    inputbuffer.remove(inputbuffer.size() - 1);
    return pointer - 1;
  }
  
  public int addChar(List<Character> inputbuffer,
      int textbuffer, int pointer, char zchar) {
    inputbuffer.add(zchar);
    return pointer + 1;
  }
  
  /**
   * Test if the reset will set the index to size(), which is 0.
   */
  @Test
  public void testResetInitial() {
    history.reset();
    assertEquals(0, history.getCurrentIndex());
    List<Character> inputline = new ArrayList<Character>();
    history.addInputLine(inputline);
    history.reset();
    assertEquals(1, history.getCurrentIndex());
  }
  
  @Test
  public void testIsHistoryChar() {
    assertTrue(history.isHistoryChar(ZsciiEncoding.CURSOR_UP));
    assertTrue(history.isHistoryChar(ZsciiEncoding.CURSOR_DOWN));
    assertFalse(history.isHistoryChar(ZsciiEncoding.CURSOR_LEFT));
    assertFalse(history.isHistoryChar('a'));
  }
}
