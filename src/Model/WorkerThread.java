package Model;

import Model.HandleParse.Parse;
import Model.HandleReadFiles.ReadFile;
import Model.IndexerAndDictionary.Indexer;
import Model.TermsAndDocs.Docs.Document;
import Model.TermsAndDocs.Pairs.TermDocPair;
import Model.TermsAndDocs.Terms.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WorkerThread implements Runnable{
    public  HashSet<String> stopWords;
    private String pathFolder;
    private String[] readFilesPath;
    private String docFilePath;
    private String sPostFilePath;
    private Boolean toStem;


    public WorkerThread(String pathFolder, String[] readFilePath, String sPostFilePath, String docFilePath, HashSet<String> stopWords, Boolean toStem) {
        this.pathFolder = pathFolder;
        this.readFilesPath = readFilePath;
        this.docFilePath = docFilePath;
        this.sPostFilePath = sPostFilePath;
        this.stopWords = stopWords;
        this.toStem = toStem;
    }

    /**
     * the run function of the thread. it read files given by the readFile, parses them and index them.
     */
    @Override
    public void run() {
        int workerID = 0;
        int numOfFiles = 10;
        int partition = readFilesPath.length / numOfFiles;
        for (int i = 0; i < partition; i++) {
            String[] currentFiles = createPartOfFolder(readFilesPath, partition, i);
            workerID++;
            ArrayList<HashMap<Term, TermDocPair>> docsPairsList = new ArrayList<>();
            String workerPostPath = sPostFilePath + "partitionWorker" + workerID;
            for (String pathFile : currentFiles) {
                String readFilePath = pathFolder + "\\" + pathFile;
                //reads a file and creates a list of docs to parser
                ArrayList<Document> docList = new ArrayList<>();
                ReadFile readFile = new ReadFile(readFilePath, docList);
                readFile.readTheFile();
                //sends docs to parser and creates pairs list of <term,doc>
                Parse parser = new Parse(stopWords, toStem);
                while (docList.size() > 0) {
                    docsPairsList.add(parser.parseDocument(docList.remove(docList.size() - 1)));
                }
            }
            Indexer indexer = new Indexer(docsPairsList, docFilePath, workerPostPath);
            indexer.index();
        }
    }




    /**
     * @param folderFiles
     * @param partition
     * @param i
     * @return initilized String [] (containing part of the files
     * this method create and initialized String [] partition of big folder
     */
    private String[] createPartOfFolder(String[] folderFiles, int partition, int i) {
        int size = folderFiles.length / partition;
        if (i == partition - 1)
            size = size + folderFiles.length % partition;
        String[] fifthFolderFiles = new String[size];
        for (int k = i * (folderFiles.length / partition); k < i * (folderFiles.length / partition) + size; k++) {
            fifthFolderFiles[k - i * (folderFiles.length / partition)] = folderFiles[k];
        }
        return fifthFolderFiles;
    }
}