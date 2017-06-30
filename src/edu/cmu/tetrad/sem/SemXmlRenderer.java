package edu.cmu.tetrad.sem;

import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.SemGraph;
import nu.xom.Attribute;
import nu.xom.Element;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: May 30, 2004
 * Time: 11:36:30 AM
 *
 * This class converts a SemIm into xml.
 *
 */
public class SemXmlRenderer {

    /**
     * Converts a Sem Im into xml.
     * @param semIm the instantiated structural equation model to convert
     * @return xml representation
     */
    public static Element getElement(SemIm semIm) {
        Element semElement = new Element(SemXmlConstants.SEM);
        semElement.appendChild(makeVariables(semIm));
        semElement.appendChild(makeEdges(semIm));
        semElement.appendChild(makeMarginalErrorDistribution(semIm));
        semElement.appendChild(makeJointErrorDistribution(semIm));
        return semElement;
    }


    private static Element makeVariables(SemIm semIm){
        Element variablesElement = new Element(SemXmlConstants.SEM_VARIABLES);
        Element variable;
        Node measuredNode, latentNode;
        for(Iterator measuredNodes = semIm.getSemPm().getMeasuredNodes().iterator(); measuredNodes.hasNext(); ){
            measuredNode = (Node) measuredNodes.next();
            variable = new Element(SemXmlConstants.CONTINUOUS_VARIABLE);
            variable.addAttribute(new Attribute(SemXmlConstants.NAME, measuredNode.getName()));
            variable.addAttribute(new Attribute(SemXmlConstants.IS_LATENT, "no"));
            variable.addAttribute(new Attribute(SemXmlConstants.MEAN, new Double(semIm.getMean(measuredNode)).toString()));
            variable.addAttribute(new Attribute(SemXmlConstants.X, new Integer(measuredNode.getCenterX()).toString()));
            variable.addAttribute(new Attribute(SemXmlConstants.Y, new Integer(measuredNode.getCenterY()).toString()));
            variablesElement.appendChild(variable);
        }
        for(Iterator latentNodes = semIm.getSemPm().getLatentNodes().iterator(); latentNodes.hasNext();){
            latentNode = (Node) latentNodes.next();
            variable = new Element(SemXmlConstants.CONTINUOUS_VARIABLE);
            variable.addAttribute(new Attribute(SemXmlConstants.NAME, latentNode.getName()));
            variable.addAttribute(new Attribute(SemXmlConstants.IS_LATENT, "yes"));
            variable.addAttribute(new Attribute(SemXmlConstants.MEAN, new Double(semIm.getMean(latentNode)).toString()));
            variable.addAttribute(new Attribute(SemXmlConstants.X, new Integer(latentNode.getCenterX()).toString()));
            variable.addAttribute(new Attribute(SemXmlConstants.Y, new Integer(latentNode.getCenterY()).toString()));
            variablesElement.appendChild(variable);
        }
        return variablesElement;
    }

    private static Element makeEdges(SemIm semIm){
        Element edgesElement = new Element(SemXmlConstants.EDGES);
        Parameter param;
        Element edge;

        for(Iterator parameters = semIm.getSemPm().getParameters().iterator(); parameters.hasNext(); ){
            param = (Parameter) parameters.next();
            if(param.getType() == ParamType.COEF){
                edge = new Element(SemXmlConstants.EDGE);
                edge.addAttribute(new Attribute(SemXmlConstants.CAUSE_NODE, param.getNodeA().getName()));
                edge.addAttribute(new Attribute(SemXmlConstants.EFFECT_NODE, param.getNodeB().getName()));
                edge.addAttribute(new Attribute(SemXmlConstants.VALUE, new Double(semIm.getParamValue(param)).toString()));
                edge.addAttribute(new Attribute(SemXmlConstants.FIXED, new Boolean(param.isFixed()).toString()));
                edgesElement.appendChild(edge);
            }
        }
        return edgesElement;
    }


    private static Element makeMarginalErrorDistribution(SemIm semIm){
        Element marginalErrorElement = new Element(SemXmlConstants.MARGINAL_ERROR_DISTRIBUTION);
        Element normal;

        SemGraph semGraph = semIm.getSemPm().getGraph();
        semGraph.setShowErrorTerms(true);

        for (Node node : getExogenousNodes(semGraph)) {
            normal = new Element(SemXmlConstants.NORMAL);
            normal.addAttribute(new Attribute(SemXmlConstants.VARIABLE, node.getName()));
            normal.addAttribute(new Attribute(SemXmlConstants.MEAN, "0.0"));
            normal.addAttribute(new Attribute(SemXmlConstants.VARIANCE, new Double(semIm.getParamValue(node,node)).toString()));
            marginalErrorElement.appendChild(normal);
        }
        return marginalErrorElement;
    }

    private static List<Node> getExogenousNodes(SemGraph graph) {
        Set<Node> a = new TreeSet<Node>();
        List<Node> exogenousNodes = new ArrayList<Node>();

        for (Node node : graph.getNodes()) {
            exogenousNodes.add(graph.getExogenous(node));
        }

        return exogenousNodes;
    }


    private static Element makeJointErrorDistribution(SemIm semIm){
        Element jointErrorElement = new Element(SemXmlConstants.JOINT_ERROR_DISTRIBUTION);
        Element normal;
        Parameter param;

        for(Iterator parameters = semIm.getSemPm().getParameters().iterator(); parameters.hasNext(); ){
            param = (Parameter) parameters.next();
            if(param.getType() == ParamType.COVAR){
                normal = new Element(SemXmlConstants.NORMAL);
                normal.addAttribute(new Attribute(SemXmlConstants.NODE_1, param.getNodeA().getName()));
                normal.addAttribute(new Attribute(SemXmlConstants.NODE_2, param.getNodeB().getName()));
                normal.addAttribute(new Attribute(SemXmlConstants.COVARIANCE, new Double(param.getStartingValue()).toString()));
                jointErrorElement.appendChild(normal);
            }
        }

        return jointErrorElement;
    }

}
