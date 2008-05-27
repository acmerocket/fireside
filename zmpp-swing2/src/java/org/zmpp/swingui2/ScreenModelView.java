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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import org.zmpp.blorb.NativeImage;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.swingui2.ScreenModelSplitView.MainViewListener;
import org.zmpp.vm.ExecutionControl;
import org.zmpp.vm.InvalidStoryException;
import org.zmpp.vm.MachineRunState;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.zscreen.BufferedScreenModel;
import org.zmpp.zscreen.BufferedScreenModel.StatusLineListener;

/**
 * A Swing component that hosts sub views that represent the Z-machine
 * screen model's windows. This component delegates a majority of the
 * functionality to its main view, which is a ScreenModelSplitView and
 * mainly deals with displaying and managing a scroll bar and a status
 * line.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ScreenModelView extends JComponent
implements AdjustmentListener, MainViewListener, MouseWheelListener,
           StatusLineListener {

  private ScreenModelSplitView mainView = new ScreenModelSplitView();
  private BufferedScreenModel screenModel = new BufferedScreenModel();
  private JScrollBar scrollbar;
  private ExecutionControl executionControl;
  private JPanel statusPanel;
  private JLabel objectDescLabel = new JLabel(" "),
                 statusLabel = new JLabel(" ");  
  
  public ScreenModelView() {
    setLayout(new BorderLayout());
    mainView.setPreferredSize(new Dimension(640, 480));
    add(mainView, BorderLayout.CENTER);
    scrollbar = new JScrollBar();
    scrollbar.addAdjustmentListener(this);
    scrollbar.addMouseWheelListener(this);
    mainView.addMouseWheelListener(this);
    add(scrollbar, BorderLayout.EAST);
    mainView.addMainViewListener(this);
    
    screenModel.addStatusLineListener(this);
    add(createStatusPanel(), BorderLayout.NORTH);
  }
  
  private JPanel createStatusPanel() {
    statusPanel = new JPanel(new GridLayout(1, 2));
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    statusPanel.add(leftPanel);
    statusPanel.add(rightPanel);
    leftPanel.add(objectDescLabel);
    rightPanel.add(statusLabel);
    statusPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    return statusPanel;
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
  
  // *************************************************************************
  // ****** StatusLineListener
  // ***************************************
  public void statusLineUpdated(String objectDescription, String status) {
    objectDescLabel.setText(objectDescription);
    statusLabel.setText(status);
  }

  // *************************************************************************
  // ****** Game controls
  // ***************************************
  /**
   * {@inheritDoc}
   */
  public void startGame(File storyFile)
      throws IOException, InvalidStoryException {
    MachineInitStruct initStruct = new MachineInitStruct();
    if (storyFile.getName().endsWith("zblorb")) {
      initStruct.blorbFile = storyFile;
    } else {
      initStruct.storyFile = storyFile;
    }
    // just for debugging
    initStruct.nativeImageFactory = new NativeImageFactory() {
      public NativeImage createImage(InputStream inputStream) throws IOException {
        return new NativeImage() {
          public int getWidth() { return 0; }
          public int getHeight() { return 0; }        
        };
      }
    };
    initStruct.screenModel = screenModel;
    initStruct.statusLine = screenModel;
    
    if (this.isVisible()) {
      executionControl = new ExecutionControl(initStruct);
      initUI();
      MachineRunState runState = executionControl.run();
      System.out.println("PAUSING WITH STATE: " + runState);
      mainView.switchModeOnRunState(runState);
    }
  }
  
  private void initUI() {
    int version = executionControl.getVersion();
    //System.out.println("initUI, story file version: " + version);
    statusPanel.setVisible(version <= 3);
    mainView.initUI(screenModel, executionControl);
  }
}
