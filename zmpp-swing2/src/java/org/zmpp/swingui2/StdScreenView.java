/*
 * $Id$
 * 
 * Created on 2008/04/25
 * Copyright 2005-2008 by Wei-ju Wu
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
package org.zmpp.swingui2;

import org.zmpp.zscreen.StatusLineModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.zmpp.io.InputStream;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.zscreen.BufferedScreenModel;
import org.zmpp.zscreen.BufferedScreenModel.ScreenModelListener;

/**
 * A standard Swing component to act as main user interface to the Z-machine.
 * The new ZMPP Swing screen model employs MVC in a stricter sense than in
 * previous versions. It employs an underlying BufferedScreenModel as its
 * model component and listens to update events from the model in order
 * to update the view.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StdScreenView extends JPanel
  implements ScreenModelListener {

  private static String EDITOR_TYPE = "text/html";
  private JEditorPane topWindow, bottomWindow;
  private JScrollPane scrollPane;
  private BufferedScreenModel screenModel = new BufferedScreenModel();
  private StatusLineModel statusLineModel = new StatusLineModel(screenModel);
  private LineBufferInputStream inputStream = new LineBufferInputStream();

  
  public StdScreenView() {
    super(new BorderLayout());
    topWindow = new JEditorPane(EDITOR_TYPE, "");
    bottomWindow = new JEditorPane(EDITOR_TYPE, "");
    
    scrollPane = new JScrollPane(bottomWindow);
    scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    topWindow.setPreferredSize(new Dimension(640, 30));
    scrollPane.setPreferredSize(new Dimension(640, 480));
    add(topWindow, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    screenModel.addScreenModelListener(this);    
  }
  
  public BufferedScreenModel getScreenModel() {
    return screenModel;
  }
  
  public StatusLineModel getStatusLineModel() {
    return statusLineModel;
  }
  
  public InputStream getKeyboardInputStream() {
    return inputStream;
  }
  
  public void viewCursor() {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          bottomWindow.setCaretPosition(bottomWindow.getDocument().getLength());
          bottomWindow.requestFocusInWindow();
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private void appendToBottom(String str) {
    //MutableAttributeSet attributes = new SimpleAttributeSet();
    //attributes.addAttribute(CharacterConstants.StrikeThrough, true);
    AttributeSet attributes = null;
    try {
      Document doc = bottomWindow.getDocument();
      doc.insertString(doc.getLength(), str, attributes);
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void screenModelUpdated(final BufferedScreenModel screenModel) {
    try {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          List<AnnotatedText> buffer = screenModel.getWindow(0).getBuffer();
          for (AnnotatedText str : buffer) {
            appendToBottom(getViewString(str.getText()));
          }
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private String getViewString(String zsciiString) {
    String result = zsciiString.replaceAll("\r", "\n");
    return result;
  }
}
