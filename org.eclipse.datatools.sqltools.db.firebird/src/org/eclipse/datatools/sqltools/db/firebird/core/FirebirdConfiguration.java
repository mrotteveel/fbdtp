package org.eclipse.datatools.sqltools.db.firebird.core;

import org.eclipse.datatools.enablement.firebird.IFBConstants;
import org.eclipse.datatools.sqltools.core.DatabaseVendorDefinitionId;
import org.eclipse.datatools.sqltools.core.SQLDevToolsConfiguration;
import org.eclipse.datatools.sqltools.core.services.ConnectionService;
import org.eclipse.datatools.sqltools.core.services.ExecutionService;
import org.eclipse.datatools.sqltools.core.services.SQLService;
import org.eclipse.datatools.sqltools.db.firebird.core.services.FirebirdConnectionService;
import org.eclipse.datatools.sqltools.db.firebird.core.services.FirebirdExecutionService;
import org.eclipse.datatools.sqltools.db.firebird.core.services.FirebirdSQLService;

/**
 * Firebird implementation of SQLDevToolsConfiguration.
 * 
 * @author Roman Rokytskyy 
 * @author Mark Rotteveel
 *
 */
public class FirebirdConfiguration extends SQLDevToolsConfiguration {

    private static FirebirdConfiguration _instance = null;
    public static final String[] FIREBIRD_ALIASES = {"Firebird", "Jaybird"};
    
    public FirebirdConfiguration() {
        _instance = this;
    }
    
	public static SQLDevToolsConfiguration getDefaultInstance() {
	    if (_instance == null) {
	        return new FirebirdConfiguration();
	    }
		return _instance;
	}
	
    public ConnectionService getConnectionService() {
        return new FirebirdConnectionService();
    }
    
    public SQLService getSQLService() {
        return new FirebirdSQLService();
    }
    
    public ExecutionService getExecutionService() {
        return new FirebirdExecutionService();
    }
    
	// TODO Implement other specific methods
	
    public boolean recognize(String product, String version) {
        DatabaseVendorDefinitionId targetid = new DatabaseVendorDefinitionId(product, version);
        for (int i = 0; i < FIREBIRD_ALIASES.length; i++) {
            DatabaseVendorDefinitionId id = new DatabaseVendorDefinitionId(FIREBIRD_ALIASES[i], getDatabaseVendorDefinitionId().getVersion());
            if (id.equals(targetid)) {
            	return true;
            }
        }
        return false;
    }

    public String[] getAssociatedConnectionProfileType() {
        return new String[] { IFBConstants.FIREBIRD_CONNECTION_PROFILE };
    }
}
