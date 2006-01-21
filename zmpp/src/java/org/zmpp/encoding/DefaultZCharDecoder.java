/*
 * $Id$
 * 
 * Created on 2006/01/09
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

import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.MemoryReadAccess;

/**
 * This is the default implementation of the ZCharDecoder interface.
 * The central method is decode2Unicode which handles abbreviations,
 * 10 Bit escape characters and alphabet table characters. Alphabet
 * table characters and shift states are handled by the ZCharTranslator
 * object.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public final class DefaultZCharDecoder implements ZCharDecoder {

  /**
   * The ZStringTranslator.
   */
  private ZCharTranslator translator;
  
  /**
   * The Zscii encoding.
   */
  private ZsciiEncoding encoding;
  
  /**
   * The abbreviations table used for decoding.
   */
  private AbbreviationsTable abbreviations;
  
  /**
   * The abbreviation decoder.
   */
  private ZCharDecoder abbreviationDecoder;
  
  /**
   * Constructor.
   * 
   * @param encoding the ZsciiEncoding object
   * @param translator the ZStringTranslator o
   * @param abbreviations the abbreviations table used for decoding
   */
  public DefaultZCharDecoder(ZsciiEncoding encoding,
      ZCharTranslator translator,
      AbbreviationsTable abbreviations) { 
    
    this.abbreviations = abbreviations;
    this.translator = translator;
    this.encoding = encoding;
  }
  
  /**
   * {@inheritDoc}
   */
  public String decode2Unicode(MemoryReadAccess memaccess, int address) {
    
    return decode2Unicode(memaccess, address, 0);
  }
  
  /**
   * {@inheritDoc}
   */
  public String decode2Unicode(MemoryReadAccess memaccess, int address,
                               int length) {
    
    StringBuilder builder = new StringBuilder();
    translator.reset();    
    
    short[] zbytes = extractZbytes(memaccess, address, length);
    
    short zchar;
    int i = 0, newpos;

    while (i < zbytes.length) {
      
      boolean decoded = false;
      zchar = zbytes[i];
      newpos = handleAbbreviation(builder, memaccess, zbytes, i);
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
   
  private int handleAbbreviation(StringBuilder builder,
      MemoryReadAccess memaccess, short[] data, int pos) {
    
    short zchar = data[pos];
    
    if (translator.isAbbreviation(zchar)) {
    
      // we need to check if we are at the end of the buffer, even if an
      // abbreviation is suggested. This happens e.g. in Zork I
      if (pos < (data.length - 1)) {
      
        pos++; // retrieve the next byte to determine the abbreviation
    
        // the abbreviations table could be null, simply skip that part in this
        // case
        if (abbreviations != null) {

          int x = data[pos];
          int entryNum = 32 * (zchar - 1) + x;
          int entryAddress = abbreviations.getWordAddress(entryNum);
          
          if (abbreviationDecoder == null) {
            
            // We only use one abbreviation decoder instance here, we need
            // to clone the alphabet table, so the abbreviation decoding
            // will not influence the continuation of the decoding process
            abbreviationDecoder = new DefaultZCharDecoder(encoding,
                  (ZCharTranslator) translator.clone(), null);
          }
          String abbrev = abbreviationDecoder.decode2Unicode(memaccess,
              entryAddress);
          builder.append(abbrev);
        }
      }
      pos++;
    }
    return pos;
  }
  
  private int handleEscapeA2(StringBuilder builder, short[] data, int pos) {
    
    if (translator.willEscapeA2(data[pos])) {

      // If the data is truncated, do not continue (check if the
      // constant should be 2 or 3)
      if (pos < data.length - 2) {
      
        joinToZsciiChar(builder, data[pos + 1], data[pos + 2]);
        pos += 2; // skip the three characters read (including the loop increment)
      }
      pos++;
      translator.resetToLastAlphabet();
    }
    return pos;
  }

  /**
   * {@inheritDoc}
   */
  public short decodeZChar(short zchar) {
    
    if (ZsciiEncoding.isAscii(zchar) || ZsciiEncoding.isAccent(zchar)) {
      
      return zchar;
      
    } else {
      
      return (short) translator.translate(zchar);
    }  
  }
  
  private void decodeZchar(StringBuilder builder, short zchar) {
          
    short c = decodeZChar(zchar);
    if (c != 0) {
        
      builder.append((char) c);
    }  
  }
  
  /**
   * {@inheritDoc}
   */
  public ZCharTranslator getTranslator() {
    
    return translator;
  }
  // ***********************************************************************
  // ******* Private
  // *****************************
  
  /**
   * Determines the last word in a z sequence. The last word has the
   * MSB set.
   * 
   * @param zword the zword
   * @return true if zword is the last word, false, otherwise
   */
  public static boolean isEndWord(short zword) {
    
    return (zword & 0x8000) > 0;
  }
  
  /**
   * This function unfortunately generates a List object on each invocation,
   * the advantage is that it will return all the characters of the Z string.
   *  
   * @param memaccess the memory access object
   * @param address the address of the z string
   * @param length the maximum length that the array should have or 0 for
   * unspecified
   * @return the z characters of the string
   */
  public static short[] extractZbytes(MemoryReadAccess memaccess,
                                      int address, int length) {
    
    //if (length > 0) System.out.println("maximum length is: " + length);
    short zword = 0;
    int currentAddr = address;
    List<short[]> byteList = new ArrayList<short[]>();
    
    do {
      
      zword = memaccess.readShort(currentAddr);
      byteList.add(extractBytes(zword));
      currentAddr += 2; // increment pointer
      
      // if this is a dictionary entry, we need to provide the
      // length and cancel the loop earlier
      if (length > 0 && (currentAddr - address) >= length) {
        
        break;
      }
      
    } while (!isEndWord(zword));
    
    short[] result = new short[byteList.size() * 3];
    int i = 0;
    for (short[] triplet : byteList) {
      for (short b : triplet) {
        result[i++] = b;
      }
    }
    return result;
  }
  
  /**
   * Extracts three 5 bit fields from the given 16 bit word and returns
   * an array of three bytes containing these characters.
   * 
   * @param zword a 16 bit word
   * @return an array of three bytes containing the three 5-bit ZSCII characters
   * encoded in the word
   */
  private static short[] extractBytes(short zword) {
    
    short[] result = new short[3];
    result[2] = (short) (zword & 0x1f);
    result[1] = (short) ((zword >> 5) & 0x1f);
    result[0] = (short) ((zword >> 10) & 0x1f);
    return result;
  }
  
  /**
   * Joins the specified two bytes into a 10 bit ZSCII character.
   * 
   * @param builder the StringBuilder to write to
   * @param top the byte holding the top 5 bit of the zchar
   * @param bottom the byte holding the bottom 5 bit of the zchar
   */  
  private void joinToZsciiChar(StringBuilder builder,
                               short top, short bottom) {
    
    short zchar = (short) (top << 5 | bottom);
    builder.append(encoding.getUnicodeChar(zchar));
  }  
}
