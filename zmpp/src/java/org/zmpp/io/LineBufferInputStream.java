/*
 * Created on 2008/04/25
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

import java.util.LinkedList;
import java.util.Queue;

/**
 * The LineBufferInputStream is the default implementation for the keyboard
 * input stream. It is simply a queue holding a number of input lines.
 * Normally this is only one, but it could be used for testing by simply
 * storing more lines and running the core on it.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class LineBufferInputStream implements InputStream {

  /**
   * The queue holding input lines.
   */
  private Queue<String> inputLines = new LinkedList<String>();
  
  /**
   * Adds an input line to the end of the buffer.
   * @param line the new input line
   */
  public void addInputLine(String line) {
    inputLines.add(line);
  }

  /**
   * {@inheritDoc}
   */
  public String readLine() {
    return inputLines.remove();
  }

  /**
   * {@inheritDoc}
   */
  public void close() { }
}
