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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Sample application for solving Alphametic Equations with the CHOCO3 library.
 * CHOCO is a java library for constraint satisfaction problems and constraint
 * programming.
 * 
 * @see <a href="https://github.com/chocoteam/choco3">https://github.com/
 *      chocoteam/choco3</a>
 * 
 */
public class AlphameticSample {

	/**
	 * Field inputFirst is the first term.
	 */
	private final String inputFirst;

	/**
	 * Field inputSecond is the second term.
	 */
	private final String inputSecond;

	/**
	 * Field inputResult is the expected result.
	 */
	private final String inputResult;

	/**
	 * Field usedCharacters contains all used characters without duplicates.
	 */
	private final String usedCharacters;

	/**
	 * Field intVarMap holds all needed variables.
	 */
	private final Map<String, IntVar> intVarMap = new HashMap<String, IntVar>();

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
		for (int i = 0; i < usedCharacters.length(); i++) {
			String variable = usedCharacters.substring(i, i + 1);
			intVarMap.put(variable, VariableFactory.integer(variable, 0, 9, solver));
		}
	}

	/**
	 * Method prepareModel defines the constraints
	 */
	private void prepareModel() {

		// 1st Constraint - the first digits are not allowed to be zero
		Constraint a = IntConstraintFactory.arithm(intVarMap.get(inputFirst.substring(0, 1)), ">", 0);
		Constraint b = IntConstraintFactory.arithm(intVarMap.get(inputSecond.substring(0, 1)), ">", 0);
		Constraint c = LogicalConstraintFactory.and(a, b);
		solver.post(c);

		// 2nd Constraint - all characters have to be different)
		IntVar[] intVarArray = new IntVar[usedCharacters.length()];
		for (int i = 0; i < usedCharacters.length(); i++) {
			intVarArray[i] = intVarMap.get(usedCharacters.substring(i, i + 1));
		}
		solver.post(ICF.alldifferent(intVarArray));

		// 3rd Constraint - the "First + Second = Result" equation
		IntVar OBJ1 = getConstraint("First", inputFirst);
		IntVar OBJ2 = getConstraint("Second", inputSecond);
		IntVar OBJ3 = getConstraint("Result", inputResult);
		solver.post(ICF.sum(new IntVar[] { OBJ1, OBJ2 }, OBJ3));
	}

	private IntVar getConstraint(String name, String term) {
		IntVar OBJ1 = VF.bounded(name, 0, 99999999, solver);
		solver.post(ICF.scalar(getIntVar(term), getFactors(term), OBJ1));
		return OBJ1;
	}

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
		buffer.append("\tTASK     : ").append(inputFirst).append(" + ");
		buffer.append(inputSecond).append(" = ").append(inputResult).append('\n');
		Solution lastSolution = solver.getSolutionRecorder().getLastSolution();
		buffer.append("\tSOLUTION : ").append((lastSolution != null) ? lastSolution.hasBeenFound() : "false");
		buffer.append('\n');

		buffer.append("\tRESULT   : ");
		appendNumber(buffer, inputFirst);
		buffer.append(" + ");
		appendNumber(buffer, inputSecond);
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
	public AlphameticSample(String first, String second, String result) {
		this.inputFirst = first.toUpperCase();
		this.inputSecond = second.toUpperCase();
		this.inputResult = result.toUpperCase();
		this.usedCharacters = removeDuplicateChar(first + second + result);
	}

	/**
	 * Method run does all the work
	 */
	public void run() {
		prepareIntegerVariables();
		prepareModel();
		solver.findSolution();
		printResult();
	}

	/**
	 * Method main contains some test cases
	 */
	public static void main(String[] args) {

		System.out.println("\npositive test case:");
		new AlphameticSample("CRACK", "HACK", "ERROR").run();

		System.out.println("\npositive test case:");
		new AlphameticSample("SEND", "MORE", "MONEY").run();

		System.out.println("\npositive test case:");
		new AlphameticSample("AGONY", "JOY", "GUILT").run();

		System.out.println("\npositive test case:");
		new AlphameticSample("APPLE", "LEMON", "BANANA").run();

		System.out.println("\nnegative test case:");
		new AlphameticSample("APPLE", "LEMON", "BANANAX").run();

		System.out.println("\npositive test case:");
		new AlphameticSample("SYSTEMA", "ATIMA", "SCURITY").run();
	}
}
