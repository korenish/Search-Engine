package Model.HandleReadFiles.DocsHandlerToRead;

/**
 * this class handle info about doc of FT doc's type
 */
public class HandleFT implements HandleDoc {

    @Override
    public String[] handle(String[] listLines, int lineIndex) {
        String[] docInfo = new String[4];
        //returns good docno and updated line index
        String[] temp = infoFinder(listLines, lineIndex, "<DOCNO>");
        lineIndex = Integer.parseInt(temp[1]);
        docInfo[0] = temp[0];

        //returns good date and title and updated line index
        temp = infoFinder(listLines, lineIndex, "<HEADLINE>");
        String[] headline = new String[2];
        headline[0] = "";
        headline[1] = "" + 0;
        if(temp[0].length() > 0)
            headline = helpHeadline(temp[0]);
        lineIndex = Integer.parseInt(temp[1]);
        docInfo[1] = headline[0];//date
        docInfo[2] = headline[1];//title

        //System.out.println("FT: " + docInfo[1]);

        docInfo[3] = "" + lineIndex;
        return docInfo;
    }

    /**
     * @param temp
     * @return good date and title values
     */
    private String[] helpHeadline(String temp) {
        String[] fixedHeadline = new String[2];
        fixedHeadline[0] = "";
        fixedHeadline[1] = "";
        String headline = temp;
        int i;
        char c;
        String date = getDateFromHeadLine(headline);
        fixedHeadline[0] = date;//DATE
        String title = getTitleFromHeadLine(headline);
        fixedHeadline[1] = title;

        return fixedHeadline;
    }

    /**
     * this method gets from headline the date
     * @param headline
     * @return String of date
     */
    private String getDateFromHeadLine(String headline) {
        int i = 0;
        char c = headline.charAt(i);
        String date = "";
        while (c == ' ' || c == 'F' || c == 'T')//useless data at the beginning
        {
            i++;
            c = headline.charAt(i);
        }
        while (c != '/')//date data and some " " at the end
        {
            date = date + c;
            i++;
            c = headline.charAt(i);
        }
        int x = date.length() - 1;
        char s = date.charAt(x);
        while (s == ' ')//getting rid of all " " at the end of good info
        {
            date = date.substring(0, x);
            x = date.length() - 1;
            s = date.charAt(x);
        }
        return date;
    }

    /**
     * this method gets from headline the title
     * @param headline
     * @return String of title
     */
    private String getTitleFromHeadLine(String headline) {
        int i = headline.indexOf('/');
        i++;
        String title = headline;
        title = title.substring(i);
        title = spacesKiller(title);
        return title;
    }
}
