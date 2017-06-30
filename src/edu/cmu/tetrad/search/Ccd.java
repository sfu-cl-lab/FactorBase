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

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * This class provides the datastructures and methods for carrying out the Cyclic Causal Discovery algorithm (CCD)
 * described by Thomas Richardson and Peter Spirtes in Chapter 7 of Computation, Causation, & Discovery by Glymour and
 * Cooper eds.  The comments that appear below are keyed to the algorithm specification on pp. 269-271. </p> The search
 * method returns an instance of a Graph but it also constructs two lists of node triples which represent the underlines
 * and dotted underlines that the algorithm discovers.
 *
 * @author Frank C. Wimberly
 */
public final class Ccd implements GraphSearch {
    private IndependenceTest test;
    private int depth = -1;
    private Knowledge knowledge;
    private List<Node> nodes;

    /**
     * The logger for this class. The config needs to be set.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    /**
     * The arguments of the constructor are an oracle which answers conditional independence questions.  In the case of
     * a continuous dataset it will most likely be an instance of the IndTestCramerT class.  The second argument is not
     * used at this time.  The author (Wimberly) asked Thomas Richardson about how to use background knowledge and his
     * answer was that it should be applied after steps A-F had been executed.  Any implementation of the use of
     * background knowledge will be done later.
     *
     * @param knowledge Background knowledge. Not used yet--can be null.
     */
    public Ccd(IndependenceTest test, Knowledge knowledge) {
        this.knowledge = knowledge;
        this.test = test;
        this.nodes = new LinkedList<Node>(test.getVariables());
        Collections.shuffle(nodes);
    }

    /**
     * The arguments of the constructor are an oracle which answers conditional independence questions.  In the case of
     * a continuous dataset it will most likely be an instance of the IndTestCramerT class.  The second argument is not
     * used at this time.  The author (Wimberly) asked Thomas Richardson about how to use background knowledge and his
     * answer was that it should be applied after steps A-F had been executed.  Any implementation of the use of
     * background knowledge will be done later.
     */
    public Ccd(IndependenceTest test) {
        this(test, new Knowledge());
    }

