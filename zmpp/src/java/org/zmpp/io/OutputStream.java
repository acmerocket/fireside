/*
 * Created on 10/05/2005
 * Copyright 2005-2009 by Wei-ju Wu
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
package org.zmpp.io;

/**
 * This interface defines an output stream in the Z-machine.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface OutputStream {

  /**
   * Prints a ZSCII character to the stream. The isInput parameter is
   * needed to implement edit buffers.
   * 
   * @param zchar the ZSCII character to print
   */
  void print(char zchar);
  
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
