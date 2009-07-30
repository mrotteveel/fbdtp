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
 */ 

package org.eclipse.datatools.sqltools.db.firebird.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.sqltools.core.DBHelper;
import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.ProcIdentifier;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdHelper extends DBHelper {
    
    public ProcIdentifier getProcIdentifier(DatabaseIdentifier databaseIdentifier, String dbObjectName, int dbObjectType, String tableName, String ownerName) {
        Map map = new HashMap();

        //don't put it null values which will cause problem when encoding/decoding
        if (ownerName != null)
        {
            map.put(ProcIdentifier.PROP_OWNER, ownerName);
        }
        if (dbObjectName != null)
        {
            map.put(ProcIdentifier.PROP_NAME, dbObjectName);
        }
        if (tableName != null)
        {
            map.put(ProcIdentifier.PROP_TABLENAME, tableName);
        }

        return new FirebirdProcIdentifier(dbObjectType, databaseIdentifier, map);
    }
}
