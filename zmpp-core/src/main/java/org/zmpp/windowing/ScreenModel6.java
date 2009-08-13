/*
 * Created on 2006/02/22
 * Copyright 2005-2009 by Wei-ju Wu
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

import org.zmpp.media.DrawingArea;

/**
 * Screen model 6 interface.
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface ScreenModel6 extends ScreenModel, DrawingArea {

  /**
   * Restricts the mouse pointer to the specified window.
   *
   * @param window the window
   */
  void setMouseWindow(int window);

  /**
   * Returns the specified window.
   *
   * @param window the window
   * @return the window
   */
  Window6 getWindow(int window);

  /**
   * Returns the currently selected window.
   *
   * @return the currently selected window
   */
  Window6 getSelectedWindow();

  /**
   * Instructs the screen model to set the width of the current string
   * to the header.
   *
   * @param zchars the z character array
   */
  void setTextWidthInUnits(char[] zchars);

  /**
   * Reads the current mouse data into the specified array.
   *
   * @param array the array address
   */
  void readMouse(int array);
}
