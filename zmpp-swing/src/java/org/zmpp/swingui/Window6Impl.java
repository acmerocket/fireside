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
import java.awt.image.BufferedImage;

import org.zmpp.vm.TextCursor;
import org.zmpp.vm.Window6;

public class Window6Impl implements Window6, CursorWindow {

  private TextCursor cursor;
  private FontFactory fontFactory;
  
  private int interruptRoutine;
  private int interruptCount;
  
  private int background;
  private int foreground;
  
  private ScreenFont font;
  
  private Viewport viewport;
  private WindowArea area;
  private WindowStyle style;
  
  public Window6Impl(Viewport viewport, FontFactory fontFactory) {
  
    this.viewport = viewport;
    this.fontFactory = fontFactory;
    
    cursor = new TextCursorImpl(this);
    
    area = new WindowArea();
    style = new WindowStyle();
  }
  
  public TextCursor getCursor() { return cursor; }
  
  public void drawPicture(BufferedImage picture, int y, int x) {

    getCanvas().drawImage(picture, x - 1, y - 1);
  }

  public void move(int y, int x) {

    area.setPosition(x, y);
  }

  public void setBufferMode(boolean flag) {

    style.setIsWrapped(flag);
  }  
  
  public void setSize(int height, int width) {
    
    area.setSize(width, height);
  }

  public void setStyle(int styleflags, int operation) {

    style.setFlags(styleflags, operation);
  }
  
  public void setMargins(int left, int right) {

    area.setMargins(left, right);
  }
  
  public int getProperty(int propertynum) {

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
    case Window6.PROPERTY_FONT_SIZE: return 1; // TODO
    case Window6.PROPERTY_ATTRIBUTES: return style.getFlags();
    case Window6.PROPERTY_LINE_COUNT: return getAvailableLines();
    default: return 0;
    }
  }
  
  private int getColorData() {
   
    return ((background << 8 & 0x0f0) | (foreground & 0x0f));
  }
  
  public void setBackground(int colornum) { background = colornum; }
  private Color getBackgroundColor() {
  
    return ColorTranslator.getInstance().translate(
        background, viewport.getDefaultBackground());
  }
  
  public void setForeground(int colornum) { foreground = colornum; }
  private Color getForegroundColor() {
    
    return ColorTranslator.getInstance().translate(
        foreground, viewport.getDefaultForeground());
  }
    
  protected Color getTextBackground() {
    
    return font.isReverseVideo() ? getForegroundColor() : getBackgroundColor();
  }
  
  protected Color getTextColor() {
    
    return font.isReverseVideo() ? getBackgroundColor() : getForegroundColor();
  }
  
  public int setFont(int fontnum) {
  
    ScreenFont newfont = fontFactory.getFont(fontnum);
    if (newfont == null) return font.getNumber();
    else {
      
      font = newfont;
      return font.getNumber();
    }
  }
  
  public void setTextStyle(int style) {
    
    font = fontFactory.getTextStyle(font, style);
  }
  
  // ************************************************************************
  // ****** CursorWindow interface
  // ***************************************
  
  public void printString(String str) {

    int lineLength = area.getOutputWidth();
    
    WordWrapper wordWrapper =
      new WordWrapper(lineLength, getCanvas(), getFont(), isBuffered());
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);    
  }
  
  public void updateCursorCoordinates() {
    
  }
  
  public boolean isBuffered() { return style.isWrapped(); }
  
  public void backspace(char previousChar) {
    
  }
  
  public void clear() {
    
    area.fill(getCanvas(), getBackgroundColor());
    resetCursorToHome();
  }
  
  // ************************************************************************
  // ****** Private methods
  // ***************************************
  
  private Canvas getCanvas() { return viewport.getCanvas(); }
  
  private Font getFont() { return font.getFont(); }
  
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
                    getCurrentY() - canvas.getFontHeight(getFont())
                    + canvas.getFontDescent(getFont()),
                    canvas.getStringWidth(getFont(), line),
                    canvas.getFontHeight(getFont()));
    canvas.drawString(textColor, getFont(),
                      getCurrentX(), getCurrentY(), line);
    cursor.setColumn(cursor.getColumn() + line.length());
  }
  
  private static boolean endsWithNewLine(String str) {
  
    return str.length() > 0 && str.charAt(str.length() - 1) == '\n';
  }
    

  private void newline() {
    
    cursor.setLine(cursor.getLine() + 1);
    cursor.setColumn(1);
  }

  // ***********************************************************************
  // ****** Coordinate calculation
  // ****************************************************
  
  private int getCurrentX() {
    
    int meanCharWidth = getCanvas().getCharWidth(getFont(), '0');      
    return area.getMarginLeft() + (cursor.getColumn() - 1) * meanCharWidth;
  }
  
  private int getCurrentY() {
    
    Font font = getFont();
    Canvas canvas = getCanvas();
    return area.getStartY() + (cursor.getLine() - 1) * canvas.getFontHeight(font)
           + (canvas.getFontHeight(font) - canvas.getFontDescent(font));
  }
  
  private void resetCursorToHome() {
    
    getCursor().setPosition(getAvailableLines(), 1);
  }

  private int getAvailableLines() {
    
    Canvas canvas = getCanvas();
    int descent = canvas.getFontDescent(getFont());
    int fontHeight = canvas.getFontHeight(getFont());
    return (area.getOutputHeight() - descent) / fontHeight;
  }  
}
