package edu.cmu.lti.oaqa.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import json.JsonCollectionReaderHelper;
import json.gson.TestQuestion;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.TypeUtil;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class CasConsumer extends CasConsumer_ImplBase {

  public static String OUTPUTPATH = "";

  public static String GOLDPATH = "";

  private Map<String, List<String>> docMap;

  private Map<String, List<String>> conceptMap;

  private Map<String, List<Triple>> tripleMap;

  private List<TestQuestion> goldstandards;

  private JsonCollectionReaderHelper jsonHelper;

  private Evaluator evaluator;

  public void initialize() throws ResourceInitializationException {
    jsonHelper = new JsonCollectionReaderHelper();
    OUTPUTPATH = "output.json";
    goldstandards = jsonHelper.testRun();

    docMap = new HashMap<String, List<String>>();
    conceptMap = new HashMap<String, List<String>>();
    tripleMap = new HashMap<String, List<Triple>>();
    for (int i = 0; i < goldstandards.size(); i++) {
      docMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getDocuments());
      conceptMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getConcepts());
      tripleMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getTriples());
    }

    evaluator = new Evaluator();
  }

  public void processCas(CAS aCAS) throws ResourceProcessException {

    JCas jcas = null;
    try {
      jcas = aCAS.getJCas();
    } catch (Exception e) {
      e.printStackTrace();
    }
    String qid = TypeUtil.getQuestion(jcas).getId();
    
    //create empty list if no list returned, to avoid null pointer exception
    Collection<Document> docList = TypeUtil.getRankedDocuments(jcas) == null ? new ArrayList<Document>()
            : TypeUtil.getRankedDocuments(jcas);
    Collection<ConceptSearchResult> conceptList = TypeUtil.getRankedConceptSearchResults(jcas) == null ? new ArrayList<ConceptSearchResult>()
            : TypeUtil.getRankedConceptSearchResults(jcas);
    Collection<TripleSearchResult> tripleList = TypeUtil.getRankedTripleSearchResults(jcas) == null ? new ArrayList<TripleSearchResult>()
            : TypeUtil.getRankedTripleSearchResults(jcas);

    List<String> docGold = docMap.get(qid) == null ? new ArrayList<String>() : docMap.get(qid);
    List<String> conceptGold = conceptMap.get(qid) == null ? new ArrayList<String>() : conceptMap
            .get(qid);
    List<Triple> tripleGold = tripleMap.get(qid) == null ? new ArrayList<Triple>() : tripleMap
            .get(qid);

    System.out.println("------------------------------------------");
    System.out.println("| Evaluatation");
    System.out.println("|-----------document----------------------");
    System.out.println("| doc ans size: " + docList.size() + " | gold ans size:" + docGold.size());
    System.out.println("| postive: " + evaluator.calDocPositive(docList, docGold));
    System.out.println("|-----------concept----------------------");
    System.out.println("| concept ans size: " + conceptList.size() + " | gold ans size:"
            + conceptGold.size());
    System.out.println("| postive: " + evaluator.calConceptPositive(conceptList, conceptGold));
    System.out.println("|-----------triple----------------------");
    System.out.println("| triple ans size: " + tripleList.size() + " | gold ans size:"
            + tripleGold.size());
    System.out.println("| postive: " + evaluator.calTriplePositive(tripleList, tripleGold));
    System.out.println("------------------------------------------");

    // print out results
    /*
     * for (Document doc : docList){ System.out.println(doc.getText()+" score:"+doc.getScore()); }
     * for (ConceptSearchResult concept : conceptList){
     * System.out.println(concept.getText()+" score:"+concept.getScore()); } for (TripleSearchResult
     * triple : tripleList){ System.out.println(triple.getText()+" score:"+triple.getScore()); }
     */

  

  }

  /**
   * This method is called after all objects are processed. We calculate precision, recall F-score
   * here
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {
    System.out.println("-------------------------------------");
    System.out.println("| Performance Report");
    System.out.println("| Precision: " + evaluator.getPrecision());
    System.out.println("| Recall: " + evaluator.getRecall());
    System.out.println("| F-Score: " + evaluator.getFScore());
    System.out.println("| Mean Average Precison: " + evaluator.getMAP());
    System.out.println("| Geometric Mean Average Precison: " + evaluator.getGMAP());
    System.out.println("-------------------------------------");
  }

}
