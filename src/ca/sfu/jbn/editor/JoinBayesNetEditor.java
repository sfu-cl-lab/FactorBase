package ca.sfu.jbn.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.SQLException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import ca.sfu.Evaluation.ReadSQL_MLN_Files;
import ca.sfu.autocorrelation.CorrelatedSQLToXML;
import ca.sfu.jbn.SQLtoXML.ReadXML;
import ca.sfu.jbn.analyzeMLN.analyze;
import ca.sfu.jbn.common.Parser;
import ca.sfu.jbn.common.global;
import ca.sfu.jbn.model.JoinBayesNetEstimatorWrapper;
import ca.sfu.jbn.parameterLearning.Decisiontree;
import ca.sfu.jbn.structureLearning.S_learning;
import cs.sfu.jbn.alchemy.ExportToMLN;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.Graph;

public class JoinBayesNetEditor extends JPanel implements LayoutEditable {

	private BayesImEditorWizard wizard;
	private BayesPm bayesPm;
	private GraphWorkbench workbench = null;

	public JoinBayesNetEditor(BayesIm bayesIm) {
		if (bayesIm == null) {
			throw new NullPointerException("Bayes IM must not be null.");
		}

		bayesPm = bayesIm.getBayesPm();
		Graph graph = bayesPm.getDag();

		// elwin added

		workbench = new GraphWorkbench(graph);

		JMenuBar menuBar = new JMenuBar();

		JMenu file = new JMenu("File");
		menuBar.add(file);
		file.add(new SaveScreenshot(this, true, "Save Screenshot..."));
		file.add(new SaveComponentImage(workbench, "Save Graph Image..."));

		// file.setAccelerator(
		// KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		file.setMnemonic('F');

		JMenu export = new JMenu("Export");
		menuBar.add(export);

		export.setMnemonic('E');

		JMenuItem sOut = new JMenuItem("Structure Output");
		export.add(sOut);

		sOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));

		JMenuItem pOut = new JMenuItem("Parameter Learning Output");
		export.add(pOut);

		pOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));

		JMenuItem dOut = new JMenuItem("Decision Tree with Weight");
		export.add(dOut);

		dOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.CTRL_MASK));

		JMenuItem dsOut = new JMenuItem("Decision Tree Structure");
		export.add(dsOut);

		dsOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.CTRL_MASK));

		JMenu tools = new JMenu("Tools");
		/* Yuke disables the MLN analyzer on July 18, 2012 */
