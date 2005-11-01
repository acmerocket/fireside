/*
 * $Id$
 * 
 * Created on 2005/09/23
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
package org.zmpp.iff;

import org.zmpp.base.MemoryAccess;

/**
 * This is the default implementation of the Chunk interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DefaultChunk implements Chunk {

  /**
   * The memory access object.
   */
  protected MemoryAccess memaccess;
  
  /**
   * The chunk id.
   */
  private byte[] id;
  
  /**
   * The chunk size.
   */
  private int chunkSize;
  
  /**
   * Constructor.
   * 
   * @param memaccess a memory access object to the chunk data
   */
  public DefaultChunk(MemoryAccess memaccess) {
    
    this.memaccess = memaccess;
    initBaseInfo();
  }
  
  /**
   * Initialize the base information for this chunk. 
   */
  private void initBaseInfo() {
    
    // Determine the chunk id
    id = new byte[CHUNK_ID_LENGTH];
    for (int i = 0; i < CHUNK_ID_LENGTH; i++) {
      
      id[i] = memaccess.readByte(i);
    }
    
    // Determine the chunk size 
    chunkSize = (int) memaccess.readUnsigned32(CHUNK_ID_LENGTH);    
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isValid() {
    
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getId() {
    
    return id;
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() {
    
    return chunkSize;
  }
  
  /**
   * {@inheritDoc}
   */
  public MemoryAccess getMemoryAccess() {
    
    return memaccess;
  }
}