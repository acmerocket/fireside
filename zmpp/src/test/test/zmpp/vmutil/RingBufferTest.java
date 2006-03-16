/*
 * $Id$
 * 
 * Created on 2006/03/10
 * Copyright 2005-2006 by Wei-ju Wu
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

import org.zmpp.vmutil.RingBuffer;

public class RingBufferTest extends TestCase {

  private RingBuffer<Integer> ringbuffer;
  
  protected void setUp() throws Exception {
    
    ringbuffer = new RingBuffer<Integer>(3);
  }

  public void testInitial() {
    
    assertEquals(0, ringbuffer.size());
  }
  
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
}
