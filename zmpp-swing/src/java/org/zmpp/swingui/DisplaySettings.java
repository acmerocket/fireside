/**
 * $Id$
 */
package org.zmpp.swingui;

public class DisplaySettings {

  private int stdFontSize;
  private int fixedFontSize;
  private int defaultForeground;
  private int defaultBackground;
  
  public DisplaySettings(int stdFontSize, int fixedFontSize,
      int defaultBackground, int defaultForeground) {
    
    this.stdFontSize = stdFontSize;
    this.fixedFontSize = fixedFontSize;
    this.defaultBackground = defaultBackground;
    this.defaultForeground = defaultForeground;
  }
  
  public int getStdFontSize() { return stdFontSize; }
  public int getFixedFontSize() { return fixedFontSize; }
  public int getDefaultBackground() { return defaultBackground; }
  public int getDefaultForeground() { return defaultForeground; }
}
