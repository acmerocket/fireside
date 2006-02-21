/*
 * $Id$
 * 
 * Created on 2006/02/15
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
package org.zmpp.swingui;

import java.io.File;

import javax.swing.JOptionPane;

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.media.Resources;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;

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
  
  public ApplicationMachineFactory(File storyfile, File blorbfile) {
  
    this.storyfile = storyfile;
    this.blorbfile = blorbfile;
  }
  
  /**
   * {@inheritDoc}
   */
  protected byte[] readStoryData() {
    
    return FileUtils.readFileBytes(storyfile);
  }
  
  /**
   * {@inheritDoc}
   */
  protected Resources readResources() {
    
    byte[] data = FileUtils.readFileBytes(blorbfile);
    if (data != null) {
      
      return new BlorbResources(
          new DefaultFormChunk(new DefaultMemoryAccess(data)));
    }
    return null;
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
  protected OutputStream getConsoleOutputStream() { return frame.getOutputStream(); }

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
  protected SaveGameDataStore getSaveGameDataStore() { return frame; }
}
