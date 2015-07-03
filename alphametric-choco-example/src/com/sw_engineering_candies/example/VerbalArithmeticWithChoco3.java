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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Sample application for solving Verbal Alphametic Equations with the CHOCO3
 * library. CHOCO is a java library for constraint satisfaction problems and
 * constraint programming.
 *
 * @see <a href="https://github.com/chocoteam/choco3">https://github.com/
 *      chocoteam/choco3</a>
 *
 */
public class VerbalArithmeticWithChoco3 {

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
	 * Constructor for VerbalArithmeticWithChoco3 the strings should not be
	 * empty and if possible upper case.
	 */
	public VerbalArithmeticWithChoco3(final String first, final String second, final String result) {
		// Preconditions
		checkNotNull(first);
		checkArgument(!first.isEmpty());
		checkNotNull(second);
		checkArgument(!second.isEmpty());
		checkNotNull(result);
		checkArgument(!result.isEmpty());

		// Calculations just with upper case
		this.inputFirst = first.toUpperCase();
		this.inputSecond = second.toUpperCase();
		this.inputResult = result.toUpperCase();
		this.usedCharacters = removeDuplicateChar(first.concat(second).concat(result));

		// Solve with Choco3
		defineIntegerVariablesCharacters();
		defineConstraints();
		this.solver.findSolution();

		// Output
		printResults();
	}

	/**
	 * Method removeDuplicateChar creates a string with all containing
	 * characters.
	 */
	private static String removeDuplicateChar(final String input) {
		final StringBuffer result = new StringBuffer(input.length());
		for (int i = 0; i < input.length(); i++) {
			final String next = input.substring(i, i + 1);
			if (-1 == result.indexOf(next)) {
				result.append(next);
			}
		}
		return result.toString();
	}

	/**
	 * Method defineIntegerVariablesCharacters creates all needed variables.
	 */
	private void defineIntegerVariablesCharacters() {
		for (int i = 0; i < this.usedCharacters.length(); i++) {
			final String character = this.usedCharacters.substring(i, i + 1);
			this.intVarMap.put(character, VariableFactory.integer(character, 0, 9, this.solver));
		}
	}

	/**
	 * Method defineConstraints creates all constraints
	 */
	private void defineConstraints() {

		// 1st Constraint - the first digits are not allowed to be zero
		final Constraint a = IntConstraintFactory.arithm(this.intVarMap.get(this.inputFirst.substring(0, 1)), ">", 0);
		final Constraint b = IntConstraintFactory.arithm(this.intVarMap.get(this.inputSecond.substring(0, 1)), ">", 0);
		final Constraint c = LogicalConstraintFactory.and(a, b);
		this.solver.post(c);

		// 2nd Constraint - all characters have to be different
		final IntVar[] allVariables = new IntVar[this.usedCharacters.length()];
		for (int i = 0; i < this.usedCharacters.length(); i++) {
			allVariables[i] = this.intVarMap.get(this.usedCharacters.substring(i, i + 1));
		}
		this.solver.post(IntConstraintFactory.alldifferent(allVariables));

		// 3rd Constraint - the "First + Second = Result" equation
		final IntVar first = setConstraint("First", this.inputFirst);
		final IntVar second = setConstraint("Second", this.inputSecond);
		final IntVar result = setConstraint("Result", this.inputResult);
		IntVar[] summands = new IntVar[] { first, second };
		this.solver.post(IntConstraintFactory.sum(summands, result));
	}

	private IntVar setConstraint(final String name, final String term) {
		final IntVar result = VariableFactory.bounded(name, 0, VariableFactory.MAX_INT_BOUND, this.solver);
		this.solver.post(IntConstraintFactory.scalar(getIntVar(term), getFactors(term), result));
		return result;
	}

	private int[] getFactors(final String valueString) {
		final int[] coefficients = new int[valueString.length()];
		for (int i = 0; i < valueString.length(); i++) {
			coefficients[i] = (int) Math.pow(10, valueString.length() - i - 1);
		}
		return coefficients;
	}

	private IntVar[] getIntVar(final String valueString) {
		final IntVar[] values = new IntVar[valueString.length()];
		for (int i = 0; i < valueString.length(); i++) {
			values[i] = this.intVarMap.get(valueString.substring(i, i + 1));
		}
		return values;
	}

	private void printResults() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("\tTASK     : ");
		buffer.append(this.inputFirst);
		buffer.append(" + ");
		buffer.append(this.inputSecond);
		buffer.append(" = ");
		buffer.append(this.inputResult);
		buffer.append('\n');

		buffer.append("\tSOLUTION : ");
		final Solution solution = this.solver.getSolutionRecorder().getLastSolution();
		buffer.append((solution != null) ? solution.hasBeenFound() : "false");
		buffer.append('\n');

		buffer.append("\tRESULT   : ");
		buffer.append(getIntegerNumber(this.inputFirst));
		buffer.append(" + ");
		buffer.append(getIntegerNumber(this.inputSecond));
		buffer.append(" = ");
		buffer.append(getIntegerNumber(this.inputResult));

		System.out.println(buffer);
	}

	/**
	 * Method getIntegerNumber is a helper for output method of numbers
	 */
	private String getIntegerNumber(final String text) {
		final StringBuilder result = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			final String variable = text.substring(i, i + 1);
			result.append(String.format("%d", this.intVarMap.get(variable).getValue()));
		}
		return result.toString();
	}

	/**
	 * Method main contains some test cases
	 */
	public static void main(final String[] args) {

		System.out.println("positive test case:");
		new VerbalArithmeticWithChoco3("CRACK", "HACK", "ERROR");

		System.out.println("\npositive test case:");
		new VerbalArithmeticWithChoco3("SEND", "MORE", "MONEY");

		System.out.println("\npositive test case:");
		new VerbalArithmeticWithChoco3("AGONY", "JOY", "GUILT");

		System.out.println("\npositive test case:");
		new VerbalArithmeticWithChoco3("APPLE", "LEMON", "BANANA");

		System.out.println("\npositive test case:");
		new VerbalArithmeticWithChoco3("SYSTEMA", "ATIMA", "SECURER");
	}
}