    /**
     * The search method assumes that the IndependenceTest provided to the constructor is a conditional independence
     * oracle for the SEM (or Bayes network) which describes the causal structure of the population. The method returns
     * a PAG instantiated as a Tetrad GaSearchGraph which represents the equivalence class of digraphs which are
     * d-separation equivalent to the digraph of the underlying model (SEM or BN). </p> Although they are not returned
     * by the search method it also computes two lists of triples which, respectively store the underlines and dotted
     * underlines of the PAG.
     */
    @Override
	public Graph search() {
        TetradLogger.getInstance().log("info", "Starting CCD algorithm.");
        TetradLogger.getInstance().log("info", "Independence test = " + test);
        TetradLogger.getInstance().log("info", "Depth = " + depth);

        Graph phi = new EdgeListGraph(new LinkedList<Node>(nodes));

        Map<Triple, List<Node>> supSepset = new HashMap<Triple, List<Node>>();

        //Step A
        TetradLogger.getInstance().log("info", "\nStep A");

        int _depth = depth;

        if (_depth == -1) {
            _depth = Integer.MAX_VALUE;
        }

//        phi.fullyConnect(Endpoint.CIRCLE);
//        Fas search = new Fas(phi, test);
//        search.setDepth(_depth);
//        search.setKnowledge(getKnowledge());
//        search.setFci(true);
//        phi = search.search();
//        SepsetMap sepsets = search.getSepsets();

        // Using the faster FAS with sepsets, old code commented out above.

        Fas5 search = new Fas5(phi, test);
        search.setDepth(_depth);
        search.setKnowledge(getKnowledge());
        phi = search.search();
        SepsetMap sepsets = search.getSepsets();

//        Jpc search = new Jpc(test);
//        phi = search.search();
//        phi.reorientAllWith(Endpoint.CIRCLE);
//        SepsetMap sepsets = search.getSepsets();

        //Step B

        //For each triple of vertices A,B and C
        TetradLogger.getInstance().log("info", "\nStep B");

        for (Node bnode : nodes) {

            //Set of nodes adjacent to B
            List<Node> adjB = phi.getAdjacentNodes(bnode);

            if (adjB.size() < 2) {
                continue;
            }

            ChoiceGenerator cg = new ChoiceGenerator(adjB.size(), 2);
            int[] choice;

            while ((choice = cg.next()) != null) {
                Node anode = adjB.get(choice[0]);
                Node cnode = adjB.get(choice[1]);

//                if (phi.getEndpoint(anode, bnode) == Endpoint.ARROW && phi.getEndpoint(cnode, bnode) == Endpoint.ARROW) {
//                    continue;
//                }

                if (phi.isAdjacentTo(anode, cnode)) {
                    continue;
                }

                //Orient A*--*B*--*C as A->B<-C iff B not in sepset<A,C>
                if (!sepsets.get(anode, cnode).contains(bnode)) {
                    phi.setEndpoint(anode, bnode, Endpoint.ARROW);
                    phi.setEndpoint(bnode, anode, Endpoint.TAIL);
                    phi.setEndpoint(cnode, bnode, Endpoint.ARROW);
                    phi.setEndpoint(bnode, cnode, Endpoint.TAIL);
//
//                    phi.setEndpoint(anode, bnode, Endpoint.ARROW);
//                    phi.setEndpoint(cnode, bnode, Endpoint.ARROW);
//                    if (phi.getEndpoint(bnode, anode) == Endpoint.CIRCLE) {
//                        phi.setEndpoint(bnode, anode, Endpoint.TAIL);
//                    }
//
//                    if (phi.getEndpoint(bnode, cnode) == Endpoint.CIRCLE) {
//                        phi.setEndpoint(bnode, cnode, Endpoint.TAIL);
//                    }

                    TetradLogger.getInstance().log("colliderOrientations", "Orienting collider " + anode + "-->" +
                            bnode + "<--" + cnode);
                } else {
                    phi.addUnderlineTriple(anode, bnode, cnode);
                    TetradLogger.getInstance().log("underlines", "Adding underline " + new Triple(anode, bnode, cnode));
                }
            }
        }

        //Step C
        TetradLogger.getInstance().log("info", "\nStep C");

        for (int x = 0; x < nodes.size(); x++) {
            Node xnode = nodes.get(x);

            for (int y = 0; y < nodes.size(); y++) {

                // X,Y distinct
                if (x == y) {
                    continue;
                }

                Node ynode = nodes.get(y);

                // ...X and Y are adjacent...
                if (!phi.isAdjacentTo(xnode, ynode)) {
                    continue;
                }

                // Check each A
                for (int a = 0; a < nodes.size(); a++) {
                    if (a == x || a == y) {
                        continue;  //distinctness
                    }

                    Node anode = nodes.get(a);

                    //...A is not adjacent to X and A is not adjacent to Y...
                    if (phi.isAdjacentTo(xnode, anode) ||
                            phi.isAdjacentTo(ynode, anode)) {
                        continue;
                    }

                    //...X is not in sepset<A, Y>...
                    List<Node> _sepset = sepsets.get(anode, ynode);

                    if ((_sepset.contains(xnode))) {
                        continue;
                    }

                    IndependenceTest _test = new IndTestDSep(phi);

                    //If A and X are d-connected given SepSet<A, Y>
                    //then orient Xo-oY or Xo-Y as X<-Y.
                    if (!_test.isIndependent(anode, xnode, _sepset)) {  // Spec says dseparation, but dsepTest creates non-PAGs.
                        if (phi.getEndpoint(ynode, xnode) != Endpoint.CIRCLE) {
                            continue;
                        }

                        if (phi.getEndpoint(xnode, ynode) == Endpoint.ARROW) {
                            continue;
                        }

                        phi.setEndpoint(xnode, ynode, Endpoint.TAIL);
                        phi.setEndpoint(ynode, xnode, Endpoint.ARROW);

//                        phi.setEndpoint(ynode, xnode, Endpoint.TAIL);
//                        phi.setEndpoint(xnode, ynode, Endpoint.ARROW);

                        TetradLogger.getInstance().log("impliedOrientations", "Orienting " + ynode + "-->" + xnode);
                    }
                }
            }
        }

        //Step D
        TetradLogger.getInstance().log("info", "\nStep D");

        //Construct Local(phi, V) for each node V in phi
        List[] local = new ArrayList[nodes.size()];
        for (int v = 0; v < nodes.size(); v++) {
            Node vnode = nodes.get(v);
            local[v] = new ArrayList<Node>();

            //Is X p-adjacent to V in phi?
            for (int x = 0; x < nodes.size(); x++) {
                if (x == v) {
                    continue;  //TEST
                }
                Node xnode = nodes.get(x);
                if (phi.isAdjacentTo(vnode, xnode)) {
                    local[v].add(xnode);
                }

                //or is there a collider between X and V in phi?
                for (int y = 0; y < nodes.size(); y++) {
                    if (y == v || y == x) {
                        continue; //TEST
                    }
                    Node ynode = nodes.get(y);
                    if (phi.getEndpoint(xnode, ynode) == Endpoint.ARROW &&
                            phi.getEndpoint(ynode, xnode) == Endpoint.TAIL &&
                            phi.getEndpoint(vnode, ynode) == Endpoint.ARROW &&
                            phi.getEndpoint(ynode, vnode) == Endpoint.TAIL) {
                        local[v].add(xnode);
                    }
                }
            } //End x loop
        } //End v loop--Local(phi, V) now exists

        int m = 1;

        //maxCountLocalMinusSep is the largest cardinality of all sets of the
        //form Loacl(phi,A)\(SepSet<A,C> union {B,C})
        while (maxCountLocalMinusSep(phi, sepsets, local, phi) >= m) {
            for (int a = 0; a < nodes.size(); a++) {
                Node anode = nodes.get(a);

                for (int c = a + 1; c < nodes.size(); c++) {
//                    if (c == a) {
//                        continue;
//                    }

                    Node cnode = nodes.get(c);

                    // A and C are not adjacent.
                    if (phi.isAdjacentTo(anode, cnode)) {
                        continue;
                    }

                    for (int b = 0; b < nodes.size(); b++) {
                        if (b == c || b == a) {
                            continue;
                        }

                        Node bnode = nodes.get(b);

                        // This should never happen..
                        if (supSepset.get(new Triple(anode, bnode, cnode)) != null) {
                            continue;
                        }

                        // A-->B<--C
                        if (!isCollider(phi, anode, bnode, cnode)) {
                            continue;
                        }

                        //Compute the number of elements (count)
                        //in Local(phi,A)\(sepset<A,C> union {B,C})
                        Set<Node> localMinusSep = countLocalMinusSep(phi,
                                sepsets, local, anode, bnode, cnode);
                        int count = localMinusSep.size();

                        if (count < m) {
                            continue; //If not >= m skip to next triple.
                        }

                        //Compute the set T (setT) with m elements which is a subset of
                        //Local(phi,A)\(sepset<A,C> union {B,C})
                        Object[] v = new Object[count];
                        for (int i = 0; i < count; i++) {
                            v[i] = (localMinusSep.toArray())[i];
                        }

                        ChoiceGenerator generator = new ChoiceGenerator(count, m);
                        int[] choice;

                        while ((choice = generator.next()) != null) {
                            Set<Node> setT = new LinkedHashSet<Node>();
                            for (int i = 0; i < m; i++) {
                                setT.add((Node) v[choice[i]]);
                            }

                            setT.add(bnode);
                            setT.addAll(sepsets.get(nodes.get(a), nodes.get(c)));

                            List<Node> listT = new ArrayList<Node>(setT);

                            //Note:  B is a collider between A and C (see above).
                            //If anode and cnode are d-separated given T union
                            //sep[a][c] union {bnode} create a dotted underline triple
                            //<A,B,C> and record T union sepset<A,C> union {B} in
                            //supsepset<A,B,C> and in supsepset<C,B,A>

                            if (test.isIndependent(anode, cnode, listT)) {
//                                supsepset[a][b][c] = listT;
//                                supsepset[c][b][a] = listT;

                                supSepset.put(new Triple(anode, bnode, cnode), listT);

                                phi.addDottedUnderlineTriple(anode, bnode, cnode);
                                TetradLogger.getInstance().log("underlines", "Adding dotted underline: " +
                                        new Triple(anode, bnode, cnode));

                                break;
                            }
                        }
                    }
                }
            }

            m++;
        }

        //Step E
        TetradLogger.getInstance().log("info", "\nStep E");

        //Steps E and F require at least 4 vertices
        if (nodes.size() < 4) {
            return phi;
        }

        //If there is a quadruple <A,B,C,D> of distinct vertices such that...
        for (int a = 0; a < nodes.size(); a++) {
            Node nodeA = nodes.get(a);

            for (int b = 0; b < nodes.size(); b++) {
                if (b == a) {
                    continue; //Distinct
                }

                Node nodeB = nodes.get(b);

                for (int c = 0; c < nodes.size(); c++) {
                    if (c == a || c == b) {
                        continue; //Distinct
                    }

                    Node nodeC = nodes.get(c);

                    for (int d = 0; d < nodes.size(); d++) {
                        if (d == a || d == b || d == c) {
                            continue; //Distinct
                        }

                        Node nodeD = nodes.get(d);

                        //...B is a dotted underline collider in phi between A and C...
                        //...D is a collider betwen A and C in phi...
                        //...B and D are adjacent in phi...
                        //...A, D and C are not an underline triple...
                        if (isCollider(phi, nodeA, nodeB, nodeC) &&
                                phi.isDottedUnderlineTriple(nodeA, nodeB, nodeC) &&
                                isCollider(phi, nodeA, nodeD, nodeC) &&
                                /*!inTriplesList(nodeA, nodeD, nodeC,
                                        underLineTriples) &&*/ // Could be underlined or not.
                                phi.isAdjacentTo(nodeB, nodeD)) {

//                            if (supsepset[a][b][c].contains(nodeD)) {
                            if (supSepset.get(new Triple(nodeA, nodeB, nodeC)).contains(nodeD)) {
                                if (phi.getEndpoint(nodeB, nodeD) != Endpoint.CIRCLE) {
                                    continue;
                                }

                                // Orient B*-oD as B*-D
                                phi.setEndpoint(nodeB, nodeD, Endpoint.TAIL);
                                TetradLogger.getInstance().log("impliedOrientations", "Orienting " + nodeB + "*--" + nodeD);
                            } else {
                                if (phi.getEndpoint(nodeD, nodeB) == Endpoint.ARROW) {
                                    continue;
                                }

                                if (phi.getEndpoint(nodeB, nodeD) != Endpoint.CIRCLE) {
                                    continue;
                                }

                                // Or orient Bo-oD or B-oD as B->D...
                                TetradLogger.getInstance().log("impliedOrientations", "Orienting " + nodeB + "->" + nodeD);
                                phi.setEndpoint(nodeB, nodeD, Endpoint.ARROW);
                            }
                        }
                    }
                }
            }
        }

        //Step F
        TetradLogger.getInstance().log("info", "\nStep F");

        //For each quadruple <A,B,C,D> of distinct vertices...
        for (int a = 0; a < nodes.size(); a++) {
            Node nodeA = nodes.get(a);

            for (int b = 0; b < nodes.size(); b++) {
                if (b == a) {
                    continue;  //Distinct
                }
                Node nodeB = nodes.get(b);

                for (int c = 0; c < nodes.size(); c++) {
                    if (c == a || c == b) {
                        continue;  //Distinct
                    }
                    Node nodeC = nodes.get(c);

                    //...if A, B, C aren't a dotted underline triple get next triple...
                    if (!(new Triple(nodeA, nodeB, nodeC).alongPathIn(phi) &&
                            phi.isDottedUnderlineTriple(nodeA, nodeB, nodeC))) {
                        continue;
                    }

                    if (!isCollider(phi, nodeA, nodeB, nodeC)) {
                        continue;
                    }

                    for (int d = 0; d < nodes.size(); d++) {
                        if (d == a || d == b || d == c) {
                            continue;  //Distinct
                        }
                        Node nodeD = nodes.get(d);

                        //...and D is not adjacent to both A and C in phi...
                        if (phi.isAdjacentTo(nodeA, nodeD) &&
                                phi.isAdjacentTo(nodeC, nodeD)) {
                            continue;
                        }
                        //...and B and D are adjacent...
                        if (!phi.isAdjacentTo(nodeB, nodeD)) {
                            continue;
                        }

                        Set<Node> supSepUnionD = new LinkedHashSet<Node>();
                        supSepUnionD.add(nodeD);
                        supSepUnionD.addAll(supSepset.get(new Triple(nodeA, nodeB, nodeC)));
                        List<Node> listSupSepUnionD = new ArrayList<Node>(supSepUnionD);

                        //If A and C are a pair of vertices d-connected given
                        //SupSepset<A,B,C> union {D} then orient Bo-oD or B-oD
                        //as B->D in phi.

                        if (!test.isIndependent(nodeA, nodeC, listSupSepUnionD)) {
                            if (phi.getEndpoint(nodeB, nodeD) != Endpoint.CIRCLE) {
                                continue;
                            }

                            if (phi.getEndpoint(nodeD, nodeB) == Endpoint.ARROW) {
                                continue;
                            }

                            phi.setEndpoint(nodeB, nodeD, Endpoint.ARROW);
                            phi.setEndpoint(nodeD, nodeB, Endpoint.TAIL);
                            TetradLogger.getInstance().log("impliedOrientations", "Orienting " + nodeB + "->" + nodeD);
                        }
                    }
                }
            }
        }

        TetradLogger.getInstance().log("graph", "\nFinal Graph:");
        TetradLogger.getInstance().log("graph", phi.toString());

        this.logger.log("graph", "\nReturning this graph: " + phi);

        return phi;
    }

