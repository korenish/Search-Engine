package Model.HandleParse;

import Model.HandleParse.DataConfiguration.Stemmer;
import Model.HandleParse.DataConfiguration.eNumMonths;
import Model.TermsAndDocs.Docs.Document;
import Model.TermsAndDocs.Pairs.TermDocPair;
import Model.TermsAndDocs.Terms.Term;
import Model.TermsAndDocs.Terms.TermBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class represents a parses that fills the 'terms' list for a given document ('parse' method)
 */
public class Parse {
    protected static Pattern splitBySigns = Pattern.compile("[\\|\\:\\;\\)\\(\\?\\{\\}\\`\\+\\_\\&\\^\"\\!*\\[\\]]|['][s]");
    protected static Pattern notNumbers = Pattern.compile("[^0-9]+");
    protected static Pattern numbers = Pattern.compile("[-]?[0-9]+|[-]?[0-9]+[\\.][0-9]+");
    protected static Pattern words = Pattern.compile("[a-zA-Z]+[-]?[a-zA-Z]+");
    protected static Pattern hyphenControl = Pattern.compile("[-]+");
    protected static Pattern zitata = Pattern.compile("[']");
    protected static Pattern slash = Pattern.compile("[/]+");
    protected static Pattern splitDots = Pattern.compile("[\\.]");
    protected static Pattern deleteTitles = Pattern.compile("[<][^>]*[>]");
    protected static Pattern splitSpaces = Pattern.compile("[\\s]");
    protected static Pattern numOrChar = Pattern.compile("[0-9a-zA-Z]");
    protected static Pattern dotZero = Pattern.compile("[0-9]+[\\.][0]");
    protected static HashSet<String> monthsAndShortMonths = new HashSet<>(Arrays.asList
            ("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December",
                    "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER",
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
                    "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"));
    protected static HashSet<String> currencies = new HashSet<>(Arrays.asList("dollars", "Dollars", "euro", "yuan", "yen", "pounds", "peso", "rupee", "ruble",
            "franc", "won", "lira", "krone", "zloty", "baht"));
    protected static Pattern commas = Pattern.compile("[\\,]");
    protected static  HashSet<String> unitsOfMeasurement = new HashSet<>(Arrays.asList("meters", "meter", "kilometers", "kilometer", "km", "KM", "liter", "liters", "milliliters", "milliliter", "mile", "miles"));
    protected TermBuilder termBuilder;
    protected String _EntityTermClass;
    protected String _DocumentDateTermClass;
    protected String _CapsTermClass;
    protected String _ExpressionTermClass;
    protected String _DateTermClass;
    protected String _MeasurementTermClass;
    protected String _NumericTermClass;
    protected String _RegularTermClass;
    protected String _PercentageTermClass;
    protected String _PriceTermClass;
    protected HashSet<String> stopWords;
    protected String _DocText;
    protected boolean toStem;

    public Parse(HashSet<String> stopWords, boolean toStem) {
        this.termBuilder = new TermBuilder();
        this._DocText = "text";
        this._EntityTermClass = "EntityTerm";
        this._DocumentDateTermClass = "DocumentDateTerm";
        this._CapsTermClass = "CapsTerm";
        this._ExpressionTermClass = "ExpressionTerm";
        this._DateTermClass = "DateTerm";
        this._MeasurementTermClass = "MeasurementTerm";
        this._NumericTermClass = "NumericTerm";
        this._RegularTermClass = "RegularTerm";
        this._PercentageTermClass = "PercentageTerm";
        this._PriceTermClass = "PriceTerm";
        this.toStem = toStem;

        this.stopWords = stopWords;
    }

    public static void deleteStatics() {
        splitBySigns = null;
        notNumbers = null;
        numbers = null;
        words  = null;
        hyphenControl  = null;
        zitata  = null;
        slash  = null;
        splitDots  = null;
        deleteTitles  = null;
        splitSpaces  = null;
        numOrChar  = null;
        dotZero  = null;
        monthsAndShortMonths = null;
        currencies=null;
        commas=null;
        unitsOfMeasurement = null;
    }

    /**
     * GloveTrainedFilesUsage method of the class - parses documents to terms
     *
     * @param document
     * @return ArrayList of termDocPairs
     */
    public HashMap<Term, TermDocPair> parseDocument(Document document) {
        HashMap<Term, TermDocPair> pairs = new HashMap<>();//output list

        //text parsing
        String text = document.getText();
        text = deleteTitlesFunc(text);
        document.deleteText(); //saving ram
        ArrayList<String> initialWords = splitBySpaceToArrayList(text);
        initialWords = handlePunctuation(initialWords);
        initialWords = deleteEmptyWords(initialWords);
        parseTextToList(initialWords, pairs, document, _DocText);

        //add date pair
        String date = document.getDate();
        initialWords = splitBySpaceToArrayList(date);
        initialWords = handlePunctuation(initialWords);
        initialWords = deleteEmptyWords(initialWords);
        parseDocumentDateToList(initialWords, pairs, document);

        //System.out.println(pairs);

        //return output hash of pairs
        return pairs;
    }


