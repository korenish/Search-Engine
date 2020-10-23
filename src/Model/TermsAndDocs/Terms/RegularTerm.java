package Model.TermsAndDocs.Terms;

public class RegularTerm extends Term {
    public RegularTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "RegularTerm";
    }
}
