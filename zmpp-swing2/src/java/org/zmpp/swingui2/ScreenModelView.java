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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import org.zmpp.swingui2.ScreenModelSplitView.MainViewListener;

/**
 * A Swing component that hosts sub views that represent the Z-machine
 * screen model's windows.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ScreenModelView extends JComponent
implements AdjustmentListener, MainViewListener, MouseWheelListener {

  private ScreenModelSplitView mainView = new ScreenModelSplitView();
  private JScrollBar scrollbar;
  
  public ScreenModelView() {
    setLayout(new BorderLayout());
    mainView.setPreferredSize(new Dimension(640, 480));
    mainView.split(5);
    add(mainView, BorderLayout.CENTER);
    scrollbar = new JScrollBar();
    scrollbar.addAdjustmentListener(this);
    scrollbar.addMouseWheelListener(this);
    mainView.addMouseWheelListener(this);
    add(scrollbar, BorderLayout.EAST);
    mainView.addMainViewListener(this);
  }
  
  /**
   * Called when the scrollbar has changed in some way.
   * @param viewHeight the new view height
   * @param viewportHeight the new viewport height
   * @param currentPos the current position of the view
   */
  public void viewDimensionsChanged(int viewHeight, int viewportHeight,
          int currentViewPos) {
    scrollbar.setMinimum(0);
    scrollbar.setMaximum(viewHeight);
    scrollbar.setValue(mapViewPosToScrollPos(currentViewPos));
    scrollbar.setVisibleAmount(viewportHeight);
  }

  /**
   * Called when the scrollbar was moved.
   * @param e the AdjustmentEvent
   */
  public void adjustmentValueChanged(AdjustmentEvent e) {
    if (e.getValueIsAdjusting()) {
      scrollToScrollbarPos();
    }
  }
  
  /**
   * Maps a scroll bar position to a view position
   * @param scrollPos the scroll position
   * @return the view position
   */
  private int mapScrollPosToViewPos(int scrollPos) { return -scrollPos; }
  
  /**
   * Maps a view position to the scroll bar's position.
   * @param viewPos the view position
   * @return the position of the scroll bar
   */
  private int mapViewPosToScrollPos(int viewPos) { return -viewPos; }

  /**
   * 
   * @param e
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    int units = e.getUnitsToScroll();
    scrollbar.setValue(scrollbar.getValue() + units);
    scrollToScrollbarPos();
  }
  
  private void scrollToScrollbarPos() {
    mainView.scroll(mapScrollPosToViewPos(scrollbar.getValue()));    
  }
}
