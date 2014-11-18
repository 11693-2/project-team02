package edu.cmu.lti.oaqa.pipeline;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

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
		
		//System.out.println("question annotator");
		
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();
		Question question = (Question)iter.get();
		
		//split by white space
		String queText = question.getText();
		List<String> wordList = new ArrayList<String>();
		for (String s: queText.split("\\s+"))
		   wordList.add(s);
		
		//stop words list from input file 
		List<String> stopWordList = new LinkedList<String>();
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader("src/main/resources/data/stopwords.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while(bf.readLine() != null){
				stopWordList.add(bf.readLine());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// regular expression for punctuation removal 
		// stop words list for stop word removal 
		String t = null;
		for(String word : wordList){
			if(Pattern.matches("\\p{Punct}+", word)||stopWordList.contains(word)){
				continue;
			}
			t = t + " " + word;	
		}

		// AtomicQueryConcept to cas
		AtomicQueryConcept atomic = new AtomicQueryConcept(aJCas); 
		atomic.setText(t);
		atomic.setOriginalText(question.getText());
		atomic.addToIndexes();
		
		List<AtomicQueryConcept> list = new ArrayList<AtomicQueryConcept>();
		list.add(atomic);
		
		
		//ComplexQueryConcept to cas
		ComplexQueryConcept complex = new ComplexQueryConcept(aJCas); 
		complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, list));
		complex.addToIndexes();
//		while (iter.hasNext()){
//			Question question = (Question)iter.next();
//			String queText = question.getText();
//	
//			// stem words with StanfordLemmatizer
//			String stemmedQue = StanfordLemmatizer.stemText(queText);
//			
//			// remove stop words by StopWordRemover
//			StopWordRemover stopWordRemover = StopWordRemover.getInstance();
//			String StopRemovedQue = stopWordRemover.removeStopWords(stemmedQue);
//			
//			//System.err.println("fin11111111:"+ StopRemovedQue);
//			
//			// tokenize question with OpenNLP
////			OpenNLPTokenization OpenNLPTokenizer = OpenNLPTokenization.getInstance();
////			List<String> wordList = new ArrayList<String>();
////			wordList = OpenNLPTokenizer.tokenize(StopRemovedQue);
////			
////			for(String t : wordList){
////				System.err.println(t);
////			}
////			
////			HashMap<String, Integer> tokenMap = new HashMap<String, Integer>();
////			for (String token: wordList){
////				if(Pattern.matches("\\p{Punct}+", token)){
////					continue;
////				}
////				if (tokenMap.containsKey(token)){
////					tokenMap.put(token, tokenMap.get(token)+1);
////				}else{
////					tokenMap.put(token, 1);	
////				}
////			}
//			
//			
//
////			String t = "";
////			for(String key : tokenMap.keySet()){
////				t = t + " " + key;	
////				System.err.println("fin:"+ t);
////			}
//			
//			//System.err.println("final:"+ t);
//			
//			 
//			// AtomicQueryConcept to cas
//			AtomicQueryConcept atomic = new AtomicQueryConcept(aJCas); 
//			atomic.setText(StopRemovedQue);
//			atomic.setOriginalText(question.getText());
//			atomic.addToIndexes(aJCas);
//			//System.out.println("finish1");
//			
//			//ComplexQueryConcept to cas
//			List<AtomicQueryConcept> list = new ArrayList<AtomicQueryConcept>();
//			list.add(atomic);
//			ComplexQueryConcept complex = new ComplexQueryConcept(aJCas); 
//			complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, list));
//			complex.addToIndexes();
//			
//		}
	
		//System.out.println("finish question annotation");
		
	}

}
