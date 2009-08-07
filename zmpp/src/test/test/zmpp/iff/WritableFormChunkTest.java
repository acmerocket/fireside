/*
 * Created on 2005/12/06
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
package test.zmpp.iff;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.zmpp.iff.WritableFormChunk;

/**
 * Test class for WritableFormChunk.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class WritableFormChunkTest {

  private WritableFormChunk formChunk;
  
  @Before
  public void setUp() throws Exception {
    formChunk = new WritableFormChunk("IFhd".getBytes());
  }

  @Test
  public void testIsValid() { 
    assertTrue(formChunk.isValid());
    assertNotNull(formChunk.getMemory());
    assertNotNull(formChunk.getSubChunks());
    assertNull(formChunk.getSubChunk(1234));
    assertEquals(0, formChunk.getAddress());
  }
}