    @Override
	public long getElapsedTime() {

        return 0;
    }

    /**
     * Returns true if b is a collider between a and c in the GaSearchGraph phi; returns false otherwise.
     */
    private static boolean isCollider(Graph phi, Node a, Node b, Node c) {
        return !(phi.getEndpoint(a, b) != Endpoint.ARROW ||
                phi.getEndpoint(b, a) != Endpoint.TAIL ||
                phi.getEndpoint(c, b) != Endpoint.ARROW ||
                phi.getEndpoint(b, c) != Endpoint.TAIL);

    }

    /**
     * For a given GaSearchGraph phi and for a given set of sepsets, each of which is associated with a pair of vertices
     * A and C, computes and returns the set Local(phi,A)\(SepSet<A,C> union {B,C}).
     */
    private static Set<Node> countLocalMinusSep(Graph phi, SepsetMap sepset,
                                                List<Node>[] loc, Node anode,
                                                Node bnode, Node cnode) {
        List<Node> nodes = phi.getNodes();
        int a = nodes.indexOf(anode);
        Set<Node> localMinusSep = new HashSet<Node>();

        localMinusSep.addAll(loc[a]);
        localMinusSep.removeAll(sepset.get(anode, cnode));
        localMinusSep.remove(bnode);
        localMinusSep.remove(cnode);

        return localMinusSep;
    }

