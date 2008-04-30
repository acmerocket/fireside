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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineFactory;
import org.zmpp.vm.MachineFactory.MachineInitCallback;
import org.zmpp.vm.MachineFactory.MachineInitStruct;
import org.zmpp.windowing.AnnotatedText;
import org.zmpp.zscreen.BufferedScreenModel;
import org.zmpp.zscreen.BufferedScreenModel.ScreenModelListener;
import org.zmpp.zscreen.BufferedScreenModel.StatusLineListener;

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
  implements ScreenView, ScreenModelListener, StatusLineListener,
             MachineInitCallback {

  private static String EDITOR_TYPE = "text/html";
  private JPanel mainPanel = new JPanel(new BorderLayout());
  private JEditorPane topWindow, bottomWindow;
  private JScrollPane scrollPane;
  private JLabel objectDescLabel = new JLabel(" "),
                 statusLabel = new JLabel(" ");  
  private BufferedScreenModel screenModel = new BufferedScreenModel();
  private LineBufferInputStream inputStream = new LineBufferInputStream();
  private int editStart;
  private boolean isReadLine, isReadChar;
  private ExecutionControl executionControl;
  private JFrame frame;
  
  /**
   * Constructor.
   * @param frame the application frame, can be null in case of applet mode
   */
  public StdScreenView(JFrame frame) {
    super(new BorderLayout());
    createTopWindow();
    createBottomWindow();
    add(createStatusPanel(), BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
    mainPanel.setBorder(null);
    mainPanel.add(topWindow, BorderLayout.NORTH);    
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    screenModel.addScreenModelListener(this);
    screenModel.addStatusLineListener(this);
    installBottomWindowHandlers();
  }
  
  /**
   * Constructor.
   */
  public StdScreenView() {
    this(null);
  }

  private void createTopWindow() {
    topWindow = new JEditorPane(EDITOR_TYPE, "");
    topWindow.setBorder(null);
    topWindow.setPreferredSize(new Dimension(640, 0));    
  }

  private void createBottomWindow() {
    bottomWindow = new JEditorPane(EDITOR_TYPE, "");
    bottomWindow.setBorder(null);
    scrollPane = new JScrollPane(bottomWindow);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(640, 480));
  }
  
  private JPanel createStatusPanel() {
    JPanel statusPanel = new JPanel(new GridLayout(1, 2));
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    statusPanel.add(leftPanel);
    statusPanel.add(rightPanel);
    leftPanel.add(objectDescLabel);
    rightPanel.add(statusLabel);
    return statusPanel;
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
  
  // *********************************************************************
  // ****** MachineInitCallback
  // ***************************************
  
  /**
   * {@inheritDoc}
   */
  public void initUI(Machine machine) {
    int version = machine.getVersion();
    System.out.println("initUI, story file version: " + version);
  }

  /**
   * {@inheritDoc}
   */
  public void reportInvalidStory() {
    System.out.println("invalid story");
  }
  
  // *********************************************************************
  // ****** ScreenModelListener
  // ***************************************
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

  // *********************************************************************
  // ****** StatusLineListener
  // ***************************************
  /**
   * {@inheritDoc}
   */
  public void statusLineUpdated(String objectDescription, String status) {
    objectDescLabel.setText(objectDescription);
    statusLabel.setText(status);
  }
  
  // *********************************************************************
  // ****** ScreenView
  // ***************************************
  
  /**
   * {@inheritDoc}
   */
  public void startGame(File storyFile) throws IOException {
    MachineInitStruct initStruct = new MachineInitStruct();
    initStruct.storyFile = storyFile;
    initStruct.screenModel = screenModel;
    initStruct.statusLine = screenModel;
    initStruct.keyboardInputStream = inputStream;
    
    MachineFactory factory = new MachineFactory(initStruct, this);
    Machine machine = factory.buildMachine();
    if (this.isVisible()) {
      executionControl = new ExecutionControl(machine, this);
      executionControl.run();    
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setReadLine(boolean flag) {
    isReadLine = flag;
    viewCursor(flag);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setReadChar(boolean flag) {
    isReadChar = flag;
    viewCursor(flag);
  }
  
  private void resetToRunMode() {
    setReadLine(false);
    setReadChar(false);
  }
  
  private void consumeKeyEvent(KeyEvent e) {
    if (e != null) e.consume();
  }
  
  private void preventKeyActionIfNeeded(KeyEvent e) {
    if (isReadChar) {
      // Blabla TODO
      inputStream.addInputLine("\r");
      consumeKeyEvent(e);
      resetToRunMode();
    }
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
    if (isReadLine) {
      Document doc = bottomWindow.getDocument();
      try {
        String input = doc.getText(editStart, doc.getLength() - editStart);
        System.out.println("ENTER PRESSED, input: " + input);
        inputStream.addInputLine(convertToZsciiInputLine(input));
        consumeKeyEvent(e);
        doc.insertString(doc.getLength(), "\n", null);
        resetToRunMode();
        executionControl.run();
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  private String convertToZsciiInputLine(String input) {
    return input + "\r";
  }
  
  private boolean keyCodeLeadsToPreviousPosition(int keyCode) {
    return keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_LEFT;            
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
