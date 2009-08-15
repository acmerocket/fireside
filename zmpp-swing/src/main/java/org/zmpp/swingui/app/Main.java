/*
 * Created on 2008/04/23
 * Copyright 2005-2009 by Wei-ju Wu
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
package org.zmpp.swingui.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.zmpp.blorb.MemoryInputStream;
import org.zmpp.blorb.NativeImage;
import org.zmpp.blorb.NativeImageFactory;
import org.zmpp.blorb.SoundEffectFactory;
import org.zmpp.iff.Chunk;
import org.zmpp.media.SoundEffect;

/**
 * New application class using the Swing 2 model.
 * @author Wei-ju Wu
 * @version 1.5
 */
public final class Main {

  /**
   * Private constructor.
   */
  private Main() { }
  /**
   * Localized message bundle.
   */
  private static final PropertyResourceBundle MESSAGE_BUNDLE =
    (PropertyResourceBundle) PropertyResourceBundle.getBundle("zmpp_messages");

  /**
   * Debug flag.
   */
  public static final boolean DEBUG = true;

  /**
   * Image class.
   */
  static class AwtImage implements NativeImage {

    private BufferedImage image;
    public AwtImage(BufferedImage image) { this.image = image; }
    public BufferedImage getImage() { return image; }
    public int getWidth() { return image.getWidth(); }
    public int getHeight() { return image.getHeight(); }
  }

  /**
   * Image factory.
   */
  static class AwtImageFactory implements NativeImageFactory {
    public NativeImage createImage(InputStream inputStream) throws IOException {
      return new AwtImage(ImageIO.read(inputStream));
    }
  }

  /**
   * Sound effect factory.
   */
  static class DefaultSoundEffectFactory implements SoundEffectFactory {
    public SoundEffect createSoundEffect(Chunk aiffChunk)
        throws IOException {
      final InputStream aiffStream =
        new  MemoryInputStream(aiffChunk.getMemory(), 0,
          aiffChunk.getSize() + Chunk.CHUNK_HEADER_LENGTH);
      try {
        final AudioFileFormat aiffFormat =
          AudioSystem.getAudioFileFormat(aiffStream);
        final AudioInputStream stream = new AudioInputStream(aiffStream,
            aiffFormat.getFormat(), (long) aiffChunk.getSize());
        final Clip clip = AudioSystem.getClip();
        clip.open(stream);
        return new DefaultSoundEffect(clip);
      } catch (UnsupportedAudioFileException ex) {
        throw new IOException("could not read sound effect");
      } catch (LineUnavailableException ex) {
        throw new IOException("could not read sound effect");
      }
    }
  }

  /**
   * Application name.
   */
  public static final String APP_NAME = getMessage("app.name");

  /**
   * Global function to return the message string.
   * @param property the property name
   * @return the message
   */
  public static String getMessage(String property) {
    return MESSAGE_BUNDLE.getString(property);
  }

  /**
   * main() method.
   * @param args command line arguments
   */
  public static void main(String[] args) {
    setMacOsXProperties();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      LogManager.getLogManager().readConfiguration();
      Logger.getLogger("org.zmpp").setLevel(Level.SEVERE);
      Logger.getLogger("org.zmpp.screen").setLevel(Level.SEVERE);
      Logger.getLogger("org.zmpp.ui").setLevel(Level.SEVERE);
      Logger.getLogger("org.zmpp.control").setLevel(Level.SEVERE);
      //ExecutionControl.DEBUG = true;
      //ExecutionControl.DEBUG_INTERRUPT = true;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    if (args.length >= 1) {
      runWithParameters(args);
    } else {
      ZmppFrame.openStoryFile();
    }
  }

  /**
   * Determines whether this is a Mac OS X application.
   * @return true if Mac OS X application, false otherwise
   */
  public static boolean isMacOsX() {
    return System.getProperty("mrj.version") != null;
  }

  /**
   * Sets properties to start as Mac OS X application.
   */
  private static void setMacOsXProperties() {
    if (isMacOsX()) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode",
          "false");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          "ZMPP");
    }
  }

  /**
   * Runs ZMPP with command line arguments.
   * @param args the command line arguments
   */
  private static void runWithParameters(String[] args)
  {
    if (isFile(args[0])) {
      ZmppFrame.openStoryFile(new File(args[0]));
    } else if (isUrl(args[0])) {
      try {
        ZmppFrame.openStoryUrl(new URL(args[0]));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else {
      JOptionPane.showMessageDialog(null,
        MessageFormat.format(getMessage("error.open.msg"), ""),
        getMessage("error.open.title"), JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Determines whether the specified string is a file.
   * @param str input string
   * @return true if input string is a file, false otherwise
   */
  private static boolean isFile(String str) {
    File file = new File(str);
    return file.exists() && file.isFile();
  }

  /**
   * Determines whether the specified string is a valid URL.
   * @param str input string
   * @return true if url, false otherwise
   */
  private static boolean isUrl(String str) {
    try {
      new URL(str);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
