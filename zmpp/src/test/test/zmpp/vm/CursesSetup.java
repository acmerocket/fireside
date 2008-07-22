/*
 * $Id: LongInstructionTest.java 524 2007-11-15 00:32:16Z weiju $
 * 
 * Created on 10/04/2005
 * Copyright 2005-2008 by Wei-ju Wu
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
package test.zmpp.vm;

import java.io.File;
import java.io.FileInputStream;

import org.junit.BeforeClass;
import org.zmpp.base.Memory;
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
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vmutil.FileUtils;
import static test.zmpp.testutil.ZmppTestUtil.*;

/**
 * Set up the test with a Curses game.
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
  	File cursesFile = createLocalFile("testfiles/curses.z5");
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

  	abbreviations = new Abbreviations(curses,
  			machine.readUnsigned16(StoryFileHeader.ABBREVIATIONS));
  	ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
  	AlphabetTable alphabetTable = new DefaultAlphabetTable();
  	ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
  	converter = new DefaultZCharDecoder(encoding, translator, abbreviations);

  }
}
