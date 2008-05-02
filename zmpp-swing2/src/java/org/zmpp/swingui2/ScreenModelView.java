/*
 * $Id$
 * 
 * Created on 2008/05/01
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
package org.zmpp.swingui2;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;

/**
 * A Swing component that hosts sub views that represent the Z-machine
 * screen model's windows.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ScreenModelView extends JComponent
  implements ComponentListener {
  private TextGridView topWindow;
  private TextWindowView bottomWindow;
  private int topWindowHeight, splitGap = 5;
  //private static final Font STD_FONT = new Font("Baskerville", Font.PLAIN, 20);
  private static final Font STD_FONT = new Font("American Typewriter", Font.PLAIN, 16);
  private static final Font FIXED_FONT = new Font("Monaco", Font.PLAIN, 16);
  private FontSelector fontSelector = new FontSelector();

  public ScreenModelView() {
    setPreferredSize(new Dimension(640, 480));
    setDefaultFonts();
    addComponentListener(this);
  }
  
  private void setDefaultFonts() {
    /*
    String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
    for (String fontName : fontNames) {
      System.out.println("Font name: " + fontName);
    }*/
    fontSelector.setFixedFont(FIXED_FONT);
    fontSelector.setStandardFont(STD_FONT);    
  }
  
  private void initWindows() {
    if (topWindow == null && bottomWindow == null) {
      topWindow = new TextGridView(this, fontSelector);
      bottomWindow = new TextWindowView(this, fontSelector);
      setSplit(30);
    }
  }
  
  private void setSplit(int topWindowHeight) {
    this.topWindowHeight = topWindowHeight;
    topWindow.setBounds(0, 0, getWidth(), topWindowHeight);
    int upperHeight = topWindowHeight + splitGap;
    bottomWindow.setBounds(0, upperHeight, getWidth(),
                           getHeight() - upperHeight);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    topWindow.paint(g2d);
    bottomWindow.paint(g2d);
  }

  // ************************************************************************
  // ***** ComponentListener interface
  // ************************************************************************
  
  /**
   * Invalidate sub views and force them to redo their layout on a resize.
   * @param e ComponentEvent.
   */
  public void componentResized(ComponentEvent e) {
    initWindows();
    topWindow.invalidate();
    bottomWindow.invalidate();
    setSplit(topWindowHeight);
  }

  /** {@inheritDoc} */
  public void componentMoved(ComponentEvent e) { }

  /** {@inheritDoc} */
  public void componentShown(ComponentEvent e) { }

  /** {@inheritDoc} */
  public void componentHidden(ComponentEvent e) { }    
}
