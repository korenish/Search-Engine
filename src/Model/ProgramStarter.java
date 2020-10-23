package Model;

import Model.HandleParse.Parse;
import Model.HandleReadFiles.QueryFileUtil;
import Model.HandleSearch.DocDataHolders.DocumentDataToView;
import Model.HandleSearch.DocDataHolders.QueryIDDocDataToView;
import Model.HandleSearch.Searcher;
import Model.IndexerAndDictionary.CountAndPointerDicValue;
import Model.IndexerAndDictionary.Dictionary;
import Model.IndexerAndDictionary.HandleMerge;
import Model.IndexerAndDictionary.Indexer;
import Model.OuputFiles.DictionaryFileHandler;
import Model.OuputFiles.DocumentFile.DocumentFileHandler;
import Model.OuputFiles.DocumentFile.DocumentFileObject;
import Model.TermsAndDocs.TermCounterPair;
import Model.TermsAndDocs.Terms.Term;
import View.AlertBox;
import View.GUI;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProgramStarter {
    public static Dictionary dictionary;

    /**
     * this method starts the GloveTrainedFilesUsage program by creating workers an executing them.
     *
     * @param inputPath
     * @param outputPath
     * @param toStemm
     */
    public static void startProgram(String inputPath, String outputPath, boolean toStemm) {
        dictionary = new Dictionary();
        Indexer.dictionary = new Dictionary();
        String pathFolder = inputPath + "\\corpus";
        String stemRelatedFolder = getStemRelatedFolder(toStemm);
        initFolders(toStemm, outputPath);
        File folder = new File(pathFolder);
        String[] folderFiles = folder.list();
        ThreadPoolExecutor executor;
        String[][] arrays = initWorkersArrays(folderFiles);
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() + 2));
        HashSet<String> stopWords = null;
        try {
            stopWords = readStopWords(inputPath + "\\05 stop_words");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> docsPath = new ArrayList<>();
        for (int i = 0; i < arrays.length; i++) {
            String[] readFilesPath = arrays[i];//302-305
            String sPostFilePath = stemRelatedFolder + "\\workersFiles\\workerArray" + i + "\\";
            String docFilePath = outputPath + "\\" + stemRelatedFolder + "\\DocsFiles\\docFile" + i;
            docsPath.add(docFilePath);
            WorkerThread wt = new WorkerThread(pathFolder, readFilesPath, sPostFilePath, docFilePath, stopWords, toStemm);
            executor.execute(wt);
        }
        try {
            executor.shutdown();
            executor.awaitTermination(200000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        HashSet<String> deletedTerms = Indexer.dictionary.deleteNotEntities();
        ConcurrentHashMap<Term, CountAndPointerDicValue> dic = Indexer.dictionary.dictionaryTable;
        long end = System.currentTimeMillis();

        HandleMerge handleMerge = new HandleMerge(deletedTerms, Indexer.dictionary, outputPath, toStemm);
        handleMerge.merge();

        DictionaryFileHandler dictionaryFileHandler = new DictionaryFileHandler(Indexer.dictionary);
        dictionaryFileHandler.writeToFile(outputPath, toStemm);


    }

    private static String getStemRelatedFolder(boolean toStemm) {
        if (toStemm)
            return "stemOur";
        return "noStemOur";
    }

    /**
     * this is a static method which getting
     *
     * @param path
     * @return hashSet of all the sth given stop words
     */
    public static HashSet<String> readStopWords(String path) throws IOException {
        File file = new File(path);
        BufferedReader br;
        HashSet<String> stopWords = new HashSet<>();
        br = new BufferedReader(new FileReader(file + ".txt"));
        String st;
        while ((st = br.readLine()) != null) {
            if (!st.equals(""))
                stopWords.add(st);
        }
        br.close();
        return stopWords;
    }

    /**
     * this method inits the files array that we are sending to each worker
     *
     * @param folderFiles
     * @return
     */
    private static String[][] initWorkersArrays(String[] folderFiles) {
        int n = folderFiles.length;
        String[] aFilesArray = new String[(n + 1) / 6];//gonna hold 302 files
        String[] bFilesArray = new String[(n + 1) / 6];//gonna hold 302 files
        String[] cFilesArray = new String[(n + 1) / 6];//gonna hold 302 files
        String[] dFilesArray = new String[(n + 1) / 6];//gonna hold 302 files
        String[] eFilesArray = new String[(n + 1) / 6];//gonna hold 302 files
        String[] fFilesArray = new String[n - (5 * aFilesArray.length)];//gonna hold 305

        System.arraycopy(folderFiles, 0, aFilesArray, 0, aFilesArray.length);
        System.arraycopy(folderFiles, aFilesArray.length, bFilesArray, 0, bFilesArray.length);
        System.arraycopy(folderFiles, 2 * aFilesArray.length, cFilesArray, 0, cFilesArray.length);
        System.arraycopy(folderFiles, 3 * aFilesArray.length, dFilesArray, 0, dFilesArray.length);
        System.arraycopy(folderFiles, 4 * aFilesArray.length, eFilesArray, 0, eFilesArray.length);
        System.arraycopy(folderFiles, 5 * aFilesArray.length, fFilesArray, 0, fFilesArray.length);
        String[][] arrays = {aFilesArray, bFilesArray, cFilesArray, dFilesArray, eFilesArray, fFilesArray};
        return arrays;
    }

    /**
     * this method makes the folders we need if they dont exist
     *
     * @param toStem
     * @param outputPath
     */
    private static void initFolders(boolean toStem, String outputPath) {
        //String sPostFilePath = stemRelatedFolder + "\\workersFiles\\workerArray" + i + "\\";
        //String docFilePath = outputPath + "\\" + stemRelatedFolder + "\\tempDocsFiles\\docFile" + i;

        String stemWorkersDirPath = getStemRelatedFolder(toStem);
        File stemWorkerDir = new File(stemWorkersDirPath);
        if (!stemWorkerDir.exists()) {
            stemWorkerDir.mkdir();
        }

        stemWorkersDirPath = stemWorkersDirPath + "\\workersFiles";
        File stemWorkingDir = new File(stemWorkersDirPath);
        if (!stemWorkingDir.exists()) {
            stemWorkingDir.mkdir();
        }

        for (int i = 0; i < 6; i++) {
            String path = stemWorkersDirPath + "\\workerArray" + i;
            File workerArrayDir = new File(path);
            if (!workerArrayDir.exists()) {
                workerArrayDir.mkdir();
            }
        }

        String tempDocFilesPath = outputPath + "\\" + getStemRelatedFolder(toStem);
        File tempDocFiles = new File(tempDocFilesPath);
        if (!tempDocFiles.exists()) {
            tempDocFiles.mkdir();
        }

        tempDocFilesPath = outputPath + "\\" + getStemRelatedFolder(toStem) + "\\" + "DocsFiles";
        File fileTooCheck = new File(tempDocFilesPath);
        if (!fileTooCheck.exists()) {
            fileTooCheck.mkdir();
        }
        File[] files = fileTooCheck.listFiles();
        if (files != null)
            for (File f : files) {
                f.delete();
            }

    }

    @SuppressWarnings("Duplicates")
    public static void runQueriesFromFile(String path, boolean similarWords, boolean writeResultToFileCheckBoxIsSelected, boolean showEntitiesCheckBoxIsSelected, boolean stemCheckBoxIsSelected, boolean onlineSemanticIsSelected, String resultFileText, String resultFileName, String inputPath, boolean showDatesIsSelected) {
        long time1 = System.currentTimeMillis();
        try {
            HashMap<String, String> queriesHash = QueryFileUtil.extractQueries(path);
            ArrayList<String> queries = new ArrayList<>();
            ArrayList<String> queriesID = new ArrayList<>();
            for (Map.Entry<String, String> entry : queriesHash.entrySet()) {
                queries.add(entry.getValue());
                queriesID.add(entry.getKey());
            }
            boolean writeToFile = writeResultToFileCheckBoxIsSelected;
            boolean entities = showEntitiesCheckBoxIsSelected;
            boolean stemIsSelected = stemCheckBoxIsSelected;
            boolean onlineIsSelected = onlineSemanticIsSelected;
            if (dictionary == null || dictionary.dictionaryTable.size() == 0) {
                dictionary = Indexer.dictionary;
                if(dictionary.dictionaryTable.size() == 0) {
                    AlertBox.display("", "No dictionary in memory, please load dictionary!");
                    return;
                }
            }
            if(DocumentFileObject.getInstance().docsHolder == null || DocumentFileObject.getInstance().docsHolder.size() == 0){
                DocumentFileHandler documentFileHandler = new DocumentFileHandler();
                DocumentFileObject documentFileObject = DocumentFileObject.getInstance();
                documentFileObject.setInstance(documentFileHandler.extractDocsData(generateDocsFiles(stemCheckBoxIsSelected, GUI.outputPathTextField.getText())));
            }
            Searcher searcher = new Searcher(similarWords, stemIsSelected, dictionary, generateStopWords(inputPath), queries, entities, onlineIsSelected);
            ArrayList<QueryIDDocDataToView> datas = new ArrayList<>();
            ArrayList<DocumentDataToView>[] queryAnswers = searcher.search();
            for (int i = 0; i < queryAnswers.length; i++) {
                for (DocumentDataToView docData : queryAnswers[i]) {
                    datas.add(new QueryIDDocDataToView(queriesID.get(i), docData.getDocNo(), docData.getDate(), docData.getEntities()));
                }
            }
            Collections.sort(datas, new Comparator<QueryIDDocDataToView>() {
                @Override
                public int compare(QueryIDDocDataToView o1, QueryIDDocDataToView o2) {
                    return o1.getQueryID().compareTo(o2.getQueryID());
                }
            });
            if (writeToFile) {
                path = resultFileText + "\\" + resultFileName + ".txt";
                try {
                    File toCreateFile = new File(path);
                    if (toCreateFile.exists())
                        toCreateFile.delete();
                    toCreateFile.createNewFile(); //create empty file
                    FileWriter fw = new FileWriter(path, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    for (int i = 0; i < datas.size(); i++) {
                        String queryId = datas.get(i).getQueryID();
                        if (queryId.charAt(queryId.length() - 1) == ' ')
                            queryId = queryId.substring(0, queryId.length() - 1);
                        bw.append(queryId + " " + 0 + " " + datas.get(i).getDocNo() + " " + "1" + " " + "1.1" + " " + "mt");
                        bw.newLine();
                    }
                    bw.close();
                } catch (Exception e) {
                }
            }
            showResultsWithIds(datas, showDatesIsSelected, showEntitiesCheckBoxIsSelected);
            long time2 = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("Duplicates")
    public static void runSingleQuery(String query, boolean similarWords, boolean writeResultToFileIsSelected, boolean showEntitiesIsSelected, boolean stemCheckBoxIsSelected, boolean onlineSemanticIsSelected, String resultFileText, String resultFileName, boolean showDatesIsSelected, String inputPath) {
        boolean writeToFile = writeResultToFileIsSelected;
        boolean entities = showEntitiesIsSelected;
        if (dictionary == null || dictionary.dictionaryTable.size() == 0) {
            dictionary = Indexer.dictionary;
            if(dictionary.dictionaryTable.size() ==  0){
                AlertBox.display("", "No dictionary in memory, please load dictionary!");
                return;
            }
        }

        if(DocumentFileObject.getInstance().docsHolder == null || DocumentFileObject.getInstance().docsHolder.size() == 0){
            DocumentFileHandler documentFileHandler = new DocumentFileHandler();
            DocumentFileObject documentFileObject = DocumentFileObject.getInstance();
            try {
                documentFileObject.setInstance(documentFileHandler.extractDocsData(generateDocsFiles(stemCheckBoxIsSelected, GUI.outputPathTextField.getText())));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ArrayList<String> queryList = new ArrayList<>();
        queryList.add(query);
        Searcher searcher = new Searcher(similarWords, stemCheckBoxIsSelected, dictionary, generateStopWords(inputPath), queryList, entities, onlineSemanticIsSelected);
        ArrayList<DocumentDataToView>[] answer = searcher.search();
        showResultsWithoutIds(answer[0], showDatesIsSelected, showEntitiesIsSelected);
        if (writeToFile) {
            String qId = "000";
            String path = resultFileText + "\\" + resultFileName + ".txt";
            try {
                File toCreateFile = new File(path);
                if (toCreateFile.exists())
                    toCreateFile.delete();
                toCreateFile.createNewFile(); //create empty file
                FileWriter fw = new FileWriter(path, true);
                BufferedWriter bw = new BufferedWriter(fw);
                for (int i = 0; i < answer[0].size(); i++) {
                    bw.append(qId + " " + 0 + " " + answer[0].get(i).getDocNo() + " " + "1" + " " + "1.1" + " " + "mt" + "\n");
                    bw.newLine();
                }
                bw.close();
            } catch (Exception e) {
            }
        }

    }

    @SuppressWarnings("Duplicates")
    private static void showResultsWithoutIds(ArrayList<DocumentDataToView> answer, boolean showDateIsSelected, boolean showEntitiesIsSelected) {
        Stage stage = new Stage();
        TableView tableView = new TableView();

        TableColumn<String, DocumentDataToView> docNoCol = new TableColumn("DocNo");
        docNoCol.setCellValueFactory(new PropertyValueFactory<>("docNo"));
        TableColumn<String, DocumentDataToView> dateCol = new TableColumn("Doc Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<String, DocumentDataToView> entitiesCol = new TableColumn("Entities");
        entitiesCol.setCellValueFactory(new PropertyValueFactory<>("entities"));

        tableView.getColumns().add(docNoCol);
        if (showDateIsSelected)
            tableView.getColumns().add(dateCol);
        if (showEntitiesIsSelected)
            tableView.getColumns().add(entitiesCol);

        for (int i = 0; i < answer.size(); i++) {
            tableView.getItems().add(answer.get(i));
        }
        VBox vbox = new VBox(tableView);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("Duplicates")
    private static void showResultsWithIds(ArrayList<QueryIDDocDataToView> answer, boolean showDateIsSelected, boolean showEntitiesIsSelected) {
        Stage stage = new Stage();
        TableView tableView = new TableView();


        TableColumn<String, QueryIDDocDataToView> queryIdCol = new TableColumn("QueryId");
        queryIdCol.setCellValueFactory(new PropertyValueFactory<>("queryID"));
        TableColumn<String, QueryIDDocDataToView> docNoCol = new TableColumn("DocNo");
        docNoCol.setCellValueFactory(new PropertyValueFactory<>("docNo"));
        TableColumn<String, QueryIDDocDataToView> dateCol = new TableColumn("Doc Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<String, QueryIDDocDataToView> entitiesCol = new TableColumn("Entities");
        entitiesCol.setCellValueFactory(new PropertyValueFactory<>("entities"));

        tableView.getColumns().add(queryIdCol);
        tableView.getColumns().add(docNoCol);
        if (showDateIsSelected)
            tableView.getColumns().add(dateCol);
        if (showEntitiesIsSelected)
            tableView.getColumns().add(entitiesCol);

        for (int i = 0; i < answer.size(); i++) {
            tableView.getItems().add(answer.get(i));
        }
        VBox vbox = new VBox(tableView);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }

    private static HashSet<String> generateStopWords(String inputPath) {
        try {
            return ProgramStarter.readStopWords(inputPath + "\\05 stop_words");
        } catch (Exception e) {
            try {
                return ProgramStarter.readStopWords("data\\05 stop_words");
            } //default path
            catch (Exception ourE) {
                ourE.printStackTrace();
            }
        }
        return new HashSet<>();
    }

    private static ArrayList<String> generateDocsFiles(boolean stemIsSelected, String outputPath) {
        ArrayList<String> output = new ArrayList<>();
        String stemRelatedFolder = getStemRelatedFolderForDocFiles(stemIsSelected);

        for (int i = 0; i < 6; i++) {
            String docFilePath = outputPath + "\\" + stemRelatedFolder + "\\DocsFiles\\docFile" + i;
            output.add(docFilePath);
        }
        return output;
    }

    private static String getStemRelatedFolderForDocFiles(boolean selected) {
        if (selected)
            return "stemOur";
        return "noStemOur";
    }


    /**
     * loads doctionary from outputPath, according to stemming box
     *
     * @param outputPath
     */
    public static void loadDictionaryToMemory(String outputPath, boolean stemIsSelected) {
        try {
            DocumentFileHandler documentFileHandler = new DocumentFileHandler();
            DocumentFileObject documentFileObject = DocumentFileObject.getInstance();
            documentFileObject.setInstance(documentFileHandler.extractDocsData(generateDocsFiles(stemIsSelected, outputPath)));

            boolean isWithStemming = stemIsSelected;
            DictionaryFileHandler dfh = new DictionaryFileHandler(new Model.IndexerAndDictionary.Dictionary());
            dictionary = dfh.readFromFile(outputPath, isWithStemming);
            if(dictionary != null && dictionary.dictionaryTable.size() > 0 && documentFileObject.docsHolder != null && documentFileObject.docsHolder.size() > 0)
                AlertBox.display("Loaded", "Dictionary loaded!");
            else
                AlertBox.display("", "No dictionary file! or docs files!");

        } catch (Exception e) {
            AlertBox.display("", "No dictionary file!");
        }
    }

    /**
     * showing sorted dictionary
     */
    public static void showSortedDictionary() {
        Stage stage = new Stage();
        TableView tableView = new TableView();
        TableColumn<String, TermCounterPair> termCol = new TableColumn("Term");
        termCol.setCellValueFactory(new PropertyValueFactory<>("termStr"));
        TableColumn<Integer, TermCounterPair> countCol = new TableColumn("Count");
        countCol.setCellValueFactory(new PropertyValueFactory<>("count"));

        tableView.getColumns().add(termCol);
        tableView.getColumns().add(countCol);

        for (Map.Entry<Term, CountAndPointerDicValue> entry : dictionary.dictionaryTable.entrySet()) {
            tableView.getItems().add(new TermCounterPair(entry.getKey().getData(), entry.getValue().getTotalCount()));
        }


        VBox vbox = new VBox(tableView);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.show();
    }


    /**
     * delete info from dictionary if files exists
     *
     * @param outputPath
     */
    public static void reset(String outputPath) {
        File index = new File(outputPath);
        String[] entries = index.list();
        if (entries == null) {
            AlertBox.display("", "Nothing to delete!");
            return;
        }
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        if ((!deleteDir(new File(outputPath + "\\stemOur"))) && (!deleteDir(new File(outputPath + "\\noStemOur")))) {
            AlertBox.display("", "Nothing to delete!");
            return;
        }
        //index.delete();
        Parse.deleteStatics();
        Dictionary.deleteMutex();
        Indexer.deleteDictionary();
        dictionary = null;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /*
    /**
     * starts the processing
     *
     * @param inputPath
     * @param outputPath
     * @param toStemm
    private void startProgram(String inputPath, String outputPath, boolean toStemm) {
        ProgramStarter.startProgram(inputPath, outputPath, toStemm);
    }
    */

}