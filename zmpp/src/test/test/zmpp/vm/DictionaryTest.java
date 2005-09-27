/*
 * $Id$
 * 
 * Created on 24.09.2005
 * Copyright 2005 by Wei-ju Wu
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
package test.zmpp.vm;

import org.zmpp.vm.Dictionary;

/**
 * This class tests the dictionary view.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DictionaryTest extends MemoryMapSetup {

  private Dictionary dictionary;
  
  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    super.setUp();
    dictionary = new Dictionary(minizorkmap,
                                fileheader.getDictionaryAddress(), converter);
  }
  
  public void testDictionaryInformation() {
    assertEquals(3, dictionary.getNumberOfSeparators());
    assertEquals(7, dictionary.getEntryLength());
    assertEquals(536, dictionary.getNumberOfEntries());

    assertEquals(".", dictionary.getEntryString(1));
    assertEquals(",", dictionary.getEntryString(2));
    assertEquals("#comm", dictionary.getEntryString(3));
    assertEquals("again", dictionary.getEntryString(13));
    assertEquals("air-p", dictionary.getEntryString(15));
    assertEquals("$ve", dictionary.getEntryString(0));
  }
  
  public void testGetSeparator() {
    
    //System.out.println("0: " + dictionary.getSeparator(0));
    //assertEquals('.', dictionary.getSeparator(0));
    assertEquals('.', dictionary.getSeparator(1));
    assertEquals(',', dictionary.getSeparator(2));
  }
}
