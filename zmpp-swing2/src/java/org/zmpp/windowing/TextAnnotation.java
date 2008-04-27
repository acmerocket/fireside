/*
 * $Id$
 * 
 * Created on 2008/04/26
 * Copyright 2005-2008 by Wei-ju Wu
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.windowing;

/**
 * An annotation to indicate how a sequence of characters should be printed.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TextAnnotation {

  public static final int FONT_NORMAL  = 1;
  public static final int FONT_CHARACTER_GRAPHICS  = 3;
  public static final int FONT_FIXED   = 4;

  public static final int TEXTSTYLE_ROMAN          = 0;
  public static final int TEXTSTYLE_REVERSE_VIDEO  = 1;
  public static final int TEXTSTYLE_BOLD           = 2;
  public static final int TEXTSTYLE_ITALIC         = 4;
  public static final int TEXTSTYLE_FIXED          = 8;

  private int font;
  private int style;
  private int background;
  private int foreground;
  
  public TextAnnotation(int font, int style, int background, int foreground) {
    this.font = font;
    this.style = style;
    this.background = background;
    this.foreground = foreground;
  }
  
  public TextAnnotation(int font, int style) {
    this(font, style, -1, -1);
  }
  
  public int getFont() { return font; }
  public int getStyle() { return style; }
  public int getBackground() { return background; }
  public int getForeground() { return foreground; }
}
