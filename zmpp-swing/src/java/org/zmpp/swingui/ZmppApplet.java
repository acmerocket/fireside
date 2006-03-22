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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;

/**
 * This is the applet class for ZMPP.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZmppApplet extends JApplet
implements InputStream, StatusLine, IOSystem {

  private static final long serialVersionUID = 1L;
  
  private JLabel global1ObjectLabel;
  private JLabel statusLabel;
  private ScreenModel screen;
  private Machine machine;
  private LineEditorImpl lineEditor;
  private GameThread currentGame;
  private boolean savetofile;
  
  public void init() {
        
    String story = getParameter("storyfile");
    String blorb = getParameter("blorbfile");
    String saveto = getParameter("saveto");
    savetofile = "file".equalsIgnoreCase(saveto);
    
    try {

      URL blorburl = null;
      if (blorb != null) blorburl = new URL(getDocumentBase(), blorb);
      
      AppletMachineFactory factory = null;

      if (story != null) {

        URL storyurl = new URL(getDocumentBase(), story);
        factory = new AppletMachineFactory(this, storyurl, blorburl, savetofile);

      } else {

        factory = new AppletMachineFactory(this, blorburl, savetofile);
      }
      machine = factory.buildMachine();
      
    } catch (Exception ex) {
      
      ex.printStackTrace();      
    }
  }
  
  public void initUI(Machine machine) {
    
    lineEditor = new LineEditorImpl(machine.getGameData().getStoryFileHeader(),
        machine.getGameData().getZsciiEncoding());
    
    JComponent view = null;
    
    if (machine.getGameData().getStoryFileHeader().getVersion() == 6) {
      
      view = new Viewport6(machine, lineEditor);
      screen = (ScreenModel) view;
      
    } else {
      
      view = new TextViewport(machine, lineEditor);
      screen = (ScreenModel) view;
    }
    view.setPreferredSize(new Dimension(640, 480));
    view.setMinimumSize(new Dimension(400, 300));
    
    if (machine.getGameData().getStoryFileHeader().getVersion() <= 3) {
      
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
      getContentPane().add(view, BorderLayout.CENTER);
      
    } else {
            
      setContentPane(view);
    }
    
    addKeyListener(lineEditor);
    view.addKeyListener(lineEditor);
    view.addMouseListener(lineEditor);
  }
  
  public void start() {
   
    currentGame = new GameThread(machine, screen);
    currentGame.start();
  }
  
  public ScreenModel getScreenModel() {
    
    return screen;
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

      screen.resetPagers();
      lineEditor.setInputMode(true);
    }
  }
  
  private void leaveEditMode() {
    
    lineEditor.setInputMode(false);
  }  
}
