/*
 * $Id$
 * 
 * Created on 2005/09/23
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

import org.zmpp.base.MemoryReadAccess;

/**
 * This interface provides decoding for the Z character encoding into
 * the Java character system. It is important to point out that there
 * is a difference between Z characters and the ZCSII encoding. Where
 * ZSCII is a character set that is similar to ASCII/iso-8859-1, the
 * Z characters are a encoded form of characters in memory that provide
 * some degree of compression and encryption.
 * 
 * ZCharConverter uses the alphabet tables specified in the Z machine
 * standards document 1.0, section 3.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface ZCharDecoder {

  /**
   * This interface defines the abstract access to an abbreviations
   * table in memory, this will be used for decoding if needed. 
   */
  public interface AbbreviationsTable {
  
    int getWordAddress(int entryNum);
  }
  
  /**
   * Performs a ZSCII to Unicode conversion at the specified position of
   * the given memory object.
   * 
   * @param memaccess a MemoryReadAccess object
   * @param address the address of a ZSCII string
   * @return a converted Unicode string 
   */
  String decode2Unicode(MemoryReadAccess memaccess, int address);

  /**
   * Decodes the given byte value to the specified buffer using the working
   * alphabet.
   * 
   * @param builder the buffer to print into
   * @param alphabet the working alphabet
   * @param b a z character, needs to be a non-shift character
   */
  void decodeZchar(StringBuilder builder, byte b);
  
  /**
   * Returns the ZStringTranslator.
   * 
   * @return the translator
   */
  ZCharTranslator getTranslator();
}
