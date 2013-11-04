package edu.cmu.lti.f13.hw4.hw4_abhimank.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_abhimank.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_abhimank.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_abhimank.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	public ArrayList<HashMap<String, Integer>> stringFreqList;
	public ArrayList<Integer> rankArray;

		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		stringFreqList = new ArrayList<HashMap<String,Integer>>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();

			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			//ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			//Do something useful here
			HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
			for(Token token : tokenList){
			  String word = token.getText();
			  int freq = token.getFrequency();
			  freqMap.put(word, freq);
			}
			stringFreqList.add(freqMap);
		}
	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);
		rankArray = new ArrayList<Integer>();
		// TODO :: compute the cosine similarity measure
		for(int i=0; i<qIdList.size(); i++){
		  if(relList.get(i)!=99)
		    continue;
		  int qId = qIdList.get(i);
		  HashMap<String, Integer> queryMap= stringFreqList.get(i);
		  ArrayList<DocScore> arrayScore = new ArrayList<DocScore>();
		  for(int j=0; j<qIdList.size(); j++){
		    if(relList.get(j)==99)
		      continue;
		    if(qIdList.get(j)==qId){
		      HashMap<String, Integer> docMap = stringFreqList.get(j);
		      double score = computeCosineSimilarity(queryMap, docMap);
		      DocScore docScore = new DocScore(score, qId, relList.get(j));
		      arrayScore.add(docScore);
		    }
		  }
		  int rank = computeRank(arrayScore);
		  rankArray.add(rank);
		}
		
		
		// TODO :: compute the rank of retrieved sentences
		
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	private int computeRank(ArrayList<DocScore> arrayScore){
	  Collections.sort(arrayScore, new DocComparator());
	  int rank =0;
	  for(DocScore docScore : arrayScore){
	    rank++;
	    if(docScore.relevance==1){
	      System.out.println("Document: qid, relevance " + docScore.qId + ", "+ 
	              docScore.relevance);
	      return rank;
	    }
	  }
	  return arrayScore.size();
	}
	
	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;

		// TODO :: compute cosine similarity between two sentences
		int squareSumQ = 0;
		for(String word : queryVector.keySet()){
		  int freq = queryVector.get(word);
		  squareSumQ+=(freq*freq);
		}
		int squareSumD = 0;
		for(String word: docVector.keySet()){
		  int freq = docVector.get(word);
		  squareSumD+=(freq*freq);
		}
    for(String wordQ : queryVector.keySet()){
      for(String wordD: docVector.keySet()){
        if(wordQ.matches(wordD))
          cosine_similarity +=
          (queryVector.get(wordQ)*docVector.get(wordD)*1.0/Math.sqrt(squareSumD*squareSumQ*1.0));
      }
    }
		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for(int rank : rankArray){
		  System.out.println("rank : " + rank);
		  metric_mrr += 1.0/rank;
		}
		metric_mrr = metric_mrr/rankArray.size();
		return metric_mrr;
	}
	
	class DocScore{
	  public double score;
	  public int qId;
	  public int relevance;
	  public String text;
	  public DocScore(double score, int qId, int relevance) {
	    this.score = score;
	    this.qId = qId;
	    this.relevance = relevance;
	  }
	}
	
	private class DocComparator implements Comparator<DocScore> {
    public int compare(DocScore o1, DocScore o2) {
            if (o1.score <= o2.score)
                    return 1;
            else
                    return -1;

    }
	}
	
}
