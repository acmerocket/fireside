/*
 * $Id$
 * 
 * Created on 2006/03/05
 * Copyright 2005-2006 by Wei-ju Wu
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

import org.zmpp.base.MemoryAccess;
import org.zmpp.encoding.ZCharDecoder;

/**
 * This class holds common functionality of a ZObject.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public abstract class AbstractZObject implements ZObject {

  /**
   * The property table is defined in the object's context
   * to make it dependent on a specific instance.
   */
  abstract class PropertyTable {
    
    /**
     * Returns this property table's description string address.
     * 
     * @return the address of the description
     */
    public int getDescriptionAddress() {
      
      return getPropertyTableAddress() + 1;
    }
    
    /**
     * Returns the start address of the actual property entries.
     * 
     * @return the property entries' start address
     */
    private int getPropertyEntriesStart() {
      
      return getPropertyTableAddress() + getDescriptionHeaderSize();
    }
    
    /**
     * Returns the size of the description header in bytes that is,
     * the size byte plus the description string size. This stays the same
     * for all story file versions.
     *  
     * @return the size of the description header 
     */
    private int getDescriptionHeaderSize() {
      
      int startAddr = getPropertyTableAddress();
      return memaccess.readUnsignedByte(startAddr) * 2 + 1;
    }
    
    /**
     * Reads the property size byte at the given address.
     * 
     * @param address the address of a property size byte
     * @return the size of the property entry in bytes
     */
    protected abstract int getPropertySizeAtAddress(int address);
    
    /**
     * Returns the number of property size bytes at the specified address.
     * 
     * @param address the address of the property entry
     * @return the number of size bytes
     */
    abstract protected int getNumPropertySizeBytes(int address);
    
    /**
     * Returns the address of the specified property.
     * 
     * @param index the property index
     * @return the address of the specified property
     */
    protected int getPropertyAddressAt(int index) {
      
      int addr = getPropertyEntriesStart();
      int size = 0;
      
      // iterate over the previous entries and
      // skip over the entries        
      for (int i = 0; i < index; i++) {
          
        size = getPropertySizeAtAddress(addr);
        addr += (size + getNumPropertySizeBytes(addr));
      }
      return addr;
    }
    
    /**
     * Returns the number of properties in this property table.
     * 
     * @return the number of properties
     */
    public int getNumProperties() {
      
      int row = 0;
      while (memaccess.readUnsignedByte(getPropertyAddressAt(row)) > 0) {
        
        row++;
      }
      return row;
    }
    
    /**
     * Returns the size of the property in bytes.
     * 
     * @param index the property index
     * @return the size of the specified property in bytes
     */
    public int getSize(int index) {
      
      return getPropertySizeAtAddress(getPropertyAddressAt(index));
    }
    
    /**
     * Returns the property at the specified row and column position.
     * 
     * @param index the property index
     * @param bytenum the byte number
     * @return the unsigned byte value at the specified position of the
     * property
     */
    public byte getPropertyByte(int index, int bytenum) {
      
      int addr = getPropertyAddressAt(index);
      return memaccess.readByte(addr + getNumPropertySizeBytes(addr) + bytenum);
    }
    
    /**
     * Writes a value to the specified property byte.
     * 
     * @param index the property index
     * @param bytenum the byte number
     * @param value the value to set
     */
    public void setPropertyByte(int index, int bytenum, byte value) {
      
      int addr = getPropertyAddressAt(index);
      memaccess.writeByte(addr + getNumPropertySizeBytes(addr) + bytenum,
                          value);
    }
    
    /**
     * Returns the number of size bytes for the specified property.
     * 
     * @param index the property index
     * @return the number of size bytes
     */
    public int getNumSizeBytesForPropertyIndex(int index) {
    
      return getNumPropertySizeBytes(getPropertyAddressAt(index));
    }

    /**
     * Returns the property number at the specified table index.
     * 
     * @param index the property table index
     * @return the property number
     */
    public abstract short getPropertyNum(int index);
    
    /**
     * Returns the property table index for the specified property number.
     * 
     * @param propertyNum the property number
     * @return the table index
     */
    public short getPropertyIndex(int propertyNum) {
      
      for (int i = 0; i < getNumProperties(); i++) {
        
        if (getPropertyNum(i) == propertyNum) return (short) i;
      }
      return -1;
    }
    
    /**
     * Returns the size of this object's property table in bytes.
     * 
     * @return the size of the property table in bytes
     */
    public int getDataLength() {
      
      int size = getDescriptionHeaderSize();
      
      for (int i = 0; i < getNumProperties(); i++) {
        
        size += (getSize(i) + 1);
      }
      return size;
    }
  }
  
  /**
   * The memory access object.
   */
  private MemoryAccess memaccess;
  
  /**
   * The address of this Z-object.
   */
  private int address;
  
  /**
   * This object's properties.
   */
  private PropertyTable propertyTable;
  
  /**
   * The Z char decoder.
   */
  private ZCharDecoder decoder;
  
  /**
   * Constructor.
   *
   * @param memaccess a MemoryAccess object
   * @param address the start address of the object
   * @param decoder a Z char decoder object
   */
  public AbstractZObject(MemoryAccess memaccess,
                         int address, ZCharDecoder decoder) {
    
    this.memaccess = memaccess;
    this.address = address;
    this.propertyTable = createPropertyTable();
    this.decoder = decoder;
  }
  
  /**
   * Creates a property table.
   * 
   * @return the property table
   */
  protected abstract PropertyTable createPropertyTable();
  
  /**
   * Returns the memory object.
   * 
   * @return the memory object
   */
  protected MemoryAccess getMemoryAccess() { return memaccess; }
  
  /**
   * Returns the object address.
   * 
   * @return the object address
   */
  protected int getObjectAddress() { return address; }
  
  /**
   * {@inheritDoc}
   */
  public boolean isAttributeSet(int attributeNum) {
    
    short value = memaccess.readUnsignedByte(address + attributeNum / 8);
    return (value & (0x80 >> (attributeNum & 7))) > 0;
  }

  /**
   * {@inheritDoc}
   */
  public void setAttribute(int attributeNum) {
    
    int attributeByteAddress = address + attributeNum / 8;
    short value = memaccess.readUnsignedByte(attributeByteAddress);
    value |= (0x80 >> (attributeNum & 7));
    memaccess.writeUnsignedByte(attributeByteAddress, value);
  }

  /**
   * {@inheritDoc}
   */
  public void clearAttribute(int attributeNum) {

    int attributeByteAddress = address + attributeNum / 8;
    short value = memaccess.readUnsignedByte(attributeByteAddress);
    value &= (~(0x80 >> (attributeNum & 7)));
    memaccess.writeUnsignedByte(attributeByteAddress, value);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getPropertiesDescriptionAddress() {
    
    return propertyTable.getDescriptionAddress();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumProperties() {
    
    return propertyTable.getNumProperties();
  }

  /**
   * {@inheritDoc}
   */
  public int getPropertySize(int property) {
    
    int index = propertyTable.getPropertyIndex(property);
    return propertyTable.getSize(index);
  }
  
  /**
   * {@inheritDoc}
   */
  public byte getPropertyByte(int property, int bytenum) {
    
    int index = propertyTable.getPropertyIndex(property);
    return propertyTable.getPropertyByte(index, bytenum);
  }

  /**
   * {@inheritDoc}
   */
  public int getPropertyAddress(int property) {
    
    int index = propertyTable.getPropertyIndex(property);    
    int numSizeBytes = propertyTable.getNumSizeBytesForPropertyIndex(index);
    return propertyTable.getPropertyAddressAt(index) + numSizeBytes;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isPropertyAvailable(int property) {
    
    return (propertyTable.getPropertyIndex(property) >= 0);
  }

  /**
   * {@inheritDoc}
   */
  public int getNextProperty(int property) {
    
    if (property == 0) {
      
      return propertyTable.getPropertyNum(0);
      
    } else {
      
      int index = propertyTable.getPropertyIndex(property);
      return propertyTable.getPropertyNum(index + 1);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setPropertyByte(int property, int bytenum, byte value) {
    
    int index = propertyTable.getPropertyIndex(property);
    propertyTable.setPropertyByte(index, bytenum, value);
  }
  
  /**
   * Dumps the content of the object in a pretty formatted string. The
   * output resembles the infodump output
   * 
   * @return the string presentation of that object
   */
  public String toString() {

    // description
    String str = decoder.decode2Zscii(memaccess,
        getPropertiesDescriptionAddress(), 0).toString(); 
    
    // hierarchy
    StringBuilder hierarchyBuffer = new StringBuilder();
    hierarchyBuffer.append("parent: " + getParent());
    hierarchyBuffer.append(" sibling: " + getSibling());
    hierarchyBuffer.append(" child: " + getChild());
    
    // check attributes
    StringBuilder attrBuff = new StringBuilder("attributes: [");
    for (int j = 0; j < 48; j++) {
      
      if (isAttributeSet(j)) {
        
        if (attrBuff.length() > "attributes: [".length()) attrBuff.append(", ");
        attrBuff.append(j);
      }
    }
    attrBuff.append("]");
    
    // Properties
    StringBuilder propBuff = new StringBuilder();    
    int nextProperty = 0;
    propBuff.append(String.format("property table address: %04x\n", getPropertyTableAddress()));
    do {
      nextProperty = getNextProperty(nextProperty);
      if (nextProperty != 0) {
        
        propBuff.append("[" + nextProperty + "] ");
        int propsize = getPropertySize(nextProperty);
        for (int i = 0; i < propsize; i++) {

          propBuff.append(
              String.format("%02x", getPropertyByte(nextProperty, i)) + " ");
        }
        propBuff.append("\n");
      }
      
    } while (nextProperty != 0);
    
    return "Description: \"" + str + "\"\n"
           + hierarchyBuffer.toString() + "\n"
           + attrBuff.toString() + "\n"
           + propBuff.toString();
  }
}
