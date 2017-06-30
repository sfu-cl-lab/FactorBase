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

import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.search.IndTestDSep;
import edu.cmu.tetrad.search.IndependenceTest;
import edu.cmu.tetrad.session.DelegatesEditing;
import edu.cmu.tetrad.util.JOptionUtils;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetrad.util.TetradLoggerConfig;
import edu.cmu.tetrad.util.TetradSerializable;
import javax.swing.*;
import javax.swing.border.MatteBorder;

import ca.sfu.jbn.model.dbDagWrapper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a workbench editing workbench area together with a toolbench for
 * editing tetrad-style graphs.
 *
 * @author Aaron Powers
 * @author Joseph Ramsey
 * @version $Revision: 6512 $ 
 */
public final class dbDagEditor extends JPanel
        implements GraphEditable, LayoutEditable, DelegatesEditing, IndTestProducer {
    private final GraphWorkbench workbench;
    private DagWrapper dagWrapper;

    public dbDagEditor(dbDagWrapper graphWrapper) {
        this(graphWrapper.getDag());
        this.dagWrapper = graphWrapper;
        //System.out.println("HI dbDag Editor constructor!");
    }

    public dbDagEditor(Dag dag) {
        setPreferredSize(new Dimension(550, 450));
        setLayout(new BorderLayout());

        this.workbench = new GraphWorkbench(dag);

        this.workbench.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                if ("graph".equals(evt.getPropertyName())) {
                    if (getDagWrapper() != null) {
                        getDagWrapper().setDag((Dag) evt.getNewValue());
                    }
                }
            }
        });

        DagGraphToolbar toolbar = new DagGraphToolbar(getWorkbench());
        JMenuBar menuBar = createGraphMenuBar();

        add(new JScrollPane(getWorkbench()), BorderLayout.CENTER);
        add(toolbar, BorderLayout.WEST);
        add(menuBar, BorderLayout.NORTH);

        JLabel label = new JLabel("Double click variable to change name.");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalStrut(2));
        b.add(label);
        b.add(Box.createHorizontalGlue());
        b.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));

        add(b, BorderLayout.SOUTH);

        this.workbench.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();

                if ("dag".equals(propertyName)) {
                    Dag _graph = (Dag) evt.getNewValue();

                    if (getWorkbench() != null) {
                        getDagWrapper().setDag(_graph);
                    }
                }
            }
        });

        this.workbench.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
			public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();

                if ("modelChanged".equals(propertyName)) {
                    firePropertyChange("modelChanged", evt.getOldValue(),
                            evt.getNewValue());
                }
            }
        });
    }

    /**
     * Sets the name of this editor.
     */
    @Override
	public final void setName(String name) {
        String oldName = getName();
        super.setName(name);
        firePropertyChange("name", oldName, getName());
    }

    @Override
	public JComponent getEditDelegate() {
        return getWorkbench();
    }

    @Override
	public GraphWorkbench getWorkbench() {
        return workbench;
    }

    /**
     * Returns a list of all the SessionNodeWrappers (TetradNodes) and
     * SessionNodeEdges that are model components for the respective
     * SessionNodes and SessionEdges selected in the workbench. Note that the
     * workbench, not the SessionEditorNodes themselves, keeps track of the
     * selection.
     *
     * @return the set of selected model nodes.
     */
    @Override
	public List getSelectedModelComponents() {
        List<Component> selectedComponents =
                getWorkbench().getSelectedComponents();
        List<TetradSerializable> selectedModelComponents =
                new ArrayList<TetradSerializable>();

        for (Object comp : selectedComponents) {
            if (comp instanceof DisplayNode) {
                selectedModelComponents.add(
                        ((DisplayNode) comp).getModelNode());
            }
            else if (comp instanceof DisplayEdge) {
                selectedModelComponents.add(
                        ((DisplayEdge) comp).getModelEdge());
            }
        }

        return selectedModelComponents;
    }

    /**
     * Pastes list of session elements into the workbench.
     */
    @Override
	public void pasteSubsession(List sessionElements, Point upperLeft) {
        getWorkbench().pasteSubgraph(sessionElements, upperLeft);
        getWorkbench().deselectAll();

        for (Object sessionElement : sessionElements) {
            if (sessionElement instanceof GraphNode) {
                Node modelNode = (Node) sessionElement;
                getWorkbench().selectNode(modelNode);
            }
        }

        getWorkbench().selectConnectingEdges();
    }

    @Override
	public Graph getGraph() {
        return workbench.getGraph();
    }

    @Override
	public void setGraph(Graph graph) {
        workbench.setGraph(graph);
    }

    @Override
	public Knowledge getKnowledge() {
        return null;
    }

    @Override
	public Graph getSourceGraph() {
        return getWorkbench().getGraph();
    }

    @Override
	public void layoutByGraph(Graph graph) {
        getWorkbench().layoutByGraph(graph);
    }

    @Override
	public void layoutByKnowledge() {
        // Does nothing.
    }

    @Override
	public Rectangle getVisibleRect() {
        return getWorkbench().getVisibleRect();
    }

    private DagWrapper getDagWrapper() {
        return dagWrapper;
    }

    private JMenuBar createGraphMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu editMenu = createEditMenu();
        JMenu graphMenu = createGraphMenu();

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        TetradLoggerConfig config = TetradLogger.getInstance().getTetradLoggerConfigForModel(DagWrapper.class);
        if(config != null){
            menuBar.add(new LoggingMenu(config));
        }
        menuBar.add(graphMenu);
        menuBar.add(new LayoutMenu(this));

        return menuBar;
    }

    /**
     * Creates the "file" menu, which allows the user to load, save, and post
     * workbench models.
     *
     * @return this menu.
     */
    private JMenu createEditMenu() {

        // TODO Add Cut and Delete.
        JMenu edit = new JMenu("Edit");

        JMenuItem copy = new JMenuItem(new CopySubgraphAction(this));
        JMenuItem paste = new JMenuItem(new PasteSubgraphAction(this));

        copy.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        paste.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

        edit.add(copy);
        edit.add(paste);

        return edit;
    }

    /**
     * Creates the "file" menu, which allows the user to load, save, and post
     * workbench models.
     *
     * @return this menu.
     */
    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");

        file.add(new SaveGraph(this, "Save Graph..."));
        file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
        file.add(new SaveComponentImage(workbench, "Save Graph Image..."));

        return file;
    }

    private JMenu createGraphMenu() {
        JMenu graph = new JMenu("Graph");

        graph.add(new GraphPropertiesAction(getWorkbench()));
        graph.add(new DirectedPathsAction(getWorkbench()));
        graph.add(new TreksAction(getWorkbench()));
        graph.add(new AllPathsAction(getWorkbench()));
        graph.add(new NeighborhoodsAction(getWorkbench()));

        JMenuItem randomDagModel = new JMenuItem("Random DAG");
        graph.add(randomDagModel);

        randomDagModel.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
                RandomDagEditor editor = new RandomDagEditor();

                int ret = JOptionPane.showConfirmDialog(
                        JOptionUtils.centeringComp(), editor,
                        "Edit Random DAG Parameters",
                        JOptionPane.PLAIN_MESSAGE);

                if (ret == JOptionPane.OK_OPTION) {
                    int numNodes = editor.getNumNodes();
                    int numLatentNodes = editor.getNumLatents();
                    int maxEdges = editor.getMaxEdges();
                    int maxDegree = editor.getMaxDegree();
                    int maxIndegree = editor.getMaxIndegree();
                    int maxOutdegree = editor.getMaxOutdegree();
                    boolean connected = editor.isConnected();

                    Dag dag = GraphUtils.createRandomDag(numNodes,
                            numLatentNodes, maxEdges, maxDegree, maxIndegree,
                            maxOutdegree, connected);

                    workbench.setGraph(dag);
                }
            }
        });

//        graph.addSeparator();
//        graph.add(new JMenuItem(new SelectBidirectedAction(getWorkbench())));
//        graph.add(new JMenuItem(new SelectUndirectedAction(getWorkbench())));

        graph.addSeparator();
        IndependenceFactsAction action = new IndependenceFactsAction(
                JOptionUtils.centeringComp(), this, "D Separation Facts...");
        graph.add(action);

        return graph;
    }

    @Override
	public IndependenceTest getIndependenceTest() {
        return new IndTestDSep(workbench.getGraph());
    }
}


