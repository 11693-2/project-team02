package edu.cmu.lti.oaqa.pipeline;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import util.TypeFactory;
import util.Utils;
import edu.cmu.lti.oaqa.type.input.Keyword;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;

public class QuestionModificationAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		
		//remove punctuations and stopwords in questions
		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();
		while (iter.isValid()){
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
				bf = new BufferedReader(new FileReader("src/main/resources/stopwords.txt"));
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
			
			
			// ComplexQueryConcept to cas
			//ComplexQueryConcept complex = new ComplexQueryConcept(aJCas); 
			//complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, list));
			//complex.addToIndexes();
			
//			not use anymore...		
//			keywords store in cas 
//			Keyword keyword = new Keyword(aJCas);
//			keyword.setId(question.getId());
//			keyword.setQuestionType(question.getQuestionType());
//			keyword.setSource(question.getSource());
//			keyword.setText(t);
//			keyword.addToIndexes(aJCas);
			
		}
		
		
		
	}

}
