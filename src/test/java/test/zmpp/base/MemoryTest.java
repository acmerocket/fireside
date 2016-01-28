/*
 * Created on 2005/09/23
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
package test.zmpp.base;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import static org.junit.Assert.*;

/**
 * This class is a test for the Memory class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MemoryTest {

	private Memory memory;
	private byte[] data = { 0x03, 0x00, 0x37, 0x09, (byte) 0xff, (byte) 0xff };

	@Before
	public void setUp() throws Exception {
		memory = new DefaultMemory(data);
	}

	@Test
	public void testReadUnsignedByte() {
		assertEquals(3, memory.readUnsigned8(0x00));
	}

	@Test
	public void testReadUnsignedWord() {
		assertEquals(0x3709, memory.readUnsigned16(0x02));
	}

	@Test
	public void testGetUnsignedShortGeneral() {
		assertEquals(0xffff, memory.readUnsigned16(0x04));
		assertNotSame(-1, memory.readUnsigned16(0x04));
	}

	@Test
	public void testWriteUnsignedByte() {
		memory.writeUnsigned8(0x02, (char) 0xff);
		assertEquals(0xff, memory.readUnsigned8(0x02));

		memory.writeUnsigned8(0x03, (char) 0x32);
		assertEquals(0x32, memory.readUnsigned8(0x03));
	}

	@Test
	public void testWriteUnsignedShort() {
		memory.writeUnsigned16(0x02, (char) 0xffff);
		assertEquals(0xffff, memory.readUnsigned16(0x02));
		memory.writeUnsigned16(0x04, (char) 0x00ff);
		assertEquals(0x00ff, memory.readUnsigned16(0x04));
	}

	@Test
	public void testCopyBytesToArray() {
		byte[] dstData = new byte[4];
		int dstOffset = 1;
		int srcOffset = 2;
		int numBytes = 3;
		memory.copyBytesToArray(dstData, dstOffset, srcOffset, numBytes);
		assertEquals(0x37, dstData[1]);
		assertEquals(0x09, dstData[2]);
		assertEquals((byte) 0xff, dstData[3]);
	}

	@Test
	public void testCopyBytesFromArray() {
		byte[] srcData = { (byte) 0x00, (byte) 0xef, (byte) 0x10, (byte) 0xfe };
		int srcOffset = 1;
		int dstOffset = 0;
		int numBytes = 3;
		memory.copyBytesFromArray(srcData, srcOffset, dstOffset, numBytes);
		assertEquals(0xef, memory.readUnsigned8(0));
		assertEquals(0x10, memory.readUnsigned8(1));
		assertEquals(0xfe, memory.readUnsigned8(2));
	}

	@Test
	public void testCopyBytesFromMemory() {
		byte[] dstData = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] srcData = { (byte) 0x00, (byte) 0xef, (byte) 0x10, (byte) 0xfe };
		Memory srcMem = new DefaultMemory(srcData);
		Memory dstMem = new DefaultMemory(dstData);
		int srcOffset = 1;
		int dstOffset = 0;
		int numBytes = 3;
		dstMem.copyBytesFromMemory(srcMem, srcOffset, dstOffset, numBytes);
		assertEquals(0xef, dstMem.readUnsigned8(0));
		assertEquals(0x10, dstMem.readUnsigned8(1));
		assertEquals(0xfe, dstMem.readUnsigned8(2));
	}

	@Test
	public void testCopyArea() {
		memory.copyArea(0, 2, 3);
		assertEquals(0x03, memory.readUnsigned8(0));
		assertEquals(0x00, memory.readUnsigned8(1));
		assertEquals(0x03, memory.readUnsigned8(2));
		assertEquals(0x00, memory.readUnsigned8(3));
		assertEquals(0x37, memory.readUnsigned8(4));
	}
}
