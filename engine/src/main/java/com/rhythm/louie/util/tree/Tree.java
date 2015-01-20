/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.util.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Function;
import com.google.common.collect.TreeTraverser;

/**
 * Tree structure containing uniquely identifiable nodes. Nodes can be added and
 * parented ahead of the parents, as long as the unique identifiers are known.
 * This is useful for construction from data of undetermined order. Nodes can be
 * re-parented and deleted.
 *
 * @param <K> the unique identifier of the nodes in the tree
 * @param <V> the value stored in each node
 */
public class Tree<K,V> {
    private final TreeNode<K,V> ROOT;
    private final Map<K,TreeNode<K,V>> nodes;
    
    /**
     * Constructs a general purpose tree.  Call addNode and setParent to populate it.
     */
    public Tree() {
        this.ROOT = new TreeNode<>(null,null);
        
        nodes = new ConcurrentHashMap<>();
    }
    
    private class Traverser extends TreeTraverser<TreeNode<K, V>> {
        @Override
        public Iterable<TreeNode<K, V>> children(TreeNode<K, V> node) {
            return node.getChildren();
        }
    }
    
    public TreeTraverser<TreeNode<K,V>> getTraverser() {
        return new Traverser();
    }

    /**
     * Returns the ROOT node of the tree.  This is a special node that does not
     * have a key and does not have a value.  This node is not returned in calls
     * to getAncestors.
     * 
     * @return the hidden root node 
     */
    public TreeNode<K,V> getRoot() {
        return ROOT;
    }
    
    /**
     * Creates new node with a parent of ROOT and value of NULL, 
     * or returns an existing node.
     * 
     * @param key the unique identifier of the node
     * @return the newly created or existing node
     */
    private TreeNode<K,V> getOrCreateNode(K key) {
        TreeNode<K,V> node = nodes.get(key);
        if (node == null) {
            // TreeNode does not exists, so create and set parent to root
            node = new TreeNode<>(key, null);
            nodes.put(key,node);
            node.setParent(ROOT);
        }
        
        return node;
    }
    
    /**
     * Adds a node into the tree as a root level node.  If the node needs to be
     * re-parented, you should call setParent.  If the node already exists, the 
     * value of the existing node is replaced by the value specified.
     * 
     * @param key the unique identifier
     * @param value the new value to set on the node
     * @return the newly created or existing node
     */
    synchronized public TreeNode<K,V> addNode(K key, V value) {
        TreeNode<K,V> node = getOrCreateNode(key);
        node.setValue(value);
        return node;
    }
    
    /**
     * Deletes a given node if it exists.  If the node has children, the parent
     * of the children becomes the parent of the node that was deleted.
     * 
     * @param key the unique identifier of a node
     * 
     * @return true if the node exists and was deleted
     */
    synchronized public boolean removeNode(K key) {
        TreeNode<K,V> node = nodes.get(key);
        if (node==null) {
            return false;
        }
        
        for(TreeNode<K,V> child : node.getChildren()) {
            child.setParent(node.getParent());
        }
        node.getParent().removeChild(node);
        nodes.remove(key);
        return true;
    }
    
    /**
     * Registers a parent with an existing child.
     * 
     * If the child does not exist, nothing happens.
     * If a parent does not exist, A place holder is injected into the tree. 
     * 
     * If addNode is called later with the parentKey specified, the placeholder value is 
     * replaced with the value specified in addNode.  This is to aid in constructing the tree
     * so that you do not have to use your own data structures in order to assemble 
     * a tree from data in an indeterminate order.
     * 
     * If null is specified for the parentKey, the child is promoted to be a root level node.
     * 
     * @param childKey
     * @param parentKey 
     */
    synchronized public void setParent(K childKey, K parentKey) {
        if (childKey == null) {
            return;
        }
        TreeNode<K,V> child = nodes.get(childKey);
        if (child==null) {
            return;
        }
        
        TreeNode<K,V> parent;
        if (parentKey != null) {
            parent = getOrCreateNode(parentKey);
        } else {
            parent = ROOT;
        }
        
        child.setParent(parent);
    }
    
    /**
     * Inserts all values in the tree into the valuestore
     *
     * @param valuestore a collection to store the values
     */
    public void getAllValues(Collection<V> valuestore) {
        for (TreeNode<K, V> node : nodes.values()) {
            // Values can be null if the tree contains parent place holders
            if (node.getValue() != null) {
                valuestore.add(node.getValue());
            }
        }
    }
    
