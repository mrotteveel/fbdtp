package org.eclipse.datatools.enablement.firebird;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
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
