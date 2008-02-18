/*
 * $Id$
 *
 * Copyright 2005-2007 by Wei-ju Wu
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
package test.zmpp.encoding;

import junit.framework.TestCase;

import org.zmpp.encoding.AlphabetElement;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV2;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.AlphabetTable.Alphabet;

public class ZCharTranslatorTest extends TestCase {

  private AlphabetTable alphabetTable;
  private ZCharTranslator translator;
  
  private AlphabetTable alphabetTableV2;
  private ZCharTranslator translatorV2;
  
  protected void setUp() {
    
    alphabetTable = new DefaultAlphabetTable();
    translator = new DefaultZCharTranslator(alphabetTable);
    alphabetTableV2 = new AlphabetTableV2();
    translatorV2 = new DefaultZCharTranslator(alphabetTableV2);
  }
  
  public void testTranslate() {
    
    // Unknown
    assertEquals('?', translator.translate((char) 255));

    // alphabet 0
    assertEquals('a', translator.translate((char) 6));
    
    // Alphabet 1
    translator.translate((char) AlphabetTable.SHIFT_4);
    assertEquals('C', translator.translate((char) 8));
    
    // Alphabet 2
    translator.translate((char) AlphabetTable.SHIFT_5);
    assertEquals('2', translator.translate((char) 10));

    // Alphabet 2, NEWLINE
    translator.translate((char) AlphabetTable.SHIFT_5);
    assertEquals('\n', translator.translate((char) 7));
  }
  
  public void test0IsSpace() {
    
    assertEquals(' ', translator.translate((char) 0));
    translator.translate((char) AlphabetTable.SHIFT_4);
    assertEquals(' ', translator.translate((char) 0));
    translator.translate((char) AlphabetTable.SHIFT_5);
    assertEquals(' ', translator.translate((char) 0));
    
    assertEquals(' ', translatorV2.translate((char) 0));
    translatorV2.translate((char) AlphabetTable.SHIFT_4);
    assertEquals(' ', translatorV2.translate((char) 0));
    translatorV2.translate((char) AlphabetTable.SHIFT_5);
    assertEquals(' ', translatorV2.translate((char) 0));
  }

  // **********************************************************************
  // ***** Shift
  // ******************************************
  
  public void testShiftFromA0() {
    
    char c = translator.translate((char) AlphabetTable.SHIFT_4);
    assertEquals('\0', c);
    assertEquals(Alphabet.A1, translator.getCurrentAlphabet());
    
    translator.reset();
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());

    c = translator.translate((char) AlphabetTable.SHIFT_5);
    assertEquals('\0', c);
    assertEquals(Alphabet.A2, translator.getCurrentAlphabet());    
  }
  
  public void testShiftFromA1() {

    // Switch to A1
    char c = translator.translate((char) AlphabetTable.SHIFT_4);
    
    c = translator.translate((char) AlphabetTable.SHIFT_4);
    assertEquals('\0', c);
    assertEquals(Alphabet.A2, translator.getCurrentAlphabet());
    
    // Switch to A1 again
    translator.reset();
    c = translator.translate((char) AlphabetTable.SHIFT_4);
    
    c = translator.translate((char) AlphabetTable.SHIFT_5);
    assertEquals('\0', c);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
  }
  
  public void testShiftFromA2() {

    // Switch to A2
    char c = translator.translate((char) AlphabetTable.SHIFT_5);
    
    c = translator.translate((char) AlphabetTable.SHIFT_4);
    assertEquals('\0', c);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
    
    // Switch to A2 again
    translator.reset();
    c = translator.translate((char) AlphabetTable.SHIFT_5);
    
    c = translator.translate((char) AlphabetTable.SHIFT_5);
    assertEquals('\0', c);
    assertEquals(Alphabet.A1, translator.getCurrentAlphabet());
  }
  
  /**
   * The default alphabet table should reset to A0 after retrieving a
   * code. 
   */
  public void testImplicitReset() {
    
    translator.translate((char) AlphabetTable.SHIFT_4);
    translator.translate((char) 7);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
    
    translator.translate((char) AlphabetTable.SHIFT_5);
    translator.translate((char) 7);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
  }  

  public void testGetAlphabetElement() {

    // Alphabet A0
    AlphabetElement elem1 = translator.getAlphabetElementFor('c');
    assertEquals(Alphabet.A0, elem1.getAlphabet());
    assertEquals(8, elem1.getZCharCode());

    AlphabetElement elem1b = translator.getAlphabetElementFor('a');
    assertEquals(Alphabet.A0, elem1b.getAlphabet());
    assertEquals(6, elem1b.getZCharCode());
    
    AlphabetElement elem2 = translator.getAlphabetElementFor('d');
    assertEquals(Alphabet.A0, elem2.getAlphabet());
    assertEquals(9, elem2.getZCharCode());

    // Alphabet A1
    AlphabetElement elem3 = translator.getAlphabetElementFor('C');
    assertEquals(Alphabet.A1, elem3.getAlphabet());
    assertEquals(8, elem3.getZCharCode());

    // Alphabet A2
    AlphabetElement elem4 = translator.getAlphabetElementFor('#');
    assertEquals(Alphabet.A2, elem4.getAlphabet());
    assertEquals(23, elem4.getZCharCode());
    
    // ZSCII code
    AlphabetElement elem5 = translator.getAlphabetElementFor('@');
    assertEquals(null, elem5.getAlphabet());
    assertEquals(64, elem5.getZCharCode());
    
    // Newline is tricky, this is always A2/7 !!!
    AlphabetElement newline = translator.getAlphabetElementFor('\n');
    assertEquals(Alphabet.A2, newline.getAlphabet());
    assertEquals(7, newline.getZCharCode());
  }  

  // **********************************************************************
  // ***** Shifting in V2
  // ******************************************
  
  public void testShiftV2FromA0() {
    
    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_2));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    
    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_4));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();

    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_3));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
    translatorV2.reset();

    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_5));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
  }
  
  public void testShiftV2FromA1() {
    
    translatorV2.translate((char) AlphabetTable.SHIFT_2);
    
    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_2));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate((char) AlphabetTable.SHIFT_2);
    
    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_4));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate((char) AlphabetTable.SHIFT_2);

    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_3));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate((char) AlphabetTable.SHIFT_2);

    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_5));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
  }

  public void testShiftV2FromA2() {
    
    translatorV2.translate((char) AlphabetTable.SHIFT_3);
    
    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_2));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate((char) AlphabetTable.SHIFT_3);
    
    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_4));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate((char) AlphabetTable.SHIFT_3);

    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_3));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate((char) AlphabetTable.SHIFT_3);

    assertEquals(0, translatorV2.translate((char) AlphabetTable.SHIFT_5));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
  }

  public void testShiftNotLocked() {

    translatorV2.translate((char) AlphabetTable.SHIFT_2);
    translatorV2.translate((char) 10);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
   
    translatorV2.translate((char) AlphabetTable.SHIFT_3);
    translatorV2.translate((char) 10);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
  }
  
  public void testShiftNotLockedChar0() {
        
    translatorV2.translate((char) AlphabetTable.SHIFT_2);
    translatorV2.translate((char) 0);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
   
    translatorV2.translate((char) AlphabetTable.SHIFT_3);
    translatorV2.translate((char) 0);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
  }

  public void testShiftLocked() {
    
    translatorV2.translate((char) AlphabetTable.SHIFT_4);
    translatorV2.translate((char) 10);
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    
    translatorV2.translate((char) AlphabetTable.SHIFT_5);
    translatorV2.translate((char) 10);
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
  }

  /**
   * Test if the shift lock is reset after the a non-locking shift was
   * met.
   */
  public void testShiftLockSequenceLock1() {
    
    translatorV2.translate((char) AlphabetTable.SHIFT_4);
    translatorV2.translate((char) AlphabetTable.SHIFT_2);
    translatorV2.translate((char) 10);
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
  }
  
  public void testShiftLockSequenceLock2() {
    
    translatorV2.translate((char) AlphabetTable.SHIFT_5);
    translatorV2.translate((char) AlphabetTable.SHIFT_3);
    translatorV2.translate((char) 10);
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
  }
}
