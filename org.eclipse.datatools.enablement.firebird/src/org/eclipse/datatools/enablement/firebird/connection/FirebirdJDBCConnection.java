package org.eclipse.datatools.enablement.firebird.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.datatools.connectivity.DriverConnectionBase;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.Version;
import org.eclipse.datatools.enablement.firebird.IFBConstants;

/**
 * JDBCConnection implementation for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 *
 */
public class FirebirdJDBCConnection extends DriverConnectionBase {

    public static final String TECHNOLOGY_ROOT_KEY = "firebird_jdbc";
    public static final String TECHNOLOGY_NAME = "Firebird JDBC Connection";

    private Version mTechVersion = Version.NULL_VERSION;
    private Version mServerVersion = Version.NULL_VERSION;
    private String mServerName;

    public FirebirdJDBCConnection(IConnectionProfile profile, Class factoryClass) {
        super(profile, factoryClass);
        open();
    }

    protected Object createConnection(ClassLoader cl) throws Throwable {
        Properties props = getConnectionProfile().getBaseProperties();
        Properties connectionProps = new Properties();
        
        String driverClass = getDriverDefinition().getProperty(
                IFBConstants.DRIVER_CLASS_PROP_ID);
        String connectURL = props
                .getProperty(IFBConstants.URL_PROP_ID);
        String uid = props
                .getProperty(IFBConstants.USERNAME_PROP_ID);
        String pwd = props
                .getProperty(IFBConstants.PASSWORD_PROP_ID);
        String nameValuePairs = props
                .getProperty(IFBConstants.CONNECTION_PROPERTIES_PROP_ID);
        String propDelim = ",";//$NON-NLS-1$

        if (uid != null) {
            connectionProps.setProperty("user", uid); //$NON-NLS-1$
        }
        if (pwd != null) {
            connectionProps.setProperty("password", pwd); //$NON-NLS-1$
        }

        if (nameValuePairs != null && nameValuePairs.length() > 0) {
            String[] pairs = parseString(nameValuePairs, ","); //$NON-NLS-1$
            String addPairs = ""; //$NON-NLS-1$
            for (int i = 0; i < pairs.length; i++) {
                String[] namevalue = parseString(pairs[i], "="); //$NON-NLS-1$
                connectionProps.setProperty(namevalue[0], namevalue[1]);
                if (i == 0 || i < pairs.length - 1) {
                    addPairs = addPairs + propDelim;
                }
                addPairs = addPairs + pairs[i];
            }
        }

        Driver jdbcDriver = (Driver) cl.loadClass(driverClass).newInstance();
        return jdbcDriver.connect(connectURL, connectionProps);
    }

    public void close() {
        Connection connection = (Connection) getRawConnection();
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                // RJC Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getProviderName() {
        return mServerName;
    }

    public Version getProviderVersion() {
        return mServerVersion;
    }

    protected String getTechnologyRootKey() {
        return TECHNOLOGY_ROOT_KEY;
    }

    public String getTechnologyName() {
        return TECHNOLOGY_NAME;
    }

    public Version getTechnologyVersion() {
        return mTechVersion;
    }

    protected void initVersions() {
    	mServerName = "Firebird";
   		try {
			DatabaseMetaData dbmd = ((Connection)getRawConnection()).getMetaData();
			//TODO Add release and build info
			mServerVersion = new Version(dbmd.getDatabaseMajorVersion(), dbmd.getDatabaseMinorVersion(), 0, "");
			mTechVersion = new Version(dbmd.getDriverMajorVersion(), dbmd.getDatabaseMinorVersion(), 0, "");
		} catch (SQLException e) {
			// Defaults
		}
    }

    /**
     * @param str_list
     * @param token
     * @return
     */
    protected String[] parseString(String str_list, String token) {
        StringTokenizer tk = new StringTokenizer(str_list, token);
        String[] pieces = new String[tk.countTokens()];
        int index = 0;
        while (tk.hasMoreTokens())
            pieces[index++] = tk.nextToken();
        return pieces;
    }

}
