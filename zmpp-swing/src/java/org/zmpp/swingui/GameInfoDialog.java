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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.zmpp.blorb.InformMetadata;
import org.zmpp.media.Resources;

/**
 * This dialog displays information about a story given its meta information.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class GameInfoDialog extends JDialog {

  private static final long serialVersionUID = 1L;
  
  public GameInfoDialog(JFrame owner, Resources resources) {
    
    super(owner, "About " + resources.getMetadata().getStoryInfo().getTitle());
    JTabbedPane tabPane = new JTabbedPane();
    JPanel infopanel = new JPanel();
    tabPane.add("Info", infopanel);
    add(tabPane, BorderLayout.CENTER);
    
    int coverartnum = resources.getCoverArtNum();
    InformMetadata metadata = resources.getMetadata();
    
    // If the picture number is not in the Frontispiece chunk, retrieve it
    // from the metadata
    if (coverartnum <= 0) {
      coverartnum = metadata.getStoryInfo().getCoverPicture();
    }
    if (coverartnum > 0) {
      BufferedImage image =
        resources.getImages().getResource(coverartnum);
      JLabel label = new PictureLabel(image);
      label.setPreferredSize(new Dimension(400, 400));
      infopanel.add(label);
    }

    // Set up the other controls
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonPanel.add(okButton);
    add(buttonPanel, BorderLayout.SOUTH);
    pack();
  }
}
