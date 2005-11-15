/*
 * $Id$
 * 
 * Created on 20.10.2005
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

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.zmpp.vm.Machine;
import org.zmpp.vm.OutputStream;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vmutil.ZsciiEncoding;

public class TextViewport extends JViewport implements OutputStream {

  private static final long serialVersionUID = 1L;
  
  private BufferedImage imageBuffer;
  private int y;
  private int x;
  private boolean initialized;
  
  private static final int OFFSET_X = 3;
  private static final int OFFSET_Y = 3;  
  
  private Font[] fonts;
  private StringBuilder streambuffer;
  private StringBuilder textbuffer;
  private Machine machine;
  
  private boolean editMode;
  private int charsTyped; 
  
  public TextViewport(Machine machine) {
    
    fonts = new Font[4];
    fonts[0] = getFont();
    fonts[3] = new Font("Courier New", Font.PLAIN, fonts[0].getSize());
    this.machine = machine;
    streambuffer = new StringBuilder();
    textbuffer = new StringBuilder();
  }
  
  public void setEditMode(final boolean flag) {
    
    try {
      
      // It is very important that the output is flushed before entering
      // input mode. SwingUtilities.invokeAndWait() blocks the whole
      // processing (probably a deadlock between input events and the
      // rendering), so we notify a waiting thread after the rendering
      SwingUtilities.invokeLater(new Runnable() {
        
        public void run() {

          determineFont();
        
          // Flush the output stream
          if (flag) {
          
            printString(streambuffer.toString());
            streambuffer = new StringBuilder();
          }
          
          drawCaret(flag);
          repaint();
         
          synchronized (TextViewport.this) {
            
            // Set status variables
            editMode = flag;
            charsTyped = 0;
            TextViewport.this.notifyAll();
          }
        }
      });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
  }
  
  public synchronized boolean isEditMode() {
    
    return editMode;
  }
  
  public void clear() {
    
    determineFont();    
    Graphics g = getViewGraphics();
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    FontMetrics fm = g.getFontMetrics();
    
    x = getOffsetX();
    setInitialY(fm.getHeight(), getHeight());
  }  
  
  /**
   * Set the current font.
   * 
   * @param fontNumber the font number 
   */
  public void setFont(int fontNumber) {
    
    setFont(fonts[fontNumber - 1]);
  }
  
  public synchronized boolean isInitialized() {
    
    return initialized;
  }
  
  public synchronized void setInitialized() {
    
    this.initialized = true;
    notifyAll();
  }
  
  public synchronized void waitInitialized() {
    
    while (!isInitialized()) {
      
      try { wait(); } catch (Exception ex) { }
    }
  }
  
  public void scrollUp() {
    
    Graphics g = getViewGraphics();
    
    g.setClip(3, 3, getWidth() - 6, getHeight() - 6);
    FontMetrics fm = g.getFontMetrics();
    g.copyArea(0, 0, getWidth(), getHeight(), 0, -fm.getHeight());
  }
  
  public void paint(Graphics g) {

    if (imageBuffer == null) {
      
      imageBuffer = new BufferedImage(getWidth(), getHeight(),
          BufferedImage.TYPE_INT_RGB);
      Graphics g_img = getViewGraphics();
      g_img.setColor(getBackground());
      g_img.fillRect(0, 0, getWidth(), getHeight());
      
      FontMetrics fm = g_img.getFontMetrics();
      setInitialY(fm.getHeight(), getHeight());
      x = getOffsetX();
              
      setInitialized();
    }
    
    g.drawImage(imageBuffer, 0, 0, this);
  }
  
  public void print(final short zsciiChar) {
        
    SwingUtilities.invokeLater(new Runnable() {
      
      public void run() {

        determineFont();
        if (isEditMode()) drawCaret(false);
        
        if (zsciiChar == ZsciiEncoding.NEWLINE) {
          
          printChar('\n');
          
        } else if (zsciiChar == ZsciiEncoding.DELETE && isEditMode()) {
          
          backSpace();
          
        } else {
          
          ZsciiEncoding encoding = ZsciiEncoding.getInstance();
          printChar(encoding.getUnicodeChar(zsciiChar));
          
          // Count chars for backspace
          if (isEditMode()) charsTyped++;
        }
        
        if (isEditMode()) {
          
          drawCaret(true);
          repaint();
        }
      }
    });
  }
  
  private void printChar(char c) {

    textbuffer.append(c);
    
    if (isEditMode()) {
      
      printString(String.valueOf(c));
      
    } else {
      
      streambuffer.append(c);
    }
  }
    
  public void close() { }
  
  // **********************************************************************
  // ******** Private functions
  // *************************************************

  private int getOffsetY() {
    
    return OFFSET_Y;
  }
  
  private int getOffsetX() {
    
    return OFFSET_X;
  }
  
  private Graphics getViewGraphics() {
    
    Graphics g = imageBuffer.getGraphics();
    g.setFont(getFont());
    return g;
  }        
  
  /**
   * Sets the initial y position in the window. According to the specification
   * this is the last line in the current window.
   * 
   * @param fontHeight the current font height
   * @param windowHeight the window height
   */
  private void setInitialY(int fontHeight, int windowHeight) {
   
    // calculate the available lines first
    int availableLines = (windowHeight - 2 * OFFSET_Y) / fontHeight;
    y = getOffsetY() + fontHeight * availableLines;
  }  
  
  private void printString(String str) {
    
    Graphics g = getViewGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.setColor(getForeground());

    int width = getWidth();
    int lineLength = width - getOffsetX() * 2;
    g.setClip(3, 3, getWidth() - 6, getHeight() - 6);
    
    WordWrapper wordWrapper = new WordWrapper(lineLength, fm);
    String[] lines = wordWrapper.wrap(x, str);
    for (int i = 0; i < lines.length; i++) {
     
      while (y + fm.getHeight() > getHeight()) {
        
        scrollUp();
        y -= fm.getHeight();
      }
      g.drawString(lines[i], x, y);
      x += fm.stringWidth(lines[i]);
      
      if (i < lines.length - 1) {
        
        newline();
      }
    }
  }

  private void backSpace() {
    
    if (charsTyped > 0) {
      
      Graphics g = getViewGraphics();
      FontMetrics fm = g.getFontMetrics();
    
      int lastIndex = textbuffer.length() - 1;
      char lastChar = textbuffer.charAt(lastIndex);
      
      int charWidth = fm.charWidth(lastChar);
      g.setColor(getBackground());
      x -= charWidth;
      g.fillRect(x, y - fm.getMaxAscent(), charWidth, fm.getHeight());
      
      textbuffer.deleteCharAt(lastIndex);
      charsTyped--;
    }
  }
  
  private void newline() {
    
    FontMetrics fm = getViewGraphics().getFontMetrics();
    while (y + fm.getHeight() > getHeight()) {
      
      scrollUp();
      y -= fm.getHeight();
    }
    y += fm.getHeight();
    x = getOffsetX();
  }
  
  private void drawCaret(boolean showCaret) {
    
    determineFont();
    
    Graphics g = getViewGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.setColor(showCaret ? getForeground() : getBackground());
    int charWidth = fm.charWidth('B');
    g.fillRect(x, y - fm.getMaxAscent(), charWidth, fm.getHeight());
  }
  
  private void determineFont() {
    
    if (machine.getStoryFileHeader().forceFixedFont()) {
      
      setFont(ScreenModel.FONT_FIXED);
      
    } else {
      
      setFont(ScreenModel.FONT_NORMAL);
    }
  }    
}
