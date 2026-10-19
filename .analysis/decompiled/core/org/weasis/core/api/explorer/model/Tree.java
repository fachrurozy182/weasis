/*
 * Decompiled with CFR 0.152.
 */
package org.weasis.core.api.explorer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class Tree<T> {
    private final T head;
    private final ArrayList<Tree<T>> leafs = new ArrayList();
    private Tree<T> parent = null;
    private HashMap<T, Tree<T>> locate = new HashMap();

    public Tree(T head) {
        this.head = head;
        this.locate.put(head, this);
    }

    public synchronized void addLeaf(T root, T leaf) {
        if (this.locate.containsKey(root)) {
            this.locate.get(root).addLeaf(leaf);
        } else {
            this.addLeaf(root).addLeaf(leaf);
        }
    }

    private synchronized Tree<T> addLeaf(T leaf) {
        Tree<T> t = new Tree<T>(leaf);
        this.leafs.add(t);
        t.parent = this;
        t.locate = this.locate;
        this.locate.put(leaf, t);
        return t;
    }

    public synchronized void removeLeaf(T leaf) {
        Tree<T> t = this.locate.remove(leaf);
        if (t != null) {
            t.parent.leafs.remove(t);
            t.parent = null;
            t.locate = null;
        }
    }

    public synchronized Tree<T> setAsParent(T parentRoot) {
        Tree<T> t = new Tree<T>(parentRoot);
        t.leafs.add(this);
        this.parent = t;
        t.locate = this.locate;
        t.locate.put(this.head, this);
        t.locate.put(parentRoot, t);
        return t;
    }

    public T getHead() {
        return this.head;
    }

    public synchronized Tree<T> getTree(T element) {
        return this.locate.get(element);
    }

    public synchronized Tree<T> getParent() {
        return this.parent;
    }

    public synchronized Collection<T> getSuccessors(T root) {
        ArrayList<T> successors = new ArrayList<T>();
        Tree<T> tree = this.getTree(root);
        if (null != tree) {
            for (Tree<T> leaf : tree.leafs) {
                successors.add(leaf.head);
            }
        }
        return successors;
    }

    public Collection<Tree<T>> getSubTrees() {
        return this.leafs;
    }

    public static <T> Collection<T> getSuccessors(T of, Collection<Tree<T>> in) {
        for (Tree<T> tree : in) {
            if (!tree.locate.containsKey(of)) continue;
            return tree.getSuccessors(of);
        }
        return new ArrayList();
    }

    public String toString() {
        return this.printTree(0);
    }

    public synchronized void clear() {
        this.locate.clear();
        this.leafs.clear();
        this.locate.put(this.head, this);
    }

    private String printTree(int increment) {
        char[] value = new char[increment];
        Arrays.fill(value, ' ');
        String inc = new String(value);
        StringBuilder s = new StringBuilder(inc + String.valueOf(this.head));
        for (Tree<T> child : this.leafs) {
            s.append("\n");
            s.append(child.printTree(increment + 2));
        }
        return s.toString();
    }
}

