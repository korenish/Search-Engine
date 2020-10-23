package Model.TermsAndDocs.Terms;

public class EntityTerm extends Term  {
    private boolean isEntity;
    public EntityTerm(String data) {
        super(data);
        isEntity = false;
    }
    /**
     * returns term's type
     * @return
     */
    @Override
    public String getType() {
        return "EntityTerm";
    }

    /**
     * make isEntity true
     */
    public void setToEntity() {
        isEntity = true;
    }

    /**
     * get isEntity
     * @return isEntity
     */
    public boolean isEntity() {
        return isEntity;
    }
}