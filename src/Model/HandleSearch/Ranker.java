package Model.HandleSearch;

import Model.HandleSearch.DocDataHolders.DocRankData;
import Model.TermsAndDocs.Terms.Term;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * This class is responsible for ranking documents with respect to a query
 */
public class Ranker {

    /**
     * Field mentioning if we should take into account the result of the semantic connection
     * between the query and the document. Probably will be decided by the user.
     * If it is false, only BM25 will be taken into account.
     */
    private boolean isSemantic;


    /**
     * The percentage given to the ranking functions on the original query words when calculating the rank in case of taking into account
     * the semantically close words.
     * It is slitted by two for the BM25 an the TFIDF ranking methods.
     * 1 minus this value is the percentage given to the semantic similarities words.
     */
    //0.6
    private double weightOfOriginalQuery = 0.75;

    /**
     * The percentage given to the bm25 functions when scoring with the different parameters
     * The reat goes to cos similarity, is the word in the header, etc...
     */
    private final double weightOfBM25 = 0.8;


    /**
     * k1 parameter for bm25
     */
    //2
    //1.2 134
    private double k1 = 1.2;

    /**
     * b parameter for bm25
     */
    //0.8
    //0.865 134
    private double b = 0.865;

    /**
     * This is the number of documents in the corpus
     */
    public final int numOfDocs = 472522;

    /**
     * This is the avg doc length
     */
    public final int avgDocLength = 250;

    /**
     * @param isSemantic field mentioning if we should take into account the result of the semantic connection
     */
    public Ranker(boolean isSemantic, boolean isStemm) {
        if (isStemm == false){
            b = 0.75;
            weightOfOriginalQuery = 0.8;
            k1 = 1.5;
        }
        this.isSemantic = isSemantic;
    }

    /**
     * computes the final ranking of the document, by calculating BM25 and TfIdf ranking of the
     * original query, and if {@code isSemantic} is true with the semantic close words also.
     * @return ranking
     */
    public double rankDocument(DocRankData docRankData) {
        double output;
        double bM25ofQuery = getBM25Rank(docRankData.getQueryWords(), docRankData.getQueryWordsTfs(), docRankData.getQueryWordsDfs(), docRankData.getLengthOfDoc(), docRankData.getNumOfUniqTerms());
        double termsInHeaderScoreQuery = getTermsInHeaderScore(docRankData.getQueryWords(), docRankData.getDocHeaderStrings());
        double cosSimRankQuery = getCosSimRank(docRankData.getQueryWords(), docRankData.getQueryWordsTfs(), docRankData.getQueryWordsDfs());
        double queryScore = weightOfBM25 * bM25ofQuery + 0.05 * termsInHeaderScoreQuery + (1 - 0.05 - weightOfBM25) * cosSimRankQuery;
        if (!isSemantic) {
            output = queryScore;
        } else { //with semantics
            double bM25OfSemantic = getBM25Rank(docRankData.getSimilarWords(), docRankData.getSimilarWordsTfs(), docRankData.getSimilarWordsDfs(), docRankData.getLengthOfDoc(), docRankData.getNumOfUniqTerms());
            double termsInHeaderScoreSimilar = 0.05 * getTermsInHeaderScore(docRankData.getSimilarWords(), docRankData.getDocHeaderStrings());
            double cossimSimilar = getCosSimRank(docRankData.getSimilarWords(), docRankData.getSimilarWordsTfs(), docRankData.getSimilarWordsDfs());

            output = weightOfOriginalQuery * queryScore
                    + (1 - weightOfOriginalQuery) * (weightOfBM25 * bM25OfSemantic + 0.05 * termsInHeaderScoreSimilar + (1 - 0.05 - weightOfBM25) * cossimSimilar);
        }
        if(QueryContainsMaxTerm(docRankData))
            output += 0.1 * output;
        return output;
    }

