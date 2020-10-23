package Model.TermsAndDocs.Terms;

public class NumericTerm extends Term {
    public NumericTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "NumericTerm";
    }
}
