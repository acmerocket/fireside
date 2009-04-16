/*
 * $Id$
 *
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
package test.zmpp.instructions;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.Machine;

/**
 * Super class for Instruction tests.
 * @author Wei-ju Wu
 * @version 1.5
 */
public abstract class InstructionTestBase {
  protected Mockery context = new JUnit4Mockery();
	protected Machine machine;
	protected OutputStream outputStream;
	protected Dictionary dictionary;

	protected void setUp() throws Exception { 
		machine = context.mock(Machine.class);
		outputStream = context.mock(OutputStream.class);
		dictionary = context.mock(Dictionary.class);
	}

  protected void expectStoryVersion(final int version) {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getVersion(); will(returnValue(version));
    }});
  }
}
