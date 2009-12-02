/*
 * Created on 2006/01/29
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
package org.zmpp.media;

/**
 * This interface defines the sound system of the Z-machine preservation
 * project.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface SoundSystem {

  /**
   * High pitched bleep.
   */
  int BLEEP_HIGH  = 1;

  /**
   * Low pitched bleep.
   */
  int BLEEP_LOW   = 2;

  /**
   * Prepares a sound.
   */
  int EFFECT_PREPARE  = 1;

  /**
   * Starts a sound.
   */
  int EFFECT_START    = 2;

  /**
   * Stops a sound.
   */
  int EFFECT_STOP     = 3;

  /**
   * Finishes a sound.
   */
  int EFFECT_FINISH   = 4;

  /**
   * The maximum value for volume.
   */
  int VOLUME_MAX = 0;

  /**
   * The minimum value for volume.
   */
  int VOLUME_MIN = 255;

  /**
   * Sets the volume to default.
   */
  int VOLUME_DEFAULT = -1;

  /**
   * Plays a sound.
   *
   * @param number the number of the resource, 1 and 2 are bleeps
   * @param effect the effect
   * @param volume the volume
   * @param repeats how often should the sound be played
   * @param routine the interrupt routine (can be 0)
   */
  void play(int number, int effect, int volume, int repeats, int routine);

  /**
   * Resets the sound system.
   */
  void reset();
}
