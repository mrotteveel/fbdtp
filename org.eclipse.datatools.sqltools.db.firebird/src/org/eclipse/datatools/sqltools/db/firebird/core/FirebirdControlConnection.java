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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.modelbase.sql.routines.Routine;
import org.eclipse.datatools.modelbase.sql.schema.*;
import org.eclipse.datatools.sqltools.core.*;
import org.eclipse.datatools.sqltools.core.dbitem.IDBItem;
import org.eclipse.datatools.sqltools.core.dbitem.ParameterDescriptor;
import org.eclipse.datatools.sqltools.core.internal.dbitem.SQLObjectItem;
import org.eclipse.datatools.sqltools.core.profile.NoSuchProfileException;
import org.eclipse.datatools.sqltools.core.profile.ProfileUtil;
import org.eclipse.datatools.sqltools.internal.core.AbstractControlConnection;
import org.eclipse.datatools.sqltools.sql.util.ModelUtil;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditorStorageEditorInput;
import org.eclipse.emf.common.util.EList;
import org.eclipse.ui.*;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdParameter;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdProcedure;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdUDF;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdControlConnection extends AbstractControlConnection {

    /**
     * @param sd
     * @throws SQLException
     * @throws NoSuchProfileException
     */
    public FirebirdControlConnection(IControlConnectionManager manager,
            DatabaseIdentifier databaseIdentifier) {
        super(manager, databaseIdentifier);
    }

    protected void aboutToDisconnect() {
        IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < wins.length; i++) {
            IWorkbenchPage[] pages = wins[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IEditorReference[] editors = pages[j].getEditorReferences();
                for (int k = 0; k < editors.length; k++) {
                    try {
                        IEditorInput input = editors[k].getEditorInput();
                        if (input instanceof SQLEditorStorageEditorInput) {
                            IConnectionProfile profile = ((SQLEditorStorageEditorInput) input)
                                    .getConnectionInfo().getConnectionProfile();
                            boolean connected = profile != null
                                    && profile.getConnectionState() == IConnectionProfile.CONNECTED_STATE;

                            if (connected) {
                                final IEditorPart editor = editors[k]
                                        .getEditor(false);
                                if (editor != null) {
                                    final IWorkbenchPage page = pages[j];
                                    PlatformUI.getWorkbench().getDisplay()
                                            .syncExec(new Runnable() {
                                                public void run() {
                                                    page.closeEditor(editor,
                                                            true);
                                                }
                                            });
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        EditorCorePlugin.getDefault().log(e);
                    }

                }
            }
        }
    }

    protected IDBItem createDBItem(ProcIdentifier proc) {
        Database db = ProfileUtil.getDatabase(proc.getDatabaseIdentifier());
        SQLObject obj = null;
        if (proc.getType() == ProcIdentifier.TYPE_UDF
                || proc.getType() == ProcIdentifier.TYPE_SP) {
            EList schemas = db.getSchemas();
            for (Iterator iterator = schemas.iterator(); iterator.hasNext();) {
                Schema schema = (Schema) iterator.next();

                if (!schema.getName().equals(proc.getOwnerName()))
                    continue;

                EList routines = schema.getRoutines();
                for (Iterator iter = routines.iterator(); iter.hasNext();) {
                    Routine routine = (Routine) iter.next();

                    if ((routine instanceof FirebirdProcedure)
                            && proc.getType() == ProcIdentifier.TYPE_SP) {
                        if (routine.getName().equals(proc.getProcName()))
                            obj = routine;
                        break;
                    }
                    else if ((routine instanceof FirebirdUDF)
                            && proc.getType() == ProcIdentifier.TYPE_UDF) {
                        if (routine.getName().equals(proc.getProcName()))
                            obj = routine;
                        break;
                    }
                }
            }
        }

        if (obj == null)
            obj = ModelUtil.findProceduralObject(proc);

        if (obj != null && proc.getType() == ProcIdentifier.TYPE_UDF) {
            return new FirebirdUDFObjectItem(proc, obj, this);
        }
        else
            return new SQLObjectItem(proc, obj, this);

    }

    private static class FirebirdUDFObjectItem extends SQLObjectItem {

        public FirebirdUDFObjectItem(ProcIdentifier proc, SQLObject routine,
                IControlConnection controlConn) {
            super(proc, routine, controlConn);
        }

        public ParameterDescriptor[] getParameterDescriptor()
                throws SQLException {
            // return super.getParameterDescriptor();

            // ParameterDescriptor pd = new ParameterDescriptor(_proc
            // .getDatabaseIdentifier(), name, type, dataType,
            // precision, (short) scale, dataTypeName, nullable,
            // null);

            if (!(_routine instanceof FirebirdUDF))
                return new ParameterDescriptor[0];

            ArrayList result = new ArrayList();

            FirebirdUDF udf = (FirebirdUDF) _routine;
            EList parameters = udf.getParameters();
            for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            	FirebirdParameter parameter = (FirebirdParameter) iter
                        .next();

                ParameterDescriptor pd = new ParameterDescriptor(_proc
                        .getDatabaseIdentifier(), parameter.getName(),
                        parameter.getMode().getValue(), FirebirdConversionUtil
                                .getJdbcType(parameter.getFieldType()),
                        parameter.getFieldPrecision(), (short) parameter
                                .getFieldScale(), parameter.getDataTypeName(),
                        DatabaseMetaData.attributeNullable, null);

                boolean outParam = parameter.getArgumentPosition() == udf
                        .getReturnArgument();

                pd
                        .setParmType(outParam ? DatabaseMetaData.procedureColumnReturn
                                : DatabaseMetaData.procedureColumnIn);

                result.add(pd);
            }

            return (ParameterDescriptor[]) result
                    .toArray(new ParameterDescriptor[result.size()]);
        }
    }

}
