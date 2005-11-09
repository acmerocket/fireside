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

import org.zmpp.base.MemoryAccess;

public class LineEditor implements KeyListener {

  //private Logger editlogger = Logger.getLogger("LineEditor");
  private boolean inputMode;
  private MemoryAccess memaccess;
  private int bufferaddress;
  private int bufferlength;
  private StringBuilder editbuffer = new StringBuilder();
  private TextViewport viewport;
  
  
  public LineEditor(TextViewport viewport) {
  
    this.viewport = viewport;
  }
  
  public synchronized void leaveInputMode() {
    
    this.inputMode = false;
    notifyAll();
  }

  public synchronized void enterInputMode(MemoryAccess memaccess,
      int address, int bufferlen) {
  
    this.memaccess = memaccess;
    this.bufferaddress = address;
    this.bufferlength = bufferlen;
    this.inputMode = true;
    notifyAll();
  }
  
  public synchronized boolean isInputMode() {
    
    return inputMode;
  }
  
  public void keyPressed(KeyEvent e) {
    
    switch (e.getKeyCode()) {
    case KeyEvent.VK_BACK_SPACE:
      if (isInputMode() && editbuffer.length() > 0) {
        
        char lastchar = editbuffer.charAt(editbuffer.length() - 1);
        editbuffer.deleteCharAt(editbuffer.length() - 1);
        viewport.backSpace(lastchar);
        viewport.repaint();
      }
      break;
    case KeyEvent.VK_SPACE:
      if (isInputMode() && editbuffer.length() > 0) {
        
        if (editbuffer.length() < (bufferlength - 1)) {
          
          editbuffer.append(' ');
          viewport.printChar(' ');
          viewport.repaint();
        } 
      }
      break;
    }
  }
  
  public void keyReleased(KeyEvent e) {
  
    //editlogger.info("keyReleased(), thread: " + Thread.currentThread().getName());
    switch (e.getKeyCode()) {
    case KeyEvent.VK_ENTER:
      if (isInputMode()) {
        
        viewport.stopEditing();
        viewport.newline();
        viewport.repaint();
        
        transferEditBuffer();
        leaveInputMode();
      }
      break;      
    default:
      break;
    }
  }

  private void transferEditBuffer() {
    
    String editstring = editbuffer.toString();
    int n = editstring.length();
    for (int i = 0; i < n; i++) {

      memaccess.writeByte(bufferaddress + i, (byte) editstring.charAt(i));
    }
    memaccess.writeByte(bufferaddress + n, (byte) 0);
    editbuffer = new StringBuilder();
  }
  
  
  public void keyTyped(KeyEvent e) {
  
    //editlogger.info("keyTyped(), thread: " + Thread.currentThread().getName());
    char c = e.getKeyChar();
    if (isInputMode()) {
      
      if (isPrintable(c) && editbuffer.length() < (bufferlength - 1)) {
        
        editbuffer.append(c);
        viewport.printChar(c);
        viewport.repaint();
      } 
    }
  }
  
  private boolean isPrintable(char c) {
    
    return Character.isLetterOrDigit(c) || c == ',' || c == '\"';
  }  
}
