/*
 * $Id$
 * 
 * Created on 2006/01/29
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
package org.zmpp.media;

import java.awt.Toolkit;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.base.MemoryAccess;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.instructions.Interruptable;

/**
 * This class implements the SoundSystem interface. The implementation
 * is using a Java 5 thread executor which makes it very easy to
 * assign a control task to each sound which can handle the stopping
 * of a sound easily.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class SoundSystemImpl implements SoundSystem {

  /**
   * The resource database.
   */
  private MediaCollection<SoundEffect> sounds;
  
  /**
   * The executor service.
   */
  private ExecutorService executor;
  
  /**
   * The interruptable.
   */
  private Interruptable interruptable;
  
  /**
   * The current sound task.
   */
  protected PlaySoundTask currentTask;
  
  /**
   * Constructor.
   * 
   * @param sounds the sound resources
   */
  public SoundSystemImpl(MediaCollection<SoundEffect> sounds) {
    
    this.sounds = sounds;

    // That's pretty cool:
    // We can control the number of concurrent sounds to be played
    // simultaneously by the size of the thread pool.
    this.executor = Executors.newSingleThreadExecutor();
  }

  /**
   * This method handles the situation if a sound effect is going to
   * be played and a previous one is not finished.
   */
  protected void handlePreviousNotFinished() {

    // The default behaviour is to stop the previous sound
    currentTask.stop();
  }
  
  /**
   * {@inheritDoc}
   */
  public void play(int number, int effect, int repeats, int volume,
                   int routine) {

    SoundEffect sound = sounds.getResource(number);
    if (sound != null) {

      if (effect == SoundSystem.EFFECT_START) {
        
        startSound(number, sound, routine, repeats, volume);
        
      } else if (effect == SoundSystem.EFFECT_STOP) {
        
        stopSound(number);
        
      } else if (effect == SoundSystem.EFFECT_PREPARE) {
        
        sounds.loadResource(number);
        
      } else if (effect == SoundSystem.EFFECT_FINISH) {
        
        stopSound(number);
        sounds.unloadResource(number);
      }

    } else {
      
      Toolkit.getDefaultToolkit().beep();
    }
  }
  
  /**
   * Starts the specified sound.
   * 
   * @param number the sound number
   * @param sound the sound object
   * @param routine the interrupt routine
   * @param repeats the number of repeats
   * @param volume the volume
   */
  private void startSound(int number, SoundEffect sound, int routine,
                          int repeats, int volume) {
    
    if (currentTask != null && !currentTask.wasPlayed()) {
      
      handlePreviousNotFinished();
    }
    
    currentTask = (routine <= 0) ?
      new PlaySoundTask(number, sound, repeats, volume) :
      new PlaySoundTask(number, sound, repeats, volume, interruptable, routine);  
    executor.submit(currentTask);
  }
  
  /**
   * Stops the sound with the given number.
   * 
   * @param number the number
   */
  private void stopSound(int number) {
    
    // only stop the sound if the numbers match
    if (currentTask != null && currentTask.getResourceNumber() == number) {
      
      currentTask.stop();
    }
  }
  
  public static void main(String[] args) {
    
    File file = new File("testfiles/sherlock.blb");
    RandomAccessFile rndfile = null;
    
    try {
      
      rndfile = new RandomAccessFile(file, "r");
      byte[] data = new byte[(int) rndfile.length()];
      rndfile.readFully(data);
      MemoryAccess memaccess = new DefaultMemoryAccess(data);
      FormChunk formchunk = new DefaultFormChunk(memaccess);
      
      BlorbResources resources = new BlorbResources(formchunk);
      SoundSystem system = new SoundSystemImpl(resources.getSounds());
      system.play(16, SoundSystem.EFFECT_START, 3, 255, 0);
      Thread.sleep(2000);
      
      // stop method
      //system.play(16, SoundSystem.EFFECT_STOP, 0, 0, 0);
      
    } catch (Exception ex) {
    
      ex.printStackTrace();
      
    } finally {
      
      if (rndfile != null) try { rndfile.close(); } catch (Exception ex) {}
    }
  }
}
