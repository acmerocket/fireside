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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import org.zmpp.vm.ScreenModel;

/**
 * A class representing a text grid in a Z-machine or Glk screen model.
 * Rather than representing the view through its own Swing component,
 * conceptually it just a clipped area within a hosting component. 
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TextGridView extends VirtualView {

  public TextGridView(JComponent hostingComponent, FontSelector fontSelector) {
    super(hostingComponent, fontSelector);
  }
  
  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);
    g2d.setColor(Color.BLACK);
    int textY = getUsableTop(); // - 8;
    Font font = getFixedFont();
    g2d.setFont(font);
    g2d.drawString("Fixed text", getUsableLeft(), textY + g2d.getFontMetrics(font).getAscent());
  }
  
  /**
   * Returns the fixed font in roman. For grids, only fixed width fonts
   * make sense.
   * @return the fixed font
   */
  private Font getFixedFont() {
    return getFontSelector().getFont(ScreenModel.FONT_FIXED,
                                     ScreenModel.TEXTSTYLE_ROMAN);
  }
}
