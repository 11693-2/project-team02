package edu.cmu.lti.oaqa.pipeline;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import json.gson.Snippet;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

/**
 * This is the evaluator which evaluates the performance of current algorithm
 * 
 * @author yifu
 *
 */
public class Evaluator {

  private static int totalDoc = 0;

  private static int totalConcept = 0;

  private static int totalTriple = 0;

  private static int posDoc = 0;

  private static int posConcept = 0;

  private static int posTriple = 0;

  // currently, we assume supposed answer number is golden answer number
  private static int supposeDoc = 0;

  private static int supposeConcept = 0;

  private static int supposeTriple = 0;

  // these variables are for snippets
  private static int intersactSnippetNum = 0;

  private static int snippetReturnNum = 0;

  private static int snippetGoldNum = 0;

  // 0: document; 1: concept; 2: triple; 3: snippet
  private static ArrayList<ArrayList<Double>> avgPrecisionArr = new ArrayList<ArrayList<Double>>(4);

  private static final int typeDocument = 0;

  private static final int typeConcept = 1;

  private static final int typeTriple = 2;

  private static final int typeSnippet = 3;

  Evaluator() {
    for (int i = 0; i < 4; i++) {
      avgPrecisionArr.add(new ArrayList<Double>());
    }
  }

  /**
   * Calculate positive number, total number, supposed number, and average precision of document
   * 
   * @param docList
   * @param docGold
   * @return
   */
  public int calDocPositive(Collection<Document> docList, List<String> docGold) {
    if (docGold.size() == 0)
      return 0;
    Set<String> goldSet = new HashSet<String>();

    for (int i = 0; i < docGold.size(); i++) {
      String str = docGold.get(i);
      goldSet.add(str);
      // System.out.println("| golden: " + str);
    }

    double avgPrecision = 0.0;
    int counter = 0;
    int positiveNum = 0;
    for (Document doc : docList) {
      // TODO: may change docId to other feature
      counter++;
      // System.out.println("| document ans: http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getDocId());
      if (goldSet.contains("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getDocId())) {
        positiveNum++;
        avgPrecision += (double) positiveNum / counter;
      }
    }
    if (counter != 0) {
      avgPrecision /= counter;
    }
    System.out.println("| AVG Precision: " + avgPrecision);
    if (avgPrecision > 0.00001) {
      avgPrecisionArr.get(typeDocument).add(avgPrecision);
    }

    totalDoc += docList.size();
    supposeDoc += docGold.size();

    // get intersection of two sets
    posDoc += positiveNum;
    return positiveNum;
  }

  /**
   * Calculate positive number, total number, supposed number, and average precision of concept
   * 
   * @param docList
   * @param conceptGold
   * @return
   */
  public int calConceptPositive(Collection<ConceptSearchResult> conceptList,
          List<String> conceptGold) {
    if (conceptGold.size() == 0)
      return 0;
    Set<String> goldSet = new HashSet<String>();

    for (int i = 0; i < conceptGold.size(); i++) {
      String str = conceptGold.get(i);
      goldSet.add(str);
      // System.out.println("| golden: " + str);
    }

    double avgPrecision = 0.0;
    int counter = 0;
    int positiveNum = 0;
    for (ConceptSearchResult concept : conceptList) {
      counter++;
      // System.out.println("| concept ans: " + concept.getUri());
      if (goldSet.contains(concept.getUri())) {
        positiveNum++;
        avgPrecision += (double) positiveNum / counter;
      }
    }
    if (counter != 0) {
      avgPrecision /= counter;
    }
    System.out.println("| AVG Precision: " + avgPrecision);
    if (avgPrecision > 0.00001) {
      avgPrecisionArr.get(typeConcept).add(avgPrecision);
    }

    totalConcept += conceptList.size();
    supposeConcept += conceptGold.size();

    // get intersection of two sets
    posConcept += positiveNum;
    return positiveNum;
  }

  /**
   * Calculate positive number, total number, supposed number, and average precision of triple
   * 
   * @param tripleList
   * @param tripleGold
   * @return
   */
  public int calTriplePositive(Collection<TripleSearchResult> tripleList,
          List<json.gson.Triple> tripleGold) {
    if (tripleGold.size() == 0)
      return 0;
    // store golden standard entry of three types
    HashMap<String, Integer> goldObjectDict = new HashMap<String, Integer>();
    ArrayList<String> goldPredicateArr = new ArrayList<String>();
    ArrayList<String> goldSubjectArr = new ArrayList<String>();

    for (int i = 0; i < tripleGold.size(); i++) {
      json.gson.Triple t = tripleGold.get(i);
      goldObjectDict.put(t.getO(), i);
      goldPredicateArr.add(t.getP());
      goldSubjectArr.add(t.getS());
      // System.out.println("| golden: " + str);
    }

    double avgPrecision = 0.0;
    int counter = 0;
    int positiveNum = 0;
    for (TripleSearchResult result : tripleList) {
      counter++;
      edu.cmu.lti.oaqa.type.kb.Triple t = result.getTriple();
      // System.out.println("| concept ans: " + concept.getUri());
      if (goldObjectDict.containsKey(t.getObject())) {
        int index = goldObjectDict.get(t.getObject());
        if (goldPredicateArr.get(index).equals(t.getPredicate())
                && goldSubjectArr.get(index).equals(t.getSubject())) {
          positiveNum++;
          avgPrecision += (double) positiveNum / counter;
        }
      }
    }
    if (counter != 0) {
      avgPrecision /= counter;
    }
    System.out.println("| AVG Precision: " + avgPrecision);
    if (avgPrecision > 0.00001) {
      avgPrecisionArr.get(typeTriple).add(avgPrecision);
    }

    totalTriple += tripleList.size();
    supposeTriple += tripleGold.size();

    // get intersection of two sets
    posTriple += positiveNum;
    return positiveNum;
  }

  public int calSnippetPositive(Collection<Passage> snippetList, List<Snippet> snippetGold) {

    // golden answer dictionary, key: start and end offset; value: frequency
    HashMap<OffsetPair<Integer, Integer>, Integer> goldDict = new HashMap<OffsetPair<Integer, Integer>, Integer>();

    for (int i = 0; i < snippetGold.size(); i++) {
      Snippet snippet = snippetGold.get(i);
      OffsetPair<Integer, Integer> thiskey = new OffsetPair<Integer, Integer>(
              snippet.getOffsetInBeginSection(), snippet.getOffsetInEndSection());
      if (goldDict.containsKey(thiskey)) {
        int freq = goldDict.get(thiskey);
        goldDict.put(thiskey, freq + 1);
      } else {
        goldDict.put(thiskey, 1);
      }
    }

    double avgPrecision = 0.0;
    int counter = 0;
    int positiveNum = 0;
    for (Passage p : snippetList) {
      counter++;
      OffsetPair<Integer, Integer> thiskey = new OffsetPair<Integer, Integer>(
              p.getOffsetInBeginSection(), p.getOffsetInEndSection());

      if (goldDict.containsKey(thiskey)) {
        positiveNum++;
        avgPrecision += (double) positiveNum / counter;
      }
    }
    if (counter != 0) {
      avgPrecision /= counter;
    }
    System.out.println("| AVG Precision: " + avgPrecision);
    if (avgPrecision > 0.00001) {
      avgPrecisionArr.get(typeSnippet).add(avgPrecision);
    }

    intersactSnippetNum += positiveNum;
    snippetReturnNum += snippetList.size();
    snippetGoldNum += snippetGold.size();

    return positiveNum;
  }

  /**
   * get precision value of certain type
   * 
   * @param typeStr
   *          : the type of data: document, concept, triple, snippet
   * @return
   */
  public double getPrecision(String typeStr) {
    switch (typeStr) {
      case "document":
        if (totalDoc == 0)
          return 0.0;
        return (double) posDoc / totalDoc;
      case "concept":
        if (totalConcept == 0)
          return 0.0;
        return (double) posConcept / totalConcept;
      case "triple":
        if (totalTriple == 0)
          return 0.0;
        return (double) posTriple / totalTriple;
      case "snippet":
        if (snippetReturnNum == 0)
          return 0.0;
        return (double) intersactSnippetNum / snippetReturnNum;
      default:
        System.err.println("Wrong type parameter for precision");
        return 0.0;
    }
  }

  /**
   * Currently just sum up all three categories of result
   * 
   * @return
   */
  public double getRecall(String typeStr) {
    switch (typeStr) {
      case "document":
        if (supposeDoc == 0)
          return 0.0;
        return (double) posDoc / supposeDoc;
      case "concept":
        if (supposeConcept == 0)
          return 0.0;
        return (double) posConcept / supposeConcept;
      case "triple":
        if (supposeTriple == 0)
          return 0.0;
        return (double) posTriple / supposeTriple;
      case "snippet":
        if (snippetGoldNum == 0)
          return 0.0;
        return (double) intersactSnippetNum / snippetGoldNum;
      default:
        System.err.println("Wrong type parameter for recall");
        return 0.0;
    }
  }

  public double getFScore(String typeStr) {
    double precision = getPrecision(typeStr);
    double recall = getRecall(typeStr);
    if (precision + recall == 0)
      return 0.0;
    else
      return 2.0 * precision * recall / (precision + recall);
  }

  /**
   * This function returns Mean Average Precison
   * 
   * @return
   */
  public double getMAP(String typeStr) {
    double map = 0.0;
    double eplison = 0.00001; // to avoid zero
    int counter = 0;
    int type = 0;

    switch (typeStr) {
      case "document":
        type = typeDocument;
        break;
      case "concept":
        type = typeConcept;
        break;
      case "triple":
        type = typeTriple;
        break;
      case "snippet":
        type = typeSnippet;
        break;
      default:
        System.err.println("Wrong type parameter for getMAP");
        return 0.0;
    }

    ArrayList<Double> currentArr = avgPrecisionArr.get(type);
    for (int i = 0; i < currentArr.size(); i++) {
      if (currentArr.get(i) > eplison) {
        map += currentArr.get(i);
        counter++;
      }
    }
    if (counter == 0)
      return 0.0;
    else
      return map / counter;
  }

  /**
   * This function returns Geometric Mean Average Precison
   * 
   * @return
   */
  public double getGMAP(String typeStr) {
    double eplison = 0.00001; // to avoid zero
    double gmap = 1.0;
    int type = 0;

    switch (typeStr) {
      case "document":
        type = typeDocument;
        break;
      case "concept":
        type = typeConcept;
        break;
      case "triple":
        type = typeTriple;
        break;
      case "snippet":
        type = typeSnippet;
        break;
      default:
        System.err.println("Wrong type parameter for getMAP");
        return 0.0;
    }

    ArrayList<Double> currentArr = avgPrecisionArr.get(type);
    for (int i = 0; i < currentArr.size(); i++) {
      if (currentArr.get(i) > eplison) {
        gmap *= (currentArr.get(i));
      }
    }
    // if gmap not change
    if (gmap == 1.0)
      return 0.0;
    else
      return Math.pow(gmap, 1.0 / avgPrecisionArr.size());
  }

  /**
   * This class is used to store the start and end offset in snippet
   * 
   * @author yifu
   *
   * @param <A>
   * @param <B>
   */
  private class OffsetPair<A, B> {
    private A first;

    private B second;

    public OffsetPair(A first, B second) {
      super();
      this.first = first;
      this.second = second;
    }

    public int hashCode() {
      int hashFirst = first != null ? first.hashCode() : 0;
      int hashSecond = second != null ? second.hashCode() : 0;
      return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
      if (other instanceof OffsetPair) {
        OffsetPair otherPair = (OffsetPair) other;
        return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first
                .equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
                && otherPair.second != null && this.second.equals(otherPair.second))));
      }
      return false;
    }

    public String toString() {
      return "(" + first + ", " + second + ")";
    }

    public A getFirst() {
      return first;
    }

    public void setFirst(A first) {
      this.first = first;
    }

    public B getSecond() {
      return second;
    }

    public void setSecond(B second) {
      this.second = second;
    }
  }
}
