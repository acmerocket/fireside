/*
 * $Id$
 * 
 * Created on 2005/11/19
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
package org.zmpp.swingui;

import java.awt.Color;
import java.awt.Font;

import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.TextCursor;

/**
 * The class SubWindow manages a sub window within the screen model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public abstract class SubWindow implements CursorWindow {

  private Canvas canvas;
  private TextCursor cursor;
  private LineEditor editor;
  private ScreenModel screen;
  
  private int top;
  private int height;
  private Color foreground;
  private Color background;
 
  private int fontnumber;
  private Font font;
  private boolean isReverseVideo;
  
  private String name;
 
  /**
   * Constructor.
   * 
   * @param parentComponent the parent component
   * @param editor the line editor
   * @param canvas the canvas
   * @param name the window name
   */
  public SubWindow(ScreenModel screen, LineEditor editor,
                   Canvas canvas, String name) {
    
    this.canvas = canvas;
    this.editor = editor;
    this.screen = screen;
    this.cursor = new TextCursorImpl(this);
    this.name = name;
  }
  
  public int getFontNumber() { return fontnumber; }
  
  public void setFontNumber(int fontnumber) { this.fontnumber = fontnumber; }
  
  /**
   * Sets the reverse video text mode.
   * 
   * @param flag true or false to activate or deactivate reverse video
   */
  public void setReverseVideo(boolean flag) { isReverseVideo = flag; }
  
  /**
   * Returns the reverse video status.
   * 
   * @return true if reverse video, false otherwise
   */
  public boolean isReverseVideo() { return isReverseVideo; }

  /**
   * Access to this window's cursor.
   * 
   * @return the cursor
   */
  public TextCursor getCursor() {
   
    return cursor;
  }
  
  public int getHeight() {
    
    return height;
  }
  
  public int getTop() {
    
    return top;
  }
  
  public void setVerticalBounds(int top, int height) {
    
    this.top = top;
    this.height = height;
    sizeUpdated();
  }
  
  public void resize(int numLines) {
    
    height = getCanvas().getFontHeight(font) * numLines;
    sizeUpdated();
  }
  
  /**
   * Sets this window's current font.
   * 
   * @param font the current font
   */
  public void setFont(Font font) {
    
    this.font = font;
  }
  
  /**
   * Returns this window's current font.
   * 
   * @return the current font
   */
  public Font getFont() {
    
    return font;
  }
  
  public void clear() {
    
    clipToCurrentBounds();
    canvas.fillRect(background, 0, getTop(), canvas.getWidth(), height);
    resetCursorToHome();
  }
  
  public void eraseLine() {
    
    int currentX = getCurrentX();
    clipToCurrentBounds();
    canvas.fillRect(background, currentX,
                    getCurrentY() - canvas.getFontAscent(font),
                    canvas.getWidth() - currentX,
                    canvas.getFontHeight(font));
  }
  
  public void setBackground(Color color) {
    
    background = color;
  }
  
  public Color getBackground() {
    
    return background;
  }
  
  public void setForeground(Color color) {
    
    foreground = color;
  }
  
  /**
   * This is the function that does the actual printing to the screen.
   * 
   * @param str a string to pring
   */
  public void printString(String str) {

    //System.out.printf("printString(), %s: '%s'\n", name, str);
    int width = canvas.getWidth();
    int lineLength = width;
    
    WordWrapper wordWrapper =
      new WordWrapper(lineLength, canvas, font, isBuffered());
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);    
  }
 
  public String toString() {
    
    return "[" + name + "]";
  }

  public void drawCursor(boolean flag) {
    
    int meanCharWidth = canvas.getCharWidth(font, '0');    
    
    clipToCurrentBounds();
    canvas.fillRect(flag ? foreground : background, getCurrentX(),
                    getCurrentY() - canvas.getFontAscent(font), meanCharWidth,
                    canvas.getFontHeight(font));
  }
  
  public void backspace(char c) {
    
    int charWidth = canvas.getCharWidth(font, c);
    
    // Clears the text under the cursor
    clipToCurrentBounds();
    canvas.fillRect(background, getCurrentX() - charWidth,
                    getCurrentY() - canvas.getFontAscent(font), charWidth,
                    canvas.getFontHeight(font));
    cursor.setColumn(cursor.getColumn() - 1);
  }

  // **********************************************************************
  // ***** Protected methods
  // **************************************
  
  protected void newline() {
    
    //System.out.println("newline()");
    cursor.setLine(cursor.getLine() + 1);
    cursor.setColumn(1);
  }
  
  protected Canvas getCanvas() {
    
    return canvas;
  }
  
  protected ScreenModel getScreen() {
    
    return screen;
  }
  
  protected LineEditor getEditor() {
    
    return editor;
  }
  
  protected Color getTextBackground() {
    
    return isReverseVideo ? foreground : background;
  }
  
  protected Color getTextColor() {
    
    return isReverseVideo ? background : foreground;
  }
  
  protected void printLine(String line, Color textbackColor,
                           Color textColor) {

    //System.out.printf("printLine(): '%s'\n", line);
    clipToCurrentBounds();
    canvas.fillRect(textbackColor, getCurrentX(),
                    getCurrentY() - canvas.getFontHeight(font)
                    + canvas.getFontDescent(font),
                    canvas.getStringWidth(font, line),
                    canvas.getFontHeight(font));
    canvas.drawString(textColor, font, getCurrentX(), getCurrentY(), line);
    cursor.setColumn(cursor.getColumn() + line.length());
  }
  
  // ************************************************************************
  // ******* Abstract methods
  // *************************************************
  
  /**
   * Sets the buffer mode.
   * 
   * @param flag the buffer mode flag
   */
  public abstract void setBufferMode(boolean flag);
  
  /**
   * Returns the buffer mode.
   * 
   * @return the buffer mode
   */
  public abstract boolean isBuffered();

  /**
   * Sets the paging flag. This feature must be available to be controlled
   * from within the core (file input for replaying recorded sessions).
   * 
   * @param flag true to enable paging, false otherwise
   */
  public abstract void setPagingEnabled(boolean flag);
  
  /**
   * Returns the status of the paging flag.
   * 
   * @return the paging status
   */
  public abstract boolean isPagingEnabled();
  
  /**
   * Resets the cursor to its home position.
   */
  public abstract void resetCursorToHome();  

  /**
   * Resets the internal pager.
   */
  public abstract void resetPager();
    
  protected abstract void sizeUpdated();
  
  protected abstract int getCurrentX();
  
  protected abstract int getCurrentY();
  
  // ************************************************************************
  // ******* Private methods
  // *************************************************
  
  private void printLines(String lines[]) {
    
    Color textColor = getTextColor();
    Color textbackColor = getTextBackground();    
    
    // This is a feature that is not specified, but it is supported by
    // DOS Frotz
    if ((getFont().getStyle() & Font.BOLD) > 0 && !isReverseVideo) {

      textColor = textColor.brighter();
    }
        
    for (int i = 0; i < lines.length; i++) {

      String line = lines[i];
      printLine(line, textbackColor, textColor);
      
      //if (endsWithNewLine(line) || i < lines.length - 1) {
      if (endsWithNewLine(line)) {
        
        newline();
      }
    }
  }  
  
  private static boolean endsWithNewLine(String str) {
  
    return str.charAt(str.length() - 1) == '\n';
  }
  
  protected void clipToCurrentBounds() {
    
    canvas.setClip(0, top, canvas.getWidth(), height);
  }
}
