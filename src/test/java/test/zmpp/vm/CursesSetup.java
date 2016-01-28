/*
 * Created on 10/04/2005
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

import java.io.File;
import java.io.FileInputStream;

import org.junit.BeforeClass;
import org.zmpp.base.Memory;
import org.zmpp.base.StoryFileHeader;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharDecoder;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.vm.Abbreviations;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vmutil.FileUtils;

import test.zmpp.testutil.TestUtil;

/**
 * Set up the test with a Curses game.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class CursesSetup {

	protected Memory curses;
	protected ZCharDecoder converter;
	protected StoryFileHeader fileheader;
	protected Abbreviations abbreviations;
	protected Machine machine;
	private static byte[] originalData;

	@BeforeClass
	public static void setupClass() throws Exception {
		File cursesFile = TestUtil.loadResource("curses.z5");
		FileInputStream fileInput = new FileInputStream(cursesFile);
		originalData = FileUtils.readFileBytes(fileInput);
		fileInput.close();
	}

	protected void setUp() throws Exception {
		byte[] data = new byte[originalData.length];
		System.arraycopy(originalData, 0, data, 0, originalData.length);
		machine = new MachineImpl();
		machine.initialize(data, null);
		curses = machine;
		fileheader = machine.getFileHeader();

		abbreviations = new Abbreviations(curses, machine.readUnsigned16(StoryFileHeader.ABBREVIATIONS));
		ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
		AlphabetTable alphabetTable = new DefaultAlphabetTable();
		ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
		converter = new DefaultZCharDecoder(encoding, translator, abbreviations);

	}
}
