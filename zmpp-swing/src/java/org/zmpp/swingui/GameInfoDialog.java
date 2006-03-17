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
import java.awt.Font;
import java.awt.GridLayout;
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
import org.zmpp.blorb.StoryMetadata;
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
    tabPane.add("Info", createInfoPanel(resources));
    tabPane.add("Cover Art", createPicturePanel(resources));
    add(tabPane, BorderLayout.CENTER);    
    add(createButtonPanel(), BorderLayout.SOUTH);
    pack();
    setLocation(owner.getX() + 60, owner.getY() + 50);
  }

  private JPanel createPicturePanel(Resources resources) {
    
    JPanel picpanel = new JPanel();
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
      picpanel.add(label);
    }
    return picpanel;
  }
  
  private JPanel createInfoPanel(Resources resources) {
        
    JPanel infopanel = new JPanel(new BorderLayout());    
    StoryMetadata storyinfo = resources.getMetadata().getStoryInfo();
    JPanel basicspanel = new JPanel(new GridLayout(0, 1));
    JPanel titlepanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    basicspanel.add(titlepanel);
    titlepanel.add(new JLabel("Title: "));
    titlepanel.add(new JLabel(storyinfo.getTitle()));
    infopanel.add(basicspanel, BorderLayout.NORTH);
    
    String description = storyinfo.getDescription();    
    StringBuilder builder = new StringBuilder();    
    builder.append("<html>");
    builder.append("<b>Title:</b> ");
    builder.append(storyinfo.getTitle() + "<br>");
    builder.append("<b>Author:</b> ");
    builder.append(storyinfo.getAuthor() + "<br>");
    builder.append("<b>Year:</b> ");
    builder.append(storyinfo.getYear() + "<br>");
    builder.append("<b>Genre:</b> ");
    builder.append(storyinfo.getGenre() + "<br>");
    builder.append("<b>Group:</b> ");
    builder.append(storyinfo.getGroup() + "<br>");
    builder.append("<b>Headline:</b> ");
    builder.append(storyinfo.getHeadline() + "<br>");
    builder.append("<b>Description:</b> ");
    builder.append("<p>" + description + "</p>");
    builder.append("</html>");
    JLabel basicinfo = new JLabel(builder.toString());
    basicinfo.setFont(basicinfo.getFont().deriveFont(Font.PLAIN));
    basicinfo.setPreferredSize(new Dimension(400, 400));
    infopanel.add(basicinfo);    
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
}
