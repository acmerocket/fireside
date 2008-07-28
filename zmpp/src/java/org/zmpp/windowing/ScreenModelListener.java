/*
 * $Id$
 * 
 * Created on 2008/07/28
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
 * The ScreenModelListener interface.
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface ScreenModelListener {
  /**
   * Notifies the listener that the screen model was updated.
   * @param screenModel the updated screen model
   */
  void screenModelUpdated(ScreenModel screenModel);

  /**
   * 
   * @param cursorx
   * @param cursory
   * @param c
   */
  void topWindowUpdated(int cursorx, int cursory, AnnotatedCharacter c);
  void screenSplit(int linesUpperWindow);
  void windowErased(int window);
}
  
