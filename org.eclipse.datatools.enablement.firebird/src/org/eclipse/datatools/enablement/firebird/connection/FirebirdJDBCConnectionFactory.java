package org.eclipse.datatools.enablement.firebird.connection;

import org.eclipse.datatools.connectivity.IConnection;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.drivers.jdbc.JDBCConnectionFactory;

/**
 * JDBCConnectionFactory implementation for the Firebird database.
 * 
 * @author brianf
 * @author Mark Rotteveel
 *
 */
public class FirebirdJDBCConnectionFactory extends JDBCConnectionFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.db.generic.JDBCConnectionFactory#createConnection(org.eclipse.datatools.connectivity.IConnectionProfile)
	 */
	public IConnection createConnection(IConnectionProfile profile) {
	    FirebirdJDBCConnection connection = new FirebirdJDBCConnection(profile, getClass());
	    connection.open();
		return connection;
	}

}
