package Model.HandleSearch;

import Model.HandleSearch.DocDataHolders.DocRankData;
import Model.HandleSearch.DocDataHolders.DocumentDataToView;
import Model.IndexerAndDictionary.CountAndPointerDicValue;
import Model.IndexerAndDictionary.Dictionary;
import Model.OuputFiles.DocumentFile.DocumentFileObject;
import Model.OuputFiles.PostingFile.FindTermsData;
import Model.TermsAndDocs.Pairs.TermDocPair;
import Model.TermsAndDocs.Terms.CapsTerm;
import Model.TermsAndDocs.Terms.RegularTerm;
import Model.TermsAndDocs.Terms.Term;
import Model.TermsAndDocs.Terms.TermBuilder;
import com.medallia.word2vec.Word2VecModel;
import Model.HandleSearch.datamuse.DatamuseQuery;
import Model.HandleSearch.datamuse.JSONParse;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * this class is responsible for the return of all the relevant docs for a given query
 * this class uses the class Ranker for that purpose
 */
public class Searcher {
    private static Pattern stickPattern = Pattern.compile("[\\|]");
    private static Pattern escape = Pattern.compile("[ ]");
    private static Pattern splitByEntities = Pattern.compile("[E][N][T][I][T][I][E][S][:]");
    private static Pattern splitByDotCom = Pattern.compile("[\\;]");
    private static Pattern splitByBracket = Pattern.compile("[\\(]");
    private HashSet<String> stopWords;

    /**
     * Field mentioning if we should take into account the result of the semantic connection
     * between the query and the document. Probably will be decided by the user.
     * If it is false, only original query words will be taken into account.
     */
    private boolean isSemantic;
    private boolean isStemm;
    private Dictionary dictionary;
    private boolean withEntities;
    private ArrayList<String> queries;
    private boolean isOnline;


    public Searcher(boolean isSemantic, boolean isStemm, Dictionary dictionary, HashSet<String> stopWords
            , ArrayList<String> queries, boolean withEntities, boolean online) {
        this.isSemantic = isSemantic;
        this.isStemm = isStemm;
        this.dictionary = dictionary;
        this.stopWords = stopWords;
        this.withEntities = withEntities;
        this.queries = queries;
        this.isOnline=online;
    }

