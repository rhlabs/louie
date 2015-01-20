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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A node for use in a Tree.  Can have a single parent and multiple children.
 * Not intended to be manipulated directly, only through the use of the Tree itself.
 * 
 * @param <K> the unique identifier of the nodes in the tree
 * @param <V> the value stored in each node
 */
public class TreeNode<K, V> {
    private final K key;
    private V value;
    private Map<K, TreeNode<K, V>> children;
    private TreeNode<K, V> parent;

    /**
     * Constructs a TreeNode
     * 
     * @param key
     * @param value 
     */
    public TreeNode(K key, V value) {
        this.key = key;
        this.value = value;
        children = null;
        parent = null;
    }

    /**
     * Return the node of the parent.
     * @return the parent, or null if the node does not have a parent
     */
    public TreeNode<K, V> getParent() {
        return parent;
    }

    /**
     * Returns true if the node has a parent
     * @return true if the node has a parent
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns a the children of the node
     * @return the children of the node, or an emptySet if node does not have children
     */
    public Iterable<TreeNode<K, V>> getChildren() {
        if (children == null) {
            return Collections.emptySet();
        }
        return children.values();
    }

    /**
     * Sets the new parent of the node, modifying the existing parent if necessary
     * 
     * @param parent the new parent node of this node 
     */
    protected synchronized void setParent(TreeNode<K, V> parent) {
        if (this.parent == parent) {
            return;
        }

        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Adds a child to this node
     * @param child node to be added
     */
    protected synchronized void addChild(TreeNode<K, V> child) {
        // lazy create the child map
        if (children == null) {
            children = new ConcurrentHashMap<>();
        }
        children.put(child.getKey(), child);
    }

    /**
     * Removes a child from this node, returns true if the child false if the node 
     * was not a child of this node
     * 
     * @param child to be removed
     * @return true of the child is successfully removed
     */
    protected synchronized boolean removeChild(TreeNode<K, V> child) {
        if (children == null) {
            return false;
        }
        // Should we clean up map here if empty... ?
        return children.remove(child.getKey()) != null;
    }

    /**
     * Returns true if this node has at least 1 child
     * @return true if the node has children 
     */
    public boolean hasChildren() {
        if (children == null) {
            return false;
        }
        return !children.isEmpty();
    }

    /**
     * Return the value stored in the node
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /**
     * Returns the unique identifier of the node
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * Sets the value for the node
     * @param value the value to set
     */
    public void setValue(V value) {
        this.value = value;
    }
    
    /**
     * String representation of the node.  Key->Value
     * @return a String
     */
    @Override
    public String toString() {
        return key+"->"+value;
    }
}
