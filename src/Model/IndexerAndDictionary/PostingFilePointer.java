package Model.IndexerAndDictionary;

import Model.TermsAndDocs.Terms.Term;

import java.util.regex.Pattern;

public class PostingFilePointer {

    private String fileStrName;
    private String fileStr;
    private static Pattern stemSplit = Pattern.compile("[s][t][e][m][O][u][r][_]");
    private static Pattern notStemSplit = Pattern.compile("[n][o][S][t][e][m][O][u][r][_]");

    public PostingFilePointer(String fileStr)
    {
        this.fileStr = fileStr;
        String[] splitter = stemSplit.split(fileStr);
        String[] splitter2 = notStemSplit.split(fileStr);
        if(splitter.length > 1)
        {
            this.fileStrName = splitter[1];
        }
        else if(splitter2.length > 1)
        {
            splitter = notStemSplit.split(fileStr);
            this.fileStrName = splitter[1];
        }
        else
        {
            fileStrName = "NONE";
        }

    }

    public PostingFilePointer() {
        this.fileStr = "";
    }

    /**
     * this method finds the pointer string for term
     * @param term
     * @param outPath
     * @param toStem
     */
    public void findFileStr(Term term, String outPath, boolean toStem) {
        if (toStem)
            outPath = outPath + "\\stemOur_";
        else
            outPath = outPath + "\\noStemOur_";
        String sPath = outPath;
        if (!term.getType().equals("RegularTerm") && !term.getType().equals("CapsTerm") && !term.getType().equals("EntityTerm")
                && !term.getType().equals("NumericTerm") && !term.getType().equals("DateTerm")) {
            this.fileStrName = term.getType();
            this.fileStr = sPath + term.getType();
        }
        else if (term.getType().equals("DateTerm"))
        {
            this.fileStrName = "Num-NumTerm";
            this.fileStr = sPath + "Num-NumTerm";
        }
        else if (term.getType().equals("RegularTerm") || term.getType().equals("CapsTerm") || term.getType().equals("EntityTerm")) {
            String val = term.getData();
            char firstChar = val.charAt(0);
            if ((firstChar >= 'a' && firstChar <= 'e') || (firstChar >= 'A' && firstChar <= 'E'))
            {
                this.fileStrName = "WordTerm_a-e";
                this.fileStr = sPath + "WordTerm_a-e";
            }
            else if ((firstChar >= 'f' && firstChar <= 'j') || (firstChar >= 'F' && firstChar <= 'J'))
            {
                this.fileStrName = "WordTerm_f-j";
                this.fileStr = sPath + "WordTerm_f-j";
            }
            else if((firstChar >= 'k' && firstChar <= 'o') || (firstChar >= 'K' && firstChar <= 'O'))
            {
                this.fileStrName = "WordTerm_k-o";
                this.fileStr = sPath + "WordTerm_k-o";
            }
            else if((firstChar >= 'p' && firstChar <= 't') || (firstChar >= 'P' && firstChar <= 'T'))
            {
                this.fileStrName = "WordTerm_p-t";
                this.fileStr = sPath + "WordTerm_p-t";
            }
            else
            {
                this.fileStrName = "WordTerm_u-z";
                this.fileStr = sPath + "WordTerm_u-z";
            }
        } else //NumericTerm
        {
            String val = term.getData();
            char firstChar = val.charAt(0);
            if (firstChar >= '0' && firstChar <= '4')
            {
                this.fileStrName = "NumericTerm_0-4";
                this.fileStr = sPath + "NumericTerm_0-4";
            }
            else
            {
                this.fileStrName = "NumericTerm_5-9";
                this.fileStr = sPath + "NumericTerm_5-9";
            }
        }
    }

    public String getFileStr(){
        return fileStr;
    }


    /**
     * getter for fileStrName
     * @return
     */
    public String getFileStrName() {
        return fileStrName;
    }
}
