package Model.TermsAndDocs.Terms;

public class CapsTerm extends Term{


    public CapsTerm(String data) {
        super(data);
    }

    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "CapsTerm";
    }
}
