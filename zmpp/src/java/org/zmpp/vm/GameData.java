/*
 * $Id$
 * 
 * Created on 10/10/2005
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
package org.zmpp.vm;

import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.encoding.AccentTable;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV1;
import org.zmpp.encoding.AlphabetTableV2;
import org.zmpp.encoding.CustomAccentTable;
import org.zmpp.encoding.CustomAlphabetTable;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharDecoder;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharEncoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.media.Resources;

/**
 * A class that holds the main machine objects.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class GameData {

  private StoryFileHeader fileheader;
  private Memory memory;
  private Dictionary dictionary;
  private ObjectTree objectTree;
  private ZsciiEncoding encoding;
  private ZCharDecoder decoder;
  private ZCharEncoder encoder;  
  private AlphabetTable alphabetTable;  
  private Resources resources;  
  private byte[] storyfileData;
  private int checksum;  

  /**
   * Just for JMock.
   */
  public GameData() { }

  /**
   * Constructor.
   * @param storyfile the story file as a byte array
   * @param resources the media resources
   */
  public GameData(byte[] storyfile, Resources resources) {
    storyfileData = storyfile;
    this.resources = resources;
    reset();
  }
  
  /**
   * Resets the data.
   */
  public final void reset() {
    // Make a copy and initialize from the copy
    final byte[] data = new byte[storyfileData.length];
    System.arraycopy(storyfileData, 0, data, 0, storyfileData.length);
    
    memory = new DefaultMemory(data);
    fileheader = new DefaultStoryFileHeader(memory);
    checksum = calculateChecksum();
    
    // Install the whole character code system here
    initEncodingSystem();
    
    // The object tree and dictionaries depend on the code system
    if (fileheader.getVersion() <= 3) {
      objectTree = new ClassicObjectTree(memory,
          fileheader.getObjectTableAddress());
    } else {
      objectTree = new ModernObjectTree(memory,
          fileheader.getObjectTableAddress());
    }
    final DictionarySizes sizes = (fileheader.getVersion() <= 3) ?
        new DictionarySizesV1ToV3() : new DictionarySizesV4ToV8();
    dictionary = new DefaultDictionary(memory,
        fileheader.getDictionaryAddress(), decoder, sizes);
  }
  
  private void initEncodingSystem() {
    final AccentTable accentTable = (fileheader.getCustomAccentTable() == 0) ?
        new DefaultAccentTable() :
        new CustomAccentTable(memory, fileheader.getCustomAccentTable());
    encoding = new ZsciiEncoding(accentTable);

    // Configure the alphabet table
    if (fileheader.getCustomAlphabetTable() == 0) {
      if (fileheader.getVersion() == 1) {
        alphabetTable = new AlphabetTableV1();
      } else if (fileheader.getVersion() == 2) {
        alphabetTable = new AlphabetTableV2();
      } else {
        alphabetTable = new DefaultAlphabetTable();
      }
    } else {
      alphabetTable = new CustomAlphabetTable(memory,
          fileheader.getCustomAlphabetTable());
    }
    
    final ZCharTranslator translator =
      new DefaultZCharTranslator(alphabetTable);
        
    final Abbreviations abbreviations = new Abbreviations(memory,
        fileheader.getAbbreviationsAddress());
    decoder = new DefaultZCharDecoder(encoding, translator, abbreviations);
    encoder = new ZCharEncoder(translator);
    ZsciiString.initialize(encoding);
  }
    
  
  /**
   * Returns the file data as a Memory object.
   * @return the file data
   */
  public Memory getMemory() { return memory; }
  
  /**
   * Returns the story file header.
   * @return the story file header
   */
  public StoryFileHeader getStoryFileHeader() { return fileheader; }
  
  /**
   * Returns the dictionary for this game.
   * @return the dictionary
   */
  public Dictionary getDictionary() { return dictionary; }
  
  /**
   * Returns the object tree for this game.
   * @return the object tree
   */
  public ObjectTree getObjectTree() { return objectTree; }

  /**
   * Returns the Z char decoder for this game.
   * @return the z char decoder
   */
  public ZCharDecoder getZCharDecoder() { return decoder; }
  
  /**
   * Returns the Z char encoder for this game.
   * @return the z char encoder
   */
  public ZCharEncoder getZCharEncoder() { return encoder; }
  
  /**
   * Returns the ZSCII encoding object.
   * @return the encoding object
   */
  public ZsciiEncoding getZsciiEncoding() { return encoding; }
  
  /**
   * Returns this game's alphabet table.
   * @return the alphabet table
   */
  public AlphabetTable getAlphabetTable() { return alphabetTable; }
  
  /**
   * Returns the multimedia resources.
   * @return the multimedia resources
   */
  public Resources getResources() { return resources; }

  /**
   * Returns the calculated check sum.
   * @return the calculated check sum
   */
  public int getCalculatedChecksum() {
    return checksum;
  }

  /**
   * Calculates the checksum of the file.
   * @param fileheader the file header
   * @return the check sum
   */
  private int calculateChecksum() {
    final int filelen = fileheader.getFileLength();
    int sum = 0;
    for (int i = 0x40; i < filelen; i++) {
      sum += getMemory().readUnsignedByte(i);
    }
    return (sum & 0xffff);
  }
  
  /**
   * Returns true, if the checksum validation was successful.
   * @return true if checksum is valid
   */
  public boolean hasValidChecksum() {
    return getStoryFileHeader().getChecksum() == checksum;
  }
}
