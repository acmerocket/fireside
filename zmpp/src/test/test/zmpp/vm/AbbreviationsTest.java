/*
 * $Id$
 * 
 * Created on 25.09.2005
 * Copyright 2005-2007 by Wei-ju Wu
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


/**
 * This class tests the Abbreviations class.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class AbbreviationsTest extends MiniZorkSetup {

  private static String[] testdata = {

    "the ", "The ", "You ", ", ", "your ", "is ", "and ", "There ", "you ",
    "of ", ". ", "with ", "to ", "are ", "large ", "This ", "cyclops ", "that ",
    "from ", "have ", "through ", "here", "in ", "It's ", "which ", "small ",
    "room ", "closed", "A ", "can't ", "You're ", "into ", "Room", "Your ",
    "grating ", "already ", "Frigid ", "isn't ", "It ", "thief ", "be ",
    "that", "for ", "water ", "leads ", "won't ", "narrow ", "cannot ", "but ",
    "not ", "this ", "south ", "seems ", "ground", "about ", "passage ",
    "appears ", "don't ", "southwest", "on ", "west ", "north ", "There's ",
    "his ", "feet ", "east ", "door ", "cyclops", "can ", "white ", "That ",
    "probably ", "Maze", "an ", "too ", "has ", "wooden ", "In ", "south",
    "north", "How ", "would ", "With ", "sentence", "rainbow ", "lurking ",
    "looking ", "leading ", "darkness", "candles ", "against ", "treasures ",
    "staircase ", "northeast ", "one ", "now "
  };
  
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testGetWordAddress() {
    
    // Test of the abbreviations in the minizorkmap
    for (int i = 0; i < testdata.length; i++) {
      
      assertEquals(testdata[i], converter.decode2Zscii(minizorkmap,
         abbreviations.getWordAddress(i), 0).toString());
    }
  }  
}
