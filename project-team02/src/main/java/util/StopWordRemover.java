package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class StopWordRemover {
  static HashSet<String> stopWordsDict;
  //File storeFile = ;
  private static StopWordRemover instance = null;
  private StopWordRemover(){
    stopWordsDict = new HashSet<String>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("src/main/resources/data/stopwords.txt"));
      String line =reader.readLine();
      while((line=reader.readLine())!=null){
        stopWordsDict.add(line);
      }
      reader.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static StopWordRemover getInstance(){
    return new StopWordRemover();
  }
  public String removeStopWords (String sentence){
	  List<String> tokenList =  new ArrayList<String>();
	  Collections.addAll(tokenList,sentence.split(" ")); 
	  for (String token: tokenList){
		 // System.out.println(token);
		  /*
		  if (StopWordRemover.checkExistance(token)){
			  tokenList.remove(token);
		  }
		  */
	  }
	  return tokenList.toString();
  }
  public boolean checkExistance(String token){
     return stopWordsDict.contains(token);
  }
}
