///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
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

package edu.cmu.tetrad.data;

import edu.cmu.tetrad.graph.Graph;

import java.util.List;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: josephramsey
 * Date: Jun 29, 2010
 * Time: 5:36:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IKnowledge {
    void addToTier(int tier, String var);

    void addToTiersByVarNames(List<String> varNames);

    List<KnowledgeGroup> getKnowledgeGroups();

    void removeKnowledgeGroup(int index);

    void addKnowledgeGroup(KnowledgeGroup group);

    void setKnowledgeGroup(int index, KnowledgeGroup group);

    Iterator<KnowledgeEdge> forbiddenCommonCausesIterator();

    Iterator<KnowledgeEdge> forbiddenEdgesIterator();

    Iterator<KnowledgeEdge> explicitlyForbiddenEdgesIterator();

    List<String> getVarsNotInTier();

    List<String> getTier(int tier);

    int getNumTiers();

    boolean commonCauseForbidden(String var1, String var2);

    boolean edgeExplicitlyRequired(String var1, String var2);

    boolean edgeExplicitlyRequired(KnowledgeEdge edge);

    boolean edgeForbidden(String var1, String var2);

    boolean edgeRequired(String var1, String var2);

    boolean edgeRequiredByGroups(String var1, String var2);

    boolean edgeForbiddenByGroups(String var1, String var2);

    boolean noEdgeRequired(String x, String y);

    @SuppressWarnings({"SimplifiableIfStatement"})
    boolean isForbiddenByTiers(String var1, String var2);

    boolean isEmpty();

    Iterator<KnowledgeEdge> requiredCommonCausesIterator();

    Iterator<KnowledgeEdge> requiredEdgesIterator();

    Iterator<KnowledgeEdge> explicitlyRequiredEdgesIterator();

    void setEdgeForbidden(String var1, String var2,
                                       boolean forbid);

    void setEdgeRequired(String var1, String var2,
                                                                         boolean required);

    void removeFromTiers(String var);

    void setTierForbiddenWithin(int tier, boolean forbidden);

    boolean isTierForbiddenWithin(int tier);

    int getMaxTierForbiddenWithin();

    void setDefaultToKnowledgeLayout(
            boolean defaultToKnowledgeLayout);

    boolean isDefaultToKnowledgeLayout();

    void clear();

    @Override
	int hashCode();

    @Override
	@SuppressWarnings({"SimplifiableIfStatement"})
    boolean equals(Object o);

    @Override
	String toString();

    boolean isViolatedBy(Graph graph);

    void setTier(int tier, List<String> vars);

    void addVariable(String varName);

    void removeVariable(String varName);

    List<String> getVariables();
}

