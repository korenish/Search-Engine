package Model.HandleReadFiles.DocsHandlerToRead;

/**
 * interface of handling a file
 */
public interface HandleDoc {

    /**
     * @param listLines
     * @param lineIndex
     * @return string [] of relevant info about the doc.
     * the method returns (docno, date, title, updated lineIndex)
     */
    String [] handle(String [] listLines, int lineIndex);

    /**
     * @param listLines
     * @param lineIndex
     * @param wantedStr
     * @return edited lines of requested info and updated lineIndex
     */
    default String [] infoFinder(String [] listLines, int lineIndex, String wantedStr) {
        while (!listLines[lineIndex].contains(wantedStr)) {
            if(lineIndex == listLines.length - 1)
            {
                String [] error = new String[2];
                error[0] = "";
                error[1] = "" + 0;
                return error;
            }
            lineIndex++;
        }
        String helper = "";
        helper = (listLines[lineIndex]);
        String secWantedStr = wantedStr.substring(0,1) + "/" + wantedStr.substring(1);
        while (!listLines[lineIndex].contains(secWantedStr))
        {
            lineIndex++;
            helper += " " +listLines[lineIndex];
        }
        helper = helper.replace(wantedStr, "");
        helper = helper.replace(secWantedStr,"");
        String [] ans = new String[2];

        helper = spacesKiller(helper);

        ans[0] = helper;
        ans[1] = "" + lineIndex;
        return ans;
    }

    /**
     * @param helper
     * @return the given string without useless ' ' in the beginning and end
     */
    default String spacesKiller(String helper){
        if(helper.length() > 0)
        {
            char c = helper.charAt(0);
            while(c == ' ' && helper.length() > 0)//throw starting ' '
            {
                helper = helper.substring(1);
                if(helper.length() > 0)
                    c = helper.charAt(0);
            }
            if(helper.length() > 0)
            {
                c = helper.charAt(helper.length()-1);
                while(c == ' ' && helper.length() > 0)//throw ending ' '
                {
                    helper = helper.substring(0, helper.length()-1);
                    if(helper.length() > 0)
                        c = helper.charAt(helper.length() - 1);
                }
            }
        }
        return helper;
    }
}
