/*
 * $Id$
 * 
 * Created on 11/23/2005
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
package org.zmpp.vm;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements output stream 3. This stream writes to dynamic
 * memory. The stream contains a table address stack in order to
 * support nested selections.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MemoryOutputStream implements OutputStream {

  /**
   * Support nested
   */
  private List<Integer> tableStack;
    
  public MemoryOutputStream() {
  
    tableStack = new ArrayList<Integer>();
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(short zchar) {

  }

  /**
   * {@inheritDoc}
   */
  public void close() {

  }

  /**
   * {@inheritDoc}
   */
  public void setEnabled(boolean flag) {
    
    if (!flag) {
      
      // TODO: Do what needs to be done before popping the last element off
      tableStack.remove(tableStack.size() - 1);
    }
  }
  
  public void select(int tableAddress) {

    tableStack.add(tableAddress);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isEnabled() {

    return !tableStack.isEmpty();
  }
}
