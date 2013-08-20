
/**
 * Title:        StanfordMaxEnt<p>
 * Description:  A Maximum Entropy Toolkit<p>
 * Copyright:    Copyright (c) Kristina Toutanova<p>
 * Company:      Stanford University<p>
 */

package edu.stanford.nlp.tagger.maxent;

import edu.stanford.nlp.io.OutDataStreamFile;
import edu.stanford.nlp.stats.IntCounter;

import java.util.HashMap;
import java.io.DataInputStream;


/**
 * This class was created to store the possible tags of a word along with how many times
 * the word appeared with each tag.
 *
 * @author Kristina Toutanova
 * @version 1.0
 */
class TagCount {

  private HashMap<String, Integer> map = new HashMap<String, Integer>();
  private int ambClassId = -1; /* This is a numeric ID shared by all words that have the same set of possible tags. */

  private String[] getTagsCache; // = null;
  int sumCache;

  TagCount() { }

  TagCount(IntCounter<String> tagCounts) {
    for (String tag : tagCounts.keySet()) {
      map.put(tag, tagCounts.getIntCount(tag));
    }

    getTagsCache = map.keySet().toArray(new String[map.keySet().size()]);
    sumCache = calculateSumCache();
  }

  private static final String NULL_SYMBOL = "<<NULL>>";

  /**
   * Saves the object to the file.
   *
   * @param rf is a file handle
   *           Supposedly other objects will be written after this one in the file. The method does not close the file. The TagCount is saved at the current position.
   */
  protected void save(OutDataStreamFile rf) {
    try {
      rf.writeInt(map.size());
      for (String tag : map.keySet()) {
        if (tag == null) {
          rf.writeUTF(NULL_SYMBOL);
        } else {
          rf.writeUTF(tag);
        }
        rf.writeInt(map.get(tag));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void setAmbClassId(int ambClassId) {
    this.ambClassId = ambClassId;
  }

  public int getAmbClassId() {
    return ambClassId;
  }

  // The object's fields are read form the file. They are read from
  // the current position and the file is not closed afterwards.
  protected void read(DataInputStream rf) {
    try {

      int numTags = rf.readInt();
      map = new HashMap<String, Integer>(numTags);

      for (int i = 0; i < numTags; i++) {
	String tag = rf.readUTF();
        int count = rf.readInt();

	if (tag.equals(NULL_SYMBOL)) tag = null;
	map.put(tag, count);
      }

      getTagsCache = map.keySet().toArray(new String[map.keySet().size()]);
      sumCache = calculateSumCache();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @return the number of total occurrences of the word .
   */
  protected int sum() {
    return sumCache;
  }

  // Returns the number of occurrence of a particular tag.
  protected int get(String tag) {
    Integer count = map.get(tag);
    if (count == null) {
      return 0;
    }
    return count;
  }

  private int calculateSumCache() {
    int s = 0;
    for (Integer i : map.values()) {
      s += i;
    }
    return s;
  }

  /**
   * @return an array of the tags the word has had.
   */
  public String[] getTags() {
    return getTagsCache; //map.keySet().toArray(new String[0]);
  }


  protected int numTags() { return map.size(); }


  /**
   * @return the most frequent tag.
   */
  public String getFirstTag() {
    String maxTag = null;
    int max = 0;
    for (String tag : map.keySet()) {
      int count = map.get(tag);
      if (count > max) {
        maxTag = tag;
        max = count;
      }
    }
    return maxTag;
  }

  @Override
  public String toString() {
    return map.toString();
  }

}

