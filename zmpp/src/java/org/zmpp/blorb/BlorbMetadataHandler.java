/*
 * $Id$
 * 
 * Created on 2006/03/04
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
package org.zmpp.blorb;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.zmpp.base.MemoryReadAccess;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.media.InformMetadata;
import org.zmpp.media.StoryMetadata;
import org.zmpp.media.StoryMetadata.Auxiliary;

/**
 * This class parses the metadata chunk in the Blorb file and converts
 * it into an Inform metadata chunk.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class BlorbMetadataHandler extends DefaultHandler {

  private StoryMetadata story;
  private StringBuilder buffer;
  private Auxiliary auxiliary;
    
  public BlorbMetadataHandler(FormChunk formchunk) {
 
    extractMetadata(formchunk);    
  }
  
  public InformMetadata getMetadata() {
    
    return (story != null) ? new InformMetadata(story) : null;
  }
  
  private void extractMetadata(FormChunk formchunk) {

    Chunk chunk = formchunk.getSubChunk("IFmd".getBytes());
    if (chunk != null) {

      MemoryReadAccess memaccess = chunk.getMemoryAccess();
      MemoryAccessInputStream meminput = new MemoryAccessInputStream(memaccess,
          Chunk.CHUNK_HEADER_LENGTH,
          chunk.getSize() + Chunk.CHUNK_HEADER_LENGTH);
      
      try {
        
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(meminput, this);
        
      } catch (Exception ex) {
        
        ex.printStackTrace();
      }
    }
  }
    
  // **********************************************************************
  // **** Parsing meta data
  // *********************************
  
  public void startElement(String uri, String localName, String qname,
                          Attributes attributes) {
    
    if ("story".equals(qname)) {

      story = new StoryMetadata();
    }
    if ("title".equals(qname)) {
      
      buffer = new StringBuilder();
    }
    if ("headline".equals(qname)) {
      
      buffer = new StringBuilder();
    }
    if ("author".equals(qname)) {
      
      buffer = new StringBuilder();
    }
    if ("genre".equals(qname)) {
      
      buffer = new StringBuilder();
    }
    if ("description".equals(qname)) {
      
      buffer = new StringBuilder();
    }
    if ("year".equals(qname)) {
      
      buffer = new StringBuilder();
    }
    if (qname.equals("auxiliary")) {
      
      auxiliary = new Auxiliary();
    }
    if (qname.equals("group")) {
      
      buffer = new StringBuilder();
    }
    if (qname.equals("leafname")) {
      
      buffer = new StringBuilder();
    }
    if (qname.equals("coverpicture")) {
      
      buffer = new StringBuilder();
    }
  }

  public void endElement(String uri, String localName, String qname) {

    if ("title".equals(qname)) {
      
      story.setTitle(buffer.toString());
    }
    if ("headline".equals(qname)) {
      
      story.setHeadline(buffer.toString());
    }
    if ("author".equals(qname)) {
      
      story.setAuthor(buffer.toString());
    }
    if ("genre".equals(qname)) {
      
      story.setGenre(buffer.toString());
    }
    if ("description".equals(qname)) {
      
      if (auxiliary != null) {
        
        auxiliary.setDescription(buffer.toString());
        
      } else {
        
        story.setDescription(buffer.toString());
      }
    }
    if ("year".equals(qname)) {
      
      story.setYear(buffer.toString());
    }
    if ("group".equals(qname)) {
      
      story.setGroup(buffer.toString());
    }
    if ("coverpicture".equals(qname)) {
      
      String val = buffer.toString().trim();
      try {
        
        story.setCoverPicture(Integer.parseInt(val));
        
      } catch (NumberFormatException ex) {
        
        System.err.println("NumberFormatException in cover picture: " + val);
      }
    }
    if ("leafname".equals(qname) && auxiliary != null) {
      
      auxiliary.setLeafName(buffer.toString());
    }
    if ("auxiliary".equals(qname)) {
      
      if (auxiliary != null) story.addAuxiliary(auxiliary);
      auxiliary = null;
    }
    buffer = null;
  }
  
  public void characters(char[] ch, int start, int length) {
    
    if (buffer != null) {
      
      StringBuilder partbuilder = new StringBuilder();
      for (int i = start; i < start + length; i++) {

        partbuilder.append(ch[i]);
      }
      buffer.append(partbuilder.toString().trim());
    }
  }
}
