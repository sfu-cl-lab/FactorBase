package ca.sfu.cs.factorbase.database;

import java.util.List;
import java.util.Set;

import ca.sfu.cs.factorbase.data.ContingencyTable;
import ca.sfu.cs.factorbase.data.DataExtractor;
import ca.sfu.cs.factorbase.data.FunctorNodesInfo;
import ca.sfu.cs.factorbase.exception.DataBaseException;
import ca.sfu.cs.factorbase.exception.DataExtractionException;
import ca.sfu.cs.factorbase.graph.Edge;
import ca.sfu.cs.factorbase.lattice.RelationshipLattice;

/**
 * Methods expected to be implemented to enable the extraction of data from a database for FactorBase.
 */
public interface FactorBaseDataBase {
    /**
     * This method should setup all the extra databases/tables required for FactorBase to learn a
     * Bayesian network for the provided database.
     *
     * @throws DataBaseException if an error occurs when attempting to access the database.
     */
    void setupDatabase() throws DataBaseException;


    /**
     * This method should cleanup any content (e.g. databases/tables) that are generated by the other
     * methods of this interface and are no longer needed after FactorBase has finished running.
     *
     * @throws DataBaseException if an error occurs when attempting to access the database.
     */
    void cleanupDatabase() throws DataBaseException;


    /**
     * Retrieve all the edges that are not allowed for the given rnode IDs.
     *
     * @param rnodeIDs - a list of the rnode IDs to get the forbidden edges for.
     * @return a List of the edges that are forbidden for the given rnode IDs.
     *
     * @throws DataBaseException if an error occurs when attempting to retrieve the information.
     */
    List<Edge> getForbiddenEdges(List<String> rnodeIDs) throws DataBaseException;


    /**
     * Retrieve all the edges that are required for the given rnode IDs.
     *
     * @param rnodeIDs - a list of the rnode IDs to get the required edges for.
     * @return a List of the edges that are required for the given rnode IDs.
     *
     * @throws DataBaseException if an error occurs when attempting to retrieve the information.
     */
    List<Edge> getRequiredEdges(List<String> rnodeIDs) throws DataBaseException;


    /**
     * Retrieve the CT table {@code DataExtractor} for the given RNode/PVar ID.
     * <p>
     * Note: The {@code DataExtractor} for the given RNode/PVar ID should only be retrievable once and any references
     *       to it in the FactorBaseDataBase implementation should be removed.
     * </p>
     *
     * @param dataExtractorID - the RNode/PVar ID that we want to retrieve the {@code DataExtractor} for.
     * @return the CT table DataExtractor for the given RNode/PVar ID.
     *
     * @throws DataExtractionException if a non database error occurs when retrieving the DataExtractor.
     * @throws DataBaseException if a database error occurs when retrieving the DataExtractor.
     */
    DataExtractor getAndRemoveCTDataExtractor(String dataExtractorID) throws DataBaseException, DataExtractionException;


    /**
     * Retrieve the functor node information for all the PVariables.
     *
     * @return Information for all the functor nodes of each PVariable in the database.
     * @throws DataBaseException if an error occurs when attempting to retrieve the information.
     */
    List<FunctorNodesInfo> getPVariablesFunctorNodeInfo() throws DataBaseException;


    /**
     * Retrieve the contingency table for the given child and parent variables.
     *
     * @param functorInfos - {@code FunctorNodesInfo} with the ID that is the prefix of the "_counts" table to get the counts from.
     * @param child - the child variable to get the counts for.
     * @param parents - the parent variables to get the counts for.
     * @param totalNumberOfStates - the total number of combination of states between the child and parent variables.
     * @return contingency table for the given child and parent variables.
     * @throws DataBaseException if an error occurs when attempting to retrieve the information.
     */
    ContingencyTable getContingencyTable(FunctorNodesInfo functorInfos, String child, Set<String> parents, int totalNumberOfStates) throws DataBaseException;


    /**
     * Retrieve the relationship lattice for the entire database.
     *
     * @return the {@code RelationshipLattice} for the entire database.
     * @throws DataBaseException if an error occurs when attempting to retrieve the information.
     */
    RelationshipLattice getGlobalLattice() throws DataBaseException;
}