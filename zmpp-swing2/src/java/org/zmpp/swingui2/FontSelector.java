/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zmpp.swingui2;

import java.awt.Font;
import org.zmpp.vm.ScreenModel;

/**
 *
 * @author weiju
 */
public class FontSelector {
  
  private Font fixedFont, stdFont;
  
  public void setFixedFont(Font font) {
    fixedFont = font;
  }
  
  public void setStandardFont(Font font) {
    stdFont = font;
  }
  
  public Font getFont(int fontnum, int style) {
    if (fontnum == ScreenModel.FONT_FIXED ||
        (style & ScreenModel.TEXTSTYLE_FIXED) == ScreenModel.TEXTSTYLE_FIXED) {
      return getFixedFont(style);
    } else if (fontnum == ScreenModel.FONT_NORMAL) {
      return getStandardFont(style);
    }
    return null;
  }
  
  private Font getFixedFont(int style) {
    // TODO: styles
    return fixedFont;
  }
  
  private Font getStandardFont(int style) {
    // TODO: styles
    return stdFont;
  }
}
