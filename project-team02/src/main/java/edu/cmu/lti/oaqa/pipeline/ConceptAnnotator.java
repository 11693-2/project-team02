package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.StanfordLemmatizer;
import util.StopWordRemover;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

/**
 * This is a concept annotator, which gets the question type and return the
 * concept type using Mesh, disease ontology, gene ontology,jochem, uniport.
 * 
 *
 */
public class ConceptAnnotator extends JCasAnnotator_ImplBase {
	
	GoPubMedService service=null;
	public void initialize(UimaContext aContext) throws ResourceInitializationException{
		super.initialize(aContext);

		try {
			service = new GoPubMedService("project.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  }
	 

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */

		
	    FSIterator<TOP> iter = aJCas.getJFSIndexRepository().getAllIndexedFS
	    		(AtomicQueryConcept.type);
		int rank = 1;

		// iterate
		if (iter.hasNext()) {

			AtomicQueryConcept a = (AtomicQueryConcept) iter.next();
			/*String docText = a.getText();
			String text = docText;
			System.out.println(text);
			*/
			String text=a.getOriginalText().replace("?","").trim();
			//String text=a.getOriginalText().toLowerCase().replace("?", "").trim();
			//System.out.println(text+"----heihei");
			/************************************************************************/

			OntologyServiceResponse.Result diseaseOntologyResult = null;
			try {
				diseaseOntologyResult = service.findDiseaseOntologyEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			rank = 1;
			for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
				
				if(finding==null) break;
				
				if(finding.getScore()<0.1) break;

				Concept concept = TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), finding
						.getConcept().getUri());
				ConceptSearchResult concept_search_result = TypeFactory.createConceptSearchResult(aJCas, concept,
						finding.getConcept().getUri(), finding.getScore(), "", "");
				concept_search_result.setRank(rank);
				concept_search_result.addToIndexes(aJCas);
				rank++;

			}

			/************************************************************************/

			OntologyServiceResponse.Result geneOntologyResult = null;
			try {
				geneOntologyResult = service.findGeneOntologyEntitiesPaged(text, 0, 10);
			} catch (IOException e) {
				e.printStackTrace();
			}

			rank = 1;
			for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
				if(finding==null) break;
				
				if(finding.getScore()<0.1) break;

				Concept concept = TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), finding
						.getConcept().getUri());
				ConceptSearchResult concept_search_result = TypeFactory.createConceptSearchResult(aJCas, concept,
						finding.getConcept().getUri(), finding.getScore(), "", "");
				concept_search_result.setRank(rank);
				concept_search_result.addToIndexes(aJCas);
				rank++;

			}
			/************************************************************************/

			OntologyServiceResponse.Result jochemResult = null;
			try {
				jochemResult = service.findJochemEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			rank = 1;
			for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
				if(finding==null) break;
				if(finding.getScore()<0.1) break;
				
				Concept concept = TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), finding
						.getConcept().getUri());
				ConceptSearchResult concept_search_result = TypeFactory.createConceptSearchResult(aJCas, concept,
						finding.getConcept().getUri(), finding.getScore(), "", "");
				concept_search_result.setRank(rank);
				concept_search_result.addToIndexes(aJCas);
				rank++;

			}
			/************************************************************************/

			OntologyServiceResponse.Result meshResult = null;
			try {
				meshResult = service.findMeshEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("MeSH: " + meshResult.getFindings().size());
			rank = 1;
			for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {

				if(finding==null) break;
				if(finding.getScore()<0.1) break;
				
				Concept concept = TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), finding
						.getConcept().getUri());
				ConceptSearchResult concept_search_result = TypeFactory.createConceptSearchResult(aJCas, concept,
						finding.getConcept().getUri(), finding.getScore(), "", "");
				concept_search_result.setRank(rank);
				concept_search_result.addToIndexes(aJCas);
				rank++;

			}

			/************************************************************************/

			OntologyServiceResponse.Result uniprotResult = null;
			try {
				uniprotResult = service.findUniprotEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println("UniProt: " +
			// uniprotResult.getFindings().size());
			rank = 1;
			for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {

				if(finding==null) break;
				if(finding.getScore()<0.1) break;

				Concept concept = TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), finding
						.getConcept().getUri());
				ConceptSearchResult concept_search_result = TypeFactory.createConceptSearchResult(aJCas, concept,
						finding.getConcept().getUri(), finding.getScore(), "", "");
				concept_search_result.setRank(rank);
				concept_search_result.addToIndexes(aJCas);
				rank++;

			}

			/************************************************************************/
			Collection<ConceptSearchResult> cs = TypeUtil.getRankedConceptSearchResults(aJCas);

			Collection<ConceptSearchResult> result = TypeUtil.rankedSearchResultsByScore(
					JCasUtil.select(aJCas, ConceptSearchResult.class), cs.size());

			 System.err.println("concept result size(in consumer):"+result.size());

			Iterator<ConceptSearchResult> it = result.iterator();
			rank = 1;
			while (it.hasNext()) {
				ConceptSearchResult csr = (ConceptSearchResult) it.next();
				csr.setRank(rank);
				rank++;
				
			}
		}
	}

}
