package Model.HandleSearch;

import Model.HandleParse.Parse;
import Model.TermsAndDocs.Docs.Document;
import Model.TermsAndDocs.Pairs.TermDocPair;
import Model.TermsAndDocs.Terms.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SearcherParse extends Parse {
    public SearcherParse(HashSet<String> stopWords, boolean toStem) {
        super(stopWords, toStem);
    }

    public HashMap<Term, TermDocPair> parseForSearcher(ArrayList<String> query, int k){
        String text =getTextFromQuery(query);
        text = deleteTitlesFunc(text);
        query = splitBySpaceToArrayList(text);
        HashMap<Term, TermDocPair> pairs = new HashMap<>();//output list
        ArrayList<String> initialWords = new ArrayList<>();
        for(String word : query){
            initialWords.add(word);
        }
        initialWords = handlePunctuation(initialWords);
        initialWords = deleteEmptyWords(initialWords);
        parseTextToList(initialWords, pairs, new Document(""+k, ""), "Query");
        return pairs;
    }

    private String getTextFromQuery(ArrayList<String> query) {
        String ans = "";
        for (String word : query){
            ans += word + " ";
        }
        return ans;
    }

}
