/*
 * $Id$
 * 
 * Created on 2006/02/24
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

import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Machine;

public class ScreenOutputStream implements OutputStream {

  private boolean isSelected;
  private StringBuilder streambuffer;
  private Machine machine;
  private Viewport viewport;
  
  public ScreenOutputStream(Machine machine, Viewport viewport) {
  
    this.machine = machine;
    this.viewport = viewport;
    streambuffer = new StringBuilder();
  }
  
  public boolean isSelected() {
    
    return isSelected;
  }
  
  public void select(boolean flag) {
  
    isSelected = flag;
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(final short zsciiChar, boolean isInput) {

    //System.out.printf("@print %c (isInput: %b)\n", (char) zsciiChar, isInput);    
    if (zsciiChar == ZsciiEncoding.NEWLINE) {
    
      printChar('\n', isInput);
    
    } else {
    
      printChar(machine.getGameData().getZsciiEncoding().getUnicodeChar(
          zsciiChar), isInput);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deletePrevious(short zchar) {
    
    char deleteChar =
      machine.getGameData().getZsciiEncoding().getUnicodeChar(zchar);
    viewport.getCurrentWindow().backspace(deleteChar);
  }
  
  public void flush() {
    
    // save some unnecessary flushes
    if (streambuffer.length() > 0) {
      
      viewport.getCurrentWindow().printString(streambuffer.toString());
      streambuffer = new StringBuilder();
    }
  }
    
  /**
   * {@inheritDoc}
   */
  public void close() { }
  
  private void printChar(char c, boolean isInput) {

    if (isInput || !viewport.getCurrentWindow().isBuffered()) {
      
      viewport.getCurrentWindow().printString(String.valueOf(c));
      
    } else {
      
      streambuffer.append(c);
    }
  } 
}
