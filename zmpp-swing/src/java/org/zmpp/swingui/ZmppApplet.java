/*
 * $Id$
 * 
 * Created on 2005/11/15
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.instructions.DefaultInstructionDecoder;
import org.zmpp.io.FileInputStream;
import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
import org.zmpp.io.TranscriptOutputStream;
import org.zmpp.media.Resources;
import org.zmpp.vm.GameData;
import org.zmpp.vm.GameDataImpl;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineImpl;
import org.zmpp.vm.MemoryOutputStream;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.StatusLine;
import org.zmpp.vm.StoryFileHeader;

/**
 * This is the applet class for ZMPP.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZmppApplet extends JApplet
implements InputStream, StatusLine, SaveGameDataStore, IOSystem {

  private static final long serialVersionUID = 1L;
  
  private JLabel global1ObjectLabel;
  private JLabel statusLabel;
  private TextViewport viewport;
  private Machine machine;
  private LineEditorImpl lineEditor;
  private GameThread currentGame;
  
  public void init() {
        
    machine = openStoryFile();
    lineEditor = new LineEditorImpl(machine.getServices().getStoryFileHeader(),
        machine.getServices().getZsciiEncoding());
    
    viewport = new TextViewport(machine, lineEditor);
    viewport.setPreferredSize(new Dimension(640, 480));
    viewport.setMinimumSize(new Dimension(400, 300));
    
    if (machine.getServices().getStoryFileHeader().getVersion() <= 3) {
      
      JPanel statusPanel = new JPanel(new GridLayout(1, 2));
      JPanel status1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      JPanel status2Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      statusPanel.add(status1Panel);
      statusPanel.add(status2Panel);        
      global1ObjectLabel = new JLabel(" ");
      statusLabel = new JLabel(" ");
      status1Panel.add(global1ObjectLabel);
      status2Panel.add(statusLabel);    
      getContentPane().add(statusPanel, BorderLayout.NORTH);
    }
    
    getContentPane().add(viewport, BorderLayout.CENTER);
    
    addKeyListener(lineEditor);
    viewport.addKeyListener(lineEditor);
    viewport.addMouseListener(lineEditor);
    
    initMachine();
  }
  
  public void start() {
   
    currentGame = new GameThread(machine, viewport);
    currentGame.start();
  }
  
  private void initMachine() {
    
    // Machine initialization
    
    // Input streams
    FileInputStream fileIs = new FileInputStream(this,
        machine.getServices().getZsciiEncoding());      
    machine.setInputStream(0, this);
    machine.setInputStream(1, fileIs);
    
    // Output streams
    machine.setOutputStream(1, this.getOutputStream());
    machine.selectOutputStream(1, true);
    TranscriptOutputStream transcriptStream = new TranscriptOutputStream(
        this, machine.getServices().getZsciiEncoding());
    machine.setOutputStream(2, transcriptStream);
    machine.selectOutputStream(2, false);
    machine.setOutputStream(3, new MemoryOutputStream(machine));
    machine.selectOutputStream(3, false);
    
    machine.setStatusLine(this);
    machine.setScreen(viewport);
    machine.setSaveGameDataStore(this);        
  }
  
  private Machine openStoryFile() {
    
    String story = getParameter("storyfile");
    java.io.InputStream storyis = null;
    String blorb = getParameter("blorbfile");
    Resources blorbresources = null;
    
    try {
      
      URL storyurl = new URL(getDocumentBase(), story);
      storyis = storyurl.openStream();
      
      if (blorb != null) {
        
        URL blorburl = new URL(getDocumentBase(), blorb);
        java.io.InputStream blorbis = blorburl.openStream();
        blorbresources = readBlorbResources(blorbis);
      }
    
      byte[] storyfile = FileUtils.readFileBytes(storyis);
      GameData gamedata = new GameDataImpl(storyfile, blorbresources);
      StoryFileHeader fileheader = gamedata.getStoryFileHeader();
    
      if (fileheader.getVersion() < 1
          || fileheader.getVersion() == 6) {
      
        JOptionPane.showMessageDialog(null,
          "Story file version 6 is not supported.",
          "Story file read error", JOptionPane.ERROR_MESSAGE);
        stop();
      }
      Machine machine = new MachineImpl();
      InstructionDecoder decoder = new DefaultInstructionDecoder();
      machine.initialize(gamedata, decoder);
      return machine;
      
    } catch (Exception ex) {
      
      ex.printStackTrace();
      
    } finally {
      
      try {
        
        if (storyis != null) storyis.close();
        
      } catch (Exception ex) {
        
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  private Resources readBlorbResources(java.io.InputStream inputstream) {
  
    byte[] data = FileUtils.readFileBytes(inputstream);
    if (data != null) {
    
      FormChunk formchunk = new DefaultFormChunk(new DefaultMemoryAccess(data));
      return new BlorbResources(formchunk);
    }
    return null;
  }
  
  // *************************************************************************
  // ******** StatusLine interface
  // ******************************************
  
  public void updateStatusScore(final String objectName, final int score,
      final int steps) {

    SwingUtilities.invokeLater(new Runnable() {
      
      public void run() {
        
        global1ObjectLabel.setText(objectName);
        statusLabel.setText(score + "/" + steps);
      }
    });
  }
  
  public void updateStatusTime(final String objectName, final int hours,
      final int minutes) {
        
    SwingUtilities.invokeLater(new Runnable() {
      
      public void run() {
        
        global1ObjectLabel.setText(objectName);
        statusLabel.setText(hours + ":" + minutes);
      }
    });
  }

  // *************************************************************************
  // ******** OutputStream interface
  // ******************************************

  public OutputStream getOutputStream() {

    return viewport;
  }
  
  // Save games are stored in memory only
  private WritableFormChunk savegame;
  
  public boolean saveFormChunk(WritableFormChunk formchunk) {

    savegame = formchunk;
    return true;
  }
  
  public FormChunk retrieveFormChunk() {

    return savegame;
  }

  public Writer getTranscriptWriter() {
    
    return new OutputStreamWriter(System.out);
  }
  
  public Reader getInputStreamReader() {
    
    File currentdir = new File(System.getProperty("user.dir"));    
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle("Set input stream file ...");
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      
      try {
        
        return new FileReader(fileChooser.getSelectedFile());
        
      } catch (IOException ex){
        
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  // *************************************************************************
  // ******** InputStream interface
  // ******************************************
  
  public void close() { }

  public void cancelInput() {
    
    lineEditor.cancelInput();
  }
  
  public short getZsciiChar() {

    enterEditMode();
    short zsciiChar = lineEditor.nextZsciiChar();
    leaveEditMode();
    return zsciiChar;
  }
  
  private void enterEditMode() {
    
    if (!lineEditor.isInputMode()) {

      viewport.resetPagers();
      lineEditor.setInputMode(true);
    }
  }
  
  private void leaveEditMode() {
    
    lineEditor.setInputMode(false);
  }  
}
