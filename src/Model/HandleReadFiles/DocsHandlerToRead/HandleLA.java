package Model.HandleReadFiles.DocsHandlerToRead;

/**
 * this class handle info about doc of LA doc's type
 */
public class HandleLA implements HandleDoc {

    @Override
    public String[] handle(String[] listLines, int lineIndex) {
        String[] docInfo = new String[4];

        //returns good docno and updated line index
        String[] temp = infoFinder(listLines, lineIndex, "<DOCNO>");
        lineIndex = Integer.parseInt(temp[1]);
        docInfo[0] = temp[0];

        //returns date and updated line index
        temp = infoFinder(listLines, lineIndex, "<DATE>");
        lineIndex = Integer.parseInt(temp[1]);
        docInfo[1] = getFixedDate(temp[0]);//fix date

        //returns title and updated line index
        temp = infoFinder(listLines, lineIndex, "<HEADLINE>");
        docInfo[2] = getFixedHeadLine(temp[0]);//fix title
        lineIndex = Integer.parseInt(temp[1]);

        //System.out.println("LA: " + docInfo[1]);

        docInfo[3] = "" + lineIndex;
        return docInfo;
    }

    /**
     * this method edits and creates good date
     * @param str
     * @return edited doc's date
     */
    private String getFixedDate(String str) {
        String date = "";
        if(str.length() > 0)
        {
            str = str.replace("<P>", "");
            str = str.replace("</P>", "");
            str = spacesKiller(str);

            String [] spreadDate = str.split(",");
            String [] mounthDay = spreadDate[0].split(" ");
            if(mounthDay.length > 0 && spreadDate.length > 1)
            {
                String goodDate = mounthDay[0] + spreadDate[1];
                date = goodDate;
            }
            else
                return date;
        }
        else {
            return date;
        }
        return date;
    }

    /**
     * this method edits and creates good title
     * @param str
     * @return edited doc's headline
     */
    private String getFixedHeadLine(String str) {
        if(str.length() > 0)
        {

            str = str.replace("<P>", "");
            int cut = str.indexOf("</P>");
            str = str.substring(0,cut);
            str = spacesKiller(str);
        }
        return str;
    }
}
