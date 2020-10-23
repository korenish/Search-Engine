package Model.OuputFiles.DocumentFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * implements Runnable class - for the purpose of running simultaneously through different files in order to find doc
 * using it's ID (docNo)
 */
public class FindDocData implements Runnable {
    private BufferedReader reader;
    private ConcurrentHashMap<String, String> dataHolder;

    protected FindDocData(BufferedReader reader, ConcurrentHashMap<String, String> dataHolder) {
        this.reader = reader;
        this.dataHolder = dataHolder;
    }

    /**
     * saving all the docs data in that file inside hash
     * saving it's line of properties
     */
    @Override
    public void run() {
        String line = null;
        try {
            line = reader.readLine();
            while (line != null) {
                String docNo = "";
                int i = 0;
                char ch = line.charAt(i);
                while (ch != ';'){
                    docNo += ch;
                    i++;
                    ch = line.charAt(i);
                }
                i++;
                String docData = line.substring(i);
                dataHolder.put(docNo, docData);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
