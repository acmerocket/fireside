/**
 * $Id$
 */
package org.zmpp.swingui;

/**
 * A class to hold the display settings.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DisplaySettings {

  private int stdFontSize;
  private int fixedFontSize;
  private int defaultForeground;
  private int defaultBackground;
  private boolean antialias;

  /**
   * Constructor.
   * @param stdFontSize the standard font size
   * @param fixedFontSize the fixed font size
   * @param defaultBackground the default background
   * @param defaultForeground the default foreground
   * @param antialias the antialias flag
   */
  public DisplaySettings(int stdFontSize, int fixedFontSize,
      int defaultBackground, int defaultForeground, boolean antialias) {
    setSettings(stdFontSize, fixedFontSize, defaultBackground,
        defaultForeground, antialias);
  }
  
  /**
   * Returns the standard font size.
   * @return the standard font size
   */
  public int getStdFontSize() { return stdFontSize; }
  
  /**
   * Returns the fixed font size.
   * @retunr hte fixed font size
   */
  public int getFixedFontSize() { return fixedFontSize; }
  
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
   * @param stdFontSize the standard font size
   * @param fixedFontSize the fixed font size
   * @param defaultBackground the default background
   * @param defaultForeground the default foreground
   * @param antialias the antialias flag
   */
  public void setSettings(int stdFontSize, int fixedFontSize,
      int defaultBackground, int defaultForeground, boolean antialias) {
    this.stdFontSize = stdFontSize;
    this.fixedFontSize = fixedFontSize;
    this.defaultBackground = defaultBackground;
    this.defaultForeground = defaultForeground;
    this.antialias = antialias;
  }
}
