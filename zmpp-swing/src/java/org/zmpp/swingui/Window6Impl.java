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
import java.awt.Dimension;
import java.awt.Font;

import org.zmpp.blorb.BlorbImage;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.TextCursor;
import org.zmpp.vm.Window6;

/**
 * Windows area the most important aspect in the V6 screen model, all output
 * is done in respect to them.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Window6Impl implements Window6, CursorWindow {

  private TextCursor cursor;
  private FontFactory fontFactory;
  
  private int interruptRoutine;
  private int interruptCount;
  private int linecount;
  
  private int background;
  private int foreground;
  
  private ScreenFont font;
  
  private Viewport6 viewport;
  private WindowArea area;
  private WindowStyle style;
  private int windownum;

  /**
   * Constructor.
   * 
   * @param viewport the viewport containing the window
   * @param fontFactory the font factory
   * @param num the window number
   */
  public Window6Impl(Viewport6 viewport, FontFactory fontFactory, int num) {
  
    this.viewport = viewport;
    this.fontFactory = fontFactory;
    this.windownum = num;
    
    cursor = new TextCursorImpl(this);
    
    area = new WindowArea();
    style = new WindowStyle();
  }
  
  /**
   * Returns this window's cursor object.
   * 
   * @return the cursor
   */
  public TextCursor getCursor() { return cursor; }
  
  /**
   * Displays or hides the cursor.
   * 
   * @param show if true, the cursor is shown, otherwise it is hidden
   */
  public void displayCursor(boolean show) {
    
    Color color = show ? getForegroundColor() : getBackgroundColor();
    getCanvas().fillRect(color, getCurrentX(),
        getCurrentY() - getCanvas().getFontAscent(getFont()),
        getFontWidth(), getFontHeight());
  }

  /**
   * {@inheritDoc}
   */
  public void drawPicture(BlorbImage picture, int y, int x) {

    Dimension size = picture.getSize(viewport.getWidth(), viewport.getHeight());    
    getCanvas().drawImage(picture.getImage(), area.getStartX() + (x - 1),
        area.getStartY() + (y - 1), size.width, size.height);
  }

  /**
   * {@inheritDoc}
   */
  public void erasePicture(BlorbImage picture, int y, int x) {

    Dimension size = picture.getSize(viewport.getWidth(), viewport.getHeight());    
    getCanvas().fillRect(getBackgroundColor(), area.getStartX() + (x - 1),
        area.getStartY() + (y - 1), size.width, size.height);
  }

  /**
   * {@inheritDoc}
   */
  public void move(int y, int x) {

    System.out.printf("window.move(): %d %d %d\n", windownum, x, y);
    area.setPosition(x, y);
  }

  /**
   * Sets the buffer mode of the window.
   * 
   * @param flag the buffer mode flag
   */
  public void setBufferMode(boolean flag) {

    style.setIsWrapped(flag);
  }  
  
  /**
   * {@inheritDoc}
   */
  public void setSize(int height, int width) {
    
    //System.out.printf("win %d: setSize(): w: %d h: %d\n", windownum, width, height);
    area.setSize(width, height);
  }

  /**
   * {@inheritDoc}
   */
  public void setStyle(int styleflags, int operation) {

    //System.out.printf("win %d: setStyle(): %d %d\n", windownum, styleflags, operation);
    style.setFlags(styleflags, operation);
  }

  /**
   * {@inheritDoc}
   */
  public void setMargins(int left, int right) {

    //System.out.printf("setMargins(): %d %d\n", left, right);
    area.setMargins(left, right);
  }

  /**
   * {@inheritDoc}
   */
  public int getProperty(int propertynum) {

    //System.out.printf("win: %d getProperty(): %d\n", windownum, propertynum);
    switch (propertynum) {
    case Window6.PROPERTY_Y_COORD: case Window6.PROPERTY_X_COORD:
    case Window6.PROPERTY_Y_SIZE: case Window6.PROPERTY_X_SIZE:
    case Window6.PROPERTY_LEFT_MARGIN: case Window6.PROPERTY_RIGHT_MARGIN:
      return area.getProperty(propertynum);
    case Window6.PROPERTY_X_CURSOR: return cursor.getColumn();
    case Window6.PROPERTY_Y_CURSOR: return cursor.getLine();
    case Window6.PROPERTY_INTERRUPT_COUNT: return interruptCount;
    case Window6.PROPERTY_INTERRUPT_ROUTINE: return interruptRoutine;
    case Window6.PROPERTY_FONT_NUMBER: return font.getNumber();    
    case Window6.PROPERTY_TEXTSTYLE: return font.getStyle();    
    case Window6.PROPERTY_COLOURDATA: return getColorData();
    case Window6.PROPERTY_FONT_SIZE: return getFontSize();
    case Window6.PROPERTY_ATTRIBUTES: return style.getFlags();
    case Window6.PROPERTY_LINE_COUNT: return linecount;
    default: return 0;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void putProperty(int propertynum, short value) {
    
    System.out.printf("window %d putProperty() prop: %d value: %d\n",
        windownum, propertynum, value);
    
    // this method mainly is to set the interrupt function setup, all
    // other properties are not supported
    switch (propertynum) {
    
      case Window6.PROPERTY_INTERRUPT_COUNT:
        interruptCount = value;
        break;
      case Window6.PROPERTY_INTERRUPT_ROUTINE:        
        interruptRoutine = value;
        break;
      case Window6.PROPERTY_LINE_COUNT:        
        linecount = value;
        break;
      default:
        System.out.println("unsupported property to set: " + propertynum);
    }
  }
  
  /**
   * Returns the font size, the high byte contains the height, the low
   * byte the width.
   * 
   * @return the font size
   */
  private int getFontSize() {
    
    return ((getFontHeight() << 8 & 0xff00) | (getFontWidth() & 0xff));
  }
  
  private int getFontHeight() {
    
    StoryFileHeader fileheader =
      viewport.getMachine().getGameData().getStoryFileHeader();
    return fileheader.getFontHeight();
  }
  
  private int getFontWidth() {
    
    StoryFileHeader fileheader =
      viewport.getMachine().getGameData().getStoryFileHeader();
    return fileheader.getFontWidth();
  }
  
  /**
   * Packs the current color numbers into an 16-bit integer value,
   * background color in the hi-byte, foreground in the low-byte.
   * 
   * @return the color data
   */
  private int getColorData() {
   
    return ((background << 8 & 0xff00) | (foreground & 0xff));
  }
  
  /**
   * Sets the new background color.
   * 
   * @param colornum the color number
   */
  public void setBackground(int colornum) {
    
    if (colornum != ColorTranslator.COLOR_CURRENT) {
      
      background = colornum;
    }
  }

  /**
   * Sets the new foreground color.
   * 
   * @param colornum the color number
   */
  public void setForeground(int colornum) {

    if (colornum != ColorTranslator.COLOR_CURRENT) {
      
      foreground = colornum;
    }
  }

  /**
   * Sets the font in the current window.
   * 
   * @param fontnum the new font number
   * @return the new font number
   */
  public int setFont(int fontnum) {
  
    ScreenFont newfont = fontFactory.getFont(fontnum);
    if (newfont == null) {
      
      return font.getNumber();
      
    } else {
      
      font = newfont;
      return font.getNumber();
    }
  }
  
  /**
   * Sets a new text style.
   * 
   * @param style the text style to set
   */
  public void setTextStyle(int style) {
    
    font = fontFactory.getTextStyle(font, style);
  }
  
  /**
   * Perform an erase operation on the current line. If the specified number
   * of pixels is 1, the line will be erased from the current position to
   * the end of the window
   * 
   * @param value the number of pixels
   */
  public void eraseLine(int value) {
  
    System.out.printf("@erase_line, win: %d value: %d\n", windownum, value);
    Canvas canvas = getCanvas();
    
    if (value == 1) {
      
      // erase from the current x position to the end of the line in the
      // current window
      // Note: This could be moved to the canvas itself
      int currentX = getCurrentX();
      area.clip(canvas);
      canvas.fillRect(getBackgroundColor(), currentX,
                      getCurrentY() - canvas.getFontAscent(font.getFont()),
                      area.getOutputWidth() - currentX,
                      getFontHeight());
    } else {
      
      // erase an area of value pixels right from the current position
      area.clip(canvas);
      canvas.fillRect(getBackgroundColor(), getCurrentX(),
          getCurrentY() - canvas.getFontAscent(font.getFont()),
          value, getFontHeight());
    }
  }
  
  /**
   * Returns the area height in pixels.
   * 
   * @return the area height in pixels
   */
  public int getHeight() { return area.getHeight(); }

  /**
   * Resizes the windows to display the given numbe of lines.
   * 
   * @param lines the number of lines
   */
  public void resize(int lines) {
  
    System.out.printf("resize(), win: %d, lines: %d\n", windownum, lines);
    int height = getCanvas().getFontHeight(getFont()) * lines;
    area.setPosition(1, 1);
    area.setSize(getCanvas().getWidth(), height);
    cursor.setPosition(1, 1); // XXX This is arbitrarily set by me
  }

  /**
   * Sets the vertical bounds of a window, leaving the left position and
   * width where they are.
   * 
   * @param top the top position
   * @param height the height of the window
   */
  public void setVerticalBounds(int top, int height) {
    
    System.out.printf("setVerticalBounds(), win: %d, top: %d height: %d\n", windownum, top, height);
    area.setPosition(1, top);
    area.setSize(getCanvas().getWidth(), height);
    cursor.setPosition(1, 1); // XXX This is arbitrarily set by me
  }
  
  // ************************************************************************
  // ****** CursorWindow interface
  // ***************************************
  
  /**
   * {@inheritDoc}
   */
  public void printChar(char c, boolean isInput) {

    if (isZorkZeroSpecial()) {

      printCharZorkZeroSpecial(c, isInput);
      
    } else {
      
      printCharStandard(c, isInput);
    }
  }
  
  private boolean isZorkZeroSpecial() {
    
    StoryFileHeader fileheader =
      viewport.getMachine().getGameData().getStoryFileHeader();
    return (fileheader.getInterpreterNumber() == 6
        && fileheader.getRelease() == 393
        && "890714".equals(fileheader.getSerialNumber()));    
  }

  /**
   * {@inheritDoc}
   */
  public void updateCursorCoordinates() {
    
    // the cursor in V6 is pixel-based, so there is no need to do anything
    // here
  }
  
  /**
   * {@inheritDoc}
   */
  public void flushBuffer() {
    
    // TODO
  }

  /**
   * {@inheritDoc}
   */
  public void backspace(char previousChar) {
    
    // TODO
  }
  
  /**
   * {@inheritDoc}
   */
  public void clear() {
    
    area.fill(getCanvas(), getBackgroundColor());
    resetCursorToHome();
  }
  
  // ************************************************************************
  // ****** Private methods
  // ***************************************
  
  private Canvas getCanvas() { return viewport.getCanvas(); }
  
  private Font getFont() { return font.getFont(); }
  
  private Color getBackgroundColor() {
    
    if (background == ColorTranslator.COLOR_UNDER_CURSOR) {
     
      return getCanvas().getColorAtPixel(getCurrentX(), getCurrentY());
    }
    return ColorTranslator.getInstance().translate(
        background, viewport.getDefaultBackground());
  }
  
  private Color getForegroundColor() {
    
    if (foreground == ColorTranslator.COLOR_UNDER_CURSOR) {
      
      return getCanvas().getColorAtPixel(getCurrentX(), getCurrentY());
    }
    return ColorTranslator.getInstance().translate(
        foreground, viewport.getDefaultForeground());
  }
    
  private Color getTextBackground() {
    
    return font.isReverseVideo() ? getForegroundColor() : getBackgroundColor();
  }
  
  private Color getTextColor() {
    
    return font.isReverseVideo() ? getBackgroundColor() : getForegroundColor();
  }  
  
  // ***********************************************************************
  // ****** Coordinate calculation
  // ****** In V6, the units are in pixels, so the cursor column position is
  // ****** actually also the x position
  // ****************************************************
  
  private int getCurrentX() {
    
    //System.out.println("window: " + windownum + ", currentX: " + (area.getMarginLeft() + (cursor.getColumn() - 1)));    
    return area.getStartX() + (cursor.getColumn() - 1);
  }

  private int getCurrentY() {
    
    // Current y always returns the baseline for the current line
    // which means it is the cursor-y - (fontheight - fontdescent)
    Font font = getFont();
    Canvas canvas = getCanvas();
    
    //int y =  area.getStartY() + (cursor.getLine() - 1)
    //        + (canvas.getFontHeight(font) - canvas.getFontDescent(font)) ;    
    //System.out.println("window: " + windownum + ", currentY: " + y);
    return area.getStartY() + (cursor.getLine() - 1)
           + (getFontHeight() - canvas.getFontDescent(font)) ;
  }
  
  private void resetCursorToHome() {
    
    getCursor().setPosition(1, 1);
  }

  private void scrollIfNeeded() {

    int fontDescent = getCanvas().getFontDescent(getFont());
    area.clip(getCanvas());
    
    // We calulate an available height with a correction amount
    // of fontDescent to reserve enough scrolling space
    while (getCurrentY() > (area.getStartY() + area.getHeight() - fontDescent)) {
      
      getCanvas().scrollUp(getBackgroundColor(), area.getStartX(),
          area.getStartY(), area.getWidth(), area.getHeight(), getFontHeight());
      getCursor().setLine(getCursor().getLine() - getFontHeight());
    }
  }
  
  // **********************************************************************
  // ****** Printing methods, standard
  // **********************************************

  private void printCharStandard(char c, boolean isInput) {
    
    handleNewLineInterrupt(c);    
    printString(String.valueOf(c));
  }
  
  private void printString(String str) {

    int lineLength = area.getOutputWidth();
    //System.out.printf("printString(): %s  window: %d linelength: %d wrapped: %b buffered: %b x: %d\n",
    //    str, windownum, lineLength, style.isWrapped(), style.outputIsBuffered(), getCurrentX());    
    WordWrapper wordWrapper =
      new WordWrapper(lineLength, getCanvas(), getFont(), style.isWrapped());
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);
  }

  private void printLines(String lines[]) {
    
    Color textColor = getTextColor();
    Color textbackColor = getTextBackground();    
    
    // This is a feature that is not specified, but it is supported by
    // DOS Frotz
    if ((getFont().getStyle() & Font.BOLD) > 0 && !font.isReverseVideo()) {

      textColor = textColor.brighter();
    }
        
    for (int i = 0; i < lines.length; i++) {

      String line = lines[i];
      printLine(line, textbackColor, textColor);
      
      if (endsWithNewLine(line)) {
        
        newline();
      }
    }
  }  
  
  private void printLine(String line, Color textbackColor,
      Color textColor) {

    Canvas canvas = getCanvas();
    area.clip(canvas);
    canvas.fillRect(textbackColor, getCurrentX(),
                    getCurrentY() - getFontHeight()
                    + canvas.getFontDescent(getFont()),
                    canvas.getStringWidth(getFont(), line),
                    getFontHeight());
    
    scrollIfNeeded();
    
    //System.out.printf("printLine win: %d (%d, %d) str: [%s], x: %d y: %d fg: %d bg: %d\n",
    //    windownum, area.getStartX(), area.getStartY(),
    //    line, getCurrentX(), getCurrentY(), foreground, background);
    canvas.drawString(textColor, getFont(),
                      getCurrentX(), getCurrentY(), line);
    cursor.setColumn(cursor.getColumn() + 
        canvas.getStringWidth(getFont(), line));
  }
  
  private static boolean endsWithNewLine(String str) {
    
    return str.length() > 0 && str.charAt(str.length() - 1) == '\n';
  }
    

  private void newline() {
    
    cursor.setLine(cursor.getLine() + getFontHeight());
    cursor.setColumn(1);
  }

  /**
   * Check, if newline interrupt needs to be called.
   * 
   * @param c the character
   */
  private void handleNewLineInterrupt(char c) {
    
    if (c == '\n' && interruptCount > 0) {
      
      linecount--;
      System.out.println("line count is now: " + linecount);
      if (linecount <= 0) {
        
        linecount = 0;
        System.out.println("calling interrupt");
        viewport.getMachine().getCpu().callInterrupt(interruptRoutine);
        interruptCount--;
        System.out.println("interrupt count is now: " + interruptCount);
      }
    }
  }
  
  // **********************************************************************
  // ****** Printing methods, Zork Zero special
  // ****** Since printing is so special, it should be
  // ****** outfactored in a pluggable object that is given to
  // ****** the window on start up
  // **********************************************
  
  private void printCharZorkZeroSpecial(char c, boolean isInput) {
    
    // Handle the special case of
    // release 393.890714 and Interpreter version 6
    printCharStandard(c, isInput);
  }    
}
