///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
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

package ca.sfu.jbn.model;

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.session.SessionModel;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Wraps a DataModel as a model class for a Session, providing constructors for
 * the parents of Tetrad that are specified by Tetrad.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6039 $ $Date: 2006-01-20 13:34:55 -0500 (Fri, 20 Jan
 *          2006) $
 */
public class DataWrapper implements SessionModel, KnowledgeEditable {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * Stores a reference to the data model being wrapped.
     *
     * @serial Cannot be null.
     */
    private DataModelList dataModelList;

    /**
     * Maps columns to discretization specs so that user's work is not forgotten
     * from one editing of the same data set to the next.
     *
     * @serial Cannot be null.
     */
    private Map discretizationSpecs = new HashMap();

    /**
     * Stores a reference to the source workbench, if there is one.
     *
     * @serial Can be null.
     */
    private Graph sourceGraph;

    /**
     * A list of known variables. Variables can be looked up in this list and
     * reused where appropriate.
     *
     * @serial Can be null.
     */
    private List<Node> knownVariables;

    //==============================CONSTRUCTORS===========================//

    /**
     * Constructs a data wrapper using a new DataSet as data model.
     */
    public DataWrapper() {
        setDataModel(new ColtDataSet(0, new LinkedList<Node>()));
        System.out.println("DataWrapper Generating");
    }


    /**
     * Copy constructor.
     *
     * @param wrapper
     */
    public DataWrapper(DataWrapper wrapper) {
    	System.out.println("re");
        this.name = wrapper.name;
        this.dataModelList = new DataModelList();
        for (int i = 0; i < wrapper.dataModelList.size(); i++) {
            if (wrapper.dataModelList.get(i) instanceof RectangularDataSet) {
                RectangularDataSet data = (RectangularDataSet) wrapper.dataModelList.get(i);
                this.dataModelList.add(copyData(data));
            }
        }
        if(wrapper.sourceGraph != null){
            this.sourceGraph = new EdgeListGraph(wrapper.sourceGraph);
        }
        if(wrapper.knownVariables != null){
            this.knownVariables = new ArrayList<Node>(wrapper.knownVariables);
        }
    }


    /**
     * Constructs a data wrapper using a new DataSet as data model.
     */
    public DataWrapper(RectangularDataSet dataSet) {
        setDataModel(dataSet);
    }

    public DataWrapper(Graph graph) {
        if (graph == null) {
            throw new NullPointerException();
        }

        List<Node> nodes = graph.getNodes();
        List<Node> variables = new LinkedList<Node>();

        for (Object node1 : nodes) {
            Node node = (Node) node1;
            String name = node.getName();
            NodeType nodetype = node.getNodeType();
            if (nodetype == NodeType.MEASURED) {
                ContinuousVariable var = new ContinuousVariable(name);
                variables.add(var);
            }
        }

        RectangularDataSet dataSet = new ColtDataSet(0, variables);
        this.dataModelList = new DataModelList();
        this.dataModelList.add(dataSet);
    }

    public DataWrapper(DagWrapper dagWrapper) {
        this(dagWrapper.getDag());
    }

    public DataWrapper(SemGraphWrapper wrapper) {
        this(wrapper.getGraph());
    }

    public DataWrapper(GraphWrapper wrapper) {
        this(wrapper.getGraph());
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static DataWrapper serializableInstance() {
        return new DataWrapper(DataUtils.discreteSerializableInstance());
    }

    //==============================PUBLIC METHODS========================//

    /**
     * Returns the list of models.
     */
    public DataModelList getDataModelList() {
        return this.dataModelList;
    }

    /**
     * Returns the data model for this wrapper.
     */
    public DataModel getSelectedDataModel() {
        DataModelList modelList = this.dataModelList;
        return modelList.getSelectedModel();
    }

    /**
     * Sets the data model.
     */
    public void setDataModel(DataModel dataModel) {
        if (dataModel == null) {
            dataModel = new ColtDataSet(0, new LinkedList<Node>());
        }

        if (dataModel instanceof DataModelList) {
            this.dataModelList = (DataModelList) dataModel;
        } else {
            this.dataModelList = new DataModelList();
            this.dataModelList.add(dataModel);
        }
    }

    @Override
	public Knowledge getKnowledge() {
        return getSelectedDataModel().getKnowledge();
    }

    @Override
	public void setKnowledge(Knowledge knowledge) {
        getSelectedDataModel().setKnowledge(knowledge);
    }

    @Override
	public List<String> getVarNames() {
        return getSelectedDataModel().getVariableNames();
    }

    /**
     * Returns the source workbench, if there is one.
     */
    @Override
	public Graph getSourceGraph() {
        return this.sourceGraph;
    }

    /**
     * Returns the variable names, in order.
     */
    public List getVariables() {
        return this.getSelectedDataModel().getVariables();
    }

    /**
     * Sets the source graph.
     */
    public void setSourceGraph(Graph sourceGraph) {
        this.sourceGraph = sourceGraph;
    }

    /**
     * Sets the source graph.
     */
    public void setKnownVariables(List<Node> variables) {
        this.knownVariables = variables;
    }

    public Map getDiscretizationSpecs() {
        return discretizationSpecs;
    }

    public List<Node> getKnownVariables() {
        return knownVariables;	
    }

    //=============================== Private Methods ==========================//

    private static DataModel copyData(RectangularDataSet data) {
        ColtDataSet newData = new ColtDataSet(data.getNumRows(), data.getVariables());
        for (int col = 0; col < data.getNumColumns(); col++) {
            for (int row = 0; row < data.getNumRows(); row++) {
                newData.setObject(row, col, data.getObject(row, col));
            }
        }
        newData.setKnowledge(new Knowledge(data.getKnowledge()));
        if (data.getName() != null) {
            newData.setName(data.getName());
        }
        return newData;
    }


    /**
     * Adds semantic checks to the default deserialization method. This method
     * must have the standard signature for a readObject method, and the body of
     * the method must begin with "s.defaultReadObject();". Other than that, any
     * semantic checks can be specified and do not need to stay the same from
     * version to version. A readObject method of this form may be added to any
     * class, even if Tetrad sessions were previously saved out using a version
     * of the class that didn't include it. (That's what the
     * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
     *
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (dataModelList == null) {
           //throw new NullPointerException();
        }

        if (discretizationSpecs == null) {
            //throw new NullPointerException();
        }
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }
}