    /**
     * delets punctuation from end of word and all the commas inside
     *
     * @param terms the arraylist of terms
     */
    @SuppressWarnings("Duplicates")
    protected ArrayList<String> handlePunctuation(ArrayList<String> terms) {
        if (terms.size() == 0) {
            return terms;
        }
        StringBuilder helpTerm = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            String[] spliter = splitBySigns.split(terms.get(i));
            for (int j = 0; j < spliter.length; j++) {
                helpTerm.append(spliter[j]);
            }
            spliter = zitata.split(helpTerm.toString());// removes '
            helpTerm.delete(0, helpTerm.length());
            for (int j = 0; j < spliter.length; j++) {
                helpTerm.append(spliter[j]);
            }
            Matcher matcher = notNumbers.matcher(helpTerm);
            if (matcher.matches()) {
                //removes all dots
                spliter = splitDots.split(helpTerm);
                helpTerm.delete(0, helpTerm.length());
                for (int j = 0; j < spliter.length; j++) {
                    helpTerm.append(spliter[j]);
                }
                //removes all commas
                spliter = commas.split(helpTerm);
                helpTerm.delete(0, helpTerm.length());
                for (int j = 0; j < spliter.length; j++) {
                    helpTerm.append(spliter[j]);
                }
            }
            //replace -* to only one - between words
            matcher = hyphenControl.matcher(helpTerm);
            String d = matcher.replaceAll("-");
            helpTerm.delete(0, helpTerm.length());
            helpTerm.append(d);
            while (helpTerm.length() > 0 && (helpTerm.toString()).charAt(helpTerm.length() - 1) == '.')//remove dots from the end to everyone
            {
                helpTerm.delete(helpTerm.length() - 1, helpTerm.length());
            }
            String termVal = helpTerm.toString();
            if (termVal.length() > 0 && termVal.charAt(termVal.length() - 1) == ',')//removes end ,
            {
                termVal = termVal.substring(0, termVal.length() - 1);
            }
            terms.set(i, termVal);
            helpTerm.delete(0, helpTerm.length());
        }
        ArrayList<String> newTerms = handleHypen(terms);
        return newTerms;
    }

    /**
     * //     * @param text
     * //     * @return deletes all <*> </*> from the doc text
     */
    protected static String deleteTitlesFunc(String text) {
        StringBuilder buildNewText = new StringBuilder();
        String[] noTitles = deleteTitles.split(text);
        for (int i = 0; i < noTitles.length; i++) {
            buildNewText.append(noTitles[i]);
        }
        text = buildNewText.toString();
        return text;
    }


    /**
     * this method units separated terms with '-' between them
     *
     * @param terms
     * @return updated terms list
     */
    @SuppressWarnings("Duplicates")
    protected ArrayList<String> handleHypen(ArrayList<String> terms) {
        ArrayList<String> newTerms = new ArrayList<>();
        StringBuilder appender = new StringBuilder();
        for (int i = 0; i < terms.size(); i++) {
            String next = null;
            if (i < terms.size() - 1)
                next = terms.get(i + 1);
            String currnent = terms.get(i);
            String prev = null;
            if (newTerms.size() > 0)
                prev = newTerms.get(newTerms.size() - 1);
            if (currnent.length() > 0 && currnent.charAt(0) == '-' && prev != null && !isNumericIgnoreCommas(currnent)) {
                if (prev.length() > 0 && prev.charAt(prev.length() - 1) == '-')
                    prev = prev.substring(0, prev.length() - 1);
                appender.append(prev);
                appender.append(currnent);
                newTerms.remove(newTerms.size() - 1);
            } else {
                if (currnent.length() > 0 && currnent.charAt(0) == '-' && (!isNumericIgnoreCommas(currnent)))
                    currnent = currnent.substring(1);
                appender.append(currnent);
            }
            if (currnent.length() > 0 && currnent.charAt(currnent.length() - 1) == '-' && next != null) {
                if (next.length() > 0 && next.charAt(0) == '-' && !isNumericIgnoreCommas(next))
                    next = next.substring(1);
                appender.append(next);
                terms.set(i + 1, appender.toString());
            } else {
                if (currnent.length() > 0 && currnent.charAt(currnent.length() - 1) == '-')
                    appender.delete(appender.length() - 1, appender.length());
                newTerms.add(appender.toString());
            }
            appender.delete(0, appender.length());
        }
        return newTerms;
    }

    /**
     * this method parses the document's date to a string, by moving evety date to the same format so they can be compare later
     * @param initialWords
     * @param target
     * @param document
     */
    private void parseDocumentDateToList(ArrayList<String> initialWords, HashMap<Term, TermDocPair> target, Document document) {
        {
            String month = "";
            String year = "";
            if (initialWords.size() == 2) {
                if (isMonth(initialWords.get(0))) {
                    month = initialWords.get(0);
                    year = initialWords.get(1);
                }
                if (isMonth(initialWords.get(1))) {
                    month = initialWords.get(1);
                    year = initialWords.get(0);
                }
                if (year.length() == 4)
                    addDateTermToList(target, document, month, year.substring(2));
                else
                    addDateTermToList(target, document, month, year);
            }
            if (initialWords.size() == 3) {
                if (isMonth(initialWords.get(0))) {
                    month = initialWords.get(0);
                    if (getNumericIgnoreCommas(initialWords.get(1)) >= getNumericIgnoreCommas(initialWords.get(2)))
                        year = initialWords.get(1); //assuming larger is the year
                    else
                        year = initialWords.get(2);
                }
                if (isMonth(initialWords.get(1))) {
                    month = initialWords.get(1);
                    if (getNumericIgnoreCommas(initialWords.get(0)) >= getNumericIgnoreCommas(initialWords.get(2)))
                        year = initialWords.get(0); //assuming larger is the year
                    else
                        year = initialWords.get(2);
                }
                if (isMonth(initialWords.get(2))) {
                    month = initialWords.get(2);
                    if (getNumericIgnoreCommas(initialWords.get(0)) >= getNumericIgnoreCommas(initialWords.get(1)))
                        year = initialWords.get(0); //assuming larger is the year
                    else
                        year = initialWords.get(1);
                }
                if (year.length() == 2)
                    addDateTermToList(target, document, month, year);
                if (year.length() == 4)
                    addDateTermToList(target, document, month, year.substring(2));
            }
        }
    }

    protected void addDateTermToList(HashMap<Term, TermDocPair> target, Document document, String month, String year) {
        StringBuilder finalDate;
        month = month.toLowerCase();
        month = eNumMonths.valueOf(month).toString();
        finalDate = new StringBuilder();
        finalDate.append(month);
        finalDate.append("-");
        finalDate.append(year);
        Term dateTerm = termBuilder.buildTerm(_DocumentDateTermClass, finalDate.toString());
        TermDocPair pair = new TermDocPair(dateTerm, document);
        target.put(dateTerm, pair);
    }

    protected void parseTextToList(ArrayList<String> initialWords, HashMap<Term, TermDocPair> target, Document document, String termType) {
        for (int i = 0; i < initialWords.size(); i++) {
            boolean wasProccessed = false;
            String current = initialWords.get(i); //saving current word

            //setting 3 next words to the current if existing
            String[] closeStr = initFunToParse(initialWords, i);
            String next = closeStr[0];
            String secondNext = closeStr[1];
            String thirdNext = closeStr[2];

            boolean currentNumeric = false;
            boolean nextNumeric = false;
            boolean secondNextNumeric = false;
            if (isNumericIgnoreCommas(current))
                currentNumeric = true;
            if (isNumericIgnoreCommas(next))
                nextNumeric = true;
            boolean isWord = words.matcher(current).matches();
            String currentLowerCase = current.toLowerCase();

            if (isDate(current, next, currentNumeric, nextNumeric)) {
                Term dateTerm = handleDate(current, next, currentNumeric, nextNumeric, currentLowerCase);
                TermDocPair termDocPair = new TermDocPair(dateTerm, document);
                addToPairs(termDocPair, target, termType);
                i++;
                continue;
            }

            if (isNumericIgnoreCommas(secondNext))
                secondNextNumeric = true;

            if (isBetween(current)) {
                StringBuilder appender = new StringBuilder();
                if (nextNumeric && secondNextNumeric) {
                    appender.append(current);
                    appender.append(" ");
                    appender.append(next);
                    appender.append(" ");
                    appender.append(secondNext);
                    Term term;
                    term = termBuilder.buildTerm(_ExpressionTermClass, appender.toString());
                    addToPairs(new TermDocPair(term, document), target, termType);
                    i = i + 2;
                    continue;
                }
            }

            if (isExpression(current)) {
                String[] spliter = hyphenControl.split(current);
                addExpressions(target, spliter, document, termType);
                wasProccessed = true;
            }

            if (startsWithCapital(current) && isWord) //handling sequence words starts with capitals
            {
                //parsing big caps sequence
                int k = handleCapitalLetterSequence(current, i, document, initialWords, target, termType); //pair for each term we want to make
                if (k > i + 1) {
                    if (wasProccessed) {
                        Term del = termBuilder.buildTerm(_ExpressionTermClass, current);
                        target.remove(del);
                    }
                    i = k - 1;
                    continue;
                }
            }

            if (stopWords.contains(currentLowerCase) || wasProccessed) { //prevents stop word or exp term to be processed
                continue;
            }

            //parsing big capital term:
            if (startsWithCapital(current) && isWord) {
                String toStemStr = currentLowerCase;
                if (this.toStem) {
                    toStemStr = stemmStr(toStemStr);
                }
                toStemStr = toStemStr.toUpperCase();
                Term currentCapsTerm = termBuilder.buildTerm(_CapsTermClass, toStemStr);
                addToPairs(new TermDocPair(currentCapsTerm, document), target, termType);
                continue;
            }

            if (!isWord) {
                if (isPercent(current, next, currentNumeric)) { //handling percent
                    Term percentTerm = handlePercent(current, termType);
                    percentTerm.setData(percentTerm.getData().substring(0, percentTerm.getData().length() - 1));
                    tryRemoveDotZero(percentTerm);
                    percentTerm.setData(percentTerm.getData() + "%");
                    addToPairs(new TermDocPair(percentTerm, document), target, termType);
                    if (current.charAt(0) != '%') {
                        i++;
                    }
                    continue;
                }

                if (isMesurment(current, next, currentNumeric)) {
                    Term mesurmentTerm = handleMesurment(current, next, termType);
                    tryRemoveDotZero(mesurmentTerm);
                    addToPairs(new TermDocPair(mesurmentTerm, document), target, termType);
                    i++;
                    continue;
                }

                if (isPrice(current, next, secondNext, thirdNext, currentNumeric)) {
                    Term priceTerm = handlePrice(current, next, secondNext, thirdNext, termType, currentNumeric, nextNumeric);
                    tryRemoveDotZero(priceTerm);
                    addToPairs(new TermDocPair(priceTerm, document), target, termType);
                    if ((next.equals("million") || next.equals("billion") || next.equals("trillion")) && secondNext.equals("US") &&
                            thirdNext.equals("dollars")) {
                        i = i + 3;
                    } else if (isFraction(next) || next.equals("Dollars") || next.equals("million") || next.equals("billion")) {
                        i++; //don't parse fraction already parsed
                    } else if ((next.equals("m") || next.equals("bn")) && secondNext.equals("Dollars")) {
                        i = i + 2;
                    }
                    continue;
                }

                if (currentNumeric) {
                    //handling regular numbers (must be in the end of all numbers options!)
                    double number = getNumericIgnoreCommas(current);
                    Term numberTerm = handleNumber(number, next);
                    tryRemoveDotZero(numberTerm);
                    addToPairs(new TermDocPair(numberTerm, document), target, termType);
                    if (isFraction(next) || next.equals("Thousand") || next.equals("Million") || next.equals("Billion")) {
                        i++; //don't parse fraction / words already used
                    }
                    if (isFraction(next) && secondNext.equals("Thousand") || secondNext.equals("Million") || secondNext.equals("Billion")) {
                        i++; //dont parse word used after fraction
                    }
                    continue;
                }

            }
            //regular terms:
            if (isWord) {
                String regularWord = currentLowerCase;
                if (toStem)
                    regularWord = stemmStr(regularWord);//stemming regular words
                addToPairs(new TermDocPair(termBuilder.buildTerm(_RegularTermClass, regularWord), document), target, termType);
            }
        }//end for
    }

    /**
     * @param initialWords
     * @param i
     * @return the 3 next strings of a term in the list (if existing)
     */
    protected String[] initFunToParse(ArrayList<String> initialWords, int i) {
        String next = "";
        String secondNext = "";
        String thirdNext = "";
        if (i < (initialWords.size() - 1))
            next = initialWords.get(i + 1);
        if (i < (initialWords.size() - 2))
            secondNext = initialWords.get(i + 2);
        if (i < (initialWords.size() - 3))
            thirdNext = initialWords.get(i + 3);
        String[] ans = new String[3];
        ans[0] = next;
        ans[1] = secondNext;
        ans[2] = thirdNext;
        return ans;
    }

    protected boolean isBetween(String current) {
        return (current.equals("between") || current.equals("Between"));
    }

    protected boolean isExpression(String current) {
        return hyphenControl.matcher(current).find();
    }

    /**
     * this method stems the word it's getting as parameter
     *
     * @param toStem
     * @return string after stemming
     */
    protected String stemmStr(String toStem) {
        Stemmer stemm = new Stemmer();
        stemm.add(toStem.toCharArray(), toStem.length());
        stemm.stem();
        return (stemm.toString());
    }

    /**
     * this method add pairs with '-' between them
     *
     * @param pairs
     * @param spliter
     * @param document
     * @param termType
     */
    @SuppressWarnings("Duplicates")
    protected void addExpressions(HashMap<Term, TermDocPair> pairs, String[] spliter, Document document, String termType) {
        //handling cases of word-word, num-word, word-num, num-num, word-word-word
        String save;
        save = _ExpressionTermClass;
        TermDocPair termDocPair;
        termDocPair = new TermDocPair((termBuilder.buildTerm(_ExpressionTermClass, "")), document);

        if (isNumericIgnoreCommas(spliter[0])) {
            //handling num-word, num-num
            if (isNumericIgnoreCommas(spliter[1])) {
                termDocPair.setTerm(termBuilder.buildTerm(save, spliter[0] + "-" + spliter[1]));
                addToPairs(termDocPair, pairs, termType);//num-num
            } else if (words.matcher(spliter[1]).matches()) {
                termDocPair.setTerm(termBuilder.buildTerm(save, spliter[0] + "-" + spliter[1]));
                addToPairs(termDocPair, pairs, termType);//num-word
            }
        } else if (words.matcher(spliter[0]).matches()) {
            //handling word-word, word-num, word-word-word
            if (isNumericIgnoreCommas(spliter[1])) {
                termDocPair.setTerm(termBuilder.buildTerm(save, spliter[0] + "-" + spliter[1]));
                addToPairs(termDocPair, pairs, termType);//word-num
            } else if (words.matcher(spliter[1]).matches()) {
                if (spliter.length > 2) {
                    if (words.matcher(spliter[2]).matches()) {
                        termDocPair.setTerm(termBuilder.buildTerm(save, spliter[0] + "-" + spliter[1] + "-" + spliter[2]));
                        addToPairs(termDocPair, pairs, termType);//word-word-word
                    }
                } else {
                    termDocPair.setTerm(termBuilder.buildTerm(save, spliter[0] + "-" + spliter[1]));
                    addToPairs(termDocPair, pairs, termType);//word-word
                }
            }
        }
    }

    /**
     * this method updates the hash map of the pairs
     *
     * @param termDocPair
     * @param pairs
     */
    protected void addToPairs(TermDocPair termDocPair, HashMap<Term, TermDocPair> pairs, String termType) {
        Term term = termDocPair.getTerm();
        if (pairs.containsKey(term)) {
            pairs.get(term).incrementCounter();//add to counter for existing pair
        } else {
            pairs.put(term, termDocPair);//add new pair to the hash
        }
    }

    /**
     * @param current
     * @return true if the term start with capital letter, else false
     */
    protected boolean startsWithCapital(String current) {
        if (current.length() > 0 && current.charAt(0) >= 'A' && current.charAt(0) <= 'Z') {
            return true;
        }
        return false;
    }

    /**
     * handles the law of sequence of capital letters
     *
     * @param current
     * @param i
     * @param target
     * @param termType
     * @return arrayList of entities
     */
    @SuppressWarnings("Duplicates")
    protected int handleCapitalLetterSequence(String current, int i, Document document, ArrayList<String> lWords, HashMap<Term, TermDocPair> target, String termType) {
        StringBuilder appender = new StringBuilder();
        String next = current;
        int prog = i;
        while (prog < lWords.size() && startsWithCapital(next)) {
            appender.append(next);
            appender.append(" ");
            prog++;
            if (prog < lWords.size())
                next = lWords.get(prog);
        }
        if (prog > i + 1) {
            appender.delete(appender.length() - 1, appender.length());
            Term sTerm = termBuilder.buildTerm(_EntityTermClass, appender.toString());
            TermDocPair termDocPair = new TermDocPair(sTerm, document);
            addToPairs(termDocPair, target, termType);
        }
        return prog;
    }


    /**
     * handles mesurment
     *
     * @return term with currect data
     */
    protected Term handleMesurment(String current, String next, String termType) {
        String data = (current + " " + next);
        return termBuilder.buildTerm(_MeasurementTermClass, data);
    }

    /**
     * check if this is a numeric followed by unit of mesurment
     *
     * @param current
     * @param next
     * @param currentNumeric
     * @return
     */
    protected boolean isMesurment(String current, String next, boolean currentNumeric) {
        return (currentNumeric && unitsOfMeasurement.contains(next));
    }

    /**
     * this method checks if a given term has unwanted .0, if he does it removes it
     *
     * @param term
     * @return true if found and removed, else false
     */
    protected boolean tryRemoveDotZero(Term term) {
        if (dotZero.matcher(term.getData()).matches()) //if has unwanted ".0" inside
        {
            //build the new string
            term.setData(term.getData().substring(0, term.getData().length() - 2));
            return true;
        }
        return false;
    }

    /**
     * handles date
     *
     * @return term with currect data
     */
    protected Term handleDate(String current, String next, boolean currentNumeric, boolean nextNumeric, String currentLower) {
        String output = "";

        //month year
        if (nextNumeric && next.length() == 4) {
            output = next + "-" + eNumMonths.valueOf(currentLower);
            return termBuilder.buildTerm(_DateTermClass, output);
        }

        //DD month
        if (currentNumeric) {
            if (current.length() == 1)
                output = eNumMonths.valueOf(next.toLowerCase()) + "-0" + current;
            else
                output = eNumMonths.valueOf(next.toLowerCase()) + "-" + current;
            return termBuilder.buildTerm(_DateTermClass, output);
        }

        //month DD
        if (next.length() == 1)
            output = eNumMonths.valueOf(currentLower) + "-0" + next;
        else
            output = eNumMonths.valueOf(currentLower) + "-" + next;
        return termBuilder.buildTerm(_DateTermClass, output);
    }

    /**
     * @param current
     * @param next
     * @param currentNumeric
     * @param nextNumeric
     * @return true if current word reprresents a date
     */
    protected boolean isDate(String current, String next, boolean currentNumeric, boolean nextNumeric) {
        if (currentNumeric) {
            double temp = getNumericIgnoreCommas(current);
            int currentNum = (int) temp;
            if (temp == currentNum) {
                if ((currentNum >= 1) && (currentNum <= 31) && (isMonth(next))) {
                    return true;
                }
            }
        }
        else if (nextNumeric) {
            double temp = getNumericIgnoreCommas(next);
            int nextNum = (int) temp;
            if (temp == nextNum) {
                if (isMonth(current)) {
                    if (((nextNum >= 1) && (nextNum <= 31)) || ((nextNum >= 1000) && (nextNum <= 2050)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * check if given string is a month or 3 first letters of a month
     *
     * @param str
     * @return
     */
    protected boolean isMonth(String str) {
        if (monthsAndShortMonths.contains(str))
            return true;
        return false;
    }

    /**
     * @param terms
     * @return updated list of terms that's not contained "" or terms without numbers or letters
     */
    @SuppressWarnings("Duplicates")
    protected static ArrayList deleteEmptyWords(ArrayList<String> terms) {
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            if (terms.get(i).length() > 0 && numOrChar.matcher(terms.get(i)).find()) {
                newList.add(terms.get(i));
            }
        }
        return newList;
    }

    /**
     * handles price
     *
     * @return term with currect data
     */
    protected Term handlePrice(String current, String next, String secondNext, String thirdNext, String termType, boolean currentNumeric, boolean nextNumeric) {
        String outputToken = "";
        Double numDouble = null;

        //initialize substrings
        String substring2 = "";
        String substring1 = "";
        if (current.length() > 2)
            substring2 = current.substring(0, current.length() - 2);
        if (current.length() > 1)
            substring1 = current.substring(0, current.length() - 1);

        //starts with $
        if (current.startsWith("$")) {
            numDouble = getNumericIgnoreCommas(current.substring(1));
            if (numDouble != null && numDouble >= 1000000 || next.equals("million") || next.equals("billion")) //above million
            {
                if (!next.equals("million")) {
                    if (next.equals("billion"))
                        numDouble = numDouble * 1000;
                    else { //just a number
                        numDouble = numDouble / 1000000;
                        numDouble = round(numDouble, 3);
                    }
                }
                int intNum = numDouble.intValue();
                if (numDouble == intNum)
                    outputToken = intNum + " M Dollars";//remove .0
                else {
                    outputToken = numDouble + " M Dollars";
                }
            } else //under million
            {
                Term term = termBuilder.buildTerm(_PriceTermClass, current.substring(1));
                tryRemoveDotZero(term);
                outputToken = term.getData() + " Dollars";//removes .0
            }
            return termBuilder.buildTerm(_PriceTermClass, outputToken);
        }

        //Price something U.S. dollars (for sure above M)
        if (secondNext.equals("US") && thirdNext.equals("dollars")) {
            numDouble = getNumericIgnoreCommas(current);
            if (next.equals("million")) {
                int intNum = numDouble.intValue();
                if (numDouble == intNum)
                    outputToken = intNum + " M Dollars";//remove .0
                else {
                    outputToken = numDouble + " M Dollars";
                }
            }
            if (next.equals("billion")) {
                numDouble = numDouble * 1000;
                int intNum = numDouble.intValue();
                if (numDouble == intNum)
                    outputToken = intNum + " M Dollars";//remove .0
                else {
                    outputToken = numDouble + " M Dollars";
                }
            }
            if (next.equals("trillion")) {
                numDouble = numDouble * 1000000;
                int intNum = numDouble.intValue();
                if (numDouble == intNum)
                    outputToken = intNum + " M Dollars";//remove .0
                else {
                    outputToken = numDouble + " M Dollars";
                }
            }
            return termBuilder.buildTerm(_PriceTermClass, outputToken);
        }

        //numeric currency
        if (currentNumeric && currencies.contains(next)) {
            numDouble = getNumericIgnoreCommas(current);
            if (numDouble >= 1000000) //above million
            {
                numDouble = numDouble / 1000000;
                numDouble = round(numDouble, 3);
                int intNum = numDouble.intValue();
                if (numDouble == intNum)
                    outputToken = intNum + "";//remove .0
                else {
                    outputToken = numDouble + "";
                }
                outputToken += " M " + next;
                return termBuilder.buildTerm(_PriceTermClass, outputToken);
            } else {
                int intNum = numDouble.intValue();
                if (numDouble == intNum)
                    outputToken = intNum + "";//remove .0
                else {
                    outputToken = numDouble + "";
                }
                outputToken += " " + next;
                return termBuilder.buildTerm(_PriceTermClass, outputToken);
            }
        }

        //numeric fraction currency
        if (currentNumeric && isFraction(next) && currencies.contains(secondNext)) {
            numDouble = getNumericIgnoreCommas(current);
            int intNum = numDouble.intValue();
            if (numDouble == intNum)//remove .0
                outputToken = intNum + "";//remove .0
            else {
                outputToken = numDouble + "";
            }
            outputToken = outputToken + " " + next + " " + secondNext;
            return termBuilder.buildTerm(_PriceTermClass, outputToken);
        }

        //price m/price bn currency (for sure above M)
        if (currentNumeric || (isNumericIgnoreCommas(substring1) || isNumericIgnoreCommas(substring2))) {
            if (current.endsWith("m"))
                numDouble = getNumericIgnoreCommas(substring1);
            if (current.endsWith("bn"))
                numDouble = getNumericIgnoreCommas(substring2) * 1000;
            int intNum = numDouble.intValue();
            if (numDouble == intNum)
                outputToken = intNum + "";//remove .0
            else {
                outputToken = numDouble + "";
            }
            outputToken += " M " + next;
            return termBuilder.buildTerm(_PriceTermClass, outputToken);
        }
        return null;
    }

    /**
     * checks if string given should be a price token (only dollars supported)
     *
     * @param current
     * @param next
     * @param secondNext
     * @param thirdNext
     * @param currentNumeric
     * @return true if and only if current represents price
     */
    protected boolean isPrice(String current, String next, String secondNext, String thirdNext, boolean currentNumeric) {

        //initialize substrings
        String substring2 = "";
        String substring1 = "";
        if (current.length() > 2 && current.endsWith("bn"))
            substring2 = current.substring(0, current.length() - 2);
        if (current.length() > 1 && current.endsWith("m"))
            substring1 = current.substring(0, current.length() - 1);

        //check conditions
        if ((current.startsWith("$") && isNumericIgnoreCommas(current.substring(1))) //$price
                || (currentNumeric && currencies.contains(next))//number currency
                || ((isNumericIgnoreCommas(substring1) || isNumericIgnoreCommas(substring2)) && ((current.endsWith("m") || current.endsWith("bn")) && currencies.contains(next))) //numberm/numbernb dollars
                || (currentNumeric && (next.equals("billion") || next.equals("million") || next.equals("trillion")) && secondNext.equals("US") && thirdNext.equals("dollars"))// number million/billion/trillion u.s. dollars
                || (currentNumeric && isFraction(next) && currencies.contains(secondNext))) //number fraction currency
            return true;
        return false;
    }

    /**
     * checks if given string is a numeric/numeric fraction
     *
     * @param str
     * @return true if and only if the input is a fraction
     */
    protected boolean isFraction(String str) {
        if (!str.contains("/")) //not a fraction
            return false;
        String[] fraction = slash.split(str);
        if (fraction.length != 2) //not a fraction
            return false;
        if (isNumericIgnoreCommas(fraction[0]) && isNumericIgnoreCommas(fraction[1]))//for now!!!!!!!!!!!!!!!!!!!!!!
            return true;
        return false;


    }

    /**
     * handles percent
     *
     * @param s
     * @return term with currect data
     */
    protected Term handlePercent(String s, String termType) {
        if (s.endsWith("%"))
            return termBuilder.buildTerm(_PercentageTermClass, s);
        String data = s + "%";
        return termBuilder.buildTerm(_PercentageTermClass, data);

    }

    /**
     * checks if the given string represents percent
     *
     * @param strNum
     * @param next
     * @param currentNumeric
     * @return
     */
    protected boolean isPercent(String strNum, String next, boolean currentNumeric) {
        if (strNum.endsWith("%") && (isNumericIgnoreCommas(strNum.substring(0, strNum.length() - 1))))
            return true;
        if ((next.equals("percent") || next.equals("percentage")) && currentNumeric)
            return true;
        return false;
    }

    /**
     * method handling numbers without units
     * does not delete "Thousand", "Million", "Billion" from list
     *
     * @param number
     * @param nextToken token after the number, to check if it is part of the number
     * @return token for the number
     */
    protected Term handleNumber(double number, String nextToken) {
        String outputToken = "";
        double correctedNumber = number;

        //<1000
        if (number < 1000 && !nextToken.equals("Thousand") && !nextToken.equals("Million") && !nextToken.equals("Billion")) {//number is smaller than 1000
            if ((int) number == number) { //number is .0
                int intNum = (int) number;
                if (isFraction(nextToken)) {
                    outputToken = intNum + " " + nextToken;
                } else {
                    outputToken = intNum + "";
                }
            } else //number is not .0
            {
                int intNum = (int) number;
                if (isFraction(nextToken)) {
                    outputToken = (round(number, 3)) + " " + nextToken;
                } else {
                    outputToken = (Double.toString(round(number, 3)));
                }
            }
            return termBuilder.buildTerm(_NumericTermClass, outputToken);

        }

        //>=1000
        else {
            if (number >= 1000000000 || nextToken.equals("Billion")) //larger than B
            {
                if (number >= 1000000000)
                    correctedNumber = number / 1000000000;
                correctedNumber = round(correctedNumber, 3);
                int round = (int) correctedNumber;
                if (correctedNumber == round) {
                    outputToken = (round) + "B";
                } else {
                    outputToken = (correctedNumber) + "B";
                }
                return termBuilder.buildTerm(_NumericTermClass, outputToken);
            }

            if ((number >= 1000000 || nextToken.equals("Million"))) {
                if (number >= 1000000)
                    correctedNumber = number / 1000000;
                correctedNumber = round(correctedNumber, 3);
                int round = (int) correctedNumber;
                if (correctedNumber == round) {
                    outputToken = (round) + "M";
                } else {
                    outputToken = (correctedNumber) + "M";
                }
                return termBuilder.buildTerm(_NumericTermClass, outputToken);
            }

            if ((number >= 1000 || nextToken.equals("Thousand"))) {
                if (number >= 1000)
                    correctedNumber = number / 1000;
                correctedNumber = round(correctedNumber, 3);
                int round = (int) correctedNumber;
                if (correctedNumber == round) {
                    outputToken = (round) + "K";
                } else {
                    outputToken = (correctedNumber) + "K";
                }
                return termBuilder.buildTerm(_NumericTermClass, outputToken);
            }
            return null;
        }
    }

    /**
     * separates words in text by space
     *
     * @param text
     * @return arrayList of separated words
     */
    protected ArrayList<String> splitBySpaceToArrayList(String text) {
        if (text == null)
            return null;
        ArrayList<String> splitedArrayList = new ArrayList<String>(Arrays.asList(splitSpaces.split(text)));
        return splitedArrayList;
    }

    /**
     * @param strNum
     * @return true if and only if string is number, else false
     */
    protected boolean isNumericIgnoreCommas(String strNum) {
        strNum = removeCommas(strNum);
        double d;
        try {
            return numbers.matcher(strNum).matches();
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
    }

    /**
     * removes commas from string
     *
     * @param strNum
     * @return string without the commas
     */
    protected String removeCommas(String strNum) {
        if (strNum == null)
            return null;
        String[] separated = commas.split(strNum);
        StringBuilder withoutCommas = new StringBuilder();
        for (int i = 0; i < separated.length; i++) {
            withoutCommas.append(separated[i]);
        }
        return withoutCommas.toString();
    }

    /**
     * @param strNum numeric string (might be with commas, they are ignored)
     * @return numeric value of strNum
     */
    protected Double getNumericIgnoreCommas(String strNum) {
        String copyNoComma = removeCommas(strNum);
        Double d;
        try {
            d = Double.parseDouble(copyNoComma);
        } catch (NumberFormatException | NullPointerException nfe) {
            return null;
        }
        return d;
    }

    /**
     * @param value  value to round
     * @param places how many places after decimal point
     * @return rounded number as double (half up)
     */
    protected double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}