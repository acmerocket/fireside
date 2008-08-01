/*
 * $Id: DisplaySettings.java 535 2008-02-19 06:02:50Z weiju $
 *
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
package org.zmpp.swingui.view;

import java.awt.Font;

/**
 * A class to hold the display settings.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DisplaySettings {

  private Font stdFont;
  private Font fixedFont;
  private int defaultForeground;
  private int defaultBackground;
  private boolean antialias;

  /**
   * Constructor.
   * @param stdFont the standard font
   * @param fixedFont the fixed font
   * @param defaultBackground the default background
   * @param defaultForeground the default foreground
   * @param antialias the antialias flag
   */
  public DisplaySettings(Font stdFont, Font fixedFont,
      int defaultBackground, int defaultForeground, boolean antialias) {
    setSettings(stdFont, fixedFont, defaultBackground,
        defaultForeground, antialias);
  }
  
  /**
   * Returns the standard font.
   * @return the standard font
   */
  public Font getStdFont() { return stdFont; }
  
  /**
   * Returns the fixed font size.
   * @retunr hte fixed font size
   */
  public Font getFixedFont() { return fixedFont; }
  
  /**
   * Returns the default background color number.
   * @return the default background
   */
  public int getDefaultBackground() { return defaultBackground; }
  
  /**
   * Returns the default foreground color number.
   * @return the default foreground
   */
  public int getDefaultForeground() { return defaultForeground; }
  
  /**
   * Returns the antialias flag.
   * @return the antialias flag
   */
  public boolean getAntialias() { return antialias; }
  
  /**
   * Sets the settings.
   * @param stdFont the standard font
   * @param fixedFont the fixed font
   * @param defaultBackground the default background
   * @param defaultForeground the default foreground
   * @param antialias the antialias flag
   */
  public void setSettings(Font stdFont, Font fixedFont,
      int defaultBackground, int defaultForeground, boolean antialias) {
    this.stdFont = stdFont;
    this.fixedFont = fixedFont;
    this.defaultBackground = defaultBackground;
    this.defaultForeground = defaultForeground;
    this.antialias = antialias;
  }
}
