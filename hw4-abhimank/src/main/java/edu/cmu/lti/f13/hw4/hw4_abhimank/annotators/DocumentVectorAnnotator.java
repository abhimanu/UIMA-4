package edu.cmu.lti.f13.hw4.hw4_abhimank.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_abhimank.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_abhimank.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_abhimank.utils.Utils;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		String[] stringWord = docText.split("\\s+");
		HashMap<String, Integer> stringMap = new HashMap<String, Integer>();
		for(String word : stringWord){
		  if(stringMap.containsKey(word))
		    stringMap.put(word, stringMap.get(word)+1);
		  else
		    stringMap.put(word, 1);
		}
		ArrayList<Token> arrayList = new ArrayList<Token>();
		for(String word : stringMap.keySet()){
		  Token token  = new Token(jcas);
		  token.setFrequency(stringMap.get(word));
		  token.setText(word);
		  token.addToIndexes();
		  arrayList.add(token);
		}
		FSList  fslist=Utils.fromCollectionToFSList(jcas, arrayList);
		doc.setTokenList(fslist);
		//done test
		//TO DO: construct a vector of tokens and update the tokenList in CAS
		

	}

}
