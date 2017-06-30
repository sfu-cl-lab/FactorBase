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
/////////////////////////////////////////////////////////////////////////////
package ca.sfu.jbn.model;

import edu.cmu.tetrad.graph.Dag;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphUtils;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.TetradLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import ca.sfu.jbn.common.Parser;

/**
 * Holds a tetrad dag with all of the constructors necessary for it to serve as
 * a model for the tetrad application.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2005-05-23 15:53:09 -0400 (Mon, 23 May
 *          2005) $
 */
public class dbDagWrapper extends DagWrapper{
 

	public dbDagWrapper(BayesEstimatorWrapper wrapper) {
		super(wrapper);
		// TODO Auto-generated constructor stub
		//System.out.println("HI! dbDagWrapper constr");
		
		dag=wrapper.getEstimatedBayesIm().getBayesPm().getDag();
				
		for(Node x : dag.getNodes()){
			//System.out.println("Node is "+x.getName());
			int flag =0;
			
			
			for(Object t : Parser.getInstance().getRel_att()){
				//System.out.println("t is "+t.toString());
				if (t.toString().contains(x.getName())) flag=1;
			}
			
			for(Object t : Parser.getInstance().getEntity_att()){
				//System.out.println("t is "+t.toString());
				if (t.toString().contains(x.getName())) flag=2;
			}
			
			if (x.getName().contains("B(")){
				flag=3;
			}
			
			if (flag==1){
				String tableName = Parser.getInstance().getTableOfField(x.getName());
				x.setName(x.getName()+"(");
				for(Object s : Parser.getInstance().getRefEntities(tableName)){
					//System.out.println("S is "+s.toString());
					x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+",");
				}
				x.setName(x.getName().substring(0,x.getName().length()-1)+")");
			}
			else if(flag==2){
				String tableName = Parser.getInstance().getTableOfField(x.getName());
				x.setName(x.getName()+"(");
				for(Object s : Parser.getInstance().getEntityPrimaryKey(tableName)){
					//System.out.println("S is "+s.toString());
					x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+",");
				
				}
				x.setName(x.getName().substring(0,x.getName().length()-1)+")");
			}
			else if(flag==3){
				String st = x.getName();
				String tableName = st.substring(st.indexOf("(")+1,st.indexOf(")"));
				x.setName("B_"+tableName);
				//System.out.println("relation table name:"+tableName);
				x.setName(x.getName()+"(");
				for(Object s : Parser.getInstance().getRefEntities(tableName)){
					//System.out.println("S is "+s.toString());
					x.setName(x.getName()+s.toString().toUpperCase().charAt(0)+",");
				
				}
				x.setName(x.getName().substring(0,x.getName().length()-1)+")");
			}
			
			
			if(x.getName().contains("dummy")){
				//System.out.println("Find Dummy!");
				x.setName(x.getName().replace("dummy", "aux"));
			}

		}
		
		
	}

	static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    private Dag dag;

    //=============================CONSTRUCTORS==========================//

     

    //================================PUBLIC METHODS=======================//

    @Override
	public Dag getDag() {
        return dag;
    }

    @Override
	public void setDag(Dag graph) {
        this.dag = graph;
    }

    //============================PRIVATE METHODS========================//


    private void log(){
        TetradLogger.getInstance().setTetradLoggerConfigForModel(dbDagWrapper.class);
        TetradLogger.getInstance().info("Graph type = DAG");
        TetradLogger.getInstance().log("graph", "Graph = " + dag);
        TetradLogger.getInstance().reset();
    }



    private void createRandomDag(GraphParams params) {
        dag = GraphUtils.createRandomDag(params.getNumNodes(),
                params.getNumLatents(), params.getMaxEdges(),
                params.getMaxDegree(), params.getMaxIndegree(),
                params.getMaxOutdegree(), params.isConnected());
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

        if (dag == null) {
            //throw new NullPointerException();
        }
    }

    @Override
	public Graph getGraph() {
        return dag;
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


