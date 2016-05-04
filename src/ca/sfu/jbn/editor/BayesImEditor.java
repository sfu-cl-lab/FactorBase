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

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Graph;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An editor for Bayes net instantiated models.  Assumes that the workbench and
 * parameterized model have been established (that is, that the nodes have been
 * identified and named and that the number and names of the values for the
 * nodes have been specified) and allows the user to set conditional
 * probabilities of node values given combinations of parent values.
 *
 * @author Aaron Powers
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 * @version $Revision: 5292 $ $Date: 2006-01-08 23:44:17 -0500 (Sun, 08 Jan
 *          2006) $
 */
public class BayesImEditor extends JPanel {

    /**
     * The wizard that allows the user to modify parameter values for this IM.
     */
    private BayesImEditorWizard wizard;

    /**
     * Constructs a new instanted model editor from a Bayes IM.
     */
    public BayesImEditor(BayesIm bayesIm) {
        if (bayesIm == null) {
            throw new NullPointerException("Bayes IM must not be null.");
        }

        BayesPm bayesPm = bayesIm.getBayesPm();
        Graph graph = bayesPm.getDag();
        GraphWorkbench workbench = new GraphWorkbench(graph);

        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        menuBar.add(file);
        //file.add(new SaveBayesImXmlAction(this));
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(workbench, "Save Graph Image..."));
        setLayout(new BorderLayout());
        add(menuBar, BorderLayout.NORTH);

        wizard = new BayesImEditorWizard(bayesIm, workbench);

        wizard.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                if ("editorValueChanged".equals(evt.getPropertyName())) {
                    firePropertyChange("modelChanged", null, null);
                }
            }
        });

        JScrollPane workbenchScroll = new JScrollPane(workbench);
        JScrollPane wizardScroll = new JScrollPane(getWizard());

        workbenchScroll.setPreferredSize(new Dimension(450, 450));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                workbenchScroll, wizardScroll);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(workbenchScroll.getPreferredSize().width);
        add(splitPane, BorderLayout.CENTER);

        setName("Bayes IM Editor");
        getWizard().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                if ("editorClosing".equals(evt.getPropertyName())) {
                    firePropertyChange("editorClosing", null, getName());
                }

                if ("closeFrame".equals(evt.getPropertyName())) {
                    firePropertyChange("closeFrame", null, null);
                    firePropertyChange("editorClosing", true, true);
                }

                if ("modelChanged".equals(evt.getPropertyName())) {
                    firePropertyChange("modelChanged", evt.getOldValue(),
                            evt.getNewValue());
                }
            }
        });
    }

    /**
     * Constructs a new instanted model editor from a Bayes IM wrapper.
     */
    public BayesImEditor(BayesImWrapper bayesImWrapper) {
        this(bayesImWrapper.getBayesIm());
    }

    /**
     * Constructs a new Bayes IM Editor from a Bayes estimator wrapper.
     */
    public BayesImEditor(BayesEstimatorWrapper bayesEstWrapper) {
        this(bayesEstWrapper.getEstimatedBayesIm());
    }

    /**
     * Constructs a new Bayes IM Editor from a Dirichlet Prior.
     */
    public BayesImEditor(DirichletEstimatorWrapper dirichletEstWrapper) {
        this(dirichletEstWrapper.getEstimatedBayesIm());
    }

    /**
     * Sets the name of this editor.
     */
    @Override
	public void setName(String name) {
        String oldName = getName();
        super.setName(name);
        firePropertyChange("name", oldName, getName());
    }

    /**
     * Returns a reference to this editor.
     */
    public BayesImEditorWizard getWizard() {
        return wizard;
    }
}


