package org.eclipse.datatools.enablement.firebird.catalog.loader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCSchemaLoader;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdSchema;
import org.eclipse.datatools.modelbase.sql.schema.Schema;

public class FirebirdSchemaLoader extends JDBCSchemaLoader {
    
    public FirebirdSchemaLoader() {
        this(null);
    }
    
    public FirebirdSchemaLoader(ICatalogObject catalogObject) {
        super(catalogObject);
    }
    
    public FirebirdSchemaLoader(ICatalogObject catalogObject,
            IConnectionFilterProvider connectionFilterProvider) {
        super(catalogObject, connectionFilterProvider);
    }
    
    /**
     * Firebird specific implementation of loadSchemas to account for the non-existence
     * of schemas.
     * 
     * Creates two schemas: SYSTEM and USER to separate system objects for user objects.
     */
    public void loadSchemas(List containmentList, Collection existingSchemas)
        throws SQLException {
        initActiveFilter();
        createSchema(Activator.getResourceString("schema.system"), true, containmentList, existingSchemas);
        createSchema(Activator.getResourceString("schema.user"), false, containmentList, existingSchemas);
    }
    
    private void createSchema(String name, boolean systemSchema, List containmentList, Collection existingSchemas) {
        if (!isFiltered(name)) {
            Schema schema = (Schema)getAndRemoveSQLObject(existingSchemas, name);
            if (schema == null) {
                schema = new FirebirdSchema(name, systemSchema);
                containmentList.add(schema);
            } else {
                containmentList.add(schema);
                if (schema instanceof ICatalogObject) {
                    ((ICatalogObject)schema).refresh();
                }
            }
        }
    }

}
