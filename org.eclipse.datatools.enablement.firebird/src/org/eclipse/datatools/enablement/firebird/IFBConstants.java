package org.eclipse.datatools.enablement.firebird;

/**
 * @author brianf
 *
 */
public interface IFBConstants {

	public static final String FIREBIRD_CATEGORY_ID = "org.eclipse.datatools.enablement.firebird.driver.category"; //$NON-NLS-1$
	public static final String FIREBIRD_CONNECTION_PROFILE = "org.eclipse.datatools.enablement.firebird.connectionProfile"; //$NON-NLS-1$
    public static final String CONNECTION_PROPERTY_PAGE_ID = "org.eclipse.datatools.enablement.firebird.connectionProfile.profileProperties";
    /**
     * Property prefix used for property keys
     */
    public static final String PROP_PREFIX = "org.eclipse.datatools.connectivity.db."; //$NON-NLS-1$

    /**
     * Property used to store the DB vendor name.  This property is used in
     * resolving the dbdefinition referenced by the driver.
     */
    public static final String DATABASE_VENDOR_PROP_ID = PROP_PREFIX + "vendor"; //$NON-NLS-1$

    /**
     * Property used to store the DB version.  This property is used in
     * resolving the dbdefinition referenced by the driver.
     */
    public static final String DATABASE_VERSION_PROP_ID = PROP_PREFIX + "version"; //$NON-NLS-1$
    
    /**
     * Property used to store the driver class name.  This property is used in
     * creating JDBC connections.
     */
    public static final String DRIVER_CLASS_PROP_ID = PROP_PREFIX + "driverClass"; //$NON-NLS-1$

    /**
     * Property used to store the default DB name value.  This property is used
     * by the generic DB connection profile wizard during initialization.  This
     * property is also used by the DB connection profile.
     */
    public static final String DATABASE_NAME_PROP_ID = PROP_PREFIX + "databaseName"; //$NON-NLS-1$

    /**
     * Property used to store the default connection URL.  This property is used
     * the generic DB connection profile wizard during initialization.  This
     * property is also used by the DB connection profile.
     */
    public static final String URL_PROP_ID = PROP_PREFIX + "URL"; //$NON-NLS-1$
    
    /**
     * Property used to store the default user name.  This property is used
     * the generic DB connection profile wizard during initialization.  This
     * property is also used by the DB connection profile.
     */
    public static final String USERNAME_PROP_ID = PROP_PREFIX + "username"; //$NON-NLS-1$
    
    /**
     * Property used to store the default password.  This property is used
     * the generic DB connection profile wizard during initialization.  This
     * property is also used by the DB connection profile.
     */
    public static final String PASSWORD_PROP_ID = PROP_PREFIX + "password"; //$NON-NLS-1$
    
    /**
     * Property used to store JDBC connection properties (i.e. properties passed
     * as a <code>Properties</code> object to <code>java.sql.Driver.connect()</code>).
     */
    public static final String CONNECTION_PROPERTIES_PROP_ID = PROP_PREFIX + "connectionProperties"; //$NON-NLS-1$

    /**
     * Property used to store the persistence setting for the password.  A value
     * of true indicates the password will be persisted within the workspace.
     */
    public static final String SAVE_PASSWORD_PROP_ID = PROP_PREFIX + "savePWD"; //$NON-NLS-1$

	
}