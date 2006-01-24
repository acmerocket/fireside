/*
 * $Id$
 * 
 * Created on 11/19/2005
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
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.zmpp.vm.TextCursor;

/**
 * The class SubWindow manages a sub window within the screen model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public abstract class SubWindow {

  private BufferedImage image;
  private int top;
  private int height;
  private Color foreground;
  private Color background;

  private TextCursor cursor;
  
  private Font font;
  private boolean isReverseVideo;
  private LineEditor editor;
  private Component parentComponent;
  private String name;
  private int linesPerPage;
  private int linesPrinted;
 
  /**
   * Constructor.
   * 
   * @param parentComponent the parent component
   * @param editor the line editor
   * @param windowNumber the window number
   * @param img the buffer image
   * @param name the window name
   */
  public SubWindow(Component parentComponent, LineEditor editor,
                   BufferedImage img, String name) {
    
    this.image = img;
    this.editor = editor;
    this.parentComponent = parentComponent;
    this.cursor = new TextCursorImpl();
    this.name = name;
  }
  
  /**
   * Sets the reverse video text mode.
   * 
   * @param flag true or false to activate or deactivate reverse video
   */
  public void setReverseVideo(boolean flag) {
    
    this.isReverseVideo = flag;
  }

  /**
   * Access to this window's cursor.
   * 
   * @return the cursor
   */
  public TextCursor getCursor() {
   
    return cursor;
  }
  
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

  public abstract void setIsPagingEnabled(boolean flag);
  
  public int getHeight() {
    
    return height;
  }
  
  public int getTop() {
    
    return top;
  }
  
  public void setVerticalBounds(int top, int height) {
    
    this.top = top;
    this.height = height;
    updatePageSize();
    resetCursorToHome();
  }
  
  public void resize(int numLines) {
    
    height = getGraphics().getFontMetrics().getHeight() * numLines;
    updatePageSize();
    resetCursorToHome();
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
  
  /**
   * Accesses this window's graphics object.
   * 
   * @return the graphics object
   */
  public Graphics getGraphics() {
    
    Graphics g = image.getGraphics();
    g.setFont(font);
    return g;
  }
  
  public void clear() {
    
    Graphics g_img = getGraphics();
    g_img.setColor(background);
    g_img.fillRect(0, top, image.getWidth(), height);
    resetCursorToHome();
  }
  
  public void eraseLine() {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.setColor(background);
    g.fillRect(getCurrentX(),
               getCurrentY() - fm.getMaxAscent(),
               image.getWidth() - getCurrentX(), fm.getHeight());
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

    //if (position.equals("BOTTOM")) {
      
    //System.out.printf("[%s] printString(), str: [%s], x: %d y: %d\n", position,
    //    str, cursor.getCurrentX(), cursor.getCurrentY());
    //}
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    
    int width = image.getWidth();
    int lineLength = width;
    
    WordWrapper wordWrapper = new WordWrapper(lineLength, fm, isBuffered());
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);    
  }
 
  /**
   * Resets the internal pager.
   */
  public void resetPager() {
    
    linesPrinted = 0;
  }
  
  public void newline() {
    
    cursor.setLine(cursor.getLine() + 1);
    cursor.setColumn(1);
    scrollIfNeeded();
  }
  
  public String toString() {
    
    return "sub window [" + name + "]";
  }

  public void drawCursor(boolean flag) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    int meanCharWidth = fm.charWidth('0');    
    
    g.setColor(flag ? foreground : background);
    g.fillRect(getCurrentX(), getCurrentY() - fm.getMaxAscent(),
               meanCharWidth, fm.getHeight());
  }
  
  public void backspace(char c) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    int charWidth = fm.charWidth(c);
    cursor.setColumn(cursor.getColumn() - 1);
    
    // Clears the text under the cursor
    g.setColor(background);      
    g.fillRect(getCurrentX() - charWidth, getCurrentY() - fm.getMaxAscent(),
               charWidth, fm.getHeight());
  }

  public abstract void resetCursorToHome();
  
  protected abstract void scrollIfNeeded();
  
  protected BufferedImage getImage() {
    
    return image;
  }
  
  protected Component getParentComponent() {
    
    return parentComponent;
  }
  
  protected LineEditor getEditor() {
    
    return editor;
  }
  
  protected int getLinesPrinted() {
    
    return linesPrinted;
  }
  
  protected int getLinesPerPage() {
    
    return linesPerPage;
  }
  // ************************************************************************
  // ******* Private methods
  // *************************************************
  

  protected int getCurrentY() {
    
    FontMetrics fm = getGraphics().getFontMetrics();      
    return top + (cursor.getLine() - 1) * fm.getHeight()
           + (fm.getHeight() - fm.getMaxDescent());
  }
 
  protected int getCurrentX() {
    
    // This code has one big problem: It does not work !!!
    // It only works if we do not use the proportional font, but if we
    // do, we need to know, what was actually typed ahead...
    int meanCharWidth = getGraphics().getFontMetrics().charWidth('0');      
    return (cursor.getColumn() - 1) * meanCharWidth;      
  }
  
  
  /**
   * Updates the page size.
   */
  private void updatePageSize() {
    
    linesPerPage = (height / getGraphics().getFontMetrics().getHeight()) - 1;
  }
  
  protected abstract void handlePaging();
  
  private void printLines(String lines[]) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();    
    Color textColor = getTextColor();
    Color textbackColor = getTextBackground();
    
    for (int i = 0; i < lines.length; i++) {

      handlePaging();
      String line = lines[i];
      //if (position.equals("BOTTOM"))
      //System.out.printf("line = '%s', lines printed: %d\n", line, linesPrinted);
      printLine(line, g, fm, textbackColor, textColor);
      
      if (isEmptyLine(line) || endsWithNewLine(line)
          || i < lines.length - 1) {
        
        newline();
        linesPrinted++;
      }
    }
  }
  
  protected Color getTextBackground() {
    
    return isReverseVideo ? foreground : background;
  }
  
  protected Color getTextColor() {
    
    return isReverseVideo ? background : foreground;
  }
  
  protected void printLine(String line, Graphics g, FontMetrics fm,
      Color textbackColor, Color textColor) {
    
    scrollIfNeeded();
    g.setColor(textbackColor);
    g.fillRect(getCurrentX(),
               getCurrentY() - fm.getHeight() + fm.getMaxDescent(),
               fm.stringWidth(line), fm.getHeight());
    g.setColor(textColor);
    g.drawString(line, getCurrentX(), getCurrentY());
    cursor.setColumn(cursor.getColumn() + line.length());
  }
  
  private static boolean isEmptyLine(String str) {
    
    return str.length() == 0;
  }
  
  private static boolean endsWithNewLine(String str) {
  
    return str.charAt(str.length() - 1) == '\n';
  }
}
