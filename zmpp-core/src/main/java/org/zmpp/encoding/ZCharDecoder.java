/*
 * Created on 2005/09/23
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
package org.zmpp.encoding;

import org.zmpp.base.Memory;

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
 * @version 1.5
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
   * Performs a ZSCII decoding at the specified position of
   * the given memory object, this method is exclusively designed to
   * deal with the problems of dictionary entries. These can be cropped,
   * leaving the string in a state, that can not be decoded properly
   * otherwise. If the provided length is 0, the semantics are
   * equal to the method without the length parameter.
   *
   * @param memory a Memory object
   * @param address the address of the string
   * @param length the maximum length in bytes
   * @return the decoded string
   */
  String decode2Zscii(Memory memory, int address, int length);

  /**
   * Returns the number of Z encoded bytes at the specified position.
   * @param memory the Memory object
   * @param address the string address
   * @return the number Z encoded bytes
   */
  int getNumZEncodedBytes(Memory memory, int address);

  /**
   * Decodes the given byte value to the specified buffer using the working
   * alphabet.
   *
   * @param zchar a z encoded character, needs to be a non-shift character
   */
  char decodeZChar(char zchar);

  /**
   * Returns the ZStringTranslator.
   *
   * @return the translator
   */
  ZCharTranslator getTranslator();
}