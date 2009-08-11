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

import java.util.HashMap;
import java.util.Map;

import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.media.SoundEffect;

/**
 * This class implements the Blorb sound collection.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class BlorbSounds extends BlorbMediaCollection<SoundEffect> {

  /**
   * This map implements the database.
   */
  private Map<Integer, SoundEffect> sounds;
  
  /**
   * Constructor.
   * 
   * @param formchunk the form chunk
   */
  public BlorbSounds(SoundEffectFactory factory, FormChunk formchunk) {
    super(null, factory, formchunk);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    super.clear();
    sounds.clear();
  }
  
  /**
   * {@inheritDoc}
   */
  protected void initDatabase() {
    
    sounds = new HashMap<Integer, SoundEffect>();
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isHandledResource(final byte[] usageId) {
    
    return usageId[0] == 'S' && usageId[1] == 'n' && usageId[2] == 'd'
           && usageId[3] == ' ';
  }
  
  /**
   * {@inheritDoc}
   */
  public SoundEffect getResource(final int resourcenumber) {
    return sounds.get(resourcenumber);
  }

  /**
   * {@inheritDoc}
   */
  protected boolean putToDatabase(final Chunk aiffChunk, final int resnum) {
    try {
      sounds.put(resnum, soundEffectFactory.createSoundEffect(aiffChunk));
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }
}
