package edu.cmu.lti.oaqa.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
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

  // currently supposed answer number is golden answer number
  private static int supposeDoc = 0;

  private static int supposeConcept = 0;

  private static int supposeTriple = 0;

  private static ArrayList<Double> avgPrecisionArr = new ArrayList<Double>();

  /**
   * Calculate positive number, total number, supposed number, and average precision of document
   * 
   * @param docList
   * @param docGold
   * @return
   */
  public int calDocPositive(Collection<Document> docList, List<String> docGold) {
    if(docGold.size() == 0) return 0;
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
      avgPrecisionArr.add(avgPrecision);
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
    if(conceptGold.size() == 0) return 0;
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
      avgPrecisionArr.add(avgPrecision);
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
  public int calTriplePositive(Collection<TripleSearchResult> tripleList, List<json.gson.Triple> tripleGold) {
    if(tripleGold.size() == 0) return 0;
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
      avgPrecisionArr.add(avgPrecision);
    }

    totalTriple += tripleList.size();
    supposeTriple += tripleGold.size();

    // get intersection of two sets
    posTriple += positiveNum;
    return positiveNum;
  }

  /**
   * Currently just sum up all three categories of result
   * 
   * @return
   */
  public double getPrecision() {
    if (totalDoc + totalConcept + totalTriple == 0)
      return 0.0;
    return (double) (posDoc + posConcept + posTriple) / (totalDoc + totalConcept + totalTriple);
  }

  /**
   * Currently just sum up all three categories of result
   * 
   * @return
   */
  public double getRecall() {
    if (supposeDoc + supposeConcept + supposeTriple == 0)
      return 0.0;
    return (double) (posDoc + posConcept + posTriple)
            / (supposeDoc + supposeConcept + supposeTriple);
  }

  public double getFScore() {
    double precision = getPrecision();
    double recall = getRecall();
    return 2.0 * precision * recall / (precision + recall);
  }

  /**
   * This function returns Mean Average Precison
   * 
   * @return
   */
  public double getMAP() {
    double map = 0.0;
    double eplison = 0.00001; // to avoid zero
    int counter = 0;
    for (int i = 0; i < avgPrecisionArr.size(); i++) {
      if (avgPrecisionArr.get(i) > eplison) {
        map += avgPrecisionArr.get(i);
        counter++;
      }
    }
    return map / counter;
  }

  /**
   * This function returns Geometric Mean Average Precison
   * 
   * @return
   */
  public double getGMAP() {
    double eplison = 0.00001; // to avoid zero
    double gmap = 1.0;
    for (int i = 0; i < avgPrecisionArr.size(); i++) {
      if (avgPrecisionArr.get(i) > eplison) {
        gmap *= (avgPrecisionArr.get(i));
      }
    }
    return Math.pow(gmap, 1.0 / avgPrecisionArr.size());
  }
}
