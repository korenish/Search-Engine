package Model.TermsAndDocs.Terms;

public class MeasurementTerm extends Term {
    public MeasurementTerm(String data) {
        super(data);
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "MeasurementTerm";
    }
}
