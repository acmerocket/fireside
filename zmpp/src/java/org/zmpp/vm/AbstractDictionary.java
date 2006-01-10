package org.zmpp.vm;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.vmutil.ZCharDecoder;

public abstract class AbstractDictionary implements Dictionary {

  /**
   * The memory map.
   */
  private MemoryReadAccess memaccess;
  
  /**
   * The dictionary start address.
   */
  private int address;
  
  /**
   * A Z char decoder.
   */
  private ZCharDecoder decoder;
  
  /**
   * Constructor.
   * 
   * @param map the memory map
   * @param address the start address of the dictionary
   * @param converter a Z char decoder object
   */
  public AbstractDictionary(MemoryReadAccess map, int address,
                            ZCharDecoder decoder) {
    
    this.memaccess = map;
    this.address = address;
    this.decoder = decoder;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfSeparators() {
    
    return memaccess.readUnsignedByte(address);
  }
  
  /**
   * {@inheritDoc}
   */
  public byte getSeparator(int i) {
    
    return (byte) memaccess.readUnsignedByte(address + i + 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getEntryLength() {
    
    return memaccess.readUnsignedByte(address + getNumberOfSeparators() + 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfEntries() {
    
    return memaccess.readUnsignedShort(address + getNumberOfSeparators() + 2);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getEntryAddress(int entryNum) {
   
    int headerSize = getNumberOfSeparators() + 4;    
    return address + headerSize + entryNum * getEntryLength();
  }
  
  protected ZCharDecoder getDecoder() {
    
    return decoder;
  }
  
  protected MemoryReadAccess getMemoryAccess() {
    
    return memaccess;
  }
  
  protected String truncateToken(String token) {
    
    String truncated = token;
    
    // Unfortunately it seems that the maximum size of an entry is not equal 
    // to the size declared in the dictionary header, therefore we take
    // the maximum length of any token in the dictionary
    int maxEntrySize = getMaxEntrySize();
    int entryLength = getEntryLength();
    entryLength = (maxEntrySize < entryLength) ? maxEntrySize : entryLength;
    //System.out.println("lookup(), token: '" + token + "' entrylen: "
    //                  + entryLength);
    
    // The lookup token can only be 6 characters long in version 3
    // and 9 in versions >= 4
    if (token.length() > entryLength) {
      
      truncated = token.substring(0, entryLength);
    }
    return truncated;
  }
  
  
  abstract protected int getMaxEntrySize();

  /**
   * Creates a string presentation of this dictionary.
   * 
   * @return the string presentation
   */
  public String toString() {

    StringBuilder buffer = new StringBuilder();
    int entryAddress;
    int i = 0;
    int n = getNumberOfEntries();
    
    while (true) {
      
      entryAddress = getEntryAddress(i);
      String str = getDecoder().decode2Unicode(getMemoryAccess(),
          entryAddress);
      buffer.append(String.format("[%4d] '%-9s' ", (i + 1), str));
      i++;
      if ((i % 4) == 0) buffer.append("\n");
      if (i == n) break;
    }
    return buffer.toString();
  }
}
