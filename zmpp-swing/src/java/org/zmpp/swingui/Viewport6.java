/*
 * $Id$
 * 
 * Created on 2006/02/23
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel6;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.TextCursor;
import org.zmpp.vm.Window6;
import org.zmpp.vm.StoryFileHeader.Attribute;

public class Viewport6 extends JViewport implements ScreenModel6, OutputStream {

  private static final long serialVersionUID  = 1L;
  private static final int NUM_V6_WINDOWS = 8;
  
  private BufferedImage imageBuffer;
  private boolean initialized;
  private boolean isSelected;
  private Color defaultForeground;
  private Color defaultBackground;
  private Font standardFont, fixedFont;
    
  private Window6[] windows;
  private int currentwindow;
  private Machine machine;
  private LineEditor editor;
  
  public Viewport6(Machine machine, LineEditor editor) {

    this.machine = machine;
    this.editor = editor;
    windows = new Window6[NUM_V6_WINDOWS];
    standardFont = getFont();
    fixedFont = new Font("Monospaced", Font.ROMAN_BASELINE,
        standardFont.getSize());    
    
  }
  
  // ********************************************************************
  // ***** ScreenModel interface
  // *******************************************
  
  public void eraseWindow(final int window) {
    
  }
  
  public void eraseLine(int value) {
    
  }
  
  public TextCursor getTextCursor() {
    
    return windows[currentwindow].getCursor();
  }

  public void setTextCursor(int line, int column) {

    windows[currentwindow].getCursor().setPosition(line, column);
  }
  
  public void splitWindow(final int linesUpperWindow) {
    
  }
  
  public void setWindow(final int window) {
    
    currentwindow = window;
  }
  
  public void setTextStyle(int style) {
    
  }
  
  public void setBufferMode(boolean flag) {
    
  }
  
  public void setPaging(boolean flag) {
    
  }
  
  /**
   * Reset the line counters.
   */
  public void resetPagers() {
    
  }

  public synchronized void waitInitialized() {
    
    while (!isInitialized()) {
      
      try { wait(); } catch (Exception ex) { }
    }
  }
  
  public synchronized boolean isInitialized() {
    
    return initialized;
  }
  
  public synchronized void setInitialized() {
    
    this.initialized = true;
    notifyAll();
  }
  
  public OutputStream getOutputStream() {
    
    return this;
  }
  
  private static Color GREEN = new Color(0, 190, 0);
  private static Color RED = new Color(190, 0, 0);
  private static Color YELLOW = new Color(190, 190, 0);
  private static Color BLUE = new Color(0, 0, 190);
  private static Color MAGENTA = new Color(190, 0, 190);
  private static Color CYAN = new Color(0, 190, 190);
  
  private Color translateColornum(int colornum, boolean foreground) {
    
    switch (colornum) {
    
    case COLOR_DEFAULT:
      return (foreground) ? defaultForeground : defaultBackground;
    case COLOR_BLACK:
      return Color.BLACK;
    case COLOR_RED:
      return RED;
    case COLOR_GREEN:
      return GREEN;
    case COLOR_YELLOW:
      return YELLOW;
    case COLOR_BLUE:
      return BLUE;
    case COLOR_MAGENTA:
      return MAGENTA;
    case COLOR_CYAN:
      return CYAN;
    case COLOR_WHITE:
      return Color.WHITE;
    case COLOR_MS_DOS_DARKISH_GREY:
      return Color.DARK_GRAY;
    }
    return Color.BLACK;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setForegroundColor(int colornum) {

    if (colornum > 0) {
      
      flush();
      Color foreground = translateColornum(colornum, true);
      for (int i = 0; i < windows.length; i++) {
        
        windows[i].setForeground(foreground);
      }
    }
  }
  
  public void setBackgroundColor(int colornum) {
    
    if (colornum > 0) {
      
      flush();
      Color background = translateColornum(colornum, true);
      for (int i = 0; i < windows.length; i++) {
        
        windows[i].setBackground(background);
      }
    }
  }

  public void redraw() {
    
    repaintInUiThread();
  }
  
  public int setFont(int fontnum) {
   
    return 0;
  }

  public synchronized void displayCursor(boolean showCaret) {
    
  }
  
  public void reset() {
    
    setScreenProperties();
  }
  
  // **************************************************************************
  // ****** OutputStream interface
  // **************************************

  public void close() { }  
  
  public boolean isSelected() {
    
    return isSelected;
  }
  
  public void select(boolean flag) {
  
    isSelected = flag;
  }
  
  public void flush() {
    
  }
  
  public void print(final short zsciiChar, boolean isInput) {
    
    if (zsciiChar == ZsciiEncoding.NEWLINE) {
      
      printChar('\n', isInput);
    
    } else {
    
      printChar(machine.getGameData().getZsciiEncoding().getUnicodeChar(
          zsciiChar), isInput);
    }
  }

  public void deletePrevious(short zchar) {
    
  }
  
  // ********************************************************************
  // ***** ScreenModel V6 interface
  // *******************************************
  
  public void setMouseWindow(int window) {

    // do nothing at the moment
  }
  
  public Window6 getCurrentWindow() {

    return windows[currentwindow];
  }
  
  public Window6 getWindow(int window) {

    return windows[window];
  }
  
  public void paint(Graphics g) {

    if (imageBuffer == null) {
      
      imageBuffer = new BufferedImage(getWidth(), getHeight(),
          BufferedImage.TYPE_INT_RGB);

      // Default colors
      defaultBackground = Color.WHITE;
      defaultForeground = Color.BLACK;
            
      Canvas canvas = new CanvasImpl(imageBuffer, this);
      for (int i = 0; i < NUM_V6_WINDOWS; i++) {
      
        windows[i] = new Window6Impl(canvas);
        windows[i].setBackground(defaultBackground);
        windows[i].setForeground(defaultForeground);
        windows[i].setFont(fixedFont);
      }
      
      Graphics g_img = imageBuffer.getGraphics();
      g_img.setColor(defaultBackground);
      g_img.fillRect(0, 0, getWidth(), getHeight());
      
      setScreenProperties();      
      setInitialized();
    }

    g.drawImage(imageBuffer, 0, 0, this);    
  }

  private void setScreenProperties() {
    
    StoryFileHeader fileheader = machine.getGameData().getStoryFileHeader();
    fileheader.setEnabled(Attribute.SUPPORTS_BOLD, true);
    fileheader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, true);
    fileheader.setEnabled(Attribute.SUPPORTS_ITALIC, true);
    
    fileheader.setEnabled(Attribute.SUPPORTS_COLOURS, true);
    fileheader.setDefaultBackgroundColor(COLOR_WHITE);
    fileheader.setDefaultForegroundColor(COLOR_BLACK);
    fileheader.setFontWidth(1);
    fileheader.setFontHeight(1);    
    determineStandardFont();
    updateDimensionsInHeader();
  }
  
  private void determineStandardFont() {
          
    standardFont = fixedFont;      
  }
  
  private void updateDimensionsInHeader() {
    
    StoryFileHeader fileheader = machine.getGameData().getStoryFileHeader();
    Font font = fixedFont;
    FontMetrics fm = imageBuffer.getGraphics().getFontMetrics(font);
    int screenWidth = imageBuffer.getWidth() / fm.charWidth('0');
    int screenHeight = imageBuffer.getHeight() / fm.getHeight();    
    fileheader.setScreenWidth(screenWidth);
    fileheader.setScreenHeight(screenHeight);
  }
  
  
  private void printChar(char c, boolean isInput) {

    windows[currentwindow].printString(String.valueOf(c));
  }
  
  private void repaintInUiThread() {
    
    try {
      
      SwingUtilities.invokeAndWait(new Runnable() {
        
        public void run() {
          
          // replace the expensive repaint() call with a fast copying of
          // the double buffer
          if (imageBuffer != null) {
            
            getGraphics().drawImage(imageBuffer, 0, 0, Viewport6.this);
          }
        }
      });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
  }  
}
