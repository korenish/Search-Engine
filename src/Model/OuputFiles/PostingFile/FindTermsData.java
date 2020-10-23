package Model.OuputFiles.PostingFile;

import Model.TermsAndDocs.Terms.Term;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * this class is responsible for finding the line of properties in given posting file
 * helps to searcher
 */
public class FindTermsData {

    public FindTermsData() { }

    /**
     * @param path
     * @param requestList
     * @return
     */
    public ArrayList<Pair<Term, String>> searchAllTermsInPostFile(String path, ArrayList<Pair<Term, String>> requestList) {
        try {
            ArrayList<Pair<Term, String>> termAndLine = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String ansLine = reader.readLine();
            int i = 0;
            int requestSize = requestList.size();
            while (ansLine != null && i < requestSize) {
                int k = 0;
                char ch = ansLine.charAt(k);
                String currentPostTerm = "";
                while (ch != '('){
                    currentPostTerm += ch;
                    k++;
                    ch = ansLine.charAt(k);
                }

                if(currentPostTerm.equals(requestList.get(i).getValue())){
                    termAndLine.add(new Pair<>(requestList.get(i).getKey(), ansLine));
                    i++;
                }
                ansLine = reader.readLine();
            }
            reader.close();
            return termAndLine;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
