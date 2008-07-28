/*
 * $Id: ColorTranslator.java 535 2008-02-19 06:02:50Z weiju $
 * 
 * Created on 2006/02/24
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

import java.awt.Color;
import org.zmpp.windowing.ScreenModel;

/**
 * This class translates color numbers into Java AWT Color objects. It
 * was outfactored from the Viewport because it is common behaviour which
 * is shared between model 6 and non-model 6 screen models.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ColorTranslator {

  private static final Color GREEN    = new Color(0, 190, 0);
  private static final Color RED      = new Color(190, 0, 0);
  private static final Color YELLOW   = new Color(190, 190, 0);
  private static final Color BLUE     = new Color(0, 0, 190);
  private static final Color MAGENTA  = new Color(190, 0, 190);
  private static final Color CYAN     = new Color(0, 190, 190);
  
  private static ColorTranslator instance = new ColorTranslator();

  /**
   * Constructor.
   */
  private ColorTranslator()  { }

  /**
   * Returns the singleton instance.
   * @return the singleton instance
   */
  public static ColorTranslator getInstance() { return instance; }
  
  /**
   * Translates the specified color number.
   * 
   * @param colornum the color number
   * @param defaultColor the default color
   * @return the color for the number
   */
  public Color translate(int colornum, int defaultColor) {
    switch (colornum) {
    case ScreenModel.COLOR_DEFAULT:
      return translate(defaultColor, ScreenModel.UNDEFINED);
    case ScreenModel.COLOR_BLACK:
      return Color.BLACK;
    case ScreenModel.COLOR_RED:
      return RED;
    case ScreenModel.COLOR_GREEN:
      return GREEN;
    case ScreenModel.COLOR_YELLOW:
      return YELLOW;
    case ScreenModel.COLOR_BLUE:
      return BLUE;
    case ScreenModel.COLOR_MAGENTA:
      return MAGENTA;
    case ScreenModel.COLOR_CYAN:
      return CYAN;
    case ScreenModel.COLOR_WHITE:
      return Color.WHITE;
    case ScreenModel.COLOR_MS_DOS_DARKISH_GREY:
      return Color.DARK_GRAY;
    }
    return Color.BLACK;
  }

  /**
   * Translates the specified color number to a Color object.
   * @param colornum the color number
   * @return the color
   */
  public Color translate(int colornum) {
    return translate(colornum, ScreenModel.UNDEFINED);
  }
}
