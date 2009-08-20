/*
 * Created on 2006/03/04
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
package org.zmpp.blorb;

import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.zmpp.base.Memory;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.media.InformMetadata;
import org.zmpp.media.StoryMetadata;

/**
 * This class parses the metadata chunk in the Blorb file and converts
 * it into a Treaty of Babel metadata object.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class BlorbMetadataHandler extends DefaultHandler {

  private static final Logger LOG = Logger.getLogger("org.zmpp");
  private StoryMetadata story;
  private StringBuilder buffer;
  private boolean processAux;

  /**
   * Constructor.
   * @param formchunk a FORM chunk
   */
  public BlorbMetadataHandler(FormChunk formchunk) {
    extractMetadata(formchunk);
  }

  /**
   * Returns the meta data object.
   * @return meta data object
   */
  public InformMetadata getMetadata() {
    return (story == null) ? null : new InformMetadata(story);
  }

  /**
   * Extracts inform meta data from the specified FORM chunk.
   * @param formchunk the FORM chunk
   */
  private void extractMetadata(final FormChunk formchunk) {
    final Chunk chunk = formchunk.getSubChunk("IFmd");
    if (chunk != null) {
      final Memory chunkmem = chunk.getMemory();
      final MemoryInputStream meminput =
        new MemoryInputStream(chunkmem, Chunk.CHUNK_HEADER_LENGTH,
          chunk.getSize() + Chunk.CHUNK_HEADER_LENGTH);

      try {
        final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(meminput, this);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  // **********************************************************************
  // **** Parsing meta data
  // *********************************

  /** {@inheritDoc} */
  @Override
  public void startElement(final String uri, final String localName,
      final String qname, final Attributes attributes) {
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
    if (isPublishYear(qname)) {
      buffer = new StringBuilder();
    }
    if ("auxiliary".equals(qname)) {
      processAux = true;
    }
    if ("coverpicture".equals(qname)) {
      buffer = new StringBuilder();
    }
    if ("group".equals(qname)) {
      buffer = new StringBuilder();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void endElement(final String uri, final String localName,
      final String qname) {
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
    if ("description".equals(qname) && !processAux) {
      story.setDescription(buffer.toString());
    }
    if (isPublishYear(qname)) {
      story.setYear(buffer.toString());
    }
    if ("group".equals(qname)) {
      story.setGroup(buffer.toString());
    }
    if ("coverpicture".equals(qname)) {
      final String val = buffer.toString().trim();
      try {
        story.setCoverPicture(Integer.parseInt(val));
      } catch (NumberFormatException ex) {
        LOG.throwing("BlorbMetadataHandler", "endElement", ex);
      }
    }
    if ("auxiliary".equals(qname)) {
      processAux = false;
    }
    if ("br".equals(qname) && buffer != null) {
      buffer.append("\n");
    }
  }

  /** {@inheritDoc} */
  @Override
  public void characters(final char[] ch, final int start, final int length) {
    if (buffer != null) {
      final StringBuilder partbuilder = new StringBuilder();
      for (int i = start; i < start + length; i++) {
        partbuilder.append(ch[i]);
      }
      buffer.append(partbuilder.toString().trim());
    }
  }

  /**
   * Unfortunately, year was renamed to firstpublished between the preview
   * metadata version of Inform 7 and the Treaty of Babel version, so
   * we handle both here.
   *
   * @param str the qname
   * @return true if matches, false, otherwise
   */
  private boolean isPublishYear(String str) {
    return "year".equals(str) || "firstpublished".equals(str);
  }
}