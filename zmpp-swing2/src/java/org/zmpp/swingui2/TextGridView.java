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
import org.zmpp.windowing.AnnotatedCharacter;
import org.zmpp.windowing.TextAnnotation;

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
  private AnnotatedCharacter[][] grid;

  public void setFontSelector(FontSelector selector) {
    this.fontSelector = selector;
  }
  
  public void setGridSize(int numrows, int numcols) {
    grid = new AnnotatedCharacter[numrows][numcols];
  }
  
  public void clear() {
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        grid[row][col] = null;
      }
    }
  }

  private void visualizeCursorPosition(Graphics g, int row, int col) {
    
    // Draw it
    AnnotatedCharacter c = grid[row][col];
    if (c != null) {
      drawCharacter(g, row, col);
    } else {
      //clearCursorPosition(g, row, col);
    }
  }

  private void clearCursorPosition(Graphics g, int row, int col) {
    FontMetrics fontMetrics = g.getFontMetrics(fontSelector.getFixedFont());
    int posy = row * fontMetrics.getHeight() + fontMetrics.getAscent();
    int posx = col * fontMetrics.charWidth('0');
    g.clearRect(posx, posy, fontMetrics.charWidth('0'),
            fontMetrics.getHeight());
  }

  private void drawCharacter(Graphics g, int row, int col) {
    
    // Draw it
    AnnotatedCharacter c = grid[row][col];
    if (c != null) {
      TextAnnotation annotation = c.getAnnotation();
      Font drawfont = fontSelector.getFont(annotation);
      g.setFont(drawfont);
      FontMetrics fontMetrics = g.getFontMetrics();
      int posy = row * fontMetrics.getHeight() + fontMetrics.getAscent();
      int posx = col * fontMetrics.charWidth('0');
      
      ColorTranslator colTranslator = ColorTranslator.getInstance();
      Color foreground = colTranslator.translate(
        annotation.getForeground(), ScreenModel.COLOR_BLACK);
      Color background = colTranslator.translate(
        annotation.getBackground(), ScreenModel.COLOR_WHITE);
      g.setColor(background);
      g.fillRect(posx, row * fontMetrics.getHeight(),
                 fontMetrics.charWidth('0'), fontMetrics.getHeight());
      g.setColor(foreground);    
      g.drawString(String.valueOf(c.getCharacter()), posx, posy);
    }
  }

  public void setCharacter(int line, int column, AnnotatedCharacter c) {
    grid[line - 1][column - 1] = c;
  }

  @Override
  public void paintComponent(Graphics g) {
    //super.paintComponent(g);
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        visualizeCursorPosition(g, row, col);
      }
    }
  }
}
