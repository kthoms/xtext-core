/*
 * generated by Xtext
 */
package org.eclipse.xtext.xtext.ui.ecore2xtext.parser.antlr;

import com.google.inject.Inject;

import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.xtext.ui.ecore2xtext.services.Ecore2XtextTestGrammarAccess;

public class Ecore2XtextTestParser extends org.eclipse.xtext.parser.antlr.AbstractAntlrParser {
	
	@Inject
	private Ecore2XtextTestGrammarAccess grammarAccess;
	
	@Override
	protected void setInitialHiddenTokens(XtextTokenStream tokenStream) {
		tokenStream.setInitialHiddenTokens("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT");
	}
	
	@Override
	protected org.eclipse.xtext.xtext.ui.ecore2xtext.parser.antlr.internal.InternalEcore2XtextTestParser createParser(XtextTokenStream stream) {
		return new org.eclipse.xtext.xtext.ui.ecore2xtext.parser.antlr.internal.InternalEcore2XtextTestParser(stream, getGrammarAccess());
	}
	
	@Override 
	protected String getDefaultRuleName() {
		return "Root";
	}
	
	public Ecore2XtextTestGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(Ecore2XtextTestGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
	
}