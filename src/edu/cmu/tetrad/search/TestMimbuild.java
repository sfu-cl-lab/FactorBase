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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.cluster.ClusterUtils;

import java.util.*;
import java.io.File;
import java.io.FileWriter;

/**
 * Tests the BooleanFunction class.
 *
 * @author Joseph Ramsey
 */
public class TestMimbuild extends TestCase {

    /**
     * Standard constructor for JUnit test cases.
     */
    public TestMimbuild(String name) {
        super(name);
    }


    public void testMimbuild() {
//        RandomUtil.getInstance().setSeed(127679241L);
        long seed = RandomUtil.getInstance().getSeed();
        System.out.println(seed);

        boolean arrangeGraph = false;
        boolean acyclic = false;

        int numLatents = 20;
        int numLatentEdges = numLatents;
        int numImpurities = 0; //2 * numLatents;
        int numMeasurementsPerLatent = 7;
        int maxClusterSelectionSize = 10;
        int minClusterSize = 5;
        int maxClusterSize = 10;
        int numPurifyCovSamples = 1000;
        double purifyAlpha = 1e-9;
        double mimbuildAlpha = .0001;

        System.out.println("Make random MIM.");

        Graph mim = GraphUtils.randomMim(numLatents, numLatentEdges, numMeasurementsPerLatent,
                numImpurities, numImpurities, 0, arrangeGraph, acyclic);

        System.out.println("Simulate data.");

        LargeSemSimulator sim = new LargeSemSimulator(mim);
        DataSet data = sim.simulateDataAcyclic(1000);

        System.out.println("Form a random selection of the clusters");

        List<Node> latents = new ArrayList<Node>();

        for (Node node : mim.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                latents.add(node);
            }
        }

        List<List<Node>> clustering = ClusterUtils.mimClustering(latents, mim, data);
//        List<List<Node>> clusterSelection = clustering;
        List<List<Node>> clusterSelection = ClusterUtils.getClusterSelection(maxClusterSelectionSize, data, clustering);

        List<Node> subClusterNodes = new ArrayList<Node>(ClusterUtils.getAllNodesInClusters(clusterSelection));
        DataSet subClusterData = data.subsetColumns(subClusterNodes);

//        DataUtils.zeroMean(subClusterData);


        TetradTest test = new ContinuousTetradTest(subClusterData, TestType.TETRAD_WISHART, 0.000001);

        IPurify purify = new PurifyTetradBasedD(test);
//        IPurify purify = new PurifyTetradBasedH(test, maxClusterSize);
        List<List<Node>> _clustering = purify.purify(clusterSelection);

        Mimbuild2 mimbuild = new Mimbuild2();
        mimbuild.setMinClusterSize(minClusterSize);
        mimbuild.setNumCovSamples(numPurifyCovSamples);
        mimbuild.setTrueMim(mim);
        mimbuild.setLatentsBeforeSearch(latents);
        mimbuild.setAlpha(mimbuildAlpha);
        Graph _graph = mimbuild.search(_clustering, subClusterData);
        List<Node> _latents = mimbuild.latentsAfterSearch();

        Graph _structuralGraph = mim.subgraph(_latents);
        Graph _structuralGraphPattern = SearchGraphUtils.patternForDag(_structuralGraph);
//        IndependenceTest dsepTest = new IndTestDSep(_structuralGraph, true);
//        Graph _structuralGraphPattern = new Pc(dsepTest).search(_graph.getNodes());

        int adjFp = GraphUtils.adjacenciesComplement(_graph, _structuralGraphPattern).size();
        int adjFn = GraphUtils.adjacenciesComplement(_structuralGraphPattern, _graph).size();

        int arrowFp = GraphUtils.arrowEndpointComplement(_graph, _structuralGraphPattern);
        int arrowFn = GraphUtils.arrowEndpointComplement(_structuralGraphPattern, _graph);

        System.out.println("adj fp = " + adjFp + " adj fn = " + adjFn);
        System.out.println("arrow fp = " + arrowFp + " + arrow fn = " + arrowFn);

