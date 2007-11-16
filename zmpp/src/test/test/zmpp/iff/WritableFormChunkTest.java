/*
 * $Id$
 * 
 * Created on 2005/12/06
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
package test.zmpp.iff;

import org.jmock.MockObjectTestCase;
import org.zmpp.iff.WritableFormChunk;

public class WritableFormChunkTest extends MockObjectTestCase {

  private WritableFormChunk formChunk;
  
  protected void setUp() throws Exception {
    
    formChunk = new WritableFormChunk("IFhd".getBytes());
  }

  public void testIsValid() {
    
    assertTrue(formChunk.isValid());
  }
  
  public void testGetMemoryAccess() {
    
    assertNotNull(formChunk.getMemory());
  }
  
  public void testGetSubChunks() {
    
    assertNotNull(formChunk.getSubChunks());
  }
}
