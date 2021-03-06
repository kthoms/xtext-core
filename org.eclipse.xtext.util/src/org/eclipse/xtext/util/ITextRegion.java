/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.util;

/**
 * Represents a range in a stream of characters.
 * @author Jan Koehnlein - Initial contribution and API
 */
public interface ITextRegion {
	int getOffset();

	int getLength();

	ITextRegion merge(ITextRegion region);
	
	boolean contains(ITextRegion other);
	
	boolean contains(int offset);

	static ITextRegion EMPTY_REGION = ITextRegionWithLineInformation.EMPTY_REGION;
}
