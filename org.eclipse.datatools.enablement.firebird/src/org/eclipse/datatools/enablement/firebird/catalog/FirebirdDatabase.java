package org.eclipse.datatools.enablement.firebird.catalog;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.RefreshManager;
import org.eclipse.datatools.modelbase.sql.schema.Catalog;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.SQLSchemaPackage;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.schema.impl.DatabaseImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;

/**
 * This is the Database implementation, and contains schemas (Firebird does not
 * have schemas).
 * 
 * @author Roman Rokytskyy
 */
public class FirebirdDatabase extends DatabaseImpl implements ICatalogObject,
		Catalog {

	private static final long serialVersionUID = 1L;

	private Connection connection;

	public FirebirdDatabase(Connection connection) {
		super();
		if (connection == null) {
			System.err.println("null connection"); //$NON-NLS-1$
			throw new RuntimeException();
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
		if (schemas != null) {
			schemas.clear();
			schemas = null;
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
		if (schemas == null) {
			schemas = new EObjectWithInverseResolvingEList(Schema.class, this,
					SQLSchemaPackage.DATABASE__SCHEMAS,
					SQLSchemaPackage.SCHEMA__DATABASE);

			Schema schema = new FirebirdSchema(this, false);
			schema.setName("USER");
			schemas.add(schema);

			schema = new FirebirdSchema(this, true);
			schema.setName("SYSTEM");
			schemas.add(schema);
		}
		return this.schemas;
	}

	public Database getDatabase() {
		return this;
	}

	public void setDatabase(Database value) {
		throw new UnsupportedOperationException();
	}

}