//		menuBar.add(tools);

		tools.setMnemonic('T');

		// JMenuItem setConnection = new JMenuItem("Set Connection");
		// tools.add(setConnection);
		//		
		// setConnection.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// openFile("config.xml");
		//		
		// }
		// });
		//		
		JMenuItem mlncheck = new JMenuItem("MLN Analyzer");
		tools.add(mlncheck);

		menuBar.add(new LayoutMenu(this));

		sOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				output("mbn");
				openMLN();
				msgbox("Export MLN successfully!");
			}
		});

		pOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String method = inputBox("Input method (mbn/log/lsn)");
				if (!method.equals(""))
				{
					output(method);
					openMLN();
					msgbox("Export MLN successfully!");
				}
			}
		});

		dOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				outputDecisionTree("Weight");
				openMLN("DT");
				msgbox("Export MLN successfully!");
			}
		});

		dsOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				outputDecisionTree("Structure");
				openMLN("DT");
				msgbox("Export MLN successfully!");
			}
		});

		mlncheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				analyzeMln();
			}
		});

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
					firePropertyChange("modelChanged", evt.getOldValue(), evt
							.getNewValue());
				}
			}
		});

	}

	public void analyzeMln() {
		String method = inputBox("See VJ or DT?");
		String file = "";
		if (method.equals("VJ")) {
			file = global.WorkingDirectory + "/" + global.schema + "_VJ.mln";
		} else if (method.equals("DT")) {
			file = global.WorkingDirectory + "/" + global.schema
					+ "_DecisionTree.mln";
		}
		analyze a = new analyze();
		a.process(file);
		openFile("result.txt");
	}

	public void openFile(String file) {
		Runtime load = Runtime.getRuntime();
		String program = "\"C:\\WINDOWS\\system32\\write.exe\"";
		try {
			load.exec(program + " " + file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void openMLN() {
		Runtime load = Runtime.getRuntime();
		String program = "\"C:\\WINDOWS\\system32\\write.exe\"";
		String file = global.WorkingDirectory + "/" + global.schema
				+ "_VJ.mln";
		try {
			load.exec(program + " " + file);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void openMLN(String method) {
		if (method.equals("DT")) {
			Runtime load = Runtime.getRuntime();
			String program = "\"C:\\WINDOWS\\system32\\write.exe\"";
			String file = global.WorkingDirectory + "/" + global.schema
					+ "_DecisionTree.mln";
			try {
				load.exec(program + " " + file);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void outputDecisionTree(String method) {

		// prepare
		global.initialize();

		System.out.println("Starting MLN parameter learning package on "
				+ global.schema);
		ReadXML sqlToXMLReader = new CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		Parser.initialize();

		ReadSQL_MLN_Files r = new ReadSQL_MLN_Files();
		try {
			r.initialize();
			PrintStream out1 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + ".db"));
			PrintStream out2 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
							+ "predicate.mln"));
			PrintStream out3 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
							+ "predicate_temp.mln"));

			r.read(out1, out2, out3);

			System.out.println("DB file and MLN predicate file created");

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long l = System.currentTimeMillis();
		S_learning sLearn = new S_learning(2);
		// System.out.println("Stucture learning begins");
		BayesPm bayes = sLearn.major();
		long l2 = System.currentTimeMillis();
		System.out.print("SLtime(ms):   ");
		System.out.println(l2 - l);

		// Preparing();
		try {
			System.out
					.println("Structure learning using Decision Trees begins");
			long l3 = System.currentTimeMillis();
			Decisiontree tree = new Decisiontree(bayes);
			String rules = null;
			if (method.equals("Weight"))
				rules = tree.decisionTreeLearner("dtlsn");
			else 
				rules = tree.decisionTreeLearner("dtmbn");
			long l4 = System.currentTimeMillis();
			System.out
					.print("Structure learning using Decision Trees ends. Running time of Decision tree leasrning(ms)");
			System.out.println(l4 - l3);
			Writer output = null;
			File file = new File(global.WorkingDirectory + "/" + global.schema
					+ "_DecisionTree.mln");
			try {
				output = new BufferedWriter(new FileWriter(file));
				output.write(rules.toString());
				output.close();
				System.out.println("MLN ready for use "
						+ global.WorkingDirectory + "/" + global.schema
						+ "_DecisionTree.mln");
			} catch (Exception e) {

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void output(String method) {

		Preparing();
		ExportToMLN export = new ExportToMLN();

		StringBuffer rules;
		try {
			rules = export.export(method);

			FileOutputStream outputStream = new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + "_VJ.mln",
					true);
			outputStream.write(rules.toString().getBytes());
			System.out.println("MLN ready for use " + global.WorkingDirectory
					+ "/" + global.schema + "_VJ.mln");
			outputStream.close();
		} catch (Exception e) {

		}
	}

	public void Preparing() {
		global.initialize();

		System.out.println("Starting MLN parameter learning package on "
				+ global.schema);
		ReadXML sqlToXMLReader = new CorrelatedSQLToXML();
		try {
			sqlToXMLReader.initialize();
		} catch (SQLException e1) {
			System.out.println("SQLtoXML initialization problem");
			e1.printStackTrace();
		}
		// xml file is ready

		Parser.initialize();

		ReadSQL_MLN_Files r = new ReadSQL_MLN_Files();
		try {
			r.initialize();
			PrintStream out1 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + ".db"));
			PrintStream out2 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema + "_VJ.mln"));
			PrintStream out3 = new PrintStream(new FileOutputStream(
					global.WorkingDirectory + "/" + global.schema
							+ "predicate_temp.mln"));

			r.read(out1, out2, out3);

			System.out.println("DB file and MLN predicate file created");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructs a new instanted model editor from a Bayes IM wrapper.
	 */

	public JoinBayesNetEditor(DirichletBayesImWrapper dirichletBayesImWrapper) {
		this(dirichletBayesImWrapper.getDirichletBayesIm());
	}

	/**
	 * Constructs a new Bayes IM Editor from a Dirichlet Prior.
	 */
	public JoinBayesNetEditor(JoinBayesNetEstimatorWrapper jbEstWrapper) {

		this(jbEstWrapper.getEstimatedBayesIm());
	}

	/**
	 * Sets the name of this editor.
	 */
	@Override
	public void setName(String name) {
		String oldName = getName();
		super.setName(name);
		this.firePropertyChange("name", oldName, getName());
	}

	public String inputBox(String title) {
		String method = JOptionPane.showInputDialog(this, title);
		return method;

	}

	public void msgbox(String information) {
		JOptionPane.showMessageDialog(this, information);
	}

	/**
	 * Returns a reference to this editor.
	 */

	public BayesImEditorWizard getWizard() {
		return wizard;
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return bayesPm.getDag();
	}

	@Override
	public Knowledge getKnowledge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph getSourceGraph() {
		// TODO Auto-generated method stub
		return workbench.getGraph();
	}

	@Override
	public void layoutByGraph(Graph graph) {
		// TODO Auto-generated method stub
		workbench.layoutByGraph(graph);
	}

	@Override
	public void layoutByKnowledge() {
		// TODO Auto-generated method stub

	}
}