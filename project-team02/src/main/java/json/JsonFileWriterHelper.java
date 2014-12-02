package json;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import json.gson.Snippet;
import json.gson.Triple;


public class JsonFileWriterHelper {
  public static void main(String[] args) {
    JsonFileWriterHelper jsHelper = new JsonFileWriterHelper();
    //jsHelper.testRun("./output.json");
  }

  //TODO: create file, add answer form(once or multiple), close file
  public void writeAnswerToFile(String filePath, ArrayList<YesNoAnswerFormat> answerArr) {
    Writer writer = null;
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "utf-8"));
      for(int i=0; i<answerArr.size(); i++){
        writer.write(gson.toJson(answerArr.get(i)));
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        writer.close();
      } catch (Exception ex) {
      }
    }
  }  
}
