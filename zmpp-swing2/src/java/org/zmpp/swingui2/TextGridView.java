/*
 * $Id$
 * 
 * Created on 2008/05/10
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
import java.awt.FontMetrics;
import java.awt.Graphics;
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
public class TextGridView extends JComponent {

  private FontSelector fontSelector;

  public void setFontSelector(FontSelector selector) {
    this.fontSelector = selector;
  }

  @Override
  public void paintComponent(Graphics g) {
    //super.paintComponent(g);
    // TODO: Render with Insets as margins
    Font fixedFont = fontSelector.getFont(ScreenModel.FONT_FIXED,
            ScreenModel.TEXTSTYLE_ROMAN);
    g.setFont(fixedFont);
    g.setColor(Color.BLACK);
    FontMetrics fontMetrics = g.getFontMetrics();
    g.drawString("This is a text", 0, fontMetrics.getAscent());
    g.drawString("This is a text", 0, fontMetrics.getAscent() + fontMetrics.getHeight());
    g.drawString("This is a text", 0, fontMetrics.getAscent() + fontMetrics.getHeight() * 2);
  }
}
