/**
 * Copyright (C) 2012-2015, Markus Sprunck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package com.sw_engineering_candies.example;

import java.util.HashMap;
import java.util.Map;
import static java.lang.System.arraycopy;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.sum.Scalar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

/**
 * Sample application for solving Alphametric Equations with the CHOCO library.
 * CHOCO is a java library for constraint satisfaction problems (CSP) and
 * constraint programming (CP).
 * 
 * @see <a href="http://choco.sourceforge.net">http://choco.sourceforge.net</a>
 * 
 */
public class AlphametricSample {
	/**
	 * Field inputTerm1 is the first term.
	 */
	private final String inputTerm1;
	/**
	 * Field inputTerm2 is the second term.
	 */
	private final String inputTerm2;
	/**
	 * Field inputResult is the expected result.
	 */
	private final String inputResult;
	/**
	 * Field allUniqueCharacters contains all used characters.
	 */
	private final String allUniqueCharacters;
	/**
	 * Field intVarMap holds all needed variables.
	 */
	private final Map<String, IntVar> intVarMap = new HashMap<String, IntVar>();
	/**
	 * Field model holds structures for a model of constraint programming
	 */
	// private final CPModel model = new CPModel();
	/**
	 * Field solver for the model.
	 */
	private final Solver solver = new Solver();

	/**
	 * Method removeDuplicateChar creates a string with a containing characters
	 */
	private static String removeDuplicateChar(String s) {
		StringBuffer result = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); i++) {
			String next = s.substring(i, i + 1);
			if (-1 == result.indexOf(next)) {
				result.append(next);
			}
		}
		return result.toString();
	}

	/**
	 * Method prepareIntegerVariables creates all needed variables
	 */
	private void prepareIntegerVariables() {
		for (int i = 0; i < allUniqueCharacters.length(); i++) {
			String variable = allUniqueCharacters.substring(i, i + 1);
			intVarMap.put(variable, VariableFactory.integer(variable, 0, 9, solver));
		}
	}

	/**
	 * Method prepareModel defines the constraints
	 */
	private void prepareModel() {
		// 1st Constraint - the first digits are not allowed to be zero)
		Constraint a = IntConstraintFactory.arithm(intVarMap.get(inputTerm1.substring(0, 1)), ">", 0);
		Constraint b = IntConstraintFactory.arithm(intVarMap.get(inputTerm2.substring(0, 1)), ">", 0);
		Constraint c = LogicalConstraintFactory.and(a, b);
		solver.post(c);

		// 2nd Constraint - all characters have to be different)
		IntVar[] intVarArray = new IntVar[allUniqueCharacters.length()];
		for (int i = 0; i < allUniqueCharacters.length(); i++) {
			intVarArray[i] = intVarMap.get(allUniqueCharacters.substring(i, i + 1));
		}
		solver.post(ICF.alldifferent(intVarArray));

		// 3rd Constraint - the "term1 + term2 = result" equation)
		IntVar OBJ1 = VF.bounded("objective1", 0, 99999, solver);
		IntVar OBJ2 = VF.bounded("objective2", 0, 99999, solver);
		IntVar OBJ3 = VF.bounded("objective3", 0, 99999, solver);
		solver.post(ICF.scalar(getIntVar(inputTerm1), getFactors(inputTerm1), OBJ1));
		solver.post(ICF.scalar(getIntVar(inputTerm2), getFactors(inputTerm2), OBJ2));
		solver.post(ICF.scalar(getIntVar(inputResult), getFactors(inputResult), OBJ3));
		solver.post(ICF.sum(new IntVar[]{OBJ1, OBJ2}, OBJ3));

	}

	/**
	 * Method createExpression creates the variables from the input strings
	 */
	private int[] getFactors(String valueString) {
		// Creates the scalar product of the variables
		int[] lc = new int[valueString.length()];
		for (int i = 0; i < valueString.length(); i++) {
			lc[i] = (int) Math.pow(10, valueString.length() - i - 1);
		}
		return lc;
	}

	private IntVar[] getIntVar(String valueString) {
		// Creates the scalar product of the variables
		IntVar[] first = new IntVar[valueString.length()];
		for (int i = 0; i < valueString.length(); i++) {
			first[i] = intVarMap.get(valueString.substring(i, i + 1));
		}
		return first;
	}

	/**
	 * Method printResult needed just for output of input and results
	 */
	private void printResult() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("   TASK     : ");
		buffer.append(inputTerm1);
		buffer.append(" + ");
		buffer.append(inputTerm2);
		buffer.append(" = ");
		buffer.append(inputResult);
		buffer.append('\n');
		buffer.append("   SOLUTION : ");
		buffer.append(solver.toString());
		buffer.append('\n');
		buffer.append("   RESULT   : ");
		appendNumber(buffer, inputTerm1);
		buffer.append(" + ");
		appendNumber(buffer, inputTerm2);
		buffer.append(" = ");
		appendNumber(buffer, inputResult.toString());
		System.out.println(buffer);
	}

	/**
	 * Method appendNumber is a helper for output method printResult()
	 */
	private void appendNumber(StringBuffer buffer, String text) {
		for (int i = 0; i < text.length(); i++) {
			String variable = text.substring(i, i + 1);
			buffer.append(String.format("%d", intVarMap.get(variable).getValue()));
		}
	}

	/**
	 * Constructor for AlphametricSample the strings should not be empty
	 */
	public AlphametricSample(String term1, String term2, String result) {
		this.inputTerm1 = term1.toUpperCase();
		this.inputTerm2 = term2.toUpperCase();
		this.inputResult = result;
		this.allUniqueCharacters = removeDuplicateChar(term1 + term2 + result);
	}

	/**
	 * Method run does all the work
	 */
	public void run() {
		prepareIntegerVariables();
		prepareModel();

		// 5. Launch the resolution process
		solver.findSolution();
		// 6. Print search statistics
		printResult();
	}

	/**
	 * Method main contains two test cases
	 */
	public static void main(String[] args) {
		System.out.println("First sample:");
		new AlphametricSample("CRACK", "HACK", "ERROR").run();
		System.out.println("\nSecond sample:");
		new AlphametricSample("SEND", "MORE", "MONEY").run();
	}
}
