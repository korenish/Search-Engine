package Model.TermsAndDocs.Terms;

public class DocumentDateTerm extends Term{

    public DocumentDateTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "DocumentDateTerm";
    }
}
