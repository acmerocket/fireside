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

import javax.swing.SwingUtilities;

import org.zmpp.vm.TextCursor;

/**
 * The class SubWindow manages a sub window within the screen model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class SubWindow {

  public enum HomeYPosition {
    
    TOP, BOTTOM
  }
  
  private BufferedImage image;
  private int top;
  private int height;
  private Color foreground;
  private Color background;

  private TextCursor cursor;
  
  private HomeYPosition yHomePos;
  private Font font;
  private boolean isReverseVideo;
  private boolean isBuffered;
  private boolean isPaged;
  private boolean isScrolled;
  private LineEditor editor;
  private Component parentComponent;
  private String position;
  private int linesPerPage;
  private int linesPrinted;
 
  /**
   * Constructor.
   * 
   * @param parentComponent the parent component
   * @param editor the line editor
   * @param windowNumber the window number
   * @param img the buffer image
   */
  public SubWindow(Component parentComponent, LineEditor editor,
                   BufferedImage img, String position) {
    
    this.image = img;
    this.editor = editor;
    this.parentComponent = parentComponent;
    isBuffered = true;
    this.cursor = new TextCursorImpl();
    yHomePos = HomeYPosition.BOTTOM;
    this.position = position;
  }
  
  /**
   * Sets the home position, which can be either TOP or BOTTOM.
   * 
   * @param pos a home position.
   */
  public void setHomeYPosition(HomeYPosition pos) {
    
    yHomePos = pos;
  }
  
  public HomeYPosition getHomeYPosition() {
    
    return yHomePos;
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
  
  public void setBufferMode(boolean flag) {
   
    this.isBuffered = flag;
  }
  
  public boolean isBuffered() {
    
    return isBuffered;
  }
  
  public void setIsPagingEnabled(boolean flag) {
    
    this.isPaged = flag;
  }
  
  public void setIsScrolled(boolean flag) {
    
    this.isScrolled = flag;
  }
  
  public int getHeight() {
    
    return height;
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
   * Updates the page size.
   */
  private void updatePageSize() {
    
    linesPerPage = (height / getGraphics().getFontMetrics().getHeight()) - 1;
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
    
    WordWrapper wordWrapper = new WordWrapper(lineLength, fm, isBuffered);
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);    
  }
 
  private void doMeMore() {
        
    printLine("<MORE> (Press key to continue)", getGraphics(),
        getGraphics().getFontMetrics(),
        getTextBackground(), getTextColor());
    
    // Rendering in the UI thread
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
        
          parentComponent.repaint();
        }
      });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
    
    // Do this exclusively to have better thread control, we need to stay
    // in the application thread
    editor.setInputMode(true);
    editor.nextZsciiChar();
    cursor.setColumn(1);
    eraseLine();        
    editor.setInputMode(false);
    resetPager();
  }
  
  private void printLines(String lines[]) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();    
    Color textColor = getTextColor();
    Color textbackColor = getTextBackground();
    
    for (int i = 0; i < lines.length; i++) {

      if (isPaged && linesPrinted >= linesPerPage) {
        
        doMeMore();
      }
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
  
  /**
   * Resets the internal pager.
   */
  public void resetPager() {
    
    linesPrinted = 0;
  }
  
  private Color getTextBackground() {
    
    return isReverseVideo ? foreground : background;
  }
  
  private Color getTextColor() {
    
    return isReverseVideo ? background : foreground;
  }
  
  private void printLine(String line, Graphics g, FontMetrics fm,
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
  
  public void scrollUp() {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.copyArea(0, top + fm.getHeight(), image.getWidth(),
               height - fm.getHeight(), 0, -fm.getHeight());
    g.setColor(background);
    g.fillRect(0, top + height - fm.getHeight(),
               image.getWidth(), fm.getHeight());
  }
  
  public void newline() {
    
    cursor.setLine(cursor.getLine() + 1);
    cursor.setColumn(1);
    scrollIfNeeded();
  }
  
  private void scrollIfNeeded() {

    // CODE-DEBT: We could solve this by polymorphism ???    
    if (isScrolled) {
      
      Graphics g = getGraphics();
      FontMetrics fm = g.getFontMetrics();
      // We calulate an available height with a correction amount
      // of fm.getMaxDescent() to reserve enough scrolling space
      while (getCurrentY() > (top + height - fm.getMaxDescent())) {
      
        if (isScrolled) scrollUp();
        cursor.setLine(cursor.getLine() - 1);
      }
    }
  }  
 
  public int getCurrentX() {
    
    // This code has one big problem: It does not work !!!
    // It only works if we do not use the proportional font, but if we
    // do, we need to know, what was actually typed ahead...
    int meanCharWidth = getGraphics().getFontMetrics().charWidth('0');      
    return (cursor.getColumn() - 1) * meanCharWidth;      
  }
  
  public int getCurrentY() {
          
    FontMetrics fm = getGraphics().getFontMetrics();      
    return top + (cursor.getLine() - 1) * fm.getHeight()
           + (fm.getHeight() - fm.getMaxDescent());
  }
 
  public String toString() {
    
    return "sub window [" + position + "]";
  }

  public void drawCursor(boolean flag) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    int meanCharWidth = fm.charWidth('0');    
    
    g.setColor(flag ? foreground : background);
    g.fillRect(getCurrentX(), getCurrentY() - fm.getMaxAscent(),
               meanCharWidth, fm.getHeight());
  }
  
  public int getAvailableLines() {
    
    FontMetrics fm = getGraphics().getFontMetrics();
    return (height - fm.getMaxDescent()) / fm.getHeight();
 }
  
 //
  public void resetCursorToHome() {

    if (yHomePos == HomeYPosition.BOTTOM) {

      // We calulate an available height with a correction amount
      // of fm.getMaxDescent() to reserve enough scrolling space
      cursor.setPosition(getAvailableLines(), 1);

    } else if (yHomePos == HomeYPosition.TOP) {

      cursor.setPosition(1, 1);
    }
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
  
}
