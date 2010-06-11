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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableIndexLoader;
import org.eclipse.datatools.enablement.firebird.Activator;

/**
 * Index loader for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdIndexLoader extends JDBCTableIndexLoader {

	private static final String GET_INDEX_INFO = 
	          "SELECT"
			+ "  NULL as TABLE_CAT "
	        + ", NULL as TABLE_SCHEM "
	        + ", TRIM(ind.RDB$RELATION_NAME) AS TABLE_NAME "
	        + ", CASE ind.RDB$UNIQUE_FLAG "
	        + "    WHEN 1 THEN 'F'"
	        + "    ELSE 'T'"
	        + "  END AS NON_UNIQUE "
	        + ", NULL as INDEX_QUALIFIER "
	        + ", TRIM(ind.RDB$INDEX_NAME) as INDEX_NAME "
	        + ", " + DatabaseMetaData.tableIndexOther + " as TYPE "
	        + ", ise.rdb$field_position + 1 as ORDINAL_POSITION "
	        + ", TRIM(ise.rdb$field_name) as COLUMN_NAME "
	        + ", CASE ind.RDB$INDEX_TYPE "
	        + "    WHEN 1 THEN 'D' "
	        + "    ELSE 'A' " 
	        + "  END as ASC_OR_DESC "
	        + ", 0 as CARDINALITY "
	        + ", 0 as \"PAGES\" "
	        + ", NULL as FILTER_CONDITION "
			+ "FROM"
			+ " rdb$indices ind,"
			+ " rdb$index_segments ise "
			+ "WHERE"
			+ " ind.rdb$index_name = ise.rdb$index_name"
			+ " AND ind.rdb$relation_name = ?"
			+ " AND NOT EXISTS ("
			+ "  SELECT * FROM rdb$relation_constraints rc"
			+ "  WHERE rc.rdb$index_name = ind.rdb$index_name) "
			+ "ORDER BY 4, 6, 8";
	
	/**
	 * Constructs the index loader.
	 */
	public FirebirdIndexLoader() {
	    super(null, null);
	}

	/**
	 * Constructs the index loader with a filter.
	 * 
     * @param catalogObject the Table object upon which this loader operates.
     * @param connectionFilterProvider the filter provider used for filtering
     *        the "index" objects being loaded
	 */
	public FirebirdIndexLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider) {
		super(catalogObject, connectionFilterProvider);
	}

	/**
	 * Constructs the index loader.
	 * 
     * @param catalogObject the Table object upon which this loader operates.
	 * @param systemIndexes true: system indices, false: normal indices
	 */
	public FirebirdIndexLoader(ICatalogObject catalogObject) {
		super(catalogObject);
	}

	/**
     * Creates a result set to be used by the loading logic.
     * 
     * @return a result containing the information used to initialize Index
     *         objects
     * 
     * @throws SQLException if an error occurs
     */
	protected ResultSet createResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_INDEX_INFO);
			stmt.setString(1, getTable().getName());
			
			return stmt.executeQuery();
		} catch (RuntimeException e) {
			SQLException error = new SQLException(
			        Activator.getResourceString("error.index.loading")); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}
}
