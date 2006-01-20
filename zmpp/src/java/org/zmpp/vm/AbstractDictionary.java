package org.zmpp.vm;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.encoding.ZCharDecoder;

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
   * A sizes object.
   */
  private DictionarySizes sizes;
  
  /**
   * Constructor.
   * 
   * @param map the memory map
   * @param address the start address of the dictionary
   * @param converter a Z char decoder object
   * @param an object specifying the sizes of the dictionary entries
   */
  public AbstractDictionary(MemoryReadAccess map, int address,
                            ZCharDecoder decoder, DictionarySizes sizes) {
    
    this.memaccess = map;
    this.address = address;
    this.decoder = decoder;
    this.sizes = sizes;
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
  
  protected DictionarySizes getSizes() {
    
    return sizes;
  }
  
  protected String truncateToken(String token) {
    
    // Unfortunately it seems that the maximum size of an entry is not equal 
    // to the size declared in the dictionary header, therefore we take
    // the maximum length of a token defined in the Z-machine specification.    
    // The lookup token can only be 6 characters long in version 3
    // and 9 in versions >= 4
    if (token.length() > sizes.getMaxEntryChars()) {
      
      return token.substring(0, sizes.getMaxEntryChars());
    }
    return token;
  }
  
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
          entryAddress, sizes.getNumEntryBytes());
      buffer.append(String.format("[%4d] '%-9s' ", (i + 1), str));
      i++;
      if ((i % 4) == 0) buffer.append("\n");
      if (i == n) break;
    }
    return buffer.toString();
  }
}
