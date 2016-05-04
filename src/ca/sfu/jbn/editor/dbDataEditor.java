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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.Graph;
import javax.swing.*;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.model.dbDataWrapper;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Displays data objects and allows users to edit these objects as well as load
 * and save them.
 *
 * @author Joseph Ramsey
 */
public final class dbDataEditor extends JPanel implements PropertyChangeListener {

	/**
	 * The data wrapper being displayed.
	 */
	private dbDataWrapper dataWrapper;

	/**
	 * A tabbed pane containing displays for all data models and displaying
	 * 'dataModel' currently.
	 */
	private JTabbedPane tabbedPane = new JTabbedPane();

	JToggleButton button = new JToggleButton();
	JLabel label = new JLabel();
	JLabel label2 = new JLabel();
	//==========================CONSTUCTORS===============================//

	/**
	 * Constructs the data editor with an empty list of data displays.
	 */
	public dbDataEditor() {
		//System.out.println("dfs");
	}

	public dbDataEditor(GraphComparison comparison) {
		this(new dbDataWrapper(comparison.getDataSet()));
	}


	/**
	 * Constructs a standalone data editor.
	 */
	public dbDataEditor(dbDataWrapper dataWrapper) {

		if (dataWrapper == null) {
			throw new NullPointerException("Data wrapper must not be null.");
		}
		this.dataWrapper = dataWrapper;

		setLayout(new BorderLayout());

		reset();
		askdata(dataWrapper);
	}

	private void askdata(dbDataWrapper dataWrapper){

	}
	//==========================PUBLIC METHODS=============================//




	/**
	 * Replaces the current Datamodels with the given one. Note, that by calling this
	 * you are removing ALL the current data-models, they will be lost forever!
	 *
	 * @param model - The model, must not be null
	 */


	/**
	 * Sets this editor to display contents of the given data model wrapper.
	 */
	public final void reset() {


		tabbedPane().removeAll();
		setPreferredSize(new Dimension(200, 100));

		DataModelList dataModelList = dataWrapper.getDataModelList();
		DataModel selectedModel = dataWrapper.getSelectedDataModel();

		removeAll();
		removeEmptyModels(dataModelList);

		int selectedIndex = -1;




		label.setText("Schema: "+global.schema);
		label2.setText("dbURL: "+global.dbURL);
		add(menuBar(), BorderLayout.NORTH);
		//add(button, BorderLayout.CENTER);
		add(label, BorderLayout.CENTER);
		add(label2, BorderLayout.SOUTH);
		validate();
	}


	/**
	 * Returns the data sets that's currently in front.
	 */


	public Graph getSourceGraph() {
		return dataWrapper.getSourceGraph();
	}

	/**
	 * Retrieves the data wrapper for this editor (read-only).
	 */
	public dbDataWrapper getDataWrapper() {
		return this.dataWrapper;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("modelChanged".equals(evt.getPropertyName())) {
			firePropertyChange("modelChanged", null, null);
		}
	}

	//=============================PRIVATE METHODS======================//

	private static void removeEmptyModels(DataModelList dataModelList) {
		for (int i = dataModelList.size() - 1; i >= 0; i--) {
			DataModel dataModel = dataModelList.get(i);

			if (dataModel instanceof RectangularDataSet &&
					((RectangularDataSet) dataModel).getNumColumns() == 0) {
				if (dataModelList.size() > 1) {
					dataModelList.remove(dataModel);
				}
			}
		}
	}

	private JTable getSelectedJTable() {
		Object display = tabbedPane().getSelectedComponent();

		if (display instanceof DataDisplay) {
			return ((DataDisplay) display).getDataDisplayJTable();
		} else if (display instanceof CovMatrixDisplay) {
			return ((CovMatrixDisplay) display).getCovMatrixJTable();
		}

		return null;
	}

	private JMenuBar menuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		menuBar.add(file);


		JMenuItem fileItem = new JMenuItem("Load DB");
		file.add(fileItem);
		
		JMenuItem setConnection = new JMenuItem("Set Connection");
		file.add(setConnection);
		
		setConnection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dbtempname = JOptionPane.showInputDialog(tabbedPane, "Input dbURL:",global.dbURL);
				global.dbURL=dbtempname;
				
				dbtempname = JOptionPane.showInputDialog(tabbedPane, "Input dbUser:",global.dbUser);
				global.dbUser=dbtempname;
				
				dbtempname = JOptionPane.showInputDialog(tabbedPane, "Input dbPassword:",global.dbPassword);
				global.dbPassword=dbtempname;
				
				label2.setText("dbURL: "+global.dbURL);
						
			}
		});
		

		JMenuItem fileItem12 = new JMenuItem("Parameter Learning");
		file.add(fileItem12);

		file.setMnemonic('F');
		
		
		setConnection.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));


		fileItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));

		//elwin added:
			fileItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String dbtempname = JOptionPane.showInputDialog(tabbedPane, "input dbname",global.schema);
					global.schema=dbtempname;

					label.setText("Schema: "+global.schema);
				}
			});

			fileItem12.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (global.schema==null) msgbox("Database is null");
					else {
						String method = JOptionPane.showInputDialog(tabbedPane, "input Method(log/cpt/ls/mbn)");
						dataWrapper.parameterLearning(method);
						msgbox("Finished output!");
					}
				}
			});

			return menuBar;
	}

	public void msgbox(String information){
		JOptionPane.showMessageDialog(tabbedPane, information);
	}

	private static String tabName(Object dataModel, int i) {
		String tabName = ((DataModel) dataModel).getName();

		if (tabName == null) {
			tabName = "Data Set " + i;
		}

		return tabName;
	}

	/**
	 * Returns the data display for the given model.
	 */
	private JComponent dataDisplay(Object model) {
		if (model instanceof RectangularDataSet) {
			DataDisplay dataDisplay = new DataDisplay((RectangularDataSet) model);
			dataDisplay.addPropertyChangeListener(this);
			return dataDisplay;
		} else if (model instanceof CovarianceMatrix) {
			CovMatrixDisplay covMatrixDisplay = new CovMatrixDisplay((CovarianceMatrix) model);
			covMatrixDisplay.addPropertyChangeListener(this);
			return covMatrixDisplay;
		} else if (model instanceof TimeSeriesData) {
			return new TimeSeriesDataDisplay((TimeSeriesData) model);
		} else {
			throw new IllegalArgumentException("Unrecognized data type.");
		}
	}

	private JTabbedPane tabbedPane() {
		return tabbedPane;
	}
}

