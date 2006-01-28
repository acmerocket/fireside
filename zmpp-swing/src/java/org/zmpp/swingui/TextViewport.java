/*
 * $Id$
 * 
 * Created on 20.10.2005
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
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.TextCursor;
import org.zmpp.vm.StoryFileHeader.Attribute;

public class TextViewport extends JViewport implements OutputStream,
ScreenModel {

  private static final long serialVersionUID = 1L;
  
  private BufferedImage imageBuffer;
  private boolean initialized;
    
  private static final int TEXTSTYLE_ROMAN          = 0;
  private static final int TEXTSTYLE_REVERSE_VIDEO  = 1;
  private static final int TEXTSTYLE_BOLD           = 2;
  private static final int TEXTSTYLE_ITALIC         = 4;
  private static final int TEXTSTYLE_FIXED          = 8;
  
  private static final int WINDOW_BOTTOM  = 0;
  private static final int WINDOW_TOP     = 1;
  
  private Color defaultForeground;
  private Color defaultBackground;
  private Font standardFont, fixedFont;
  
  /**
   * This buffer holds the current text within the output stream.
   */
  private StringBuilder streambuffer;
  
  private Machine machine;
  private LineEditor editor;
  
  private boolean isSelected;
  private SubWindow[] windows;
  private int[] fontnumbers;
  private int activeWindow;
  private static final boolean DEBUG = false;
  
  public TextViewport(Machine machine, LineEditor editor) {
    
    this.machine = machine;
    this.editor = editor;
    
    standardFont = getFont();
    fixedFont = new Font("Monospaced", Font.ROMAN_BASELINE,
                         standardFont.getSize());    
    streambuffer = new StringBuilder();
    windows = new SubWindow[2];
    fontnumbers = new int[2];
    activeWindow = WINDOW_BOTTOM;
  }
  
  public void reset() {
    
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
    
  public void eraseWindow(final int window) {
    
    if (window == -1) {
      
      resizeWindows(0);
      windows[WINDOW_BOTTOM].clear();
      
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
  
  public void setTextCursor(final int line, final int column) {
   
    windows[activeWindow].getCursor().setPosition(line, column);
  }
  
  public void splitWindow(final int linesUpperWindow) {
   
    // The standard document suggests that a split should only take part 
    // if the lower window is selected (S 8.7.2.1), but Bureaucracy does
    // the split with the upper window selected, so we do that resizing
    // always
    resizeWindows(linesUpperWindow);
      
    // S 8.6.1.1.2: Top window is cleared in version 3
    if (machine.getServices().getStoryFileHeader().getVersion() == 3) {
        
      windows[WINDOW_TOP].clear();
    }
  }
  
  public void setWindow(final int window) {
    
    //System.out.printf("@set_window %d\n", window);
    // Flush out the current active window
    flush();
    
    activeWindow = window;
    
    // S 8.7.2: If the top window is set active, reset the cursor position
    if (activeWindow == WINDOW_TOP) {
      
      windows[activeWindow].resetCursorToHome();
    }
  }

  /**
   * This function implements text styles in our screen model.
   * 
   * @param style the style mask as defined in the standards document
   */
  public void setTextStyle(int style) {

    // Flush the output before setting a new style
    flush();
    
    // Reset to plain if style is roman, or get the current font style
    // otherwise
    int fontStyle = (style == TEXTSTYLE_ROMAN) ? Font.PLAIN :
          windows[activeWindow].getFont().getStyle();
    
    Font windowFont;
    
    // Ensure that the top window is always set in a fixed font
    if ((style & TEXTSTYLE_FIXED) > 0 || activeWindow == WINDOW_TOP) {
      
      windowFont = fixedFont;
      
    } else {
      
      windowFont = standardFont;
    }
    
    windows[activeWindow].setReverseVideo(
        (style & TEXTSTYLE_REVERSE_VIDEO ) > 0);
    
    fontStyle |= ((style & TEXTSTYLE_BOLD) > 0) ? Font.BOLD : 0;
    fontStyle |= ((style & TEXTSTYLE_ITALIC) > 0) ? Font.ITALIC : 0;
    
    windows[activeWindow].setFont(windowFont.deriveFont(fontStyle));
  }
  
  public void setBufferMode(boolean flag) {

    // only affects bottom window
    flush();
    windows[WINDOW_BOTTOM].setBufferMode(flag);
  }
  
  public void setPaging(boolean flag) {
    
    windows[WINDOW_BOTTOM].setPagingEnabled(flag);
  }
  
  public void flush() {
    
    // save some unnecessary flushes
    if (streambuffer.length() > 0) {
      
      windows[activeWindow].printString(streambuffer.toString());
      streambuffer = new StringBuilder();
    }
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
      Canvas canvas = new CanvasImpl(imageBuffer);
      
      // Default colors
      defaultBackground = Color.WHITE;
      defaultForeground = Color.BLACK;
      setBackground(Color.WHITE);
      setForeground(Color.BLACK);
      
      // Create the two sub windows
      windows[WINDOW_TOP] = new TopWindow(this, editor, canvas);
      windows[WINDOW_TOP].setBackground(defaultBackground);
      windows[WINDOW_TOP].setForeground(defaultForeground);
      // S. 8.7.2.4: use fixed font for upper window
      windows[WINDOW_TOP].setFont(fixedFont);
      fontnumbers[WINDOW_TOP] = ScreenModel.FONT_FIXED;
            
      windows[WINDOW_BOTTOM] = new BottomWindow(this, editor, canvas);           
      windows[WINDOW_BOTTOM].setBackground(defaultBackground);
      windows[WINDOW_BOTTOM].setForeground(defaultForeground);
      windows[WINDOW_BOTTOM].setFont(standardFont);
      fontnumbers[WINDOW_TOP] = ScreenModel.FONT_NORMAL;

      activeWindow = WINDOW_BOTTOM;

      Graphics g_img = imageBuffer.getGraphics();
      g_img.setColor(defaultBackground);
      g_img.fillRect(0, 0, getWidth(), getHeight());
      resizeWindows(0);
      windows[WINDOW_TOP].resetCursorToHome();
      windows[WINDOW_BOTTOM].resetCursorToHome();
      setScreenProperties();
      setInitialized();
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
  
  public boolean isSelected() {
    
    return isSelected;
  }
  
  public void select(boolean flag) {
  
    isSelected = flag;
  }
  
  /**
   * Reset the line counters.
   */
  public void resetPagers() {
    
    windows[WINDOW_TOP].resetPager();
    windows[WINDOW_BOTTOM].resetPager();
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(final short zsciiChar, boolean isInput) {

    //System.out.printf("@print %c (isInput: %b)\n", (char) zsciiChar, isInput);    
    if (zsciiChar == ZsciiEncoding.NEWLINE) {
    
      printChar('\n', isInput);
    
    } else {
    
      printChar(machine.getServices().getZsciiEncoding().getUnicodeChar(
          zsciiChar), isInput);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deletePrevious(short zchar) {
    
    char deleteChar =
      machine.getServices().getZsciiEncoding().getUnicodeChar(zchar);
    windows[activeWindow].backspace(deleteChar);
  }
  
  /**
   * {@inheritDoc}
   */
  public void close() { }
  
  /**
   * {@inheritDoc}
   */
  public void setForegroundColor(int colornum) {
   
    //if (activeWindow == WINDOW_BOTTOM)
    //System.out.printf("setForegroundColor(): %d\n", colornum);    
    if (colornum > 0) {
      
      flush();
      Color foreground = translateColornum(colornum, true);
      windows[WINDOW_TOP].setForeground(foreground);
      windows[WINDOW_BOTTOM].setForeground(foreground);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setBackgroundColor(int colornum) {
    
    //if (activeWindow == WINDOW_BOTTOM)
    //System.out.printf("setBackgroundColor(): %d\n", colornum);    
    if (colornum > 0) {
      
      flush();
      Color background = translateColornum(colornum, false);
      windows[WINDOW_TOP].setBackground(background);
      windows[WINDOW_BOTTOM].setBackground(background);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void redraw() {
    
    repaintInUiThread();
  }
  
  private Color translateColornum(int colornum, boolean foreground) {
    
    switch (colornum) {
    
    case COLOR_DEFAULT:
      return (foreground) ? defaultForeground : defaultBackground;
    case COLOR_BLACK:
      return Color.BLACK;
    case COLOR_RED:
      return Color.RED;
    case COLOR_GREEN:
      return Color.GREEN;
    case COLOR_YELLOW:
      return Color.YELLOW;
    case COLOR_BLUE:
      return Color.BLUE;
    case COLOR_MAGENTA:
      return Color.MAGENTA;
    case COLOR_CYAN:
      return Color.CYAN;
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
  public int setFont(int fontnum) {
    
    flush();
    int previous = fontnumbers[activeWindow];
    switch (fontnum) {
    case FONT_FIXED:
      windows[activeWindow].setFont(fixedFont);
      fontnumbers[activeWindow] = fontnum;
      return previous;
    case FONT_NORMAL:
      windows[activeWindow].setFont(standardFont);
      fontnumbers[activeWindow] = fontnum;
      return previous;
    case FONT_CHARACTER_GRAPHICS:
      
      // CODE-DEBT:
      // Note: if the @set_font command requests font 3, we will switch the
      // window to fixed, so we solve the misalignment, but will return the
      // 0 font anyways. This is not really correct, but will make sure
      // that "Beyond Zork" does not look so weird...
      windows[activeWindow].setFont(fixedFont);
      fontnumbers[activeWindow] = fontnum;
      return 0;
    }
    return 0;
  }
  
  /**
   * {@inheritDoc}
   */
  public synchronized void displayCursor(boolean showCaret) {
    
    windows[activeWindow].drawCursor(showCaret);
  }
  
  // **********************************************************************
  // ******** Private functions
  // *************************************************

  private void printChar(char c, boolean isInput) {

    //if (activeWindow == WINDOW_BOTTOM)
    //  System.out.println("printChar: " + c + " active: " + activeWindow);
    if (isInput || !windows[activeWindow].isBuffered()) {
      
      windows[activeWindow].printString(String.valueOf(c));
      
    } else {
      
      streambuffer.append(c);
    }
  }    
    
  private void determineStandardFont() {
    
    // Sets the fixed font as the standard
    if (machine.getServices().getStoryFileHeader().isEnabled(
        Attribute.FORCE_FIXED_FONT)) {
      
      standardFont = fixedFont;      
    }
  }

  private void resizeWindows(int linesUpperWindow) {
    
    windows[WINDOW_TOP].resize(linesUpperWindow);
    int heightWindowTop = windows[WINDOW_TOP].getHeight();
    windows[WINDOW_BOTTOM].setVerticalBounds(heightWindowTop - 1,
                                             getHeight() - heightWindowTop);
  }

  private void setScreenProperties() {
    
    StoryFileHeader fileheader = machine.getServices().getStoryFileHeader();
    if (fileheader.getVersion() <= 3) {
      
      fileheader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, true);    
      fileheader.setEnabled(Attribute.SUPPORTS_STATUSLINE, true);
      fileheader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, true);
      
    }
    if (fileheader.getVersion() >= 4) {
      
      fileheader.setEnabled(Attribute.SUPPORTS_BOLD, true);
      fileheader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, true);
      fileheader.setEnabled(Attribute.SUPPORTS_ITALIC, true);
      
      FontMetrics fm = imageBuffer.getGraphics().getFontMetrics(fixedFont);
      int screenWidth = imageBuffer.getWidth() / fm.charWidth('0');
      int screenHeight = imageBuffer.getHeight() / fm.getHeight();
      //System.out.println("screenWidth: " + screenWidth
      //    + " screenHeight: " + screenHeight);
      
      fileheader.setScreenWidth(screenWidth);
      fileheader.setScreenHeight(screenHeight);
    }
    
    if (fileheader.getVersion() >= 5) {

      fileheader.setEnabled(Attribute.SUPPORTS_COLOURS, true);
      fileheader.setDefaultBackgroundColor(COLOR_WHITE);
      fileheader.setDefaultForegroundColor(COLOR_BLACK);
      fileheader.setFontWidth(1);
      fileheader.setFontHeight(1);
    }
    determineStandardFont();
  }
  
  private void repaintInUiThread() {
    
    try {
      
      SwingUtilities.invokeAndWait(new Runnable() {
        
        public void run() {
          
          // replace the expensive repaint() call with a fast copying of
          // the double buffer
          if (imageBuffer != null) {
            
            getGraphics().drawImage(imageBuffer, 0, 0, TextViewport.this);
          }
        }
      });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
  }
}
