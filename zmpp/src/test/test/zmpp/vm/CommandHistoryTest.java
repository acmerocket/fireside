/*
 * $Id$
 * 
 * Created on 03/10/2006
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

import junit.framework.TestCase;

import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.vm.CommandHistory;
import org.zmpp.vm.InputLine;

public class CommandHistoryTest extends TestCase 
implements InputLine {

  private CommandHistory history;
  
  protected void setUp() throws Exception {
    
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
  public void testResetInitial() {
    
    history.reset();
    assertEquals(0, history.getCurrentIndex());
    
    List<Character> inputline = new ArrayList<Character>();
    history.addInputLine(inputline);
    history.reset();
    assertEquals(1, history.getCurrentIndex());
  }
  
  public void testIsHistoryChar() {
    
    assertTrue(history.isHistoryChar(ZsciiEncoding.CURSOR_UP));
    assertTrue(history.isHistoryChar(ZsciiEncoding.CURSOR_DOWN));
    assertFalse(history.isHistoryChar(ZsciiEncoding.CURSOR_LEFT));
    assertFalse(history.isHistoryChar('a'));
  }
}
