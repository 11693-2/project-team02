package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

/**
 * This is a concept annotator, which gets the question type and return the
 * concept type using Mesh, disease ontology, gene ontology,jochem, uniport.
 * 
 *
 */
public class ConceptAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */
		FSIterator iter = aJCas.getAnnotationIndex(AtomicQueryConcept.type).iterator();
		int rank = 1;

		// iterate
		if (iter.hasNext()) {

			// get the Question type
			AtomicQueryConcept a = (AtomicQueryConcept) iter.next();

			String docText = a.getText();
			String text = docText.replace("?", "");

			System.out.println(text);

			/************************************************************************/

			GoPubMedService service = null;

			try {
				service = new GoPubMedService("project.properties");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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

			// System.err.println("result size(in consumer):"+result.size());

			Iterator<ConceptSearchResult> it = result.iterator();
			rank = 1;
			while (it.hasNext()) {
				ConceptSearchResult csr = (ConceptSearchResult) it.next();
				csr.setRank(rank);
				rank++;
				// System.err.println("CAS score:"+
				// csr.getScore()+csr.getUri()+csr.getRank());
			}
		}
	}

}
