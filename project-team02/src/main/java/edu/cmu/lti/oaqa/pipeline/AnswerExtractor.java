package edu.cmu.lti.oaqa.pipeline;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import json.JsonCollectionReaderHelper;
import json.gson.TestQuestion;
import json.gson.TestSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.StanfordLemmatizer;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.answer.CandidateAnswerVariant;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.Passage;




public class AnswerExtractor extends JCasAnnotator_ImplBase {
	
	/** calculate the number of each word in a sentence **/
	
	public Map<String, Double> docVector;
	public Map<String, Double> senVector;
	
	public static int calculate(String input, String target) {
		int count = 0;
		StringTokenizer tokenizer = new StringTokenizer(input);
		while (tokenizer.hasMoreElements()) {
			String element = (String) tokenizer.nextElement();

			/** not ignore the case to compare */

			if (target.equalsIgnoreCase(element))
				count++;
		}
		return count;
	}
	
	private double computeCosineSimilarity(Map<String, Double> queryVector, Map<String, Double> docVector) {
		double cosine_similarity = 0.0;

		double vec = 0.0;

		Map<String, Double> temp = new HashMap<String, Double>();

		temp.putAll(queryVector);
		temp.putAll(docVector);
		/**
		 * combine the two map together to form a new map which contains the key
		 * in query or doc
		 **/
		for (Map.Entry<String, Double> entry : temp.entrySet()) {
			String a = entry.getKey();

			/** if the key is both in query and document, we will calculate **/
			if (queryVector.containsKey(a) && docVector.containsKey(a)) {
				vec += queryVector.get(a) * docVector.get(a);
			}

		}
		double doc1 = 0.0;
		double doc2 = 0.0;

		/** using the cosine similarity formula to calculate the score **/

		for (Map.Entry<String, Double> entry : queryVector.entrySet()) {
			doc1 += Math.pow(entry.getValue(), 2);
		}
		for (Map.Entry<String, Double> entry : docVector.entrySet()) {
			doc2 += Math.pow(entry.getValue(), 2);
		}

		double sq1 = Math.sqrt(doc1);
		double sq2 = Math.sqrt(doc2);

		cosine_similarity = (double) (vec) / (double) (sq1 * sq2);

		return cosine_similarity;
	}
	
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
	
	List<String> tokenize0(String doc) {
		List<String> res = new ArrayList<String>();

		for (String s : doc.split("\\s+"))
			res.add(s);
		return res;
	}

	public String afinn;
	
	public String sentimentwords;
	
	public List<String> alist;
	public Map<String, Integer> doc;
		
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		afinn = "src/main/resources/data/AFINN-111.txt";
		
		doc = new HashMap<String, Integer>();
				
		try {
			BufferedReader bf = new BufferedReader(new FileReader(afinn));
			
			String content = null;
			
			while((content = bf.readLine()) != null){
				String ary[] = content.trim().split("\\s+");
				
				doc.put(ary[0],Integer.parseInt(ary[1]));
			}
			
			bf.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		 Collection<Passage> snippetList = (TypeUtil.getRankedPassages(aJCas) == null) ? new ArrayList<Passage>()
		            : TypeUtil.getRankedPassages(aJCas);
			
		 int position=0;
		 double rel=0.0;
		 double sen=0.0;
			// iterate
		 int count=0;
		 
		 String x=" ";
		 
		 String t=" ";
		 
		 for (Passage a : snippetList) {
			 
			 String text=a.getText();
			 String newtext=StanfordLemmatizer.stemText(text).replaceAll("[\\p{Punct}]+", " ");
			 t+=newtext+" ";
			 
		 }
		 
		 double score=0.0;
		 double subscore=0.0;
		 
		 int positive=0;
		 int negtive=0;
		 
		 double max_score=0.0;
		 
		 for (Passage a : snippetList)  {
			 
			 	if(Double.isNaN(a.getScore())) continue;
			 	
				count++;
				//if(count>50) break;
				
				position=a.getPosition();
				rel=a.getScore();
			    System.err.println("text:"+ a.getText()+ "\n pos:"+position + "\n rel:" +rel);
				
				/****************************************************/
				/**calculate sentiment score, look in the dictionary for AFINN**/
				String text=a.getText();
				String word=a.getWordlist();
				System.err.println("word:"+ word);
				alist = new ArrayList<String>();				
				alist =tokenize0(word);

				String newtext=StanfordLemmatizer.stemText(text).replaceAll("[\\p{Punct}]+", " ");
				int i;
				String temp;
				
				docVector = new HashMap<String, Double>();
				senVector =new HashMap<String, Double>();
				
				for (i = 0; i < alist.size(); i++) {
					
					temp = alist.get(i);
					
					int co = calculate(newtext, temp);
					// System.out.println(temp + " " + count);
					int co2=calculate(t,temp);
					
					/************* tf*idf for each word****/
					double tf=(double)co/(double)tokenize0(newtext).size();
					double idf=Math.log((double)snippetList.size()/(double)co2+1);
							
					docVector.put(temp, (double)tf*idf);
					
					System.err.println("tfidf** tf:"+ (double)tf*idf + temp);
					
					/**********find sentiment score in the dictionary*****************/
					
					//Pattern p = Pattern.compile(temp);
					//Matcher matcher = p.matcher(stopwords);
					if(doc.containsKey(temp))
						{
							senVector.put(temp, (double)doc.get(temp));
							System.err.println("^^^^^^^:"+ (double)doc.get(temp) + temp);
							
						}
					else senVector.put(temp, 0.0);
											
				}
				
				sen=computeCosineSimilarity(docVector,senVector);
				
				if(Double.isNaN(sen)) sen=0.0;
				
				/********************************************************************/
				/***subscore for this sentence**/
				System.err.println("rel:"+ rel + a.getText());
				System.err.println("sen:"+ sen + a.getText());
				System.err.println("position:"+ position + a.getText());
				
				subscore=rel*sen*(double)position;
				
				System.err.println("subscore:"+ subscore + a.getText());
				
				/***final score***/
				//score+=subscore;	
				if(subscore>0) positive++;
				else if(subscore<0) negtive++;
				
				/***********************************************************************/
				if(Math.abs(subscore)>=max_score) x=text;
				
				System.err.println("&&&&&:"+ subscore + a.getText());
			}
		 //System.out.println("Yes");
		 
		 System.err.println(positive+"************************************"+negtive);
		
		 	if(positive>=negtive){
		 		Answer answer = TypeFactory.createAnswer(aJCas,"Yes," + x,new ArrayList<>(),0);
		 		
		 		answer.addToIndexes();
		 		 System.out.println("Yes."+x);
		 	}
		 	else {
		 		Answer answer = TypeFactory.createAnswer(aJCas,"No," + x,new ArrayList<>(),0);
		 		answer.addToIndexes();
		 		 System.out.println("No."+x);
		 	}
			
	}

}
