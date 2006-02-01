/*
 * $Id$
 * 
 * Created on 2006/02/01
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
package org.zmpp.encoding;

/**
 * This class represents ZSCII strings. These are especially important to
 * the input system since the dictionaries store their entries in ZSCII
 * and the input will be converted into this encoding.
 * 
 * ZSCII strings are represented as a sequence of 16-bit characters.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZsciiString {

  /**
   * The encoding object.
   */
  private static ZsciiEncoding encoding;

  private short[] data;
  
  /**
   * Global initialization method.
   * 
   * @param encoding the encoding
   */
  public static void initialize(ZsciiEncoding encoding) {
 
    ZsciiString.encoding = encoding;
  }

  /**
   * Constructor.
   * 
   * @param data the source array
   */
  public ZsciiString(short[] data) {
    
    this.data = data;
  }

  /**
   * Returns the length of this string.
   * 
   * @return the length
   */
  public int length() {
    
    return data.length;
  }
  
  /**
   * {@inheritDoc}
   */
  public int hashCode() {
    
    int hashvalue = 0;
    for (int i = 0; i < data.length; i++) {
      
      hashvalue = 31 * hashvalue + data[i];
    }
    return hashvalue;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    
    if (o == this) return true;
    if (o != null && o instanceof ZsciiString) {
      
      short[] data2 = ((ZsciiString) o).data;
      if (data.length == data2.length) {
        
        for (int i = 0; i < data.length; i++) {
        
          if (data[i] != data2[i]) return false;
        }
        return true;
      }
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public String toString() {
   
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      
      builder.append(encoding.getUnicodeChar(data[i]));
    }
    return builder.toString();
  }
}
