/*
 * Created on 2006/03/03
 * Copyright 2005-2009 by Wei-ju Wu
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
package org.zmpp.blorb;

import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;

/**
 * This class extracts story data from a Blorb file.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class BlorbFile {

  private FormChunk formChunk;

  /**
   * Constructor.
   * @param formchunk the FORM chunk
   */
  public BlorbFile(final FormChunk formchunk) {
    this.formChunk = formchunk;
  }

  /**
   * Returns the story data contained in the Blorb.
   * @return the story data
   */
  public byte[] getStoryData() {
    final Chunk chunk = formChunk.getSubChunk("ZCOD");
    final int size = chunk.getSize();
    final byte[] data = new byte[size];
    chunk.getMemory().copyBytesToArray(data, 0, Chunk.CHUNK_HEADER_LENGTH,
                                       size);
    return data;
  }
}
