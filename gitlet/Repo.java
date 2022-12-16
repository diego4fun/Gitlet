package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Arrays;
import gitlet.CommitTree.CommitNode;

/**
 * Class representing the entire
 * gitlet repository.
 * @author Oumar Balde
 */


public class Repo implements Serializable {

    /** Repo default constructor.
     * Initializes Instance Objects/Variables,
     * creates the initial commit and adds it
     * to the commit tree.
     **/
    Repo() throws IOException {
        _commitTree = new CommitTree();
        Commit commit0 = new Commit("initial commit");
        commit0.setSha1(Utils.sha1(Commit.serialized(commit0)));
        _commitTree.setRoot(new CommitNode(commit0));
        _commitNodes.put(commit0.getSha1(), _commitTree.getRoot());
        _commitMessages.put(commit0.getSha1(), "initial commit");
        _branches.put("master", _commitTree.getRoot());
        _branchesHead.put("master", _commitTree.getRoot());
        _currBranch = "master";
    }


    public void simpleLog() {
        CommitNode headNode = getHEAD();
        CommitNode node = headNode;
        while (node != _commitTree.getRoot()) {
            Commit commit = node.getCommit();
            System.out.println("===");
            System.out.println("commit " + commit.getSha1());
            System.out.println("Date: " + commit.getTime());
            System.out.println(commit.getMessage());
            System.out.println();
            node = headNode.getParent();
            headNode = node;
        }
        CommitNode root = _commitTree.getRoot();
        Commit commit0 = root.getCommit();
        System.out.println("===");
        System.out.println("commit " + commit0.getSha1());
        System.out.println("Date: " + commit0.getTime());
        System.out.println(commit0.getMessage());
        System.out.println();
    }

    public void add(String fileName) {
        File file = new File(fileName);
        byte[] blob = Utils.readContents(file);
        if (_stagedRemove.containsKey(fileName)) {
            if (Arrays.equals(_stagedRemove.get(fileName), blob)) {
                _stagedRemove.remove(fileName);
                return;
            }
        }
        Commit currCommit = getHEAD().getCommit();
        if (currCommit.getFilesTracked().containsKey(fileName)) {
            byte[] blob1 = currCommit.getFilesTracked().get(fileName);
            if (Arrays.equals(blob, blob1)) {
                return;
            }
        }
        if (_stagedAdd.containsKey(fileName)) {
            _stagedAdd.replace(fileName, blob);
        } else {
            _stagedAdd.put(fileName, blob);
        }
    }

