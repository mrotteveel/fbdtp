package org.eclipse.datatools.enablement.firebird.catalog;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdRoutineLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdSequenceLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdTableLoader;
import org.eclipse.datatools.modelbase.sql.schema.Catalog;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.SQLSchemaPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * This class is the Schema implementation, its purpose is to load tables.
 * 
 * @author Roman Rokytskyy
 */
public class FirebirdSchema extends JDBCSchema implements ICatalogObject {

    private static final long serialVersionUID = 1L;

    private FirebirdDatabase catalogDatabase;

    private boolean systemSchema;

    private Object sequenceLoadMutex = new Object();
    private boolean sequencesLoaded = false;

    public FirebirdSchema(FirebirdDatabase catalogDatabase, boolean systemSchema) {
        this.catalogDatabase = catalogDatabase;
        this.systemSchema = systemSchema;
    }

    public Catalog getCatalog() {
        return catalogDatabase;
    }

    public synchronized void refresh() {
        super.refresh();

        // RefreshManager.getInstance().referesh(this);
    }

    public boolean isSystemObject() {
        return false;
    }
    
    public Connection getConnection() {
        Database database = getDatabase();
        return ((FirebirdDatabase) database).getConnection();
    }

    public Database getCatalogDatabase() {
        return getDatabase();
    }

    public boolean eIsSet(EStructuralFeature eFeature) {
        switch(eDerivedStructuralFeatureID(eFeature)) {
            case SQLSchemaPackage.SCHEMA__TRIGGERS:
                getTriggers();
                break;
        }
        return super.eIsSet(eFeature);
    }

    protected JDBCTableLoader createTableLoader() {
        return new FirebirdTableLoader(this, systemSchema);
    }

    protected JDBCRoutineLoader createRoutineLoader() {
        return new FirebirdRoutineLoader(this, systemSchema);
    }

    public EList getSequences() {
        synchronized (sequenceLoadMutex) {
            if (!sequencesLoaded)
                loadSequences();
        }

        return super.getSequences();
    }

    protected void loadSequences() {
        synchronized (sequenceLoadMutex) {
            boolean deliver = eDeliver();
            try {
                List container = super.getSequences();
                List existingSequences = new ArrayList(container);

                eSetDeliver(false);

                container.clear();

                final FirebirdSequenceLoader sequenceLoader = getSequenceLoader();
                sequenceLoader.loadSequences(container, existingSequences);
                sequenceLoader.clearSequences(existingSequences);

                sequencesLoaded = true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                eSetDeliver(deliver);
            }
        }
    }

    protected FirebirdSequenceLoader getSequenceLoader() {
        return new FirebirdSequenceLoader(this, systemSchema);
    }
}
