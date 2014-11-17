package edu.cmu.lti.oaqa.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;

/**
 * This is the evaluator which evaluates the performance of current algorithm
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
   * Calculate positive document num
   * 
   * @param docList
   * @param docGold
   * @return
   */
  public int calDocPositive(Collection<Document> docList, List<String> docGold) {
    Set<String> goldSet = new HashSet<String>();

    for (int i = 0; i < docGold.size(); i++) {
      String str = docGold.get(i);
      goldSet.add(str);
      System.out.println("| golden: " + str);
    }

    double avgPrecision = 0.0;
    int counter = 0;
    int positiveNum = 0;
    for (Document doc : docList) {
      // TODO: change docId to other feature
      counter++;
      //nx.add("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getDocId());
      System.out.println("| doc: http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getDocId());
      if (goldSet.contains("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getDocId())) {
        positiveNum++;
        avgPrecision += (double) positiveNum / counter;
      }
    }
    if(counter != 0){
      avgPrecision /= counter;
    }
    System.out.println("| AVG Precision: " + avgPrecision);
    avgPrecisionArr.add(avgPrecision);

    totalDoc += docList.size();
    supposeDoc += docGold.size();

    // get intersection of two sets
    posDoc += positiveNum;
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
      if(avgPrecisionArr.get(i) > eplison){
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
      if(avgPrecisionArr.get(i) > eplison){
        gmap *= (avgPrecisionArr.get(i));
      }
    }
    return Math.pow(gmap, 1.0 / avgPrecisionArr.size());
  }
}
