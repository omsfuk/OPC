package cn.omsfuk.compiling.support;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Node {
    public Node parent;
    public List<Node> children;
    public String name;

    public Node(String name) {
        this.name = name;
        this.children = new LinkedList<>();
    }

    public Node(Node parent, String name) {
        this.parent = parent;
        this.name = name;
        this.children = new LinkedList<>();
    }

    public Node(Node parent, List<Node> children, String name) {
        this.parent = parent;
        this.children = children;
        this.name = name;
    }

    public Node(Node parent, Node children, String name) {
        this.parent = parent;
        this.children = new ArrayList<>(Arrays.asList(children));
        this.name = name;
    }

    public void appendChild(Node child) {
        this.children.add(child);
    }
}

