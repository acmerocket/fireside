/*
 * $Id$
 * 
 * Created on 01/23/2006
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
package test.zmpp.swingui;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.swingui.Canvas;
import org.zmpp.swingui.LineEditor;
import org.zmpp.swingui.TopWindow;
import org.zmpp.vm.ScreenModel;

public class TopWindowTest extends MockObjectTestCase {

  private Mock mockScreen;
  private ScreenModel screen;
  private Mock mockCanvas;
  private Canvas canvas;
  private Mock mockEditor;
  private LineEditor editor;
  
  private TopWindow window;
  
  protected void setUp() throws Exception {

    mockScreen = mock(ScreenModel.class);
    screen = (ScreenModel) mockScreen.proxy();
    mockCanvas = mock(Canvas.class);
    canvas = (Canvas) mockCanvas.proxy();
    mockEditor = mock(LineEditor.class);
    editor = (LineEditor) mockEditor.proxy();
    
    window = new TopWindow(screen, editor, canvas);
  }

  public void testUnbuffered() {
    
    assertFalse(window.isBuffered());
    window.setBufferMode(true);
    assertFalse(window.isBuffered());
    window.setBufferMode(false);
    assertFalse(window.isBuffered());
  }

  public void testNonPaged() {
    
    assertFalse(window.isPagingEnabled());
    window.setPagingEnabled(true);
    assertFalse(window.isPagingEnabled());
    window.setPagingEnabled(false);
    assertFalse(window.isPagingEnabled());
  }
}
