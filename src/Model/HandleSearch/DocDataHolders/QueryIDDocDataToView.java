package Model.HandleSearch.DocDataHolders;

public class QueryIDDocDataToView {

    private String queryID;
    private String docNo;
    private String date;
    private String entities;



    public QueryIDDocDataToView(String queryID, String docNo, String date, String entities) {
        this.queryID = queryID;
        this.docNo = docNo;
        this.date = date;
        this.entities = entities;
    }

    public String getQueryID() {
        return queryID;
    }

    public String getDocNo() {
        return docNo;
    }

    public String getDate() {
        return date;
    }

    public String getEntities() {
        return entities;
    }
}
