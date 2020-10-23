package Model.OuputFiles.DocumentFile;

import java.util.concurrent.ConcurrentHashMap;

public class DocumentFileObject
{
    // static variable single_instance of type Singleton
    private static DocumentFileObject single_instance = null;

    // variable of type String
    public ConcurrentHashMap<String, String> docsHolder;

    // private constructor restricted to this class itself
    private DocumentFileObject()
    {
        docsHolder = new ConcurrentHashMap<>();
    }

    // static method to create instance of Singleton class
    public static DocumentFileObject getInstance()
    {
        if (single_instance == null)
            single_instance = new DocumentFileObject();

        return single_instance;
    }

    public void setInstance(ConcurrentHashMap<String, String> docsHolder)
    {
        this.docsHolder = docsHolder;
    }
}