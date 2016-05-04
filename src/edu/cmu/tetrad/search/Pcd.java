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
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.search.SepsetMap;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.List;

/**
 * Implements the PCD algorithm, a modification of the PC algorithm intended to address search issues when deterministic
 * relationships exist among variables in the data set. Two changes are made to PC. First, the fast adjacency search is
 * modified so that if set Z of variables determines X or Y, the step that removes edge X---Y if X _||_ Y | Z is
 * skipped. Second, the collider orientation rule is modified. In the original version, X*--Y--*Z is oriented as
 * X*->Y<-*Z if Y is not in the Sepset(X, Z). In the modified version, X*--Y--*Z is oriented as X*->Y<-*Z if Y is not
 * determined by Sepset(X, Z). The algorithm modifications are due to Clark Glymour.
 *
 * @author Joseph Ramsey
 */
public class Pcd implements GraphSearch {

    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * Forbidden and required edges for the search.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * Sepset information accumulated in the search.
     */
    private SepsetMap sepset;

    /**
     * The maximum number of nodes conditioned on in the search.
     */
    private int depth = Integer.MAX_VALUE;

    /**
     * The graph that's constructed during the search.
     */
    private EdgeListGraph graph;

    //=============================CONSTRUCTORS==========================//

    public Pcd(IndependenceTest independenceTest) {
        if (independenceTest == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
        this.setKnowledge(knowledge);

    }

    //==============================PUBLIC METHODS========================//

    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public SepsetMap getSepset() {
        return sepset;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Runs PC starting with a fully connected graph.
     */
    @Override
	public Graph search() {
        TetradLogger.getInstance().log("info", "Starting PCD algorithm.");
        TetradLogger.getInstance().log("info", "Independence test = " + independenceTest + ".");
        long startTime = System.currentTimeMillis();

        if (getIndependenceTest() == null) {
            throw new NullPointerException();
        }

        List<Node> nodes = getIndependenceTest().getVariables();
        graph = new EdgeListGraph(nodes);
        graph.fullyConnect(Endpoint.TAIL);

        FasDeterministic fas = new FasDeterministic(graph, getIndependenceTest());
        fas.setKnowledge(getKnowledge());
        fas.setDepth(getDepth());

        this.sepset = fas.search();

        // The orientation methods have been moved to SearchGraphUtils
        // since there was interest from other algorithms in using them.
        Knowledge knowledge1 = getKnowledge();
        TetradLogger.getInstance().log("info", "Starting PCD Orientation.");

        SearchGraphUtils.pcOrientbk(knowledge1, graph, nodes);
        SearchGraphUtils.pcdOrientC(getSepset(), getIndependenceTest(), knowledge1, graph);
        MeekRules rules = new MeekRules();
        rules.setKnowledge(knowledge1);
        rules.orientImplied(graph);

        TetradLogger.getInstance().log("info", "Finishing PCD Orientation");

        TetradLogger.getInstance().log("graph", "\nReturning this graph: " + graph);
        long endTime = System.currentTimeMillis();
        TetradLogger.getInstance().log("info", "Elapsed time = " + (endTime - startTime) / 1000. + " s");
        TetradLogger.getInstance().log("info", "Finishing PC algorithm.");
        TetradLogger.getInstance().flush();
        return graph;
    }

    @Override
	public long getElapsedTime() {
        return 0;
    }

    public Graph getPartialGraph() {
        return new EdgeListGraph(graph);
    }

    //=============================PRIVATE METHODS========================//

}


