/*
 * $Id$
 * 
 * Created on 05.10.2005
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
package org.zmpp.io;

/**
 * This interface defines an output stream in the Z-machine.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface OutputStream {

  /**
   * Prints a ZSCII character to the stream. The isInput parameter is
   * needed to implement edit buffers.
   * 
   * @param zchar the ZSCII character to print
   * @param isInput is true if the character is an echo to the input
   */
  void print(short zchar, boolean isInput);
  
  /**
   * Close underlying resources.
   */
  void close();
  
  /**
   * Flushes the output.
   */
  void flush();
  
  /**
   * Enables/disables this output stream.
   * 
   * @param flag true to enable, false to disable
   */
  void select(boolean flag);
  
  /**
   * Determine, if this stream is selected.
   * 
   * @return true if selected, false if deselected
   */
  boolean isSelected();  
}
