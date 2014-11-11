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
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;

/**
 * This is a concept annotator, which gets the question type and return the
 * concept type using Mesh, disease ontology, gene ontology,jochem, uniport.
 * 
 * @author leixiao
 *
 */
public class ConceptAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

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
			
			Concept concept = new Concept(aJCas);
			
			
			try {
				service = new GoPubMedService("project.properties");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			    
			    OntologyServiceResponse.Result diseaseOntologyResult = null;
				try {
					diseaseOntologyResult = service
					        .findDiseaseOntologyEntitiesPaged(text, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  //  System.out.println("Disease ontology: " + diseaseOntologyResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
			     /* System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());
			      */
			      //uri.add(finding.getConcept().getUri());
			    	//System.out.println(finding.getConcept().getUri());
			    	if(finding.getScore()>0.1)
			    	 uri.add(finding.getConcept().getUri());
			      
			      
			    }
			   
			    
			    
			    OntologyServiceResponse.Result geneOntologyResult = null;
				try {
					geneOntologyResult = service.findGeneOntologyEntitiesPaged(text,
					        0, 10);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    //System.out.println("Gene ontology: " + geneOntologyResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
			     /* System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    	if(finding.getScore()>0.1)
				    	 uri.add(finding.getConcept().getUri());
			    	
			    }
			    OntologyServiceResponse.Result jochemResult = null;
				try {
					jochemResult = service.findJochemEntitiesPaged(text, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  //  System.out.println("Jochem: " + jochemResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    	if(finding.getScore()>0.1)
				    	 uri.add(finding.getConcept().getUri());
			    	
			    }
			    OntologyServiceResponse.Result meshResult = null;
				try {
					meshResult = service.findMeshEntitiesPaged(text, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  //  System.out.println("MeSH: " + meshResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    	if(finding.getScore()>0.1)
				    	 uri.add(finding.getConcept().getUri());
			    	
			    }
			    OntologyServiceResponse.Result uniprotResult = null;
				try {
					uniprotResult = service.findUniprotEntitiesPaged(text, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  //  System.out.println("UniProt: " + uniprotResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    	if(finding.getScore()>0.1)
				    	 uri.add(finding.getConcept().getUri());
			    	
			    }

			
			    
			    concept.addToIndexes();
			   
			   /* for(int i=0; i<uri.size();i++){  
			        System.out.println(uri.get(i));  
			       }  
			    uri.clear(); 
			    
			    System.out.println("***********");*/
			
			    iter.moveToNext();

		}
	}

}
