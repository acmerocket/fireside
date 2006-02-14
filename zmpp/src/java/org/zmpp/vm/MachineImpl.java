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

import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.iff.FormChunk;
import org.zmpp.iff.WritableFormChunk;
import org.zmpp.instructions.Interruptable;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;
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
public class MachineImpl implements Machine, MachineServices, Interruptable {

  /**
   * The configuration object.
   */
  private GameData gamedata;
  
  /**
   * This is the array of output streams.
   */
  private OutputStream[] outputStream;
  
  /**
   * This is the array of input streams.
   */
  private InputStream[] inputStream;
  
  /**
   * The selected input stream.
   */
  private int selectedInputStreamIndex;
  
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
   * Returns the checksum verification status of the story file.
   */
  private boolean hasValidChecksum;
  
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
   * Constructor.
   */
  public MachineImpl() {

    this.inputFunctions = new InputFunctionsImpl(this);
  }
  
  public MachineServices getServices() {
  
    return this;
  }
  
  public GameData getGameData() {
    
    return gamedata;
  }
  
  public Cpu getCpu() {
    
    return cpu;
  }
  
  /**
   * {@inheritDoc}
   */
  public void initialize(GameData gamedata, InstructionDecoder decoder) {
  
    this.gamedata = gamedata;
    this.outputStream = new OutputStream[3];
    this.inputStream = new InputStream[2];    
    this.selectedInputStreamIndex = 0;
    this.random = new UnpredictableRandomGenerator();
    cpu = new CpuImpl(this, decoder);
    
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
   * Resets all state to initial values, using the configuration object.
   */
  private void resetState() {
    
    cpu.reset();
    int checksum = calculateChecksum(gamedata.getStoryFileHeader());
    hasValidChecksum = gamedata.getStoryFileHeader().getChecksum() == checksum;
    //gamedata.getStoryFileHeader().setStandardRevision(1, 0);
    
    if (gamedata.getStoryFileHeader().getVersion() >= 4) {
            
      gamedata.getStoryFileHeader().setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
      gamedata.getStoryFileHeader().setInterpreterNumber(6); // IBM PC
      gamedata.getStoryFileHeader().setInterpreterVersion(1);
    }
  }
  
  /**
   * Calculates the checksum of the file.
   * 
   * @param fileheader the file header
   * @return the check sum
   */
  private int calculateChecksum(StoryFileHeader fileheader) {
    
    int filelen = fileheader.getFileLength();
    int sum = 0;
    
    for (int i = 0x40; i < filelen; i++) {
    
      sum += gamedata.getMemoryAccess().readUnsignedByte(i);
    }
    return (sum & 0xffff);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasValidChecksum() {
    
    return this.hasValidChecksum;
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

  // **********************************************************************
  // ****** Streams
  // ****************************
  
  /**
   * {@inheritDoc}
   */
  public void setOutputStream(int streamnumber, OutputStream stream) {
    
    outputStream[streamnumber - 1] = stream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void printZString(int address) {
    
    print(gamedata.getZCharDecoder().decode2Zscii(gamedata.getMemoryAccess(),
        address, 0));
  }
  
  /**
   * {@inheritDoc}
   */
  public void print(ZsciiString str) {

    //System.out.println("print: '" + str + "'");
    printZsciiChars(str, false);
  }
  
  /**
   * {@inheritDoc}
   */
  public void newline() {
    
    printZsciiChar(ZsciiEncoding.NEWLINE, false);
  }
  
  private short[] zchars = new short[1];
  
  /**
   * {@inheritDoc}
   */
  public void printZsciiChar(short zchar, boolean isInput) {
    
    //System.out.println("printZsciiChar: '" + (char) zchar + "'");
    zchars[0] = zchar;
    printZsciiChars(new ZsciiString(zchars), isInput);
  }
  
  /**
   * {@inheritDoc}
   */
  public void deletePreviousZsciiChar(short zchar) {
    
    if (!outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
          
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          outputStream[i].deletePrevious(zchar);
        }
      }
    }
  }

  /**
   * Prints the specified array of ZSCII characters. This is the only function
   * that communicates with the output streams directly.
   * 
   * @param zsciiString the array of ZSCII characters.
   * @param isInput true if in input mode, false otherwise
   */
  private void printZsciiChars(ZsciiString zsciiString, boolean isInput) {
    
    checkTranscriptFlag();
    
    if (outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      for (int i = 0, n = zsciiString.length(); i < n; i++) {
        
        outputStream[OUTPUTSTREAM_MEMORY - 1].print(zsciiString.charAt(i), isInput);
      }
      
    } else {
    
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          for (int j = 0, n = zsciiString.length(); j < n; j++) {
          
            outputStream[i].print(zsciiString.charAt(j), isInput);
          }
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void printNumber(short number) {
    
    print(new ZsciiString(String.valueOf(number)));
  }
  
  public void flushOutput() {
    
    // At the moment flushing only makes sense for screen
    if (!outputStream[OUTPUTSTREAM_MEMORY - 1].isSelected()) {
      
      
      for (int i = 0; i < outputStream.length; i++) {
      
        if (outputStream[i] != null && outputStream[i].isSelected()) {
      
          outputStream[i].flush();
        }
      }
    }
  }

  /**
   * Checks the fileheader if the transcript flag was set by the game
   * bypassing output_stream, e.g. with a storeb to the fileheader flags
   * address. Enable the transcript depending on the status of that flag.
   */
  private void checkTranscriptFlag() {
    
    if (outputStream[OUTPUTSTREAM_TRANSCRIPT - 1] != null) {
        
      outputStream[OUTPUTSTREAM_TRANSCRIPT - 1].select(
          gamedata.getStoryFileHeader().isEnabled(Attribute.TRANSCRIPTING));
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectOutputStream(int streamnumber, boolean flag) {
    
    outputStream[streamnumber - 1].select(flag);
    
    // Sets the tranxdQscript flag if the transcipt is specified
    if (streamnumber == OUTPUTSTREAM_TRANSCRIPT) {
      
      //System.out.println("ENABLE_TRANSCRIPT_STREAM: " + flag);
      gamedata.getStoryFileHeader().setEnabled(Attribute.TRANSCRIPTING, flag);
      
    } else if (streamnumber == OUTPUTSTREAM_MEMORY && flag) {
      
      cpu.halt("invalid selection of memory stream");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectOutputStream3(int tableAddress) {

    ((MemoryOutputStream) outputStream[OUTPUTSTREAM_MEMORY - 1]).select(
        tableAddress);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setInputStream(int streamnumber, InputStream stream) {
    
    inputStream[streamnumber] = stream;
  }
  
  /**
   * {@inheritDoc}
   */
  public void selectInputStream(int streamnumber) {
    
    selectedInputStreamIndex = streamnumber;    
    screenModel.setPaging(streamnumber != Machine.INPUTSTREAM_FILE);
  }
  
  /**
   * {@inheritDoc}
   */
  public InputStream getSelectedInputStream() {
    
    return inputStream[selectedInputStreamIndex];
  }
    
  // ************************************************************************
  // ****** Control functions
  // ************************************************

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
  public void warn(String msg) {
    
    System.err.println("WARNING: " + msg);
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
  
  private boolean verifySaveGame(PortableGameState gamestate) {
    
    // Verify the game according to the standard
    StoryFileHeader fileHeader = gamedata.getStoryFileHeader();
    int checksum = fileHeader.getChecksum();
    if (checksum == 0) checksum = calculateChecksum(fileHeader);
    return gamestate.getRelease() == fileHeader.getRelease()
      && gamestate.getChecksum() == checksum
      && gamestate.getSerialNumber().equals(fileHeader.getSerialNumber());
  }
  
  /**
   * {@inheritDoc} 
   */
  public void restart() {

    restart(true);
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
  
  /**
   * {@inheritDoc} 
   */
  public void quit() {
    
    cpu.setRunning(false);
    
    // On quit, close the streams
    print(new ZsciiString("*Game ended*"));
    closeStreams();
    screenModel.redraw();
  }
  
  /**
   * Close the streams.
   */
  private void closeStreams() {
    
    if (inputStream != null) {

      for (int i = 0; i < inputStream.length; i++) {
        
        if (inputStream[i] != null) {
          
          inputStream[i].close();
        }
      }
    }
    
    if (outputStream != null) {

      for (int i = 0; i < outputStream.length; i++) {
        
        if (outputStream[i] != null) {
          
          outputStream[i].flush();
          outputStream[i].close();
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    
    cpu.setRunning(true);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setInterruptRoutine(int routine) {
    
    // TODO
    System.out.printf("setInterruptRoutine(): %04x\n", routine);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setSaveGameDataStore(SaveGameDataStore datastore) {
    
    this.datastore = datastore;
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
}
