package edu.cmu.lti.oaqa.type.input;


/* First created by JCasGen Fri Nov 14 00:06:10 EST 2014 */

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Nov 14 00:06:10 EST 2014
 * XML source: /Users/apple/Documents/project-team02/project-team02/src/main/resources/type/OAQATypes.xml
 * @generated */
public class Keyword extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Keyword.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Keyword() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Keyword(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Keyword(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Keyword(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets 
   * @generated
   * @return value of the feature 
   */
  public String getId() {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "Keyword");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "Keyword");
    jcasType.ll_cas.ll_setStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: source

  /** getter for source - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSource() {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "Keyword");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_source);}
    
  /** setter for source - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSource(String v) {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "Keyword");
    jcasType.ll_cas.ll_setStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_source, v);}    
   
    
  //*--------------*
  //* Feature: questionType

  /** getter for questionType - gets 
   * @generated
   * @return value of the feature 
   */
  public String getQuestionType() {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_questionType == null)
      jcasType.jcas.throwFeatMissing("questionType", "Keyword");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_questionType);}
    
  /** setter for questionType - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setQuestionType(String v) {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_questionType == null)
      jcasType.jcas.throwFeatMissing("questionType", "Keyword");
    jcasType.ll_cas.ll_setStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_questionType, v);}    
   
    
  //*--------------*
  //* Feature: text

  /** getter for text - gets 
   * @generated
   * @return value of the feature 
   */
  public String getText() {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "Keyword");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_text);}
    
  /** setter for text - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setText(String v) {
    if (Keyword_Type.featOkTst && ((Keyword_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "Keyword");
    jcasType.ll_cas.ll_setStringValue(addr, ((Keyword_Type)jcasType).casFeatCode_text, v);}    
  }

    