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
package org.eclipse.datatools.enablement.firebird.catalog.loader;

import java.sql.*;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCProcedureColumnLoader;
import org.eclipse.datatools.modelbase.sql.routines.Parameter;
import org.eclipse.datatools.modelbase.sql.routines.Procedure;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.schema.TypedElement;
import org.eclipse.datatools.modelbase.sql.tables.Column;

public class FirebirdProcedureColumnLoader extends JDBCProcedureColumnLoader {

    public FirebirdProcedureColumnLoader(ICatalogObject catalogObject,
            IConnectionFilterProvider connectionFilterProvider) {
        super(catalogObject, connectionFilterProvider);
    }

    public FirebirdProcedureColumnLoader(ICatalogObject catalogObject) {
        super(catalogObject);
    }

    protected TypedElement processRow(ResultSet rs) throws SQLException {
        String columnName = rs.getString(COLUMN_COLUMN_NAME);
        if (columnName == null || isFiltered(columnName)) { return null; }
        TypedElement retVal = null;
        if (rs.getShort(COLUMN_COLUMN_TYPE) == DatabaseMetaData.procedureColumnResult) {
            retVal = createColumn();
            initColumn((Column) retVal, rs);
        } else {
            retVal = createParameter();
            initParameter((Parameter) retVal, rs);
        }
        return retVal;
    }

    /**
     * Used to initialize a newly created column object. By default, this method
     * initializes the name, description, type and nullable. This method may be
     * overridden to initialize any vendor specific properties.
     * 
     * @param column
     *            a newly created Column object
     * @param rs
     *            the result set containing the information
     * @throws SQLException
     *             if anything goes wrong
     */
    protected void initColumn(Column column, ResultSet rs) throws SQLException {
        column.setName(rs.getString(COLUMN_COLUMN_NAME));
        column.setDescription(rs.getString(COLUMN_REMARKS));

        initType(column, rs);

        column.setNullable(rs.getInt(COLUMN_NULLABLE) == DatabaseMetaData.attributeNullable);
    }
    
    /**
     * Creates a result set to be used by the loading logic. The default version
     * uses of the JDBC DatabaseMetaData.getProcedureColumns() to create the
     * result set. This method may be overridden to use a vendor specific query.
     * However, the default logic requires columns named according to the
     * "COLUMN_*" fields. Keep this in mind if you plan to reuse the default
     * logic (e.g. initialize())
     * 
     * @return a result containing the information used to initialize Parameter
     *         objects
     * 
     * @throws SQLException if anything goes wrong
     */
    protected ResultSet createResultSet() throws SQLException {
        Procedure procedure = (Procedure)getRoutine();
        Schema schema = procedure.getSchema();
        
        DatabaseMetaData metaData = getCatalogObject().getConnection().getMetaData();
        
        String catalogName = schema.getCatalog().getName();
        String schemaName = schema.getName();
        String procedureName = procedure.getName();
        
        return metaData.getProcedureColumns(catalogName, schemaName, procedureName, null);
    }


}

