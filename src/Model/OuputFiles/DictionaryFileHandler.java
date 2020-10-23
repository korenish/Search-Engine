package Model.OuputFiles;

import Model.IndexerAndDictionary.CountAndPointerDicValue;
import Model.IndexerAndDictionary.Dictionary;
import Model.IndexerAndDictionary.PostingFilePointer;
import Model.TermsAndDocs.Terms.Term;
import Model.TermsAndDocs.Terms.TermBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * This class is responsible for writing the dictionary to a file
 */
public class DictionaryFileHandler {

    private Dictionary dictionary;
    private Pattern pattern;
    private static TermBuilder termBuilder = new TermBuilder();

    public DictionaryFileHandler(Dictionary dictionary) {
        this.dictionary = dictionary;
        pattern = Pattern.compile(";");
    }

    /**
     * This method writes dictionary to file so we can upload it later
     * @param dictionaryPath
     * @param toStem
     */
    public void writeToFile(String dictionaryPath, boolean toStem) {
        try {
            if (toStem)
                dictionaryPath = dictionaryPath + "\\sDic";
            else
                dictionaryPath = dictionaryPath + "\\nsDic";
            try {
                FileChannel.open(Paths.get(dictionaryPath), StandardOpenOption.WRITE).truncate(0).close();
            }
            catch (Exception e) {}
            ArrayList<Term> sortedKeys = dictionary.sortedKeys();
            FileWriter fw = new FileWriter(dictionaryPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Term key : sortedKeys) {
                StringBuilder lineToWrite = new StringBuilder();
                lineToWrite.append(key.getData());//term
                lineToWrite.append(";");
                lineToWrite.append(key.getType());//instance
                lineToWrite.append(";");
                CountAndPointerDicValue countAndPointerDicValue = dictionary.get(key);
                lineToWrite.append(countAndPointerDicValue.toString());//count;fileStrPointer
                lineToWrite.append("\n");
                bw.write(lineToWrite.toString());
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method writes dictionary to file so we can upload it later
     * @param dictionaryPath
     * @param toStem
     * @return
     */
    public Dictionary readFromFile(String dictionaryPath, boolean toStem) {
        String outPath = dictionaryPath;
        if (toStem) {
            dictionaryPath = dictionaryPath + "\\sDic";
            outPath += "\\stemOur_";
        }
        else {
            dictionaryPath = dictionaryPath + "\\nsDic";
            outPath += "\\noStemOur_";
        }
        try  {
            BufferedReader br = new BufferedReader(new FileReader(dictionaryPath));
            String line;
            int totalCount = -1;
            while ((line = br.readLine()) != null) {
                String[] splited = pattern.split(line);
                String termData = splited[0];
                String termInstance = splited[1];
                Term term = termBuilder.buildTerm(termInstance, termData);
                try {
                    totalCount = Integer.parseInt(splited[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PostingFilePointer pointer = new PostingFilePointer(outPath + splited[3]);
                CountAndPointerDicValue dicValue = new CountAndPointerDicValue();
                dicValue.setTotalCount(totalCount);
                dicValue.setPointer(pointer);
                dictionary.add(term, dicValue);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return this.dictionary;
    }

}
