/*
 * $Id$
 * 
 * Created on 2008/04/23
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
package org.zmpp.zscreen;

import org.zmpp.windowing.BufferedTextWindow;
import java.util.ArrayList;
import java.util.List;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;
import org.zmpp.vm.TextCursor;
import org.zmpp.windowing.AnnotatedCharacter;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.windowing.TextAnnotation;

/**
 * BufferedScreenModel is the attempt to provide a reusable screen model
 * that will be part of the core in later versions. It is mainly a
 * configurable virtual window management model, providing virtual windows
 * that the machine writes to. It is intended to provide interfaces to
 * both Glk and Z-machine and to combine the abilities of both.
 * 
 * @author Wei-ju Wu
 */
public class BufferedScreenModel implements ScreenModel, StatusLine,
  OutputStream {
  
  class TopWindow {
    public int cursorx = 1, cursory = 1;
    TextAnnotation annotation = new TextAnnotation(ScreenModel.FONT_FIXED,
            ScreenModel.TEXTSTYLE_ROMAN, ScreenModel.COLOR_BLACK,
            ScreenModel.COLOR_WHITE);
    
    public void resetCursor() { cursorx = cursory = 1; }
    public int setFont(int font) {
      int previousFont = this.annotation.getFont();
      annotation = annotation.deriveFont(font);
      return previousFont;
    }
    public AnnotatedCharacter annotateCharacter(char zchar) {
      return new AnnotatedCharacter(annotation, zchar);
    }
  }

  private int current = WINDOW_BOTTOM;
  private int numRowsUpper, numCharsPerRow;
  private BufferedTextWindow bottomWindow = new BufferedTextWindow();
  private TopWindow topWindow = new TopWindow();
  private List<ScreenModelListener> screenModelListeners =
    new ArrayList<ScreenModelListener>();
  private List<StatusLineListener> statusLineListeners =
    new ArrayList<StatusLineListener>();
  
  public interface ScreenModelListener {
    void screenModelUpdated(BufferedScreenModel screenModel);
    void topWindowUpdated(int cursorx, int cursory, AnnotatedCharacter c);
    void screenSplit(int linesUpperWindow);
    void windowErased(int window);
  }
  
  public interface StatusLineListener {
    void statusLineUpdated(String objectDescription, String status);
  }
  
  public void addScreenModelListener(ScreenModelListener l) {
    screenModelListeners.add(l);
  }
  
  public void addStatusLineListener(StatusLineListener l) {
    statusLineListeners.add(l);
  }
  
  public void setNumCharsPerRow(int num) {
    numCharsPerRow = num;
  }
  
  public void reset() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void splitWindow(int linesUpperWindow) {
    System.out.println("SPLIT_WINDOW: " + linesUpperWindow);
    numRowsUpper = linesUpperWindow;
    for (ScreenModelListener l : screenModelListeners) {
      l.screenSplit(linesUpperWindow);
    }
  }

  public void setWindow(int window) {
    System.out.println("SET_WINDOW: " + window);
    current = window;
    if (current == ScreenModel.WINDOW_TOP) {
      topWindow.resetCursor();
    }
  }

  public void setTextStyle(int style) {
    System.out.println("SET_TEXT_STYLE: " + style);
    topWindow.annotation = topWindow.annotation.deriveStyle(style);
    bottomWindow.setCurrentTextStyle(style);
  }

  public void setBufferMode(boolean flag) {
    System.out.println("SET_BUFFER_MODE (ignored): " + flag);
    // Simply ignored, top window is always unbuffered, bottom window always
    // buffered
  }

  public void eraseLine(int value) {
    System.out.println("ERASE_LINE: " + value);
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void eraseWindow(int window) {
    System.out.println("ERASE_WINDOW: " + window);
    for (ScreenModelListener l : screenModelListeners) {
      l.windowErased(window);
    }
    if (window == -1) {
      splitWindow(0);
      setWindow(ScreenModel.WINDOW_BOTTOM);
      topWindow.resetCursor();
    }
    if (window == ScreenModel.WINDOW_TOP) {
      for (ScreenModelListener l : screenModelListeners) {
        l.windowErased(ScreenModel.WINDOW_TOP);
      }
      topWindow.resetCursor();
    }
  }
  
  public void setTextCursor(int line, int column, int window) {
    int targetWindow = getTargetWindow(window);
    System.out.printf("SET_TEXT_CURSOR, line: %d, column: %d, " +
            "window: %d\n", line, column, targetWindow);
    if (targetWindow == WINDOW_TOP) {
      if (outOfUpperBounds(line, column)) {
        // set to left margin of current line
        topWindow.cursorx = 1;
      } else {
        setTextCursorTopWindow(line, column);
      }
    }
  }
  
  private boolean outOfUpperBounds(int line, int column) {
    if (line < 1 || line > numRowsUpper) return true;
    if (column < 1 || column > numCharsPerRow) return true;
    return false;
  }
  
  private int getTargetWindow(int window) {
    return window == ScreenModel.CURRENT_WINDOW ? current : window;
  }
  
  private void setTextCursorTopWindow(int line, int column) {
    topWindow.cursory = line;
    topWindow.cursorx = column;
  }

  public TextCursor getTextCursor() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  public int setFont(int fontnumber) {
    System.out.println("SET_FONT: " + fontnumber);
    if (current == WINDOW_TOP) {
      return topWindow.setFont(fontnumber);
    } else {
      return bottomWindow.setCurrentFont(fontnumber);
    }
  }

  public void setBackground(int colornumber, int window) {
    System.out.println("setBackground, color: " + colornumber);
    topWindow.annotation = topWindow.annotation.deriveBackground(colornumber);
    bottomWindow.setBackground(colornumber);
  }

  public void setForeground(int colornumber, int window) {
    System.out.println("setForeground, color: " + colornumber);
    topWindow.annotation = topWindow.annotation.deriveForeground(colornumber);
    bottomWindow.setForeground(colornumber);
  }

  public OutputStream getOutputStream() { return this; }

  // OutputStream
  private boolean selected;
  
  public void print(char zchar) {
    if (current == WINDOW_BOTTOM) {
      bottomWindow.printChar(zchar);
    } else if (current == WINDOW_TOP) {
      //System.out.println("PRINT: [" + zchar + "]");
      for (ScreenModelListener l : screenModelListeners) {
        l.topWindowUpdated(topWindow.cursorx, topWindow.cursory,
                           topWindow.annotateCharacter(zchar));
      }
      topWindow.cursorx++;
      // Make sure the cursor does not overrun the margin
      if (topWindow.cursorx >= numCharsPerRow) {
        topWindow.cursorx = numCharsPerRow - 1;
      }
    }
  }
  
  public void close() { }

  /**
   * Notify listeners that.
   */
  public void flush() {
    for (ScreenModelListener l : screenModelListeners) {
      l.screenModelUpdated(this);
    }
  }

  public void select(boolean flag) {
    selected = flag;
  }

  public boolean isSelected() {
    return selected;
  }

  // ***********************************************************************
  // ***** StatusLine implementation
  // ***************************************
  public void updateStatusScore(String objectName, int score, int steps) {
    for (StatusLineListener l : statusLineListeners) {
      l.statusLineUpdated(objectName, score + "/" + steps);
    }
  }

  public void updateStatusTime(String objectName, int hours, int minutes) {
    for (StatusLineListener l : statusLineListeners) {
      l.statusLineUpdated(objectName, hours + ":" + minutes);
    }
  }

  // ***********************************************************************
  // ***** Additional public interface
  // ***************************************

  public int getNumRowsUpper() {
    return numRowsUpper;
  }
  
  public int getBackground() { return bottomWindow.getBackground(); }
  public int getForeground() { return bottomWindow.getForeground(); }
  
  public List<AnnotatedText> getLowerBuffer() {
    return bottomWindow.getBuffer();
  }
}