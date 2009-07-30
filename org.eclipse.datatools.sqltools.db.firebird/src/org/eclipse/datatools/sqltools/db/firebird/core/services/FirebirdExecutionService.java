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

package org.eclipse.datatools.sqltools.db.firebird.core.services;

import java.sql.Connection;

import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.ProcIdentifier;
import org.eclipse.datatools.sqltools.core.services.ExecutionService;
import org.eclipse.datatools.sqltools.db.firebird.core.FirebirdCallableRunnable;
import org.eclipse.datatools.sqltools.editor.core.connection.IConnectionTracker;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdExecutionService extends ExecutionService {

    public Runnable createCallableSQLResultRunnable(Connection con,
            ILaunchConfiguration configuration, boolean closeCon,
            IConnectionTracker tracker, DatabaseIdentifier databaseIdentifier) {
        try {
            return new FirebirdCallableRunnable(con, configuration, closeCon,
                    tracker, databaseIdentifier);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a String prefix for invoking Routine with the specified
     * type
     * 
     * @return SQL construct that can invoke Routine with the specified
     *         type
     */
    public String getCallableStatementPrefix(int type) {
        String prefix = "";
        switch (type) {
        case ProcIdentifier.TYPE_SP:
            prefix = "call ";
            break;
        case ProcIdentifier.TYPE_UDF:
            prefix = "values ";
            break;
        }
        return prefix;
    }

}
