/*
 Copyright (c) 2006 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/dml/functions/First.java $
 $Id: First.java 3617 2008-08-21 10:02:57Z loomis $
 */

package org.quattor.pan.dml.functions;

import static org.quattor.pan.utils.MessageUtils.MSG_3_ARGS_REQ;
import static org.quattor.pan.utils.MessageUtils.MSG_VARIABLE_REF_OR_UNDEF;

import java.util.NoSuchElementException;

import org.quattor.pan.dml.Operation;
import org.quattor.pan.dml.data.BooleanProperty;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.Resource;
import org.quattor.pan.dml.data.Undef;
import org.quattor.pan.dml.operators.SetValue;
import org.quattor.pan.dml.operators.Variable;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.template.Context;
import org.quattor.pan.template.SourceRange;

/**
 * Initializes an iterator for the named resource and sets the variable
 * arguments to the values of the first entry.
 * 
 * @author loomis
 * 
 */
final public class First extends BuiltInFunction {

	private First(SourceRange sourceRange, Operation... operations)
			throws SyntaxException {
		super("first", sourceRange, operations);

	}

	public static Operation getInstance(SourceRange sourceRange,
			Operation... operations) throws SyntaxException {

		// Do some error checking before starting. First the number of arguments
		// must be exactly three.
		if (operations.length != 3) {
			throw SyntaxException.create(sourceRange, MSG_3_ARGS_REQ, "first");
		}

		// Check that the second and third arguments are variable references or
		// undef. Convert the variable reference to a SetValue operation.
		if (operations[1] instanceof Variable) {
			operations[1] = SetValue.getInstance((Variable) operations[1]);
		} else if (!(operations[1] instanceof Undef)) {
			throw SyntaxException.create(sourceRange,
					MSG_VARIABLE_REF_OR_UNDEF, "first");
		}
		if (operations[2] instanceof Variable) {
			operations[2] = SetValue.getInstance((Variable) operations[2]);
		} else if (!(operations[2] instanceof Undef)) {
			throw SyntaxException.create(sourceRange,
					MSG_VARIABLE_REF_OR_UNDEF, "first");
		}

		return new First(sourceRange, operations);
	}

	@Override
	public Element execute(Context context) {

		Resource resource = null;
		try {
			resource = (Resource) ops[0].execute(context);
		} catch (ClassCastException cce) {
			throw new EvaluationException(
					"first argument to first() must be a resource",
					getSourceRange(), context);
		}

		// Set the iteration variables to undef before starting. This is done to
		// ensure that the iteration variables are in a known state even if the
		// resource has no entries.
		if (ops[1] instanceof SetValue) {
			((SetValue) ops[1]).execute(context, Undef.VALUE);
		}
		if (ops[2] instanceof SetValue) {
			((SetValue) ops[2]).execute(context, Undef.VALUE);
		}

		// Create an iterator on the resource and register it.
		Resource.Iterator iterator = resource.iterator();
		context.setIterator(resource, iterator);

		if (iterator.hasNext()) {

			try {
				Resource.Entry entry = iterator.next();

				// Set the values.
				if (ops[1] instanceof SetValue) {
					((SetValue) ops[1]).execute(context, entry.getKey());
				}
				if (ops[2] instanceof SetValue) {
					((SetValue) ops[2]).execute(context, entry.getValue());
				}

			} catch (NoSuchElementException nsee) {
				throw new EvaluationException("illegal iteration",
						getSourceRange(), context);
			}

			return BooleanProperty.TRUE;

		} else {
			return BooleanProperty.FALSE;
		}
	}

}
