/*
 * Copyright (c) 2013 Villu Ruusmann
 *
 * This file is part of JPMML-Evaluator
 *
 * JPMML-Evaluator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Evaluator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Evaluator.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.evaluator;

import java.util.Map;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;

/**
 * <p>
 * Performs the evaluation of a {@link Model} in "interpreted mode".
 * </p>
 *
 * <h3>Obtaining and verifying an Evaluator instance</h3>
 * <pre>
 * PMML pmml = ...;
 * ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
 * Evaluator evaluator = (Evaluator)modelEvaluatorFactory.newModelManager(pmml);
 * evaluator.verify();
 * </pre>
 *
 * <h3>Preparing arguments</h3>
 * Converting an user-supplied map of arguments to a prepared map of arguments:
 * <pre>
 * Map&lt;FieldName, ?&gt; userArguments = ...;
 * Map&lt;FieldName, FieldValue&gt; arguments = new LinkedHashMap&lt;FieldName, FieldValue&gt;();
 * List&lt;FieldName&gt; activeFields = evaluator.getActiveFields();
 * for(FieldName activeField : activeFields){
 *   FieldValue activeValue = evaluator.prepare(activeField, userArguments.get(activeField));
 *   arguments.put(activeField, activeValue);
 * }
 * </pre>
 *
 * <h3>Performing the evaluation</h3>
 * <pre>
 * Map&lt;FieldName, ?&gt; result = evaluator.evaluate(arguments);
 * </pre>
 *
 * <h3>Processing results</h3>
 * Retrieving the value of the {@link #getTargetField() target field} (ie. the primary result):
 * <pre>
 * FieldName targetField = evaluator.getTargetField();
 * Object targetValue = result.get(targetField);
 * </pre>
 *
 * Decoding a {@link Computable complex value} to a Java primitive value:
 * <pre>
 * if(targetValue instanceof Computable){
 *   Computable computable = (Computable)targetValue;
 *
 *   targetValue = computable.getResult();
 * }
 * </pre>
 *
 * Retrieving the values of {@link #getOutputFields() output fields} (ie. secondary results):
 * <pre>
 * List&lt;FieldName&gt; outputFields = evaluator.getOutputFields();
 * for(FieldName outputField : outputFields){
 *   Object outputValue = result.get(outputField);
 * }
 * </pre>
 *
 * @see EvaluatorUtil
 */
public interface Evaluator extends Consumer {

	/**
	 * Prepares the input value for a field.
	 *
	 * First, the value is converted from the user-supplied representation to internal representation.
	 * Later on, the value is subjected to missing value treatment, invalid value treatment and outlier treatment.
	 *
	 * @param name The name of the field
	 * @param string The input value in user-supplied representation. Use <code>null</code> to represent a missing input value.
	 *
	 * @throws PMMLException If the input value preparation fails.
	 *
	 * @see #getDataField(FieldName)
	 * @see #getMiningField(FieldName)
	 */
	FieldValue prepare(FieldName name, Object value);

	/**
	 * Verifies the model.
	 *
	 * @throws PMMLException If the verification fails.
	 */
	void verify();

	/**
	 * Evaluates the model with the specified arguments.
	 *
	 * @param arguments Map of {@link #getActiveFields() active field} values.
	 *
	 * @return Map of {@link #getTargetFields() target field} and {@link #getOutputFields() output field} values.
	 * Simple values are represented using the Java equivalents of PMML data types (eg. String, Integer, Float, Double etc.).
	 * Complex values are represented as instances of {@link Computable} that return simple values.
	 * A missing result is represented by <code>null</code>.
	 *
	 * @throws PMMLException If the evaluation fails.
	 * This is either {@link InvalidFeatureException} or {@link UnsupportedFeatureException} if there is a persistent structural problem with the PMML class model.
	 * This is {@link EvaluationException} (or one of its subclasses) if there is a problem with the evaluation request (eg. badly prepared arguments).
	 *
	 * @see Computable
	 */
	Map<FieldName, ?> evaluate(Map<FieldName, ?> arguments);
}