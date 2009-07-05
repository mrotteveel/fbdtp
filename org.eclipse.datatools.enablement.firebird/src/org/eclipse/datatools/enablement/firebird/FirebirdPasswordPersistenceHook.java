package org.eclipse.datatools.enablement.firebird;

import org.eclipse.datatools.connectivity.drivers.jdbc.JDBCPasswordPropertyPersistenceHook;

public class FirebirdPasswordPersistenceHook extends
        JDBCPasswordPropertyPersistenceHook {

    public String getConnectionPropertiesPageID() {
        return IFirebirdConnectionProfileConstants.CONNECTION_PROPERTY_PAGE_ID;
    }

}
