/*
 * $Id$
 * 
 * Created on 2008/07/16
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
package org.zmpp.windowing;

/**
 * This class implements the virtual top window of the Z-machine screen model.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class TopWindow implements TextCursor {

  private int cursorx = 1,  cursory = 1;
  private int numCharsPerRow, numRows;
  private TextAnnotation annotation = new TextAnnotation(
          ScreenModel.FONT_FIXED,
          ScreenModel.TEXTSTYLE_ROMAN, ScreenModel.COLOR_BLACK,
          ScreenModel.COLOR_WHITE);

  /** Resets the text cursor position. */
  public void resetCursor() { cursorx = cursory = 1; }
  
  /**
   * Returns the current TextAnnotation used for this window.
   * @return the text annotation
   */
  public TextAnnotation getCurrentAnnotation() { return annotation; }
  
  /**
   * Sets the number of rows in this window.
   * @param numRows the number of rows
   */
  public void setNumRows(int numRows) { this.numRows = numRows; }
  
  /**
   * Returns the number of rows in this window.
   * @return the number of rows
   */
  public int getNumRows() { return numRows; }

  /**
   * Sets the new number of characters per row.
   * @param numChars the number of characters
   */
  public void setNumCharsPerRow(int numChars) {
    numCharsPerRow = numChars;
  }

  /**
   * Sets the font number for this window.
   * @param font the font number
   * @return returns the number of the previous font
   */
  public char setFont(char font) {
    char previousFont = this.annotation.getFont();
    annotation = annotation.deriveFont(font);
    return previousFont;
  }

  /**
   * Sets the current text style in this window.
   * @param style the text style
   */
  public void setCurrentTextStyle(int style) {
    annotation = annotation.deriveStyle(style);
  }

  /**
   * Sets the foreground color in this window.
   * @param color the new foreground color
   */
  public void setForeground(int color) {
    annotation = annotation.deriveForeground(color);
  }

  /**
   * Sets the new background color in this window.
   * @param color the new background color
   */
  public void setBackground(int color) {
    annotation = annotation.deriveBackground(color);
  }

  /**
   * Annotates the specified character with the current annotation.
   * @param zchar the character to annotate
   * @return the annotated character
   */
  public AnnotatedCharacter annotateCharacter(char zchar) {
    return new AnnotatedCharacter(annotation, zchar);
  }

  /**
   * Sets the new text cursor position.
   * @param line the line
   * @param column the column
   */
  public void setTextCursor(int line, int column) {
    if (outOfUpperBounds(line, column)) {
      // set to left margin of current line
      cursorx = 1;
    } else {
      this.cursorx = column;
      this.cursory = line;
    }
  }
  
  /**
   * Increments the current cursor position.
   */
  public void incrementCursorXPos() {
    this.cursorx++;
    // Make sure the cursor does not overrun the margin
    if (cursorx >= numCharsPerRow) {
      cursorx = numCharsPerRow - 1;
    }
  }
  
  public void notifyChange(ScreenModelListener l, char c) {
    l.topWindowUpdated(cursorx, cursory, annotateCharacter(c));
  }
  
  private boolean outOfUpperBounds(int line, int column) {
    if (line < 1 || line > numRows) return true;
    if (column < 1 || column > numCharsPerRow) return true;
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public int getLine() { return cursory; }

  /**
   * {@inheritDoc}
   */
  public int getColumn() { return cursorx; }

  /**
   * {@inheritDoc}
   */
  public void setLine(int line) { cursory = line; }

  /**
   * {@inheritDoc}
   */
  public void setColumn(int column) { cursorx = column; }

  /**
   * {@inheritDoc}
   */
  public void setPosition(int line, int column) {
    cursorx = column;
    cursory = line;
  }
}
