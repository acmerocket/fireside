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
    public int font = ScreenModel.FONT_FIXED;
    public int textStyle = ScreenModel.TEXTSTYLE_ROMAN;
    public int cursorx = 1, cursory = 1;
    public int background = ScreenModel.COLOR_BLACK;
    public int foreground = ScreenModel.COLOR_WHITE;
    
    public int setFont(int font) {
      int previousFont = this.font;
      this.font = font;
      return previousFont;
    }
    public AnnotatedCharacter annotateCharacter(char zchar) {
      TextAnnotation annot = new TextAnnotation(font, textStyle, background,
          foreground);
      return new AnnotatedCharacter(annot, zchar);
    }

  }

  public static final int WINDOW_BOTTOM = 0;
  public static final int WINDOW_TOP    = 1;
  private int current = WINDOW_BOTTOM;
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
  
  public BufferedTextWindow getBottomWindow() { return bottomWindow; }
  public void setNumCharsPerRow(int numCharsPerRow) {
  }

  public void reset() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void splitWindow(int linesUpperWindow) {
    for (ScreenModelListener l : screenModelListeners) {
      l.screenSplit(linesUpperWindow);
    }
  }

  public void setWindow(int window) {
    System.out.println("SET_WINDOW: " + window);
    current = window;
  }

  public void setTextStyle(int style) {
    System.out.println("SET_TEXT_STYLE: " + style);
    if (current == WINDOW_TOP) {
      topWindow.textStyle = style;
    } else {
      bottomWindow.setCurrentTextStyle(style);
    }
  }

  public void setBufferMode(boolean flag) {
    System.out.println("SET_BUFFER_MODE (TODO): " + flag);
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  public void eraseLine(int value) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void eraseWindow(int window) {
    System.out.println("ERASE_WINDOW (TODO): " + window);
    for (ScreenModelListener l : screenModelListeners) {
      l.windowErased(window);
    }
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setTextCursor(int line, int column, int window) {
    //System.out.printf("SET_TEXT_CURSOR (TODO), line: %d, column: %d, " +
    //        "window: %d\n", line, column, window);
    if (window == ScreenModel.CURRENT_WINDOW) {
      setTextCursorCurrentWindow(line, column);
    } else if (window == WINDOW_TOP) {
      setTextCursorTopWindow(line, column);
    } else {
      setTextCursorBottomWindow(line, column);
    }
  }
  
  private void setTextCursorCurrentWindow(int line, int column) {
    if (current == WINDOW_BOTTOM) {
      setTextCursorBottomWindow(line, column);
    } else {
      setTextCursorTopWindow(line, column);
    }
  }
  
  private void setTextCursorBottomWindow(int line, int column) {
    throw new UnsupportedOperationException("Not supported yet.");      
  }

  private void setTextCursorTopWindow(int line, int column) {
    topWindow.cursory = line;
    topWindow.cursorx = column;
  }

  public TextCursor getTextCursor() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setPaging(boolean flag) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int setFont(int fontnumber) {
    if (current == WINDOW_TOP) {
      return topWindow.setFont(fontnumber);
    } else {
      return bottomWindow.setCurrentFont(fontnumber);
    }
  }

  public void setBackgroundColor(int colornumber, int window) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setForegroundColor(int colornumber, int window) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public OutputStream getOutputStream() {
    return this;
  }

  // OutputStream
  private boolean selected;
  
  public void print(char zchar, boolean isInput) {
    if (current == WINDOW_BOTTOM) {
      bottomWindow.printChar(zchar);
    } else if (current == WINDOW_TOP) {
      //System.out.println("PRINT: [" + zchar + "]");
      for (ScreenModelListener l : screenModelListeners) {
        l.topWindowUpdated(topWindow.cursorx, topWindow.cursory,
                           topWindow.annotateCharacter(zchar));
      }
      topWindow.cursorx++;
    }
  }
  
  public void deletePrevious(char zchar) {
    throw new UnsupportedOperationException("Not supported yet.");
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
}