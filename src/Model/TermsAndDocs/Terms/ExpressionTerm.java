package Model.TermsAndDocs.Terms;

public class ExpressionTerm extends Term {
    public ExpressionTerm(String data) {
        super(data);
    }

    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "ExpressionTerm";
    }
}
