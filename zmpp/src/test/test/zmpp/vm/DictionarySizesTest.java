/*
 * $Id: DictionaryTest.java 657 2008-07-22 18:11:23Z weiju $
 * 
 * Created on 07/22/2008
 * Copyright 2005-2008 by Wei-ju Wu
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
package test.zmpp.vm;

import org.junit.Test;
import org.zmpp.vm.DictionarySizes;
import org.zmpp.vm.DictionarySizesV1ToV3;
import org.zmpp.vm.DictionarySizesV4ToV8;
import static org.junit.Assert.*;

/**
 * Test class for DictionarySizes.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DictionarySizesTest {
  @Test
  public void testDictionarySizesV4ToV8() {
    DictionarySizes sizes = new DictionarySizesV4ToV8();
    assertEquals(6, sizes.getNumEntryBytes());
    assertEquals(9, sizes.getMaxEntryChars());
  }
  @Test
  public void testDictionarySizesV1ToV3() {
    DictionarySizes sizes = new DictionarySizesV1ToV3();
    assertEquals(4, sizes.getNumEntryBytes());
    assertEquals(6, sizes.getMaxEntryChars());
  }
}
