package org.eclipse.datatools.enablement.firebird.connection;

public interface IDBConnectionProfileConstants extends IDBDriverDefinitionConstants {
	/**
	 * The connection profile ID for the generic DB connection profile type.
	 */
	public static final String CONNECTION_PROFILE_ID = "org.eclipse.datatools.connectivity.db.generic.connectionProfile"; //$NON-NLS-1$

	/**
	 * The property page ID for the generic DB connection properties.
	 */
	public static final String CONNECTION_PROPERTY_PAGE_ID = "org.eclipse.datatools.connectivity.db.generic.profileProperties"; //$NON-NLS-1$

	/**
	 * Property used to store JDBC connection properties (i.e. properties passed
	 * as a <code>Properties</code> object to
	 * <code>java.sql.Driver.connect()</code>).
	 */
	public static final String CONNECTION_PROPERTIES_PROP_ID = PROP_PREFIX + "connectionProperties"; //$NON-NLS-1$

	/**
	 * Property used to store the persistence setting for the password. A value
	 * of true indicates the password will be persisted within the workspace.
	 */
	public static final String SAVE_PASSWORD_PROP_ID = PROP_PREFIX + "savePWD"; //$NON-NLS-1$
}
