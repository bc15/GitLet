package gitlet;
import java.io.Serializable;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/** The Commit class.
 * @author Bond Chaiprasit*/
public class Commit implements Serializable {
    /** Message of the commit. */
    private String _message;
    /** The timestamp of the commit. */
    private Date timestamp;

    /** Maps name of file to its hashcode. */
    private LinkedHashMap<String, String> blobToFile;
    /** The parent of this commit. */
    private String _parent;
    /** The second commit. */
    private String _parent2;



    /** The constructor for commit.
     * @param message the msg.
     * @param parent the prnt.
     * @param parent2 the prnt2.
     * @param blob the b.
     * @param time the t.*/
    public Commit(String message, String parent, String parent2,
                  LinkedHashMap<String, String> blob, Date time) {
        this._message = message;
        this._parent = parent;
        this._parent2 = parent2;
        this.timestamp = time;
        this.blobToFile = blob;


    }

    /** Accessor method for the sha1 of this commit.
     * @return String */
    public String getSha1() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Get the map of this commit.
     * @return HashMap<String, String> */
    public HashMap<String, String> getBlobs() {
        return blobToFile;
    }

    /** Get the date of this commit.
     * @return Date */
    public Date getDate() {
        return timestamp;
    }


    /** Get the message of this commit.
     * @return String */
    public String getMessage() {
        return this._message;
    }

    /** Get the parent.
     * @return String */
    public String getParent() {
        return this._parent;
    }

    /** Get the second paret.
     * return String */
    public String getParent2() {
        return this._parent2;
    }


}
