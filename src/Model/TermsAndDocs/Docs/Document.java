package Model.TermsAndDocs.Docs;

public class Document {

    private String date;
    private String header;
    private String docNo;
    private String text;
    //private ArrayList<Term> terms;

    //minimum knowledge to create doc
    public Document(String docNo, String text) {
        //terms = new ArrayList<>();
        this.docNo = docNo;
        this.text = text;
    }

    //full knowledge about doc
    public Document(String docNo, String text, String date, String header) {
        //terms = new ArrayList<>();
        this.docNo = docNo;
        this.text = text;
        this.header = header;
        this.date = date;
    }

    /**
     * delets document's text
     */
    public void deleteText()
    {
        this.text=null;
    }

    /**
     * getter for docno
     * @return
     */
    public String getDocNo() {
        return docNo;
    }

    /**
     * getter for doc text
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * getter for doc date
     * @return
     */
    public String getDate() {
        return date;
    }

    /**
     * getter for doc header
     * @return
     */
    public String getHeader() {
        return header;
    }

   // public void setTerms(ArrayList<Term> terms) {
   //     this.terms = terms;
   // }
}//