    public void commit(String msg) throws IOException {
        if (_stagedAdd.size() == 0 && _stagedRemove.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        CommitNode headNode = getHEAD();
        Commit commit = new Commit(headNode.getCommit(), msg);
        commit.addFiles(_stagedAdd);
        commit.removeFiles(_stagedRemove);
        _stagedAdd = new TreeMap<>();
        _stagedRemove = new TreeMap<>();
        commit.setSha1(Utils.sha1(Commit.serialized(commit)));
        CommitNode node = new CommitNode(commit);
        _commitNodes.put(commit.getSha1(), node);
        _commitMessages.put(commit.getSha1(), commit.getMessage());
        headNode.addChildren(node);
        node.setParent(headNode);
        _branches.replace(_currBranch, node);
        _branchesHead.replace(_currBranch, node);
    }

    public void remove(String fileName) {
        CommitNode headNode = getHEAD();
        if (!_stagedAdd.containsKey(fileName)
                &&
                !headNode.getCommit().getFilesTracked().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (_stagedAdd.containsKey(fileName)) {
            _stagedAdd.remove(fileName);
        }
        if (headNode.getCommit().getFilesTracked().containsKey(fileName)) {
            File file = new File(fileName);
            byte[] blob = headNode.getCommit().getFilesTracked().get(fileName);
            _stagedRemove.put(fileName, blob);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public void globalLog() {
        for (CommitNode node : commitNodes().values()) {
            Commit commit = node.getCommit();
            System.out.println("===");
            System.out.println("commit " + commit.getSha1());
            System.out.println("Date: " + commit.getTime());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public void find(String msg) {
        String result = "";
        for (String commitID : _commitMessages.keySet()) {
            if (_commitMessages.get(commitID).equals(msg)) {
                result += commitID + "\n";
            }
        }
        if (result.equals("")) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        System.out.println(result);
    }

    public void status() {
        String branches = "";
        for (String branch : _branches.keySet()) {
            if (branch.equals(_currBranch)) {
                branches += "*";
            }
            branches += branch + "\n";
        }
        String stagedFiles = "";
        for (String fileName : _stagedAdd.keySet()) {
            stagedFiles += fileName + "\n";
        }
        String removedFiles = "";
        for (String fileName : _stagedRemove.keySet()) {
            removedFiles += fileName + "\n";
        }
        String modif = "";
        List<String> fileNames = Utils.plainFilenamesIn(_workingDirectory);
        Commit currCommit = getHEAD().getCommit();
        TreeMap<String, byte[]> trackedFiles = currCommit.getFilesTracked();
        for (String fileName : trackedFiles.keySet()) {
            if (fileNames.contains(fileName)) {
                byte[] blob = Utils.readContents(new File(fileName));
                if (!Arrays.equals(trackedFiles.get(fileName), blob)) {
                    modif += fileName + " (modified)\n";
                }
            } else if (!fileNames.contains(fileName)
                    && !_stagedRemove.containsKey(fileName)) {
                modif += fileName + " (deleted)\n";
            }
        }
        String untracked = "";
        for (String fileName : fileNames) {
            if (!_stagedAdd.containsKey(fileName)
                    && !trackedFiles.containsKey(fileName)) {
                untracked += fileName + "\n";
            }
        }
        System.out.println("=== Branches ===");
        System.out.println(branches);
        System.out.println("=== Staged Files ===");
        System.out.println(stagedFiles);
        System.out.println("=== Removed Files ===");
        System.out.println(removedFiles);
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println(modif);
        System.out.println("=== Untracked Files ===");
        System.out.println(untracked);
    }

    public void checkout(String... args) throws IOException {
        if (args.length == 2) {
            checkout3(args[1]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            checkout2(args[1], args[3]);
        } else {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            checkout1(args[2]);
        }
    }

    public void checkout1(String fileName) throws IOException {
        Commit headCommit = getHEAD().getCommit();
        if (!headCommit.getFilesTracked().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
                Utils.writeContents(file,
                        headCommit.getFilesTracked().get(fileName));
            } else {
                Main.resetFile(file);
                Utils.writeContents(file,
                        headCommit.getFilesTracked().get(fileName));
            }
        }
    }

    public void checkout2(String commitID, String fileName) throws IOException {
        if (!commitNodes().containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit commit = commitNodes().get(commitID).getCommit();
            if (!commit.getFilesTracked().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            } else {
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                    Utils.writeContents(file,
                            commit.getFilesTracked().get(fileName));
                } else {
                    Main.resetFile(file);
                    Utils.writeContents(file,
                            commit.getFilesTracked().get(fileName));
                }
            }
        }
    }

    public void checkout3(String branch) throws IOException {
        if (!_branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (branch.equals(_currBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit checkoutCommit = _branchesHead.get(branch).getCommit();
        String commitID = checkoutCommit.getSha1();
        TreeMap<String, byte[]> checkoutTracked =
                checkoutCommit.getFilesTracked();
        TreeMap<String, byte[]> currTracked =
                _branches.get(_currBranch).getCommit().getFilesTracked();
        List<String> fileNames = Utils.plainFilenamesIn(_workingDirectory);
        for (String fileName : checkoutTracked.keySet()) {
            if (fileNames.contains(fileName)) {
                byte[] blob = Utils.readContents(new File(fileName));
                if (untrackedCurrBranch(fileName, blob)) {
                    System.out.println("There is an untracked "
                            + "file in the way;"
                            + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            checkout2(commitID, fileName);
        }
        for (String fileName : currTracked.keySet()) {
            if (!checkoutTracked.containsKey(fileName)) {
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        _stagedRemove = new TreeMap<>();
        _stagedAdd = new TreeMap<>();
        _currBranch = branch;
    }

    public boolean untrackedCurrBranch(String fileName, byte[] blob) {
        CommitNode currentBranch = _branches.get(_currBranch);
        CommitNode copy = currentBranch;
        while (copy != null) {
            if (copy.getCommit().getFilesTracked().containsKey(fileName)) {
                byte[] blob1 = copy.getCommit().getFilesTracked().get(fileName);
                if (Arrays.equals(blob, blob1)) {
                    return false;
                }
            }
            copy = copy.getParent();
        }
        return true;
    }


    public void branch(String branch) {
        if (_branches.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        _branches.put(branch, getHEAD());
        _branchesHead.put(branch, getHEAD());
    }

    public void removeBranch(String branch) {
        if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (branch.equals(_currBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        _branches.remove(branch);
        _branchesHead.remove(branch);
    }

    public void reset(String commitID) throws IOException {
        if (!commitNodes().containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        CommitNode node = _commitNodes.get(commitID);
        Commit commit = node.getCommit();
        TreeMap<String, byte[]> commitFiles = commit.getFilesTracked();
        TreeMap<String, byte[]> currTracked =
                _branches.get(_currBranch).getCommit().getFilesTracked();
        List<String> fileNames = Utils.plainFilenamesIn(_workingDirectory);
        for (String fileName : commitFiles.keySet()) {
            if (fileNames.contains(fileName)) {
                byte[] blob = Utils.readContents(new File(fileName));
                if (untrackedCurrBranch(fileName, blob)) {
                    System.out.println("There is an untracked file in the way;"
                           + " delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
            checkout2(commitID, fileName);
        }
        for (String fileName : currTracked.keySet()) {
            if (!commitFiles.containsKey(fileName)) {
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        _stagedAdd = new TreeMap<>();
        _stagedRemove = new TreeMap<>();
        _branchesHead.replace(_currBranch, node);
    }


    public void merge(String branch) throws IOException {
        if (_stagedRemove.size() != 0 || _stagedRemove.size() != 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch.equals(_currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        CommitNode splitPoint;
        CommitNode currNode = _branches.get(_currBranch);
        CommitNode currNodeCopy = currNode;
        CommitNode givenNode = _branches.get(branch);
        CommitNode givenNodeCopy = givenNode;
        if (currNode == givenNode) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        specialSplit1(currNode, givenNode, branch);
        specialSplit2(currNode, givenNode);
    }

    public void specialSplit1(CommitNode node1, CommitNode node2, String branch)
            throws IOException {
        if (!node1.hasChildren()) {
            if (node1 == node2) {
                checkout3(branch);
                System.out.println("Current branch fast-forwarded.");
                System.exit(0);
            }
            return;
        } else if (node1 == node2) {
            checkout3(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else {
            for (CommitNode node : node1.getChildren()) {
                specialSplit1(node, node2, branch);
            }
        }
    }

    public void specialSplit2(CommitNode currNode, CommitNode givenNode) {
        CommitNode currNodeCopy = currNode;
        while (currNodeCopy.hasParent()) {
            if (currNodeCopy == givenNode) {
                System.out.println("Given branch is "
                        + "an ancestor of the current branch.");
                System.exit(0);
            }
            currNodeCopy = currNodeCopy.getParent();
        }
    }


    /** Accessor method that gets the Head Node.
     * @return Head Node
     */
    public CommitNode getHEAD() {
        return _branchesHead.get(_currBranch);
    }

    /** Accessor method that gets the commitNodes.
     * @return commitNodes
     */
    public LinkedHashMap<String, CommitNode> commitNodes() {
        return _commitNodes;
    }
    /** The current working directory.*/
    private File _workingDirectory = new File(".");
    /** This repository CommitTree.*/
    private CommitTree _commitTree;
    /** Current branch.*/
    private String _currBranch;
    /** Hashmap of the commit nodes from the commitTree.
     * The purpose of this hashmap is to make the
     * lookup of a certain commit node more efficient.*/
    private LinkedHashMap<String, CommitNode> _commitNodes =
            new LinkedHashMap<>();
    /** Hashmap of files staged for adding.*/
    private TreeMap<String, byte[]> _stagedAdd = new TreeMap<>();
    /** Hashmap of files staged for removal.*/
    private TreeMap<String, byte[]> _stagedRemove = new TreeMap<>();
    /** Hashmap of commitMessages. It maps commit Ids to messages.*/
    private LinkedHashMap<String, String> _commitMessages =
            new LinkedHashMap<>();
    /** Hashmap of Branches. Maps branch names to commit nodes.*/
    private TreeMap<String, CommitNode> _branches = new TreeMap<>();
    /** Mapping of branch names with their corresponding Head Node.*/
    private LinkedHashMap<String, CommitNode> _branchesHead =
            new LinkedHashMap<>();
}
