/*
 * Copyright (C) 2007 - 2009 members of the Firebird development team
 * and others.
 * This file was created by members of the Firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals. Contributors to this file are either listed here or
 * can be obtained from a source control (eg CVS) history command.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mark Rotteveel   - Initial implementation
 */ 

package org.eclipse.datatools.enablement.firebird;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Mark Rotteveel
 * 
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.datatools.enablement.firebird";

	private static final String BUNDLE_NAME = PLUGIN_ID + ".messages";

	private ResourceBundle resourceBundle;

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Retrieve message for given key.
	 * 
	 * @param key
	 *            Key for message
	 * @return Message
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
		try {
			if (bundle != null) {
				return bundle.getString(key);
			} else {
				return '!' + key + '!';
			}
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 * 
	 * @return ResourceBundle
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle
						.getBundle(Activator.BUNDLE_NAME);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

}
