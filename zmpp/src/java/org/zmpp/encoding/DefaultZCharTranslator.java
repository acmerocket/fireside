/*
 * $Id$
 * 
 * Created on 2006/01/15
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

import org.zmpp.encoding.AlphabetTable.Alphabet;

/**
 * The default implementation of ZCharTranslator.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DefaultZCharTranslator implements Cloneable, ZCharTranslator {

  private AlphabetTable alphabetTable;
  private Alphabet currentAlphabet;
 
  private ZsciiEncoding encoding;
  
  /**
   * Constructor.
   * 
   * @param alphabetTable the alphabet table
   * @param encoding the encoding
   */
  public DefaultZCharTranslator(AlphabetTable alphabetTable,
                                ZsciiEncoding encoding) {
    
    this.alphabetTable = alphabetTable;
    this.encoding = encoding;
    reset();
  }
  
  /**
   * {@inheritDoc}
   */
  public void reset() {
    
    this.currentAlphabet = Alphabet.A0;
  }
  
  /**
   * {@inheritDoc}
   */
  public Object clone() {
    
    DefaultZCharTranslator clone = null;
    try {
      
      clone = (DefaultZCharTranslator) super.clone();
      clone.reset();
      
    } catch (CloneNotSupportedException ex) { }
    
    return clone;
  }
  
  /**
   * {@inheritDoc}
   */
  public Alphabet getCurrentAlphabet() {
    
    return currentAlphabet;
  }
  
  /**
   * {@inheritDoc}
   */
  public char translate(short zchar) {
    
    // Handle the shift
    if (isShiftCharacter((byte) zchar)) {
      
      shift((byte) zchar);
      return '\0';
    }
    
    char result;
    
    if (zchar == 0) {
      
      result = ' ';
      
    } else if (isAsciiCharacter((byte) zchar)
             || ZsciiEncoding.isAccent(zchar)) {
      
      result = encoding.getUnicodeChar(zchar);
      
    } else if (isInAlphabetRange((byte) zchar)) {
      
      switch (currentAlphabet) {
    
        case A0:
          result = (char)
            alphabetTable.getA0Char(zchar - AlphabetTable.ALPHABET_START);
          break;
        case A1:
          result = (char)
            alphabetTable.getA1Char(zchar - AlphabetTable.ALPHABET_START);
          break;
        case A2:
        default:            
          result = (char)
            alphabetTable.getA2Char(zchar - AlphabetTable.ALPHABET_START);
          break;
      }
      
    } else {
      
      //System.out.printf("not handled : %d\n", zchar);
      result = '?';
    }
    
    // For now, that's sufficient
    reset();
    return result;
  }
  
  /**
   * Determines if the given byte falls in the ASCII range.
   * 
   * @param zchar a byte value
   * @return true, if the value falls in the ASCII range, false, else
   */
  private static boolean isAsciiCharacter(byte zchar) {
    
    return ZsciiEncoding.ASCII_START <= zchar
           && zchar <= ZsciiEncoding.ASCII_END;
  }
  
  /**
   * Determines if the given byte value falls within the alphabet range.
   * 
   * @param zchar the zchar value
   * @return true if the value is in the alphabet range, false, otherwise
   */
  private static boolean isInAlphabetRange(byte zchar) {
    
    return AlphabetTable.ALPHABET_START <= zchar
           && zchar <= AlphabetTable.ALPHABET_END;
  }
  
  /**
   * Performs a shift.
   * 
   * @param shiftChar the shift character
   */
  private void shift(byte shiftChar) {
  
    currentAlphabet = shiftFrom(currentAlphabet, shiftChar);
  }

  /**
   * This method contains the rules to shift the alphabets.
   * 
   * @param alphabet the source alphabet
   * @param shiftChar the shift character
   * @return the resulting alphabet
   */
  private static Alphabet shiftFrom(Alphabet alphabet, byte shiftChar) {
    
    switch (shiftChar) {
      case AlphabetTable.SHIFT_4:
      
        if (alphabet == Alphabet.A0) {
        
          return Alphabet.A1;
        
        } else if (alphabet == Alphabet.A1) {
        
          return Alphabet.A2;
        
        } else if (alphabet == Alphabet.A2) {
        
          return Alphabet.A0;
        }
        break;
      case AlphabetTable.SHIFT_5:
      
        if (alphabet == Alphabet.A0) {
        
          return Alphabet.A2;
        
        } else if (alphabet == Alphabet.A1) {
        
          return Alphabet.A0;
          
        } else if (alphabet == Alphabet.A2) {
        
          return Alphabet.A1;
        }
        break;
      default:        
    }
    return alphabet;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean willEscapeA2(byte zchar) {
   
    return currentAlphabet == Alphabet.A2 && zchar == AlphabetTable.A2_ESCAPE;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isShiftCharacter(byte zchar) {
    
    return alphabetTable.isShiftCharacter(zchar);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(byte zchar) {
    
    return alphabetTable.isAbbreviation(zchar);
  }

  /**
   * {@inheritDoc}
   */
  public AlphabetElement getAlphabetElementFor(short zsciiChar) {
    
    // Special handling for newline !!
    if (zsciiChar == '\n') {
      
      return new AlphabetElement(Alphabet.A2, (short) 7);
    }
    
    Alphabet alphabet = null;
    int zcharCode = alphabetTable.getA0IndexOf(zsciiChar);
    
    if (zcharCode >= 0) alphabet = Alphabet.A0;      
    else {
      
      zcharCode = alphabetTable.getA1IndexOf(zsciiChar);
      if (zcharCode >= 0) alphabet = Alphabet.A1;
      else {
        
        zcharCode = alphabetTable.getA2IndexOf(zsciiChar);
        if (zcharCode >= 0) alphabet = Alphabet.A2;
      }
    }
    
    // Was found in alphabet table, adjust by alphabet start
    if (alphabet != null) {
      
      zcharCode += AlphabetTable.ALPHABET_START;
      
    } else {
      
      // It is not in any alphabet table, we are fine with taking the code
      // number for the moment
      zcharCode = zsciiChar;
    }
    
    return new AlphabetElement(alphabet, (short) zcharCode);
  }  
}
