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
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import org.zmpp.vm.ScreenModel;

/**
 * A virtual component that represents a text view with flexible layout in
 * the Z-machine or Glk screen model.
 * Rather than representing the view through its own Swing component,
 * conceptually it just a clipped and scrollable area within a hosting
 * component.
 * A text window differs from a text grid in that it is potentially
 * only showing a section of the complete data. Therefore, it contains
 * additional information about the position within the text. It also
 * uses automatic word wrapping.
 * 
 * Therefore, there is a need to solve the following probems:
 * 
 * - converting strings from the Z-machine core's screen model to
 *   AttributeStrings
 * - buffering up those lines of AttributeStrings
 * - layout calculation for word wrapping
 * - scrolling
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TextWindowView extends VirtualView {
  
  private int textY;
  private List<AttributedString> paragraphs = new LinkedList<AttributedString>();
  
  public TextWindowView(JComponent hostingComponent,
                        FontSelector fontSelector) {
    super(hostingComponent, fontSelector);
    paragraphs.add(new AttributedString("This is a text - so ein Quatsch"));
    paragraphs.add(new AttributedString("Another line of text"));
    paragraphs.add(new AttributedString("Yet another line of text"));
    paragraphs.add(new AttributedString("Do you have enough now ?"));
  }
  
  @Override
  public void paint(Graphics2D g2d) {
    super.paint(g2d);
    if (!isValid()) {
      doLayout(g2d);
    }
    //scroll(g2d, -25);
  }
  
  private void scroll(Graphics2D g2d, int numPixels) {
    g2d.copyArea(getUsableLeft(), getUsableTop(), getUsableWidth(),
                 getUsableHeight(), 0, numPixels);
  }
  
  @Override
  protected void doLayout(Graphics2D g2d) {
    super.doLayout(g2d);
    clearArea(g2d);
    g2d.setColor(Color.BLACK);
    textY = getUsableTop(); // - 8;
    for (AttributedString str : paragraphs) {
      // Just as an example how to attribute strings
      str.addAttribute(TextAttribute.FONT, getStandardFont());
      layoutString(g2d, str);
      newLine(g2d);
    }
  }
  
  private Font getStandardFont() {
    return getFontSelector().getFont(ScreenModel.FONT_NORMAL,
                                     ScreenModel.TEXTSTYLE_ROMAN);
  }
  
  private void newLine(Graphics2D g2d) {
    textY += ((double) (g2d.getFontMetrics(getStandardFont()).getHeight()) * 1.1);
  }

  private void layoutString(Graphics2D g2d, AttributedString str) {
    g2d.drawString(str.getIterator(), getUsableLeft(),
                   textY + g2d.getFontMetrics(getStandardFont()).getAscent());
  }
}
