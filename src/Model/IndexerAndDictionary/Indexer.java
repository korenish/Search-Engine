package Model.IndexerAndDictionary;

import Model.OuputFiles.DocumentFile.DocumentFileHandler;
import Model.OuputFiles.PostingFile.WorkerPostingFileHandler;
import Model.TermsAndDocs.Docs.Document;
import Model.TermsAndDocs.Pairs.TermDocPair;
import Model.TermsAndDocs.Terms.*;

import java.util.*;

public class Indexer {

    public static Dictionary dictionary = new Dictionary();
    private ArrayList<HashMap<Term, TermDocPair>> pairsMapsList;
    private HashMap<Term, String> termDescriptionMap; //term, term description of current file
    private TermBuilder termBuilder;
    private DocumentFileHandler documentFileHandler;
    private WorkerPostingFileHandler postFile;
    String documentDataFilePath;


    public Indexer(ArrayList<HashMap<Term, TermDocPair>> pairsMapsList, String documentDataFilePath, String postFilePath) {
        this.pairsMapsList = pairsMapsList;
        termDescriptionMap = new HashMap<>();
        termBuilder = new TermBuilder();
        documentFileHandler = new DocumentFileHandler();
        this.documentDataFilePath = documentDataFilePath;
        postFile = new WorkerPostingFileHandler(postFilePath);
    }

    /**
     * this method builds dictionary and writes posting files to specified paths given by the user.
     */

    public void index() {
        int count = 0;
        //iterating through the file
        for (int i = pairsMapsList.size() - 1; i >= 0; i--) {
            HashMap<Term, TermDocPair> currentMap = pairsMapsList.get(i);
            Document document = new Document("", "", "", "");

            int numOfUniqueTerms = currentMap.size();
            Term mostCommmonTerm = termBuilder.buildTerm("RegularTerm", "");
            int mostCommonTermCounter = 0;

            DocumentDateTerm documentDateTerm = new DocumentDateTerm("");

            HashMap<Term, Integer> docEntities = new HashMap<>();

            int docSize = 0;

            //iterating through current document's map, each term at a time
            for (Map.Entry<Term, TermDocPair> entry : currentMap.entrySet()) {
                docSize += entry.getValue().getCounter();

                int currentTermCounter;
                TermDocPair currentPair = entry.getValue();
                document = currentPair.getDoc();
                currentTermCounter = currentPair.getCounter();
                Term currentTerm = entry.getKey();
                String termDataEntry;
                if (currentPair.getCounter() > mostCommonTermCounter) { //update most common if needed
                    mostCommonTermCounter = currentPair.getCounter();
                    mostCommmonTerm = currentTerm;
                }

                //handles doc date single term
                if (currentTerm instanceof DocumentDateTerm) {
                    documentDateTerm = (DocumentDateTerm) currentTerm; //no need to add this term to the dictionary
                    continue;
                }

                //updates the dictionary by given term
                else {
                    currentTerm = updatingDictionary(currentTerm, currentTermCounter, docEntities);
                }

                //handles the output post file
                if (termDescriptionMap.containsKey(currentTerm)) { //if term already in this file's term description map
                    termDataEntry = termDescriptionMap.get(currentTerm);
                    termDataEntry = addPairToDataString(currentPair, termDataEntry);
                    termDescriptionMap.put(currentTerm, termDataEntry); //updating the string in the map
                } else { //if term is not in map
                    termDataEntry = addPairToDataString(currentPair, currentTerm.getData());
                    termDescriptionMap.put(currentTerm, termDataEntry); //add to files map
                }
            }
            try {
                documentFileHandler.writeDocumentDataToFile(this.documentDataFilePath, document.getDocNo(), numOfUniqueTerms, mostCommonTermCounter, mostCommmonTerm, documentDateTerm, document.getHeader(), docSize, docEntities);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pairsMapsList.remove(i); //delete already used map to save ram! (o(1) because deleting from the end)
        }
        ArrayList<String> sortedValues = new ArrayList<>();
        for (Map.Entry<Term, String> entry : termDescriptionMap.entrySet()) {
            sortedValues.add(entry.getValue());
        }
        Collections.sort(sortedValues, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                String s1 = (String) o1;
                String s2 = (String) o2;
                if(s1.length() > 0 && s2.length() > 0)
                {
                    int cut1 = s1.indexOf('(');
                    int cut2 = s2.indexOf('(');
                    String s1Comp = s1.substring(0, cut1);
                    String s2Comp = s2.substring(0, cut2);
                    return s2Comp.compareTo(s1Comp);
                }
                return s1.compareTo(s2);
            }
        });

        postFile.writeWorkerFile(sortedValues);
    }

    /**
     * this method gets a term and update the dictionary if necessary, (output term might change because we want to save
     * in the posting file lower case term data, for future merging.
     *
     * @param currentTerm
     * @param docEntities
     * @return update current term
     */
    private Term updatingDictionary(Term currentTerm, int currentPairCounter, HashMap<Term, Integer> docEntities) {
        //handles entity terms
        if (currentTerm instanceof EntityTerm) {
            docEntities.put(currentTerm, currentPairCounter);
            if (dictionary.contains(currentTerm)) {
                ((EntityTerm) currentTerm).setToEntity();
                dictionary.add(currentTerm,currentPairCounter);
            } else {
                dictionary.add(currentTerm,currentPairCounter);
            }
        }
        //handles regular terms
        else if (currentTerm instanceof RegularTerm) {
            String currentUpperCase = currentTerm.getData().toUpperCase();
            Term tempUp = termBuilder.buildTerm("CapsTerm", currentUpperCase);
            int counter = 0;
            if (dictionary.contains(tempUp)) {
                counter = dictionary.get(tempUp).totalCount;
                dictionary.remove(tempUp); //remove similar term with upper case from dictionary
            }
            dictionary.add(currentTerm,currentPairCounter + counter);
        }
        //handles caps terms
        else if (currentTerm instanceof CapsTerm) {
            String lowerCase = currentTerm.getData().toLowerCase();
            Term tempLow = termBuilder.buildTerm("RegularTerm", lowerCase);
            if (!dictionary.contains(tempLow)) {
                dictionary.add(currentTerm,currentPairCounter);
            }
            else
            {
                dictionary.add(tempLow, currentPairCounter);
            }
            currentTerm = tempLow;
        }
        //handles every other term (Date, Price, Exp, Measurement, Numeric, Percentage)
        else {
            dictionary.add(currentTerm,currentPairCounter);  //and to dictionary if needed
        }
        return currentTerm;
    }


    /**
     * @param pair
     * @param termDataEntry
     * @return creating for each term a fitting String for the post file
     */
    private String addPairToDataString(TermDocPair pair, String termDataEntry) {
        StringBuilder appender = new StringBuilder();
        appender.append(termDataEntry);
        appender.append("(");
        appender.append(pair.getDoc().getDocNo());
        appender.append(";");
        appender.append(pair.getCounter());
        appender.append(")");
        return appender.toString();
    }

    /**
     * set dictionary to null
     */
    public static void deleteDictionary()
    {
        dictionary=null;
    }
}
