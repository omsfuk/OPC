package cn.omsfuk.compiling.support;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class SyntaxTreeGUI extends JFrame  {

    private JTree tree;

    private Node node;

    private TreeNode treeNode = new DefaultMutableTreeNode();

    public SyntaxTreeGUI(Node node) {
        this.node = node;
        copy((DefaultMutableTreeNode) treeNode, node);
    }

    public void build() {
        tree = new JTree(treeNode);
        tree.setRootVisible(false);
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportBorder(BorderFactory.createEtchedBorder());
        this.add(scrollPane);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public void copy(DefaultMutableTreeNode treeNode, Node node) {
        DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(node.name);
        treeNode.add(subNode);
        for (Node child : node.children) {
            copy(subNode, child);
        }
    }
}
