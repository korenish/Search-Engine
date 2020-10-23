package Model.IndexerAndDictionary;

import Model.TermsAndDocs.Terms.EntityTerm;
import Model.TermsAndDocs.Terms.Term;
import sun.awt.Mutex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * this class represent the dictionary of the search engine
 * contains Concurrent Hash Map of Terms, counter for each term, pointer for it's posting file
 */
public class Dictionary {

    public ConcurrentHashMap<Term, CountAndPointerDicValue> dictionaryTable;
    private static Mutex mutex = new Mutex();

    public Dictionary() {
        dictionaryTable = new ConcurrentHashMap<>();
    }

    /**
     * @param term
     * @return if the term is inside the dictionary
     */
    public boolean contains(Term term) {
        if (term==null)
            return false;
        return dictionaryTable.containsKey(term);
    }

    public static void deleteMutex()
    {
        mutex= null;
    }

    /**
     * @param term
     * adding new term to the dictionary (initializes with counter 0)
     */
    private void add(Term term)
    {
        dictionaryTable.put(term, new CountAndPointerDicValue());
    }


    /**
     * adds to the given term the given counter
     * if term doesnt exist, add it
     * @param currentTerm
     * @param currentPairCounter
     */
    public void add(Term currentTerm, int currentPairCounter) {
        if (!dictionaryTable.containsKey(currentTerm)) {
            this.add(currentTerm);
            CountAndPointerDicValue dicValue = dictionaryTable.get(currentTerm);
            dicValue.increaseCount(currentPairCounter);
        }
        else
        {
            int counterForEntities = 0;
            if (currentTerm instanceof EntityTerm) {
                mutex.lock();
                counterForEntities = dictionaryTable.get(currentTerm).getTotalCount();
                dictionaryTable.remove(currentTerm);
                add(currentTerm);
                mutex.unlock();
            }

            mutex.lock();
            CountAndPointerDicValue dicValue = dictionaryTable.get(currentTerm);
            dicValue.increaseCount(currentPairCounter+counterForEntities);
            mutex.unlock();
            System.out.print("");
        }
    }

    /**
     * adds to the given term the given counter and given path
     * if term doesnt exist, add it
     * @param currentTerm
     * @param countAndPointerDicValue
     */
    public void add(Term currentTerm, CountAndPointerDicValue countAndPointerDicValue){
        this.dictionaryTable.put(currentTerm, countAndPointerDicValue);
    }


    /**
     * @param term
     * removes given term from the dictionary
     */
    public void remove(Term term)
    {
        if(dictionaryTable.containsKey(term))
            dictionaryTable.remove(term);
    }

    /**
     * this method initialize the right pointers for the dictionary
     * @param outPath
     */
    public void initializePointers(String outPath, Boolean toStem) {
        for (Map.Entry<Term, CountAndPointerDicValue> entry : dictionaryTable.entrySet()) {
            PostingFilePointer pointer = entry.getValue().pointer;
            Term term = entry.getKey();
            pointer.findFileStr(term, outPath, toStem);
        }
    }

    /**
     * this method deletes every entity term that appeared only in one doc
     */
    public HashSet<String> deleteNotEntities() {
        HashSet<String> deletesEntities = new HashSet<>();
        for (Map.Entry<Term, CountAndPointerDicValue> entry : dictionaryTable.entrySet()) {
            Term term = entry.getKey();
            if (term instanceof EntityTerm) {
                if (!((EntityTerm) term).isEntity()) {
                    deletesEntities.add(term.getData());
                    dictionaryTable.remove(term);
                }
            }
        }
        return deletesEntities;
    }

    /**
     * @return the dictionary
     */
    public ConcurrentHashMap<Term, CountAndPointerDicValue> getDictionaryTable() {
        return dictionaryTable;
    }

    /**
     * @param t - Term
     * @return for a given term returns it's counter + pointer
     */
    public CountAndPointerDicValue get(Term t)
    {
        CountAndPointerDicValue countAndPointerDicValue = dictionaryTable.get(t);
        return countAndPointerDicValue;
    }


    public ArrayList<Term> sortedKeys(){
        ArrayList<Term> sortedKeys = new ArrayList<>();
        for (Map.Entry<Term, CountAndPointerDicValue> entry : dictionaryTable.entrySet()) {
            sortedKeys.add(entry.getKey());
        }
        Collections.sort(sortedKeys, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Term s1 = (Term) o1;
                Term s2 = (Term) o2;
                return s1.getData().compareTo(s2.getData());
            }
        });

        return sortedKeys;
    }

    /**
     * returns countStr
     * @return
     */
    public String getTermCountStr() {
        StringBuilder output = new StringBuilder();
        ArrayList<Term> sortedKeys = sortedKeys();
        for (Term key : sortedKeys)
        {
            output.append(key.getData());
            output.append(" : ");
            output.append(key.getType());
            output.append(": counter = ");
            CountAndPointerDicValue countAndPointerDicValue = dictionaryTable.get(key);
            output.append(countAndPointerDicValue.totalCount);
            output.append("\n");
        }
        return output.toString();
    }

}
