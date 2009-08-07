/*
 * Created on 2006/02/15
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
package org.zmpp.vm;

import org.zmpp.windowing.StatusLine;
import org.zmpp.windowing.ScreenModel;
import java.io.IOException;
import java.net.URL;

import org.zmpp.base.DefaultMemory;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.blorb.BlorbFile;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.blorb.SoundEffectFactory;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.io.FileInputStream;
import org.zmpp.io.InputStream;
import org.zmpp.io.IOSystem;
import org.zmpp.io.TranscriptOutputStream;
import org.zmpp.media.Resources;
import org.zmpp.vmutil.FileUtils;

/**
 * Constructing a Machine object is a very complex task, the building process
 * deals with creating the game objects, the UI and the I/O system. 
 * Initialization was changed so it is not necessary to create a subclass
 * of MachineFactory. Instead, an init struct and a init callback object
 * should be provided.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MachineFactory {

  public static class MachineInitStruct {
    public java.io.InputStream storyFile, blorbFile;
    public URL storyURL, blorbURL;
    public InputStream keyboardInputStream;
    public StatusLine statusLine;
    public ScreenModel screenModel;
    public IOSystem ioSystem;
    public SaveGameDataStore saveGameDataStore;
    public NativeImageFactory nativeImageFactory;
    public SoundEffectFactory soundEffectFactory;
  }
  
  private MachineInitStruct initStruct;
  private FormChunk blorbchunk;

  /**
   * Constructor.
   * @param initStruct an initialization structure
   */
	public MachineFactory(MachineInitStruct initStruct) {
		this.initStruct = initStruct;
	}
	
  /**
   * This is the main creation function.
   * 
   * @return the machine
   */
  public Machine buildMachine() throws IOException, InvalidStoryException {
    final MachineImpl machine = new MachineImpl();
    machine.initialize(readStoryData(), readResources());
    if (isInvalidStory(machine.getVersion())) {
      throw new InvalidStoryException();
    }
    initIOSystem(machine);
    return machine;
  }

  // ***********************************************************************
  // ****** Helpers
  // *****************************
  /**
   * Reads the story data.
   * 
   * @return the story data
   * @throws IOException if reading story file revealed an error
   */
  private byte[] readStoryData() throws IOException {
  	if (initStruct.storyFile != null || initStruct.blorbFile != null)
      return readStoryDataFromFile();
  	if (initStruct.storyURL != null || initStruct.blorbURL != null)
      return readStoryDataFromUrl();
  	return null;
  }

  /**
   * {@inheritDoc}
   */
  private byte[] readStoryDataFromUrl() throws IOException {
  	java.io.InputStream storyis = null, blorbis = null;
    try {
      if (initStruct.storyURL != null) {
        storyis = initStruct.storyURL.openStream();
      }
      if (initStruct.blorbURL != null) {
        blorbis = initStruct.blorbURL.openStream();
      }      
    } catch (Exception ex) {
      ex.printStackTrace();      
    }

    if (storyis != null) {
      return FileUtils.readFileBytes(storyis);
    } else {
      return new BlorbFile(readBlorb(blorbis)).getStoryData();
    }
  }

  /**
   * {@inheritDoc}
   */
  private byte[] readStoryDataFromFile() throws IOException {        
    if (initStruct.storyFile != null) {
      return FileUtils.readFileBytes(initStruct.storyFile);
    } else {
      // Read from Z BLORB
      FormChunk formchunk = readBlorbFromFile();
      return formchunk != null ? new BlorbFile(formchunk).getStoryData() : null;
    }
  }
  
  /**
   * Reads the resource data.
   * @return the resource data
   * @throws IOException if reading resources revealed an error
   */
  protected Resources readResources() throws IOException {
  	if (initStruct.blorbFile != null) return readResourcesFromFile();
  	if (initStruct.blorbURL != null) return readResourcesFromUrl();
  	return null;
  }
  
  private FormChunk readBlorbFromFile() throws IOException {    
    if (blorbchunk == null) {
      byte[] data = FileUtils.readFileBytes(initStruct.blorbFile);
      if (data != null) {        
        blorbchunk = new DefaultFormChunk(new DefaultMemory(data));
        if (!"IFRS".equals(blorbchunk.getSubId())) {
          throw new IOException("not a valid Blorb file");
        }
      }
    }
    return blorbchunk;
  }  

  private Resources readResourcesFromFile() throws IOException {
    FormChunk formchunk = readBlorbFromFile();
    return (formchunk != null) ?
      new BlorbResources(initStruct.nativeImageFactory,
                         initStruct.soundEffectFactory, formchunk) : null;
  }

  private FormChunk readBlorb(java.io.InputStream blorbis) throws IOException {
  	if (blorbchunk == null) {
    	byte[] data = FileUtils.readFileBytes(blorbis);
      if (data != null) {
        blorbchunk = new DefaultFormChunk(new DefaultMemory(data));
      }
  	}
  	return blorbchunk;
  }

  private Resources readResourcesFromUrl() throws IOException {
    FormChunk formchunk = readBlorb(initStruct.blorbURL.openStream());
    return (formchunk != null) ?
      new BlorbResources(initStruct.nativeImageFactory,
                         initStruct.soundEffectFactory, formchunk) : null;
  }

  // ************************************************************************
  // ****** Private methods
  // ********************************
  /**
   * Checks the story file version.
   * 
   * @param version the story file version
   * @return true if not supported
   */
  private boolean isInvalidStory(final int version) {
    
    return version < 1 || version > 8;
  }
  
  /**
   * Initializes the I/O system.
   * 
   * @param machine the machine object
   */
  private void initIOSystem(final MachineImpl machine) {
    initInputStreams(machine);
    initOutputStreams(machine);    
    machine.setStatusLine(initStruct.statusLine);
    machine.setScreen(initStruct.screenModel);
    machine.setSaveGameDataStore(initStruct.saveGameDataStore);    
  }
  
  /**
   * Initializes the input streams.
   * 
   * @param machine the machine object
   */
  private void initInputStreams(final MachineImpl machine) {
    
    machine.setInputStream(0, initStruct.keyboardInputStream);
    machine.setInputStream(1, new FileInputStream(initStruct.ioSystem,
        machine));
  }

  /**
   * Initializes the output streams.
   * 
   * @param machine the machine object
   */
  private void initOutputStreams(final MachineImpl machine) {
    machine.setOutputStream(1, initStruct.screenModel.getOutputStream());
    machine.selectOutputStream(1, true);
    machine.setOutputStream(2, new TranscriptOutputStream(
        initStruct.ioSystem, machine));
    machine.selectOutputStream(2, false);
    machine.setOutputStream(3, new MemoryOutputStream(machine));
    machine.selectOutputStream(3, false);
  }
}
