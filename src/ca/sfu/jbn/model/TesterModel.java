package ca.sfu.jbn.model;

import edu.cmu.tetrad.session.SessionModel;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/**
 * Provides a simple model for notes that the user may want to add to the
 * session. Notes are stored as styled documents, on the theory that maybe
 * at some point the ability to add styling will be nice. Names are also
 * stored on the theory that maybe someday the name of the node can be
 * displayed in the interface. That day is not this day.
 *
 * @author Joseph Ramsey
 * @version $Revision$ $Date$
 */
public class TesterModel implements SessionModel {
    static final long serialVersionUID = 23L;

    private StyledDocument note = new DefaultStyledDocument();
    private String name;

    public TesterModel() {
        
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static TesterModel serializableInstance() {
        return new TesterModel();
    }

    public StyledDocument getNote() {
        return note;
    }

    public void setNote(StyledDocument note) {
        this.note = note;
    }

    @Override
	public void setName(String name) {
        this.name = name;
    }

    @Override
	public String getName() {
        return this.name;
    }
}
