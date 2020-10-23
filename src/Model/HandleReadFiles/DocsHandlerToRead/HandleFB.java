package Model.HandleReadFiles.DocsHandlerToRead;

/**
 * this class handle info about doc of FB doc's type
 */
public class HandleFB implements HandleDoc {

    /**
     *
     * @param listLines
     * @param lineIndex
     * @return
     */
    @Override
    public String[] handle(String[] listLines, int lineIndex) {
        String[] docInfo = new String[4];
        //returns good docno and updated line index
        String[] temp = infoFinder(listLines, lineIndex, "<DOCNO>");
        lineIndex = Integer.parseInt(temp[1]);
        docInfo[0] = temp[0];

        //returns good date and updated line index
        temp = infoFinder(listLines, lineIndex, "<DATE1>");
        lineIndex = Integer.parseInt(temp[1]);
        docInfo[1] = temp[0];

        //returns good title and updated line index
        temp = infoFinder(listLines, lineIndex, "<H3>");
        temp[0] = temp[0].replace("<TI>", "");
        temp[0] = temp[0].replace("</TI>", "");
        temp[0] = spacesKiller(temp[0]);
        docInfo[2] = temp[0];

        //System.out.println("FB: " + docInfo[1]);

        lineIndex = Integer.parseInt(temp[1]);
        docInfo[3] = "" + lineIndex;
        return docInfo;
    }
}
