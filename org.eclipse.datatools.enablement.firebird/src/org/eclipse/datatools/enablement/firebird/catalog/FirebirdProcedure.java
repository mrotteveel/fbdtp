package org.eclipse.datatools.enablement.firebird.catalog;

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCProcedure;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCProcedureColumnLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdProcedureColumnLoader;
import org.eclipse.datatools.modelbase.sql.routines.impl.SourceImpl;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdProcedure extends JDBCProcedure {

    // have to cache here, otherwise we get StackOverflowError
    private FirebirdSchema schema;
    
    public FirebirdProcedure(FirebirdSchema schema) {
        super();
        this.schema = schema;
    }
    
    public Schema getSchema() {
        return this.schema;
    }
    
    public Database getCatalogDatabase() {
        return getSchema().getDatabase();
    }

    protected JDBCProcedureColumnLoader createParameterLoader() {
        return new FirebirdProcedureColumnLoader(this);
    }

    public void setSourceCode(String code) {
        setSource(new FirebirdProcedureSource(code));
    }
    
    private static class FirebirdProcedureSource extends SourceImpl {

        public FirebirdProcedureSource(String code) {
            setBody(code);
        }
    }
}
