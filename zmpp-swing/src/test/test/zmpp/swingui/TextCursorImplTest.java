/*
 * $Id$
 * 
 * Created on 01/23/2006
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
package test.zmpp.swingui;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.swingui.CursorWindow;
import org.zmpp.swingui.TextCursorImpl;
import org.zmpp.vm.TextCursor;

/**
 * This class tests the TextCursorImpl class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TextCursorImplTest extends MockObjectTestCase {

  private TextCursor cursor;
  private Mock mockwindow;
  private CursorWindow window;
  
  protected void setUp() throws Exception {
  
    mockwindow = mock(CursorWindow.class);    
    window = (CursorWindow) mockwindow.proxy();
    cursor = new TextCursorImpl(window);
  }
  
  public void testInitialState() {
    
    assertEquals("Initial line position should be 1",  1, cursor.getLine());
    assertEquals("Initial column position should be 1", 1, cursor.getColumn());
  }

  public void testSetPosition() {
    
    mockwindow.expects(once()).method("updateCursorCoordinates");
    cursor.setPosition(3, 7);
    assertEquals("Line should be 3", 3, cursor.getLine());
    assertEquals("Column should be 7",  7, cursor.getColumn());
  }
  
  public void testSetLine() {

    cursor.setLine(5);
    assertEquals("Line should be 5", 5, cursor.getLine());
  }
 
  public void testSetColumn() {

    cursor.setColumn(19);
    assertEquals("Column should be 19", 19, cursor.getColumn());
  }
  
  public void testSetLineNegative() {
    
    cursor.setLine(-5);
    assertEquals("Negative values should be reset to 1", 1, cursor.getLine());
  }

  public void testSetColumnNegative() {
    
    cursor.setColumn(-23);
    assertEquals("Negative values should be reset to 1", 1, cursor.getColumn());
  }
  
  public void testSetPositionNegative() {
   
    mockwindow.expects(exactly(3)).method("updateCursorCoordinates");
    cursor.setPosition(-1, 5);
    assertEquals("Negative values should be reset to 1", 1, cursor.getLine());

    cursor.setPosition(6, -2);
    assertEquals("Negative values should be reset to 1", 1, cursor.getColumn());

    cursor.setPosition(-6, -2);
    assertEquals("Negative values should be reset to 1", 1, cursor.getLine());
    assertEquals("Negative values should be reset to 1", 1, cursor.getColumn());
  }
}
