///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.data;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests the Knowledge class.
 *
 * @author Joseph Ramsey
 */
public final class TestKnowledge extends TestCase {
    private Knowledge knowledge;

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestKnowledge(String name) {
        super(name);
    }

    @Override
	public final void setUp() {
        this.knowledge = new Knowledge();
    }

    public final void testForbiddenGroups() {
        Knowledge knowledge = new Knowledge();

        Set<String> from = createSet("x1", "x2");
        Set<String> to = createSet("x3");
        KnowledgeGroup group = new KnowledgeGroup(KnowledgeGroup.FORBIDDEN, from, to);
        knowledge.addKnowledgeGroup(group);

        from = createSet("x3");
        to = createSet("x5");
        group = new KnowledgeGroup(KnowledgeGroup.FORBIDDEN, from, to);
        knowledge.addKnowledgeGroup(group);


        assertTrue(knowledge.getKnowledgeGroups().size() == 2);
        assertTrue(knowledge.edgeForbidden("x1", "x3"));
        assertTrue(knowledge.edgeForbiddenByGroups("x1", "x3"));
        assertTrue(knowledge.edgeForbidden("x2", "x3"));
        assertTrue(knowledge.edgeForbiddenByGroups("x2", "x3"));
        assertTrue(knowledge.edgeForbidden("x3", "x5"));
        assertTrue(knowledge.edgeForbiddenByGroups("x3", "x5"));

        assertFalse(knowledge.edgeForbidden("x3", "x1"));
        assertFalse(knowledge.edgeForbidden("x2", "x1"));
    }


    public final void testRequiredGroups() {
        Knowledge knowledge = new Knowledge();

        Set<String> from = createSet("x1", "x2");
        Set<String> to = createSet("x3");
        KnowledgeGroup group = new KnowledgeGroup(KnowledgeGroup.REQUIRED, from, to);
        knowledge.addKnowledgeGroup(group);

        from = createSet("x3");
        to = createSet("x5");
        group = new KnowledgeGroup(KnowledgeGroup.REQUIRED, from, to);
        knowledge.addKnowledgeGroup(group);

        assertTrue(knowledge.getKnowledgeGroups().size() == 2);
        assertTrue(knowledge.edgeRequired("x1", "x3"));
        assertTrue(knowledge.edgeRequiredByGroups("x1", "x3"));
        assertTrue(knowledge.edgeRequired("x2", "x3"));
        assertTrue(knowledge.edgeRequiredByGroups("x2", "x3"));
        assertTrue(knowledge.edgeRequired("x3", "x5"));
        assertTrue(knowledge.edgeRequiredByGroups("x3", "x5"));

        assertFalse(knowledge.edgeRequired("x3", "x1"));
        assertFalse(knowledge.edgeRequired("x2", "x1"));
    }


    /**
     * Makes sure that setting fobidden edges works.
     */
    public final void testSetEdgeForbidden() {
        this.knowledge.clear();
        this.knowledge.setEdgeForbidden("X1", "X2", true);
        this.knowledge.setEdgeForbidden("X4", "X5", true);
        this.knowledge.setEdgeForbidden("X1", "X3", false);
        assertTrue(this.knowledge.edgeForbidden("X4", "X5"));
        assertTrue(!(this.knowledge.edgeForbidden("X1", "X3")));
        assertTrue(!(this.knowledge.edgeForbidden("X2", "X3")));
    }

    /**
     * Makes sure that setting required edges works.
     */
    public final void testSetEdgeRequired() {
        this.knowledge.clear();
        this.knowledge.setEdgeRequired("X1", "X2", true);
        this.knowledge.setEdgeRequired("X4", "X5", true);
        this.knowledge.setEdgeRequired("X1", "X3", false);
        assertTrue(this.knowledge.edgeRequired("X4", "X5"));
        assertTrue(!(this.knowledge.edgeRequired("X1", "X3")));
        assertTrue(!(this.knowledge.edgeRequired("X2", "X3")));
    }

    /**
     * Makes sure that forbidden and requiring edges are mutually exclusive.
     */
    public final void testForbiddenRequiredMix() {
        this.knowledge.clear();
        this.knowledge.setEdgeForbidden("X1", "X2", true);
        this.knowledge.setEdgeForbidden("X1", "X2", false);
        System.out.println(knowledge);
        this.knowledge.setEdgeRequired("X1", "X2", true);
        assertTrue(this.knowledge.edgeRequired("X1", "X2"));
        assertTrue(!this.knowledge.edgeForbidden("X1", "X2"));
        this.knowledge.setEdgeRequired("X1", "X2", false);
        this.knowledge.setEdgeForbidden("X1", "X2", true);
        assertTrue(!(this.knowledge.edgeRequired("X1", "X2")));
        assertTrue(this.knowledge.edgeForbidden("X1", "X2"));
    }

    public static void rtestLoadKnowledge() {
        try {
            String filename = "test_data/knowledge.txt";
            File file = new File(filename);

            DataReader reader = new DataReader();
            Knowledge knowledge = reader.parseKnowledge(file);
            System.out.println(knowledge);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void testSaveKnowledge() {
        Knowledge knowledge = new Knowledge();
        knowledge.addToTier(1, "x1");
        knowledge.addToTier(1, "x2");
        knowledge.addToTier(2, "x3");
        knowledge.addToTier(2, "x4");
        knowledge.addToTier(4, "x5");

        knowledge.setEdgeForbidden("x5", "x1", true);
        knowledge.setEdgeForbidden("x6", "x1", true);

        knowledge.setEdgeRequired("x1", "x3", true);
        knowledge.setEdgeRequired("x2", "x4", true);

        //        System.out.println(knowledge);
        //
        try {
            CharArrayWriter writer = new CharArrayWriter();
            Knowledge.saveKnowledge(knowledge, writer);
            System.out.println(writer.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testCopyKnowledge() {
        Knowledge knowledge = new Knowledge();
        knowledge.addToTier(0, "x1");
        knowledge.addToTier(0, "x2");
        knowledge.addToTier(1, "x3");
        knowledge.addToTier(1, "x4");
        knowledge.addToTier(3, "x6");
        knowledge.addToTier(3, "x7");
        knowledge.addToTier(4, "x5");

        Set<String> from = createSet("x10", "x11");
        Set<String> to = createSet("x12");

        KnowledgeGroup group = new KnowledgeGroup(KnowledgeGroup.REQUIRED, from, to);
        knowledge.addKnowledgeGroup(group);

        System.out.println(knowledge);

        Knowledge knowledge2 = new Knowledge(knowledge);

        System.out.println(knowledge2);

        assertEquals(knowledge, knowledge2);
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestKnowledge.class);
    }


    private static Set<String> createSet(String... vars) {
        HashSet<String> set = new HashSet<String>();
        for (String v : vars) {
            set.add(v);
        }
        return set;
    }

}



