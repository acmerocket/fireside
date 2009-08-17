/*
 * Created on 2006/01/10
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
package test.zmpp.encoding;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharEncoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.vm.DictionarySizesV4ToV8;

/**
 * This is the test for the encoder class. In general it is not recommended to
 * rely on existing classes for tests, but mocks are not so practical
 * in this case either. We will use a decoder and a DefaultMemoryAccess
 * object to test our implementation instead of mocking these classes.
 * By this means we can instantly verify our result in an easy way.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZCharEncoderTest {

  //private ZCharEncoder encoderV1;
  private ZCharEncoder encoderV4;
  private int sourceAddress = 100;
  private int targetAddress = 199;
  private byte[] data = new byte[206];
  private Memory realmem;
  
  @Before
  public void setUp() throws Exception {
    AlphabetTable alphabetTable = new DefaultAlphabetTable();
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    //encoderV1 = new ZCharEncoder(translator, new DictionarySizesV1ToV3());
    encoderV4 = new ZCharEncoder(translator, new DictionarySizesV4ToV8());
    realmem = new DefaultMemory(data);
  }
  
  /**
   * A single character to be encoded. We need to make sure it is in lower
   * case and the string is padded out with shift characters.
   */
  @Test
  public void testEncodeSingleCharacter() {
    int length = 1;
    
    // we expect to have an end word, padded out with shift 5's
    data[sourceAddress] = (byte) 'a'; // Encode an 'a'
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // 'a' + 2 pad
    assertEquals(0x18a5, realmem.readUnsigned16(targetAddress));
    
    // Test that the rest is padded and marked with the end bit
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  @Test
  public void testEncodeTwoCharacters() {
    
    int length = 2;    
    data[sourceAddress] = (byte) 'a';
    data[sourceAddress + 1] = (byte) 'b';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // 'ab' + pad
    assertEquals(0x18e5, realmem.readUnsigned16(targetAddress));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  @Test
  public void testEncode4Characters() {
    
    int length = 4;
    data[sourceAddress] = (byte) 'a';
    data[sourceAddress + 1] = (byte) 'b';
    data[sourceAddress + 2] = (byte) 'c';
    data[sourceAddress + 3] = (byte) 'd';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // 'abc'
    assertEquals(0x18e8, realmem.readUnsigned16(targetAddress));
    
    // 'd' + 2 pads
    assertEquals(0x24a5, realmem.readUnsigned16(targetAddress + 2));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));    
  }
  
  // Test with a different alphabet
  @Test
  public void testEncodeAlphabet1() {
    
    int length = 1;
    data[sourceAddress] = (byte) 'A';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // Shift-4 + 'a' + Pad
    assertEquals(0x10c5, realmem.readUnsigned16(targetAddress));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  @Test
  public void testEncodeAlphabet1SpanWordBound() {
    
    int length = 3;
    data[sourceAddress] = (byte) 'a';
    data[sourceAddress + 1] = (byte) 'b';
    data[sourceAddress + 2] = (byte) 'C';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // 'ab' + Shift 4
    assertEquals(0x18e4, realmem.readUnsigned16(targetAddress));
    
    // 'c'
    assertEquals(0x20a5, realmem.readUnsigned16(targetAddress + 2));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  @Test
  public void testEncodeAlphabet2SpanWordBound() {
    
    int length = 3;
    data[sourceAddress] = (byte) 'a';
    data[sourceAddress + 1] = (byte) 'b';
    data[sourceAddress + 2] = (byte) '3';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // 'ab' + Shift 5
    assertEquals(0x18e5, realmem.readUnsigned16(targetAddress));
    
    // '3'
    assertEquals(0x2ca5, realmem.readUnsigned16(targetAddress + 2));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  // Encoding of special characters in the unicode has to work.
  // We test this on our favorite character: '@'
  // Do not forget the testing across word boundaries
  //
  // How are characters handled that are larger than a byte ?
  // See how Frotz handles this
  @Test
  public void testEncodeEscapeA2() {
    
    int length = 1;
    data[sourceAddress] = (byte) '@';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // Tricky, tricky (and memory-inefficient)
    // Shift-5 + 6 + '@' (64), encoded in 10 bit, the upper half contains 2
    assertEquals(0x14c2, realmem.readUnsigned16(targetAddress));
    
    // the lower half contains 0 + 2 pads
    assertEquals(0x00a5, realmem.readUnsigned16(targetAddress + 2));    

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  // For triangulation, we use another character (126)
  @Test
  public void testEncodeEscapeA2Tilde() {
    
    int length = 1;
    data[sourceAddress] = (byte) '~';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // Tricky, tricky (and memory-inefficient)
    // Shift-5 + 6 + '~' (126), encoded in 10 bit, the upper half contains 3
    assertEquals(0x14c3, realmem.readUnsigned16(targetAddress));
    
    // the lower half contains 30 + 2 pads
    assertEquals(0x78a5, realmem.readUnsigned16(targetAddress + 2));    

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }  

  @Test
  public void testEncodeEscapeA2TildeSpansWord() {
    
    int length = 2;
    data[sourceAddress] = (byte) 'a';
    data[sourceAddress + 1] = (byte) '~';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // Tricky, tricky (and memory-inefficient)
    // 'a' + Shift-5 + 6
    assertEquals(0x18a6, realmem.readUnsigned16(targetAddress));
    
    // both halfs of '~' + 1 pad
    assertEquals(0x0fc5, realmem.readUnsigned16(targetAddress + 2));    

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  // We test a situation where the 6 bytes are exceeded by the 9 source
  // characters. In practice, this happens, when there are characters
  // in the source buffer that need to be escaped, since they take the
  // space of 4 lower case characters, which means that one special character
  // can be combined with 5 lower case characters
  @Test
  public void testEncodeCharExceedsTargetBuffer() {
    
    // Situation 1: there are lower case letters at the end, we need
    // to ensure that the dictionary is cropped and the characters
    // that exceed the buffer are ommitted
    int length = 7;
    data[sourceAddress] = '@';
    data[sourceAddress + 1] = 'a';
    data[sourceAddress + 2] = 'b';
    data[sourceAddress + 3] = 'c';
    data[sourceAddress + 4] = 'd';
    data[sourceAddress + 5] = 'e';
    data[sourceAddress + 6] = 'f';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // Shift-5 + 6 + '@' (64), encoded in 10 bit, the upper half contains 2
    assertEquals(0x14c2, realmem.readUnsigned16(targetAddress));
    
    // the lower half contains 0, 'ab'
    assertEquals(0x00c7, realmem.readUnsigned16(targetAddress + 2));
    
    // 'cde' + end bit
    assertEquals(0xa12a, realmem.readUnsigned16(targetAddress + 4));    
  }
  
  @Test
  public void testEncodeCharExceedsTargetBufferEscapeAtEnd() {
    
    // Situation 2: in this case the escaped character is at the end,
    // so we need to ommit that escape sequence completely, padding
    // out the rest of the string
    int length = 7;
    data[sourceAddress] = 'a';
    data[sourceAddress + 1] = 'b';
    data[sourceAddress + 2] = 'c';
    data[sourceAddress + 3] = 'd';
    data[sourceAddress + 4] = 'e';
    data[sourceAddress + 5] = 'f';
    data[sourceAddress + 6] = '@';
    
    encoderV4.encode(realmem, sourceAddress, length, targetAddress);
    
    // 'abc'
    assertEquals(0x18e8, realmem.readUnsigned16(targetAddress));
    
    // 'def'
    assertEquals(0x254b, realmem.readUnsigned16(targetAddress + 2));
    
    // not long enough, pad it out
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  // **********************************************************************
  // ***** encode() with source String
  // **********************************************************************

  @Test
  public void testEncodeStringSingleCharacter() {
    // we expect to have an end word, padded out with shift 5's
    encoderV4.encode("a", realmem, targetAddress);
    
    // 'a' + 2 pad
    assertEquals(0x18a5, realmem.readUnsigned16(targetAddress));
    
    // Test that the rest is padded and marked with the end bit
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  @Test
  public void testEncodeStringTwoCharacters() {
    
    encoderV4.encode("ab", realmem, targetAddress);
    
    // 'ab' + pad
    assertEquals(0x18e5, realmem.readUnsigned16(targetAddress));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  @Test
  public void testEncodeString4Characters() {    
    encoderV4.encode("abcd", realmem, targetAddress);
    
    // 'abc'
    assertEquals(0x18e8, realmem.readUnsigned16(targetAddress));
    
    // 'd' + 2 pads
    assertEquals(0x24a5, realmem.readUnsigned16(targetAddress + 2));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));    
  }

  // Test with a different alphabet
  @Test
  public void testEncodeStringAlphabet1() {
    encoderV4.encode("A", realmem, targetAddress);
    
    // 'a' + Pad
    assertEquals(0x18a5, realmem.readUnsigned16(targetAddress));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  @Test
  public void testEncodeStringAlphabet1SpanWordBound() {
    encoderV4.encode("abC", realmem, targetAddress);
    
    // 'abc'
    assertEquals(0x18e8, realmem.readUnsigned16(targetAddress));
    
    // pad
    assertEquals(0x14a5, realmem.readUnsigned16(targetAddress + 2));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  @Test
  public void testEncodeStringAlphabet2SpanWordBound() {
    encoderV4.encode("ab3", realmem, targetAddress);
    
    // 'ab' + Shift 5
    assertEquals(0x18e5, realmem.readUnsigned16(targetAddress));
    
    // '3'
    assertEquals(0x2ca5, realmem.readUnsigned16(targetAddress + 2));

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  // Encoding of special characters in the unicode has to work.
  // We test this on our favorite character: '@'
  // Do not forget the testing across word boundaries
  @Test
  public void testEncodeStringEscapeA2() {    
    encoderV4.encode("@", realmem, targetAddress);
    
    // Tricky, tricky (and memory-inefficient)
    // Shift-5 + 6 + '@' (64), encoded in 10 bit, the upper half contains 2
    assertEquals(0x14c2, realmem.readUnsigned16(targetAddress));
    
    // the lower half contains 0 + 2 pads
    assertEquals(0x00a5, realmem.readUnsigned16(targetAddress + 2));    

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }

  // For triangulation, we use another character (126)
  @Test
  public void testEncodeStringEscapeA2Tilde() {
    encoderV4.encode("~", realmem, targetAddress);
    
    // Tricky, tricky (and memory-inefficient)
    // Shift-5 + 6 + '~' (126), encoded in 10 bit, the upper half contains 3
    assertEquals(0x14c3, realmem.readUnsigned16(targetAddress));
    
    // the lower half contains 30 + 2 pads
    assertEquals(0x78a5, realmem.readUnsigned16(targetAddress + 2));    

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }  

  @Test
  public void testEncodeStringEscapeA2TildeSpansWord() {
    encoderV4.encode("a~", realmem, targetAddress);
    
    // Tricky, tricky (and memory-inefficient)
    // 'a' + Shift-5 + 6
    assertEquals(0x18a6, realmem.readUnsigned16(targetAddress));
    
    // both halfs of '~' + 1 pad
    assertEquals(0x0fc5, realmem.readUnsigned16(targetAddress + 2));    

    // Test that the rest is padded and marked with the end bit
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
  
  // We test a situation where the 6 bytes are exceeded by the 9 source
  // characters. In practice, this happens, when there are characters
  // in the source buffer that need to be escaped, since they take the
  // space of 4 lower case characters, which means that one special character
  // can be combined with 5 lower case characters
  @Test
  public void testEncodeStringCharExceedsTargetBuffer() {
    // Situation 1: there are lower case letters at the end, we need
    // to ensure that the dictionary is cropped and the characters
    // that exceed the buffer are ommitted
    encoderV4.encode("@abcdef", realmem, targetAddress);
    
    // Shift-5 + 6 + '@' (64), encoded in 10 bit, the upper half contains 2
    assertEquals(0x14c2, realmem.readUnsigned16(targetAddress));
    
    // the lower half contains 0, 'ab'
    assertEquals(0x00c7, realmem.readUnsigned16(targetAddress + 2));
    
    // 'cde' + end bit
    assertEquals(0xa12a, realmem.readUnsigned16(targetAddress + 4));    
  }
  
  @Test
  public void testEncodeStringCharExceedsTargetBufferEscapeAtEnd() {
    // Situation 2: in this case the escaped character is at the end,
    // so we need to ommit that escape sequence completely, padding
    // out the rest of the string
    encoderV4.encode("abcdef@", realmem, targetAddress);
    
    // 'abc'
    assertEquals(0x18e8, realmem.readUnsigned16(targetAddress));
    
    // 'def'
    assertEquals(0x254b, realmem.readUnsigned16(targetAddress + 2));
    
    // not long enough, pad it out
    assertEquals(0x94a5, realmem.readUnsigned16(targetAddress + 4));
  }
/*
  // Just for debugging purposes
  @Test
  public void testEncodeStringLikeDictionaryV1() {
    encodeAndPrintV1("wooden");
    encodeAndPrintV1("winding");
    encodeAndPrintV1("Y");
  }

  @Test
  public void testEncodeStringLikeDictionaryV4() {
    encodeAndPrintV4("y");
    encodeAndPrintV4("weather");
    encodeAndPrintV4("weedkiller");
  }

  private void encodeAndPrintV1(String str) {
    encoderV1.encode(str, realmem, targetAddress);
    System.out.printf("str = '%s' { %02x, %02x, %02x, %02x }\n", str,
        (int) realmem.readUnsigned8(targetAddress),
        (int) realmem.readUnsigned8(targetAddress + 1),
        (int) realmem.readUnsigned8(targetAddress + 2),
        (int) realmem.readUnsigned8(targetAddress + 3)
        );
  }
  private void encodeAndPrintV4(String str) {
    encoderV4.encode(str, realmem, targetAddress);
    System.out.printf("str = '%s' { %02x, %02x, %02x, %02x, %02x, %02x }\n", str,
        (int) realmem.readUnsigned8(targetAddress),
        (int) realmem.readUnsigned8(targetAddress + 1),
        (int) realmem.readUnsigned8(targetAddress + 2),
        (int) realmem.readUnsigned8(targetAddress + 3),
        (int) realmem.readUnsigned8(targetAddress + 4),
        (int) realmem.readUnsigned8(targetAddress + 5)
        );
  }
  */
}
