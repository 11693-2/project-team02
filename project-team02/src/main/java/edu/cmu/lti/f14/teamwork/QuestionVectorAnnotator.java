package edu.cmu.lti.f14.teamwork;

import java.util.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.nlp.Parse;
import edu.cmu.lti.oaqa.type.nlp.Token;


public class QuestionVectorAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
    if (iter.isValid()) {
      iter.moveToNext();
      Question doc = (Question) iter.get();
      createTermFreqVector(jcas, doc);
    }
  }

  /**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   * 
   * @param doc
   *          input text
   * @return a list of tokens.
   */

  List<String> tokenize0(String doc) {
    List<String> res = new ArrayList<String>();

    for (String s : doc.split("\\s+")) {
      res.add(s);
      // System.out.println(s);
    }
    return res;
  }

  /**
   * A optimized tokenizer separates words by following separators:
   * _(space) ; . , ? ! () [] {} <> ""
   * 
   * @param doc
   *          input text
   * @return a list of tokens.
   */
  List<String> tokenize1(String doc) {
    List<String> res = new ArrayList<String>();
    // changing every word to lowercase to avoid mismatch
    doc = doc.toLowerCase().trim();
    // spreators
    String patternStr = "[\\s\\;\\(\\)\\[\\]\\{\\}\\<\\>\".,?!]+";
    for (String s : doc.split(patternStr)) {
      String tokenStr = StanfordLemmatizer.stemWord(s);
      if (tokenStr.length() - 1 > 0 && tokenStr.charAt(tokenStr.length() - 1) == '\'') {
        tokenStr = (String) tokenStr.subSequence(0, tokenStr.length() - 1);
      }
      res.add(tokenStr);
      //System.out.println(s + "," + tokenStr);
    }
    return res;
  }

  /**
   * This method break sentences into tokens, and append tokenlist to CAS.
   * @param jcas
   * @param q
   */

  private void createTermFreqVector(JCas jcas, Question q) {
    String questionText = q.getText();
    ArrayList<String> tokens = (ArrayList<String>) tokenize1(questionText);
    HashMap<String, Integer> freq_dict = new HashMap<String, Integer>();
    Iterator<String> iter = tokens.iterator();
    //firstly calculate frequency
    while (iter.hasNext()) {
      String s = iter.next();
      freq_dict.put(s, freq_dict.get(s) == null ? 1 : freq_dict.get(s) + 1);
    }
    // insert data to tokenlist
    Parse tokenParse = new Parse(jcas);
    iter = tokens.iterator();
    ArrayList<Token> tokenList = new ArrayList<Token>();
    while (iter.hasNext()) {
      String s = iter.next();
      Token t = new Token(jcas);
      t.setLemmaForm(s);
      t.setFrequency(freq_dict.get(s));
      // System.out.println("str:" + s + " freq:" + freq_dict.get(s));
      tokenList.add(t);
    }
    // add tokenlist to cas
    FSList fslist = Utils.fromCollectionToFSList(jcas, tokenList);
    tokenParse.setTokens(fslist);
    tokenParse.addToIndexes(jcas);
  }
}
