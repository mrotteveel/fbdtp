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
 * This class is the Schema implementation, its purpose is to load tables.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
public class FirebirdSchema extends JDBCSchema implements ICatalogObject {

    private static final long serialVersionUID = 1L;

    private boolean systemSchema;

    private final Object sequenceLoadMutex = new Object();
    private boolean sequencesLoaded = false;

    public FirebirdSchema(String name, boolean systemSchema) {
        this.systemSchema = systemSchema;
        this.setName(name);
    }

    public synchronized void refresh() {
        synchronized (sequenceLoadMutex) {
            sequencesLoaded = false;
        }
        super.refresh();
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
