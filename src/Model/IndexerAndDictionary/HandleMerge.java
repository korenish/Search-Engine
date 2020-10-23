package Model.IndexerAndDictionary;

import Model.TermsAndDocs.Terms.Term;
import Model.TermsAndDocs.Terms.TermBuilder;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 */
@SuppressWarnings("DuplicatedCode")
public class HandleMerge {
    private boolean toStem;
    private String outPath;
    private HashSet<String> deletedTerms;
    private Dictionary dictionary;
    private static String _NumNumTerm = "Num-NumTerm";
    private static String _ExpressionTerm = "ExpressionTerm";
    private static String _PercentageTerm = "PercentageTerm";
    private static String _PriceTerm = "PriceTerm";
    private static String _MeasurementTerm = "MeasurementTerm";
    private static String _WordTerm_aTe = "WordTerm_a-e";
    private static String _WordTerm_fTj = "WordTerm_f-j";
    private static String _WordTerm_kTo = "WordTerm_k-o";
    private static String _WordTerm_pTt = "WordTerm_p-t";
    private static String _WordTerm_uTz = "WordTerm_u-z";
    private static String _NumericTerm_0T4 = "NumericTerm_0-4";
    private static String _NumericTerm_5T9 = "NumericTerm_5-9";
    private static Pattern countDF = Pattern.compile("[\\(]");

    public HandleMerge(HashSet<String> deletedTerms, Dictionary dictionary, String outPath, boolean toStem) {
        this.deletedTerms = deletedTerms;
        this.dictionary = dictionary;
        this.outPath = outPath;
        this.toStem = toStem;

    }

    public void merge() {
        long start = System.currentTimeMillis();
        dictionary.initializePointers(outPath, toStem);
        mergePostingFiles(outPath, toStem);
        long end = System.currentTimeMillis();
        System.out.println("time to merge: " + (end - start));
    }

