package edu.cmu.lti.oaqa.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import util.StanfordLemmatizer;
import util.StopWordRemover;
import util.Utils;
//import util.Utils;
//import edu.cmu.lti.oaqa.type.input.Keyword;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.ComplexQueryConcept;

public class QuestionAnnotator extends JCasAnnotator_ImplBase {

	public static String readToString(String fileName) {
		String encoding = "ISO-8859-1";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}

	public String stopwords;

	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		stopwords = readToString("src/main/resources/data/stopwords.txt");

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

		// System.out.println("question annotator");

		FSIterator iter = aJCas.getAnnotationIndex(Question.type).iterator();
		if (iter.isValid()) {
			Question question = (Question) iter.get();

			String queText = question.getText();
			/************************************************************/
			
			String m=queText.replaceAll("[\\p{Punct}]+", " ");
			
			System.err.println("^^^^^^"+m);
			
			String stemmedQue = StanfordLemmatizer.stemText(m);
			
			//StopWordRemover stopWordRemover = StopWordRemover.getInstance();			
			//String StopRemovedQue = stopWordRemover.removeStopWords(stemmedQue);

			// split by white space
			List<String> wordList = new ArrayList<String>();
			for (String s : stemmedQue.split("\\s+"))
				wordList.add(s);
			
			

			// stop words list from input file
			// regular expression for punctuation removal
			// stop words list for stop word removal
			
			String t = null;
			for (String word : wordList) {
				if (stopwords.contains(word)) {
					continue;
				}
				if(t!=null)
					t = t + " " + word;
				else t=word;
			}

			// AtomicQueryConcept to cas
			
			
			System.err.println(t);
			
			AtomicQueryConcept atomic = new AtomicQueryConcept(aJCas);
			atomic.setText(t);
			atomic.setOriginalText(question.getText());
			atomic.addToIndexes(aJCas);

			List<AtomicQueryConcept> list = new ArrayList<AtomicQueryConcept>();
			list.add(atomic);

			// ComplexQueryConcept to cas
			ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
			complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, list));
			complex.addToIndexes();

			/***************************************************************/

			iter.moveToNext();
		}
		// Question question = (Question)iter.get();

		// while (iter.hasNext()){
		// Question question = (Question)iter.next();
		// String queText = question.getText();
		//
		// // stem words with StanfordLemmatizer
		// String stemmedQue = StanfordLemmatizer.stemText(queText);
		//
		// // remove stop words by StopWordRemover
		// StopWordRemover stopWordRemover = StopWordRemover.getInstance();
		// String StopRemovedQue = stopWordRemover.removeStopWords(stemmedQue);
		//
		// //System.err.println("fin11111111:"+ StopRemovedQue);
		//
		// // tokenize question with OpenNLP
		// // OpenNLPTokenization OpenNLPTokenizer =
		// OpenNLPTokenization.getInstance();
		// // List<String> wordList = new ArrayList<String>();
		// // wordList = OpenNLPTokenizer.tokenize(StopRemovedQue);
		// //
		// // for(String t : wordList){
		// // System.err.println(t);
		// // }
		// //
		// // HashMap<String, Integer> tokenMap = new HashMap<String,
		// Integer>();
		// // for (String token: wordList){
		// // if(Pattern.matches("\\p{Punct}+", token)){
		// // continue;
		// // }
		// // if (tokenMap.containsKey(token)){
		// // tokenMap.put(token, tokenMap.get(token)+1);
		// // }else{
		// // tokenMap.put(token, 1);
		// // }
		// // }
		//
		//
		//
		// // String t = "";
		// // for(String key : tokenMap.keySet()){
		// // t = t + " " + key;
		// // System.err.println("fin:"+ t);
		// // }
		//
		// //System.err.println("final:"+ t);
		//
		//
		// // AtomicQueryConcept to cas
		// AtomicQueryConcept atomic = new AtomicQueryConcept(aJCas);
		// atomic.setText(StopRemovedQue);
		// atomic.setOriginalText(question.getText());
		// atomic.addToIndexes(aJCas);
		// //System.out.println("finish1");
		//
		// //ComplexQueryConcept to cas
		// List<AtomicQueryConcept> list = new ArrayList<AtomicQueryConcept>();
		// list.add(atomic);
		// ComplexQueryConcept complex = new ComplexQueryConcept(aJCas);
		// complex.setOperatorArgs(Utils.fromCollectionToFSList(aJCas, list));
		// complex.addToIndexes();
		//
		// }

		// System.out.println("finish question annotation");

	}

}