    private boolean QueryContainsMaxTerm(DocRankData docRankData) {
        ArrayList<Pair<Term, Integer>> queryWords = docRankData.getQueryWords();
        String mostCommonTerm = docRankData.getMostCommonTerm();
        for (Pair<Term, Integer> pair : queryWords){
            if(pair.getKey().getData().contains(mostCommonTerm)){
                return true;
            }
            if(mostCommonTerm.contains(pair.getKey().getData()));
                return true;
        }
        ArrayList<Pair<Term, Integer>> similarWords = docRankData.getSimilarWords();
        for (Pair<Term, Integer> pair : similarWords){
            if(pair.getKey().getData().contains(mostCommonTerm)){
                return true;
            }
            if(mostCommonTerm.contains(pair.getKey().getData()));
            return true;
        }
        return false;
    }

    private double getCosSimRank(ArrayList<Pair<Term, Integer>> termsCounter, ArrayList<Integer> tfs, ArrayList<Integer> dfs) {

        double[] queryVector = new double[tfs.size()];
        for (int i = 0; i < queryVector.length; i++) {
            queryVector[i] = termsCounter.get(i).getValue();
        }

        double[] docVector = new double[tfs.size()];
        for (int i = 0; i < docVector.length; i++) {
            docVector[i] = tfs.get(i) * getIdf(dfs.get(i));
        }
        if (queryVector.length==0 || docVector.length==0) {
            return 0;
        }
        return cosineSimilarity(queryVector, docVector);
    }

    private double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        double scoreCOS = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return scoreCOS;
    }

    private double getBM25Rank(ArrayList<Pair<Term, Integer>> termAndCounter, ArrayList<Integer> tfs, ArrayList<Integer> dfs, int lengthOfDoc, int numOfUnique) {
        double output = 0;
        for (int i = 0; i < tfs.size(); i++) {
            output += termAndCounter.get(i).getValue() * getBM25ForOneTerm(tfs.get(i), dfs.get(i), lengthOfDoc, numOfUnique);
        }
        return output;
    }

    private double getBM25ForOneTerm(Integer tf, Integer df, int lengthOfDoc, int numOfUnique) {
        double numerator = (tf) * (k1 + 1);
        double denominatorFraction1 = (double)lengthOfDoc / (double)avgDocLength;
        double denominatorFraction2 = (double)numOfUnique / (double)avgDocLength;
        double denominator1 = (tf + k1 * (1 - b + b * (denominatorFraction1)));
        double denominator2 = (tf + k1 * (1 - b + b * (denominatorFraction2)));
        double fraction1 = numerator/denominator1;
        double fraction2 = numerator/denominator2;
        double result1 = getIdf(df) * fraction1;
        double result2 = getIdf(df) * fraction2;
        double result = result1 * 0.8 + result2 * 0.2;
        return result;
    }

    /**
     * @param term
     * @param docHeaderStrings
     * @return num of times (terms) header contains version of the term supllied, else false
     */
    private int isTermInHeader(Term term, ArrayList<Pair<Term, Integer>> docHeaderStrings) {
        String termData = term.getData();
        if(termData.charAt(0) >= 'A' && termData.charAt(0) <= 'Z'){
            termData = termData.toLowerCase();
        }
        for(Pair<Term, Integer> termPairCurrent : docHeaderStrings){
            if(termPairCurrent.getKey().getData().contains(termData))
                return termPairCurrent.getValue();
        }
        return 0;
    }

    /**
     * returns the percentage of the words from the query that are in the documents header
     *
     * @param terms
     * @param docHeaderStrings
     * @return the percentage of the words from the query that are in the documents header
     */
    private double getTermsInHeaderScore(ArrayList<Pair<Term, Integer>> terms, ArrayList<Pair<Term, Integer>> docHeaderStrings) {
        int counter = 0;
        if (terms.size()==0)
            return 0;
        for (Pair<Term, Integer> pairTerm : terms) {
            counter +=  pairTerm.getValue() * isTermInHeader(pairTerm.getKey(), docHeaderStrings); //this will give us the number of terms from the list which are in the header
        }
        double score = ((double) counter) / ((double) terms.size());
        return score;
    }

    /**
     * @param df
     * @return idf of given df, based on {@code numOfDocs} field
     */
    private double getIdf(int df) {
        //return ((Math.log(numOfDocs/df)) / Math.log(2));
        double idf=(Math.log10((numOfDocs-df+0.5) / ((df+0.5)))) / Math.log10(2);
        return idf;
    }


}
