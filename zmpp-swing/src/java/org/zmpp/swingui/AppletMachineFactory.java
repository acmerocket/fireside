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

import java.net.URL;

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.blorb.BlorbResources;
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

/**
 * This class implements machine creation for an applet.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class AppletMachineFactory extends MachineFactory<ZmppApplet> {

  private ZmppApplet applet;
  private java.io.InputStream storyis;
  private java.io.InputStream resourceis;

  /**
   * Constructor.
   * 
   * @param applet the applet object
   * @param storyurl the story file url
   * @param resourceurl the blorb file url
   * @throws Exception if an error occurs
   */
  public AppletMachineFactory(ZmppApplet applet, URL storyurl, URL resourceurl)
    throws Exception {
    
    this.applet = applet;
    try {
      
      storyis = storyurl.openStream();
      
      if (resourceurl != null) {
        resourceis = resourceurl.openStream();
      }      
    } catch (Exception ex) {
      
      ex.printStackTrace();      
    }
  }
  
  /**
   * {@inheritDoc}
   */
  protected Resources readResources() {
    
    byte[] data = FileUtils.readFileBytes(resourceis);
    if (data != null) {
    
      FormChunk formchunk = new DefaultFormChunk(new DefaultMemoryAccess(data));
      return new BlorbResources(formchunk);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  protected byte[] readStoryData() {
    
    return FileUtils.readFileBytes(storyis);
  }

  /**
   * {@inheritDoc}
   */
  protected void reportInvalidStory() {
    
    System.err.printf("invalid story file");
  }
  
  /**
   * {@inheritDoc}
   */
  protected IOSystem getIOSystem() { return applet; }  

  /**
   * {@inheritDoc}
   */
  protected InputStream getKeyboardInputStream() { return applet; }

  /**
   * {@inheritDoc}
   */
  protected StatusLine getStatusLine() { return applet; }

  /**
   * {@inheritDoc}
   */
  protected ScreenModel getScreenModel() { return applet.getScreenModel(); }

  /**
   * {@inheritDoc}
   */
  protected SaveGameDataStore getSaveGameDataStore() { return applet; }
  
  /**
   * {@inheritDoc}
   */
  protected ZmppApplet initUI(Machine machine) {
   
    applet.initUI(machine);
    return applet;
  }
  
  /**
   * {@inheritDoc}
   */
  public ZmppApplet getUI() { return applet; }
}
