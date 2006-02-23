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
import java.awt.image.BufferedImage;

/**
 * This interface defines an abstract access to the Java graphics system
 * so it will be easier to test.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface Canvas {

  int getWidth();
  int getHeight();
  
  int getFontHeight(Font font);
  int getFontAscent(Font font);
  int getFontDescent(Font font);
  int getCharWidth(Font font, char c);
  int getStringWidth(Font font, String str);
    
  void scrollUp(Color backColor, Font font, int top, int height);
  void fillRect(Color color, int left, int top, int width, int height);
  void drawString(Color color, Font font, int x, int y, String str);
  void setClip(int left, int top, int width, int height);
  void drawImage(BufferedImage image, int x, int y);
}