    /**
     * Computes and returns the size (cardinality) of the largest set of the form Local(phi,A)\(SepSet<A,C> union {B,C})
     * where B is a collider between A and C and where A and C are not adjacent.  A, B and C should not be a dotted
     * underline triple.
     */
    private static int maxCountLocalMinusSep(Graph phi, SepsetMap sep,
                                             List<Node>[] loc,
                                             Graph graph) {
        List<Node> nodes = phi.getNodes();
        int num = nodes.size();

        int maxCount = -1;

        for (int a = 0; a < num; a++) {
            Node anode = nodes.get(a);

            for (int c = a + 1; c < num; c++) {
                if (c == a) {
                    continue;
                }
                Node cnode = nodes.get(c);
                if (phi.isAdjacentTo(anode, cnode)) {
                    continue;
                }

                for (int b = 0; b < num; b++) {
                    if (b == a || b == c) {
                        continue;
                    }
                    Node bnode = nodes.get(b);
                    //Want B to be a collider between A and C but not for
                    //A, B, and C to be an underline triple.
                    if (phi.isUnderlineTriple(anode, bnode, cnode)) {
                        continue;
                    }
                    //Is B a collider between A and C?
                    if (!isCollider(phi, anode, bnode, cnode)) {
                        continue;
                    }

                    //int count =
                    //        countLocalMinusSep(phi, sep, loc, anode, bnode, cnode);
                    //
                    Set<Node> localMinusSep = countLocalMinusSep(phi, sep, loc,
                            anode, bnode, cnode);
                    int count = localMinusSep.size();

                    if (count > maxCount) {
                        maxCount = count;
                    }
                }
            }
        }

        return maxCount;
    }


    public Knowledge getKnowledge() {
        return knowledge;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

}




