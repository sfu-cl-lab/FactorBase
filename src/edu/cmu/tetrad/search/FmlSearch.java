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

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemOptimizerScattershot;
import edu.cmu.tetrad.sem.SemPm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a set of variables, reports the DAG over these variables that maximizes the p value of the estimated SEM model
 * for this DAG as against the estimated SEM model for all alternative DAGs.
 * <p/>
 * Should only be attempted on very small graphs, out of respect for the combinatorial explosion of number of DAGs for
 * graphs as the number of variables increases.
 *
 * @author Joseph Ramsey
 */
public class FmlSearch {
    private DataSet data;
    private List<Node> nodes;
    private double alpha;
    private int maxEdges;
    private Dag trueDag;
    private SemIm trueIm;

    public FmlSearch(DataSet data, double alpha, int maxEdges) {
        if (data == null || !data.isContinuous()) {
            throw new IllegalArgumentException("Please provide a continuous dataset.");
        }

        if (alpha < 0) {
            throw new IllegalArgumentException("Alpha must be >= 0: " + alpha);
        }

        if (maxEdges < 0) {
            throw new IllegalArgumentException("Max edges must be >= 0: " + maxEdges);
        }

        this.data = data;
        this.nodes = data.getVariables();
        this.alpha = 0.05;
        this.maxEdges = maxEdges;
    }

    public void setTrueDag(Dag dag) {
        this.trueDag = dag;
    }

    public void setTrueIm(SemIm trueIm) {
        this.trueIm = trueIm;
    }

    public List<Graph> search() {
        List<Graph> currentPatterns = new ArrayList<Graph>();
        currentPatterns.add(new EdgeListGraph(nodes));

        while (true) {

            // Among all of the current patterns, find the pattern + edge
            // combinations that result in the minimal FML scores for those
            // combinations.
            List<Map<Edge, Double>> edgeFmls = new ArrayList<Map<Edge, Double>>();

            for (Graph pattern : currentPatterns) {
                Graph dag = SearchGraphUtils.dagFromPattern(pattern);

                Map<Edge, Double> _edgeFmls = new LinkedHashMap<Edge, Double>();
                edgeFmls.add(_edgeFmls);

                for (int i = 0; i < nodes.size(); i++) {
                    for (int j = 0; j < i; j++) {
                        Node node1 = nodes.get(i);
                        Node node2 = nodes.get(j);

                        if (dag.isAdjacentTo(node1, node2)) {
                            continue;
                        }

                        if (!dag.existsDirectedPathFromTo(node2, node1)) {
                            dag.addDirectedEdge(node1, node2);
                            Edge edge1 = dag.getEdge(node1, node2);
                            _edgeFmls.put(edge1, fml(dag));
                            dag.removeEdge(edge1);
                        }

                        if (!dag.existsDirectedPathFromTo(node1, node2)) {
                            dag.addDirectedEdge(node2, node1);
                            Edge edge2 = dag.getEdge(node2, node1);
                            _edgeFmls.put(edge2, fml(dag));
                            dag.removeEdge(edge2);
                        }
                    }
                }
            }

            double minFml = Double.POSITIVE_INFINITY;

            for (Map<Edge, Double> _edgeFmls : edgeFmls) {
                for (Edge edge : _edgeFmls.keySet()) {
                    if (_edgeFmls.get(edge) < minFml) {
                        minFml = _edgeFmls.get(edge);
                    }
                }
            }

            System.out.println("Min fml = " + minFml);

            for (Map<Edge, Double> _edgeFmls : edgeFmls) {
                for (Edge edge : _edgeFmls.keySet()) {
                    if (_edgeFmls.get(edge) >= minFml && _edgeFmls.get(edge) < 1.1 * minFml) {
                        System.out.println("Nearby: " + edge + " FML = " + _edgeFmls.get(edge));
                    }
                }
            }

            for (Map<Edge, Double> _edgeFmls : edgeFmls) {
                Map<Edge, Double> copy = new LinkedHashMap<Edge, Double>(_edgeFmls);

                for (Edge edge : copy.keySet()) {
                    if (_edgeFmls.get(edge) > minFml) {
                        _edgeFmls.remove(edge);
                    }
                }
            }

            // Collect up all the distinct patterns that result from adding
            // the edges to DAGs from previous patterns that result in
            // the minimal FML score.
            List<Graph> newCurrentPatterns = new ArrayList<Graph>();

            for (int i = 0; i < edgeFmls.size(); i++) {
                Graph pattern = currentPatterns.get(i);
                Map<Edge, Double> _edgeFmls = edgeFmls.get(i);
                Graph graph = SearchGraphUtils.dagFromPattern(pattern);

                for (Edge edge : _edgeFmls.keySet()) {
                    Graph _graph = new EdgeListGraph(graph);
                    _graph.addEdge(edge);

                    Node head = Edges.getDirectedEdgeHead(edge);

                    for (Node node : graph.getNodesOutTo(head, Endpoint.ARROW)) {
                        graph.removeEdge(head, node);
                        graph.addDirectedEdge(head, node);
                    }

                    Graph newPattern = SearchGraphUtils.patternFromDag(_graph);

                    if (!newCurrentPatterns.contains(newPattern)) {
                        newCurrentPatterns.add(newPattern);
                    }
                }
            }

            // Find a best DAG in each putative patterns and base a revised
            // pattern on that.
            List<Graph> newCurrentPatternsRevised = new ArrayList<Graph>();

            for (int i = 0; i < newCurrentPatterns.size(); i++) {
                Graph pattern = newCurrentPatterns.get(i);
                DagInPatternIterator iterator = new DagInPatternIterator(pattern);

                double _minFml = Double.POSITIVE_INFINITY;
                Graph aBestDag = null;

                while (iterator.hasNext()) {
                    Graph dag = iterator.next();
                    double _fml = fml(dag);

                    if (_fml < _minFml) {
                        _minFml = _fml;
                        aBestDag = dag;
                    }
                }

                newCurrentPatternsRevised.add(SearchGraphUtils.patternFromDag(aBestDag));
            }

            currentPatterns = newCurrentPatternsRevised;

            System.out.println("New current Patterns = " + currentPatterns);

            if (currentPatterns.isEmpty()) {
                return new ArrayList<Graph>();
            }

            // If at this point the patterns are significant (all DAGs in
            // each pattern should have the same p value), pick a DAG in
            // each pattern, trim out any edge that when removed results in
            // a model with p value greater than alpha, and return the
            // resulting list of models.
            double pValue = pValue(SearchGraphUtils.dagFromPattern(currentPatterns.get(0)));

            if (pValue > alpha) {
                System.out.println("P value of found models = " + pValue);

                List<Graph> trimmedPatterns = new ArrayList<Graph>();

                for (Graph pattern : currentPatterns) {
                    trimmedPatterns.add(trimPattern(pattern));
                }

                currentPatterns = trimmedPatterns;

                System.out.println("True DAG " + trueDag);
                System.out.println("FML = " + fml(trueDag));
                System.out.println("P Value of true model = " + pValue(trueDag));

                for (int i = 0; i < currentPatterns.size(); i++) {
                    System.out.println("Output pattern # " + (i + 1));
                    System.out.println(currentPatterns.get(i));
                    System.out.println("P Value of that = " + pValue(SearchGraphUtils.dagFromPattern(currentPatterns.get(i))));
                }

                return currentPatterns;
            }
        }
    }

