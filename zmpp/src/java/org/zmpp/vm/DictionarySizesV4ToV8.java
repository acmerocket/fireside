/*
 * $Id$
 * 
 * Created on 2006/01/19
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
package org.zmpp.vm;

/**
 * The dictionary size definitions for the story file versions 4-8.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DictionarySizesV4ToV8 implements DictionarySizes {

  public int getNumEntryBytes() { return 6; }
  public int getMaxEntryChars() { return 9; }
}
