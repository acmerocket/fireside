/*
 * $Id$
 * 
 * Created on 2005/10/03
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

import org.zmpp.encoding.ZsciiString;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.media.SoundSystem;
import org.zmpp.media.SoundSystemImpl;
import org.zmpp.vm.StoryFileHeader.Attribute;
import org.zmpp.vmutil.PredictableRandomGenerator;
import org.zmpp.vmutil.RandomGenerator;
import org.zmpp.vmutil.UnpredictableRandomGenerator;

/**
 * This class implements the state and some services of a Z-machine, version 3.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MachineImpl implements Machine {

  /**
   * The configuration object.
   */
  private GameData gamedata;
  
  /**
   * The random generator.
   */
  private RandomGenerator random;
  
  /**
   * The status line.
   */
  private StatusLine statusLine;
  
  /**
   * The screen model.
   */
  private ScreenModel screenModel;
  
  /**
   * The save game data store.
   */
  private SaveGameDataStore datastore;
  
  /**
   * The undo game state.
   */
  private PortableGameState undoGameState;
  
  /**
   * The input functions object.
   */
  private InputFunctionsImpl inputFunctions;
  
  /**
   * The sound system.
   */
  private SoundSystem soundSystem;
  
  /**
   * The CPU object.
   */
  private Cpu cpu;
  
  /**
   * The output streams.
   */
  private Output output;
  
  /**
   * The input streams.
   */
  private Input input;
  
  /**
   * Constructor.
   */
  public MachineImpl() {

    this.inputFunctions = new InputFunctionsImpl(this);
  }
  
  /**
   * {@inheritDoc}
   */
  public GameData getGameData() {
    
    return gamedata;
  }
  
  /**
   * {@inheritDoc}
   */
  public Cpu getCpu() {
    
    return cpu;
  }
  
  /**
   * {@inheritDoc}
   */
  public Output getOutput() {
    
    return output;
  }
  
  /**
   * {@inheritDoc}
   */
  public Input getInput() {
    
    return input;
  }
  
  /**
   * {@inheritDoc}
   */
  public void initialize(GameData gamedata, InstructionDecoder decoder) {
  
    this.gamedata = gamedata;
    this.random = new UnpredictableRandomGenerator();
    cpu = new CpuImpl(this, decoder);
    output = new OutputImpl(gamedata, cpu);
    input = new InputImpl(this);
    
    // initialize the media access
    if (gamedata.getResources() != null) {
    
      if (gamedata.getResources().getSounds() != null) {
        this.soundSystem =
          new SoundSystemImpl(gamedata.getResources().getSounds());
      }
    }    
    resetState();
  }

  /**
   * {@inheritDoc}
   */
  public short random(short range) {
    
    if (range < 0) {
      
      random = new PredictableRandomGenerator(-range);
      return 0;
      
    } else if (range == 0) {
      
      random = new UnpredictableRandomGenerator();
      return 0;
    }
    return (short) ((random.next() % range) + 1);
  }

  // ************************************************************************
  // ****** Control functions
  // ************************************************

  /**
   * {@inheritDoc}
   */
  public void warn(String msg) {
    
    System.err.println("WARNING: " + msg);
  }
  
  /**
   * {@inheritDoc} 
   */
  public void restart() {

    restart(true);
  }
  
  /**
   * {@inheritDoc} 
   */
  public void quit() {
    
    cpu.setRunning(false);
    
    // On quit, close the streams
    output.print(new ZsciiString("*Game ended*"));
    closeStreams();
    screenModel.redraw();
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    
    cpu.setRunning(true);
  }
  
  // ************************************************************************
  // ****** Machine services
  // ************************************************
  
  /**
   * {@inheritDoc}
   */
  public InputFunctions getInputFunctions() {
    
    return inputFunctions;
  }
  
  /**
   * {@inheritDoc}
   */
  public Tokenizer getTokenizer() {
    
    return inputFunctions;
  }
  
  /**
   * {@inheritDoc}
   */
  public SoundSystem getSoundSystem() {
    
    return soundSystem;
  }

  /**
   * {@inheritDoc}
   */
  public void setSaveGameDataStore(SaveGameDataStore datastore) {
    
    this.datastore = datastore;
  }

  /**
   * {@inheritDoc}
   */
  public void updateStatusLine() {
  
    if (gamedata.getStoryFileHeader().getVersion() <= 3 && statusLine != null) {
      
      int objNum = cpu.getVariable(0x10);    
      ZObject obj = gamedata.getObjectTree().getObject(objNum);
      String objectName = gamedata.getZCharDecoder().decode2Zscii(
          gamedata.getMemoryAccess(),
          obj.getPropertiesDescriptionAddress(), 0).toString();      
      int global2 = cpu.getVariable(0x11);
      int global3 = cpu.getVariable(0x12);
      if (gamedata.getStoryFileHeader().isEnabled(Attribute.SCORE_GAME)) {
        
        statusLine.updateStatusScore(objectName, global2, global3);
      } else {
        
        statusLine.updateStatusTime(objectName, global2, global3);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setStatusLine(StatusLine statusLine) {
    
    this.statusLine = statusLine;
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreen(ScreenModel screen) {
   
    this.screenModel = screen;
  }
  
  /**
   * {@inheritDoc}
   */
  public ScreenModel getScreen() {
    
    return screenModel;
  }  
  
  /**
   * {@inheritDoc} 
   */
  public boolean save(int savepc) {
    
    if (datastore != null) {
      
      PortableGameState gamestate = new PortableGameState();
      gamestate.captureMachineState(this, savepc);
      WritableFormChunk formChunk = gamestate.exportToFormChunk();
      return datastore.saveFormChunk(formChunk);
    }
    
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean save_undo(int savepc) {
    
    undoGameState = new PortableGameState();
    undoGameState.captureMachineState(this, savepc);
    return true;
  }

  /**
   * {@inheritDoc} 
   */
  public PortableGameState restore() {
    
    if (datastore != null) {
      
      PortableGameState gamestate = new PortableGameState();
      FormChunk formchunk = datastore.retrieveFormChunk();
      gamestate.readSaveGame(formchunk);
      
      // verification has to be here
      if (verifySaveGame(gamestate)) {
        
        // do not reset screen model, since e.g. AMFV simply picks up the
        // current window state
        restart(false);
        gamestate.transferStateToMachine(this);
        //System.out.printf("restore(), pc is: %4x running: %b\n", getProgramCounter(), isRunning());
        return gamestate;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public PortableGameState restore_undo() {
    
    // do not reset screen model, since e.g. AMFV simply picks up the
    // current window state
    if (undoGameState != null) {
      restart(false);
      undoGameState.transferStateToMachine(this);
      System.out.printf("restore(), pc is: %4x\n", cpu.getProgramCounter());
      return undoGameState;
    }
    return null;
  }
  
  // ***********************************************************************
  // ***** Private methods
  // **************************************
  
  private boolean verifySaveGame(PortableGameState gamestate) {
    
    // Verify the game according to the standard
    StoryFileHeader fileHeader = gamedata.getStoryFileHeader();
    int checksum = fileHeader.getChecksum();
    if (checksum == 0) checksum = gamedata.getCalculatedChecksum();
    return gamestate.getRelease() == fileHeader.getRelease()
      && gamestate.getChecksum() == checksum
      && gamestate.getSerialNumber().equals(fileHeader.getSerialNumber());
  }  

  /**
   * Close the streams.
   */
  private void closeStreams() {

    input.close();
    output.close();
  }
  
  /**
   * Resets all state to initial values, using the configuration object.
   */
  private void resetState() {
    
    cpu.reset();
    //gamedata.getStoryFileHeader().setStandardRevision(1, 0);
    
    if (gamedata.getStoryFileHeader().getVersion() >= 4) {
            
      gamedata.getStoryFileHeader().setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
      gamedata.getStoryFileHeader().setInterpreterNumber(6); // IBM PC
      gamedata.getStoryFileHeader().setInterpreterVersion(1);
    }
  }
  
  private void restart(boolean resetScreenModel) {
    
    // Transcripting and fixed font bits survive the restart
    StoryFileHeader fileHeader = gamedata.getStoryFileHeader();
    boolean fixedFontForced = fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT);
    boolean transcripting = fileHeader.isEnabled(Attribute.TRANSCRIPTING);
    gamedata.reset();
    resetState();
    if (resetScreenModel) screenModel.reset();    
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, transcripting);
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, fixedFontForced);
  }  
}