        String s = GraphUtils.graphComparisonString("_graph", _graph, "_structuralGraph", _structuralGraph, true);
        System.out.println(s);

    }

    public void test1() {

        long start = System.currentTimeMillis();

        int numVars = 5;

        Graph trueGraph = GraphUtils.randomDag(numVars, numVars, false);

        System.out.println("A");

        LargeSemSimulator sim = new LargeSemSimulator(trueGraph);
        DataSet data = sim.simulateDataAcyclic(1000);

        System.out.println("B");

        IndependenceTest test = new IndTestFisherZ2(data, 1e-6);

        System.out.println("C");

        Cpc2 search = new Cpc2(test);
        search.setDepth(3);
        Graph pattern = search.search();

//        Mbfs search = new Mbfs(test, 3);
//        Graph pattern = search.search(data.getVariable(0));

//        Jpc jpc = new Jpc(test);
//        jpc.setMaxAdjacencies(5);
//        jpc.setMaxDescendantPath(20);
//        jpc.setMaxIterations(100);


        System.out.println("D");

        System.out.println(pattern);

        Graph truePattern = SearchGraphUtils.patternForDag(trueGraph);
//        GraphUtils.replaceNodes(pattern, truePattern.getNodes());

        System.out.println("D1");

        int adjFp = GraphUtils.adjacenciesComplement(pattern, truePattern).size();
        int adjFn = GraphUtils.adjacenciesComplement(truePattern, pattern).size();

        System.out.println("D2");

        int arrowFp = GraphUtils.arrowEndpointComplement(pattern, truePattern);
        int arrowFn = GraphUtils.arrowEndpointComplement(truePattern, pattern);

        System.out.println("D3");

        System.out.println("adj fp = " + adjFp + " adj fn = " + adjFn);
        System.out.println("arrow fp = " + arrowFp + " + arrow fn = " + arrowFn);

//        String s = GraphUtils.graphComparisonString("pattern", pattern, "truePattern", truePattern, true);
//        System.out.println(s);

        System.out.println("D4");

        writeGraphToFile(pattern, "test_data/sub_9_run_1_graph3.xml");

        System.out.println("D5");

        long stop = System.currentTimeMillis();

        System.out.println("Elapsed: " + (stop - start) / 1000. + " seconds");

    }

    public void rtest3() {

        String id = "exp2.5000var";

        long start = System.currentTimeMillis();

        int numVars = 500;

        Graph trueGraph = GraphUtils.randomDag(numVars, 0, numVars, 6, 6, 6, false);

        System.out.println("A");

        LargeSemSimulator sim = new LargeSemSimulator(trueGraph);
        DataSet data = sim.simulateDataAcyclic(2000);
//
//        SemPm pm = new SemPm(trueGraph);
//        SemIm im = new SemIm(pm);
//        DataSet data = im.simulateData(1000, false);

        System.out.println("B");

        IndependenceTest test = new IndTestFisherZ(data, 1e-6);

        System.out.println("C");

        Cpc2 cpc = new Cpc2(test);
        cpc.setDepth(3);
        Graph cpcPattern = cpc.search();


//        test.setAlpha(1e-3);

        writeGraphToFile(cpcPattern, "test_data/" + id + ".cpcGraph.xml");

        Graph truePattern = SearchGraphUtils.patternForDag(trueGraph);
//        GraphUtils.replaceNodes(pattern, truePattern.getNodes());

        System.out.println("D1");

        System.out.println("CPC pattern stats");

        int adjFp = GraphUtils.adjacenciesComplement(cpcPattern, truePattern).size();
        int adjFn = GraphUtils.adjacenciesComplement(truePattern, cpcPattern).size();

        System.out.println("D2");

        int arrowFp = GraphUtils.arrowEndpointComplement(cpcPattern, truePattern);
        int arrowFn = GraphUtils.arrowEndpointComplement(truePattern, cpcPattern);

        System.out.println("D3");

        System.out.println("adj fp = " + adjFp + " adj fn = " + adjFn);
        System.out.println("arrow fp = " + arrowFp + " + arrow fn = " + arrowFn);

//        Mbfs search = new Mbfs(test, 3);
//        Graph pattern = search.search(data.getVariable(0));

//        Jpc jcpc = new Jpc(test);
//        jcpc.setAlgorithmType(Jpc.AlgorithmType.CPC);
//        jcpc.setInitialGraph(cpcPattern);
//        jcpc.setMaxAdjacencies(15);
//        jcpc.setMaxDescendantPath(25);
//        jcpc.setMaxIterations(40);
//        jcpc.setKnowledge(new BlankKnowledge());
//
//
        Jcpc jcpc = new Jcpc(test);
        jcpc.setInitialGraph(cpcPattern);
        jcpc.setMaxAdjacencies(15);
        jcpc.setMaxDescendantPath(15);
        jcpc.setMaxIterations(40);
        jcpc.setKnowledge(new BlankKnowledge());


        Graph pattern = jcpc.search();

        System.out.println("D");

        System.out.println(pattern);

        System.out.println("CPC pattern stats");

        System.out.println("D1");

        int _adjFp = GraphUtils.adjacenciesComplement(pattern, truePattern).size();
        int _adjFn = GraphUtils.adjacenciesComplement(truePattern, pattern).size();

        System.out.println("D2");

        int _arrowFp = GraphUtils.arrowEndpointComplement(pattern, truePattern);
        int _arrowFn = GraphUtils.arrowEndpointComplement(truePattern, pattern);

        System.out.println("D3");

        System.out.println("adj fp = " + _adjFp + " adj fn = " + _adjFn);
        System.out.println("arrow fp = " + _arrowFp + " + arrow fn = " + _arrowFn);

//        String s = GraphUtils.graphComparisonString("pattern", pattern, "truePattern", truePattern, true);
//        System.out.println(s);

        System.out.println("D4");

        writeGraphToFile(pattern, "test_data/" + id + ".finalJpcGraph.xml");

        System.out.println("D5");

        long stop = System.currentTimeMillis();

        System.out.println("Elapsed: " + (stop - start) / 1000. + " seconds");

    }

    private void writeGraphToFile(Graph pattern, String path) {
        try {
            String xml = GraphUtils.graphToXml(pattern);
//            System.out.println(xml);

            File _file = new File(path);

            FileWriter writer = new FileWriter(_file);

            writer.write(xml);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method uses reflection to collect up all of the test methods from this class and return them to the test
     * runner.
     */
    public static Test suite() {

        // Edit the name of the class in the parens to match the name
        // of this class.
        return new TestSuite(TestMimbuild.class);
    }
}
