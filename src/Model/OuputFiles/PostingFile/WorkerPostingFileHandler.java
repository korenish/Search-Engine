package Model.OuputFiles.PostingFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * this class is responsible for writing the post file per worker
 */
public class WorkerPostingFileHandler {
    private String path;

    public WorkerPostingFileHandler(String path) {
        this.path = path;
    }

    /**
     * this method receives sorted array list and write it to a file
     * @param sortedValues
     */
    public void writeWorkerFile(ArrayList<String> sortedValues)
    {
        try {
            File workerPost = new File(path);
            workerPost.createNewFile();
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter(this.path));
            while (sortedValues.size() > 0) {
                String current = sortedValues.remove(sortedValues.size() - 1);
                writer.newLine();
                writer.write(current);
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
