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

package ca.sfu.jbn.editor;

import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.session.DelegatesEditing;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
/**
 * Displays a workbench editing workbench area together with a toolbench for
 * editing tetrad-style graphs.
 *
 * @author Aaron Powers
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 6401 $ $Date: 2006-01-05 16:37:15 -0500 (Thu, 05 Jan
 *          2006) $
 */
public class dbBayesPmEditor extends JPanel implements PropertyChangeListener, DelegatesEditing {

    /**
     * True iff the editing of measured variables is allowed.
     */
    private boolean editingMeasuredVariablesAllowed = true;

    /**
     * True iff the editing of latent variables is allowed.
     */
    private boolean editingLatentVariablesAllowed = false;

    /**
     * The wizard that lets the user edit values.
     */
    private BayesPmEditorWizard wizard;

    /**
     * Constructs a new editor for parameterized models (for now only for Bayes
     * net parameterized models).
     *
     * @param bayesPmWrapper the wrapper for the Bayes PM being displayed.
     */
    public dbBayesPmEditor(BayesPmWrapper bayesPmWrapper) {
        this(bayesPmWrapper.getBayesPm());
    }

    /**
     * Constructs a new editor for parameterized models (for now only for Bayes
     * net parameterized models).
     *
     * @param bayesPm the BayesPm being displayed
     */
    public dbBayesPmEditor(BayesPm bayesPm) {
        setLayout(new BorderLayout());

        Graph graph = bayesPm.getDag();
        GraphWorkbench workbench = new GraphWorkbench(graph);
        BayesPmEditorWizard wizard =
                new BayesPmEditorWizard(bayesPm, workbench);


        JScrollPane workbenchScroll = new JScrollPane(workbench);
        JScrollPane wizardScroll = new JScrollPane(wizard);

        workbenchScroll.setPreferredSize(new Dimension(450, 450));
        wizardScroll.setPreferredSize(new Dimension(450, 450));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                workbenchScroll, wizardScroll);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(workbenchScroll.getPreferredSize().width);
        add(splitPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        menuBar.add(file);
        //file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(workbench, "Save Graph Image..."));
        add(menuBar, BorderLayout.NORTH);

        setName("Bayes PM Editor");
        wizard.addPropertyChangeListener(this);

        wizard.setEditingLatentVariablesAllowed(isEditingLatentVariablesAllowed());
        wizard.setEditingMeasuredVariablesAllowed(isEditingMeasuredVariablesAllowed());
        this.wizard = wizard;
    }

    /**                                      G
     * Reacts to property change events.
     */
    @Override
	public void propertyChange(PropertyChangeEvent e) {
        if ("editorClosing".equals(e.getPropertyName())) {
            firePropertyChange("editorClosing", null, getName());
        }

        if ("closeFrame".equals(e.getPropertyName())) {
            firePropertyChange("closeFrame", null, null);
        }

        if ("modelChanged".equals(e.getPropertyName())) {
            firePropertyChange("modelChanged", e.getOldValue(),
                    e.getNewValue());
        }

    }

    /**
     * Sets the name fo the Bayes PM.
     */
    @Override
	public void setName(String name) {
        String oldName = getName();
        super.setName(name);
        firePropertyChange("name", oldName, getName());
    }

    @Override
	public JComponent getEditDelegate() {
        return wizard;
    }

    /**
     * True iff the editing of measured variables is allowed.
     */
    public boolean isEditingMeasuredVariablesAllowed() {
        return editingMeasuredVariablesAllowed;
    }

    /**
     * True iff the editing of measured variables is allowed.
     */
    public void setEditingMeasuredVariablesAllowed(boolean editingMeasuredVariablesAllowed) {
        this.editingMeasuredVariablesAllowed = editingMeasuredVariablesAllowed;
        wizard.setEditingMeasuredVariablesAllowed(isEditingMeasuredVariablesAllowed());
    }

    /**
     * True iff the editing of latent variables is allowed.
     */
    public boolean isEditingLatentVariablesAllowed() {
        return editingLatentVariablesAllowed;
    }

    /**
     * True iff the editing of latent variables is allowed.
     */
    public void setEditingLatentVariablesAllowed(boolean editingLatentVariablesAllowed) {
        this.editingLatentVariablesAllowed = editingLatentVariablesAllowed;
        wizard.setEditingLatentVariablesAllowed(isEditingLatentVariablesAllowed());
    }
}


