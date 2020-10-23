package Model.TermsAndDocs.Terms;

public class DateTerm extends Term {
    public DateTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "DateTerm";
    }
}
