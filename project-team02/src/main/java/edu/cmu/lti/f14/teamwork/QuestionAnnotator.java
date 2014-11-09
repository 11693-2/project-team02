package edu.cmu.lti.f14.teamwork;

import java.io.IOException;
import java.util.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.nlp.Parse;
import edu.cmu.lti.oaqa.type.nlp.Token;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;

public class QuestionAnnotator extends JCasAnnotator_ImplBase {

  private static String PropertiesPath = "PropertiesPATH";

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Question q = (Question) iter.get();
      
      try {
        createTermFreqVector(jcas, q);
      } catch (ConfigurationException | IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * A optimized tokenizer separates words by following separators: _(space) ; . , ? ! () [] {} <>
   * ""
   * 
   * @param doc
   *          input text
   * @return a list of tokens.
   */
  List<String> tokenize(String doc) {
    List<String> res = new ArrayList<String>();
    // changing every word to lowercase to avoid mismatch
    doc = doc.toLowerCase().trim();
    // spreators
    String patternStr = "[\\s\\;\\(\\)\\[\\]\\{\\}\\<\\>\".,?!]+";
    for (String s : doc.split(patternStr)) {
      String tokenStr = StanfordLemmatizer.stemWord(s);
      if (tokenStr.length() - 1 > 0 && tokenStr.charAt(tokenStr.length() - 1) == '\'') {
        tokenStr = (String) tokenStr.subSequence(0, tokenStr.length() - 1);
      }
      res.add(tokenStr);
      // System.out.println(s + "," + tokenStr);
    }
    return res;
  }

  /**
   * This method get Question from outer method, then append AtomicQueryConcept to CAS.
   * 
   * @param jcas
   * @param q
   * @throws ConfigurationException
   * @throws IOException
   * @throws ClientProtocolException
   */

  private void createTermFreqVector(JCas jcas, Question q) throws ConfigurationException,
          ClientProtocolException, IOException {

    // NOTE: not tokenize this time, just get keyword sentence using API
    String questionText = q.getText();

    questionText = questionText.replaceAll("\\pP", ""); // remove all punctuation
    GoPubMedService service = new GoPubMedService(PropertiesPath);
    OntologyServiceResponse.Result diseaseOntologyResult = service.findMeshEntitiesPaged(
            questionText, 0);
    
    //TODO: stdout only for test, remove those output in future
    for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel());
    }
    System.out.println(diseaseOntologyResult.getKeywords());
    
    AtomicQueryConcept query = new AtomicQueryConcept(jcas);
    query.setOriginalText(q.getText());
    query.setText(diseaseOntologyResult.getKeywords());
    query.addToIndexes(jcas);
  }
}
