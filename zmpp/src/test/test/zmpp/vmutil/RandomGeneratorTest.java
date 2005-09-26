/*
 * $Id$
 * 
 * Created on 2005/09/23
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
package test.zmpp.vmutil;

import junit.framework.TestCase;

import org.zmpp.vmutil.PredictableRandomGenerator;
import org.zmpp.vmutil.RandomGenerator;
import org.zmpp.vmutil.UnpredictableRandomGenerator;

/**
 * This class is a test for the RandomGenerator classes.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class RandomGeneratorTest extends TestCase {

  RandomGenerator predictable1, predictable2, random1, random2;

  protected void setUp() throws Exception {
    
    long seed = 4711;
    predictable1 = new PredictableRandomGenerator(seed);
    predictable2 = new PredictableRandomGenerator(seed);
    random1 = new UnpredictableRandomGenerator();
    random2 = new UnpredictableRandomGenerator();
  }

  public void testUnpredictableRandomSequence() {
 
    int rnd1 = random1.next();
    int rnd2 = random1.next();
    assertNotSame(rnd1, rnd2);
    assertTrue(1 <= rnd1 && rnd1 <= RandomGenerator.MAX_VALUE);
  }
  
  public void testUnpredictableRandomDifferentSequences() {
   
    int rnd11 = random1.next();
    int rnd12 = random1.next();
    int rnd21 = random2.next();
    int rnd22 = random2.next();
    
    assertNotSame(rnd11, rnd12);
    assertNotSame(rnd21, rnd22);
  }
  
  public void testPredictableRandomSequence() {
    
    int rnd1 = predictable1.next();
    int rnd2 = predictable1.next();
    assertNotSame(rnd1, rnd2);
    assertTrue(1 <= rnd1 && rnd1 <= RandomGenerator.MAX_VALUE);
  }
  
  public void testPredictableSameSequences() {
    
    int rnd11 = predictable1.next();
    int rnd12 = predictable1.next();
    int rnd21 = predictable2.next();
    int rnd22 = predictable2.next();
    assertEquals(rnd11, rnd21);
    assertEquals(rnd12, rnd22);
  }
}
