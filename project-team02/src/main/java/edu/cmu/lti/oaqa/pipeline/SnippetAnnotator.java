package edu.cmu.lti.oaqa.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import util.StanfordLemmatizer;
import util.TypeFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.cmu.lti.oaqa.type.answer.CandidateAnswerVariant;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;

public class SnippetAnnotator extends JCasAnnotator_ImplBase {

	private static final String prefix = "http://metal.lti.cs.cmu.edu:30002/pmc/";

	private static final CloseableHttpClient HTTP = HttpClients.createDefault();

	public ArrayList<String> wordList;
	public List<String> querylist;
	public List<String> sentencelist;
	public List<String> alist;
	public Map<String, Integer> queryVector;
	public Map<String, Integer> docVector;
	

	public String stopwords;

	/**
	 * A basic white-space tokenizer, it deliberately does not split on
	 * punctuation!
	 *
	 * @param doc
	 *            input text
	 * @return a list of tokens.
	 */

	/** calculate the number of each word in a sentence **/

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
	
	List<String> tokenize0(String doc) {
		List<String> res = new ArrayList<String>();

		for (String s : doc.split("\\s+"))
			res.add(s);
		return res;
	}

	List<String> tokenize_sentence(String doc) {
		List<String> res = new ArrayList<String>();
		//doc.replace("?", ".");
		//doc.replace("!", ".");
		doc.replace(" ", "#");
		System.err.println(doc);
		int a=calculate(doc.toString(),".");
		System.err.print(a);
		//System.err.print(doc.indexOf("."));
		/*for (String s : doc.split(".")){
			res.add(s);
			System.err.print(s);			
		}*/
		return res;
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


	/**
	 * @param queryVector
	 * @param docVector
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector, Map<String, Integer> docVector) {
		double cosine_similarity = 0.0;

		int vec = 0;

		Map<String, Integer> temp = new HashMap<String, Integer>();

		// TODO :: compute cosine similarity between two sentences

		temp.putAll(queryVector);
		temp.putAll(docVector);

		/**
		 * combine the two map together to form a new map which contains the key
		 * in query or doc
		 **/
		for (Map.Entry<String, Integer> entry : temp.entrySet()) {
			String a = entry.getKey();

			/** if the key is both in query and document, we will calculate **/
			if (queryVector.containsKey(a) && docVector.containsKey(a)) {
				vec += queryVector.get(a) * docVector.get(a);
			}

		}

		double doc1 = 0.0;
		double doc2 = 0.0;

		/** using the cosine similarity formula to calculate the score **/

		for (Map.Entry<String, Integer> entry : queryVector.entrySet()) {
			doc1 += Math.pow(entry.getValue(), 2);
		}
		for (Map.Entry<String, Integer> entry : docVector.entrySet()) {
			doc2 += Math.pow(entry.getValue(), 2);
		}

		double sq1 = Math.sqrt(doc1);
		double sq2 = Math.sqrt(doc2);

		cosine_similarity = (double) (vec) / (double) (sq1 * sq2);

		return cosine_similarity;
	}

	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		stopwords = readToString("src/main/resources/data/stopwords.txt");

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		String whole;
		String row;

		FSIterator<TOP> iter = aJCas.getJFSIndexRepository().getAllIndexedFS(Document.type);

		while (iter.hasNext()) {

			

			/**
			 * get the document docID to add them into the prefix and then to go
			 * to the website
			 **/

			Document doc = (Document) iter.next();

			System.err.println(doc.getDocId());
			System.err.println(doc.getQueryString());

			/*********************************************************************************/
			/**
			 * get the query and then to create the queryVector to get the
			 * cosine similarity
			 **/

			String query = doc.getQueryString();

			System.err.println(query);

			querylist = new ArrayList<String>();

			querylist = tokenize0(query);

			wordList = new ArrayList<String>();

			/************************************************************/
			/** queryVetcor **/
			int i;
			String temp;

			queryVector = new HashMap<String, Integer>();

			for (i = 0; i < querylist.size(); i++) {
				temp = querylist.get(i);
				if ((wordList.indexOf(temp) != -1))
					continue;
				wordList.add(temp);
				int count = calculate(query, temp);
				// System.out.println(temp + " " + count);
				queryVector.put(temp, count);

			}
			/***********************************************/
			// get the article from the url
			String url = prefix + doc.getDocId();

			HttpGet http_name = new HttpGet(url);

			try (CloseableHttpResponse response = HTTP.execute(http_name)) {

				if (response == null)
					continue;

				HttpEntity entity = response.getEntity();

				if (entity == null)
					continue;

				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

				/**********************************************************************/
				/*** read the article by line **/

				whole = "";
				row = "";
				while ((row = reader.readLine()) != null) {
					whole += row;
				}

				System.out.println("article:" + whole);

				JsonElement jsonEle = new JsonParser().parse(whole);
				JsonObject jsonObj = jsonEle.getAsJsonObject();
				
				JsonArray a = jsonObj.getAsJsonArray("sections");
				String section = a.get(0).getAsString().replace(".", "#");
				
				System.err.println("ss***:" + section);

				/*****************************************************************/
				/** get the sentence from the article **/
				
				sentencelist = new ArrayList<String>();
				section.replace("?", "#");
				section.replace("!", "#");

				for (String s : section.split("#")){
					sentencelist.add(s);
					System.out.println(s);			
				}				
				System.out.println("&&&&&&&&:" + sentencelist.size());
				
				/*****************************************************************/
				/**
				 * calculate the cosine similarity of each sentence and find the
				 * one sentence with the maximum score and store it to the
				 * passage type
				 **/
				/*****************/
				/** get the docVector for each sentence **/
				double max=0.0;
				String max_text = sentencelist.get(0);
				
				for (String s : sentencelist) {
					System.err.println(s);

					docVector = new HashMap<String, Integer>();
					wordList = new ArrayList<String>();
					alist = new ArrayList<String>();// store the word in each
													// document

					String b = s.replaceAll("[\\p{Punct}]+", " ");
					String c = StanfordLemmatizer.stemText(b);
					alist = tokenize0(c);

					for (i = 0; i < alist.size(); i++) {
						temp = alist.get(i);
						Pattern p = Pattern.compile(temp);
						Matcher matcher = p.matcher(stopwords);

						/** delete the same token or stopwords **/
						if ((wordList.indexOf(temp) != -1) || matcher.find())
							continue;

						wordList.add(temp);
						int count = calculate(c, temp);
						// System.out.println(temp + " " + count);
						docVector.put(temp, count);
					}

					/***** calculate the cosine similarity score for each sentence ***/
					double score = computeCosineSimilarity(queryVector, docVector);
					
					System.out.println(s + " " + score);
					if(score>max){
						max=score;
						max_text=s;
					}
				}
				/********************/
				/**rank the sentence by score and find the maximum to store in the passage type**/
				
				System.out.println("*************"+ max + " " + max_text);
				
		        int begin = section.indexOf(max_text);
		        int end = begin+max_text.length() + 1;
		        
		        Passage passage = TypeFactory.createPassage(aJCas, url, doc.getScore(), max_text, doc.getRank(), query, "", new ArrayList<CandidateAnswerVariant>(), doc.getTitle(), doc.getDocId(), begin, end, "sections.0", "sections.0", "");
		        
		        passage.addToIndexes();
								

			} catch (ClientProtocolException e) {
			
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}

		}
	}

}
