package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class OpenNLPTokenization {
  Tokenizer tokenizer;
  private static OpenNLPTokenization instance = null;
  String modelFile = "src/main/resources/data/en-token.bin";
  public static OpenNLPTokenization getInstance(){
    if(instance == null)
      instance = new OpenNLPTokenization();
    return instance;
  }
  OpenNLPTokenization(){
    try {
      InputStream is = new FileInputStream(modelFile);
      TokenizerModel model = new TokenizerModel(is);      
      tokenizer = new TokenizerME(model);   
      is.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (InvalidFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
  public List<String> tokenize(String str){
    LinkedList<String> tokens = new LinkedList<String>();
    for(String s : tokenizer.tokenize(str))
      tokens.add(s);
    return tokens;
  }
  public static void main(String[] args){
    OpenNLPTokenization test = OpenNLPTokenization.getInstance();
    List<String>r = test.tokenize("From a single hamburger stand in San Bernardino, Calif., in 1948, the systematized approach that the McDonald brothers developed to offer customers reasonably priced food at a rapid pace formed the cornerstone of the fast-food business.");
    for(String t: r)
      System.out.println(t);
  }
}
