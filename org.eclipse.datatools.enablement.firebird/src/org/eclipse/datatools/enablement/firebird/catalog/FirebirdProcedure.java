/*
 * Copyright (C) 2007 - 2013 members of the Firebird development team
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

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCProcedure;
import org.eclipse.datatools.modelbase.sql.routines.impl.SourceImpl;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;

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

    public void setSourceCode(String code) {
        setSource(new FirebirdProcedureSource(code));
    }
    
    private static class FirebirdProcedureSource extends SourceImpl {

        public FirebirdProcedureSource(String code) {
            setBody(code);
        }
    }
}
