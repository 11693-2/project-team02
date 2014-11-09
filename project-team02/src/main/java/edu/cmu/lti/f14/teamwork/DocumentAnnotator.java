package edu.cmu.lti.f14.teamwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.nlp.Parse;
import edu.cmu.lti.oaqa.type.nlp.Token;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {

  private static String PropertiesPath = "PropertiesPATH";
  GoPubMedService service;
  
  /**
   * get AtomicQueryConcept type from last annotator, output document type
   * 
   */
  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    try {
      service = new GoPubMedService(PropertiesPath);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    FSIterator it = jcas.getAnnotationIndex(AtomicQueryConcept.type).iterator();
    while(it.hasNext()){
      AtomicQueryConcept parse = (AtomicQueryConcept) it.next();
      System.out.println("query: " + parse.getText());
      try {
        PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(parse.getText(), 0);
        ArrayList<Document> list = (ArrayList<Document>) pubmedResult.getDocuments();
        //print result url
        for(int i=0; i<list.size(); i++){
          String url = "http://www.ncbi.nlm.nih.gov/pubmed/" + list.get(i).getPmid();
          System.out.println("document result: " + url);
          edu.cmu.lti.oaqa.type.retrieval.Document d = new edu.cmu.lti.oaqa.type.retrieval.Document(jcas);
          d.setTitle(url);
          d.setDocId(String.valueOf(i));
          d.addToIndexes(jcas);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
