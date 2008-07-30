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
 * @version 1.5
 */
public class FontSelector {
  
  private Font fixedFont, stdFont;
  
  /**
   * Sets the fixed font in the system.
   * @param font the fixed font
   */
  public void setFixedFont(Font font) { fixedFont = font; }
  
  /**
   * Sets the standard font in the system.
   * @param font the standard font
   */
  public void setStandardFont(Font font) { stdFont = font; }
  
  /**
   * Returns a font object for the specified TextAnnotation.
   * @param annotation the TextAnnotation
   * @return the font
   */
  public Font getFont(TextAnnotation annotation) {
    return getStyledFont(annotation.isFixed(), annotation.isBold(),
                         annotation.isItalic());
  }

  /**
   * Returns a font with the specified font number and style. This is a
   * convenience method.
   * @param fontnum the font number
   * @param style the style
   * @return the font object
   */
  public Font getFont(char fontnum, int style) {
    return getFont(new TextAnnotation(fontnum, style));
  }
  
  /**
   * Returns the roman fixed font object.
   * @return the roman fixed font object
   */
  public Font getFixedFont() { return getStyledFont(true, false, false); }
  
  /**
   * Returns a styled font object for the specified attributes.
   * @param fixed true if fixed font
   * @param bold true if bold style
   * @param italic true if italic style
   * @return the font object
   */
  private Font getStyledFont(boolean fixed, boolean bold, boolean italic) {
    Font font = fixed ? fixedFont : stdFont;
    if (bold) { font = font.deriveFont(Font.BOLD); }
    if (italic) { font = font.deriveFont(Font.ITALIC); }
    return font;
  }
}
