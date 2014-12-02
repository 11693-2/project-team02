package edu.cmu.lti.oaqa.pipeline;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import json.JsonCollectionReaderHelper;
import json.JsonFileWriterHelper;
import json.YesNoAnswerFormat;
import json.gson.Snippet;
import json.gson.TestQuestion;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.TypeUtil;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

public class CasConsumer extends CasConsumer_ImplBase {

  public static String OUTPUTPATH = "OutputDocument";

  public static String GOLDPATH = "GoldenAnsPath";

  private Map<String, List<String>> docMap;

  private Map<String, List<String>> conceptMap;

  private Map<String, List<Triple>> tripleMap;

  private Map<String, List<Snippet>> snippetMap;

  private List<TestQuestion> goldstandards;

  private JsonCollectionReaderHelper jsonHelper;

  private Evaluator evaluator;
  
  private ArrayList<YesNoAnswerFormat> answerArr;
  
  private ArrayList<Boolean> validateAnsArr; //store if this answer is YesNo question
  
  private static int ansIndex;

  public void initialize() throws ResourceInitializationException {
    jsonHelper = new JsonCollectionReaderHelper();
    //FileWriter fw = new FileWriter(((String) getConfigParameterValue(PARAM_OUTPUTPATH)).trim(), false);
    goldstandards = jsonHelper.getGoldenAns(((String) getConfigParameterValue(GOLDPATH)).trim());

    // Dictionaries of golden standard answer
    docMap = new HashMap<String, List<String>>();
    conceptMap = new HashMap<String, List<String>>();
    tripleMap = new HashMap<String, List<Triple>>();
    snippetMap = new HashMap<String, List<Snippet>>();

    validateAnsArr = new ArrayList<Boolean>();
    ansIndex = 0;
    
    // get golden answer
    for (int i = 0; i < goldstandards.size(); i++) {
      if(goldstandards.get(i).getType().equals("yesno")){
        docMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getDocuments());
        conceptMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getConcepts());
        tripleMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getTriples());
        snippetMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getSnippets());
        validateAnsArr.add(true);
      } else {
        validateAnsArr.add(false);
      }
    }

    // initialize evaluator
    evaluator = new Evaluator();
    
    //initialize answer array
    answerArr = new ArrayList<YesNoAnswerFormat>();
    
    System.out.println("Consumer initialize");
  }

  // TODO: create answer type, call writer helper
  public void processCas(CAS aCAS) throws ResourceProcessException {

    if(validateAnsArr.get(ansIndex) == true){//if this answer is not yes no question
      JCas jcas = null;
      try {
        jcas = aCAS.getJCas();
      } catch (Exception e) {
        e.printStackTrace();
      }
      String qid = TypeUtil.getQuestion(jcas).getId();

      // create empty list if no list returned, to avoid null pointer exception
      Collection<Document> docList = (TypeUtil.getRankedDocuments(jcas) == null) ? new ArrayList<Document>()
              : TypeUtil.getRankedDocuments(jcas);
      Collection<ConceptSearchResult> conceptList = (TypeUtil.getRankedConceptSearchResults(jcas) == null) ? new ArrayList<ConceptSearchResult>()
              : TypeUtil.getRankedConceptSearchResults(jcas);
      Collection<TripleSearchResult> tripleList = (TypeUtil.getRankedTripleSearchResults(jcas) == null) ? new ArrayList<TripleSearchResult>()
              : TypeUtil.getRankedTripleSearchResults(jcas);
      Collection<Passage> snippetList = (TypeUtil.getRankedPassages(jcas) == null) ? new ArrayList<Passage>()
              : TypeUtil.getRankedPassages(jcas);
      Collection<Answer> answerList = (TypeUtil.getRankedPassages(jcas) == null) ? new ArrayList<Answer>()
              : TypeUtil.getAnswers(jcas);

      // create golden standard answer list for evaluator
      List<String> docGold = (docMap.get(qid) == null) ? new ArrayList<String>() : docMap.get(qid);
      List<String> conceptGold = (conceptMap.get(qid) == null) ? new ArrayList<String>() : conceptMap
              .get(qid);
      List<Triple> tripleGold = (tripleMap.get(qid) == null) ? new ArrayList<Triple>() : tripleMap
              .get(qid);
      List<Snippet> snippetGold = (snippetMap.get(qid) == null) ? new ArrayList<Snippet>()
              : snippetMap.get(qid);

      System.out.println("+-----------------------------------------");
      System.out.println("| Evaluatation");
      System.out.println("+-----------document----------------------");
      System.out.println("| doc ans size: " + docList.size() + " | gold ans size:" + docGold.size());
      System.out.println("| postive: " + evaluator.calDocPositive(docList, docGold));
      System.out.println("+-----------concept----------------------");
      System.out.println("| concept ans size: " + conceptList.size() + " | gold ans size:"
              + conceptGold.size());
      System.out.println("| postive: " + evaluator.calConceptPositive(conceptList, conceptGold));
      System.out.println("+-----------triple----------------------");
      System.out.println("| triple ans size: " + tripleList.size() + " | gold ans size:"
              + tripleGold.size());
      System.out.println("| postive: " + evaluator.calTriplePositive(tripleList, tripleGold));
      System.out.println("+-----------snippet----------------------");
      System.out.println("| snippet ans size: " + snippetList.size() + " | gold ans size:"
              + snippetGold.size());
      System.out.println("| postive: " + evaluator.calSnippetPositive(snippetList, snippetGold));
      System.out.println("+-----------------------------------------");

      // print out results
      /*
       * for (Document doc : docList){ System.out.println(doc.getText()+" score:"+doc.getScore()); }
       * for (ConceptSearchResult concept : conceptList){
       * System.out.println(concept.getText()+" score:"+concept.getScore()); } for (TripleSearchResult
       * triple : tripleList){ System.out.println(triple.getText()+" score:"+triple.getScore()); }
       */

      /*
       * int count = 0; for (Passage snippet : snippetList) { if (count == 5) break;
       * 
       * System.out.println("beginSection:" + snippet.getBeginSection());
       * System.out.println("document:" + snippet.getUri()); System.out.println("endSection:" +
       * snippet.getEndSection()); System.out.println("offsetInBeginSection:" +
       * snippet.getOffsetInBeginSection()); System.out.println("offsetInEndSection:" +
       * snippet.getOffsetInEndSection()); System.out.println("text:" + snippet.getText());
       * System.out.println("Score:" + snippet.getScore()); count++; }
       */

      // output answer to json file
      YesNoAnswerFormat format = new YesNoAnswerFormat();
      format.setBody(TypeUtil.getQuestion(jcas).getText());
      // add concept
      for (ConceptSearchResult c : conceptList) {
        format.getConcepts().add(c.getUri());
      }
      // add document
      for (Document doc : docList) {
        format.getDocuments().add("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getDocId());
      }
      
      //set exact answer
      Iterator<Answer> iter = answerList.iterator();
      if(iter.hasNext()){ //there will only be one answer -- yes or no
        String ansText = iter.next().getText();
        int divider = ansText.indexOf(",");
        format.setExact_answer(ansText.substring(0, divider-1));
        format.setIdeal_answer(ansText.substring(divider));
      }
      
      format.setId(qid);
      format.setExact_answer("...");
      // add snippet
      for (Passage p : snippetList) {
        Snippet s = new Snippet("http://www.ncbi.nlm.nih.gov/pubmed/" + p.getDocId(), p.getText(),
                p.getOffsetInBeginSection(), p.getOffsetInEndSection(), p.getBeginSection(),
                p.getEndSection());
        format.getSnippets().add(s);
      }
      for (TripleSearchResult result : tripleList) {
        Triple t = new Triple(result.getTriple().getSubject(), result.getTriple().getPredicate(),
                result.getTriple().getObject());
        format.getTriples().add(t);
      }
      format.setType("yesno");
      answerArr.add(format);
      ansIndex++;
    }
  }

  /**
   * This method is called after all objects are processed. We print precision, recall, F-score,
   * MAP, and GMAP here
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {
    DecimalFormat df = new DecimalFormat("0.0000");
    System.out.println("+-------------------------------------");
    System.out.println("|       Performance Report           |");
    System.out.println("+------------------------------------+");
    System.out.println("| Document                           ");
    System.out.println("| Precision: " + df.format(evaluator.getPrecision("document")));
    System.out.println("| Recall: " + df.format(evaluator.getRecall("document")));
    System.out.println("| F-Score: " + df.format(evaluator.getFScore("document")));
    System.out.println("| MAP: " + df.format(evaluator.getMAP("document")));
    System.out.println("| GMAP: " + df.format(evaluator.getGMAP("document")));
    System.out.println("+------------------------------------+");
    System.out.println("| Concept                            ");
    System.out.println("| Precision: " + df.format(evaluator.getPrecision("concept")));
    System.out.println("| Recall: " + df.format(evaluator.getRecall("concept")));
    System.out.println("| F-Score: " + df.format(evaluator.getFScore("concept")));
    System.out.println("| MAP: " + df.format(evaluator.getMAP("concept")));
    System.out.println("| GMAP: " + df.format(evaluator.getGMAP("concept")));
    System.out.println("+------------------------------------+");
    System.out.println("| Triple                             ");
    System.out.println("| Precision: " + df.format(evaluator.getPrecision("triple")));
    System.out.println("| Recall: " + df.format(evaluator.getRecall("triple")));
    System.out.println("| F-Score: " + df.format(evaluator.getFScore("triple")));
    System.out.println("| MAP: " + df.format(evaluator.getMAP("triple")));
    System.out.println("| GMAP: " + df.format(evaluator.getGMAP("triple")));
    System.out.println("+------------------------------------+");
    System.out.println("| Snippet                            ");
    System.out.println("| Precision: " + df.format(evaluator.getPrecision("snippet")));
    System.out.println("| Recall: " + df.format(evaluator.getRecall("snippet")));
    System.out.println("| F-Score: " + df.format(evaluator.getFScore("snippet")));
    System.out.println("| MAP: " + df.format(evaluator.getMAP("snippet")));
    System.out.println("| GMAP: " + df.format(evaluator.getGMAP("snippet")));
    System.out.println("+------------------------------------+");
    
    //use helper to write answer to json file
    JsonFileWriterHelper helper = new JsonFileWriterHelper();
    helper.writeAnswerToFile(((String) getConfigParameterValue(OUTPUTPATH)).trim(), answerArr);
  }

}
