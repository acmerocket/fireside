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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.swingui.Canvas;
import org.zmpp.swingui.WordWrapper;

public class WordWrapperTest extends MockObjectTestCase {

  private WordWrapper bufferedWordWrapper;
  private WordWrapper unbufferedWordWrapper;
  private Mock mockCanvas;
  private Canvas canvas;
  private FontMetrics fontMetrics;
  private Font font;
  
  protected void setUp() throws Exception {
 
    mockCanvas = mock(Canvas.class);
    canvas = (Canvas) mockCanvas.proxy();
    JFrame frame = new JFrame();
    font = new Font("monospaced", Font.ROMAN_BASELINE, 8);
    
    fontMetrics =
      frame.getFontMetrics(font);
    bufferedWordWrapper = new WordWrapper(100, canvas, font, true);
    unbufferedWordWrapper = new WordWrapper(85, canvas, font, false);
  }

  public void testWrapBuffered() {

    String line = "A line that is to be wrapped";
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("A")).will(returnValue(fontMetrics.stringWidth("A")));
    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq(" ")).will(returnValue(fontMetrics.stringWidth(" ")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("line")).will(returnValue(fontMetrics.stringWidth("line")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("that")).will(returnValue(fontMetrics.stringWidth("that")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("is")).will(returnValue(fontMetrics.stringWidth("is")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("to")).will(returnValue(fontMetrics.stringWidth("to")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("be")).will(returnValue(fontMetrics.stringWidth("be")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("wrapped")).will(returnValue(fontMetrics.stringWidth("wrapped")));
    
    assertEquals(140, fontMetrics.stringWidth(line));
    
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(2, lines.length);
    assertEquals("A line that is to be\n", lines[0]);
    assertEquals("wrapped", lines[1]);
  }

  public void testWrapBufferedSingleNewline() {

    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("\n")).will(returnValue(fontMetrics.stringWidth("\n")));
    
    String line = "\n";
    assertEquals(0, fontMetrics.stringWidth(line));
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(1, lines.length);
    assertEquals("\n", lines[0]);
  }
  
  public void testWrapBufferedWithNewLine() {
    
    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq(" ")).will(returnValue(fontMetrics.stringWidth(" ")));
    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq("\n")).will(returnValue(fontMetrics.stringWidth("\n")));
    
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("A")).will(returnValue(fontMetrics.stringWidth("A")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("line")).will(returnValue(fontMetrics.stringWidth("line")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("that")).will(returnValue(fontMetrics.stringWidth("that")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("is")).will(returnValue(fontMetrics.stringWidth("is")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("to")).will(returnValue(fontMetrics.stringWidth("to")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("be")).will(returnValue(fontMetrics.stringWidth("be")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("wrapped")).will(returnValue(fontMetrics.stringWidth("wrapped")));
    
    String line =  "A line that\nis to\nbe wrapped";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(3, lines.length);
    assertEquals("A line that\n", lines[0]);
    assertEquals("is to\n", lines[1]);
    assertEquals("be wrapped", lines[2]);
  }

  public void testWrapBufferedWithNewLineEndsWithNewLine() {

    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq(" ")).will(returnValue(fontMetrics.stringWidth(" ")));
    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq("\n")).will(returnValue(fontMetrics.stringWidth("\n")));
    
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("A")).will(returnValue(fontMetrics.stringWidth("A")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("line")).will(returnValue(fontMetrics.stringWidth("line")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("that")).will(returnValue(fontMetrics.stringWidth("that")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("is")).will(returnValue(fontMetrics.stringWidth("is")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("to")).will(returnValue(fontMetrics.stringWidth("to")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("be")).will(returnValue(fontMetrics.stringWidth("be")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("wrapped")).will(returnValue(fontMetrics.stringWidth("wrapped")));
    
    String line =  "A line that\nis to\nbe wrapped\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(3, lines.length);
    assertEquals("A line that\n", lines[0]);
    assertEquals("is to\n", lines[1]);
    assertEquals("be wrapped\n", lines[2]);
    //assertEquals("", lines[3]);
  }

  public void testWrapBufferedWithNewLineStartsWithNewLine() {
    
    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq(" ")).will(returnValue(fontMetrics.stringWidth(" ")));
    mockCanvas.expects(atLeastOnce()).method("getStringWidth").with(
        eq(font), eq("\n")).will(returnValue(fontMetrics.stringWidth("\n")));
    
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("A")).will(returnValue(fontMetrics.stringWidth("A")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("line")).will(returnValue(fontMetrics.stringWidth("line")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("that")).will(returnValue(fontMetrics.stringWidth("that")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("is")).will(returnValue(fontMetrics.stringWidth("is")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("to")).will(returnValue(fontMetrics.stringWidth("to")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("be")).will(returnValue(fontMetrics.stringWidth("be")));
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("wrapped")).will(returnValue(fontMetrics.stringWidth("wrapped")));
    
    
    String line =  "\nA line that\nis to\nbe wrapped\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(4, lines.length);
    assertEquals("\n", lines[0]);
    assertEquals("A line that\n", lines[1]);
    assertEquals("is to\n", lines[2]);
    assertEquals("be wrapped\n", lines[3]);
  }
  
  public void testWrapBufferedNewLine() {
    
    mockCanvas.expects(once()).method("getStringWidth").with(
        eq(font), eq("\n")).will(returnValue(fontMetrics.stringWidth("\n")));
    
    String line =  "\n";
    String[] lines = bufferedWordWrapper.wrap(0, line);
    assertEquals(1, lines.length);
    assertEquals("\n", lines[0]);
  }
  
  public void testWrapUnbuffered() {

    mockCanvas.expects(atLeastOnce()).method("getCharWidth").with(
        eq(font), eq(' ')).will(returnValue(fontMetrics.charWidth(' ')));
    
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('A')).will(returnValue(fontMetrics.charWidth('A')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('l')).will(returnValue(fontMetrics.charWidth('l')));
    mockCanvas.expects(exactly(3)).method("getCharWidth").with(
        eq(font), eq('i')).will(returnValue(fontMetrics.charWidth('i')));
    mockCanvas.expects(exactly(2)).method("getCharWidth").with(
        eq(font), eq('n')).will(returnValue(fontMetrics.charWidth('n')));
    mockCanvas.expects(exactly(3)).method("getCharWidth").with(
        eq(font), eq('e')).will(returnValue(fontMetrics.charWidth('e')));
    mockCanvas.expects(exactly(3)).method("getCharWidth").with(
        eq(font), eq('t')).will(returnValue(fontMetrics.charWidth('t')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('h')).will(returnValue(fontMetrics.charWidth('h')));
    mockCanvas.expects(exactly(2)).method("getCharWidth").with(
        eq(font), eq('a')).will(returnValue(fontMetrics.charWidth('a')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('s')).will(returnValue(fontMetrics.charWidth('s')));
    mockCanvas.expects(exactly(2)).method("getCharWidth").with(
        eq(font), eq('g')).will(returnValue(fontMetrics.charWidth('g')));
    mockCanvas.expects(exactly(2)).method("getCharWidth").with(
        eq(font), eq('o')).will(returnValue(fontMetrics.charWidth('o')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('b')).will(returnValue(fontMetrics.charWidth('b')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('w')).will(returnValue(fontMetrics.charWidth('w')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('r')).will(returnValue(fontMetrics.charWidth('r')));
    mockCanvas.expects(exactly(2)).method("getCharWidth").with(
        eq(font), eq('p')).will(returnValue(fontMetrics.charWidth('p')));
    mockCanvas.expects(once()).method("getCharWidth").with(
        eq(font), eq('d')).will(returnValue(fontMetrics.charWidth('d')));
    
    String line = "A line that is going to be wrapped";
    assertEquals(170, fontMetrics.stringWidth(line));
    String[] lines = unbufferedWordWrapper.wrap(0, line);
    assertEquals(2, lines.length);
    assertEquals("A line that is go", lines[0]);
    assertEquals("ing to be wrapped", lines[1]);
  }
}
