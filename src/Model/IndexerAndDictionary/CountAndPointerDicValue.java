package Model.IndexerAndDictionary;

public class CountAndPointerDicValue {

    Integer totalCount;
    PostingFilePointer pointer;

    public CountAndPointerDicValue() {
        this.pointer = new PostingFilePointer();
        this.totalCount=0;
    }

    /**
     * increases counter by given value
     * @param toAdd
     */
    public void increaseCount(int toAdd)
    {
        totalCount=totalCount+toAdd;
    }

    /**
     * getter fo totalCount
     * @return
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * setter for totalCount
     * @return
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * getter for pointer
     * @return
     */
    public PostingFilePointer getPointer() {
        return pointer;
    }

    /**
     * setter for pointer
     * @param pointer
     */
    public void setPointer(PostingFilePointer pointer) {
        this.pointer = pointer;
    }

    @Override
    public String toString() {
        return totalCount + ";" + pointer.getFileStrName();
    }
}
