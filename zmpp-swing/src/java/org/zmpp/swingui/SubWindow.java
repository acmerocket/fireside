/*
 * $Id$
 * 
 * Created on 11/19/2005
 * Copyright 2005 by Wei-ju Wu
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


/**
 * The class SubWindow manages a sub window within the screen model.
 * 
 * @author Wei-ju Wu
 *
 */
public class SubWindow {

  private static final int OFFSET_X = 3;
  
  private BufferedImage image;
  private int top;
  private int height;
  private Color foreground;
  private Color background;
  private int currentX;
  private int currentY;
  private Font font;
  private boolean isReverseVideo;
  private boolean isBuffered;
 
  public SubWindow(BufferedImage img) {
    
    this.image = img;
    isBuffered = true;
  }

  public void setReverseVideo(boolean flag) {
    
    this.isReverseVideo = flag;
  }
  
  public void setBufferMode(boolean flag) {
    
    this.isBuffered = flag;
  }  
  
  public int getHeight() {
    
    return height;
  }
  
  public void setVerticalBounds(int top, int height) {
    
    this.top = top;
    this.height = height;
  }
  
  public void resize(int numLines) {
    
    this.height = getGraphics().getFontMetrics().getHeight() * numLines;
  }
  
  public void setFont(Font font) {
    
    this.font = font;
  }
  
  public Graphics getGraphics() {
    
    Graphics g = image.getGraphics();
    g.setFont(font);
    return g;
  }
  
  public void clear() {
    
    Graphics g_img = getGraphics();
    g_img.setColor(background);
    g_img.fillRect(0, top, image.getWidth(), height);
    
    FontMetrics fm = g_img.getFontMetrics();
    setInitialY(fm.getHeight());
    currentX = OFFSET_X;
    
  }
  
  public void setBackground(Color color) {
    
    background = color;
  }
  
  public void setForeground(Color color) {
    
    foreground = color;
  }
  
  public void drawCaret(boolean showCaret) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.setColor(showCaret ? foreground : background);
    int charWidth = fm.charWidth('B');
    g.fillRect(currentX, currentY - fm.getMaxAscent(), charWidth,
               fm.getHeight());
  }
  
  public void backSpace(char lastChar) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    
    int charWidth = fm.charWidth(lastChar);
    g.setColor(background);
    currentX -= charWidth;
    g.fillRect(currentX, currentY - fm.getMaxAscent(), charWidth,
               fm.getHeight());
  }
  
  /**
   * This is the function that does the actual printing to the screen.
   * 
   * @param str a string to pring
   */
  public void printString(String str) {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    
    // TODO: Handle reverse video !!
    g.setColor(foreground);

    int width = image.getWidth();
    int lineLength = width - OFFSET_X * 2;
    g.setClip(0, top, image.getWidth(), height);
    
    // TODO: Handle the isBuffered flag !!!!
    WordWrapper wordWrapper = new WordWrapper(lineLength, fm);
    String[] lines = wordWrapper.wrap(currentX, str);
    for (int i = 0; i < lines.length; i++) {
     
      while (currentY + fm.getHeight() > (top + height)) {
        
        scrollUp();
        currentY -= fm.getHeight();
      }
      g.drawString(lines[i], currentX, currentY);
      currentX += fm.stringWidth(lines[i]);
      
      if (i < lines.length - 1) {
        
        newline();
      }
    }
  }
  
  /**
   * Sets the initial y position in the window. According to the specification
   * this is the last line in the current window.
   * 
   * @param fontHeight the current font height
   */
  private void setInitialY(int fontHeight) {
   
    // calculate the available lines first
    int availableLines = height / fontHeight;
    currentY = top + fontHeight * availableLines;
    //System.out.println("height: " + height + " lines: " + availableLines
    //    + " currentY = " + currentY + " top: " + top);
  }
  
  public void scrollUp() {
    
    Graphics g = getGraphics();
    
    g.setClip(0, top, image.getWidth(), height);
    FontMetrics fm = g.getFontMetrics();
    g.copyArea(0, top + fm.getHeight(), image.getWidth(),
               height - fm.getHeight(), 0, -fm.getHeight());
  }
  
  public void newline() {
    
    FontMetrics fm = getGraphics().getFontMetrics();
    while (currentY + fm.getHeight() > (top + height)) {
      
      scrollUp();
      currentY -= fm.getHeight();
    }
    currentY += fm.getHeight();
    currentX = OFFSET_X;
  }

}
