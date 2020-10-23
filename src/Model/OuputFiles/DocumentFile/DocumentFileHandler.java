package Model.OuputFiles.DocumentFile;

import Model.TermsAndDocs.Terms.DocumentDateTerm;
import Model.TermsAndDocs.Terms.Term;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * this class is responsible for writing and writing the docs files
 */
public class DocumentFileHandler {

    public DocumentFileHandler() { }
    public static AtomicInteger countDocs = new AtomicInteger(0);

    /**
     * this method gets the details of a document
     * and writes to the relevant file
     * @param docNo
     * @param numOfUniqueTerms
     * @param mostCommonTermCounter
     * @param mostCommmonTerm
     * @param documentDateTerm
     * @param header
     * @param docSize
     * @param entities
     */
    public void writeDocumentDataToFile(String documentDataFilePath, String docNo, int numOfUniqueTerms, int mostCommonTermCounter, Term mostCommmonTerm,
                                        DocumentDateTerm documentDateTerm, String header, int docSize, HashMap<Term, Integer> entities) {
        countDocs.addAndGet(1);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(docNo);
        stringBuilder.append(";");
        stringBuilder.append(docSize);
        stringBuilder.append(";");
        stringBuilder.append(numOfUniqueTerms);
        stringBuilder.append(";");
        stringBuilder.append(mostCommmonTerm.getData());
        stringBuilder.append(";");
        stringBuilder.append(mostCommonTermCounter);
        stringBuilder.append(";");
        stringBuilder.append(documentDateTerm.getData());
        stringBuilder.append(";");
        stringBuilder.append(header);
        stringBuilder.append(";");
        stringBuilder.append("ENTITIES:");
        int count = 0;
        for (Map.Entry<Term, Integer> entry : entities.entrySet()) {
            count++;
            stringBuilder.append(entry.getKey().getData());//writing entity
            stringBuilder.append("|");
            stringBuilder.append(entry.getValue());//writing count of the entity in this doc
            stringBuilder.append(";");
        }
        if(count > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("\n");


        try {
            FileWriter fw = new FileWriter(documentDataFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(stringBuilder.toString());
            bw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * this method gets String docNo and returns all of the doc's properties from our the docs file via string line
     * @return String line of data
     */
    public ConcurrentHashMap<String, String> extractDocsData(ArrayList<String> docsPath) throws InterruptedException {
        int numOfFiles = 6;
        ExecutorService pool = Executors.newFixedThreadPool(numOfFiles);

        ConcurrentHashMap<String, String> docsHolder = new ConcurrentHashMap<>();
        //using threads to search through different files
        for (int i = 0; i < numOfFiles; i++) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(docsPath.get(i)));
                FindDocData findDocData = new FindDocData(reader, docsHolder);
                pool.execute(findDocData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
            pool.shutdown();
            pool.awaitTermination(200000, TimeUnit.SECONDS);

        //returning the line of doc's data
        return docsHolder;
    }

}
