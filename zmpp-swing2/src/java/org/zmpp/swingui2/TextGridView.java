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
import org.zmpp.vm.BufferedScreenModel;

/**
 * A class representing a text grid in a Z-machine or Glk screen model.
 * Rather than representing the view through its own Swing component,
 * conceptually it just a clipped area within a hosting component. 
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class TextGridView extends JComponent {

  private static final char REF_CHAR = '0';
  private FontSelector fontSelector;
  private AnnotatedCharacter[][] grid;
  private BufferedScreenModel screenModel;

  public void setScreenModel(BufferedScreenModel screenModel) {
    this.screenModel = screenModel;
  }

  public void setFontSelector(FontSelector selector) {
    this.fontSelector = selector;
  }
  
  public void setGridSize(int numrows, int numcols) {
    grid = new AnnotatedCharacter[numrows][numcols];
  }
  
  public int getNumRows() { return grid == null ? 0 : grid.length; }
  public int getNumColumns() {
    return grid == null || grid.length == 0 ? 0 : grid[0].length;
  }
  
  public void clear(int color) {
    // Fill the size with the background color
    TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_FIXED,
      ScreenModel.TEXTSTYLE_ROMAN, color, color);
    AnnotatedCharacter annchar = new AnnotatedCharacter(annotation, ' ');
    for (int row = 0; row < screenModel.getNumRowsUpper(); row++) {
      for (int col = 0; col < grid[row].length; col++) {
        grid[row][col] = annchar;
      }
    }
    // The rest of the lines is transparent
    for (int row = screenModel.getNumRowsUpper(); row < grid.length; row++) {
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
    }
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
      int posx = col * fontMetrics.charWidth(REF_CHAR);
      
      ColorTranslator colTranslator = ColorTranslator.getInstance();
      Color foreground = colTranslator.translate(
        annotation.getForeground(), ScreenModel.COLOR_BLUE);
      Color background = colTranslator.translate(
        annotation.getBackground(), ScreenModel.COLOR_WHITE);
      if (annotation.isReverseVideo()) {
        // swap colors
        Color tmp = foreground;
        foreground = background;
        background = tmp;
      }
      //System.out.println("Draw c: " + c.getCharacter() + " bg: " + background +
      //        " fg: " + foreground + " posx: " + posx + " posy: " + posy);
      g.setColor(background);
      g.fillRect(posx, row * fontMetrics.getHeight(),
                 fontMetrics.charWidth('0'), fontMetrics.getHeight());
      // This is the "Frotz" trick: set the foreground a little brighter,
      // "Varicella" relies on it
      g.setColor(foreground.brighter());
      g.drawString(String.valueOf(c.getCharacter()), posx, posy);
    }
  }

  public void setCharacter(int line, int column, AnnotatedCharacter c) {
    // Guarding writing out of bounds, some games do this
    //if (line < 1 || (line - 1) >= grid.length) return;
    //if (column < 1 || (column - 1) >= grid[line - 1].length) return;
    System.out.println("SET_CHAR, line: " + line + " col: " + column + " c: " +
            c.getCharacter() + " BG: " + c.getAnnotation().getBackground() +
            " FG: " + c.getAnnotation().getForeground() + " REVERSE: " +
             c.getAnnotation().isReverseVideo());
    grid[line - 1][column - 1] = c;
  }

  @Override
  public void paintComponent(Graphics g) {
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        visualizeCursorPosition(g, row, col);
      }
    }
  }
}
