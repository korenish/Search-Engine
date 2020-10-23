package Model.HandleReadFiles;

import Model.HandleReadFiles.DocsHandlerToRead.ClientDocHandler;
import Model.TermsAndDocs.Docs.Document;

import java.io.*;
import java.util.ArrayList;

/**
 * this class is responsible for reading and separating documents from files
 */
public class ReadFile {
    private String pathFolder;
    public ArrayList<Document> docList;

    public ReadFile(String pathFolder, ArrayList<Document> docList) {
        this.pathFolder = pathFolder;
        this.docList = new ArrayList<>();
        this.docList = docList;
    }

    /**
     * this method receives the path and creates threads that read the files
     */
    public void readTheFile() {
        File folderFile = new File(pathFolder);
        if (folderFile.exists()) {
            String[] fileName = folderFile.list();
            File file = new File(pathFolder + "\\" + fileName[0]);
            if (file.exists() && file.canRead()) {
                StringBuilder totalBuilderFile = getGoodLines(file);
                String totalFile = totalBuilderFile.toString();
                String [] docs = totalFile.split("<DOC>");
                for(int i = 0; i < docs.length; i++) //in every iteration we handle doc
                {
                    if(docs[i].length() == 0)
                        i++;
                    int lineIndex = 0;
                    String [] listLines = docs[i].split("\n");
                    while (!listLines[lineIndex].contains("<DOCNO>"))
                    {
                        lineIndex++;
                    }
                    String allDocno = listLines[lineIndex];
                    String[] docInfo;
                    ClientDocHandler clientDocHandler= new ClientDocHandler(allDocno);
                    docInfo = clientDocHandler.handleDocForClient(listLines, lineIndex);

                    lineIndex = Integer.parseInt(docInfo[3]);
                    String text = pullText(listLines,lineIndex);
                    //adding the doc
                    docList.add(new Document(docInfo[0], text, docInfo[1], docInfo[2]));
                }
            }
        }
    }

    /**
     * @param listLines
     * @param lineIndex
     * @return String of organized data from the text
     */
    private String pullText(String[] listLines, int lineIndex) {
        String text = "";
        StringBuilder strB = new StringBuilder();
        while(!listLines[lineIndex].contains("<TEXT>"))
        {
            if(lineIndex == listLines.length - 1)
            {
                return "";
            }
            lineIndex++;
        }
        lineIndex++;
        while(!listLines[lineIndex].contains("</TEXT>"))
        {
            if(!listLines[lineIndex].contains("</P>") && !listLines[lineIndex].contains("<P>") && !listLines[lineIndex].contains("</H3>")
                    && !listLines[lineIndex].contains("<H3>") && !listLines[lineIndex].contains("</H5>") &&
                    !listLines[lineIndex].contains("<H5>"))
                strB.append(listLines[lineIndex]);
            lineIndex++;
        }
        int headStart = strB.indexOf("[Text]");
        if (headStart > 0)
        {
            strB.delete(0, headStart + 6);
        }

        text = strB.toString();
        return text;
    }

    /**
     * @param file
     * @return returns a list of all the not empty lines in the file
     */
    private StringBuilder getGoodLines(File file) {
        BufferedReader br = null;
        StringBuilder totalFile = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                if(!st.equals(""))
                    totalFile.append(st + " \n");
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalFile;
    }
}
