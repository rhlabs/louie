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
package com.rhythm.util.tree;

import com.rhythm.louie.util.tree.Tree;
import com.rhythm.louie.util.tree.TreeNode;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class TreeTest {
    
    Tree<Integer,String> tree;
    public TreeTest() {
        tree = new Tree<Integer,String>();
        
        tree.addNode(1, "A");
        tree.addNode(2, "B");
        tree.addNode(3, "C");
        tree.addNode(4, "D");
        tree.addNode(5, "E");
        tree.addNode(6, "F");
        tree.addNode(7, "G");
        tree.addNode(8, "H");
        tree.addNode(9, "I");
        
        tree.setParent(2, 1);
        tree.setParent(3, 1);
        tree.setParent(4, 2);
        tree.setParent(5, 2);
        tree.setParent(6, 4);
        tree.setParent(7, 4);
        tree.setParent(8, 5);
        tree.setParent(9, 8);
        
    }

    @Test
    public void testToString() {
        System.out.println(tree.toString());
    }
    
    @Test
    public void testAddNode() {
        tree.addNode(30, "TEST");
        assertSame(tree.getNode(30).getParent(), tree.getRoot());
    }
    
    @Test
    public void testRemoveNode() {
        assertFalse(tree.removeNode(999));

        int testId = 40;
        tree.addNode(testId, "TEST");
        assertTrue(tree.removeNode(testId));
        assertNull(tree.getNode(testId));
        
        tree.addNode(testId, "TEST");
        
        int childId = 41;
        tree.addNode(childId, "TEST");
        tree.setParent(childId, testId);
        
        assertTrue(tree.removeNode(testId));
        assertNull(tree.getNode(testId));
        
        assertSame(tree.getNode(childId).getParent(),tree.getRoot());
        
    }

    @Test
    public void testSetParent() {
        System.out.println(tree);
        
        tree.addNode(20, "TEST");
        assertSame(tree.getNode(20).getParent(), tree.getRoot());
        
        tree.setParent(20, 19);
        assertNull(tree.getNode(20).getParent().getValue());
        assertEquals(tree.getNode(20).getParent().getKey(),new Integer(19));
        
        tree.addNode(19, "TESTPARENT");
        assertNotNull(tree.getNode(20).getParent().getValue());
        
        System.out.println(tree);
    }

    @Test
    public void testGetDescendants() {
        System.out.println("getDescendants");
        
        Set<Integer> expected = Sets.newHashSet(4,5,6,7,8,9);
        
        List<TreeNode<Integer,String>> children = tree.getDescendants(2);
        assertEquals(children.size(), expected.size());
        
        for (TreeNode<Integer,String> child : children) {
            assertTrue(expected.remove(child.getKey()));
        }
    }

    @Test
    public void testGetDescendantsDepth() {
        System.out.println("getDescendants - Depth");
        
        Set<Integer> expected = Sets.newHashSet(4,5);
        
        List<TreeNode<Integer,String>> parents = tree.getDescendants(2,1);
        assertEquals(parents.size(), expected.size());
        
        for (TreeNode<Integer,String> parent : parents) {
            assertTrue(expected.remove(parent.getKey()));
        }
    }

    @Test
    public void testGetAncestors() {
        System.out.println("getAncestors");
        
        Set<Integer> expected = Sets.newHashSet(1,2,5);
        
        List<TreeNode<Integer,String>> parents = tree.getAncestors(8);
        assertEquals(parents.size(), expected.size());
        
        for (TreeNode<Integer,String> parent : parents) {
            assertTrue(expected.remove(parent.getKey()));
        }
    }

    @Test
    public void testGetAncestorsDepth() {
        System.out.println("getAncestors - Depth");
        
        Set<Integer> expected = Sets.newHashSet(2,5);
        
        List<TreeNode<Integer,String>> parents = tree.getAncestors(8,2);
        assertEquals(parents.size(), expected.size());
        
        for (TreeNode<Integer,String> parent : parents) {
            assertTrue(expected.remove(parent.getKey()));
        }
    }
    
}
