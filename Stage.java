package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.LinkedHashMap;

/** Stage class.
 * @author Bond Chaiprasit */
public class Stage implements Serializable {
    /** Hashmap of files to stage. */
    private LinkedHashMap<String, String> addToFiles;
    /** List of files to remove. */
    private ArrayList<String> filesToRemove;

    /** Constructor for stage class. */
    public Stage() {
        addToFiles = new LinkedHashMap<>();
        filesToRemove = new ArrayList<>();

    }

    /** Returns the files to add. */
    public LinkedHashMap<String, String> getFilesToAdd() {
        return addToFiles;
    }

    /** Returns the files to remove. */
    public ArrayList<String> getFilesToRem() {
        return filesToRemove;
    }


    /** Clears the staging area to add and remove. */
    public void clear() {
        addToFiles.clear();
        filesToRemove.clear();
    }
}
