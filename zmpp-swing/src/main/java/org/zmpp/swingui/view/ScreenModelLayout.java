/*
 * Created on 2008/05/10
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
package org.zmpp.swingui.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.LayoutManager2;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import org.zmpp.windowing.ScreenModel;

/**
 * This layout manager manages a parent component and assumes it will contain
 * two child components: an upper and a lower window. It simply holds the
 * lower window in the split position, while keeping the upper window at the
 * top.
 * Margins are set by setting the component borders and are retrieved by
 * insets.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ScreenModelLayout implements LayoutManager2 {

  private JComponent upper,  lower;
  private int numRowsUpper;
  private FontSelector fontSelector;
  private boolean valid;

  public void setNumRowsUpper(int numrows) {
    this.numRowsUpper = numrows;
  }
  
  public void setFontSelector(FontSelector selector) {
    this.fontSelector = selector;
  }

  // ***********************************************************************
  // ***** LayoutManager
  // ***********************************
  /**
   * Does nothing.
   * @param name the name of the component association
   * @param comp the component
   */
  public void addLayoutComponent(String name, Component comp) { }
  /**
   * Does nothing, removal not supported.
   * @param comp the removed component
   */
  public void removeLayoutComponent(Component comp) { }
  /** {@inheritDoc} */
  public Dimension preferredLayoutSize(Container parent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /** {@inheritDoc} */
  public Dimension minimumLayoutSize(Container parent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /** {@inheritDoc} */
  public void layoutContainer(Container parent) {
    if (!valid) {
      int parentWidth = parent.getWidth();
      int parentHeight = parent.getHeight();
      int upperSize = getUpperSize();
      upper.setBounds(0, 0, parentWidth, parentHeight);
      lower.setBounds(0, upperSize, parentWidth, parentHeight - upperSize);
      valid = true;
    }
  }
  
  /**
   * Returnsthe size of the upper window.
   * @return the upper window size
   */
  private int getUpperSize() {
    return getUpperFontMetrics().getHeight() * numRowsUpper;
  }
  
  /**
   * Returns the FontMetrics of the upper window.
   * @return the FontMetrics of the upper window
   */
  private FontMetrics getUpperFontMetrics() {
    return upper.getFontMetrics(fontSelector.getFont(ScreenModel.FONT_FIXED,
                                                     1));
  }

  // ***********************************************************************
  // ***** LayoutManager2
  // ***********************************
  /** {@inheritDoc} */
  public void addLayoutComponent(Component comp, Object constraints) {
    Integer id = (Integer) constraints;
    if (JLayeredPane.DEFAULT_LAYER.equals(id)) {
      lower = (JComponent) comp;
    } else {
      upper = (JComponent) comp;
    }
  }
  /** {@inheritDoc} */
  public Dimension maximumLayoutSize(Container target) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /** {@inheritDoc} */
  public float getLayoutAlignmentX(Container target) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /** {@inheritDoc} */
  public float getLayoutAlignmentY(Container target) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  /** {@inheritDoc} */
  public void invalidateLayout(Container target) {
    valid = false;
  }
}
