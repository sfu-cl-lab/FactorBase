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
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetrad.util.TetradLogger;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Wraps a Bayes Pm for use in the Tetrad application.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5766 $ $Date: 2005-07-25 17:14:55 -0400 (Mon, 25 Jul
 *          2005) $
 */
public class BayesImWrapper implements SessionModel, GraphSource {
    static final long serialVersionUID = 23L;

    /**
     * @serial Can be null.
     */
    private String name;

    /**
     * @serial Cannot be null.
     */
    protected BayesIm bayesIm;

    //===========================CONSTRUCTORS===========================//

    public BayesImWrapper(BayesPmWrapper bayesPmWrapper,
                          BayesImWrapper oldBayesImwrapper, BayesImParams params) {
        if (bayesPmWrapper == null) {
            throw new NullPointerException("BayesPmWrapper must not be null.");
        }

        if (params == null) {
            throw new NullPointerException("Params must not be null.");
        }

        BayesPm bayesPm = new BayesPm(bayesPmWrapper.getBayesPm());
        BayesIm oldBayesIm = oldBayesImwrapper.getBayesIm();

        if (params.getInitializationMode() == BayesImParams.MANUAL_RETAIN) {
            this.bayesIm = new MlBayesIm(bayesPm, oldBayesIm, MlBayesIm.MANUAL);
        } else if (params.getInitializationMode() == BayesImParams.RANDOM_RETAIN) {
            this.bayesIm = new MlBayesIm(bayesPm, oldBayesIm, MlBayesIm.RANDOM);
        } else if (params.getInitializationMode() == BayesImParams.RANDOM_OVERWRITE) {
            this.bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
        }
        log(bayesIm);
    }
    
	public BayesImWrapper(dbDataWrapper dataWrapper) {
		if (dataWrapper == null) {
			throw new NullPointerException(
			"BayesDataWrapper must not be null.");
		}

	}

    public BayesImWrapper(BayesEstimatorWrapper wrapper) {
        if (wrapper == null) {
            throw new NullPointerException();
        }
        this.bayesIm = wrapper.getEstimatedBayesIm();
        log(bayesIm);
    }

    public BayesImWrapper(BayesPmWrapper bayesPmWrapper, BayesImParams params) {
        if (bayesPmWrapper == null) {
            throw new NullPointerException("BayesPmWrapper must not be null.");
        }

        if (params == null) {
            throw new NullPointerException("Params must not be null.");
        }

        BayesPm bayesPm = new BayesPm(bayesPmWrapper.getBayesPm());

        if (params.getInitializationMode() == BayesImParams.MANUAL_RETAIN) {
            this.bayesIm = new MlBayesIm(bayesPm);
        } else if (params.getInitializationMode() == BayesImParams.RANDOM_RETAIN) {
            this.bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
        } else if (params.getInitializationMode() == BayesImParams.RANDOM_OVERWRITE) {
            this.bayesIm = new MlBayesIm(bayesPm, MlBayesIm.RANDOM);
        }
        log(bayesIm);
    }

    public BayesImWrapper(BayesImWrapper bayesImWrapper) {
        if (bayesImWrapper == null) {
            throw new NullPointerException();
        }

        this.bayesIm = new MlBayesIm(bayesImWrapper.getBayesIm());
        log(bayesIm);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static BayesImWrapper serializableInstance() {
        return new BayesImWrapper(BayesPmWrapper.serializableInstance(),
                BayesImParams.serializableInstance());
    }

    //=============================PUBLIC METHODS=========================//

    public BayesIm getBayesIm() {
        return this.bayesIm;
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

    //============================== private methods ============================//

    private void log(BayesIm im) {
        TetradLogger.getInstance().setTetradLoggerConfigForModel(this.getClass());
        TetradLogger.getInstance().info("IM type = Bayes");
        TetradLogger.getInstance().log("im", im.toString());
        TetradLogger.getInstance().reset();
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
    }


}



