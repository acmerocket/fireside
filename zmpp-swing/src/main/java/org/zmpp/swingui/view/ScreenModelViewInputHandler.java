/*
 * Created on 2008/05/10
 * Copyright 2005-2009 by Wei-ju Wu
 * This file is part of The Z-machine Preservation Project (ZMPP).
 
 * ZMPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with ZMPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.swingui.view;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.zmpp.ExecutionControl;
import org.zmpp.windowing.ScreenModel;

/*
 * An input handler for the standard screen model.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ScreenModelViewInputHandler
  implements KeyListener, ChangeListener {

  private static final Logger LOG = Logger.getLogger("org.zmpp.ui");
  private ScreenModelSplitView view;
  private long lastConsumed;
  /*
   * A threshold to prevent the handling of the same key event within
   * keypressed, keyreleased, keytyped. We assume the fastest user
   * can not type more than 5 characters per second.
   */
  private static final long TYPING_THRESHOLD = 200;

  /*
   * Constructor.
   * @param view the main view component
   */
  public ScreenModelViewInputHandler(ScreenModelSplitView view) {
    this.view = view;
  }

  // ************************************************************************
  // **** Keyboard input handling
  // ************************************************
  /** {@inheritDoc} */
  public void keyTyped(KeyEvent e) {
    handleKeyEvent(e);
  }
  /** {@inheritDoc} */
  public void keyPressed(KeyEvent e) {
    handleKeyEvent(e);
  }
  /** {@inheritDoc} */
  public void keyReleased(KeyEvent e) {
    handleKeyEvent(e);
  }
  
  private void handleKeyEvent(KeyEvent e) {
    if (view.getScreenModel().getActiveWindow() == ScreenModel.WINDOW_BOTTOM) {
      preventBottomWindowKeyActionIfNeeded(e);
    } else {
      //System.out.println("KEY PRESSED IN UPPER WINDOW: " + e.getKeyChar());
      if (isReadChar()) {
        resumeWithInput(String.valueOf(e.getKeyChar()));
        consumeKeyEvent(e);
        return;
      }
    }
  }

  private void consumeKeyEvent(KeyEvent e) {
    if (e != null) {
      e.consume();
      lastConsumed = e.getWhen();
    }
  }
  
  private boolean wasConsumed(KeyEvent e) {
    return (Math.abs(e.getWhen() - lastConsumed) < TYPING_THRESHOLD);
  }
  
  private void preventBottomWindowKeyActionIfNeeded(KeyEvent e) {
    // Shortcut: If character was previously consumed, don't handle it anymore
    if (wasConsumed(e)) {
      consumeKeyEvent(e);
      return;
    }
    // Shortcut: If in ReadChar mode, handle here and skip the rest
    if (isReadChar()) {
      resumeWithInput(String.valueOf(e.getKeyChar()));
      consumeKeyEvent(e);
      return;
    }
    // Handling for ReadLine mode
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      // Handle up key
      consumeKeyEvent(e);
    }
    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      // Handle down key
      consumeKeyEvent(e);
    }
    setCaretToEditMarkIfNeeded(e);
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      consumeKeyEvent(e);
      handleEnterKey();
    }
    if (atOrBeforeEditStart() &&
        e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
      consumeKeyEvent(e);
    }
  }
  
  private void setCaretToEditMarkIfNeeded(KeyEvent e) {
    if (getLowerCaretPosition() <= getEditStart() && isPrintable(e)) {
      setLowerCaretPosition(getLowerDocument().getLength());
    }
  }
  
  private boolean isPrintable(KeyEvent e) {
    return e.getKeyChar() != KeyEvent.CHAR_UNDEFINED && !isCommandDown(e);
  }
  
  private static final int APPLE_CMD = 4;
  private boolean isCommandDown(KeyEvent e) {
    int modifiers = e.getModifiers();
    boolean appleCmd = ((modifiers & APPLE_CMD) == APPLE_CMD);
    return appleCmd || e.isControlDown();
  }
  
  private boolean atOrBeforeEditStart() {
    return getLowerCaretPosition() <= getEditStart();
  }

  private void handleEnterKey() {
    if (isReadLine()) {
      Document doc = getLowerDocument();
      try {
        String input = view.getCurrentInput();
        LOG.info("ENTER PRESSED, input: [" + input + "]");
        doc.insertString(doc.getLength(), "\n", null);
        resumeWithInput(input);
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  // ************************************************************************
  // **** Caret handling
  // ************************************************
  /** {@inheritDoc} */
  public void stateChanged(ChangeEvent e) {
    /*
    if (isReadLine()) {
      if (getLowerCaretPosition() < getEditStart()) {
        setLowerCaretPosition(getEditStart());
      }
    }*/
  }
  
  // ************************************************************************
  // **** Helpers that delegate to the view
  // ************************************************
  private ExecutionControl getExecutionControl() {
    return view.getExecutionControl();
  }
  private void resumeWithInput(String input) {
    view.setCurrentRunState(getExecutionControl().resumeWithInput(input));
  }
  private int getEditStart() { return view.getEditStart(); }
  private boolean isReadChar() { return view.isReadChar(); }
  private boolean isReadLine() { return view.isReadLine(); }
  private Document getLowerDocument() { return view.getLower().getDocument(); }
  private int getLowerCaretPosition() {
    return view.getLower().getCaretPosition();
  }
  private void setLowerCaretPosition(int position) {
    view.getLower().setCaretPosition(position);
  }
}
