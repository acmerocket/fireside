package org.zmpp.encoding;

public class AlphabetTableV1 extends DefaultAlphabetTable {


  private static final String A2CHARS = " 0123456789.,!?_#'\"/\\<-:()";

  /**
   * {@inheritDoc}
   */
  public short getA2Char(int index) {
    
    return (short) A2CHARS.charAt(index);
  }
  
  /**
   * {@inheritDoc}
   */
  public final int getA2IndexOf(short zsciiChar) {

    return A2CHARS.indexOf(zsciiChar);
  }
  
  
  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(short zchar) {

    // always false
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShiftCharacter(short zchar) {

    return SHIFT_2 <= zchar && zchar <= SHIFT_5;
  }

}
