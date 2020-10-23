package Model.TermsAndDocs.Pairs;

import Model.TermsAndDocs.Docs.Document;
import Model.TermsAndDocs.Terms.Term;

public class TermDocPair {

    private Term term;
    public Document doc;
    private int counter;

    /**
     * constructor
     * @param term
     * @param doc
     */
    public TermDocPair(Term term, Document doc) {
        this.term = term;
        this.doc = doc;
        this.counter = 1;
    }

    /**
     * increase counter by one
     */
    public void incrementCounter() {
        this.counter++;
    }

    /**
     * getter for term field
     * @return
     */
    public Term getTerm() {
        return term;
    }

    /**
     * getter for counter field
     * @return
     */
    public int getCounter() {
        return counter;
    }

    /**
     * setter for counter
     * @param counter
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    /**
     * setter for term
     * @param term
     */
    public void setTerm(Term term) {
        this.term = term;
    }

    /**
     * getter for doc field
     * @return
     */
    public Document getDoc() {
        Document docTemp = this.doc;
        return docTemp;
    }

    /**
     * setter for doc field
     * @param doc
     */
    public void setDoc(Document doc) {
        this.doc = doc;
    }

    /**
     * get string representation for pair
     * @return
     */
    public String toString() {
        return "term:" + term.toString() + " doc:" + doc.getDocNo() +"\n";
    }
}
