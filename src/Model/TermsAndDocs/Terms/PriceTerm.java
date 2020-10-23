package Model.TermsAndDocs.Terms;

public class PriceTerm extends Term {
    public PriceTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "PriceTerm";
    }
}
