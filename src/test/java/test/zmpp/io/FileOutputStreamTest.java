/*
 * Created on 11/08/2005
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
package test.zmpp.io;

import java.io.FileWriter;
import java.io.Writer;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.io.IOSystem;
import org.zmpp.io.TranscriptOutputStream;

import test.zmpp.testutil.TestUtil;

/**
 * Test class for FileOutputStream.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class FileOutputStreamTest {
	Mockery context = new JUnit4Mockery();
	private IOSystem iosys;
	private TranscriptOutputStream outstream;
	private Writer outputwriter;

	@Before
	public void setUp() throws Exception {
		iosys = context.mock(IOSystem.class);
		outstream = new TranscriptOutputStream(iosys, new ZsciiEncoding(new DefaultAccentTable()));
		outputwriter = new FileWriter(TestUtil.loadResource("streamoutput.txt"));
	}

	@After
	public void tearDown() throws Exception {
		outstream.close();
	}

	@Test
	public void testPrintFirstTime() {
		context.checking(new Expectations() {
			{
				one(iosys).getTranscriptWriter();
				will(returnValue(outputwriter));
			}
		});
		// mockIo.expects(once()).method("getTranscriptWriter").will(returnValue(outputwriter));
		outstream.print('a');
		outstream.print(ZsciiEncoding.NEWLINE);
		outstream.print('b');
		outstream.print(ZsciiEncoding.NEWLINE);
	}
}
