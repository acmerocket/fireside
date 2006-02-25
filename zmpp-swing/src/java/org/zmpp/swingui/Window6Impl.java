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

  private Canvas canvas;
  private TextCursor cursor;
  private LineEditor editor;
  private FontFactory fontFactory;
  
  private Color background;
  private Color foreground;
  
  private ScreenFont font;
  
  private boolean buffered;

  private WindowArea area;
  
  public Window6Impl(Canvas canvas, LineEditor editor,
      FontFactory fontFactory) {
  
    this.canvas = canvas;
    this.editor = editor;
    this.fontFactory = fontFactory;
    
    cursor = new TextCursorImpl(this);
    
    area = new WindowArea();
    area.setPosition(1, 1);
  }
  
  public TextCursor getCursor() {
    
    return cursor;
  }
  
  public void drawPicture(BufferedImage picture, int y, int x) {

    canvas.drawImage(picture, x - 1, y - 1);
  }

  public void move(int y, int x) {

    area.setPosition(x, y);
  }

  public void setBufferMode(boolean flag) {
    
    buffered = flag;
  }
  
  
  public void setSize(int height, int width) {
    
    area.setSize(width, height);
  }

  public void setStyle(int styleflags, int operation) {

  }
  
  public void setMargins(int left, int right) {

    area.setMargins(left, right);
  }
  
  public int getProperty(int propertynum) {
    
    // TODO
    return 0;
  }
  
  public void setBackground(Color color) {
    
    background = color;
  }
  
  public void setForeground(Color color) {
    
    foreground = color;
  }
  
  protected Color getTextBackground() {
    
    return font.isReverseVideo() ? foreground : background;
  }
  
  protected Color getTextColor() {
    
    return font.isReverseVideo() ? background : foreground;
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
    
    this.font = fontFactory.getTextStyle(font, style);
  }
  
  // ************************************************************************
  // ****** CursorWindow interface
  // ***************************************
  
  public void printString(String str) {

    int lineLength = area.getOutputWidth();
    
    WordWrapper wordWrapper =
      new WordWrapper(lineLength, canvas, getFont(), isBuffered());
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);    
  }
  
  public void updateCursorCoordinates() {
    
  }
  
  public boolean isBuffered() {
    
    return buffered;
  }
  
  public void backspace(char previousChar) {
    
  }
  
  public void clear() {
    
    area.fill(canvas, background);
    resetCursorToHome();
  }
  
  // ************************************************************************
  // ****** Private methods
  // ***************************************
  
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
    
    int meanCharWidth = canvas.getCharWidth(getFont(), '0');      
    return area.getMarginLeft() + (cursor.getColumn() - 1) * meanCharWidth;
  }
  
  private int getCurrentY() {
    
    Font font = getFont();
    return area.getStartY() + (cursor.getLine() - 1) * canvas.getFontHeight(font)
           + (canvas.getFontHeight(font) - canvas.getFontDescent(font));
  }
  
  private void resetCursorToHome() {
    
    getCursor().setPosition(getAvailableLines(), 1);
  }

  private int getAvailableLines() {
    
    int descent = canvas.getFontDescent(getFont());
    int fontHeight = canvas.getFontHeight(getFont());
    return (area.getOutputHeight() - descent) / fontHeight;
  }
}
