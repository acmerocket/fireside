/*
 * $Id$
 *
 * Created on 2008/7/17
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
package test.zmpp.testutil;

import org.zmpp.vm.StoryFileHeader;

/**
 * A faked StoryFileHeader for testing purposes. The methods can be overridden
 * to return any value as needed.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DummyStoryFileHeader implements StoryFileHeader {
  public int getVersion() { return 0; }
  public int getRelease() { return 0; }
  public char getHighMemAddress() { return 0; }
  public char getProgramStart() { return 0; }
  public char getDictionaryAddress() { return 0; }
  public char getObjectTableAddress() { return 0; }
  public char getGlobalsAddress() { return 0; }
  public char getStaticsAddress() { return 0; }
  public String getSerialNumber() { return ""; }
  public int getAbbreviationsAddress() { return 0; }
  public int getFileLength() { return 0; }
  public int getChecksum() { return 0; }
  public void setInterpreterNumber(int number) { }
  public int getInterpreterNumber() { return 0; }
  public void setInterpreterVersion(int version) { }
  public int getScreenWidth() { return 0; }
  public void setScreenWidth(int numChars) { }
  public void setScreenWidthUnits(int units) { }
  public int getScreenWidthUnits() { return 0; }
  public int getScreenHeight() { return 0; }
  public int getScreenHeightUnits() { return 0; }
  public void setScreenHeight(int numLines) { }
  public void setScreenHeightUnits(int units) { }
  public int getRoutineOffset() { return 0; }
  public int getStaticStringOffset() { return 0; }
  public int getDefaultBackground() { return 0; }
  public int getDefaultForeground() { return 0; }
  public void setDefaultBackground(int color) { }
  public void setDefaultForeground(int color) { }
  public void setStandardRevision(int major, int minor) { }
  public char getTerminatorsAddress() { return 0; }
  public void setFontWidth(int units) { }
  public int getFontWidth() { return 0; }
  public void setFontHeight(int units) { }
  public int getFontHeight() { return 0; }
  public char getCustomAlphabetTable() { return 0; }
  public void setMouseCoordinates(int x, int y) { }
  public char getCustomAccentTable() { return 0; }
  public void setOutputStream3TextWidth(int units) { }
  public void setEnabled(Attribute attribute, boolean flag) { }
  public boolean isEnabled(Attribute attribute) { return false; }
}
