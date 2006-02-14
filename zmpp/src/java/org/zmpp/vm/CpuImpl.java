/*
 * $Id$
 * 
 * Created on 2006/02/14
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
package org.zmpp.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zmpp.base.MemoryAccess;
import org.zmpp.encoding.ZsciiString;

public class CpuImpl implements Cpu {

  private GameData gamedata;
  
  private Machine machine;

  /**
   * This machine's current program counter.
   */
  private int programCounter;
  
  /**
   * This machine's global stack.
   */
  private List<Short> stack;  
  
  /**
   * The routine info.
   */
  private List<RoutineContext> routineContextStack;
  
  /**
   * The start of global variables.
   */
  private int globalsAddress;
  
  /**
   * The instruction decoder.
   */
  private InstructionDecoder decoder;  
  
  /**
   * This flag indicates the run status.
   */
  private boolean running;  
  
  public CpuImpl(Machine machine, InstructionDecoder decoder) {
  
    this.gamedata = machine.getGameData();
    this.machine = machine;
    this.decoder = decoder;
    this.running = true;
  }
  
  public void reset() {
    
    programCounter = gamedata.getStoryFileHeader().getProgramStart();    
    decoder.initialize(machine, gamedata.getMemoryAccess());
    stack = new ArrayList<Short>();
    routineContextStack = new ArrayList<RoutineContext>();
    globalsAddress = gamedata.getStoryFileHeader().getGlobalsAddress();
  }
 
  /**
   * {@inheritDoc}
   */
  public int getProgramCounter() {
    
    return programCounter;
  }

  /**
   * {@inheritDoc}
   */
  public void setProgramCounter(int address) {

    programCounter = address;
  }
  
  public void incrementProgramCounter(int offset) {
    
    programCounter += offset;
  }

  /**
   * {@inheritDoc}
   */
  public Instruction nextStep() {
    
    Instruction instruction = decoder.decodeInstruction(getProgramCounter());
    return instruction;
  }
    
  /**
   * {@inheritDoc}
   */
  public int translatePackedAddress(int packedAddress, boolean isCall) {
  
    // Version specific packed address translation
    switch (gamedata.getStoryFileHeader().getVersion()) {
    
      case 1: case 2: case 3:  
        return packedAddress * 2;
      case 4:
      case 5:
        return packedAddress * 4;
      case 6:
      case 7:
        return packedAddress * 4 + 8 *
          (isCall ? gamedata.getStoryFileHeader().getRoutineOffset() :
                    gamedata.getStoryFileHeader().getStaticStringOffset());
      case 8:
      default:
        return packedAddress * 8;
    }
  }
  
  /**
   * {@inheritDoc} 
   */
  public int computeBranchTarget(short offset, int instructionLength) {
        
    return getProgramCounter() + instructionLength + offset - 2;
  }
  
  /**
   * {@inheritDoc}
   */
  public void halt(String errormsg) {
  
    machine.print(new ZsciiString(errormsg));
    running = false;
  }  

  /**
   * {@inheritDoc}
   */
  public boolean isRunning() {
    
    return running;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setRunning(boolean flag) {
    
    running = flag;
  }
  
  // ********************************************************************
  // ***** Stack operations
  // ***************************************
  /**
   * {@inheritDoc}
   */
  public int getStackPointer() {
    
    return stack.size();
  }
  
  /**
   * Sets the global stack pointer to the specified value. This might pop off
   * several values from the stack.
   * 
   * @param stackpointer the new stack pointer value
   */
  private void setStackPointer(int stackpointer) {

    // remove the last diff elements
    int diff = getStackPointer() - stackpointer;
    for (int i = 0; i < diff; i++) {
     
      stack.remove(stack.size() - 1);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public short getStackTopElement() {
    
    if (stack.size() > 0) {
      
      return stack.get(stack.size() - 1);
    }
    return -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setStackTopElement(short value) {
    
    stack.set(stack.size() - 1, value);
  }
  
  /**
   * {@inheritDoc}
   */
  public short getStackElement(int index) {
    
    return stack.get(index);
  }
  
  /**
   * {@inheritDoc}
   */
  public short getVariable(int variableNumber) {

    Cpu.VariableType varType = getVariableType(variableNumber);
    if (varType == Cpu.VariableType.STACK) {
      
      if (stack.size() == 0) {
        
        throw new IllegalStateException("stack is empty");
        
      } else {
   
        return stack.remove(stack.size() - 1);
      }
      
    } else if (varType == Cpu.VariableType.LOCAL) {
      
      int localVarNumber = getLocalVariableNumber(variableNumber);
      checkLocalVariableAccess(localVarNumber);
      return getCurrentRoutineContext().getLocalVariable(localVarNumber);
      
    } else { // GLOBAL
      
      return gamedata.getMemoryAccess().readShort(globalsAddress
          + (getGlobalVariableNumber(variableNumber) * 2));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setVariable(int variableNumber, short value) {

    Cpu.VariableType varType = getVariableType(variableNumber);
    if (varType == Cpu.VariableType.STACK) {
      
      stack.add(value);
      
    } else if (varType == Cpu.VariableType.LOCAL) {
      
      int localVarNumber = getLocalVariableNumber(variableNumber);
      checkLocalVariableAccess(localVarNumber);
      getCurrentRoutineContext().setLocalVariable(localVarNumber, value);
      
    } else {
      
      gamedata.getMemoryAccess().writeShort(globalsAddress
          + (getGlobalVariableNumber(variableNumber) * 2), value);
    }
  }
  
  /**
   * Returns the variable type for the given variable number.
   * 
   * @param variableNumber the variable number
   * @return STACK if stack variable, LOCAL if local variable, GLOBAL if global
   */
  public static Cpu.VariableType getVariableType(int variableNumber) {
    
    if (variableNumber == 0) return Cpu.VariableType.STACK;
    else if (variableNumber < 0x10) return Cpu.VariableType.LOCAL;
    else return Cpu.VariableType.GLOBAL;
  }


  /**
   * {@inheritDoc}
   */
  public void pushRoutineContext(RoutineContext routineContext) {

    routineContext.setInvocationStackPointer(getStackPointer());
    routineContextStack.add(routineContext);
  }
  
  /**
   * {@inheritDoc}
   */
  public void popRoutineContext(short returnValue) {
    
    if (routineContextStack.size() > 0) {

      RoutineContext popped =
        routineContextStack.remove(routineContextStack.size() - 1);
      popped.setReturnValue(returnValue);
    
      // Restore stack pointer and pc
      setStackPointer(popped.getInvocationStackPointer());
      setProgramCounter(popped.getReturnAddress());
      int returnVariable = popped.getReturnVariable();
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
    
    if (routineContextStack.size() == 0) return null;
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
  public void setRoutineContexts(List<RoutineContext> contexts) {

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
  
  public RoutineContext call(int packedRoutineAddress, int returnAddress,
      short[] args, short returnVariable) {
    
    int routineAddress =
      translatePackedAddress(packedRoutineAddress, true);
    int numArgs = args != null ? args.length : 0;
    
    RoutineContext routineContext = decodeRoutine(routineAddress);
    
    // Sets the number of arguments
    routineContext.setNumArguments(numArgs);
    
    // Save return parameters
    routineContext.setReturnAddress(returnAddress);
    
    // Only if this instruction stores a result
    if (returnVariable != RoutineContext.DISCARD_RESULT) {
      
      routineContext.setReturnVariable(returnVariable);
      
    } else {
      
      routineContext.setReturnVariable(RoutineContext.DISCARD_RESULT);
    }      
    
    // Set call parameters into the local variables
    // if there are more parameters than local variables,
    // those are thrown away
    int numToCopy = Math.min(routineContext.getNumLocalVariables(),
        numArgs);
    
    for (int i = 0; i < numToCopy; i++) {
      
      routineContext.setLocalVariable(i, args[i]);
    }
    
    // save invocation stack pointer
    routineContext.setInvocationStackPointer(getStackPointer());
    
    // Pushes the routine context onto the routine stack
    pushRoutineContext(routineContext);
    
    // Jump to the address
    setProgramCounter(routineContext.getStartAddress());
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
  private RoutineContext decodeRoutine(int routineAddress) {

    MemoryAccess memaccess = gamedata.getMemoryAccess();    
    int numLocals = memaccess.readUnsignedByte(routineAddress);
    short[] locals = new short[numLocals];
    int currentAddress = routineAddress + 1;
    
    if (gamedata.getStoryFileHeader().getVersion() <= 4) {
      
      // Only story files <= 4 actually store default values here,
      // after V5 they are assumed as being 0 (standard document 1.0, S.5.2.1) 
      for (int i = 0; i < numLocals; i++) {
      
        locals[i] = memaccess.readShort(currentAddress);
        currentAddress += 2;
      }
    }
    //System.out.printf("setting routine start to: %x\n", currentAddress);
    
    RoutineContext info = new RoutineContext(currentAddress, numLocals);
    
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
  private int getLocalVariableNumber(int variableNumber) {
    
    return variableNumber - 1;
  }
  
  /**
   * Returns the global variable for the specified variable number.
   * 
   * @param variableNumber a variable number (0x10-0xff)
   * @return the global variable number
   */
  private int getGlobalVariableNumber(int variableNumber) {
    
    return variableNumber - 0x10;
  }
  
  /**
   * This function throws an exception if a non-existing local variable
   * is accessed on the current routine context or no current routine context
   * is set.
   * 
   * @param localVariableNumber the local variable number
   */
  private void checkLocalVariableAccess(int localVariableNumber) {
    
    if (routineContextStack.size() == 0) {
      
      throw new IllegalStateException("no routine context set");
    }
    
    if (localVariableNumber >= getCurrentRoutineContext().getNumLocalVariables()) {
      
      throw new IllegalStateException("access to non-existent local variable: "
                                      + localVariableNumber);
    }
  }
}
