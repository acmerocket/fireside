/*
 * $Id$
 * 
 * Created on 2006/02/17
 * Copyright 2005-2006 by Wei-ju Wu
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
package org.zmpp.swingui;

/**
 * This interface reduces the dependency between cursor and window and
 * facilitates testing.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface CursorWindow {

  void updateCursorCoordinates();
  
  void backspace(char previousChar);
  
  void printString(String str);
  
  boolean isBuffered();
}
