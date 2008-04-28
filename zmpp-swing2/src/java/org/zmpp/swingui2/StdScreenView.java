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

import java.awt.event.KeyEvent;
import org.zmpp.zscreen.StatusLineModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.zmpp.io.InputStream;
import org.zmpp.vm.Machine;
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
  private int editStart;
  private boolean isEditing;
  private GameExecutor executor;
  
  public StdScreenView() {
    super(new BorderLayout());
    topWindow = new JEditorPane(EDITOR_TYPE, "");
    bottomWindow = new JEditorPane(EDITOR_TYPE, "");
    bottomWindow.setBorder(null);
    topWindow.setBorder(null);
    
    scrollPane = new JScrollPane(bottomWindow);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    topWindow.setPreferredSize(new Dimension(640, 30));
    scrollPane.setPreferredSize(new Dimension(640, 480));
    add(topWindow, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    screenModel.addScreenModelListener(this); 
    installBottomWindowHandlers();
  }
  
  public void runMachine(Machine machine) {
    if (this.isVisible()) {
      executor = new GameExecutor(machine, this);
      executor.run();    
    }
  }
  
  public void setEditing(boolean flag) {
    isEditing = flag;
    viewCursor(flag);
  }
  
  private void consumeKeyEvent(KeyEvent e) {
    if (e != null) e.consume();
  }
  
  private void preventKeyActionIfNeeded(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      // Handle up key
      consumeKeyEvent(e);
    }
    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      // Handle down key
      consumeKeyEvent(e);
    }
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      handleEnterKey(e);
    }
    if (bottomWindow.getCaretPosition() <= editStart &&
        keyCodeLeadsToPreviousPosition(e.getKeyCode())) {
      consumeKeyEvent(e);
    }
  }
  
  private void handleEnterKey(KeyEvent e) {
    if (isEditing) {
      Document doc = bottomWindow.getDocument();
      try {
        String input = doc.getText(editStart, doc.getLength() - editStart);
        System.out.println("ENTER PRESSED, input: " + input);
        inputStream.addInputLine(input);
        consumeKeyEvent(e);
        doc.insertString(doc.getLength(), "\n", null);
        setEditing(false);
        executor.resume();
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  private boolean keyCodeLeadsToPreviousPosition(int keyCode) {
    return keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_LEFT;            
  }
  
  private void installBottomWindowHandlers() {
    // TODO: Mouse clicks can as well influence typing position
    // Make sure mouse clicks do not change caret position
    bottomWindow.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {        
        //System.out.println("keyTyped(): " + e.getKeyChar() + " code: " + e.getKeyCode());
      }

      public void keyPressed(KeyEvent e) {
        //System.out.println("-------------------------");
        //System.out.println("keyPressed(): " + e.getKeyChar() + " code: " + e.getKeyCode());
        preventKeyActionIfNeeded(e);
      }

      public void keyReleased(KeyEvent e) {
        //System.out.println("keyReleased(): " + e.getKeyChar() + " code: " + e.getKeyCode());
        //preventKeyActionIfNeeded(e);
      }
    });
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
  
  private void viewCursor(final boolean flag) {
    runInUIThread(new Runnable() {
      public void run() {
        if (flag) {
          editStart = bottomWindow.getDocument().getLength();
          bottomWindow.setCaretPosition(editStart);
          bottomWindow.requestFocusInWindow();
        } else {
          // might set caret to invisible
        }
      }
    });
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
    runInUIThread(new Runnable() {
      public void run() {
        List<AnnotatedText> buffer = screenModel.getWindow(0).getBuffer();
        for (AnnotatedText str : buffer) {
          appendToBottom(getViewString(str.getText()));
        }
      }
    });
  }
  
  /**
   * Convert ZSCII string to a string in Unicode.
   * @param zsciiString the ZSCII string
   * @return the Unicode representation
   */
  private String getViewString(String zsciiString) {
    String result = zsciiString.replaceAll("\r", "\n");
    return result;
  }
  
  /**
   * A little more readable to execute Runnable within UI thread.
   * @param runnable the Runnable
   */
  private void runInUIThread(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
