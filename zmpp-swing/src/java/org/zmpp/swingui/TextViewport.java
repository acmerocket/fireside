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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.zmpp.swingui.SubWindow.HomeYPosition;
import org.zmpp.vm.Machine;
import org.zmpp.vm.OutputStream;
import org.zmpp.vmutil.ZsciiEncoding;

public class TextViewport extends JViewport implements OutputStream {

  private static final long serialVersionUID = 1L;
  
  private BufferedImage imageBuffer;
  private boolean initialized;
  
  
  private static final int TEXTSTYLE_REVERSE_VIDEO  = 1;
  private static final int TEXTSTYLE_BOLD           = 2;
  private static final int TEXTSTYLE_ITALIC         = 4;
  private static final int TEXTSTYLE_FIXED          = 8;
  
  private Font standardFont, fixedFont, currentFont;
  
  private StringBuilder streambuffer;
  private StringBuilder textbuffer;
  private Machine machine;
  
  private boolean editMode;
  private int charsTyped;
  private SubWindow[] windows;
  private int activeWindow;
  private static final boolean DEBUG = true;
  
  public TextViewport(Machine machine) {
    
    this.machine = machine;
    
    standardFont = getFont();
    fixedFont = new Font("Courier New", Font.PLAIN, standardFont.getSize());
    currentFont = standardFont;
    
    streambuffer = new StringBuilder();
    textbuffer = new StringBuilder();
    windows = new SubWindow[2];
    activeWindow = 1;
  }
  
  public void setFont(Font font) {
    
    super.setFont(font);
    if (initialized) windows[activeWindow].setFont(font);
  }
  
  public void splitWindow(int linesUpperWindow) {
    
    windows[0].resize(linesUpperWindow);
    int heightWindow0 = windows[0].getHeight();
    windows[1].setVerticalBounds(heightWindow0 - 1,
                                 getHeight() - heightWindow0);
    windows[0].clear();
    windows[1].clear();
  }
  
  public void setWindow(int window) {
    
    activeWindow = window;
  }

  /**
   * This function implements text styles in our screen model.
   * 
   * @param style the style mask as defined in the standards document
   */
  public void setTextStyle(int style) {

    // Flush the output before setting a new style
    try {
      SwingUtilities.invokeLater(new Runnable() {

        public void run() {
          
          determineFont();
          flushOutput();
        }
      });
      
    } catch (Exception ex) { ex.printStackTrace(); }
    
    int fontStyle = Font.PLAIN;    
    if ((style & TEXTSTYLE_FIXED) > 0) {
      
      currentFont = fixedFont;
      
    } else {
      
      currentFont = standardFont;
    }
    
    windows[activeWindow].setReverseVideo(
        (style & TEXTSTYLE_REVERSE_VIDEO ) > 0);
    
    fontStyle |= ((style & TEXTSTYLE_BOLD) > 0) ? Font.BOLD : 0;
    fontStyle |= ((style & TEXTSTYLE_ITALIC) > 0) ? Font.ITALIC : 0;
    currentFont = currentFont.deriveFont(fontStyle);
    setFont(currentFont);
  }
  
  public void setBufferMode(boolean flag) {

    windows[activeWindow].setBufferMode(flag);
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
          if (flag) flushOutput();
          
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
  
  private void flushOutput() {
    
    windows[activeWindow].printString(streambuffer.toString());
    streambuffer = new StringBuilder();
  }
  
  public synchronized boolean isEditMode() {
    
    return editMode;
  }
  
  public void clear() {
    
    determineFont();
    windows[activeWindow].clear();
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
  
  public void paint(Graphics g) {

    if (imageBuffer == null) {
      
      imageBuffer = new BufferedImage(getWidth(), getHeight(),
          BufferedImage.TYPE_INT_RGB);
      
      // Create the two sub windows
      windows[0] = new SubWindow(imageBuffer);
      windows[1] = new SubWindow(imageBuffer);
      
      windows[0].setVerticalBounds(0, 180);
      windows[0].setBackground(getBackground());
      windows[0].setForeground(getForeground());
      windows[0].setFont(fixedFont);
      windows[0].setHomeYPosition(HomeYPosition.TOP);
      
      windows[1].setVerticalBounds(180, getHeight() - 180);
      windows[1].setBackground(getBackground());
      windows[1].setForeground(getForeground());
      windows[1].setFont(standardFont);
      
      windows[0].clear();
      windows[1].clear();

      setInitialized();

      // For debugging only
      windows[0].setReverseVideo(true);
      windows[1].setReverseVideo(true);
      this.setWindow(0);
      this.setTextStyle(TEXTSTYLE_REVERSE_VIDEO | TEXTSTYLE_FIXED | TEXTSTYLE_BOLD | TEXTSTYLE_ITALIC);
      windows[0].printString("Xiaoru darling");
      this.setWindow(1);
      this.setTextStyle(TEXTSTYLE_ITALIC);
      windows[1].printString("Xiaoru darling");
      windows[1].newline();
      //windows[1].printString("Hallo Xiaoru\nXiaoru");
      windows[1].newline();
    }
    g.drawImage(imageBuffer, 0, 0, this);
    
    if (DEBUG) {
      g.setColor(Color.BLACK);
      g.drawLine(0, windows[0].getHeight() - 1, getWidth(),
                 windows[0].getHeight() - 1);
      g.drawLine(0, 180 + windows[1].getHeight() - 1, getWidth(),
          180 + windows[1].getHeight() - 1);
    }
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
      
      windows[activeWindow].printString(String.valueOf(c));
      
    } else {
      
      streambuffer.append(c);
    }
  }
    
  public void close() { }
  
  // **********************************************************************
  // ******** Private functions
  // *************************************************

  private void backSpace() {
    
    if (charsTyped > 0) {
      
      int lastIndex = textbuffer.length() - 1;
      char lastChar = textbuffer.charAt(lastIndex);
      windows[activeWindow].backSpace(lastChar);
      textbuffer.deleteCharAt(lastIndex);
      charsTyped--;
    }
  }
    
  private void drawCaret(boolean showCaret) {
    
    determineFont();
    windows[activeWindow].drawCaret(showCaret);
  }
  
  private void determineFont() {
    
    if (machine.getStoryFileHeader().forceFixedFont()) {
      
      currentFont = fixedFont;
      
    } else {
      
      currentFont = standardFont;
    }
    setFont(currentFont);
  }  
}
