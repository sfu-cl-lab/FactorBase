package ca.sfu.cs.factorbase.jbn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cs.factorbase.data.ContingencyTableGenerator;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.database.FactorBaseDataBase;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.exception.ScoringException;
import ca.sfu.cs.factorbase.graph.Edge;

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.Pattern;
import edu.cmu.tetrad.search.GesCT;
import edu.cmu.tetrad.search.PatternToDag;


public class BayesNet_Learning_main {


    public static List<Edge> tetradLearner(
        DataExtractor dataSource,
        boolean isDiscrete
    ) throws DataExtractionException, IOException, ScoringException {
        return tetradLearner(dataSource, null, null, isDiscrete);
    }


    public static List<Edge> tetradLearner(
        FactorBaseDataBase database,
        FunctorNodesInfo functorNodesInfo,
        boolean isDiscrete
    ) throws IOException, ScoringException {
        return tetradLearner(database, functorNodesInfo, null, null, isDiscrete);
    }


    public static List<Edge> tetradLearner(
        DataExtractor dataSource,
        List<Edge> requiredEdges,
        List<Edge> forbiddenEdges,
        boolean isDiscrete
    ) throws DataExtractionException, IOException, ScoringException {
        ContingencyTableGenerator dataset = new ContingencyTableGenerator(dataSource);

        GesCT gesSearch = new GesCT(
            dataset,
            10.0000,
            1.0000
        );

        Knowledge knowledge = new Knowledge();

        // Load required edge knowledge.
        if (requiredEdges != null) {
            for (Edge edge : requiredEdges) {
                knowledge.setEdgeRequired(edge.getParent(), edge.getChild(), true);
            }
        }

        // Load forbidden edge knowledge.
        if (forbiddenEdges != null) {
            for (Edge edge : forbiddenEdges) {
                knowledge.setEdgeForbidden(edge.getParent(), edge.getChild(), true);
            }
        }

        gesSearch.setKnowledge(knowledge);

        /* learn a dag from data */
        Graph graph = gesSearch.search();
        Pattern pattern = new Pattern(graph);

        PatternToDag p2d = new PatternToDag(pattern);
        Graph dag = p2d.patternToDagMeek();

        // Extract directed edge information.
        // Note: We use our Edge implementation to prevent us from becoming dependent on the Tetrad implementation,
        //       it will also make it easier to replace Tetrad if we need to in the future.
        int numberOfEdges = dag.getEdges().size();
        List<Edge> directedEdgesLearned = new ArrayList<Edge>(numberOfEdges);

        // for loop to extract the directed edge information for each node in the graph.
        for (Node childNode : dag.getNodes()) {
            List<Node> parentNodes = dag.getParents(childNode);
            if (parentNodes.isEmpty()) {
                directedEdgesLearned.add(new Edge("", childNode.getName()));
            } else {
                // for loop to extract the directed edge information for the given child node in the graph.
                for (Node parentNode : parentNodes) {
                    directedEdgesLearned.add(new Edge(parentNode.getName(), childNode.getName()));
                }
            }
        }

        return directedEdgesLearned;
    }


    public static List<Edge> tetradLearner(
        FactorBaseDataBase database,
        FunctorNodesInfo functorNodesInfo,
        List<Edge> requiredEdges,
        List<Edge> forbiddenEdges,
        boolean isDiscrete
    ) throws IOException, ScoringException {
        GesCT gesSearch = new GesCT(
            database,
            functorNodesInfo,
            10.0000,
            1.0000
        );

        Knowledge knowledge = new Knowledge();

        // Load required edge knowledge.
        if (requiredEdges != null) {
            for (Edge edge : requiredEdges) {
                knowledge.setEdgeRequired(edge.getParent(), edge.getChild(), true);
            }
        }

        // Load forbidden edge knowledge.
        if (forbiddenEdges != null) {
            for (Edge edge : forbiddenEdges) {
                knowledge.setEdgeForbidden(edge.getParent(), edge.getChild(), true);
            }
        }

        gesSearch.setKnowledge(knowledge);

        /* learn a dag from data */
        Graph graph = gesSearch.search();
        Pattern pattern = new Pattern(graph);

        PatternToDag p2d = new PatternToDag(pattern);
        Graph dag = p2d.patternToDagMeek();

        // Extract directed edge information.
        // Note: We use our Edge implementation to prevent us from becoming dependent on the Tetrad implementation,
        //       it will also make it easier to replace Tetrad if we need to in the future.
        int numberOfEdges = dag.getEdges().size();
        List<Edge> directedEdgesLearned = new ArrayList<Edge>(numberOfEdges);

        // for loop to extract the directed edge information for each node in the graph.
        for (Node childNode : dag.getNodes()) {
            List<Node> parentNodes = dag.getParents(childNode);
            if (parentNodes.isEmpty()) {
                directedEdgesLearned.add(new Edge("", childNode.getName()));
            } else {
                // for loop to extract the directed edge information for the given child node in the graph.
                for (Node parentNode : parentNodes) {
                    directedEdgesLearned.add(new Edge(parentNode.getName(), childNode.getName()));
                }
            }
        }

        return directedEdgesLearned;
    }
}