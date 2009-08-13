/*
 * Created on 2006/02/06
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

import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.readUnsigned32;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.media.MediaCollection;

/**
 * This class defines an abstract media collection based on the Blorb
 * format.
 * It also defines the common read process for resources. The collection
 * is represented by a database and an index to the database, which maps
 * index numbers to resource numbers. The implementation of the database
 * is left to the sub classes.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public abstract class BlorbMediaCollection<T> implements MediaCollection<T> {

  /**
   * The list of resource numbers in the file.
   */
  private List<Integer> resourceNumbers;

  /**
   * Access to the form chunk.
   */
  private FormChunk formchunk;

  protected NativeImageFactory imageFactory;
  protected SoundEffectFactory soundEffectFactory;

  /**
   * Constructor.
   *
   * @param formchunk the Blorb file form chunk
   */
  public BlorbMediaCollection(NativeImageFactory imageFactory,
      SoundEffectFactory soundEffectFactory,
      FormChunk formchunk) {
    resourceNumbers = new ArrayList<Integer>();
    this.formchunk = formchunk;
    this.imageFactory = imageFactory;
    this.soundEffectFactory = soundEffectFactory;
    initDatabase();

    // Ridx chunk
    Chunk ridxChunk = formchunk.getSubChunk("RIdx");
    Memory chunkmem = ridxChunk.getMemory();
    int numresources = (int) readUnsigned32(chunkmem, 8);
    int offset = 12;
    byte[] usage = new byte[4];

    for (int i = 0; i < numresources; i++) {
      chunkmem.copyBytesToArray(usage, 0, offset, 4);
      if (isHandledResource(usage)) {
        int resnum = (int) readUnsigned32(chunkmem, offset + 4);
        int address = (int) readUnsigned32(chunkmem, offset + 8);
        Chunk chunk = formchunk.getSubChunk(address);

        if (putToDatabase(chunk, resnum)) {
          resourceNumbers.add(resnum);
        }
      }
      offset += 12;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void clear() {

    resourceNumbers.clear();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumResources() {

    return resourceNumbers.size();
  }

  /**
   * Returns the resource number at the given index.
   *
   * @param index the index
   * @return the resource number
   */
  public int getResourceNumber(final int index) {

    return resourceNumbers.get(index);
  }

  /**
   * {@inheritDoc}
   */
  public void loadResource(final int resourcenumber) {

    // intentionally left empty for possible future use
  }

  /**
   * {@inheritDoc}
   */
  public void unloadResource(final int resourcenumber) {

    // intentionally left empty for possible future use
  }

  /**
   * Access to the form chunk.
   *
   * @return the form chunk
   */
  protected FormChunk getFormChunk() {

    return formchunk;
  }

  /**
   * Initialize the database.
   */
  abstract protected void initDatabase();

  /**
   * This method is invoked by the constructor to indicate if the
   * class handles the given resource.
   *
   * @param usageId the usage id
   * @return true if the current class handles this resource, false, otherwise
   */
  abstract protected boolean isHandledResource(byte[] usageId);

  /**
   * Puts the media object based on this sub chunk into the database.
   *
   * @param chunk the blorb sub chunk
   * @param resnum the resource number
   * @return true if successful, false otherwise
   */
  abstract protected boolean putToDatabase(Chunk chunk, int resnum);
}
