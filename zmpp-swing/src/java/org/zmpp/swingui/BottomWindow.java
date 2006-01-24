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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

/**
 * This class implements the lower window of the standard Z-machine screen
 * model.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class BottomWindow extends SubWindow {

  private boolean isBuffered;
  private boolean isPaged;
  
  public BottomWindow(Component parentComponent, LineEditor editor,
      BufferedImage img) {
    
    super(parentComponent, editor, img, "BOTTOM");
    
    setBufferMode(true);
    setIsPagingEnabled(true);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBuffered() {
    
    return isBuffered;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setIsPagingEnabled(boolean flag) {
    
    this.isPaged = flag;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setBufferMode(boolean flag) {
  
    this.isBuffered = flag;
  }
    
  protected void scrollIfNeeded() {

    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    // We calulate an available height with a correction amount
    // of fm.getMaxDescent() to reserve enough scrolling space
    while (getCurrentY() > (getTop() + getHeight() - fm.getMaxDescent())) {
      
      scrollUp();
      getCursor().setLine(getCursor().getLine() - 1);
    }
  }
  
  public void resetCursorToHome() {
    
    // We calulate an available height with a correction amount
    // of fm.getMaxDescent() to reserve enough scrolling space
    getCursor().setPosition(getAvailableLines(), 1);
  }

  private int getAvailableLines() {
    
    FontMetrics fm = getGraphics().getFontMetrics();
    return (getHeight() - fm.getMaxDescent()) / fm.getHeight();
  }
  
  private void scrollUp() {
    
    Graphics g = getGraphics();
    FontMetrics fm = g.getFontMetrics();
    g.copyArea(0, getTop() + fm.getHeight(), getImage().getWidth(),
               getHeight() - fm.getHeight(), 0, -fm.getHeight());
    g.setColor(getBackground());
    g.fillRect(0, getTop() + getHeight() - fm.getHeight(),
               getImage().getWidth(), fm.getHeight());
  }

  protected void handlePaging() {
    
    if (isPaged && getLinesPrinted() >= getLinesPerPage()) {
      
      doMeMore();
    }
  }
  
  private void doMeMore() {
    
    printLine("<MORE> (Press key to continue)", getGraphics(),
        getGraphics().getFontMetrics(),
        getTextBackground(), getTextColor());
    
    // Rendering in the UI thread
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
        
          getParentComponent().repaint();
        }
      });
    } catch (Exception ex) {
      
      ex.printStackTrace();
    }
    
    // Do this exclusively to have better thread control, we need to stay
    // in the application thread
    getEditor().setInputMode(true);
    getEditor().nextZsciiChar();
    getCursor().setColumn(1);
    eraseLine();        
    getEditor().setInputMode(false);
    resetPager();
  }
}
