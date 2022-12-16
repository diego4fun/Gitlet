package gitlet;

import java.io.Serializable;
import java.util.Vector;

/** CommitTree data structure.
 * @author Oumar Balde
 */

public class CommitTree implements Serializable {

    /** Root of the CommitTree.*/
    private CommitNode _root;

    /** CommitTree default constructor.*/
    CommitTree() {
        _root = null;
    }

    /** Helper method that gets this Tree's root.
     * @return root of the commitTree*/
    public CommitNode getRoot() {
        return _root;
    }
    public void setRoot(CommitNode root) {
        _root = root;
    }

    /** CommitNode which contains a commit and
     * pointers to its children.
     */
    public static class CommitNode implements Serializable {
        /** Commit.*/
        private final Commit _commit;
        /** This commit node's Children.*/
        private Vector<CommitNode> _children;
        /** This commit node's Parent.*/
        private CommitNode _parent;

        /** Commit Node default constructor.
         * @param commit to be stored
         * */
        CommitNode(Commit commit) {
            _commit = commit;
            _children = new Vector<>();
        }

        /**
         * Adds "node" to the CommitNode on which
         * the function is called upon.
         * @param node to be added
         */
        public void addChildren(CommitNode node) {
            _children.add(node);
        }

        /** Helper method that checks whether this node
         * has any children.
         * @return -*/
        public boolean hasChildren() {
            return _children.size() != 0;
        }

        /** Helper method that checks whether this node
         * has a parent.
         * @return -*/
        public boolean hasParent() {
            return _parent != null;
        }

        /** Sets this node's parent to "node".
         * @param node parent
         */
        public void setParent(CommitNode node) {
            _parent = node;
        }

        /** Accessor method that gets this node's Commit.
         * @return commit of this node.
         */
        public Commit getCommit() {
            return _commit;
        }

        /** Accessor method that gets this node's parent.
         * @return parent*/
        public CommitNode getParent() {
            return _parent;
        }

        /** Accessor method that gets this node's children.
         * @return children*/
        public Vector<CommitNode> getChildren() {
            return _children;
        }
    }
}
