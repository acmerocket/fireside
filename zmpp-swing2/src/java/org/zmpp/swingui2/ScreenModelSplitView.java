/*
 * $Id$
 * 
 * Created on 2008/05/10
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.zmpp.ExecutionControl;
import org.zmpp.vm.MachineRunState;
import org.zmpp.windowing.AnnotatedCharacter;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.windowing.TextAnnotation;
import org.zmpp.vm.BufferedScreenModel;
import org.zmpp.vm.Instruction;
import org.zmpp.windowing.ScreenModel;
import org.zmpp.windowing.ScreenModelListener;

/**
 * The MainView class is the main view component. It contains the upper and
 * the lower windows.
 * While the lower window is layed out so that its boundaries start
 * at the split position, the upper window component always uses up the
 * whole available space. The upper window in in fact an overlay over the
 * lower window, which is controlled by implementing the MainView as a
 * JLayeredPane.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ScreenModelSplitView extends JLayeredPane
implements ScreenModelListener {

  private static final Logger LOG = Logger.getLogger("org.zmpp");
  //private static final Font STD_FONT = new Font("Baskerville", Font.PLAIN, 16);
  private static final Font STD_FONT = new Font("American Typewriter", Font.PLAIN, 12);
  private static final Font FIXED_FONT = new Font("Monaco", Font.PLAIN, 12);
  static final int DEFAULT_FOREGROUND = ScreenModel.COLOR_WHITE;
  static final int DEFAULT_BACKGROUND = ScreenModel.COLOR_BLUE;
  private int editStart;
  private ExecutionControl executionControl;
  private BufferedScreenModel screenModel;
  private MachineRunState currentRunState;

  public interface MainViewListener {
    /**
     * The view's dimensions or position have changed.
     * @param viewHeight the current view height
     * @param viewportHeight the current viewport height
     * @param currentViewPos the current view position
     */
    void viewDimensionsChanged(int viewHeight, int viewportHeight,
                               int currentViewPos);
  }

  private TextWindowView lower = new TextWindowView(this);
  private JViewport lowerViewport;
  private TextGridView upper = new TextGridView();
  private MainViewListener listener;
  private ScreenModelLayout layout = new ScreenModelLayout();
  private FontSelector fontSelector = new FontSelector();

  /**
   * Constructor.
   */
  public ScreenModelSplitView() {
    initLayout();
    createUpperView();
    createLowerView();
    split(0);
  }
  
  public int getNumUpperRows() { return upper.getNumRows(); }
  
  // ************************************************************************
  // **** User interface setup
  // ************************************
  
  private void initLayout() {
    setOpaque(true);
    setPreferredSize(new Dimension(640, 480));
    fontSelector.setFixedFont(FIXED_FONT);
    fontSelector.setStandardFont(STD_FONT);    
    layout.setFontSelector(fontSelector);
    setLayout(layout);    
  }

  private void createUpperView() {
    upper.setFontSelector(fontSelector);
    Border upperBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    upper.setBorder(upperBorder);
    add(upper, JLayeredPane.PALETTE_LAYER);
  }
  
  private void createLowerView() {
    Border lowerBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(5, 5, 5, 5));
    lower.setEditable(true);
    lower.setEnabled(true);
    lower.setBackground(getBackgroundColor(DEFAULT_BACKGROUND));
    lower.setForeground(getForegroundColor(DEFAULT_FOREGROUND));
    lowerViewport = new JViewport();
    lowerViewport.setView(lower);
    lowerViewport.addChangeListener(new ChangeListener() {

      /**
       * Called when view size changes.
       * @param e the change event
       */
      public void stateChanged(ChangeEvent e) {
        viewSizeChanged();
      }
    });
    lower.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentMoved(ComponentEvent e) {
        System.out.println("moved, pos: " + lower.getLocation().y);
        viewSizeChanged();
      }
    });
    lower.setBorder(lowerBorder);
    add(lowerViewport, JLayeredPane.DEFAULT_LAYER);
    installLowerHandlers();
  }

  private void installLowerHandlers() {
    // TODO: Mouse clicks can as well influence typing position
    // Make sure mouse clicks do not change caret position
    ScreenModelViewInputHandler inputHandler =
      new ScreenModelViewInputHandler(this);
    lower.addKeyListener(inputHandler);

    // In order to influence the caret position on a mouse click, we need to
    // override caret behaviour
    lower.getCaret().addChangeListener(inputHandler);
  }
  
  private Color getBackgroundColor(int screenModelColor) {
    return executionControl != null ?
      ColorTranslator.getInstance().translate(screenModelColor,
        executionControl.getDefaultBackground()) :
      ColorTranslator.getInstance().translate(screenModelColor);
  }
  
  private Color getForegroundColor(int screenModelColor) {
    return executionControl != null ?
      ColorTranslator.getInstance().translate(screenModelColor,
        executionControl.getDefaultForeground()) :
      ColorTranslator.getInstance().translate(screenModelColor);
  }
  
  // ***********************************************************************
  // **** Protected interface
  // *********************************

  TextWindowView getLower() { return lower; }
  int getEditStart() { return editStart; }
  boolean isReadChar() {
    return currentRunState == null ? false : currentRunState.isReadChar();
  }
  boolean isReadLine() {
    return currentRunState == null ? false : currentRunState.isReadLine();
  }
  ExecutionControl getExecutionControl() { return executionControl; }
  
  // ***********************************************************************
  // **** Public interface
  // *********************************
  private Timer currentTimer;
  
  private void stopCurrentTimer() {
    if (currentTimer != null) {
      currentTimer.stop();
      currentTimer = null;
    }
  }
  
  private void startNewInterruptTimer(final MachineRunState runState) {
    currentTimer = new Timer(runState.getTime() * 100,
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // output should be echoed in the interrupt, so set buffer mode to
          //false
          screenModel.setBufferMode(false);
          char result =
            executionControl.callInterrupt(runState.getRoutine());
          if (result == Instruction.TRUE) {
            currentTimer.stop();
            // TODO: Clear input and print
            //executionControl.resumeWithInput("\u0000");
            pressEnterKey();
          } else if (result == Instruction.FALSE) {
          }
          screenModel.setBufferMode(true);
        }
      });
    currentTimer.start();
  }
  
  private void pressEnterKey() {
    KeyEvent enterKeyEvent = new KeyEvent(lower, 1,
    System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) 0);
    for (KeyListener l : lower.getKeyListeners()) {
      l.keyReleased(enterKeyEvent);
    }
  }
  
  public void setCurrentRunState(final MachineRunState runState) {
    stopCurrentTimer();
    if (runState.getRoutine() > 0) {
      LOG.info("readchar: " + runState.isReadChar() + " time: " +
               runState.getTime() + " routine: " + runState.getRoutine());
      startNewInterruptTimer(runState);
    }
    currentRunState = runState;
    viewCursor(runState.isWaitingForInput());
  }
  
  public void initUI(BufferedScreenModel screenModel,
                     ExecutionControl control) {
    executionControl = control;
    executionControl.setDefaultColors(DEFAULT_BACKGROUND,
                                      DEFAULT_FOREGROUND);
    this.screenModel = screenModel;
    upper.setScreenModel(screenModel);
    screenModel.addScreenModelListener(this);
    setSizes();
    lower.setCurrentStyle(screenModel.getBottomAnnotation());
  }
  
  private void setSizes() {
    int componentWidth = getWidth();
    int componentHeight = getHeight();
    int charWidth = getFixedFontWidth();
    int charHeight = getFixedFontHeight();
    int numCharsPerRow = componentWidth / charWidth;
    int numRows = componentHeight / charHeight;
    screenModel.setNumCharsPerRow(numCharsPerRow);
    LOG.info("Char width: " + charWidth + " component width: " +
             componentWidth + " # chars/row: " + numCharsPerRow +
             " char height: " + charHeight + " # rows: " + numRows);
    upper.setGridSize(numRows, numCharsPerRow);
    executionControl.resizeScreen(numRows, numCharsPerRow);
  }

  public void addMainViewListener(MainViewListener l) {
    listener = l;
  }
  
  @Override
  public void addMouseWheelListener(MouseWheelListener l) {
    lower.addMouseWheelListener(l);
  }

  public void scroll(int value) {
    lower.setLocation(0, value);
    validate();
    repaint();
  }
  
  private void split(int numRowsUpper) {
    layout.setNumRowsUpper(numRowsUpper);
    // clear upper screen only in version 3
    if (executionControl != null && executionControl.getVersion() == 3 &&
        upper != null && screenModel != null) {
      clearUpper();
    }
  }

  private void viewSizeChanged() {
    listener.viewDimensionsChanged(lower.getHeight(), lowerViewport.getHeight(),
                                lower.getY());
  }
  
  public int getFixedFontWidth() {
    return upper.getGraphics().getFontMetrics(
      getRomanFixedFont()).charWidth('0');
  }
  
  public int getFixedFontHeight() {
    return upper.getGraphics().getFontMetrics(
      getRomanFixedFont()).getHeight();
  }
  
  protected Font getRomanFixedFont() {
    return fontSelector.getFont(ScreenModel.FONT_FIXED,
                                ScreenModel.TEXTSTYLE_ROMAN);
  }
  
  protected Font getRomanStdFont() {
    return fontSelector.getFont(ScreenModel.FONT_NORMAL,
                                ScreenModel.TEXTSTYLE_ROMAN);
  }
  
  protected Font getFont(TextAnnotation annotation) {
    return fontSelector.getFont(annotation);
  }

  // *************************************************************************
  // ****** ScreenModelListener
  // ***************************************
  public void screenModelUpdated(ScreenModel screenModel) {
    List<AnnotatedText> text = this.screenModel.getLowerBuffer();
    for (AnnotatedText segment : text) {
      lower.append(segment);
    }
    // flush and set styles
    lower.setCurrentStyle(screenModel.getBottomAnnotation());
    //upper.setCurrentStyle(screenModel.getBottomAnnotation());
  }
  
  public void topWindowUpdated(int cursorx, int cursory, AnnotatedCharacter c) {
    upper.setCharacter(cursory, cursorx, c);
    repaint();
  }

  public void screenSplit(int linesUpperWindow) {
    split(linesUpperWindow);
  }

  public void windowErased(int window) {
    if (window == -1) {
      clearAll();
    } else if (window == ScreenModel.WINDOW_BOTTOM) {
      lower.clear(screenModel.getBackground(), screenModel.getForeground());
    } else if (window == ScreenModel.WINDOW_TOP) {
      clearUpper();
    } else {
      throw new UnsupportedOperationException("No support for erasing window: " + window);
    }
  }
  
  private void clearUpper() {
    upper.clear(screenModel.getBackground());
  }

  private void clearAll() {
    lower.clear(screenModel.getBackground(), screenModel.getForeground());
    clearUpper();
  }
  
  // *************************************************************************
  // ****** Game control
  // ***************************************

  private void viewCursor(final boolean flag) {
    runInUIThread(new Runnable() {
      public void run() {
        if (flag) {
          editStart = lower.getDocument().getLength();
          lower.setCaretPosition(editStart);
          lower.requestFocusInWindow();
        } else {
          // might set caret to invisible
        }
      }
    });
  }

  /**
   * A little more readable to execute Runnable within UI thread.
   * @param runnable the Runnable
   */
  private void runInUIThread(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}

