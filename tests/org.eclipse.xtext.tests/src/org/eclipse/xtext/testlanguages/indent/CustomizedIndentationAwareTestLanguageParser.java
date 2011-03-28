/*******************************************************************************
 * Copyright (c) 2010 Ralf Ebert and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.xtext.testlanguages.indent;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.TokenSource;
import org.eclipse.xtext.testlanguages.indent.parser.antlr.IndentationAwareTestLanguageParser;

public class CustomizedIndentationAwareTestLanguageParser extends IndentationAwareTestLanguageParser {

	@Override
	protected TokenSource createLexer(CharStream stream) {
		IndentTokenSource tokenSource = new IndentTokenSource();
		tokenSource.setDelegate(super.createLexer(stream));
		return tokenSource;
	}

}
