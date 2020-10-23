package Model.HandleReadFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class is responsible for reading queries from queries file
 */
public class QueryFileUtil {

    /**
     *
     * @param path of query file
     * @return map of query id and query content
     */
    public static HashMap<String, String> extractQueries(String path) {
        HashMap<String, String> idTextMap = new HashMap<>();
        String num = "";
        String title = "";
        String desc = "";
        String narr = "";
        try {
            File queryFile = new File(path);
            if (!queryFile.exists())
                System.out.println("no query file");
            Scanner reader = new Scanner(queryFile);
            String line = reader.nextLine();
            while (reader.hasNextLine()) {
                while (reader.hasNextLine() && !line.startsWith("<num>")) {
                    line = reader.nextLine();
                }
                if (reader.hasNextLine()) {
                    String[] numArr = line.split("<num> Number: ");
                    num = numArr[1];
                }
                while (reader.hasNextLine() && !line.startsWith("<title>")) {
                    line = reader.nextLine();
                }
                if (reader.hasNextLine()) {
                    title = line.split("<title> ")[1];
                }
                while (reader.hasNextLine() && !line.startsWith("<desc>")) {
                    line = reader.nextLine();
                }
                if (reader.hasNextLine())
                    line = reader.nextLine();
                while (reader.hasNextLine() && !line.startsWith("<narr>")) {
                    desc = desc + " " + line;
                    line = reader.nextLine();
                }
                if (reader.hasNextLine())
                    line = reader.nextLine();
                while (reader.hasNextLine() && !line.startsWith("</top>")) {
                    narr = narr + " " + line;
                    line = reader.nextLine();
                }
                //add lower case to capitals
                String query =title + " " + desc + " " + narr;
                //entities as lower case too
                String[] queryStrings = title.split(" ");
                for (int i = 0; i < queryStrings.length; i++) {
                    if (partOfEntity(i, queryStrings))
                        query = query + " " + queryStrings[i].toLowerCase();
                }
                queryStrings = desc.split(" ");
                for (int i = 0; i < queryStrings.length; i++) {
                    if (partOfEntity(i, queryStrings))
                        query = query + " " + queryStrings[i].toLowerCase();
                }
                queryStrings = narr.split(" ");
                for (int i = 0; i < queryStrings.length; i++) {
                    if (partOfEntity(i, queryStrings))
                        query = query + " " + queryStrings[i].toLowerCase();
                }
                idTextMap.put(num, query);
                desc = "";
                narr = "";
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return idTextMap;
    }

    private static boolean partOfEntity(int i, String[] strings) {
        if (strings[i].length()==0)
            return false; //not a word
        if (!(strings[i].charAt(0) >= 'A' && strings[i].charAt(0) <= 'Z'))
            return false;
        if (i != 0 && !(strings[i-1].length()==0))
            if (strings[i - 1].charAt(0) >= 'A' && strings[i - 1].charAt(0) <= 'Z')
                return true;
        if (i != strings.length - 1 && !(strings[i + 1].length()==0))
            if (strings[i + 1].charAt(0) >= 'A' && strings[i + 1].charAt(0) <= 'Z')
                return true;
        return false;
    }
}
