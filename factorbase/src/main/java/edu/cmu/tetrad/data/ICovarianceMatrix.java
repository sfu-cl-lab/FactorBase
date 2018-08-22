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

import edu.cmu.tetrad.util.TetradSerializable;
import edu.cmu.tetrad.graph.Node;

import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * Created by IntelliJ IDEA.
 * User: jdramsey
 * Date: Jun 22, 2010
 * Time: 4:07:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ICovarianceMatrix extends DataModel, TetradSerializable {
    @Override
	List<Node> getVariables();

    @Override
	List<String> getVariableNames();

    String getVariableName(int index);

    int getDimension();

    int getSampleSize();

    @Override
	String getName();

    @Override
	void setName(String name);

    @Override
	Knowledge getKnowledge();

    @Override
	void setKnowledge(Knowledge knowledge);

    ICovarianceMatrix getSubmatrix(int[] indices);

    ICovarianceMatrix getSubmatrix(List<String> submatrixVarNames);

    ICovarianceMatrix getSubmatrix(String[] submatrixVarNames);

    double getValue(int i, int j);

    void setMatrix(DoubleMatrix2D matrix);

    void setSampleSize(int sampleSize);

    int getSize();

    DoubleMatrix2D getMatrix();

    void select(Node variable);

    void clearSelection();

    boolean isSelected(Node variable);

    List<String> getSelectedVariableNames();

    @Override
	String toString();

    Node getVariable(String name);
}

