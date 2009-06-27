package org.eclipse.datatools.enablement.firebird.catalog;

import java.sql.Connection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogProvider;
import org.eclipse.datatools.modelbase.sql.schema.Database;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdCatalogProvider implements ICatalogProvider,
		IExecutableExtension {

	private String product;
	private String version;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {

		this.product = config.getAttribute("product");
		this.version = config.getAttribute("version");
	}

	public Database getCatalogDatabase(Connection connection) {
		Database database = new FirebirdDatabase(connection);

		database.setVendor(this.product);
		database.setVersion(this.version);

		return database;
	}

}
