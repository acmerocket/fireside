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
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.zmpp.windowing.AnnotatedCharacter;
import org.zmpp.windowing.BufferedScreenModel;
import org.zmpp.windowing.TextAnnotation;
import org.zmpp.windowing.ScreenModel;
import org.zmpp.windowing.TextCursor;

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
  private ScreenModelSplitView parent;
  private static final Logger LOG = Logger.getLogger("org.zmpp");

  public TextGridView(ScreenModelSplitView parent) {
    this.parent = parent;
  }
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
    LOG.info("clear top window with color: " + color);
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
      Color foreground = colTranslator.translate(annotation.getForeground(),
              parent.getDefaultForeground());
      Color background = colTranslator.translate(annotation.getBackground(),
              parent.getDefaultBackground());
      if (annotation.isReverseVideo()) {
        // swap colors
        Color tmp = foreground;
        foreground = background;
        background = tmp;
      }
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
    if (c != null) {
    LOG.info(String.format(
      "SET_CHAR, line: %d col: %d c: '%c' BG: %d FG: %d REVERSE: %b\n",
             line, column, c.getCharacter(),
             c.getAnnotation().getBackground(),
             c.getAnnotation().getForeground(),
             c.getAnnotation().isReverseVideo()));
    }
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
  
  public void viewCursor(boolean flag) {
    TextCursor cursor = screenModel.getTextCursor();
    if (flag) {
      TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_FIXED,
        ScreenModel.TEXTSTYLE_REVERSE_VIDEO, parent.getDefaultBackground(),
        parent.getDefaultForeground());
      setCharacter(cursor.getLine(), cursor.getColumn(), new AnnotatedCharacter(annotation, ' '));
      //System.out.printf("Display cursor in upper window line: %d col: %d\n",
      //        cursor.getLine(), cursor.getColumn());
      
    } else {
      System.out.printf("Hide cursor in upper window line: %d col: %d\n",
              cursor.getLine(), cursor.getColumn());
      setCharacter(cursor.getLine(), cursor.getColumn(), null);
    }
  }
}
