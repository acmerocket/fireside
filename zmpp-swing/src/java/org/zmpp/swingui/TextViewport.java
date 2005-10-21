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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JViewport;

public class TextViewport extends JViewport {

  private static final long serialVersionUID = 1L;
  
  private BufferedImage imageBuffer;
  private int y;
  private int x;
  private boolean initialized;
  
  private static final int OFFSET_X = 3;
  private static final int OFFSET_Y = 3;
  
  public TextViewport() {
    
  }
  
  public boolean isInitialized() {
    
    return initialized;
  }
  
  private int getOffsetY() {
    
    return OFFSET_Y;
  }
  
  private int getOffsetX() {
    
    return OFFSET_X;
  }
        
  public void scrollUp() {
    
    Graphics g = imageBuffer.getGraphics();
    g.setClip(3, 3, getWidth() - 6, getHeight() - 6);
    FontMetrics fm = g.getFontMetrics();
    g.copyArea(0, 0, getWidth(), getHeight(), 0, -fm.getHeight());
  }
  
  public void paint(Graphics g) {

    if (imageBuffer == null) {
      
      imageBuffer = new BufferedImage(getWidth(), getHeight(),
          BufferedImage.TYPE_INT_RGB);
      Graphics g_img = imageBuffer.getGraphics();
      FontMetrics fm = g_img.getFontMetrics();        
      y = getOffsetY() + fm.getHeight();
      x = getOffsetX();
              
      g_img.setColor(getBackground());
      g_img.fillRect(0, 0, getWidth(), getHeight());
      
      initialized = true;
    }
    
    g.drawImage(imageBuffer, 0, 0, this);
  }
  
  public void printChar(char c) {
    
    drawCaret(true);
    
    Graphics g = imageBuffer.getGraphics();
    FontMetrics fm = g.getFontMetrics();            
    g.setColor(getForeground());
    int charWidth = fm.charWidth(c);
    g.drawString(String.valueOf(c), x, y);
    x += charWidth;
    
    drawCaret(false);
  }
  
  public void backSpace(char c) {
    
    drawCaret(true);
    
    Graphics g = imageBuffer.getGraphics();
    FontMetrics fm = g.getFontMetrics();
    
    int charWidth = fm.charWidth(c);
    g.setColor(getBackground());
    x -= charWidth;
    g.fillRect(x, y - fm.getMaxAscent(), charWidth, fm.getHeight());
    
    drawCaret(false);
  }

  public void printString(String str) {
    
    Graphics g = imageBuffer.getGraphics();
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
  
  public void newline() {
    
    y += imageBuffer.getGraphics().getFontMetrics().getHeight();
    x = getOffsetX();
  }
  
  public void clear() {
    
    Graphics g = imageBuffer.getGraphics();
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    x = getOffsetX();
    y = getOffsetY();
  }
  
  private void drawCaret(boolean clearCaret) {
    
    Graphics g = imageBuffer.getGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.setColor(clearCaret ? getBackground() : getForeground());
    int charWidth = fm.charWidth('B');
    g.fillRect(x, y - fm.getMaxAscent(), charWidth, fm.getHeight());
  }
  
  public void stopEditing() {
    
    drawCaret(true);
  }
  
  public void startEditing() {
    
    drawCaret(false);
  }
}
