package ca.sfu.jbn.parameterLearning;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class WekaTest {

	public static Instances csv2arff(String csvStr) {
		CSVLoader loader = new CSVLoader();
		Instances data = null;
		ByteArrayInputStream csv = new ByteArrayInputStream(csvStr.getBytes());

		try {
			loader.setSource(csv);
			loader.setNominalAttributes("first-last"); // force nominal
			//System.out.println("h1!");
			data = loader.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("h2!");   
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		return saver.getInstances();
	}

	public static ClassifierTree getClassifierTree(J48 j48) throws Exception {
		Field field = J48.class.getDeclaredField("m_root");
		field.setAccessible(true);
		ClassifierTree mRootTree = (ClassifierTree) field.get(j48);
		return mRootTree;
	}
	
	public static void main(String args[]) throws Exception
	{
		System.out.println("Weka test");
		String csv = "level,diff\n";

		for (int i=0; i<3; i++)
			csv += "1.0,1.0\n";
		for (int i=0; i<3; i++)
			csv += "2.0,2.0\n";
		for (int i=0; i<2; i++)
			csv += "2.0,1.0\n";
		for (int i=0; i<2; i++)
			csv += "1.0,2.0\n";
		
		Instances train = csv2arff(csv);
//		System.out.println(train.attribute(0) + " " + train.attribute(1));
//		System.out.println(train.numInstances());
		train.setClass(train.attribute("diff"));
		
		J48 j48 = new J48();
		j48.setUnpruned(true);
		j48.setUseLaplace(true);
		j48.buildClassifier(train);

//		System.out.println(getClassifierTree(j48));
		ClassifierTree tree = getClassifierTree(j48);
		System.out.println(tree.numNodes());
		System.out.println(tree);
	}

}
