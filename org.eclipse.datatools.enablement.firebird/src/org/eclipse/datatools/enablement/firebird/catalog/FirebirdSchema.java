/*
 * Copyright (C) 2007 - 2009 members of the Firebird development team
 * and others.
 * This file was created by members of the Firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals. Contributors to this file are either listed here or
 * can be obtained from a source control (eg CVS) history command.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roman Rokytskyy  - Initial implementation
 *     Mark Rotteveel   - Code cleanup, further development
 */

package org.eclipse.datatools.enablement.firebird.catalog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdRoutineLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdSequenceLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdTableLoader;
import org.eclipse.datatools.modelbase.sql.schema.SQLSchemaPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Schema implementation for Firebird, its purpose is to load tables.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public class FirebirdSchema extends JDBCSchema implements ICatalogObject {

    private final boolean systemSchema;

    private final Object sequenceLoadMutex = new Object();
    private boolean sequencesLoaded = false;

    /**
     * 
     * @param name
     *            Name of the schema
     * @param systemSchema
     *            true if system schema
     */
    public FirebirdSchema(String name, boolean systemSchema) {
        this.systemSchema = systemSchema;
        this.setName(name);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema#refresh()
     */
    public void refresh() {
        synchronized (sequenceLoadMutex) {
            sequencesLoaded = false;
        }
        super.refresh();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema#eIsSet(org.eclipse.emf.ecore.EStructuralFeature)
     */
    public boolean eIsSet(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
            case SQLSchemaPackage.SCHEMA__TRIGGERS:
                getTriggers();
                break;
        }
        return super.eIsSet(eFeature);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema#createTableLoader()
     */
    protected JDBCTableLoader createTableLoader() {
        return new FirebirdTableLoader(this, systemSchema);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema#createRoutineLoader()
     */
    protected JDBCRoutineLoader createRoutineLoader() {
        return new FirebirdRoutineLoader(this, systemSchema);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.modelbase.sql.schema.impl.SchemaImpl#getSequences()
     */
    public EList getSequences() {
        synchronized (sequenceLoadMutex) {
            if (!sequencesLoaded)
                loadSequences();
        }

        return super.getSequences();
    }

    /**
     * Load the sequences for this schema.
     */
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

    /**
     * 
     * @return sequence loader for this schema.
     */
    protected FirebirdSequenceLoader getSequenceLoader() {
        return new FirebirdSequenceLoader(this, systemSchema);
    }
}
