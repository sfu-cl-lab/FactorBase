package ca.sfu.cs.factorbase.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

public class TSVContingencyTable implements ContingencyTable {

    private boolean isDiscrete;
    private Map<String, Set<String>> variableStates = new HashMap<String, Set<String>>();
    private Set<String> variableNames = new HashSet<String>();
    private Map<Set<RandomVariableAssignment>, Long> data = new HashMap<Set<RandomVariableAssignment>, Long>();


    /**
     * Contingency table based on a TSV data source.
     *
     * @param sourceFile - path to the TSV file containing the CT table information.
     * @param countsColumn - the name of the column indicating the counts for each random variable assignment.
     * @param isDiscrete - true if the dataset only contains discrete information; otherwise false.
     * @throws IOException if unable to process the given TSV file.
     */
    public TSVContingencyTable (String sourceFile, String countsColumn, boolean isDiscrete) throws IOException {
        this.isDiscrete = isDiscrete;
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
            String[] header = reader.readLine().split("\t");
            this.variableNames = Sets.newHashSet(header);
            this.variableNames.remove(countsColumn);
            String row;

            // while loop to process and extract information from the given file.
            while ((row = reader.readLine()) != null) {

                String[] data = row.split("\t");
                Set<RandomVariableAssignment> attributes = new HashSet<RandomVariableAssignment>();
                long counts = -1L;

                // for loop to process the data rows.
                for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
                    String variableName = header[columnIndex];
                    String value = data[columnIndex];
                    if (variableName.equals(countsColumn)) {
                        counts = Long.valueOf(value);
                    } else {
                        attributes.add(new RandomVariableAssignment(variableName, value));

                        if (!this.variableStates.containsKey(variableName)) {
                            this.variableStates.put(variableName, new HashSet<String>());
                        }

                        this.variableStates.get(variableName).add(value);
                    }
                }

                this.data.put(attributes, counts);
            }
        }
    }


    @Override
    public int getNumberOfStates(String variable) {
        Set<String> states = this.variableStates.getOrDefault(variable, new HashSet<String>());
        return states.size();
    }


    @Override
    public Set<List<RandomVariableAssignment>> getStates(Set<String> variables) {
        List<Set<RandomVariableAssignment>> variableStateCombinations = new ArrayList<Set<RandomVariableAssignment>>();

        for (String variable : variables) {
            Set<RandomVariableAssignment> states = new HashSet<RandomVariableAssignment>();
            states.addAll(this.getStates(variable).stream().map(
                state -> new RandomVariableAssignment(variable, state)
            ).collect(Collectors.toSet()));
            variableStateCombinations.add(states);
        }

        return Sets.cartesianProduct(variableStateCombinations);
    }


    @Override
    public Set<String> getStates(String variable) {
        return this.variableStates.getOrDefault(variable, new HashSet<String>());
    }


    @Override
    public long getCounts(Set<RandomVariableAssignment> randomVariableAssignments) {
        if (randomVariableAssignments.size() == this.variableNames.size()) {
            return this.data.getOrDefault(randomVariableAssignments, 0L);
        } else {
            long count = 0L;

            for (Set<RandomVariableAssignment> assignments : this.data.keySet()) {
                if (assignments.containsAll(randomVariableAssignments)) {
                    count += this.data.get(assignments);
                }
            }

            return count;
        }
    }


    @Override
    public Set<String> getVariableNames() {
        return this.variableNames;
    }


    @Override
    public boolean isDiscrete() {
        return this.isDiscrete;
    }
}