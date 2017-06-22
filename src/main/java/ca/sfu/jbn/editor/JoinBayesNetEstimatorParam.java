package ca.sfu.jbn.editor;

import java.io.IOException;
import java.io.ObjectInputStream;

public class JoinBayesNetEstimatorParam implements Params {
    static final long serialVersionUID = 23L;

    /**
     * The symmetric alpha that should be used.
     *
     * @serial Range (0, +inf).
     */
    private double symmetricAlpha = 1.0;

    //=============================CONSTRUCTORS==========================//

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public JoinBayesNetEstimatorParam() {
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static JoinBayesNetEstimatorParam serializableInstance() {
        return new JoinBayesNetEstimatorParam();
    }

    //=============================PUBLIC METHODS========================//

    public double getSymmetricAlpha() {
        return symmetricAlpha;
    }

    public void setSymmetricAlpha(double symmetricAlpha) {
        if (symmetricAlpha < 0.0) {
            throw new IllegalArgumentException(
                    "Symmetric alpha should be >= 0: " + symmetricAlpha);
        }

        this.symmetricAlpha = symmetricAlpha;
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

        if (symmetricAlpha <= 0.0) {
            throw new IllegalArgumentException(
                    "Symmetric alpha invalid: " + symmetricAlpha);
        }
    }
}
