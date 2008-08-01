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
package org.zmpp.windowing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.zmpp.base.Memory;
import org.zmpp.base.StoryFileHeader;
import org.zmpp.encoding.IZsciiEncoding;
import org.zmpp.io.OutputStream;

/**
 * BufferedScreenModel is the attempt to provide a reusable screen model
 * that will be part of the core in later versions. It is mainly a
 * configurable virtual window management model, providing virtual windows
 * that the machine writes to. It is intended to provide interfaces to
 * both Glk and Z-machine and to combine the abilities of both.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class BufferedScreenModel implements ScreenModel, StatusLine,
  OutputStream {
  private static final Logger LOG = Logger.getLogger("org.zmpp.screen");

  private int current = WINDOW_BOTTOM;
  private BufferedTextWindow bottomWindow = new BufferedTextWindow();
  private TopWindow topWindow = new TopWindow();
  private List<ScreenModelListener> screenModelListeners =
    new ArrayList<ScreenModelListener>();
  private List<StatusLineListener> statusLineListeners =
    new ArrayList<StatusLineListener>();
  private IZsciiEncoding encoding;
  private Memory memory;
  
  public interface StatusLineListener {
    void statusLineUpdated(String objectDescription, String status);
  }
  
  public void addScreenModelListener(ScreenModelListener l) {
    screenModelListeners.add(l);
  }
  
  public void addStatusLineListener(StatusLineListener l) {
    statusLineListeners.add(l);
  }
  
  /**
   * Initialize the model, an Encoding object is needed to retrieve
   * Unicode characters.
   * @param memory a Memory object
   * @param encoding the ZsciiEncoding object
   */
  public void init(Memory memory, IZsciiEncoding encoding) {
    this.memory = memory;
    this.encoding = encoding;
  }
  
  /** {@inheritDoc} */
  public TextAnnotation getTopAnnotation() {
    return topWindow.getCurrentAnnotation();
  }
  /** {@inheritDoc} */
  public TextAnnotation getBottomAnnotation() {
    return bottomWindow.getCurrentAnnotation();
  }

  /**
   * Sets the number of charactes per row, should be called if the size of
   * the output area or the size of the font changes.
   * @param num the number of characters in a row
   */
  public void setNumCharsPerRow(int num) {
    topWindow.setNumCharsPerRow(num);
  }
  
  public void reset() {
    topWindow.resetCursor();
    bottomWindow.reset();
    current = WINDOW_BOTTOM;
  }

  public void splitWindow(int linesUpperWindow) {
    LOG.info("SPLIT_WINDOW: " + linesUpperWindow);
    topWindow.setNumRows(linesUpperWindow);
    for (ScreenModelListener l : screenModelListeners) {
      l.screenSplit(linesUpperWindow);
    }
  }
  /** {@inheritDoc} */
  public void setWindow(int window) {
    LOG.info("SET_WINDOW: " + window);
    current = window;
    if (current == ScreenModel.WINDOW_TOP) {
      topWindow.resetCursor();
    }
  }
  /** {@inheritDoc} */
  public int getActiveWindow() { return current; }

  public void setTextStyle(int style) {
    LOG.info("SET_TEXT_STYLE: " + style);
    topWindow.setCurrentTextStyle(style);
    bottomWindow.setCurrentTextStyle(style);
  }

  public void setBufferMode(boolean flag) {
    LOG.info("SET_BUFFER_MODE: " + flag);
    if (current == ScreenModel.WINDOW_BOTTOM) {
      bottomWindow.setBuffered(flag);
    }
  }

  public void eraseLine(int value) {
    LOG.info("ERASE_LINE: " + value);
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void eraseWindow(int window) {
    LOG.info("ERASE_WINDOW: " + window);
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
    //LOG.info(String.format("SET_TEXT_CURSOR, line: %d, column: %d, " +
    //                       "window: %d\n", line, column, targetWindow));
    if (targetWindow == WINDOW_TOP) {
      for (ScreenModelListener l : screenModelListeners) {
        l.topWindowCursorMoving(line, column);
      }
      topWindow.setTextCursor(line, column);
    }
  }
  
  private int getTargetWindow(int window) {
    return window == ScreenModel.CURRENT_WINDOW ? current : window;
  }
  
  public TextCursor getTextCursor() {
    if (this.current != ScreenModel.WINDOW_TOP) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    return topWindow;
  }
  
  public char setFont(char fontnumber) {
    if (fontnumber != ScreenModel.FONT_FIXED &&
        fontnumber != ScreenModel.FONT_NORMAL) {
      return 0;
    }
    if (current == WINDOW_TOP) {
      return topWindow.setFont(fontnumber);
    } else {
      return bottomWindow.setCurrentFont(fontnumber);
    }
  }

  public void setBackground(int colornumber, int window) {
    LOG.info("setBackground, color: " + colornumber);
    topWindow.setBackground(colornumber);
    bottomWindow.setBackground(colornumber);
  }

  public void setForeground(int colornumber, int window) {
    LOG.info("setForeground, color: " + colornumber);
    topWindow.setForeground(colornumber);
    bottomWindow.setForeground(colornumber);
  }

  public OutputStream getOutputStream() { return this; }

  // OutputStream
  private boolean selected;
  
  public void print(char zsciiChar) {
    char unicodeChar = encoding.getUnicodeChar(zsciiChar);
    if (current == WINDOW_BOTTOM) {
      bottomWindow.printChar(unicodeChar);
      if (!bottomWindow.isBuffered()) {
        flush();
      }
    } else if (current == WINDOW_TOP) {
      for (ScreenModelListener l : screenModelListeners) {
        topWindow.notifyChange(l, unicodeChar);
        topWindow.incrementCursorXPos();
      }
    }
  }
  
  public void close() { }

  /**
   * Notify listeners that the screen has changed.
   */
  public void flush() {
    for (ScreenModelListener l : screenModelListeners) {
      l.screenModelUpdated(this);
    }
  }

  public void select(boolean flag) { selected = flag; }

  public boolean isSelected() { return selected; }

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

  public int getNumRowsUpper() { return topWindow.getNumRows(); }
  
  public int getBackground() {
    int background = bottomWindow.getBackground();
    return background == COLOR_DEFAULT ?
      getDefaultBackground() : background;
  }
  public int getForeground() {
    int foreground = bottomWindow.getForeground();
    return foreground == COLOR_DEFAULT ?
      getDefaultForeground() : foreground;
  }
  private int getDefaultBackground() {
    return memory.readUnsigned8(StoryFileHeader.DEFAULT_BACKGROUND);
  }
  
  private int getDefaultForeground() {
    return memory.readUnsigned8(StoryFileHeader.DEFAULT_FOREGROUND);
  }
  
  public List<AnnotatedText> getLowerBuffer() {
    return bottomWindow.getBuffer();
  }
}