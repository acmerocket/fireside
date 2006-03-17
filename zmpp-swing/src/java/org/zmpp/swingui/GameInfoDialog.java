/*
 * $Id$
 * 
 * Created on 2006/03/10
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.zmpp.blorb.InformMetadata;
import org.zmpp.blorb.StoryMetadata;
import org.zmpp.blorb.StoryMetadata.Auxiliary;
import org.zmpp.media.Resources;

/**
 * This dialog displays information about a story given its meta information.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class GameInfoDialog extends JDialog implements ListSelectionListener {

  private static final int STD_WIDTH = 400;
  private static final long serialVersionUID = 1L;
  
  private JList auxlist;
  private JTextArea auxdescarea;
  
  public GameInfoDialog(JFrame owner, Resources resources) {
    
    super(owner, "About " + resources.getMetadata().getStoryInfo().getTitle());
    JTabbedPane tabPane = new JTabbedPane();
    tabPane.add("Info", createInfoPanel(resources));
    
    int coverartnum = this.getCoverartNum(resources);    
    if (coverartnum > 0) {
      
      tabPane.add("Cover Art", createPicturePanel(resources, coverartnum));
    }
    
    List<Auxiliary> auxiliaries =
      resources.getMetadata().getStoryInfo().getAuxiliaries();
    if (auxiliaries != null && auxiliaries.size() > 0) {
      
      tabPane.add("Auxiliaries", createAuxPanel(auxiliaries));
    }
    
    getContentPane().add(tabPane, BorderLayout.CENTER);
    getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
    pack();
    setLocation(owner.getX() + 60, owner.getY() + 50);
  }

  private JPanel createPicturePanel(Resources resources, int coverartnum) {
    
    JPanel picpanel = new JPanel();
    
    if (coverartnum > 0) {
      BufferedImage image =
        resources.getImages().getResource(coverartnum);
      JLabel label = new PictureLabel(image);
      label.setPreferredSize(new Dimension(STD_WIDTH, 400));
      picpanel.add(label);
    }
    return picpanel;
  }
  
  private JComponent createInfoPanel(Resources resources) {
        
    StoryMetadata storyinfo = resources.getMetadata().getStoryInfo();
    Box infopanel = Box.createVerticalBox();
    infopanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    infopanel.setPreferredSize(new Dimension(STD_WIDTH, 400));
    
    JLabel[] labels = new JLabel[6];
    labels[0] = new JLabel("<html><b>Title:</b> " + storyinfo.getTitle()
                           + "</html>");
    labels[1] = new JLabel("<html><b>Author:</b> " + storyinfo.getAuthor()
                           + "</html>");
    labels[2] = new JLabel("<html><b>Year:</b> " + storyinfo.getYear()
                           + "</html>");
    labels[3] = new JLabel("<html><b>Genre:</b> " + storyinfo.getGenre()
                           + "</html>");
    labels[4] = new JLabel("<html><b>Group:</b> " + storyinfo.getGroup()
                           + "</html>");
    labels[5] = new JLabel("<html><b>Headline:</b> " + storyinfo.getHeadline()
                           + "</html>");
    
    Font labelfont = labels[0].getFont().deriveFont(Font.ROMAN_BASELINE);
    
    for (int i = 0; i < labels.length; i++) {
      
      infopanel.add(labels[i]);
      labels[i].setFont(labelfont);
      labels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    infopanel.add(Box.createVerticalStrut(6));
    
    JLabel desclabel = new JLabel("Description: ");
    desclabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    infopanel.add(desclabel);    
    infopanel.add(Box.createVerticalStrut(3));
    
    JTextArea descarea = new JTextArea(storyinfo.getDescription());    
    descarea.setLineWrap(true);
    descarea.setWrapStyleWord(true);
    descarea.setEditable(false);
    
    JScrollPane spane = new JScrollPane(descarea);
    spane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    spane.setPreferredSize(new Dimension(STD_WIDTH, 200));
    spane.setAlignmentX(Component.LEFT_ALIGNMENT);
    infopanel.add(spane);
    return infopanel;
  }
  
  private JPanel createButtonPanel() {
    
    // Set up the other controls
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);
    return buttonPanel;
  }
  
  private int getCoverartNum(Resources resources) {
    
    int coverartnum = resources.getCoverArtNum();
    InformMetadata metadata = resources.getMetadata();
    
    // If the picture number is not in the Frontispiece chunk, retrieve it
    // from the metadata
    if (coverartnum <= 0) {
      coverartnum = metadata.getStoryInfo().getCoverPicture();
    }
    return coverartnum;
  }
  
  private JComponent createAuxPanel(List<Auxiliary> auxiliaries) {
    
    Box auxpanel = Box.createVerticalBox();
    auxpanel.setPreferredSize(new Dimension(STD_WIDTH, 400));
    auxpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    JLabel auxlabel = new JLabel("Auxiliaries: ");
    auxlabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    auxpanel.add(auxlabel);
    auxpanel.add(Box.createVerticalStrut(6));
    
    DefaultListModel listmodel = new DefaultListModel();
    for (Auxiliary aux : auxiliaries) listmodel.addElement(aux);
    
    auxlist = new JList(listmodel);
    auxlist.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);
    auxlist.getSelectionModel().addListSelectionListener(this);
    JScrollPane spane = new JScrollPane(auxlist);
    spane.setPreferredSize(new Dimension(STD_WIDTH, 50));
    spane.setAlignmentX(Component.LEFT_ALIGNMENT);
    spane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    auxpanel.add(spane);
    
    auxpanel.add(Box.createVerticalStrut(15));    
    
    JLabel desclabel = new JLabel("Description: ");
    desclabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    auxpanel.add(desclabel);
    auxpanel.add(Box.createVerticalStrut(6));
    
    auxdescarea = new JTextArea();
    auxdescarea.setLineWrap(true);
    auxdescarea.setWrapStyleWord(true);
    auxdescarea.setEditable(false);
    
    JScrollPane spane2 = new JScrollPane(auxdescarea);    
    spane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    spane2.setPreferredSize(new Dimension(STD_WIDTH, 50));
    spane2.setAlignmentX(Component.LEFT_ALIGNMENT);    
    auxpanel.add(spane2);
    
    auxpanel.add(Box.createVerticalGlue());
    return auxpanel;
  }
  
  /**
   * {@inheritDoc}
   */
  public void valueChanged(ListSelectionEvent e) {
    
    if (!e.getValueIsAdjusting()) {
      
      Auxiliary aux = (Auxiliary) auxlist.getSelectedValue();
      auxdescarea.setText(aux != null ? aux.getDescription() : "");
    }
  }
}
