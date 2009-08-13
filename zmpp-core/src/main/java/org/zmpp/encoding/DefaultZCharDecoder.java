/*
 * Created on 2006/01/09
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

import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.Memory;

/**
 * This is the default implementation of the ZCharDecoder interface.
 * The central method is decode2Unicode which handles abbreviations,
 * 10 Bit escape characters and alphabet table characters. Alphabet
 * table characters and shift states are handled by the ZCharTranslator
 * object.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public final class DefaultZCharDecoder implements ZCharDecoder {

  private ZCharTranslator translator;
  private ZsciiEncoding encoding;
  private AbbreviationsTable abbreviations;
  private ZCharDecoder abbreviationDecoder;

  /**
   * Constructor.
   * @param encoding the ZsciiEncoding object
   * @param translator the ZStringTranslator o
   * @param abbreviations the abbreviations table used for decoding
   */
  public DefaultZCharDecoder(final ZsciiEncoding encoding,
      final ZCharTranslator translator,
      final AbbreviationsTable abbreviations) {
    this.abbreviations = abbreviations;
    this.translator = translator;
    this.encoding = encoding;
  }

  /**
   * {@inheritDoc}
   */
  public String decode2Zscii(final Memory memory,
      final int address, final int length) {
    final StringBuilder builder = new StringBuilder();
    translator.reset();
    final char[] zbytes = extractZbytes(memory, address, length);
    char zchar;
    int i = 0, newpos;

    while (i < zbytes.length) {
      boolean decoded = false;
      zchar = zbytes[i];
      newpos = handleAbbreviation(builder, memory, zbytes, i);
      decoded = (newpos > i);
      i = newpos;

      if (!decoded) {
        newpos = handleEscapeA2(builder, zbytes, i);
        decoded = newpos > i;
        i = newpos;
      }

      if (!decoded) {
        decodeZchar(builder, zchar);
        i++;
      }
    }
    return builder.toString();
  }

  private int handleAbbreviation(final StringBuilder builder,
      final Memory memory, final char[] data, final int pos) {
    int position = pos;
    final char zchar = data[position];

    if (translator.isAbbreviation(zchar)) {

      // we need to check if we are at the end of the buffer, even if an
      // abbreviation is suggested. This happens e.g. in Zork I
      if (position < (data.length - 1)) {
        position++; // retrieve the next byte to determine the abbreviation

        // the abbreviations table could be null, simply skip that part in this
        // case
        if (abbreviations != null) {
          final int x = data[position];
          final int entryNum = 32 * (zchar - 1) + x;
          final int entryAddress = abbreviations.getWordAddress(entryNum);
          createAbbreviationDecoderIfNotExists();
          appendAbbreviationAtAddress(memory, entryAddress, builder);
        }
      }
      position++;
    }
    return position;
  }

  private void createAbbreviationDecoderIfNotExists() {
    if (abbreviationDecoder == null) {

      // We only use one abbreviation decoder instance here, we need
      // to clone the alphabet table, so the abbreviation decoding
      // will not influence the continuation of the decoding process
      try {
        abbreviationDecoder = new DefaultZCharDecoder(encoding,
                (ZCharTranslator) translator.clone(), null);
      } catch (CloneNotSupportedException ex) {
        // should never happen
        ex.printStackTrace();
      }
    }
  }

  private void appendAbbreviationAtAddress(Memory memory, int entryAddress,
          StringBuilder builder) {
    if (abbreviationDecoder != null) {
      final String abbrev = abbreviationDecoder.decode2Zscii(memory,
        entryAddress, 0);
        builder.append(abbrev);
    }
  }

  private int handleEscapeA2(final StringBuilder builder,
      final char[] data, final int pos) {
    int position = pos;
    if (translator.willEscapeA2(data[position])) {

      // If the data is truncated, do not continue (check if the
      // constant should be 2 or 3)
      if (position < data.length - 2) {
        joinToZsciiChar(builder, data[position + 1], data[position + 2]);
        // skip the three characters read (including the loop increment)
        position += 2;
      }
      position++;
      translator.resetToLastAlphabet();
    }
    return position;
  }

  /**
   * {@inheritDoc}
   */
  public char decodeZChar(final char zchar) {
    if (ZsciiEncoding.isAscii(zchar) || ZsciiEncoding.isAccent(zchar)) {
      return zchar;
    } else {
      return translator.translate(zchar);
    }
  }

  /**
   * Decodes an encoded character and adds it to the specified builder object.
   * @param builder a ZsciiStringBuilder object
   * @param zchar the encoded character to decode and add
   */
  private void decodeZchar(final StringBuilder builder,
      final char zchar) {
    final char c = decodeZChar(zchar);
    if (c != 0) {
      builder.append(c);
    }
  }

  /** {@inheritDoc} */
  public ZCharTranslator getTranslator() { return translator; }

  // ***********************************************************************
  // ******* Private
  // *****************************

  /**
   * Determines the last word in a z sequence. The last word has the
   * MSB set.
   * @param zword the zword
   * @return true if zword is the last word, false, otherwise
   */
  public static boolean isEndWord(final char zword) {
    return (zword & 0x8000) > 0;
  }

  /**
   * This function unfortunately generates a List object on each invocation,
   * the advantage is that it will return all the characters of the Z string.
   * @param memory the memory access object
   * @param address the address of the z string
   * @param length the maximum length that the array should have or 0 for
   * unspecified
   * @return the z characters of the string
   */
  public static char[] extractZbytes(final Memory memory,
                                      final int address, final int length) {
    char zword = 0;
    int currentAddr = address;
    final List<char[]> byteList = new ArrayList<char[]>();

    do {
      zword = memory.readUnsigned16(currentAddr);
      byteList.add(extractZEncodedBytes(zword));
      currentAddr += 2; // increment pointer

      // if this is a dictionary entry, we need to provide the
      // length and cancel the loop earlier
      if (length > 0 && (currentAddr - address) >= length) {
        break;
      }
    } while (!isEndWord(zword));

    final char[] result = new char[byteList.size() * 3];
    int i = 0;
    for (char[] triplet : byteList) {
      for (char b : triplet) {
        result[i++] = (char) b;
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  public int getNumZEncodedBytes(Memory memory, int address) {
    char zword = 0;
    int currentAddress = address;
    do {
      zword = memory.readUnsigned16(currentAddress);
      currentAddress += 2;
    } while (!isEndWord(zword));
    return currentAddress - address;
  }

  /**
   * Extracts three 5 bit fields from the given 16 bit word and returns
   * an array of three bytes containing these characters.
   * @param zword a 16 bit word
   * @return an array of three bytes containing the three 5-bit ZSCII characters
   * encoded in the word
   */
  private static char[] extractZEncodedBytes(final char zword) {
    final char[] result = new char[3];
    result[2] = (char) (zword & 0x1f);
    result[1] = (char) ((zword >> 5) & 0x1f);
    result[0] = (char) ((zword >> 10) & 0x1f);
    return result;
  }

  /**
   * Joins the specified two bytes into a 10 bit ZSCII character.
   * @param builder the StringBuilder to write to
   * @param top the byte holding the top 5 bit of the zchar
   * @param bottom the byte holding the bottom 5 bit of the zchar
   */
  private void joinToZsciiChar(final StringBuilder builder,
                               final char top, final char bottom) {
    builder.append((char) (top << 5 | bottom));
  }
}
