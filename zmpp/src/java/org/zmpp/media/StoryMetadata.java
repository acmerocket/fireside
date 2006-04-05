/*
 * $Id$
 * 
 * Created on 2006/03/10
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
package org.zmpp.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class holds information about a story.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StoryMetadata {

  public static class Auxiliary {
  
    private String leafname;
    private String description;
    public String getLeafName() { return leafname; }
    public String getDescription() { return description; }
    public void setLeafName(String name) { leafname = name; }
    public void setDescription(String text) { description = text; }
    public String toString() { return leafname; }
  }
  
  private String title;
  private String headline;
  private String author;
  private String genre;
  private String description;
  private String year;
  private int coverpicture;
  private String group;
  private List<Auxiliary> auxiliaries = new ArrayList<Auxiliary>();
  
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getHeadline() { return headline; }
  public void setHeadline(String headline) { this.headline = headline; }
  public String getAuthor() { return author; }
  public void setAuthor(String author) { this.author = author; }
  public String getGenre() { return genre; }
  public void setGenre(String genre) { this.genre = genre; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getYear() { return year; }
  public void setYear(String year) { this.year = year; }
  public int getCoverPicture() { return coverpicture; }
  public void setCoverPicture(int picnum) { this.coverpicture = picnum; }
  public void addAuxiliary(Auxiliary auxiliary) { auxiliaries.add(auxiliary); }
  public String getGroup() { return group; }
  public void setGroup(String group) { this.group = group; }
  public List<Auxiliary> getAuxiliaries() {
    
    return Collections.unmodifiableList(auxiliaries);
  }
  
  public String toString() {
    
    StringBuilder builder = new StringBuilder();
    builder.append("Title: '" + title + "'\n");
    builder.append("Headline: '" + headline + "'\n");
    builder.append("Author: '" + author + "'\n");
    builder.append("Genre: '" + genre + "'\n");
    builder.append("Description: '" + description + "'\n");
    builder.append("Year: '" + year + "'\n");
    builder.append("Cover picture: " + coverpicture + "\n");
    builder.append("# aux: '" + auxiliaries.size() + "'\n");
    builder.append("-------------------------------\n");
    for (Auxiliary aux : auxiliaries) {
      builder.append(aux.toString());
    }
    builder.append("Group: '" + group + "'\n");
    return builder.toString();
  }
}
