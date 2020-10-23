package Model.TermsAndDocs;

public class TermCounterPair {

    String termStr;
    Integer Count;

    public TermCounterPair(String termStr, Integer count) {
        this.termStr = termStr;
        Count = count;
    }

    /**
     * getter for termStr
     * @return
     */
    public String getTermStr() {
        return termStr;
    }

    /**
     * setter for termstr
     * @param termStr
     */
    public void setTermStr(String termStr) {
        this.termStr = termStr;
    }

    /**
     * getter for count
     * @return
     */
    public Integer getCount() {
        return Count;
    }

    /**
     * setter for ount
     * @param count
     */
    public void setCount(Integer count) {
        Count = count;
    }
}
