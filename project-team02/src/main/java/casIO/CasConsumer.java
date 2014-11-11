package casIO;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;

import json.gson.TrainingSet;
import json.gson.Question;
import json.gson.TestSet;

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

	public static final String PATH = "Output";
	public static final String Standard = "goldenstandard"; 
	public String filePath;
	public String standardPath;
	private Writer fileWriter = null;
	private List<Question> gold;
	
	public void initialize() throws ResourceInitializationException {
		filePath = (String) getConfigParameterValue(PATH);
	    
		if(filePath == null){
			throw new ResourceInitializationException(
				ResourceInitializationException.CONFIG_SETTING_ABSENT, 
				new Object[] {"output file initialization fail"}
			);	
		}
		
		try {
	        fileWriter = new FileWriter(new File(filePath));
	    
	      } catch (IOException e) {
	        e.printStackTrace();
	      }

		//get gloden standard file
		standardPath  = (String) getConfigParameterValue(Standard);	
		gold = TestSet.load(getClass().getResourceAsStream(standardPath)).stream().collect(toList());;
		gold.stream().filter(input->input.getBody() != null).forEach(input->input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));	
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
		for (Document doc : docList){
			System.out.println(doc.getText()+" score:"+doc.getScore());
		}
		for (ConceptSearchResult concept : conceptList){
			System.out.println(concept.getText()+" score:"+concept.getScore());
		}
		for (TripleSearchResult triple : tripleList){
			System.out.println(triple.getText()+" score:"+triple.getScore());
		}
				
	}

}
