/*
 * $Id$
 * 
 * Created on 2008/07/16
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
package test.zmpp.windowing;

import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.vm.ScreenModel;
import org.zmpp.windowing.TextAnnotation;

/**
 * Test class for TextAnnotation.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class TextAnnotationTest {

  @Test
  public void testCreateFull() {
    TextAnnotation annot = new TextAnnotation(ScreenModel.FONT_NORMAL,
            ScreenModel.TEXTSTYLE_ITALIC, ScreenModel.COLOR_BLUE,
            ScreenModel.COLOR_YELLOW);
    assertEquals(ScreenModel.FONT_NORMAL, annot.getFont());
  }
}