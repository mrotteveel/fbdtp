package org.eclipse.datatools.enablement.firebird.catalog;

import java.sql.Connection;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.RefreshManager;
import org.eclipse.datatools.modelbase.sql.schema.Catalog;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.schema.impl.DatabaseImpl;
import org.eclipse.emf.common.util.EList;

/**
 * This is the Database implementation, and contains schemas (Firebird does not
 * have schemas).
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public class FirebirdDatabase extends DatabaseImpl implements ICatalogObject,
		Catalog {

	private static final long serialVersionUID = 1L;
	
	private final Object schemaLoadMutex = new Object();
	private boolean schemasLoaded = false;

	private Connection connection;

	public FirebirdDatabase(Connection connection) {
		if (connection == null) {
			System.err.println("null connection"); //$NON-NLS-1$
			throw new NullPointerException();
		}
		this.connection = connection;
	}

	public String getName() {
//		try {
//			return connection.getMetaData().getDatabaseProductName();
//		} catch (SQLException ex) {
//			// FIXME
//			return super.getName();
//		}
		return super.getName();
	}

	public void refresh() {
	    synchronized(schemaLoadMutex) {
    		schemasLoaded = false;
    		schemas.clear();
	    }
		RefreshManager.getInstance().referesh(this);
	}

	public Connection getConnection() {
		return connection;
	}

	public Database getCatalogDatabase() {
		return this;
	}

	public EList getSchemas() {
	    synchronized (schemaLoadMutex) {
	        if (!schemasLoaded) {
	            // Ensure this.schemas is set
	            super.getSchemas();
        
    			Schema schema = new FirebirdSchema(this, false);
    			schema.setName("USER");
    			schemas.add(schema);
    
    			schema = new FirebirdSchema(this, true);
    			schema.setName("SYSTEM");
    			schemas.add(schema);
    			
    			schemasLoaded = true;
	        }
	    }
		return super.getSchemas();
	}

	public Database getDatabase() {
		return this;
	}

	public void setDatabase(Database value) {
		throw new UnsupportedOperationException();
	}

}