    /**
     * Returns all nodes in the tree in no specific order
     * 
     * @return a collection of all tree nodes in the tree 
     */
    public Collection<TreeNode<K,V>> getAllNodes() {
        return nodes.values();
    }
    
    /**
     * Returns the number of nodes in the tree
     * @return the number of nodes in the tree
     */
    public int size() {
        return nodes.size();
    }
    
    /**
     * Lookup a TreeNode by its key.  If key does not exist, null is returned.
     * @param key the unique identifier of a node
     * @return a node or null if node is not found
     */
    public TreeNode<K,V> getNode(K key) {
        if (key == null) {
            return null;
        }
        return nodes.get(key);
    }
    
    /**
     * Returns a list of all recursive children of a given node.
     * 
     * @param key the unique identifier of a node
     * @return an unordered list of child nodes
     */
    public List<TreeNode<K,V>> getDescendants(K key) {
        return getDescendants(key,0);
    }
    
    /**
     * Returns a list of all recursive children of a given node.
     * Depth specifies how many generations of children should be returned.
     * A depth of 0 returns all leaves.
     * 
     * @param key the unique identifier of a node
     * @param depth how many generations to be returned
     * @return an unordered list of child nodes
     */
    public List<TreeNode<K,V>> getDescendants(K key, int depth) {
        TreeNode<K,V> root = nodes.get(key);
        if (root==null) {
            return Collections.emptyList();
        }
        List<TreeNode<K,V>> results = new ArrayList<>();
        lookupChildren(root, 0, depth, results);
        
        return results;
    }
    
    private void lookupChildren(TreeNode<K,V> node, int level, int depth, List<TreeNode<K,V>> results) {
        if (level!=0) {
            results.add(node);
        }
        if (depth>0 && level>=depth) {
            return;
        }
        for (TreeNode<K,V> child : node.getChildren()) {
            lookupChildren(child,level+1,depth,results);
        }
    }
   
    /**
     * Returns a list of all ancestors recursively from the node with the given key
     * @param key
     * @return a list of ancestor nodes
     */
    public List<TreeNode<K,V>> getAncestors(K key) {
        return getAncestors(key,0);
    }
    
    /**
     * Returns a list of ancestors recursively from the node with the given key.
     * Depth specifies how many generations are returned.  Depth==1 returns just the
     * immediate parent.  Depth==0 returns all ancestors up to the root node.
     * 
     * @param key
     * @param depth
     * @return a list of ancestor nodes
     */
    public List<TreeNode<K, V>> getAncestors(K key, int depth) {
        TreeNode<K,V> node = nodes.get(key);
        if (node==null || !node.hasParent() || node.getParent()==ROOT) {
            return Collections.emptyList();
        }
        
        List<TreeNode<K,V>> results = new ArrayList<>();
        int level = 1;
        while ((depth == 0 || level <= depth) && node.hasParent() && node.getParent()!=ROOT) {
            node = node.getParent();
            results.add(node);
            level++;
        }
        return results;
    }
    /**
     * Rudimentary representation of the tree in the form
     * 
     * ROOT
     * -Child1
     * --Grandchild1
     * -Child2
     * --Grandchild2
     * 
     * @return a string representation of the tree 
     */
    @Override
    public String toString() {
        return toString(toStringFunc);
    }
    
    private final Function<V,String> toStringFunc = new NodeToStringFunction<>();
    private class NodeToStringFunction<V> implements Function<V, String> {
        @Override
        public String apply(V o) {
            if (o==null) {
                return String.valueOf(o);
            } else {
                return o.toString();
            }
        }
    }
    
    public String toString(Function<V, String> stringFunc) {
        StringBuilder sb = new StringBuilder();
        for (TreeNode<K, V> child : ROOT.getChildren()) {
            childToString(child, sb, 0, stringFunc);
        }
        return sb.toString();
    }

    private void childToString(TreeNode<K, V> node, StringBuilder sb, int depth, Function<V, String> stringFunc) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(stringFunc.apply(node.getValue())).append("\n");
        for (TreeNode<K, V> child : node.getChildren()) {
            childToString(child, sb, depth + 1, stringFunc);
        }
    }
}
