package gitlet;

import java.io.File;
import java.io.IOException;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Oumar Balde
 */
public class Main {


    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String command = args[0];
        switch (command) {
        case "init":
            if (GITLETDIR.exists()) {
                System.out.println("A Gitlet version-control system "
                        + "already exists in the current directory.");
                System.exit(0);
            }
            init();
            break;
        case "add":
            if (args.length < 2) {
                System.out.println("Missing file name");
                System.exit(0);
            }
            File fileToAdd = new File(args[1]);
            if (!fileToAdd.exists()) {
                System.out.println("File does not exist.");
                System.exit(0);
            }
            add(args[1]);
            break;
        case "commit":
            if (args.length < 2) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            } else if (args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            String message = args[1];
            commit(message);
            break;
        default:
            mainHelper(args);
            break;
        }
    }

    /**
     * Helper method such that main method does not exceed 60 lines.
     * @param args
     */
    public static void mainHelper(String... args) throws IOException {
        switch (args[0]) {
        case "rm":
            remove(args[1]);
            break;
        case "log":
            log();
            break;
        case "global-log":
            globalLog();
            break;
        case "find":
            confirmArgs(args);
            find(args[1]);
            break;
        case "status":
            status(args);
            break;
        case "checkout":
            confirmArgs(args);
            checkout(args);
            break;
        case "branch":
            confirmArgs(args);
            branch(args[1]);
            break;
        case "rm-branch":
            confirmArgs(args);
            removeBranch(args[1]);
            break;
        case "reset":
            confirmArgs(args);
            reset(args[1]);
            break;
        case "merge":
            confirmArgs(args);
            merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    public static void resetFile(File file) throws IOException {
        file.delete();
        if (file.isFile()) {
            file.createNewFile();
        } else if (file.isDirectory()) {
            file.mkdir();
        }
    }

    public static void init() throws IOException {
        GITLETDIR.mkdir();
        _repository.createNewFile();
        _repo = new Repo();
        Utils.writeObject(_repository, _repo);
    }

    public static void add(String fileName) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.add(fileName);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void commit(String msg) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.commit(msg);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void remove(String fileName) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.remove(fileName);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void log() {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.simpleLog();
    }

    public static void globalLog() {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.globalLog();
    }

    public static void find(String msg) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.find(msg);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void status(String... args) {
        try {
            _repo = Utils.readObject(_repository, Repo.class);
        } catch (IllegalArgumentException excp) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        _repo.status();
    }

    public static void checkout(String... args) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.checkout(args);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void branch(String branch) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.branch(branch);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void removeBranch(String branch) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.removeBranch(branch);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void reset(String commitID) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.reset(commitID);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void merge(String branch) throws IOException {
        _repo = Utils.readObject(_repository, Repo.class);
        _repo.merge(branch);
        resetFile(_repository);
        Utils.writeObject(_repository, _repo);
    }

    public static void confirmArgs(String... args) {
        if (args.length < 2) {
            System.out.println("Missing argument");
            System.exit(0);
        }
    }

    /**
     * Directory containing the entire gitlet version-control-system.
     */
    private static final File GITLETDIR = new File(".gitlet");
    /**
     * File in which the gitlet repository is stored.
     */
    private static File _repository = new File(GITLETDIR, "repository");
    /**
     * Repo object that represents the gitlet repository.
     */
    private static Repo _repo;
}
