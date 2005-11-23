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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.zmpp.swingui.SubWindow.HomeYPosition;
import org.zmpp.vm.Machine;
import org.zmpp.vm.OutputStream;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.TextCursor;
import org.zmpp.vmutil.ZsciiEncoding;

public class TextViewport extends JViewport implements OutputStream {

  private static final long serialVersionUID = 1L;
  
  private BufferedImage imageBuffer;
  private boolean initialized;
    
  private static final int TEXTSTYLE_REVERSE_VIDEO  = 1;
  private static final int TEXTSTYLE_BOLD           = 2;
  private static final int TEXTSTYLE_ITALIC         = 4;
  private static final int TEXTSTYLE_FIXED          = 8;
  
  private static final int WINDOW_BOTTOM  = 0;
  private static final int WINDOW_TOP     = 1;
  
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
    activeWindow = WINDOW_BOTTOM;
  }
  
  public void reset() {
    
    System.out.println("reset()");
    setScreenProperties();
    SwingUtilities.invokeLater(new Runnable() {
      
      public void run() {
        windows[WINDOW_TOP].clear();
        resizeWindows(0);
        windows[WINDOW_BOTTOM].clear();
        repaint();
      }
    });
  }
  
  public void setFont(Font font) {
    
    super.setFont(font);
    if (initialized) windows[activeWindow].setFont(font);
  }
  
  public void eraseWindow(int window) {
    
    if (window == -1) {
      
      windows[WINDOW_TOP].setBackground(windows[WINDOW_BOTTOM].getBackground());
      windows[WINDOW_TOP].clear();
      windows[WINDOW_BOTTOM].clear();
      resizeWindows(0);
      
    } else if (window == -2) {
      
      windows[WINDOW_TOP].clear();
      windows[WINDOW_BOTTOM].clear();
      
    } else {
      
      // Note: The specification leaves unclear if the cursor position
      // should be reset in this case
      windows[window].clear();
    }
  }
  
  public void eraseLine(int value) {

    if (value == 1) {
      
      windows[activeWindow].eraseLine();
    }
  }
  
  public TextCursor getTextCursor() {
  
    return windows[activeWindow].getCursor();
  }
  
  public void setTextCursor(int line, int column) {
   
    windows[activeWindow].getCursor().setPosition(line, column);
  }
  
  public void splitWindow(final int linesUpperWindow) {
   
    System.out.println("splitWindow(): " + linesUpperWindow);
    try {
    SwingUtilities.invokeAndWait(new Runnable() {
      
      public void run() {
        // Only works if lower window is selected (S 8.7.2.1)
        if (activeWindow == WINDOW_BOTTOM) {

          resizeWindows(linesUpperWindow);
      
          // S 8.6.1.1.2: Top window is cleared in version 3
          if (machine.getStoryFileHeader().getVersion() == 3) {
        
            windows[WINDOW_TOP].clear();
          }
        }
        repaint();
      }
    });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
  }
  
  public void setWindow(final int window) {
    
    System.out.println("setWindow(): " + window);
    try {
    SwingUtilities.invokeAndWait(new Runnable() {
      
      public void run() {
        activeWindow = window;
    
        // S 8.7.2: If the top window is set active, reset the cursor position
        if (activeWindow == WINDOW_TOP) {

          windows[activeWindow].getCursor().reset();
        }
        repaint();
      }
    });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
  }

  /**
   * This function implements text styles in our screen model.
   * 
   * @param style the style mask as defined in the standards document
   */
  public void setTextStyle(int style) {

    System.out.println("setTextStyle()");
    // Flush the output before setting a new style
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run() {
          
          determineFont();
          flushOutput();
        }
      });
      
    } catch (Exception ex) { ex.printStackTrace(); }
    
    int fontStyle = Font.PLAIN;
    
    // Ensure that the top window is always set in a fixed font
    if ((style & TEXTSTYLE_FIXED) > 0 || activeWindow == WINDOW_TOP) {
      
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

    // only affects bottom window
    windows[WINDOW_BOTTOM].setBufferMode(flag);
  }
  
  public void setEditMode(final boolean flag) {
    
    //System.out.println("setEditMode()");
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
    
    System.out.println("flushOutput(), activeWindow: " + activeWindow
        + " str: [" + streambuffer.toString() + "]");
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
      windows[WINDOW_TOP] = new SubWindow(imageBuffer);
      windows[WINDOW_TOP].setBackground(getBackground());
      windows[WINDOW_TOP].setForeground(getForeground());
      // S. 8.7.2.4: use fixed font for upper window
      windows[WINDOW_TOP].setFont(fixedFont);
      windows[WINDOW_TOP].setHomeYPosition(HomeYPosition.TOP);
      // S. 8.7.2.5: top window is unbuffered
      windows[WINDOW_TOP].setBufferMode(false);
      
      windows[WINDOW_BOTTOM] = new SubWindow(imageBuffer);           
      windows[WINDOW_BOTTOM].setBackground(getBackground());
      windows[WINDOW_BOTTOM].setForeground(getForeground());
      windows[WINDOW_BOTTOM].setFont(standardFont);
      windows[WINDOW_BOTTOM].setHomeYPosition(HomeYPosition.BOTTOM);
      windows[WINDOW_BOTTOM].setBufferMode(true);

      activeWindow = WINDOW_BOTTOM;

      Graphics g_img = imageBuffer.getGraphics();
      g_img.setColor(getBackground());
      g_img.fillRect(0, 0, getWidth(), getHeight());
      resizeWindows(0);
      windows[WINDOW_TOP].getCursor().reset();
      windows[WINDOW_BOTTOM].getCursor().reset();

      setScreenProperties();      
      setInitialized();

      /*
      // For debugging only
      windows[WINDOW_TOP].setReverseVideo(true);
      windows[WINDOW_BOTTOM].setReverseVideo(true);
      
      setWindow(WINDOW_TOP);
      setTextStyle(TEXTSTYLE_REVERSE_VIDEO | TEXTSTYLE_FIXED | TEXTSTYLE_BOLD | TEXTSTYLE_ITALIC);
      windows[WINDOW_TOP].printString("Xiaoru darling");
      windows[WINDOW_TOP].getCursor().setPosition(2, 10);
      windows[WINDOW_TOP].printString("Xiaoru darling");
      windows[WINDOW_TOP].getCursor().setPosition(2, 15);
      windows[WINDOW_TOP].eraseLine();
      
      setWindow(WINDOW_BOTTOM);
      setTextStyle(TEXTSTYLE_BOLD);
      windows[WINDOW_BOTTOM].printString("Xiaoru darling");
      //windows[WINDOW_BOTTOM].newline();
      //windows[WINDOW_BOTTOM].printString("Hallo Xiaoru\nXiaoru");
      //windows[WINDOW_BOTTOM].newline();
       *
       */
    }

    g.drawImage(imageBuffer, 0, 0, this);
    
    if (DEBUG) {
      
      // Draw separator lines
      g.setColor(Color.BLACK);
      g.drawLine(0, windows[WINDOW_TOP].getHeight() - 1, getWidth(),
                 windows[WINDOW_TOP].getHeight() - 1);
      g.drawLine(0, 180 + windows[WINDOW_BOTTOM].getHeight() - 1, getWidth(),
          180 + windows[WINDOW_BOTTOM].getHeight() - 1);
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
  
  public void close() { }
  
  // **********************************************************************
  // ******** Private functions
  // *************************************************

  private void printChar(char c) {

    textbuffer.append(c);
    
    if (isEditMode()) {
      
      windows[activeWindow].printString(String.valueOf(c));
      
    } else {
      
      //System.out.println("printChar: '" + c + "'");
      streambuffer.append(c);
    }
  }    
  
  private void backSpace() {
    
    if (charsTyped > 0) {
      
      int lastIndex = textbuffer.length() - 1;
      char lastChar = textbuffer.charAt(lastIndex);
      windows[activeWindow].getCursor().backspace(lastChar);
      textbuffer.deleteCharAt(lastIndex);
      charsTyped--;
    }
  }
    
  private void drawCaret(boolean showCaret) {
    
    determineFont();
    windows[activeWindow].getCursor().draw(showCaret);
  }
  
  private void determineFont() {
    
    if (machine.getStoryFileHeader().forceFixedFont()) {
      
      currentFont = fixedFont;
      
    } else {
      
      currentFont = standardFont;
    }
    setFont(currentFont);
  }  

  private void resizeWindows(int linesUpperWindow) {
    
    windows[WINDOW_TOP].resize(linesUpperWindow);
    int heightWindowTop = windows[WINDOW_TOP].getHeight();
    windows[WINDOW_BOTTOM].setVerticalBounds(heightWindowTop - 1,
                                             getHeight() - heightWindowTop);      
  }  

  private void setScreenProperties() {
    
    StoryFileHeader fileheader = machine.getStoryFileHeader();
    if (fileheader.getVersion() <= 3) {
      
      fileheader.setDefaultFontIsVariablePitch(true);    
      fileheader.setStatusLineAvailable(true);
      fileheader.setScreenSplittingAvailable(true);
      
    } else if (fileheader.getVersion() >= 4) {
      
      fileheader.setBoldFaceAvailable(true);
      fileheader.setFixedFontAvailable(true);
      fileheader.setItalicAvailable(true);
      
      FontMetrics fm = imageBuffer.getGraphics().getFontMetrics(fixedFont);
      int screenWidth = (imageBuffer.getWidth() - 2 * SubWindow.OFFSET_X) /
                        fm.charWidth('G');
      int screenHeight = imageBuffer.getHeight() / fm.getHeight();
      fileheader.setScreenWidth(screenWidth);
      fileheader.setScreenHeight(screenHeight);      
      
      // Is not really a screen property, put it into machine later
      fileheader.setTimedInputAvailable(false);
    }
  }
  
}
