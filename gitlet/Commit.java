package gitlet;


import java.io.Serializable;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;
import java.time.LocalDateTime;

/**
 * Class representing a Commit object.
 * @author Oumar Balde
 */

public class Commit implements Serializable {

    /**
     * Commit default constructor.
     * Initializes _message and _timeStamp.
     * This commit doesn't track any file.
     * @param message of this commit
     */
    Commit(String message) {
        _message = message;
        _timeStamp = "Wed Dec 31 16:00:00 1969 -0800";
    }

    /**
     * Commit secondary constructor.
     * Creates a new commit which tracks
     * the same files as commit0.
     * @param commit0 - parent of this commit
     * @param message of this commit
     */
    Commit(Commit commit0, String message) {
        _filesTracked =
                (TreeMap<String, byte[]>) commit0._filesTracked.clone();
        _message = message;
        LocalDateTime current = LocalDateTime.now();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("E MMM DD HH:mm:ss yyyy -0800");
        _timeStamp = current.format(formatter);
    }

    /**
     * Helper function that Serializes a COMMIT before
     * storing it into a Byte Array.
     * @param s - serializable object
     * @return byte[] - array of bytes
     * @throws IOException -
     */
    public static byte[] serialized(Serializable s) throws IOException {
        return Utils.serialize(s);
    }

    /**
     * Adds "files" to the files that this commit
     * is tracking iff said file does not already exist
     * in the commit.
     * If it exists, just replace it.
     * @param files to be added
     */
    public void addFiles(TreeMap<String, byte[]> files) {
        if (files.size() == 0) {
            return;
        }
        for (String key : files.keySet()) {
            if (_filesTracked.containsKey(key)) {
                _filesTracked.replace(key, files.get(key));
            } else {
                _filesTracked.put(key, files.get(key));
            }
        }
    }

    /**
     * Untrack files that have been staged for removal.
     * @param files staged for removal
     */
    public void removeFiles(TreeMap<String, byte[]> files) {
        for (String key : files.keySet()) {
            if (_filesTracked.containsKey(key)) {
                _filesTracked.remove(key);
            }
        }
    }


    public void setSha1(String sha1) {
        _sha1 = sha1;
    }
    public String getSha1() {
        return _sha1;
    }
    public String getTime() {
        return _timeStamp;
    }
    public String getMessage() {
        return _message;
    }
    public TreeMap<String, byte[]> getFilesTracked() {
        return _filesTracked;
    }

    /** This Commit's message.*/
    private String _message;
    /** Time at which this Commit was created.*/
    private String _timeStamp;
    /** Files tracked by this commit. Maps file names to their
     * serialized content.*/
    private TreeMap<String, byte[]> _filesTracked = new TreeMap<>();
    /** This commit SHA-1 hashcode.*/
    private String _sha1;
}
