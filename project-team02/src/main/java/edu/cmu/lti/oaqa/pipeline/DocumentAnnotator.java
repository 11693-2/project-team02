package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();


		// iterate
		while (iter.isValid()) {

			// get the Question type
			Question a = (Question) iter.get();

			String docText = a.getText();
			String text = docText.replace("?", "");

			System.out.println(text);
			
			GoPubMedService service = null;

			Document doc = new Document(aJCas);
			
			try {
				service = new GoPubMedService("project.properties");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PubMedSearchServiceResponse.Result pubmedResult = null;
			try {
				pubmedResult = service.findPubMedCitations(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		    System.out.println(pubmedResult.getSize());
		    for(PubMedSearchServiceResponse.Document documents : pubmedResult.getDocuments()){
		    	 //System.out.println(" >>>>>>>>>>>>>> " + documents.getPmid());
		    	 doc.setDocId(documents.getPmid());
				 doc.setTitle(documents.getTitle());
		    }
		    
		  
			doc.addToIndexes();
			
			System.out.println("***********");

			iter.moveToNext();

		}

	}
}