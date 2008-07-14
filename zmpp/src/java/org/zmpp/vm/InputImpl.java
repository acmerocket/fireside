/*
 * $Id$
 * 
 * Created on 2006/02/14
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
package org.zmpp.vm;

import java.io.Closeable;
import org.zmpp.io.InputStream;

/**
 * Input interface implementation.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class InputImpl implements Input, Closeable {

  /**
   * This is the array of input streams.
   */
  private InputStream[] inputStream = new InputStream[2];
  
  /**
   * The selected input stream.
   */
  private int selectedInputStreamIndex = 0;
  
  public void close() {
    if (inputStream != null) {
      for (int i = 0; i < inputStream.length; i++) {
        if (inputStream[i] != null) {
          inputStream[i].close();
        }
      }
    }
  }
  
  /**
   * Sets an input stream to the specified number.
   * @param streamnumber the input stream number
   * @param stream the input stream to set
   */
  public void setInputStream(final int streamnumber, final InputStream stream) {    
    inputStream[streamnumber] = stream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectInputStream(final int streamnumber) {
    selectedInputStreamIndex = streamnumber;    
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream getSelectedInputStream() {
    
    return inputStream[selectedInputStreamIndex];
  }
    
}
