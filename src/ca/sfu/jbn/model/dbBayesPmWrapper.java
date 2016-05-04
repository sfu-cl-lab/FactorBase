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

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 * @version $Revision: 6039 $ $Date: 2006-01-19 17:54:39 -0500 (Thu, 19 Jan
 *          2006) $
 */
public class dbBayesPmWrapper extends BayesPmWrapper implements SessionModel, GraphSource {
  

	public dbBayesPmWrapper(AlgorithmRunner wrapper, DataWrapper dataWrapper) {
		super(wrapper, dataWrapper);
		// TODO Auto-generated constructor stub
	}

	static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * The wrapped BayesPm.
     *
     * @serial Cannot be null.
     */
    private BayesPm bayesPm;

    //==============================CONSTRUCTORS=========================//

    /**
     * Creates a new BayesPm from the given DAG and uses it to construct a new
     * BayesPm.
     */


    //=============================PUBLIC METHODS========================//

    @Override
	public BayesPm getBayesPm() {
        return this.bayesPm;
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

        if (bayesPm == null) {
            throw new NullPointerException();
        }
    }

    @Override
	public Graph getGraph() {
        return bayesPm.getDag();
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    //================================= Private Methods ==================================//

    private void log(BayesPm pm){
        TetradLogger.getInstance().setTetradLoggerConfigForModel(dbBayesPmWrapper.class);
        TetradLogger.getInstance().info("PM type = Bayes");
        TetradLogger.getInstance().log("pm", pm.toString());
        TetradLogger.getInstance().reset();
    }


}





