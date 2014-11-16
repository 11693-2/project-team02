package edu.cmu.lti.oaqa.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import util.TypeConstants;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.type.answer.CandidateAnswerVariant;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class TripleAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		/**
		 * define an iterator to traverse the content of the cas in form of the
		 * Question Type
		 */
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();

		// iterate
		if (iter.hasNext()&&iter.isValid()) {

			// get the Question type
			Question a = (Question) iter.next();

			String docText = a.getText();
			String text = docText.replace("?", "");

			System.out.println(text);

			GoPubMedService service = null;

			try {
				service = new GoPubMedService("project.properties");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LinkedLifeDataServiceResponse.Result linkedLifeDataResult = null;
			try {
				linkedLifeDataResult = service.findLinkedLifeDataEntitiesPaged(text, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int rank = 1;

			//System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());

			for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
				//System.out.println(" > " + entity.getEntity());
				LinkedLifeDataServiceResponse.Relation relation = entity.getRelations().get(0);
				Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(),
						relation.getObj());
				TripleSearchResult triple_result = TypeFactory.createTripleSearchResult(aJCas, triple);
				triple_result.setRank(rank);
				triple_result.setScore(entity.getScore());
				triple_result.addToIndexes(aJCas);
				rank++;
			}

			/************************************************************************/
			Collection<TripleSearchResult> cs = TypeUtil.getRankedTripleSearchResults(aJCas);

			Collection<TripleSearchResult> result = TypeUtil.rankedSearchResultsByScore(
					JCasUtil.select(aJCas, TripleSearchResult.class), cs.size());

			System.err.println("result size(in consumer):" + result.size());

			Iterator<TripleSearchResult> it = result.iterator();
			rank = 1;
			while (it.hasNext()) {
				TripleSearchResult csr = (TripleSearchResult) it.next();
				csr.setRank(rank);
				rank++;
				
			}
		}

	}

}
