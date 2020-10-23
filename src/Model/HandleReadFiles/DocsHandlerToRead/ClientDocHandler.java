package Model.HandleReadFiles.DocsHandlerToRead;

public class ClientDocHandler {
    private HandleDoc handleDoc;

    /**
     * chooses what doc to create according to input
     * @param docType
     */
    public ClientDocHandler(String docType) {
        if (docType.contains("FB")) {
            handleDoc = new HandleFB();
        } else if (docType.contains("FT")) {
            handleDoc = new HandleFT();
        } else {
            handleDoc = new HandleLA();
        }
    }

    /**
     * handles doc
     * @param listLines
     * @param lineIndex
     * @return
     */
    public String [] handleDocForClient(String [] listLines, int lineIndex)
    {
        return handleDoc.handle(listLines,lineIndex);
    }
}
