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

import org.zmpp.vm.ScreenModel;

/**
 * An annotation to indicate how a sequence of characters should be printed.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TextAnnotation {

  // Font flags have the same bit layout as in the ScreenModel interface so
  // so the flags are compatible
  public static final int FONT_NORMAL  = 1;
  public static final int FONT_CHARACTER_GRAPHICS  = 3;
  public static final int FONT_FIXED   = 4;

  // Text styles have the same bit layout as in the ScreenModel interface
  // so the flags are compatible
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
    this(font, style, ScreenModel.COLOR_DEFAULT, ScreenModel.COLOR_DEFAULT);
  }
  
  public TextAnnotation deriveFont(int newFont) {
    return new TextAnnotation(newFont, this.style, this.background,
                              this.foreground);
  }
  
  public TextAnnotation deriveStyle(int newStyle) {
    int finalStyle = style;
    if (style == TextAnnotation.TEXTSTYLE_ROMAN) {
      finalStyle = style;
    } else {
      finalStyle |= style;
    }
    return new TextAnnotation(this.font, finalStyle, this.background,
                              this.foreground);
  }
  
  public TextAnnotation deriveBackground(int newBackground) {
    return new TextAnnotation(this.font, this.style, newBackground,
                              this.foreground);
  }
  
  public TextAnnotation deriveForeground(int newForeground) {
    return new TextAnnotation(this.font, this.style, this.background,
                              newForeground);
  }
  
  public int getFont() { return font; }

  public boolean isFixed() {
    return font == FONT_FIXED || (style & TEXTSTYLE_FIXED) == TEXTSTYLE_FIXED; 
  }
  public boolean isRoman() { return style == TEXTSTYLE_ROMAN; }
  public boolean isBold() {
    return (style & TEXTSTYLE_BOLD) == TEXTSTYLE_BOLD; 
  }
  public boolean isItalic() {
    return (style & TEXTSTYLE_ITALIC) == TEXTSTYLE_ITALIC;
  }
  public boolean isReverseVideo() {
    return (style & TEXTSTYLE_REVERSE_VIDEO) == TEXTSTYLE_REVERSE_VIDEO;
  }
  
  public int getBackground() { return background; }
  public int getForeground() { return foreground; }
}