    /**
     * this method is responsible for the functionality of the class
     * it receives words and search for the documents which contains this term
     * then we calculate for each of the relevant docs it's rank
     * we are returning at most 50 relevant docs by order per query
     * @return
     */
    public ArrayList<DocumentDataToView>[] search(){
        long start = System.currentTimeMillis();
        double s =(double) start;
        ArrayList<DocumentDataToView> [] allAnswers = new ArrayList[queries.size()];
        ArrayList<TermDocPair> []allQueryTerms = new ArrayList[allAnswers.length];
        ArrayList<TermDocPair> []allSemanticTerms = new ArrayList[allAnswers.length];
        for(int i = 0; i < allAnswers.length; i++){
            allQueryTerms[i] = new ArrayList<>();
            allSemanticTerms[i] = new ArrayList<>();
        }

        for (int k = 0; k < allAnswers.length; k++) {
            String query = queries.get(k);
            ArrayList<String> queryL = splitBySpaceToArrayList(query);
            ArrayList<String> semanticallyCloseWords = new ArrayList<>();
            if(isSemantic)
                semanticallyCloseWords = getSemanticallyCloseWords(queryL,isOnline);
            //parsing the words of the query and semantically close words so they would fit to the dictionary && posting file terms
            allQueryTerms[k].addAll(parseQueryAndHeader(queryL, k));
            allSemanticTerms[k].addAll(parseQueryAndHeader(semanticallyCloseWords, k));
        }
        //returns two hash maps that contains the entire post data for each term in the queries or the similar words
        HashMap<Term, String> postDataForAllQueries = getPostData(allQueryTerms);
        HashMap<Term, String> postDataForAllSimilar = getPostData(allSemanticTerms);
        for (int k = 0; k < allAnswers.length; k++) {
            if(k > 0) {
                start = System.currentTimeMillis();
                s = (double) start;
            }
            //finding the posting data line for each term
            ArrayList<Pair<TermDocPair, String>> queryTermPostingData = findPostDataInHash(allQueryTerms[k], postDataForAllQueries);
            ArrayList<Pair<TermDocPair, String>> semanticTermPostingData = findPostDataInHash(allSemanticTerms[k], postDataForAllSimilar);

            //keeping all of the doc's relevant data for the ranker calculation
            HashMap<String, DocRankData> hashChecker = new HashMap<>();
            getDocsData(queryTermPostingData, hashChecker, 0);
            getDocsData(semanticTermPostingData, hashChecker, 1);

            //ranking every relevant doc
            ArrayList<Pair<String, Double>> keepScores = new ArrayList<>();
            Ranker ranker = new Ranker(this.isSemantic, isStemm);
            for (Map.Entry<String, DocRankData> entry : hashChecker.entrySet()){
                double score = ranker.rankDocument(entry.getValue());
                keepScores.add(new Pair<>(entry.getKey(), score));
            }
            Collections.sort(keepScores, new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            //keeping only the docNo and date of the best 50 docs
            ArrayList<DocumentDataToView> goodResults = new ArrayList<>();
            for (int i = 0; (i < 50) && (i < keepScores.size()) ; i++) {
                goodResults.add(new DocumentDataToView(keepScores.get(i).getKey()));
                String currentDocNo = goodResults.get(i).getDocNo();
                String currentDocDate = hashChecker.get(currentDocNo).getDocDate();
                goodResults.get(i).setDate(currentDocDate);
            }
            long end = System.currentTimeMillis();
            double e = (double) end;

            //adding top 5 entities for the user to view
            if(withEntities) {
                for (int i = 0; i < goodResults.size(); i++) {
                    ArrayList<Term> entities = fiveTopEntities(goodResults.get(i).getDocNo());
                    String strEntities = makeEntitiesString(entities);
                    goodResults.get(i).setEntities(strEntities);
                }
            }
            allAnswers[k] = goodResults;
        }

        return allAnswers;
    }

    /**
     * this method builds array list of the relevant terms and theirs post line data
     * @param allRelavantTerms
     * @param postDic
     * @return termsDocAndPost
     */
    private ArrayList<Pair<TermDocPair, String>> findPostDataInHash(ArrayList<TermDocPair> allRelavantTerms,
                                                                    HashMap<Term, String> postDic) {
        ArrayList<Pair<TermDocPair, String>> termsDocAndPost = new ArrayList<>();
        for(TermDocPair termDoc : allRelavantTerms){
            if(postDic.containsKey(termDoc.getTerm())){
                termsDocAndPost.add(new Pair<>(termDoc, postDic.get(termDoc.getTerm())));
            }
        }
        return termsDocAndPost;
    }

    /**
     * @param entities
     * @return all the entities in one string
     */
    private String makeEntitiesString(ArrayList<Term> entities) {
        String ans = "";
        for(Term t : entities){
            ans += t.getData() + ";  ";
        }
        if (entities.size() > 0)
            ans = ans.substring(0, ans.length() - 2);
        return ans;
    }

    /**
     * this method is filling every field inside the DocRankData class
     * by getting list of terms and their data from the posting file
     * and by finding the data of every doc from the doc's file
     * @param termPostingData
     * @return
     */
    private void getDocsData(ArrayList<Pair<TermDocPair, String>> termPostingData,
                             HashMap<String, DocRankData> hashChecker, int recognizer) {
        for (int p = 0; p < termPostingData.size(); p++) {
            Term currentTerm = termPostingData.get(p).getKey().getTerm();
            int appearInQuery = termPostingData.get(p).getKey().getCounter();
            String currentTermData = termPostingData.get(p).getValue();
            //finding df of current term
            ArrayList<Object> dfAndString = findDf(currentTermData);
            int termDf = (Integer) dfAndString.get(0);
            String containsNotDf = (String) dfAndString.get(1);

            //extracting docNo && tf
            String[] splitterTfDocNo = splitByBracket.split(containsNotDf);
            for(int k = 1; k < splitterTfDocNo.length; k++){
                //getting the docNo and the term Tf for this specific doc
                String[] docNoTfCurrent = findDocNoAndTf(splitterTfDocNo[k]);
                String currentDocNo = docNoTfCurrent[0];
                int termTf = Integer.parseInt(docNoTfCurrent[1]);

                //if it's the first time we get that doc we need to create instance of DocNecessaryData the keeps that doc data
                DocRankData currentDocData = hashChecker.get(currentDocNo);
                if(currentDocData == null && recognizer == 0){
                    //reading doc's line of data from the doc's file
                    String docData = DocumentFileObject.getInstance().docsHolder.get(currentDocNo);
                    String[] splitterData = splitByDotCom.split(docData);

                    //initializing doc's fields
                    currentDocData = new DocRankData(currentDocNo);
                    initializeDocNecessaryData(currentDocData, splitterData);
                    hashChecker.put(currentDocNo, currentDocData);
                }
                //adding info for the doc info holder in the hash about the current term
                if(recognizer == 0){
                    currentDocData.addQueryWordData(new Pair<>(currentTerm, appearInQuery), termTf, termDf);
                }
                else{
                    if(currentDocData != null)
                        currentDocData.addSimilarQueryWordData(new Pair<>(currentTerm, appearInQuery), termTf, termDf);
                }
            }
        }
    }

    /**
     * This func returns array containing the docNo and tf of the document
     * @return
     */
    private String[] findDocNoAndTf(String docNoTfCurrent) {
        String[] ans = new String[2];
        int i = 0;
        char ch = docNoTfCurrent.charAt(i);
        String docNo = "";
        while (ch != ';'){
            docNo += ch;
            i++;
            ch = docNoTfCurrent.charAt(i);
        }
        ans[0] = docNo;//docNo
        ans[1] =docNoTfCurrent.substring(i + 1, docNoTfCurrent.length() - 1); //string of Tf value
        return ans;
    }

    /**
     * if it's the first time we get that doc we need to create instance of DocNecessaryData the keeps that doc data
     * this method is responsible for initialize the values that aren't changing :
     * Header, Date, Size
     * @param currentDocData
     */
    private void initializeDocNecessaryData(DocRankData currentDocData, String[] splitter) {
        //set the size of doc
        currentDocData.setLengthOfDoc(Integer.parseInt(splitter[0]));
        //set num of unique terms
        currentDocData.setNumOfUniqTerms(Integer.parseInt(splitter[1]));
        //set most common term
        currentDocData.setMostCommonTerm(splitter[2]);
        //set most common term count
        currentDocData.setMaxTf(Integer.parseInt(splitter[3]));
        //set the date of the file
        currentDocData.setDocDate(splitter[4]);
        //set the header of doc - we need to parse the header in order to get additional hits in the Ranker
        String currentHeader = splitter[5];
        ArrayList<String> inputHeaderForParse = splitBySpaceToArrayList(currentHeader);
        ArrayList<Pair<Term, Integer>> headerToSet = new ArrayList<>();
        for(int i = 0; i < inputHeaderForParse.size(); i++){
            Term t;
            if(inputHeaderForParse.get(i).length() == 0)
                continue;
            if(inputHeaderForParse.get(i).charAt(0) >= 'A' && inputHeaderForParse.get(i).charAt(0) <= 'Z')
                t = new RegularTerm(inputHeaderForParse.get(i).toLowerCase());
            else
                t = new RegularTerm(inputHeaderForParse.get(i));
            headerToSet.add(new Pair<>(t, 1));
        }
        currentDocData.setDocHeaderStrings(headerToSet);
    }

    /**
     * this method is responsible for finding the df of given term by splitting it's line from the posting file
     * and returning relevant part of the line from the posting file
     * @return
     */
    private ArrayList<Object> findDf(String termPostingData) {
        //finding term DF
        int i = termPostingData.length() - 2;
        char ch = termPostingData.charAt(i);
        String dfStr = "";
        while (ch != '{'){
            dfStr = ch + dfStr;
            i--;
            ch = termPostingData.charAt(i);
        }
        ArrayList<Object> ans = new ArrayList<>();
        ans.add(Integer.parseInt(dfStr));
        ans.add(termPostingData.substring(0, i - 3));
        return ans;
    }

    /**
     * this method is responsible for creating array list of string from string header
     * by splitting the string by ' '
     * @param currentHeader
     * @return ArrayList<String>headerWords</String>
     */
    private ArrayList<String> splitBySpaceToArrayList(String currentHeader) {
        String[] splitter = escape.split(currentHeader);
        ArrayList<String> ans = new ArrayList<>();
        for(int i = 0; i < splitter.length; i++){
            ans.add(splitter[i]);
        }
        return ans;
    }

    /**
     * @param terms
     * @return HashMap that contains for each term it received it's post line data
     * (if exists !!!)
     */
    private HashMap<Term, String> getPostData(ArrayList<TermDocPair>[] terms) {
        HashMap<String, HashMap<Term, String>> pathDivide = new HashMap<>();
        HashMap<Term, String> result = new HashMap<>();
        ArrayList<Pair<Term, String>> tempResult = new ArrayList<>();

        for (int i = 0; i < terms.length; i++) {
            for (TermDocPair currentEntry : terms[i]){
                Term currentTerm = currentEntry.getTerm();
                CountAndPointerDicValue dicVal = dictionary.get(currentTerm);
                if(dicVal != null){
                    String path = dicVal.getPointer().getFileStr();
                    if(pathDivide.get(path) == null){
                        pathDivide.put(path, new HashMap<>());
                    }
                    HashMap<Term, String> listRequest = pathDivide.get(path);
                    if(currentTerm instanceof CapsTerm) {
                        listRequest.put(currentTerm, currentTerm.getData().toLowerCase());
                    }
                    else
                        listRequest.put(currentTerm, currentTerm.getData());
                }
                else if(currentTerm instanceof CapsTerm){
                    currentTerm = new RegularTerm(currentTerm.getData().toLowerCase());
                    currentEntry.setTerm(currentTerm);
                    dicVal = dictionary.get(currentTerm);
                    if(dicVal != null){
                        String path = dicVal.getPointer().getFileStr();
                        if(pathDivide.get(path) == null){
                            pathDivide.put(path, new HashMap<>());
                        }
                        HashMap<Term, String> listRequest = pathDivide.get(path);
                        listRequest.put(currentTerm, currentTerm.getData());
                    }
                }
            }
        }

        for (Map.Entry<String, HashMap<Term, String>> entry : pathDivide.entrySet()){
            HashMap<Term, String> termsInPost = entry.getValue();
            ArrayList<Pair<Term, String>> termsInPostToSort = new ArrayList<>();
            for(Map.Entry<Term, String> termAndString : termsInPost.entrySet()){
                termsInPostToSort.add(new Pair<>(termAndString.getKey(),termAndString.getValue()));
            }
            Collections.sort(termsInPostToSort, new Comparator<Pair<Term, String>>() {
                @Override
                public int compare(Pair<Term, String> o1, Pair<Term, String> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            FindTermsData findTermsData = new FindTermsData();
            tempResult.addAll(findTermsData.searchAllTermsInPostFile(entry.getKey(), termsInPostToSort));
        }
        for(Pair<Term, String> entry : tempResult){
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    /**
     * parsing the query's words so we'll get hit in the dictionary
     * @param query
     * @param k
     */
    public ArrayList<TermDocPair> parseQueryAndHeader(ArrayList<String> query, int k) {
        SearcherParse sp = new SearcherParse(this.stopWords, this.isStemm);
        HashMap<Term, TermDocPair> hash= sp.parseForSearcher(query, k);
        ArrayList<TermDocPair> queryTerms = new ArrayList<>();
        for (Map.Entry<Term, TermDocPair> entry : hash.entrySet()) {
            queryTerms.add(entry.getValue());
        }
        return queryTerms;
    }

    /**
     * Using online\offline methods to get similar words
     * (determined by {@code isSemanticOnline} field
     * @return ArrayList of similar words
     */
    public ArrayList<String> getSemanticallyCloseWords(ArrayList<String> query, boolean isOnline) {
        if (isOnline) {
            try {
                return getSemanticallyCloseWordsOnline(query);
            } catch (Exception e) {
                return getSemanticallyCloseWordsOffline(query);
            }
        }
        else
            return getSemanticallyCloseWordsOffline(query);
    }


    /**
     * Using Word2vecJava to get a list of similar words
     * *Using pre-trained model
     * *If unknown wors is found, we simply ignore it
     * @return ArrayList of similar words
     * @param query
     */
    private ArrayList<String> getSemanticallyCloseWordsOffline(ArrayList<String> query) {
        ArrayList<String> output = new ArrayList<>();
        try {
            Word2VecModel model = Word2VecModel.fromTextFile(new File("data\\model\\word2vec.c.output.model.txt"));
            com.medallia.word2vec.Searcher semanticSearcher = model.forSearch();
            int numOfResults = 3;
            for (int i = 0; i < query.size(); i++) {
                List<com.medallia.word2vec.Searcher.Match> matches = semanticSearcher.getMatches(query.get(i).toLowerCase(), numOfResults);
                for (com.medallia.word2vec.Searcher.Match match : matches) {
                    if (!match.match().equals(query.get(i)))
                        output.add(match.match());
                }
            }
        } catch (Exception e) {
        }
        finally {
            return output;
        }
    }

    /**
     * Using Model.HandleSearch.datamuse API to get a list of similar words
     *
     * @return ArrayList of similar words
     * @apiNote requires internet connection!
     * @param query
     */
    private ArrayList<String> getSemanticallyCloseWordsOnline(ArrayList<String> query) {
        ArrayList<String> output = new ArrayList<>();
        DatamuseQuery datamuseQuery = new DatamuseQuery();
        JSONParse jSONParse = new JSONParse();
        for (String word : query) {
            String initCloseWords = datamuseQuery.findSimilar(word);
            String[] parsedCloseWords = jSONParse.parseWords(initCloseWords);
            String[] useAbleCloseWords = new String[2];
            for(int i = 0; i < 2; i++){
                useAbleCloseWords[i] = parsedCloseWords[i];
            }
            addArrayToList(useAbleCloseWords, output);
        }
        return output;

    }

    /**
     * @param docNo
     * @return {@code ArrayList) of the five (if exists) most dominating entities in the doc
     */
    public ArrayList<Term> fiveTopEntities(String docNo) {
        //finding the doc's properties
        String docData = DocumentFileObject.getInstance().docsHolder.get(docNo);
        //gets all of the entities in a doc
        String[] splitter = splitByEntities.split(docData);
        if (splitter.length == 1)
            return new ArrayList<>();
        String strEntities = splitter[1];
        String[] mayEntitiesWithCount = splitByDotCom.split(strEntities);
        TermBuilder builder = new TermBuilder();
        HashMap<Term, Integer> realEntities = new HashMap<>();
        ArrayList<Term> topFive = new ArrayList<>();
        HashMap<String, Integer> mayEntries = new HashMap<>();
        for(int i = 0; i < mayEntitiesWithCount.length; i++){
            String currentUnited = mayEntitiesWithCount[i];
            String[] splited = stickPattern.split(currentUnited);
            int apperancesInDoc = Integer.parseInt(splited[1]);
            mayEntries.put(splited[0], apperancesInDoc);
        }
        //keeping only the right entities
        for (Map.Entry<String, Integer> entry : mayEntries.entrySet()) {
            Term t = builder.buildTerm("EntityTerm", entry.getKey());
            if (dictionary.contains(t)) {
                realEntities.put(t, entry.getValue());
                topFive.add(t);
            }
        }
        if (realEntities.size() <= 5) {
            return topFive;
        } else {
            ArrayList<Pair<Term,Double>> scores = calculateScores(realEntities, docNo);
            return extractBiggestScore(scores);
        }
    }
    /**
     * this method is responsible for returning the scores for all the entity terms in a document
     * score is calculated by : ((size of term (num of words)) * (number of appearances in the doc)) / log(appearances in corpus)
     * @param realEntities
     * @param docNo
     * @return array list of scores for each entity
     */
    private ArrayList<Pair<Term, Double>> calculateScores(HashMap<Term, Integer> realEntities, String docNo) {
        ArrayList<Pair<Term,Double>> scores = new ArrayList<>();
        for (Map.Entry<Term, Integer> entry : realEntities.entrySet()) {
            Term currentEntity = entry.getKey();
            String[] strEntitySize = escape.split(currentEntity.getData());

            int entitySize = strEntitySize.length;
            int appearancesInDoc = entry.getValue();
            int appearancesInCorpus = dictionary.get(currentEntity).getTotalCount();

            //calculating by formula
            double score = entitySize * appearancesInDoc;
            score = score / Math.log(appearancesInCorpus);
            scores.add(new Pair<>(currentEntity, score));
        }
        return scores;
    }

    /**
     * this method is responsible for extracting and returning
     * the biggest score of term from the scores and realEntities arrays
     * @param scores
     * @return term with the biggest score
     */
    private ArrayList<Term> extractBiggestScore(ArrayList<Pair<Term, Double>> scores) {
        Collections.sort(scores, new Comparator<Pair<Term, Double>>() {
            @Override
            public int compare(Pair<Term, Double> o1, Pair<Term, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        ArrayList<Term> ans = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            ans.add(scores.get(i).getKey());
        }
        return ans;
    }


    /**
     * function that adds all the strings of a given string array to the given list
     */
    private void addArrayToList(String[] parsedCloseWords, List<String> list) {
        for (String word : parsedCloseWords
        ) {
            list.add(word);
        }
    }

}