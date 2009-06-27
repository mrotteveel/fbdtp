package org.eclipse.datatools.sqltools.db.firebird.core;

import org.eclipse.datatools.enablement.firebird.IFirebirdConnectionProfileConstants;
import org.eclipse.datatools.sqltools.core.DatabaseVendorDefinitionId;
import org.eclipse.datatools.sqltools.core.SQLDevToolsConfiguration;

/**
 * Firebird implementation of SQLDevToolsConfiguration.
 * 
 * @author Roman Rokytskyy 
 * @author Mark Rotteveel
 *
 */
public class FirebirdConfiguration extends SQLDevToolsConfiguration {

    private static FirebirdConfiguration _instance = new FirebirdConfiguration();
    public static final String[] FIREBIRD_ALIASES = {"Firebird", "Jaybird"};
    
    protected FirebirdConfiguration() {
    	// TODO Set DatabaseVendorDefinition
    }
    
	public static SQLDevToolsConfiguration getDefaultInstance() {
		return _instance;
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
        return new String[] { IFirebirdConnectionProfileConstants.FIREBIRD_CONNECTION_PROFILE };
    }
}
