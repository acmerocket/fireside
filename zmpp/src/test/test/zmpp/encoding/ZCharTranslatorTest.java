/**
 * $Id$
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
    assertEquals('?', translator.translate((short) 255));

    // alphabet 0
    assertEquals('a', translator.translate((short) 6));
    
    // Alphabet 1
    translator.translate(AlphabetTable.SHIFT_4);
    assertEquals('C', translator.translate((short) 8));
    
    // Alphabet 2
    translator.translate(AlphabetTable.SHIFT_5);
    assertEquals('2', translator.translate((short) 10));

    // Alphabet 2, NEWLINE
    translator.translate(AlphabetTable.SHIFT_5);
    assertEquals('\n', translator.translate((short) 7));
  }
  
  public void test0IsSpace() {
    
    assertEquals(' ', translator.translate((short) 0));
    translator.translate(AlphabetTable.SHIFT_4);
    assertEquals(' ', translator.translate((short) 0));
    translator.translate(AlphabetTable.SHIFT_5);
    assertEquals(' ', translator.translate((short) 0));
  }
  
  // **********************************************************************
  // ***** Shift
  // ******************************************
  
  public void testShiftFromA0() {
    
    char c = translator.translate(AlphabetTable.SHIFT_4);
    assertEquals('\0', c);
    assertEquals(Alphabet.A1, translator.getCurrentAlphabet());
    
    translator.reset();
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());

    c = translator.translate(AlphabetTable.SHIFT_5);
    assertEquals('\0', c);
    assertEquals(Alphabet.A2, translator.getCurrentAlphabet());    
  }
  
  public void testShiftFromA1() {

    // Switch to A1
    char c = translator.translate(AlphabetTable.SHIFT_4);
    
    c = translator.translate(AlphabetTable.SHIFT_4);
    assertEquals('\0', c);
    assertEquals(Alphabet.A2, translator.getCurrentAlphabet());
    
    // Switch to A1 again
    translator.reset();
    c = translator.translate(AlphabetTable.SHIFT_4);
    
    c = translator.translate(AlphabetTable.SHIFT_5);
    assertEquals('\0', c);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
  }
  
  public void testShiftFromA2() {

    // Switch to A2
    char c = translator.translate(AlphabetTable.SHIFT_5);
    
    c = translator.translate(AlphabetTable.SHIFT_4);
    assertEquals('\0', c);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
    
    // Switch to A2 again
    translator.reset();
    c = translator.translate(AlphabetTable.SHIFT_5);
    
    c = translator.translate(AlphabetTable.SHIFT_5);
    assertEquals('\0', c);
    assertEquals(Alphabet.A1, translator.getCurrentAlphabet());
  }
  
  /**
   * The default alphabet table should reset to A0 after retrieving a
   * code. 
   */
  public void testImplicitReset() {
    
    translator.translate(AlphabetTable.SHIFT_4);
    translator.translate((short) 7);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
    
    translator.translate(AlphabetTable.SHIFT_5);
    translator.translate((short) 7);
    assertEquals(Alphabet.A0, translator.getCurrentAlphabet());
  }  

  public void testGetAlphabetElement() {

    // Alphabet A0
    AlphabetElement elem1 = translator.getAlphabetElementFor((short) 'c');
    assertEquals(Alphabet.A0, elem1.getAlphabet());
    assertEquals(8, elem1.getZCharCode());

    AlphabetElement elem1b = translator.getAlphabetElementFor((short) 'a');
    assertEquals(Alphabet.A0, elem1b.getAlphabet());
    assertEquals(6, elem1b.getZCharCode());
    
    AlphabetElement elem2 = translator.getAlphabetElementFor((short) 'd');
    assertEquals(Alphabet.A0, elem2.getAlphabet());
    assertEquals(9, elem2.getZCharCode());

    // Alphabet A1
    AlphabetElement elem3 = translator.getAlphabetElementFor((short) 'C');
    assertEquals(Alphabet.A1, elem3.getAlphabet());
    assertEquals(8, elem3.getZCharCode());

    // Alphabet A2
    AlphabetElement elem4 = translator.getAlphabetElementFor((short) '#');
    assertEquals(Alphabet.A2, elem4.getAlphabet());
    assertEquals(23, elem4.getZCharCode());
    
    // ZSCII code
    AlphabetElement elem5 = translator.getAlphabetElementFor((short) '@');
    assertEquals(null, elem5.getAlphabet());
    assertEquals(64, elem5.getZCharCode());
    
    // Newline is tricky, this is always A2/7 !!!
    AlphabetElement newline = translator.getAlphabetElementFor((short) '\n');
    assertEquals(Alphabet.A2, newline.getAlphabet());
    assertEquals(7, newline.getZCharCode());
  }  

  // **********************************************************************
  // ***** Shifting in V2
  // ******************************************
  
  public void testShiftV2FromA0() {
    
    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_2));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    
    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_4));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();

    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_3));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
    translatorV2.reset();

    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_5));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
  }
  
  public void testShiftV2FromA1() {
    
    translatorV2.translate(AlphabetTable.SHIFT_2);
    
    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_2));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate(AlphabetTable.SHIFT_2);
    
    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_4));
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate(AlphabetTable.SHIFT_2);

    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_3));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate(AlphabetTable.SHIFT_2);

    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_5));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
  }

  public void testShiftV2FromA2() {
    
    translatorV2.translate(AlphabetTable.SHIFT_3);
    
    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_2));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate(AlphabetTable.SHIFT_3);
    
    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_4));
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate(AlphabetTable.SHIFT_3);

    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_3));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    translatorV2.translate(AlphabetTable.SHIFT_3);

    assertEquals(0, translatorV2.translate(AlphabetTable.SHIFT_5));
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
  }

  public void testShiftNotLocked() {

    translatorV2.translate(AlphabetTable.SHIFT_2);
    translatorV2.translate((short) 10);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
   
    translatorV2.translate(AlphabetTable.SHIFT_3);
    translatorV2.translate((short) 10);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
  }

  public void testShiftLocked() {
    
    translatorV2.translate(AlphabetTable.SHIFT_4);
    translatorV2.translate((short) 10);
    assertEquals(Alphabet.A1, translatorV2.getCurrentAlphabet());
    translatorV2.reset();
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    
    translatorV2.translate(AlphabetTable.SHIFT_5);
    translatorV2.translate((short) 10);
    assertEquals(Alphabet.A2, translatorV2.getCurrentAlphabet());
  }

  /**
   * Test if the shift lock is reset after the a non-locking shift was
   * met.
   */
  public void testShiftLockSequence() {
    
    translatorV2.translate(AlphabetTable.SHIFT_4);
    translatorV2.translate(AlphabetTable.SHIFT_2);
    translatorV2.translate((short) 10);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
    
    translatorV2.translate(AlphabetTable.SHIFT_4);
    translatorV2.translate(AlphabetTable.SHIFT_5);
    translatorV2.translate(AlphabetTable.SHIFT_2);
    translatorV2.translate(AlphabetTable.SHIFT_3);
    translatorV2.translate((short) 10);
    assertEquals(Alphabet.A0, translatorV2.getCurrentAlphabet());
  }
}