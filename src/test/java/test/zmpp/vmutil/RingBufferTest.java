/*
 * Created on 2006/03/10
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package test.zmpp.vmutil;

import static org.junit.Assert.*;
import org.junit.Before;

import org.junit.Test;
import org.zmpp.vmutil.RingBuffer;

/**
 * Test class for RingBuffer.
 * 
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
