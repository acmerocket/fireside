/*
 * Created on 2008/07/19
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
import org.zmpp.base.MemoryUtil;
import static org.junit.Assert.*;

/**
 * Test class for MemoryUtil.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MemoryUtilTest {

	private Memory memory;
	private byte[] data = { 0x03, 0x00, 0x37, 0x09, (byte) 0xff, (byte) 0xff };

	@Before
	public void setUp() throws Exception {
		memory = new DefaultMemory(data);
	}

	@Test
	public void testToUnsigned16() {
		assertEquals(1234, MemoryUtil.toUnsigned16(1234));
	}

	@Test
	public void testReadUnsigned32() {
		byte[] data32 = { (byte) 0xd7, (byte) 0x4b, (byte) 0xd7, (byte) 0x53 };
		Memory memaccess = new DefaultMemory(data32);
		assertEquals(0xd74bd753L, MemoryUtil.readUnsigned32(memaccess, 0x00));
	}

	@Test
	public void testWriteUnsigned32() {
		MemoryUtil.writeUnsigned32(memory, 0x00, 0xffffffffL);
		assertEquals(0x00000000ffffffffL, MemoryUtil.readUnsigned32(memory, 0x00));

		MemoryUtil.writeUnsigned32(memory, 0x00, 0xf0f00f0fL);
		assertEquals(0x00000000f0f00f0fL, MemoryUtil.readUnsigned32(memory, 0x00));
	}

	@Test
	public void testSignedToUnsigned16() {
		assertEquals(0, MemoryUtil.signedToUnsigned16((short) 0));
		assertEquals((char) 0xffff, MemoryUtil.signedToUnsigned16((short) -1));
		assertEquals((char) 0xfffe, MemoryUtil.signedToUnsigned16((short) -2));
		assertEquals((char) 32767, MemoryUtil.signedToUnsigned16((short) 32767));
		assertEquals((char) 32768, MemoryUtil.signedToUnsigned16((short) -32768));
	}

	@Test
	public void testUnsignedToSigned16() {
		assertEquals(0, MemoryUtil.unsignedToSigned16((char) 0));
		assertEquals(1, MemoryUtil.unsignedToSigned16((char) 1));
		assertEquals(-32768, MemoryUtil.unsignedToSigned16((char) 32768));
		assertEquals(32767, MemoryUtil.unsignedToSigned16((char) 32767));
		assertEquals(-1, MemoryUtil.unsignedToSigned16((char) 65535));
	}

	@Test
	public void testUnsignedToSigned8() {
		assertEquals(0, MemoryUtil.unsignedToSigned8((char) 0));
		assertEquals(1, MemoryUtil.unsignedToSigned8((char) 1));
		assertEquals(-128, MemoryUtil.unsignedToSigned8((char) 128));
		assertEquals(127, MemoryUtil.unsignedToSigned8((char) 127));
		assertEquals(-1, MemoryUtil.unsignedToSigned8((char) 0xff));
		assertEquals(-1, MemoryUtil.unsignedToSigned8((char) 0x10ff));
	}
}
