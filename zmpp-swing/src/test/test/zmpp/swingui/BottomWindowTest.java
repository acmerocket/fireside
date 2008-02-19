/*
 * $Id$
 * 
 * Created on 01/23/2006
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
package test.zmpp.swingui;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.swingui.BottomWindow;
import org.zmpp.swingui.Viewport;

public class BottomWindowTest extends MockObjectTestCase {

  private Mock mockScreen;
  private Viewport screen;
  
  private BottomWindow window;
  
  protected void setUp() {
    
    mockScreen = mock(Viewport.class);
    screen = (Viewport) mockScreen.proxy();
    
    window = new BottomWindow(screen);
  }
  
  public void testInitial() {
    
    assertTrue(window.isBuffered());
    assertTrue(window.isPagingEnabled());
  }
}
