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
import java.util.ArrayList;

/**
 * A completely blank knowledge with all default returns for methods. The idea of this
 * is to override only the methods you need with specific operations to allow use of
 * knowledge to be fast.
 *
 * @author Joseph Ramsey
 */
public class BlankKnowledge implements IKnowledge {
    @Override
	public void addToTier(int tier, String var) {
        // no op.
    }

    @Override
	public void addToTiersByVarNames(List<String> varNames) {
        // no op./
    }

    @Override
	public List<KnowledgeGroup> getKnowledgeGroups() {
        return new ArrayList<KnowledgeGroup>();
    }

    @Override
	public void removeKnowledgeGroup(int index) {
        // no op
    }

    @Override
	public void addKnowledgeGroup(KnowledgeGroup group) {
        // no op
    }

    @Override
	public void setKnowledgeGroup(int index, KnowledgeGroup group) {
        // no op.
    }

    @Override
	public Iterator<KnowledgeEdge> forbiddenCommonCausesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    @Override
	public Iterator<KnowledgeEdge> forbiddenEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    @Override
	public Iterator<KnowledgeEdge> explicitlyForbiddenEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    @Override
	public List<String> getVarsNotInTier() {
        return new ArrayList<String>();
    }

    @Override
	public List<String> getTier(int tier) {
        return new ArrayList<String>();
    }

    @Override
	public int getNumTiers() {
        return 0;
    }

    @Override
	public boolean commonCauseForbidden(String var1, String var2) {
        return false;
    }

    @Override
	public boolean edgeExplicitlyRequired(String var1, String var2) {
        return false;
    }

    @Override
	public boolean edgeExplicitlyRequired(KnowledgeEdge edge) {
        return false;
    }

    @Override
	public boolean edgeForbidden(String var1, String var2) {
        return false;
    }

    @Override
	public boolean edgeRequired(String var1, String var2) {
        return false;
    }

    @Override
	public boolean edgeRequiredByGroups(String var1, String var2) {
        return false;
    }

    @Override
	public boolean edgeForbiddenByGroups(String var1, String var2) {
        return false;
    }

    @Override
	public boolean noEdgeRequired(String x, String y) {
        return true;
    }

    @Override
	public boolean isForbiddenByTiers(String var1, String var2) {
        return false;
    }

    @Override
	public boolean isEmpty() {
        return true;
    }

    @Override
	public Iterator<KnowledgeEdge> requiredCommonCausesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    @Override
	public Iterator<KnowledgeEdge> requiredEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    @Override
	public Iterator<KnowledgeEdge> explicitlyRequiredEdgesIterator() {
        return new ArrayList<KnowledgeEdge>().iterator();
    }

    @Override
	public void setEdgeForbidden(String var1, String var2, boolean forbid) {
        // no op
    }

    @Override
	public void setEdgeRequired(String var1, String var2, boolean required) {
        // no op
    }

    @Override
	public void removeFromTiers(String var) {
        // no op
    }

    @Override
	public void setTierForbiddenWithin(int tier, boolean forbidden) {
        // no op
    }

    @Override
	public boolean isTierForbiddenWithin(int tier) {
        return false;
    }

    @Override
	public int getMaxTierForbiddenWithin() {
        return 0;
    }

    @Override
	public void setDefaultToKnowledgeLayout(boolean defaultToKnowledgeLayout) {
        // no op
    }

    @Override
	public boolean isDefaultToKnowledgeLayout() {
        return false;
    }

    @Override
	public void clear() {
        // no op.
    }

    @Override
	public boolean isViolatedBy(Graph graph) {
        return false;
    }

    @Override
	public void setTier(int tier, List<String> vars) {
        // no op
    }

    @Override
	public void addVariable(String varName) {
        // no op
    }

    @Override
	public void removeVariable(String varName) {
        // no op
    }

    @Override
	public List<String> getVariables() {
        return new ArrayList<String>();
    }
}