    /**
     * this method merges all the workers temp posting files to the final correct posting file
     */
    private void mergePostingFiles(String outPath, boolean toStem) {
        try {
            //creates and preparing post files
            String finalOutPath;
            if(toStem)
                finalOutPath = outPath + "\\stemOur";
            else
                finalOutPath = outPath + "\\noStemOur";
            HashMap<String, BufferedWriter > buffWriters = initFinalPostFiles(finalOutPath);
            String workersDirPath;
            //preparing to read from all the posting files
            if (toStem)
                workersDirPath = "stemOur\\workersFiles";
            else
                workersDirPath = "noStemOur\\workersFiles";
            File workerDir = new File(workersDirPath);
            LinkedList<File> postingFiles = new LinkedList<>();
            File[] workerPostings = workerDir.listFiles();
            //creating list of all read posting files
            for (File postingArray : workerPostings) {
                File[] postings = postingArray.listFiles();
                postingFiles.addAll(Arrays.asList(postings));
            }
            BufferedReader[] buffers = new BufferedReader[postingFiles.size()];
            for (int i = 0; i < postingFiles.size(); i++) {
                buffers[i] = new BufferedReader(new FileReader(postingFiles.get(i)));
            }

            //inserting the read posting files to string data
            String[] currentSmallests = new String[buffers.length];
            //init list of smallest terms in each doc
            initSmallest(currentSmallests, buffers);

            writeToPostings(buffWriters, currentSmallests, buffers, finalOutPath);

        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    /**
     * @param outPath
     * @return hash of all buffer writers to different files
     */
    private HashMap<String, BufferedWriter> initFinalPostFiles(String outPath) {
        HashMap<String, BufferedWriter> outersWriters= new HashMap<>();

        createBuffWriter(outPath, _NumNumTerm, outersWriters);
        createBuffWriter(outPath, _ExpressionTerm, outersWriters);
        createBuffWriter(outPath, _MeasurementTerm, outersWriters);
        createBuffWriter(outPath, _NumericTerm_0T4, outersWriters);
        createBuffWriter(outPath, _NumericTerm_5T9, outersWriters);
        createBuffWriter(outPath, _PercentageTerm, outersWriters);
        createBuffWriter(outPath, _PriceTerm, outersWriters);
        createBuffWriter(outPath, _WordTerm_aTe, outersWriters);
        createBuffWriter(outPath, _WordTerm_fTj, outersWriters);
        createBuffWriter(outPath, _WordTerm_kTo, outersWriters);
        createBuffWriter(outPath,_WordTerm_pTt, outersWriters);
        createBuffWriter(outPath, _WordTerm_uTz, outersWriters);

        return outersWriters;
    }

    /**
     * initialize buff writer to fitting file
     * @param outPath
     * @param fileName
     * @param outersWriters
     */
    private void createBuffWriter(String outPath, String fileName, HashMap<String, BufferedWriter> outersWriters) {
        try {
            File wPostFile = new File(outPath + "_" + fileName);
            wPostFile.createNewFile();
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter(wPostFile));
            outersWriters.put(fileName, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method is getting the first - smallest value of each temp posting file,
     * by saving them in: current Smallests string array
     *
     * @param currentSmallests
     * @param buffers
     */
    private void initSmallest(String[] currentSmallests, BufferedReader[] buffers) {
        try {
            for (int i = 0; i < currentSmallests.length; i++) {
                String currentSmallestInFile;
                currentSmallestInFile = buffers[i].readLine();

                while (currentSmallestInFile != null && currentSmallestInFile.length() < 1) {
                    if (currentSmallestInFile.length() < 1) {
                        currentSmallestInFile = buffers[i].readLine();
                    }
                }
                currentSmallests[i] = (currentSmallestInFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToPostings(HashMap<String, BufferedWriter> buffWriters, String[] currentSmallests, BufferedReader[] buffers, String finalOutPath) {
        try {
            TermBuilder termBuilder = new TermBuilder();
            String currentLine;
            String smallestTerm;
            LinkedList<Integer> readAgain = new LinkedList<>();
            HashSet<Integer> hashNulls = new HashSet<>();

            int startCheck = 0;
            StringBuilder fileWrite = new StringBuilder();
            Term checker = null;
            //picking each time one smallest term, and inserting another from it's files
            while (hashNulls.size() < currentSmallests.length) {
                //picking smallest and getting data
                readAgain.add(startCheck);
                currentLine = currentSmallests[startCheck];
                if (currentLine != null) {
                    StringBuilder appender = new StringBuilder();
                    int cut = currentLine.indexOf('(');
                    smallestTerm = currentLine.substring(0, cut);
                    if (!this.deletedTerms.contains(smallestTerm)) {
                        appender.append(currentLine);
                        for (int k = startCheck + 1; k < currentSmallests.length; k++) {
                            currentLine = currentSmallests[k];
                            if (currentLine != null) {
                                cut = currentLine.indexOf('(');
                                String currentTerm = currentLine.substring(0, cut);
                                if (!this.deletedTerms.contains(currentTerm)) {
                                    if (currentTerm.compareTo(smallestTerm) < 0) {
                                        //checks if term is in the dictionary
                                        readAgain.clear();
                                        readAgain.add(k);
                                        smallestTerm = currentTerm;
                                        appender = new StringBuilder();
                                        appender.append(currentLine);
                                    } else if (currentTerm.compareTo(smallestTerm) == 0) {
                                        readAgain.add(k);
                                        appender.append(currentLine.substring(cut));
                                    }
                                } else {
                                    currentSmallests[k] = buffers[k].readLine();
                                    k--;
                                }
                            } else {
                                hashNulls.add(k);
                            }
                        }
                        checker = termBuilder.buildTerm("RegularTerm", smallestTerm);
                        if (!dictionary.contains(checker))
                            checker.setData(smallestTerm.toUpperCase());
                        CountAndPointerDicValue dicValue = dictionary.get(checker);
                        fileWrite.append(appender);

                        //countDF
                        String [] sCount = countDF.split(fileWrite);
                        fileWrite.append(",df{");
                        fileWrite.append(sCount.length - 1);
                        fileWrite.append("}");
                        fileWrite.append("\n");
                        String writeNow = fileWrite.toString();

                        //write to file
                        String fileToWrite = dicValue.getPointer().getFileStrName();
                        buffWriters.get(fileToWrite).write(writeNow);
                        fileWrite = new StringBuilder();
                        while (readAgain.size() > 0) {
                            int removeIndex = readAgain.removeFirst();
                            currentSmallests[removeIndex] = buffers[removeIndex].readLine();
                        }
                    } else {
                        int removeIndex = readAgain.removeFirst();
                        currentSmallests[removeIndex] = buffers[removeIndex].readLine();
                    }
                } else {
                    hashNulls.add(startCheck);
                    startCheck++;
                }
            }
            //closing buffers
            for (int i = 0; i < buffers.length; i++) {
                buffers[i].close();
            }
            for (Map.Entry<String, BufferedWriter> entry : buffWriters.entrySet()) {
                entry.getValue().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

