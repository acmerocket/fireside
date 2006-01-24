/*
 * $Id$
 * 
 * Created on 01/23/2006
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

import java.awt.Component;
import java.awt.image.BufferedImage;

/**
 * This class implements the upper window of the standard Z-machine screen
 * model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class TopWindow extends SubWindow {

  public TopWindow(Component parentComponent, LineEditor editor,
      BufferedImage img) {
    
    super(parentComponent, editor, img, "TOP");    
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
  public void setIsPagingEnabled(boolean flag) { }
  
  /**
   * {@inheritDoc}
   */
  protected void scrollIfNeeded() { }

  /**
   * {@inheritDoc}
   */
  protected void handlePaging() { }
}
