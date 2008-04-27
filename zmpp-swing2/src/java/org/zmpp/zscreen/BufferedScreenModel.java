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
import org.zmpp.vm.TextCursor;

/**
 * BufferedScreenModel is the attempt to provide a reusable screen model
 * that will be part of the core in later versions. It is mainly a
 * configurable virtual window management model, providing virtual windows
 * that the machine writes to. It is intended to provide interfaces to
 * both Glk and Z-machine and to combine the abilities of both.
 * 
 * @author Wei-ju Wu
 */
public class BufferedScreenModel implements ScreenModel, OutputStream {
  
  private int current = 0;
  private BufferedTextWindow windows[] = new BufferedTextWindow[1];
  private List<ScreenModelListener> modelListeners =
    new ArrayList<ScreenModelListener>();
  
  public interface ScreenModelListener {
    void screenModelUpdated(BufferedScreenModel screenModel);
  }

  public BufferedScreenModel() {
    windows[0] = new BufferedTextWindow();
  }
  
  public void addScreenModelListener(ScreenModelListener l) {
    modelListeners.add(l);
  }
  
  public BufferedTextWindow getWindow(int windowNum) {
    return windows[windowNum];
  }

  public void reset() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void splitWindow(int linesUpperWindow) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setWindow(int window) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setTextStyle(int style) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setBufferMode(boolean flag) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void eraseLine(int value) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void eraseWindow(int window) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setTextCursor(int line, int column, int window) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public TextCursor getTextCursor() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setPaging(boolean flag) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int setFont(int fontnumber) {
    //windows[0].set
    throw new UnsupportedOperationException("Not supported yet.");
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
    if (zchar == '>') System.out.println("PROMPT");
    windows[current].printChar(zchar);
  }

  public void deletePrevious(char zchar) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void close() { }

  /**
   * Notify listeners that.
   */
  public void flush() {
    for (ScreenModelListener l : modelListeners) {
      l.screenModelUpdated(this);
    }
  }

  public void select(boolean flag) {
    selected = flag;
  }

  public boolean isSelected() {
    return selected;
  }

}