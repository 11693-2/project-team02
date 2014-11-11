package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;

import util.TypeFactory;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;

public class TripleAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();

		List<String> uri = new ArrayList<String>();
		uri.clear();

		// iterate
		while (iter.isValid()) {

			// get the Question type
			Question a = (Question) iter.get();

			String docText = a.getText();
			String text = docText.replace("?", "");

			System.out.println(text);
			
			GoPubMedService service = null;

			//Triple triple = new Triple(aJCas);
			
			try {
				service = new GoPubMedService("project.properties");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			LinkedLifeDataServiceResponse.Result linkedLifeDataResult = null;
			try {
				linkedLifeDataResult = service
				        .findLinkedLifeDataEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
		    for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
		      System.out.println(" > " + entity.getEntity());
		     /* for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
		        System.out.println("   - labels: " + relation.getLabels());
		        System.out.println("   - pred: " + relation.getPred());
		        System.out.println("   - sub: " + relation.getSubj());
		        System.out.println("   - obj: " + relation.getObj());
		        
		        Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(), relation.getObj());
		        triple.addToIndexes();
		        
		      }*/
		      LinkedLifeDataServiceResponse.Relation relation = entity.getRelations().get(0);
		      System.out.println("   - labels: " + relation.getLabels());
		        System.out.println("   - pred: " + relation.getPred());
		        System.out.println("   - sub: " + relation.getSubj());
		        System.out.println("   - obj: " + relation.getObj());
		      Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(), relation.getObj());
		      triple.addToIndexes();
		      
		    }
			
		   /* System.out.println(pubmedResult.getSize());
		    for(PubMedSearchServiceResponse.Document documents : pubmedResult.getDocuments()){
		    	 //System.out.println(" >>>>>>>>>>>>>> " + documents.getPmid());
		    	 doc.setDocId(documents.getPmid());
				 doc.setTitle(documents.getTitle());
		    }*/
		    
			
			System.out.println("***********");

			iter.moveToNext();

		}

	}

}
