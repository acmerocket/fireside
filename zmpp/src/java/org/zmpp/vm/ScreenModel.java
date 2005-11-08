/*
 * $Id$
 * 
 * Created on 7.11.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.vm;

/**
 * This interface defines the access to the screen model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface ScreenModel {

  /**
   * Clears the screen.
   */
  void clear();
  
  /**
   * Splits the screen into two windows, the upper window will contain
   * linesUpperWindow lines. If linesUpperWindow is 0, the window will
   * be unsplit.
   * 
   * @param linesUpperWindow the number of lines the upper window will have
   */
  void splitWindow(int linesUpperWindow);
  
  /**
   * Sets the active window.
   * 
   * @param window the active window
   */
  void setWindow(int window);
}
