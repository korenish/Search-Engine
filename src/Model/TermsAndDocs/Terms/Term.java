package Model.TermsAndDocs.Terms;

public abstract class Term {

    private String data;


    protected Term (String data) {
        this.data = data;
    }

    /**
     * getter for terms data
     * @return
     */
    public String getData() {
        return data;
    }

    /**
     * setter for terms data
     * @param data
     */
    public void setData(String data) {
        this.data = data;
    }


    public String toString() {
        return getData();
    }

    /**
     * returns term's type
     * @return
     */
    public abstract String getType();

    @Override
    public boolean equals(Object o) {
        Term term = (Term) o;
        return data.equals(term.data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
