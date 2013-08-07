/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ui.refactoring.impl;

import static com.google.common.collect.Maps.*;
import static org.eclipse.ltk.core.refactoring.RefactoringStatus.*;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.ui.editor.findrefs.IReferenceFinder;
import org.eclipse.xtext.ui.editor.findrefs.ResourceAccess;
import org.eclipse.xtext.ui.editor.findrefs.SimpleLocalResourceAccess;
import org.eclipse.xtext.ui.refactoring.ElementRenameArguments;
import org.eclipse.xtext.ui.refactoring.IRefactoringUpdateAcceptor;
import org.eclipse.xtext.ui.refactoring.IReferenceUpdater;
import org.eclipse.xtext.util.IAcceptor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Finds all references to renamed elements and dispatches to the {@link IReferenceUpdater}�of the referring languages
 * to calculate the updates.
 * 
 * @author Jan Koehnlein - Initial contribution and API
 */
public class ReferenceUpdaterDispatcher {

	@Inject
	private IReferenceFinder referenceFinder;

	@Inject
	private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
	
	@Inject
	private Provider<ResourceAccess> resourceAccessProvider;
	
	public void createReferenceUpdates(ElementRenameArguments elementRenameArguments, ResourceSet resourceSet,
			IRefactoringUpdateAcceptor updateAcceptor, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, "Updating references", 100);
		ResourceAccess resourceAccess = resourceAccessProvider.get();
		resourceAccess.registerResourceSet(resourceSet);
		
		ReferenceDescriptionAcceptor referenceDescriptionAcceptor = createFindReferenceAcceptor(updateAcceptor);
		referenceFinder.findAllReferences(elementRenameArguments.getRenamedElementURIs(), 
				resourceAccess,
				referenceDescriptionAcceptor, progress.newChild(2));
		Multimap<IReferenceUpdater, IReferenceDescription> updater2descriptions = referenceDescriptionAcceptor
				.getReferenceUpdater2ReferenceDescriptions();
		SubMonitor updaterProgress = progress.newChild(98).setWorkRemaining(updater2descriptions.keySet().size());
		for (IReferenceUpdater referenceUpdater : updater2descriptions.keySet()) {
			createReferenceUpdates(referenceUpdater, elementRenameArguments,
					updater2descriptions.get(referenceUpdater), updateAcceptor, updaterProgress);
		}
	}

	protected void createReferenceUpdates(IReferenceUpdater referenceUpdater,
			ElementRenameArguments elementRenameArguments, Iterable<IReferenceDescription> referenceDescriptions,
			IRefactoringUpdateAcceptor updateAcceptor, SubMonitor updaterProgress) {
		if (updaterProgress.isCanceled())
			return;
		referenceUpdater.createReferenceUpdates(elementRenameArguments, referenceDescriptions, updateAcceptor,
				updaterProgress.newChild(1));
	}

	protected ReferenceDescriptionAcceptor createFindReferenceAcceptor(IRefactoringUpdateAcceptor updateAcceptor) {
		return new ReferenceDescriptionAcceptor(resourceServiceProviderRegistry, updateAcceptor.getRefactoringStatus());
	}

	public static class ReferenceDescriptionAcceptor implements IAcceptor<IReferenceDescription> {

		private Map<IResourceServiceProvider, IReferenceUpdater> provider2updater = newHashMap();
		private Multimap<IReferenceUpdater, IReferenceDescription> updater2refs = HashMultimap.create();

		private StatusWrapper status;
		private final Registry resourceServiceProviderRegistry;

		public ReferenceDescriptionAcceptor(IResourceServiceProvider.Registry resourceServiceProviderRegistry,
				StatusWrapper status) {
			this.resourceServiceProviderRegistry = resourceServiceProviderRegistry;
			this.status = status;
		}

		public void accept(IReferenceDescription referenceDescription) {
			if (referenceDescription.getSourceEObjectUri() == null
					|| referenceDescription.getTargetEObjectUri() == null
					|| referenceDescription.getEReference() == null) {
				handleCorruptReferenceDescription(referenceDescription, status);
			} else {
				URI sourceResourceURI = referenceDescription.getSourceEObjectUri().trimFragment();
				IReferenceUpdater referenceUpdater = getReferenceUpdater(sourceResourceURI);
				if (referenceUpdater == null)
					handleNoReferenceUpdater(sourceResourceURI, status);
				else
					updater2refs.put(referenceUpdater, referenceDescription);
			}
		}

		protected void handleNoReferenceUpdater(URI sourceResourceURI, StatusWrapper status) {
			status.add(WARNING,
					"References from {0} will not be updated as the language has not registered an IReferenceUpdater",
					sourceResourceURI);
		}

		protected void handleCorruptReferenceDescription(IReferenceDescription referenceDescription,
				StatusWrapper status) {
			status.add(ERROR,
					"Xtext index contains invalid entries. It is suggested to perform a workspace refresh and a clean build.");
		}

		protected IReferenceUpdater getReferenceUpdater(URI sourceResourceURI) {
			IResourceServiceProvider resourceServiceProvider = resourceServiceProviderRegistry
					.getResourceServiceProvider(sourceResourceURI);
			IReferenceUpdater referenceUpdater = provider2updater.get(resourceServiceProvider);
			if (referenceUpdater == null) {
				referenceUpdater = resourceServiceProvider.get(OptionalReferenceUpdaterProxy.class).get();
				if (referenceUpdater != null)
					provider2updater.put(resourceServiceProvider, referenceUpdater);
			}
			return referenceUpdater;
		}

		public Multimap<IReferenceUpdater, IReferenceDescription> getReferenceUpdater2ReferenceDescriptions() {
			return updater2refs;
		}
	}

	protected static class OptionalReferenceUpdaterProxy {
		@Inject(optional = true)
		private IReferenceUpdater referenceUpdater;

		public IReferenceUpdater get() {
			return referenceUpdater;
		}
	}
}