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

package org.eclipse.datatools.sqltools.db.firebird.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.ProcIdentifier;
import org.eclipse.datatools.sqltools.core.SQLDevToolsConfiguration;
import org.eclipse.datatools.sqltools.core.SQLToolsFacade;
import org.eclipse.datatools.sqltools.core.dbitem.ParameterDescriptor;
import org.eclipse.datatools.sqltools.core.profile.NoSuchProfileException;
import org.eclipse.datatools.sqltools.editor.core.connection.IConnectionTracker;
import org.eclipse.datatools.sqltools.routineeditor.launching.LaunchHelper;
import org.eclipse.datatools.sqltools.routineeditor.result.CallableSQLResultRunnable;
import org.eclipse.datatools.sqltools.routineeditor.ui.launching.LaunchUI;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdCallableRunnable extends CallableSQLResultRunnable {
    public FirebirdCallableRunnable(Connection con,
            ILaunchConfiguration configuration, boolean closeCon,
            IConnectionTracker tracker, DatabaseIdentifier databaseIdentifier)
            throws CoreException, SQLException, NoSuchProfileException {
        super(con, configuration, closeCon, tracker, databaseIdentifier);
        _sql = constructFinalCallSQLString(configuration);
    }

    
    
    /**
     * Copied and modified from LaunchHelper
     * 
     * @param configuration
     * @return
     * @throws CoreException
     */
    private String constructFinalCallSQLString(
            ILaunchConfiguration configuration) throws CoreException,
            SQLException, NoSuchProfileException {
        if (LaunchHelper.isAdHocSQL(configuration))
            return LaunchHelper.readLaunchSQLStatement(configuration);
        ProcIdentifier proc = LaunchHelper.readProcIdentifier(configuration);
        if (proc == null)
            return "";
        boolean quoted_id = LaunchHelper
                .readQuotedIDConfig(configuration, proc);
        switch (proc.getType()) {
        case ProcIdentifier.TYPE_SP:
        case ProcIdentifier.TYPE_UDF:
            return constructCALLSPString(proc, LaunchHelper
                    .readParameterList(configuration), LaunchUI
                    .getAllParameterDescriptors(proc), quoted_id);
        case ProcIdentifier.TYPE_EVENT:
        // not supported
        default:
            return "";
        }
    }

    /**
     * Copied and modified from RoutineUtil
     * 
     * @param proc
     * @param values
     * @param pds
     * @param quoted_id
     * @return
     */
    private String constructCALLSPString(ProcIdentifier proc, List values,
            ParameterDescriptor[] pds, boolean quoted_id) {
        StringBuffer buffer = new StringBuffer(20);
        // Derby doesn't support {?=
        //buffer.append("?=");
        int type = proc == null ? ProcIdentifier.TYPE_SP : proc.getType();
        SQLDevToolsConfiguration config = SQLToolsFacade.getConfigurationByProfileName(proc.getDatabaseIdentifier().getProfileName());
        buffer.append(config.getExecutionService().getCallableStatementPrefix(proc.getType()));
        String procName = null;

        if (proc != null) {
            buffer.append(proc.getCallableString(quoted_id));
            procName = proc.getProcName();
        }
        if (pds != null && pds.length > 0) {
            if (type == ProcIdentifier.TYPE_UDF
                    || type == ProcIdentifier.TYPE_SP) {
                buffer.append("("); //$NON-NLS-1$
            }
            int j = 0;
            for (int i = 0; i < pds.length; i++) {
                String name = pds[i].getName();

                if (name.equals(procName)) {
                    continue;
                }
                if (j != 0) {
                    buffer.append(",?"); //$NON-NLS-1$
                } else {
                    buffer.append("?");
                }
                j++;
            }
            if (type == ProcIdentifier.TYPE_UDF
                    || type == ProcIdentifier.TYPE_SP) {
                buffer.append(")"); //$NON-NLS-1$
            }
        } else {
            buffer.append("()");
        }
        // buffer.append("}");
        return buffer.toString();
    }
}
