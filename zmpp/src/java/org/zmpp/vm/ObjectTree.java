/*
 * $Id$
 * 
 * Created on 14.10.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.vm;

/**
 * This is the interface definition of the object tree.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface ObjectTree {

  /**
   * Returns the property default value at the specified position in the
   * property defaults table.
   * 
   * @param propertyNum the default entry's propery number
   * @return the property default value
   */
  short getPropertyDefault(int propertyNum);
  
  /**
   * Returns the Z-object with the specified object number. The index is
   * 1 based
   * 
   * @param objectNum object number
   * @return the z object with the specified number
   */
  ZObject getObject(int objectNum);
  
  /**
   * Returns the total number of objects.
   * 
   * @return the number of objects
   */
  int getNumObjects();
  
  /**
   * Removes an object from its parent.
   * 
   * @param objectNum the object number
   */
  void removeObject(int objectNum);
    
  /**
   * Inserts an object to a new parent.
   * 
   * @param parentNum the parent number
   * @param objectNum the object number
   */
  void insertObject(int parentNum, int objectNum);
}
