/*
 * $Id$
 * 
 * Created on 7.11.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.swingui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import org.zmpp.vmutil.ZsciiEncoding;

public class LineEditor implements KeyListener {

  private boolean inputMode;
  private List<Short> editbuffer;
  
  public LineEditor() {
  
    editbuffer = new LinkedList<Short>();
  }
  
  public void setInputMode(boolean flag) {
    
    inputMode = flag;
    editbuffer.clear();
  }

  public short nextZsciiChar() {

    short zsciiChar = 0;
    synchronized (editbuffer) { 
      while (editbuffer.size() == 0) {
      
        try {
          editbuffer.wait();
        } catch (Exception ex) { }
      }
      zsciiChar = editbuffer.remove(0);
    }
    return zsciiChar;
  }
  
  public synchronized boolean isInputMode() {
    
    return inputMode;
  }
  
  public void keyPressed(KeyEvent e) {
    
    switch (e.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE:
      case KeyEvent.VK_DELETE:
      
        if (isInputMode()) {
        
          synchronized (editbuffer) {
          
            editbuffer.add(ZsciiEncoding.DELETE);
            editbuffer.notifyAll();
          }
        }
        break;
      case KeyEvent.VK_SPACE:
        if (isInputMode()) {
        
          synchronized (editbuffer) {
          
            editbuffer.add((short) ' ');
            editbuffer.notifyAll();
          }
        }
        break;
    }
  }
  
  public void keyTyped(KeyEvent e) {
  
    char c = e.getKeyChar();    
    ZsciiEncoding encoding = ZsciiEncoding.getInstance();
    if (isInputMode() && encoding.isConvertableToZscii(c)
        && !handledInKeyPressed(c)) {
        
      synchronized (editbuffer) {
        
        editbuffer.add(encoding.getZsciiChar(c));
        editbuffer.notifyAll();
      }
    }
  }
  
  public void keyReleased(KeyEvent e) { }

  
  private boolean handledInKeyPressed(char c) {
    
    return c == ' ' || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE;
  }
}
