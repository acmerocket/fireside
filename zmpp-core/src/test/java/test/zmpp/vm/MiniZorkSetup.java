/*
 * Created on 09/24/2005
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
package test.zmpp.vm;


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
import org.zmpp.vm.MachineImpl;
import org.zmpp.base.StoryFileHeader;
import static test.zmpp.testutil.ZmppTestUtil.*;

/**
 * This class acts as a base test class and sets up some integrated
 * testing objects for the minizork game.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public abstract class MiniZorkSetup {

  protected Memory minizorkmap;
  protected ZCharDecoder converter;
  protected StoryFileHeader fileheader;
  protected Abbreviations abbreviations;
  protected MachineImpl machine;

  protected void setUp() throws Exception {
    machine = MachineTestUtil.createMachine(createLocalFile("testfiles/minizork.z3"));
    minizorkmap = machine;
    fileheader = machine.getFileHeader();
    
    abbreviations = new Abbreviations(minizorkmap,
        machine.readUnsigned16(StoryFileHeader.ABBREVIATIONS));
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    AlphabetTable alphabetTable = new DefaultAlphabetTable();
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    converter = new DefaultZCharDecoder(encoding, translator, abbreviations); 
  }
}