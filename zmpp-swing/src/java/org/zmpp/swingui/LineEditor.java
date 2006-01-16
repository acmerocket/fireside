/*
 * $Id$
 * 
 * Created on 7.11.2005
 * Copyright 2005-2006 by Wei-ju Wu
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vmutil.ZsciiEncoding;

public class LineEditor implements KeyListener, MouseListener {

  private boolean inputMode;
  private List<Short> editbuffer;
  private StoryFileHeader fileheader;
  
  public LineEditor(StoryFileHeader fileheader, ZsciiEncoding encoding) {
  
    this.fileheader = fileheader;
    editbuffer = new LinkedList<Short>();
  }
  
  public void setInputMode(boolean flag) {
    
    synchronized (editbuffer) {
      
      inputMode = flag;
      editbuffer.clear();
      editbuffer.notifyAll();
    }
  }

  public void cancelInput() {
  
    synchronized (editbuffer) {
  
      editbuffer.add(ZsciiEncoding.NULL);
      editbuffer.notifyAll();
    }
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
      editbuffer.notifyAll();
    }
    return zsciiChar;
  }
  
  public boolean isInputMode() {
    
    return inputMode;
  }
  
  public void keyPressed(KeyEvent e) {

    switch (e.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE:
      case KeyEvent.VK_DELETE:
        addToBuffer(ZsciiEncoding.DELETE);
        break;
      case KeyEvent.VK_SPACE:
        addToBuffer((short) ' ');
        break;
    }
  }
  
  public void keyTyped(KeyEvent e) {
  
    char c = e.getKeyChar();    
    ZsciiEncoding encoding = ZsciiEncoding.getInstance();
    if (encoding.isConvertableToZscii(c)
        && !handledInKeyPressed(c)
        && !handledInKeyReleased(c)) {
        
      addToBuffer(encoding.getZsciiChar(c));
    }
  }
  
  public void keyReleased(KeyEvent e) {
    
    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
        addToBuffer(ZsciiEncoding.CURSOR_UP);
        break;
      case KeyEvent.VK_DOWN:
        addToBuffer(ZsciiEncoding.CURSOR_DOWN);
        break;
      case KeyEvent.VK_LEFT:
        addToBuffer(ZsciiEncoding.CURSOR_LEFT);
        break;
      case KeyEvent.VK_RIGHT:
        addToBuffer(ZsciiEncoding.CURSOR_RIGHT);
        break;
      case KeyEvent.VK_ESCAPE:
        addToBuffer(ZsciiEncoding.ESCAPE);
        break;
    }
  }

  private void addToBuffer(short zsciiChar) {
    
    if (isInputMode()) {
      
      synchronized (editbuffer) {
      
        editbuffer.add(zsciiChar);
        editbuffer.notifyAll();
      }
    }
  }
  
  private boolean handledInKeyPressed(char c) {
    
    return c == ' ' || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE;
  }
  
  private boolean handledInKeyReleased(char c) {
    
    return c == KeyEvent.VK_UP || c == KeyEvent.VK_DOWN
           || c == KeyEvent.VK_LEFT || c == KeyEvent.VK_RIGHT
           || c == KeyEvent.VK_ESCAPE;
  }
  
  public void mouseClicked(MouseEvent e) {
    
    // Only if mouse is used
    if (fileheader.isEnabled(StoryFileHeader.Attribute.USE_MOUSE)) {
      
      fileheader.setMouseCoordinates(e.getX(), e.getY());
      
      // Store single clicks and double clicks with different codes
      addToBuffer((short) ((e.getClickCount() == 1) ?
          ZsciiEncoding.MOUSE_SINGLE_CLICK :
          ZsciiEncoding.MOUSE_DOUBLE_CLICK)); 
    }
  }
  
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mousePressed(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) { }
}
