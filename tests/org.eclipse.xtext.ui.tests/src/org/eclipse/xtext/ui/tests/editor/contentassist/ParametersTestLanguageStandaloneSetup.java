/*
 * generated by Xtext
 */
package org.eclipse.xtext.ui.tests.editor.contentassist;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class ParametersTestLanguageStandaloneSetup extends ParametersTestLanguageStandaloneSetupGenerated{

	public static void doSetup() {
		new ParametersTestLanguageStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
