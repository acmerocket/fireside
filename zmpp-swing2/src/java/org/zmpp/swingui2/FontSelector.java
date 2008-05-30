/*
 * $Id$
 * 
 * Created on 2008/05/01
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
package org.zmpp.swingui2;

import java.awt.Font;
import org.zmpp.windowing.TextAnnotation;

/**
 * Helper class to select a font from a font number and a style number.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class FontSelector {
  
  private Font fixedFont, stdFont;
  
  public void setFixedFont(Font font) { fixedFont = font; }
  
  public void setStandardFont(Font font) { stdFont = font; }
  
  public Font getFont(TextAnnotation annotation) {
    return getStyledFont(annotation.isFixed(), annotation.isBold(),
                         annotation.isItalic());
  }

  public Font getFont(int fontnum, int style) {
    return getFont(new TextAnnotation(fontnum, style));
  }
  
  public Font getFixedFont() {
    return getStyledFont(true, false, false);
  }
  
  private Font getStyledFont(boolean fixed, boolean bold, boolean italic) {
    Font font = fixed ? fixedFont : stdFont;
    if (bold) {
      font = font.deriveFont(Font.BOLD);
    }
    if (italic) {
      font = font.deriveFont(Font.ITALIC);
    }
    return font;
  }
}
