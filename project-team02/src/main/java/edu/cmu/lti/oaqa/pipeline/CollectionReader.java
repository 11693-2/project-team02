package edu.cmu.lti.oaqa.pipeline;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import json.JsonCollectionReaderHelper;
import json.gson.Question;
import json.gson.TestSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import com.google.common.collect.Lists;


/**
 * This is a collection reader, which could read from the document question.json and store the question information to the question type.
 * @author leixiao
 *
 */
public class CollectionReader extends CollectionReader_ImplBase {

	public static final String PARAM_INPUTDIR = "InputDocument";
	List<Question> inputs;
	int i = 0;

	@Override
	public void initialize() throws ResourceInitializationException {

		//String filePath = PARAM_INPUTDIR;
		String filePath=((String) getConfigParameterValue(PARAM_INPUTDIR)).trim();
		
		inputs = Lists.newArrayList();
		
		Object value = filePath;
		if (String.class.isAssignableFrom(value.getClass())) {
			inputs = TestSet.load(getClass().getResourceAsStream(String.class.cast(value))).stream().collect(toList());
		} else if (String[].class.isAssignableFrom(value.getClass())) {
			inputs = Arrays.stream(String[].class.cast(value))
					.flatMap(path -> TestSet.load(getClass().getResourceAsStream(path)).stream()).collect(toList());
		}
		
		// trim question texts
		inputs.stream().filter(input -> input.getBody() != null)
				.forEach(input -> input.setBody(input.getBody().trim().replaceAll("\\s+", " ")));
		

	}

	public void getNext(CAS aCAS) throws IOException, CollectionException {
		
		/**
		 *get the question and store it in the question type 
		 */
		
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}
		
	    Question question = inputs.get(i);
	    //System.out.println("I am getting question!!!!!!!!");
	    System.out.println(i+" "+ inputs.get(i).getBody());
		System.out.println(i+" "+ inputs.get(i).getType());
		if(inputs.get(i).getType().equals("YES_NO")){
			JsonCollectionReaderHelper.addQuestionToIndex(question," ",jcas);
			i++;
		}
		

	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return inputs.size() - i > 0;
	}

	@Override
	public Progress[] getProgress() {
		 return new Progress[] { new ProgressImpl(i, inputs.size(), Progress.ENTITIES) };
	}

	@Override
	public void close() throws IOException {

	}

}
