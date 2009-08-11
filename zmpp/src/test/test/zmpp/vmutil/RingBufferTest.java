/*
 * Created on 2006/03/10
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
package test.zmpp.vmutil;

import static org.junit.Assert.*;
import org.junit.Before;

import org.junit.Test;
import org.zmpp.vmutil.RingBuffer;

/**
 * Test class for RingBuffer.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class RingBufferTest {

  private RingBuffer<Integer> ringbuffer;
  
  @Before
  public void setUp() throws Exception {
    ringbuffer = new RingBuffer<Integer>(3);
  }

  @Test
  public void testInitial() {
    assertEquals(0, ringbuffer.size());
  }
  
  @Test
  public void testAddElementNormal() {
    ringbuffer.add(1);
    assertEquals(1, ringbuffer.size());
    assertEquals(new Integer(1), ringbuffer.get(0));

    ringbuffer.add(2);
    assertEquals(2, ringbuffer.size());
    assertEquals(new Integer(2), ringbuffer.get(1));
    
    ringbuffer.add(3);
    assertEquals(3, ringbuffer.size());
    assertEquals(new Integer(3), ringbuffer.get(2));
    
    ringbuffer.set(1, 5);
    assertEquals(3, ringbuffer.size());
    assertEquals(new Integer(5), ringbuffer.get(1));
  }

  @Test
  public void testAddElementOverflow() {
    // fill it up to the limit
    ringbuffer.add(1);
    ringbuffer.add(2);
    ringbuffer.add(3);
    
    // now add one more, the 1 should be gone
    ringbuffer.add(4);
    assertEquals(3, ringbuffer.size());
    assertEquals(new Integer(4), ringbuffer.get(2));
    
    ringbuffer.set(0, 7);
    assertEquals(new Integer(7), ringbuffer.get(0));
  }
  
  @Test
  public void testRemoveNormal() {
    ringbuffer.add(1);
    ringbuffer.add(2);
    Integer elem = ringbuffer.remove(1);
    assertEquals(1, ringbuffer.size());
    assertEquals(new Integer(2), elem);
    
    ringbuffer.add(3);
    assertEquals(2, ringbuffer.size());
    assertEquals(new Integer(3), ringbuffer.get(1));
  }
  
  @Test
  public void testRemoveOverflow() {  
    // fill it over the limit
    ringbuffer.add(1);
    ringbuffer.add(2);
    ringbuffer.add(3);
    ringbuffer.add(4);
    
    // contains 2, 3, 4 now
    ringbuffer.remove(1);
    
    // contains 2, 4 now
    assertEquals(2, ringbuffer.size());
    assertEquals(new Integer(2), ringbuffer.get(0));
    assertEquals(new Integer(4), ringbuffer.get(1));
  }

  /**
   * A more sophisticated test that checks whether internal bounds are
   * correctly adjusted.
   */
  @Test
  public void testRemoveTooManyAndReadd() {
  	// overflow the ring buffer
  	ringbuffer.add(1);
  	ringbuffer.add(2);
  	ringbuffer.add(3);
  	ringbuffer.add(4);
  	
  	// underflow the ring buffer
  	ringbuffer.remove(0);
  	ringbuffer.remove(0);
  	ringbuffer.remove(0);
  	ringbuffer.remove(0);
  	
  	// size should be 0
  	assertEquals(0, ringbuffer.size());
  	
  	// adding should work
  	ringbuffer.add(5);
  	ringbuffer.add(6);
  	assertEquals(2, ringbuffer.size());
  	assertEquals(5, ringbuffer.get(0).intValue());
  	assertEquals(6, ringbuffer.get(1).intValue());
  }
  
  @Test
  public void testToString() {
    ringbuffer.add(1);
    ringbuffer.add(2);
    assertEquals("{ 1, 2 }", ringbuffer.toString());
  }
}
