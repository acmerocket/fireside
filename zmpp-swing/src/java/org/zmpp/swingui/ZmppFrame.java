/*
 * $Id$
 * 
 * Created on 2005/10/19
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.blorb.StoryMetadata;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.io.IOSystem;
import org.zmpp.io.InputStream;
import org.zmpp.media.Resources;
import org.zmpp.vm.Machine;
import org.zmpp.vm.SaveGameDataStore;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.StatusLine;

/**
 * This class is the main frame for ZMPP run as an application. 
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZmppFrame extends JFrame
implements InputStream, StatusLine, SaveGameDataStore, IOSystem {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private JLabel global1ObjectLabel;
  private JLabel statusLabel;
  private ScreenModel screen;
  private Machine machine;
  private LineEditorImpl lineEditor;
  private GameThread currentGame;
  private boolean isMacOs;

  /**
   * Constructor.
   * 
   * @param machine a Machine object
   */
  public ZmppFrame(final Machine machine) {
    
    super(Main.APPNAME);
    
    this.machine = machine;
    lineEditor = new LineEditorImpl(machine.getGameData().getStoryFileHeader(),
        machine.getGameData().getZsciiEncoding());
    
    isMacOs = (System.getProperty("mrj.version") != null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    JComponent view = null;
    
    if (machine.getGameData().getStoryFileHeader().getVersion() ==  6) {
      
      view = new Viewport6(machine, lineEditor);
      screen = (ScreenModel) view;
      
    } else {
      
      view = new TextViewport(machine, lineEditor);
      screen = (ScreenModel) view;
    }
    view.setPreferredSize(new Dimension(640, 476));
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
    }
    getContentPane().add(view, BorderLayout.CENTER);
    
    JMenuBar menubar = new JMenuBar();
    setJMenuBar(menubar);
    // Menus need to be slightly different on MacOS
    if (isMacOs) {
      
      // Here Macos specific stuff
      
    } else {
      
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      menubar.add(fileMenu);

      // Quit is already in the application menu
      JMenuItem exitItem = new JMenuItem("Exit");
      exitItem.setMnemonic('x');
      fileMenu.add(exitItem);
      exitItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        
          System.exit(0);
        }
      });      
    }
    JMenu helpMenu = new JMenu("Help");
    menubar.add(helpMenu);
    helpMenu.setMnemonic('H');
    
    JMenuItem aboutItem = new JMenuItem("About ZMPP...");
    aboutItem.setMnemonic('A');
    helpMenu.add(aboutItem);
    aboutItem.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {

        about();
      }
    });
    
    addKeyListener(lineEditor);
    view.addKeyListener(lineEditor);
    view.addMouseListener(lineEditor);
    
    // Add an info dialog and a title if metadata exists
    Resources resources = machine.getGameData().getResources();
    if (resources != null && resources.getMetadata() != null) {
      
      StoryMetadata storyinfo = resources.getMetadata().getStoryInfo();
      setTitle(Main.APPNAME + " - " + storyinfo.getTitle()
          + " (" + storyinfo.getAuthor() + ")");
      
      JMenuItem aboutGameItem = new JMenuItem("About " + storyinfo.getTitle()
                                              + " ...");
      helpMenu.add(aboutGameItem);
      aboutGameItem.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {

          aboutGame();
        }
      });
    }
  }

  /**
   * Access to screen model.
   * 
   * @return the screen model
   */
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

  // *************************************************************************
  // ******** SaveGameDataStore interface
  // ******************************************

  public boolean saveFormChunk(WritableFormChunk formchunk) {
  
    File currentdir = new File(System.getProperty("user.dir"));    
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle("Save game ...");
    
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      
      File savefile = fileChooser.getSelectedFile();
      RandomAccessFile raf = null;
      try {
        
        raf = new RandomAccessFile(savefile, "rw");
        byte[] data = formchunk.getBytes();
        raf.write(data);
        return true;
        
      } catch (IOException ex) {
       
        ex.printStackTrace();
        
      } finally {
        
        if (raf != null) try { raf.close(); } catch (Exception ex) { }
      }
    }
    return false;
  }
  
  public FormChunk retrieveFormChunk() {
    
    File currentdir = new File(System.getProperty("user.dir"));    
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle("Restore game...");
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      
      File savefile = fileChooser.getSelectedFile();
      RandomAccessFile raf = null;
      try {
        
        raf = new RandomAccessFile(savefile, "r");
        byte[] data = new byte[(int) raf.length()];
        raf.readFully(data);
        return new DefaultFormChunk(new DefaultMemoryAccess(data));
        
      } catch (IOException ex) {
       
        ex.printStackTrace();
        
      } finally {
        
        if (raf != null) try { raf.close(); } catch (Exception ex) { }
      }
    }
    return null;
  }

  // *************************************************************************
  // ******** IOSystem interface
  // ******************************************

  public Writer getTranscriptWriter() {
    
    File currentdir = new File(System.getProperty("user.dir"));    
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle("Set transcript file ...");
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      
      try {
        
        return new FileWriter(fileChooser.getSelectedFile());
        
      } catch (IOException ex) {
      
        ex.printStackTrace();
      }
    }
    return null;
  }
  
  public Reader getInputStreamReader() {
    
    File currentdir = new File(System.getProperty("user.dir"));    
    JFileChooser fileChooser = new JFileChooser(currentdir);
    fileChooser.setDialogTitle("Set input stream file ...");
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      
      try {
        
        return new FileReader(fileChooser.getSelectedFile());
        
      } catch (IOException ex) {
        
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
  
  /**
   * Reads a character from the keyboard.
   * 
   * @return the next character
   */
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
  
  private void about() {
    
    JOptionPane.showMessageDialog(this,
        Main.APPNAME + "\n? 2005-2006 by Wei-ju Wu\n" +
        "This software is released under the GNU public license.",
        "About...",
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void aboutGame() {
    
    GameInfoDialog dialog = new GameInfoDialog(this,
        machine.getGameData().getResources());
    dialog.setVisible(true);
  }
  
  public void startMachine() {
    
    currentGame = new GameThread(machine, screen);
    currentGame.start();
  }
}
