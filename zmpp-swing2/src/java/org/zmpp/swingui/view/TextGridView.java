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
package org.zmpp.swingui.view;

import org.zmpp.swingui.view.ScreenModelSplitView;
import org.zmpp.swingui.view.ColorTranslator;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
  private static final AnnotatedCharacter EMPTY_CHAR = null;
  private AnnotatedCharacter[][] grid;
  private ScreenModelSplitView parent;
  //private static final Logger LOG = Logger.getLogger("org.zmpp");
  private boolean cursorShown = false;

  /**
   * Constructor.
   * @param parent the parent view
   */
  public TextGridView(ScreenModelSplitView parent) {
    this.parent = parent;
  }
  
  /**
   * Returns a reference to the current screen model.
   * @return the current screen model
   */
  private BufferedScreenModel getScreenModel() {
    return parent.getScreenModel();
  }

  /**
   * Resize the grid, which changes the number of characters that can be
   * displayed
   * @param numrows the new number of rows
   * @param numcols the new number of colums
   */
  public void setGridSize(int numrows, int numcols) {
    grid = new AnnotatedCharacter[numrows][numcols];
  }
  
  /**
   * The number of rows this component can display.
   * @return the number of rows that can be displayed
   */
  public int getNumRows() { return grid == null ? 0 : grid.length; }
  
  /**
   * The number of columns this component can display.
   * @return the number of columns that can be displayed
   */
  public int getNumColumns() {
    return grid == null || grid.length == 0 ? 0 : grid[0].length;
  }
  
  /**
   * Clears the window by printing spaces in the specified color.
   * @param color the color as a ScreenModel constant
   */
  public void clear(int color) {
    //LOG.info("clear top window with color: " + color);
    // Fill the size with the background color
    TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_FIXED,
      ScreenModel.TEXTSTYLE_ROMAN, color, color);
    AnnotatedCharacter annchar = new AnnotatedCharacter(annotation, ' ');
    for (int row = 0; row < getScreenModel().getNumRowsUpper(); row++) {
      for (int col = 0; col < grid[row].length; col++) {
        grid[row][col] = annchar;
      }
    }
    // The rest of the lines is transparent
    for (int row = getScreenModel().getNumRowsUpper(); row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        grid[row][col] = null;
      }
    }
  }

  /**
   * Displays a character at the specified position. If that character is null,
   * it will not be drawn.
   * @param g the Graphics object
   * @param row the 0 based row
   * @param col the 0 based column
   */
  private void visualizeCursorPosition(Graphics g, int row, int col) {
    // Draw it
    AnnotatedCharacter c = grid[row][col];
    if (c != null) {
      drawCharacter(g, row, col);
    }
  }

  /**
   * Displays the character in the Swing component.
   * @param g the Graphics object
   * @param row the 0-based row
   * @param col the 0-based column
   */
  private void drawCharacter(Graphics g, int row, int col) {
    // Draw it
    AnnotatedCharacter c = grid[row][col];
    if (c != null) {
      TextAnnotation annotation = c.getAnnotation();
      Font drawfont = parent.getFont(annotation);
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
  
  /**
   * Sets the character at the specified position.
   * @param line the 1-based line
   * @param column the 1-based column
   * @param c the styled character
   */
  public void setCharacter(int line, int column, AnnotatedCharacter c) {
    /*
    if (c != null) {
    LOG.info(String.format(
      "SET_CHAR, line: %d col: %d c: '%c' BG: %d FG: %d REVERSE: %b\n",
             line, column, c.getCharacter(),
             c.getAnnotation().getBackground(),
             c.getAnnotation().getForeground(),
             c.getAnnotation().isReverseVideo()));
    }*/
    grid[line - 1][column - 1] = c;
  }

  /** {@inheritDoc} */
  @Override
  public void paintComponent(Graphics g) {
    for (int row = 0; row < grid.length; row++) {
      for (int col = 0; col < grid[row].length; col++) {
        visualizeCursorPosition(g, row, col);
      }
    }
  }
  
  /**
   * Displays or hides the cursor.
   * @param flag true to view, false to hide the cursor
   */
  public void viewCursor(boolean flag) {
    TextCursor cursor = getScreenModel().getTextCursor();
    if (flag) {
      setCharacter(cursor.getLine(), cursor.getColumn(), getCursorChar());
      cursorShown = true;
    } else {
      if (cursorShown) {
        setCharacter(cursor.getLine(), cursor.getColumn(), EMPTY_CHAR);
      }
      cursorShown = false;
    }
  }

  /**
   * Returns a character that can represent the cursor.
   * @return the cursor representation character
   */
  private AnnotatedCharacter getCursorChar() {
    return new AnnotatedCharacter(new TextAnnotation(ScreenModel.FONT_FIXED,
        ScreenModel.TEXTSTYLE_REVERSE_VIDEO, parent.getDefaultBackground(),
        parent.getDefaultForeground()), ' ');
  }
}
