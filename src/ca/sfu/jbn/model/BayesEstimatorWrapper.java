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

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.bayes.MlBayesEstimator;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5766 $ $Date: 2006-01-05 16:52:36 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class BayesEstimatorWrapper implements SessionModel, GraphSource {
	static final long serialVersionUID = 23L;

	/**
	 * @serial Cannot be null.
	 */
	private String name;

	/**
	 * @serial Cannot be null.
	 */
	protected BayesIm bayesIm;

	/**
	 * @serial Cannot be null.
	 */
	private RectangularDataSet dataSet;

	//=================================CONSTRUCTORS========================//


	public BayesEstimatorWrapper(dbDataWrapper dataWrapper) {
		if (dataWrapper == null) {
			throw new NullPointerException(
			"BayesDataWrapper must not be null.");
		}

		

		RectangularDataSet dataSet =
			(RectangularDataSet) dataWrapper.getSelectedDataModel();
		
		this.dataSet = dataSet;

	}
	public BayesEstimatorWrapper(DataWrapper dataWrapper,
			BayesPmWrapper bayesPmWrapper) {
		if (dataWrapper == null) {
			throw new NullPointerException(
					"BayesDataWrapper must not be null.");
		}

		if (bayesPmWrapper == null) {
			throw new NullPointerException("BayesPmWrapper must not be null");
		}

		RectangularDataSet dataSet =
			(RectangularDataSet) dataWrapper.getSelectedDataModel();
		BayesPm bayesPm = bayesPmWrapper.getBayesPm();

		this.dataSet = dataSet;

		estimate(dataSet, bayesPm);
		log(bayesPm);
	}

	public BayesEstimatorWrapper(DataWrapper dataWrapper,
			BayesImWrapper bayesImWrapper) {
		if (dataWrapper == null) {
			throw new NullPointerException(
					"BayesDataWrapper must not be null.");
		}

		if (bayesImWrapper == null) {
			throw new NullPointerException("BayesPmWrapper must not be null");
		}

		RectangularDataSet dataSet =
			(RectangularDataSet) dataWrapper.getSelectedDataModel();
		BayesPm bayesPm = bayesImWrapper.getBayesIm().getBayesPm();

		estimate(dataSet, bayesPm);
		log(bayesPm);                
	}

	/**
	 * Generates a simple exemplar of this class to test serialization.
	 *
	 * @see edu.cmu.TestSerialization
	 * @see edu.cmu.tetradapp.util.TetradSerializableUtils
	 */
	public static BayesEstimatorWrapper serializableInstance() {
		return new BayesEstimatorWrapper(DataWrapper.serializableInstance(),
				BayesPmWrapper.serializableInstance());
	}

	//==============================PUBLIC METHODS========================//

	public BayesIm getEstimatedBayesIm() {
		return this.bayesIm;
	}
	public DirichletBayesIm getEstimatedDirichletBayesIm() {
		DirichletBayesIm dbi=new DirichletBayesIm(this.bayesIm.getBayesPm());
		//    	int numNode=bayesIm.getNumNodes();
		//
		//    	for(int node=0;node<numNode;node++){
		//    		int numRow=bayesIm.getNumRows(node);
		//    		int numCol=bayesIm.getNumColumns(node);
		//    		for(int row=0;row<numRow;row++){
		//    			for(int col=0;col<numCol;col++){
		//    				
		//    				double prob=bayesIm.getProbability(node, row, col);
		//    				dbi.setProbability(node, row, col, prob);
		//    			}
		//    		}
		//    	}

		return dbi;
		//    	return this.bayesIm;
	}

	private void estimate(RectangularDataSet DataSet, BayesPm bayesPm) {
		Graph graph = bayesPm.getDag();

		for (Object o : graph.getNodes()) {
			Node node = (Node) o;
			if (node.getNodeType() == NodeType.LATENT) {
				throw new IllegalArgumentException("Estimation of Bayes IM's " +
				"with latents is not supported.");
			}
		}

		try {
			MlBayesEstimator estimator = new MlBayesEstimator();
			this.bayesIm = estimator.estimate(bayesPm, DataSet);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new RuntimeException("Value assignments between Bayes PM " +
			"and discrete data set do not match.");
		}
	}

	public RectangularDataSet getDataSet() {
		return this.dataSet;
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

		if (bayesIm == null) {
			throw new NullPointerException();
		}

		if (dataSet == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public Graph getGraph() {
		return bayesIm.getBayesPm().getDag();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	//======================== Private Methods ======================//

	private void log(BayesPm pm) {
		TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(this.getClass());
		if (config != null) {
			TetradLogger.getInstance().setTetradLoggerConfig(config);
			TetradLogger.getInstance().info("Estimated Bayes IM:");
			TetradLogger.getInstance().log("im", "" + pm);
			TetradLogger.getInstance().reset();
		}
	}


}


