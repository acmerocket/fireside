/*
 * Created on 2006/03/10
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.zmpp.media;


/**
 * This class holds information about a story.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class StoryMetadata {

  private static final char NEWLINE = '\n';

  private String title;
  private String headline;
  private String author;
  private String genre;
  private String description;
  private String year;
  private int coverpicture;
  private String group;

  public String getTitle() { return title; }
  public void setTitle(final String title) { this.title = title; }
  public String getHeadline() { return headline; }
  public void setHeadline(final String headline) { this.headline = headline; }
  public String getAuthor() { return author; }
  public void setAuthor(final String author) { this.author = author; }
  public String getGenre() { return genre; }
  public void setGenre(final String genre) { this.genre = genre; }
  public String getDescription() { return description; }
  public void setDescription(final String description) {
    this.description = description;
  }
  public String getYear() { return year; }
  public void setYear(final String year) { this.year = year; }
  public int getCoverPicture() { return coverpicture; }
  public void setCoverPicture(final int picnum) { this.coverpicture = picnum; }
  public String getGroup() { return group; }
  public void setGroup(final String group) { this.group = group; }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Title: '" + title + NEWLINE);
    builder.append("Headline: '" + headline + NEWLINE);
    builder.append("Author: '" + author + NEWLINE);
    builder.append("Genre: '" + genre + NEWLINE);
    builder.append("Description: '" + description + NEWLINE);
    builder.append("Year: '" + year + NEWLINE);
    builder.append("Cover picture: " + coverpicture + NEWLINE);
    builder.append("Group: '" + group + NEWLINE);
    return builder.toString();
  }
}
