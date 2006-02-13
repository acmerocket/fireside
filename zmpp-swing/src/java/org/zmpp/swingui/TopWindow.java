/*
 * $Id$
 * 
 * Created on 2006/01/23
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

import org.zmpp.vm.ScreenModel;

/**
 * This class implements the upper window of the standard Z-machine screen
 * model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TopWindow extends SubWindow {

  /**
   * Constructor.
   * 
   * @param screen the screen model
   * @param editor the line editor
   * @param canvas the canvas to draw to
   */
  public TopWindow(ScreenModel screen, LineEditor editor, Canvas canvas) {
    
    super(screen, editor, canvas, "TOP");    
  }
  
  /**
   * {@inheritDoc}
   */
  public void resetCursorToHome() {
    
    getCursor().setPosition(1, 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isBuffered() {
    
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setBufferMode(boolean flag) { }
  
  /**
   * {@inheritDoc}
   */
  public void setPagingEnabled(boolean flag) { }
  
  /**
   * {@inheritDoc}
   */
  public boolean isPagingEnabled() { return false; }
  
  /**
   * {@inheritDoc}
   */
  public void resetPager() { }
  
  /**
   * {@inheritDoc}
   */
  protected void scrollIfNeeded() { }

  /**
   * {@inheritDoc}
   */
  protected void sizeUpdated() { }

  /**
   * {@inheritDoc}
   */
  protected int getCurrentX() {
    
    int meanCharWidth = getCanvas().getCharWidth(getFont(), '0');      
    return (getCursor().getColumn() - 1) * meanCharWidth;
  }
    
}
