/*
 * Created on 07/22/2008
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
package test.zmpp.vm;

import org.junit.Test;
import org.zmpp.encoding.DictionarySizes;
import org.zmpp.vm.DictionarySizesV1ToV3;
import org.zmpp.vm.DictionarySizesV4ToV8;
import static org.junit.Assert.*;

/**
 * Test class for DictionarySizes.
 * 
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
