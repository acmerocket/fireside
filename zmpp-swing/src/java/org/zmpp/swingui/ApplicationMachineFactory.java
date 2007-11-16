/*
 * $Id$
 * 
 * Created on 2006/02/15
 * Copyright 2005-2007 by Wei-ju Wu
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
package org.zmpp.swingui;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.zmpp.base.DefaultMemory;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.blorb.BlorbStory;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.media.Resources;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;
import org.zmpp.vmutil.FileUtils;

/**
 * This class implements machine creation for a standalone application.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ApplicationMachineFactory extends MachineFactory<ZmppFrame> {

  private File storyfile;
  private File blorbfile;
  private ZmppFrame frame;
  private FormChunk blorbchunk;
  private SaveGameDataStore savegamestore;
  
  public ApplicationMachineFactory(File storyfile, File blorbfile) {
  
    this.storyfile = storyfile;
    this.blorbfile = blorbfile;
  }

  public ApplicationMachineFactory(File blorbfile) {
    
    this.blorbfile = blorbfile;
  }
  
  /**
   * {@inheritDoc}
   */
  protected byte[] readStoryData() throws IOException {
        
    if (storyfile != null) {
      
      return FileUtils.readFileBytes(storyfile);
      
    } else {
      
      // Read from Z BLORB
      FormChunk formchunk = readBlorb();
      return formchunk != null ? new BlorbStory(formchunk).getStoryData() : null;
    }
  }
  
  private FormChunk readBlorb() throws IOException {
    
    if (blorbchunk == null) {
      
      byte[] data = FileUtils.readFileBytes(blorbfile);
      if (data != null) {
        
        blorbchunk = new DefaultFormChunk(new DefaultMemory(data));
        if (!"IFRS".equals(new String(blorbchunk.getSubId()))) {
          
          throw new IOException("not a valid Blorb file");
        }
      }
    }
    return blorbchunk;
  }
  
  /**
   * {@inheritDoc}
   */
  protected Resources readResources() throws IOException {

    FormChunk formchunk = readBlorb();
    return (formchunk != null) ? new BlorbResources(formchunk) : null;
  }

  /**
   * {@inheritDoc}
   */
  protected void reportInvalidStory() {

    JOptionPane.showMessageDialog(null,
        "Invalid story file.",
        "Story file read error", JOptionPane.ERROR_MESSAGE);
    System.exit(0);
  }
      
  /**
   * {@inheritDoc}
   */
  protected ZmppFrame initUI(Machine machine) {
    
    frame = new ZmppFrame(machine);
    savegamestore = new FileSaveGameDataStore(frame);
    return frame;
  }

  /**
   * {@inheritDoc}
   */
  public ZmppFrame getUI() { return frame; }

  /**
   * {@inheritDoc}
   */
  protected IOSystem getIOSystem() { return frame; }  

  /**
   * {@inheritDoc}
   */
  protected InputStream getKeyboardInputStream() { return frame; }

  /**
   * {@inheritDoc}
   */
  protected StatusLine getStatusLine() { return frame; }

  /**
   * {@inheritDoc}
   */
  protected ScreenModel getScreenModel() { return frame.getScreenModel(); }

  /**
   * {@inheritDoc}
   */
  protected SaveGameDataStore getSaveGameDataStore() { return savegamestore; }
}
