package Model.HandleSearch.DocDataHolders;

import Model.TermsAndDocs.Terms.Term;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * this class purpose is to hold all the necessary data required from a file
 * in order to calculate it's rank (score) for a given query
 */
public class DocRankData {
    private String docNo;
    private ArrayList<Pair<Term, Integer>> queryWords;
    private ArrayList<Pair<Term, Integer>> similarWords;
    private ArrayList<Integer> queryWordsTfs;
    private ArrayList<Integer> similarWordsTfs;
    private ArrayList<Integer> queryWordsDfs;
    private ArrayList<Integer> similarWordsDfs;
    private int lengthOfDoc;
    private int numOfUniqTerms;
    private int maxTf;
    private String mostCommonTerm;


    private ArrayList<Pair<Term, Integer>> docHeaderStrings;
    private String docDate;

     public DocRankData(String docNo){
         this.docNo = docNo;
         this.docHeaderStrings = new ArrayList<>();
         this.queryWords = new ArrayList<>();
         this.queryWordsTfs = new ArrayList<>();
         this.queryWordsDfs = new ArrayList<>();
         this.similarWords = new ArrayList<>();
         this.similarWordsDfs = new ArrayList<>();
         this.similarWordsTfs = new ArrayList<>();
     }

    /**
     * this method is getting al the fields required to fill one of the query term's weight
     * in a specific doc
     * @param queryWord
     * @param tf
     * @param df
     */
     public void addQueryWordData(Pair<Term, Integer> queryWord, int tf, int df){
         this.queryWords.add(queryWord);
         this.queryWordsTfs.add(tf);
         this.queryWordsDfs.add(df);
     }

    /**
     * this method is getting al the fields required to fill one of the similar query term's weight
     * in a specific doc
     * @param similarWord
     * @param tf
     * @param df
     */
    public void addSimilarQueryWordData(Pair<Term, Integer> similarWord, int tf, int df){
        this.similarWords.add(similarWord);
        this.similarWordsTfs.add(tf);
        this.similarWordsDfs.add(df);
    }

    /**
     * setter for doc length
     * @param lengthOfDoc
     */
    public void setLengthOfDoc(int lengthOfDoc) {
        this.lengthOfDoc = lengthOfDoc;
    }

    /**
     * setter for doc's header
     * @param docHeaderStrings
     */
    public void setDocHeaderStrings(ArrayList<Pair<Term, Integer>> docHeaderStrings) {
        this.docHeaderStrings = docHeaderStrings;
    }

    //getters for all of this class fields:

    public ArrayList<Pair<Term, Integer>> getQueryWords() {
        return queryWords;
    }

    public ArrayList<Pair<Term, Integer>> getSimilarWords() {
        return similarWords;
    }

    public ArrayList<Integer> getQueryWordsTfs() {
        return queryWordsTfs;
    }

    public ArrayList<Integer> getSimilarWordsTfs() {
        return similarWordsTfs;
    }

    public ArrayList<Integer> getQueryWordsDfs() {
        return queryWordsDfs;
    }

    public ArrayList<Integer> getSimilarWordsDfs() {
        return similarWordsDfs;
    }

    public int getLengthOfDoc() {
        return lengthOfDoc;
    }

    public ArrayList<Pair<Term, Integer>> getDocHeaderStrings() {
        return docHeaderStrings;
    }

    public void setDocDate(String docDate) {
        this.docDate = docDate;
    }

    public String getDocDate() {
        return docDate;
    }

    public int getNumOfUniqTerms() {
        return numOfUniqTerms;
    }

    public String getMostCommonTerm() {
        return mostCommonTerm;
    }

    public void setMostCommonTerm(String mostCommonTerm) {
        this.mostCommonTerm = mostCommonTerm;
    }

    public void setNumOfUniqTerms(int numOfUniqTerms) {
        this.numOfUniqTerms = numOfUniqTerms;
    }

    public int getMaxTf() {
        return maxTf;
    }

    public void setMaxTf(int maxTf) {
        this.maxTf = maxTf;
    }

    public String getDocNo() {
        return docNo;
    }
}
