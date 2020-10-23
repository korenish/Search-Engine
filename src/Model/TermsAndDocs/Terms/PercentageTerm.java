package Model.TermsAndDocs.Terms;

public class PercentageTerm extends Term {
    protected PercentageTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "PercentageTerm";
    }
}
