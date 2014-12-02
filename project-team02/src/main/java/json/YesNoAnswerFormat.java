package json;

import java.util.ArrayList;
import java.util.Collection;

import json.gson.Snippet;
import json.gson.Triple;

public class YesNoAnswerFormat{
  private String body;
  Collection<String> concepts;
  Collection<String> documents;
  String exact_answer;
  String id;
  String ideal_answer;
  Collection<Snippet> snippets;
  Collection<Triple> triples;
  String type;
  
  public YesNoAnswerFormat(){
    concepts = new ArrayList<String>();
    documents = new ArrayList<String>();
    snippets = new ArrayList<Snippet>();
    triples = new ArrayList<Triple>();
    type = "yesno";
  }

  public Collection<String> getConcepts() {
    return concepts;
  }

  public void setConcepts(Collection<String> concepts) {
    this.concepts = concepts;
  }

  public Collection<String> getDocuments() {
    return documents;
  }

  public void setDocuments(Collection<String> documents) {
    this.documents = documents;
  }

  public String getExact_answer() {
    return exact_answer;
  }

  public void setExact_answer(String exact_answer) {
    this.exact_answer = exact_answer;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIdeal_answer() {
    return ideal_answer;
  }

  public void setIdeal_answer(String ideal_answer) {
    this.ideal_answer = ideal_answer;
  }

  public Collection<Snippet> getSnippets() {
    return snippets;
  }

  public void setSnippets(Collection<Snippet> snippets) {
    this.snippets = snippets;
  }

  public Collection<Triple> getTriples() {
    return triples;
  }

  public void setTriples(Collection<Triple> triples) {
    this.triples = triples;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}