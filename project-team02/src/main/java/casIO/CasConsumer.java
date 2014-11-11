package casIO;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

import json.JsonCollectionReaderHelper;
import json.gson.TestQuestion;
import json.gson.TrainingSet;
import json.gson.Question;
import json.gson.TestSet;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import util.TypeUtil;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
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
	
	public void initialize() throws ResourceInitializationException {		
		jsonHelper = new JsonCollectionReaderHelper();
		OUTPUTPATH = "output.json";
		goldstandards = jsonHelper.testRun();
		docMap = new HashMap<String, List<String>>();
		conceptMap = new HashMap<String, List<String>>(); 
		tripleMap = new HashMap<String, List<Triple>> (); 
		for (int i = 0; i<goldstandards.size(); i++){
			docMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getDocuments());
			conceptMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getConcepts());
			tripleMap.put(goldstandards.get(i).getId(), goldstandards.get(i).getTriples());
		}
		
	}

	public void processCas(CAS aCAS) throws ResourceProcessException {

		JCas jcas = null;
		try {
			jcas = aCAS.getJCas();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String qid = TypeUtil.getQuestion(jcas).getId();
		Collection<Document> docList = TypeUtil.getRankedDocuments(jcas);
		Collection<ConceptSearchResult> conceptList = TypeUtil.getRankedConceptSearchResults(jcas);
		Collection<TripleSearchResult> tripleList= TypeUtil.getRankedTripleSearchResults(jcas);
		
		List<String> docGold = docMap.get(qid);
		List<String> conceptGold = conceptMap.get(qid);
		List<Triple> tripleGold = tripleMap.get(qid);
		
		//compare the results...
		/*
		 * TODO: 
		 * TotalPos
		 * TotalNeg
		 * Hit
		 * Miss
		 * Recall
		 * Precision
		 * 
		 * */
		
		//print out results
		/*
		for (Document doc : docList){
			System.out.println(doc.getText()+" score:"+doc.getScore());
		}
		for (ConceptSearchResult concept : conceptList){
			System.out.println(concept.getText()+" score:"+concept.getScore());
		}
		for (TripleSearchResult triple : tripleList){
			System.out.println(triple.getText()+" score:"+triple.getScore());
		}
		*/
				
	}

}
