/*
 * $Id$
 * 
 * Created on 2008/04/23
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
 * A fixed text grid.
 * @author Wei-ju Wu
 */
public class BufferedTextGrid {
  private int numRows, numCols;
  private AnnotatedCharacter[][] textGrid;
  
  public int getNumRows() { return numRows; }
  public int getNumColumns() { return numCols; }
  
  /**
   * Resize the grid.
   * @param numRows number of rows
   * @param numColumns number of columns
   */
  public void resize(int numRows, int numColumns) {
    this.numRows = numRows;
    this.numCols = numColumns;
    textGrid = new AnnotatedCharacter[numRows][numColumns];
  }
}
