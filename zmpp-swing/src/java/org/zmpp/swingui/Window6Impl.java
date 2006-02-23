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
  private Color background;
  private Color foreground;
  private Font font;
  private boolean isReverseVideo;
  private TextCursor cursor;
  private int left, top, width, height;
  private int marginLeft, marginRight;
  
  public Window6Impl(Canvas canvas) {
  
    this.canvas = canvas;
    cursor = new TextCursorImpl(this);
  }
  
  public TextCursor getCursor() {
    
    return cursor;
  }
  
  public void drawPicture(BufferedImage picture, int y, int x) {

    canvas.drawImage(picture, x, y);
  }

  public void move(int y, int x) {

    this.left = x;
    this.top = y;
  }

  public void setSize(int height, int width) {
    
    this.height = height;
    this.width = width;
  }

  public void setStyle(int styleflags, int operation) {
    // TODO Auto-generated method stub

  }
  
  public void setMargins(int left, int right) {
    
    marginLeft = left;
    marginRight = right;
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
    
    return isReverseVideo ? foreground : background;
  }
  
  protected Color getTextColor() {
    
    return isReverseVideo ? background : foreground;
  }
  
  public Font getFont() {
    
    return font;
  }
  
  public void setFont(Font font) {
    
    this.font = font;
  }
  
  private int getCurrentX() {
    
    int meanCharWidth = canvas.getCharWidth(getFont(), '0');      
    return (cursor.getColumn() - 1) * meanCharWidth;
  }
  private int getCurrentY() {
    
    Font font = getFont();
    return top + (cursor.getLine() - 1) * canvas.getFontHeight(font)
           + (canvas.getFontHeight(font) - canvas.getFontDescent(font));
  }
  
  public boolean isBuffered() {
    
    return true;
  }
  
  public void printString(String str) {

    //System.out.println("printString(): " + str);
    int width = canvas.getWidth();
    int lineLength = width;
    
    WordWrapper wordWrapper =
      new WordWrapper(lineLength, canvas, font, isBuffered());
    String[] lines = wordWrapper.wrap(getCurrentX(), str);
    printLines(lines);    
  }
  
  private void printLines(String lines[]) {
    
    Color textColor = getTextColor();
    Color textbackColor = getTextBackground();    
    
    // This is a feature that is not specified, but it is supported by
    // DOS Frotz
    if ((getFont().getStyle() & Font.BOLD) > 0 && !isReverseVideo) {

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
  
  protected void printLine(String line, Color textbackColor,
      Color textColor) {

    //clipToCurrentBounds();
    canvas.fillRect(textbackColor, getCurrentX(),
                    getCurrentY() - canvas.getFontHeight(font)
                    + canvas.getFontDescent(font),
                    canvas.getStringWidth(font, line),
                    canvas.getFontHeight(font));
    canvas.drawString(textColor, font,
                      getCurrentX(), getCurrentY(), line);
    cursor.setColumn(cursor.getColumn() + line.length());
  }
  
  private static boolean endsWithNewLine(String str) {
  
    return str.length() > 0 && str.charAt(str.length() - 1) == '\n';
  }
    
  protected void clipToCurrentBounds() {
    
    canvas.setClip(0, top, canvas.getWidth(), height);
  }

  public void updateCursorCoordinates() {
    
  }
  
  protected void newline() {
    
    cursor.setLine(cursor.getLine() + 1);
    cursor.setColumn(1);
  }
  
}
