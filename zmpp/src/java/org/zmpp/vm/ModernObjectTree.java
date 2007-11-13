/*
 * $Id$
 * 
 * Created on 2006/03/05
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

import org.zmpp.base.MemoryAccess;
import org.zmpp.encoding.ZCharDecoder;

/**
 * This class implements the object tree for story file version >= 4.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ModernObjectTree extends AbstractObjectTree {

  /**
   * Object entries in version >= 4 have a size of 14 bytes.
   */
  private static final int OBJECTENTRY_SIZE = 14;
  
  /**
   * Property defaults entries in versions >= 4 have a size of 63 words.
   */
  private static final int PROPERTYDEFAULTS_SIZE = 63 * 2;
  
  public ModernObjectTree(MemoryAccess memaccess, int address,
                          ZCharDecoder decoder) {

    super(memaccess, address, decoder);
  }

  /**
   * {@inheritDoc}
   */
  protected ZObject createObject(final int objectNum) {
    
    return new ModernZObject(getMemoryAccess(),
        getObjectTreeStart() + (objectNum - 1) * getObjectEntrySize(),
        getDecoder());
  }

  /**
   * {@inheritDoc}
   */
  protected int getPropertyDefaultsSize() { return PROPERTYDEFAULTS_SIZE; }

  /**
   * {@inheritDoc}
   */
  protected int getObjectEntrySize() { return OBJECTENTRY_SIZE; } 

  /**
   * {@inheritDoc}
   */
  public int getPropertyLength(final int propertyAddress) {

    return ModernZObject.getPropertyLengthAtData(getMemoryAccess(),
        propertyAddress);
  }  
}
