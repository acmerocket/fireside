/*
 * Created on 2009/04/16
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

import java.io.IOException;
import org.zmpp.iff.Chunk;
import org.zmpp.media.SoundEffect;

/**
 * SoundEffectFactory interface.
 * @author Wei-ju Wu <wei-ju at boxofrats.com>
 * @version 1.0
 */
public interface SoundEffectFactory {
  /**
   * Creates a SoundEffect from an InputStream.
   * @param inputStream the input stream
   * @return the SoundEffect
   * @throws IOException if read error occurred
   */
  SoundEffect createSoundEffect(Chunk aiffChunk) throws IOException;
}
