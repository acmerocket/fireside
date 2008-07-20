/*
 * $Id$
 * 
 * Created on 2006/02/14
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
 * along with ZMPP.  If not,` see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.logging.Logger;
import org.zmpp.vmutil.FastShortStack;
import static org.zmpp.base.MemoryUtil.toUnsigned16;

/**
 * Cpu interface implementation.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class CpuImpl implements Cpu {

  private static final Logger LOG = Logger.getLogger("CpuImpl");

  /**
   * The stack size is now 64 K.
   */
  private static final int STACKSIZE = 32768;
  
  /**
   * The machine object.
   */
  private Machine machine;

  /**
   * This machine's current program counter.
   */
  private int programCounter;
  
  /**
   * This machine's global stack.
   */
  private FastShortStack stack;
  
  /**
   * The routine info.
   */
  private List<RoutineContext> routineContextStack;
  
  /**
   * The start of global variables.
   */
  private int globalsAddress;
  
  public CpuImpl(final Machine machine) {
    super();
    this.machine = machine;
  }
  
  public void reset() {
    stack = new FastShortStack(STACKSIZE);
    routineContextStack = new ArrayList<RoutineContext>();
    globalsAddress = machine.getFileHeader().getGlobalsAddress();
    
    if (machine.getVersion() == 6) {
      // Call main function in version 6
      call(machine.getFileHeader().getProgramStart(), 0, new short[0],
           (short) 0);     
    } else {
      programCounter = machine.getFileHeader().getProgramStart();
    }
  }
 
  /**
   * {@inheritDoc}
   */
  public int getPC() { return programCounter; }

  /**
   * {@inheritDoc}
   */
  public void setPC(final int address) { programCounter = address; }
  
  public void incrementPC(final int offset) { programCounter += offset; }

  /**
   * {@inheritDoc}
   */
  public int unpackStringAddress(int packedAddress) {
    return unpackAddress(packedAddress, false);
  }

  public int unpackAddress(final int packedAddress,
      final boolean isCall) {
    // Version specific packed address translation    
    switch (machine.getVersion()) {
    
      case 1: case 2: case 3:  
        return packedAddress * 2;
      case 4:
      case 5:
        return packedAddress * 4;
      case 6:
      case 7:
        return packedAddress * 4 + 8 *
          (isCall ? machine.getFileHeader().getRoutineOffset() :
                    machine.getFileHeader().getStaticStringOffset());
      case 8:
      default:
        return packedAddress * 8;
    }
  }
  
  /**
   * {@inheritDoc} 
   */
  public void doBranch(short branchOffset, int instructionLength) {
    if (branchOffset >= 2 || branchOffset < 0) {
      setPC(computeBranchTarget(branchOffset, instructionLength));
    } else {
      // FALSE is defined as 0, TRUE as 1, so simply return the offset
      // since we do not have negative offsets
      returnWith(branchOffset);
    }
  }

  private int computeBranchTarget(final short offset,
      final int instructionLength) {     
    return getPC() + instructionLength + offset - 2;
  }
  
  // ********************************************************************
  // ***** Stack operations
  // ***************************************
  /**
   * {@inheritDoc}
   */
  public int getSP() {
    
    return stack.getStackPointer();
  }
  
  /**
   * Sets the global stack pointer to the specified value. This might pop off
   * several values from the stack.
   * 
   * @param stackpointer the new stack pointer value
   */
  private void setSP(final int stackpointer) {

    // remove the last diff elements
    final int diff = stack.getStackPointer() - stackpointer;
    for (int i = 0; i < diff; i++) {
     
      stack.pop();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public short getStackTop() {
    if (stack.size() > 0) {
      return stack.top();
    }
    return -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setStackTop(final short value) {
    stack.replaceTopElement(value);
  }
  
  /**
   * {@inheritDoc}
   */
  public short getStackElement(final int index) {    
    return stack.getValueAt(index);
  }
  
  /**
   * {@inheritDoc}
   */
  public short popStack(int userstackAddress) {
    return userstackAddress == 0 ? getVariable(0) :
      popUserStack(userstackAddress);
  }

  private short popUserStack(int userstackAddress) {
    int numFreeSlots = machine.readUnsigned16(userstackAddress);
    numFreeSlots++;
    machine.writeUnsigned16(userstackAddress, toUnsigned16(numFreeSlots));
    return machine.readSigned16(userstackAddress + (numFreeSlots * 2));
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean pushStack(int userstackAddress, short value) {
    if (userstackAddress == 0) {
      setVariable(0, value);
      return true;
    } else {
      return pushUserStack(userstackAddress, value);
    }    
  }
  
  private boolean pushUserStack(int userstackAddress, short value) {
    int numFreeSlots = machine.readUnsigned16(userstackAddress);
    if (numFreeSlots > 0) {
      machine.writeSigned16(userstackAddress + (numFreeSlots * 2), value);
      machine.writeUnsigned16(userstackAddress, toUnsigned16(numFreeSlots - 1));
      return true;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public short getVariable(final int variableNumber) {
    final Cpu.VariableType varType = getVariableType(variableNumber);
    if (varType == Cpu.VariableType.STACK) {
      if (stack.size() == getInvocationStackPointer()) {
        //throw new IllegalStateException("stack underflow error");
        LOG.severe("stack underflow error");
        return 0;
      } else {   
        return stack.pop();
      }
    } else if (varType == Cpu.VariableType.LOCAL) {
      final int localVarNumber = getLocalVariableNumber(variableNumber);
      checkLocalVariableAccess(localVarNumber);
      return getCurrentRoutineContext().getLocalVariable(localVarNumber);
    } else { // GLOBAL
      return machine.readSigned16(globalsAddress
          + (getGlobalVariableNumber(variableNumber) * 2));
    }
  }
  
  /**
   * Returns the current invocation stack pointer.
   * 
   * @return the invocation stack pointer
   */
  private int getInvocationStackPointer() {
    return getCurrentRoutineContext() == null ? 0 : 
      getCurrentRoutineContext().getInvocationStackPointer();
  }

  /**
   * {@inheritDoc}
   */
  public void setVariable(final int variableNumber, final short value) {
    final Cpu.VariableType varType = getVariableType(variableNumber);
    if (varType == Cpu.VariableType.STACK) {
      stack.push(value);
    } else if (varType == Cpu.VariableType.LOCAL) {
      final int localVarNumber = getLocalVariableNumber(variableNumber);
      checkLocalVariableAccess(localVarNumber);
      getCurrentRoutineContext().setLocalVariable(localVarNumber, value);
    } else {
      machine.writeSigned16(globalsAddress
          + (getGlobalVariableNumber(variableNumber) * 2), value);
    }
  }
  
  /**
   * Returns the variable type for the given variable number.
   * 
   * @param variableNumber the variable number
   * @return STACK if stack variable, LOCAL if local variable, GLOBAL if global
   */
  public static Cpu.VariableType getVariableType(final int variableNumber) {
    if (variableNumber == 0) {
      return Cpu.VariableType.STACK;
    } else if (variableNumber < 0x10) {
      return Cpu.VariableType.LOCAL;
    } else {
      return Cpu.VariableType.GLOBAL;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void pushRoutineContext(final RoutineContext routineContext) {
    routineContext.setInvocationStackPointer(getSP());
    routineContextStack.add(routineContext);
  }
  
  /**
   * {@inheritDoc}
   */
  public void returnWith(final short returnValue) {
    if (routineContextStack.size() > 0) {
      final RoutineContext popped =
        routineContextStack.remove(routineContextStack.size() - 1);
      popped.setReturnValue(returnValue);
    
      // Restore stack pointer and pc
      setSP(popped.getInvocationStackPointer());
      setPC(popped.getReturnAddress());
      final int returnVariable = popped.getReturnVariable();
      if (returnVariable != RoutineContext.DISCARD_RESULT) {
        
        setVariable(returnVariable, returnValue);
      }
    } else {
      throw new IllegalStateException("no routine context active");
    }
  }

  /**
   * {@inheritDoc}
   */
  public RoutineContext getCurrentRoutineContext() {
    if (routineContextStack.size() == 0) {
      return null;
    }
    return routineContextStack.get(routineContextStack.size() - 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<RoutineContext> getRoutineContexts() {
    return Collections.unmodifiableList(routineContextStack);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setRoutineContexts(final List<RoutineContext> contexts) {
    routineContextStack.clear();
    for (RoutineContext context : contexts) {
      
      routineContextStack.add(context);
    }
  }
  
  /**
   * This function is basically exposed to the debug application.
   * 
   * @return the current routine stack pointer
   */
  public int getRoutineStackPointer() {
    return routineContextStack.size();
  }
  
  public RoutineContext call(final int packedRoutineAddress,
      final int returnAddress, final short[] args, final int returnVariable) {
    
    final int routineAddress =
      unpackAddress(packedRoutineAddress, true);
    final int numArgs = args == null ? 0 : args.length;    
    final RoutineContext routineContext = decodeRoutine(routineAddress);
    
    // Sets the number of arguments
    routineContext.setNumArguments(numArgs);
    
    // Save return parameters
    routineContext.setReturnAddress(returnAddress);
    
    // Only if this instruction stores a result
    if (returnVariable == RoutineContext.DISCARD_RESULT) {
      
      routineContext.setReturnVariable(RoutineContext.DISCARD_RESULT);
      
    } else {
      
      routineContext.setReturnVariable(returnVariable);
    }      
    
    // Set call parameters into the local variables
    // if there are more parameters than local variables,
    // those are thrown away
    final int numToCopy = Math.min(routineContext.getNumLocalVariables(),
                                   numArgs);
    
    for (int i = 0; i < numToCopy; i++) {
      
      routineContext.setLocalVariable(i, args[i]);
    }
    
    // save invocation stack pointer
    routineContext.setInvocationStackPointer(getSP());
    
    // Pushes the routine context onto the routine stack
    pushRoutineContext(routineContext);
    
    // Jump to the address
    setPC(routineContext.getStartAddress());
    return routineContext;
  }

  // ************************************************************************
  // ****** Private functions
  // ************************************************
  
  /**
   * Decodes the routine at the specified address.
   * 
   * @param routineAddress the routine address
   * @return a RoutineContext object
   */
  private RoutineContext decodeRoutine(final int routineAddress) {
    final int numLocals = machine.readUnsigned8(routineAddress);
    final short[] locals = new short[numLocals];
    int currentAddress = routineAddress + 1;
    
    if (machine.getVersion() <= 4) {
      // Only story files <= 4 actually store default values here,
      // after V5 they are assumed as being 0 (standard document 1.0, S.5.2.1) 
      for (int i = 0; i < numLocals; i++) {
        locals[i] = machine.readSigned16(currentAddress);
        currentAddress += 2;
      }
    }
    final RoutineContext info = new RoutineContext(currentAddress, numLocals);
    
    for (int i = 0; i < numLocals; i++) {
      
      info.setLocalVariable(i, locals[i]);
    }
    return info;
  }
    
  /**
   * Returns the local variable number for a specified variable number.
   * 
   * @param variableNumber the variable number in an operand (0x01-0x0f)
   * @return the local variable number
   */
  private int getLocalVariableNumber(final int variableNumber) {
    return variableNumber - 1;
  }
  
  /**
   * Returns the global variable for the specified variable number.
   * 
   * @param variableNumber a variable number (0x10-0xff)
   * @return the global variable number
   */
  private int getGlobalVariableNumber(final int variableNumber) {
    return variableNumber - 0x10;
  }
  
  /**
   * This function throws an exception if a non-existing local variable
   * is accessed on the current routine context or no current routine context
   * is set.
   * 
   * @param localVariableNumber the local variable number
   */
  private void checkLocalVariableAccess(final int localVariableNumber) {
    if (routineContextStack.size() == 0) {
      throw new IllegalStateException("no routine context set");
    }
    
    if (localVariableNumber >= getCurrentRoutineContext().getNumLocalVariables()) {
      throw new IllegalStateException("access to non-existent local variable: "
                                      + localVariableNumber);
    }
  }  
}