    private Graph trimPattern(Graph pattern) {
        Graph dag = SearchGraphUtils.dagFromPattern(pattern);

        for (Edge edge : dag.getEdges()) {
            dag.removeEdge(edge);
            double _pValue = pValue(dag);

//            System.out.println("Trying to remove " + edge + " p value = " + _pValue);

            if (_pValue < alpha) {
                dag.addEdge(edge);
            } else {
                System.out.println("Removing " + edge);
            }
        }

        return SearchGraphUtils.patternFromDag(dag);
    }


    private double fml(Graph graph) {
        SemPm semPm = new SemPm(graph);
//        SemEstimator semEstimator = new SemEstimator(data, semPm, new SemOptimizerRegression());
        SemEstimator semEstimator = new SemEstimator(data, semPm, new SemOptimizerScattershot());
        semEstimator.estimate();
        SemIm estimatedSem = semEstimator.getEstimatedSem();
        return estimatedSem.getFml();
    }

    private double pValue(Graph graph) {
        SemPm semPm = new SemPm(graph);
        SemEstimator semEstimator = new SemEstimator(data, semPm, new SemOptimizerScattershot());
        semEstimator.estimate();
        SemIm estimatedSem = semEstimator.getEstimatedSem();
        return estimatedSem.getPValue();
    }

    public Graph convertToPattern(List<Dag> dags) {
        if (dags == null || dags.isEmpty()) {
            return null;
        }

        Graph pattern = new EdgeListGraph(dags.get(0));

        for (int i = 1; i < dags.size(); i++) {
            Dag dag = dags.get(i);

            for (Edge edge : pattern.getEdges()) {
                if (!dag.isAdjacentTo(edge.getNode1(), edge.getNode2())) {

                    System.out.println("Not all DAGs have the same adjacencies");
                    return null;
                }
            }

            for (Edge edge : dag.getEdges()) {
                if (!pattern.isAdjacentTo(edge.getNode1(), edge.getNode2())) {
                    System.out.println("Not all DAGs have the same adjacencies");
                    return null;
                }
            }

            for (Edge patternEdge : pattern.getEdges()) {
                if (Edges.isUndirectedEdge(patternEdge)) {
                    continue;
                }

                Node node1 = patternEdge.getNode1();
                Node node2 = patternEdge.getNode2();
                Edge dagEdge = dag.getEdge(node1, node2);

                if (Edges.getDirectedEdgeHead(patternEdge) != Edges.getDirectedEdgeHead(dagEdge)) {
                    pattern.removeEdge(patternEdge);
                    pattern.addUndirectedEdge(node1, node2);
                }
            }
        }

        return pattern;
    }
}
