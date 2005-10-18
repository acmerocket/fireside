/*
 * $Id$
 * 
 * Created on 24.09.2005
 * Copyright 2005 by Wei-ju Wu
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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.zmpp.base.MemoryAccess;
import org.zmpp.vm.Abbreviations;
import org.zmpp.vm.Machine3;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.ZObject;
import org.zmpp.vmutil.ZsciiConverter;
import org.zmpp.vmutil.ZsciiString;

/**
 * This class starts the ZMPP swing interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Main extends JFrame {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1L;
  
  private JLabel global1ObjectLabel;
  private JLabel statusLabel;
  
  public Main(Machine3 machine) {
    
    super("Z-Machine Preservation Project Version 1.0 (alpha)");
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JViewport viewport = new JViewport();
    JScrollPane spane = new JScrollPane(viewport);
    JPanel statusPanel = new JPanel(new GridLayout(1, 2));
    JPanel status1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel status2Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    statusPanel.add(status1Panel);
    statusPanel.add(status2Panel);
    
    int objNum = machine.getVariable(0x10);    
    ZObject obj = machine.getObjectTree().getObject(objNum);
    String objectName = (new ZsciiString(machine.getMemoryAccess(),
          obj.getPropertiesDescriptionAddress())).toString();
    
    global1ObjectLabel = new JLabel("West of House");
    statusLabel = new JLabel("0/0");
    status1Panel.add(global1ObjectLabel);
    status2Panel.add(statusLabel);
    
    getContentPane().add(statusPanel, BorderLayout.NORTH);
    getContentPane().add(spane, BorderLayout.CENTER);
    
    JMenuBar menubar = new JMenuBar();
    setJMenuBar(menubar);
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    menubar.add(fileMenu);
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.setMnemonic('x');
    fileMenu.add(exitItem);
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        System.exit(0);
      }
    });
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    if (args.length == 0) {
      
      System.out.println("usage: java org.zmpp.swingui.Main <storyfile>");
    }
    try {
      File storyfile = new File(args[0]);
      RandomAccessFile file = new RandomAccessFile(storyfile, "r");
      int fileSize = (int) file.length();
      byte[] buffer = new byte[fileSize];    
      file.read(buffer);
      file.close();
      MemoryAccess memaccess = new MemoryAccess(buffer);
      StoryFileHeader fileheader = new StoryFileHeader(memaccess);
      Abbreviations abbreviations = new Abbreviations(memaccess,
        fileheader.getAbbreviationsAddress());
      ZsciiConverter converter = new ZsciiConverter(3, abbreviations);
      ZsciiString.setZsciiConverter(converter);      
      Machine3 machineState = new Machine3();
      machineState.initialize(memaccess, fileheader);
      
      JFrame frame = new Main(machineState);
      frame.pack();
      frame.setVisible(true);
            
    } catch (IOException ex) {
      
      ex.printStackTrace();
    }
  }
}
