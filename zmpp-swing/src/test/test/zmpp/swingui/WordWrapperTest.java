/*
 * $Id$
 * 
 * Created on 12/01/2005
 * Copyright 2005-2006 by Wei-ju Wu
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
package test.zmpp.swingui;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.zmpp.swingui.WordWrapper;

public class WordWrapperTest extends TestCase {

  private WordWrapper bufferedWordWrapper;
  private WordWrapper unbufferedWordWrapper;
  private FontMetrics fontMetrics;
  
  protected void setUp() throws Exception {
 
    JFrame frame = new JFrame();
    fontMetrics =
      frame.getFontMetrics(new Font("Courier", Font.ROMAN_BASELINE, 8));
    bufferedWordWrapper = new WordWrapper(100, fontMetrics, true);
    unbufferedWordWrapper = new WordWrapper(85, fontMetrics, false);
  }

  public void testWrapBuffered() {

    String line = "A line that is to be wrapped";
    assertEquals(140, fontMetrics.stringWidth(line));
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(2, lines.length);
    assertEquals("A line that is to be", lines[0]);
    assertEquals(" wrapped", lines[1]);
  }

  public void testWrapBufferedSingleNewline() {

    String line = "\n";
    assertEquals(5, fontMetrics.stringWidth(line));
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(1, lines.length);
    assertEquals("", lines[0]);
  }
  
  public void testWrapBufferedWithNewLine() {
    
    String line =  "A line that\nis to\nbe wrapped";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(3, lines.length);
    assertEquals("A line that", lines[0]);
    assertEquals("is to", lines[1]);
    assertEquals("be wrapped", lines[2]);
  }

  public void testWrapBufferedWithNewLineEndsWithNewLine() {
    
    String line =  "A line that\nis to\nbe wrapped\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(4, lines.length);
    assertEquals("A line that", lines[0]);
    assertEquals("is to", lines[1]);
    assertEquals("be wrapped", lines[2]);
    assertEquals("", lines[3]);
  }

  public void testWrapBufferedWithNewLineStartsWithNewLine() {
    
    String line =  "\nA line that\nis to\nbe wrapped\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(5, lines.length);
    assertEquals("", lines[0]);
    assertEquals("A line that", lines[1]);
    assertEquals("is to", lines[2]);
    assertEquals("be wrapped", lines[3]);
    assertEquals("", lines[4]);
  }
  
  public void testWrapBufferedNewLine() {
    
    String line =  "\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(1, lines.length);
    assertEquals("", lines[0]);
  }
  
  public void testWrapUnbuffered() {

    String line = "A line that is going to be wrapped";
    assertEquals(170, fontMetrics.stringWidth(line));
    String[] lines = unbufferedWordWrapper.wrap(0, line);
    assertEquals(2, lines.length);
    assertEquals("A line that is go", lines[0]);
    assertEquals("ing to be wrapped", lines[1]);
  }
}
