package edu.cmu.lti.oaqa.pipeline;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;

import util.StanfordLemmatizer;
import util.StopWordRemover;
import util.Utils;
//import util.Utils;
//import edu.cmu.lti.oaqa.type.input.Keyword;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;
import util.OpenNLPTokenization;
public class QuestionAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		
		
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();
		while (iter.isValid()){
			Question question = (Question)iter.get();
			String queText = question.getText();
			// stem words with StanfordLemmatizer
			String stemmedQue = StanfordLemmatizer.stemText(queText);
			// remove stop words by StopWordRemover
			StopWordRemover stopWordRemover = StopWordRemover.getInstance();
			String StopRemovedQue = stopWordRemover.removeStopWords(stemmedQue);
			// tokenize question with OpenNLP
			OpenNLPTokenization OpenNLPTokenizer = OpenNLPTokenization.getInstance();
			List<String> wordList = new ArrayList<String>();
			wordList = OpenNLPTokenizer.tokenize(stemmedQue);
			
			HashMap<String, Integer> tokenMap = new HashMap<String, Integer>();
			for (String token: wordList){
				if (tokenMap.containsKey(token)){
					tokenMap.put(token, tokenMap.get(token)+1);
				}else{
					tokenMap.put(token, 1);	
				}
			}
			
			// AtomicQueryConcept to cas
			String t = "";
			for(String key : tokenMap.keySet()){
				t = t + " " + key;	
			}
			AtomicQueryConcept atomic = new AtomicQueryConcept(aJCas); 
			atomic.setText(t);
			atomic.setOriginalText(question.getText());
			atomic.addToIndexes();
			
			//ComplexQueryConcept to cas
			List<AtomicQueryConcept> list = new ArrayList<AtomicQueryConcept>();
			list.add(atomic);
			ComplexQueryConcept complex = new ComplexQueryConcept(aJCas); 
			complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, list));
			complex.addToIndexes();
			
		}
		
		
		
	}

}
