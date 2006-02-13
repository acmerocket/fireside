/*
 * $Id$
 * 
 * Created on 2006/01/23
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
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * The implementation of the Canvas interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class CanvasImpl implements Canvas {

  private BufferedImage image;
  private Graphics graphics;
  
  public CanvasImpl(BufferedImage image) {
    
    this.image = image;
    this.graphics = image.getGraphics();
  }
  
  public int getWidth() {
    
    return image.getWidth();
  }
  
  public int getHeight() {
    
    return image.getHeight();
  }
  
  public void setFont(Font font) {
    
    image.getGraphics().setFont(font);
  }
  
  public int getFontHeight(Font font) {
    
    return graphics.getFontMetrics(font).getHeight();
  }
  
  public int getFontAscent(Font font) {
    
    return graphics.getFontMetrics().getMaxAscent();
  }
  
  public int getCharWidth(Font font, char c) {
    
    return graphics.getFontMetrics(font).charWidth(c);
  }
  
  public int getFontDescent(Font font) {
    
    return graphics.getFontMetrics(font).getMaxDescent();
  }
  
  public int getStringWidth(Font font, String str) {
    
    return graphics.getFontMetrics(font).stringWidth(str);
  }
  
  public void fillRect(Color color, int left, int top, int width,
      int height) {
     
    graphics.setColor(color);
    graphics.fillRect(left, top, width, height);
  }
  
  public void drawString(Color color, Font font, int x, int y, String str) {
   
    graphics.setFont(font);
    graphics.setColor(color);
    graphics.drawString(str, x, y);
  }

  public void scrollUp(Color backColor, Font font, int top, int height) {
    
    int fontHeight = getFontHeight(font);
    graphics.copyArea(0, top + fontHeight, getWidth(),
                      height - fontHeight, 0, -fontHeight);
    graphics.setColor(backColor);
    graphics.fillRect(0, top + height - fontHeight,
                      getWidth(), fontHeight);
  }
}
