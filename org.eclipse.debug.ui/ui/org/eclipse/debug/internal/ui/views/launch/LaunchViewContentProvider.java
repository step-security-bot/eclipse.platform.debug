/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.ui.DefaultDebugViewContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the debug view.
 */
public class LaunchViewContentProvider implements ITreeContentProvider {
	
	/**
	 * Map of custom content providers keyed by debug model id.
	 */
	private Map fContentProviders;

	/**
	 * Default content provider for models that do not supply their own.
	 */
	private ITreeContentProvider fDefaultContentProvider;
	
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return getContentProvider(parent).getChildren(parent);
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		return getContentProvider(element).getParent(element);
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		return getContentProvider(element).hasChildren(element);
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * Nothing to dispose.
	 * 
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
		if (fDefaultContentProvider != null) {
			fDefaultContentProvider.dispose();
		}
		if (fContentProviders != null) {
			Iterator iterator = fContentProviders.values().iterator();
			while (iterator.hasNext()) {
				ITreeContentProvider provider = (ITreeContentProvider) iterator.next();
				provider.dispose();
			}
			fContentProviders.clear();
		}
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	/**
	 * Returns the content provider to use for the given object.
	 * 
	 * @param object object for which content is being queried
	 * @return the content provider to use for the given object
	 */
	private ITreeContentProvider getContentProvider(Object object) {
		if (object instanceof IDebugElement) {
			IDebugElement element = (IDebugElement) object;
			String modelIdentifier = element.getModelIdentifier();
			LaunchConfigurationPresentationManager manager = LaunchConfigurationPresentationManager.getDefault();
			ITreeContentProvider provider = null;
			if (manager.hasDebugViewContentProivder(modelIdentifier)) {
				if (fContentProviders == null) {
					fContentProviders = new HashMap();
				}
				provider = (ITreeContentProvider) fContentProviders.get(modelIdentifier);
				if (provider == null) {
					provider = manager.newDebugViewContentProvider(modelIdentifier);
					if (provider != null) {
						fContentProviders.put(modelIdentifier, provider);
					}
				}
			}
			if (provider != null) {
				return provider;
			}
		}
		return getDefaultContentProvider(); 
	}
	
	/**
	 * Returns the default content provider
	 * 
	 * @return default content provider
	 */
	private ITreeContentProvider getDefaultContentProvider() {
		if (fDefaultContentProvider == null) {
			fDefaultContentProvider = new DefaultDebugViewContentProvider();
		}
		return fDefaultContentProvider;
	}

}
