package Model.TermsAndDocs.Terms;

public class TermBuilder {


    public TermBuilder() {
    }

    /**
     * building term with given arguments
     * @param termType
     * @param data
     * @return
     */
    public Term buildTerm(String termType, String data) {
        Term output = null;
        switch (termType) {
            case ("DocumentDateTerm"):
                output = new DocumentDateTerm(data);
            break;
            case ("EntityTerm"):
                output = new EntityTerm(data);
            break;
            case ("CapsTerm"):
                output = new CapsTerm(data);
                break;
            case  ("ExpressionTerm"):
                output = new ExpressionTerm(data);
                break;
            case  ("RegularTerm"):
                output = new RegularTerm(data);
                break;
            case ("DateTerm"):
                output = new DateTerm(data);
                break;
            case  ("NumericTerm"):
                output = new NumericTerm(data);
                break;
            case  ("PercentageTerm"):
                output = new PercentageTerm(data);
                break;
            case  ("PriceTerm"):
                output = new PriceTerm(data);
                break;
            case ("MeasurementTerm"):
                output = new MeasurementTerm(data);
                break;
            default:
                output = null;
        }
        return output;
    }
}
