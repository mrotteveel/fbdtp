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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableIndexLoader;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.modelbase.sql.constraints.Index;
import org.eclipse.datatools.modelbase.sql.constraints.IndexMember;
import org.eclipse.datatools.modelbase.sql.tables.Column;

/**
 * Index loader for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdIndexLoader extends JDBCTableIndexLoader {

    //TODO Do we actually need to differentiate between system and non-system objects?
	private static final String GET_INDEX_INFO = 
	          "SELECT"
			+ " ind.RDB$RELATION_NAME AS TABLE_NAME,"
			+ " ind.RDB$UNIQUE_FLAG AS NON_UNIQUE,"
			+ " ind.RDB$INDEX_NAME as INDEX_NAME,"
			+ " ise.rdb$field_position+1 as ORDINAL_POSITION,"
			+ " ise.rdb$field_name as COLUMN_NAME,"
			+ " ind.RDB$INDEX_TYPE as ASC_OR_DESC "
			+ "FROM"
			+ " rdb$indices ind,"
			+ " rdb$index_segments ise "
			+ "WHERE"
			+ " ind.rdb$index_name = ise.rdb$index_name"
			+ " AND ind.rdb$relation_name = ?"
			+ " AND ind.rdb$system_flag = ?"
			+ " AND NOT EXISTS ("
			+ "  SELECT * FROM rdb$relation_constraints rc"
			+ "  WHERE rc.rdb$index_name = ind.rdb$index_name) "
			+ "ORDER BY 2, 3, 4";

	private final boolean systemIndexes;

	/**
	 * 
     * @param catalogObject the Table object upon which this loader operates.
     * @param connectionFilterProvider the filter provider used for filtering
     *        the "index" objects being loaded
	 * @param systemIndexes true: system indices, false: normal indices
	 */
	public FirebirdIndexLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider,
			boolean systemIndexes) {
		super(catalogObject, connectionFilterProvider);

		this.systemIndexes = systemIndexes;
	}

	/**
	 * 
     * @param catalogObject the Table object upon which this loader operates.
	 * @param systemIndexes true: system indices, false: normal indices
	 */
	public FirebirdIndexLoader(ICatalogObject catalogObject,
			boolean systemIndexes) {
		super(catalogObject);

		this.systemIndexes = systemIndexes;
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
			stmt.setInt(2, systemIndexes ? 1 : 0);

			return stmt.executeQuery();
		} catch (RuntimeException e) {
			SQLException error = new SQLException(
			        Activator.getResourceString("error.index.loading")); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableIndexLoader#loadIndexes(java.util.List, java.util.Collection)
	 */
	public void loadIndexes(List containmentList, Collection existingIndexes)
			throws SQLException {
		ResultSet rs = null;
		try {
			initActiveFilter();
			Index index = null;
			for (rs = createResultSet(); rs.next();) {
				String indexName = rs.getString(COLUMN_INDEX_NAME);

				if (indexName != null)
					indexName = indexName.trim();

				if (indexName == null || isFiltered(indexName)) {
					continue;
				}

				if (index == null || !index.getName().equals(indexName)) {
					index = (Index) getAndRemoveSQLObject(existingIndexes,
							indexName);
					if (index == null) {
						index = createIndex();
						initIndex(index, rs);
					} else {
						initIndex(index, rs);
						index.getIncludedMembers().clear();
						if (index instanceof ICatalogObject) {
							((ICatalogObject) index).refresh();
						}
					}
					containmentList.add(index);
				}

				String columnName = rs.getString(COLUMN_COLUMN_NAME);
				if (columnName != null)
					columnName = columnName.trim();

				Column column = findColumn(columnName);
				if (column == null) {
					continue;
				}

				IndexMember im = createIndexMember();
				if (im == null) {
					continue;
				}

				initIndexMember(im, column, rs);
				index.getIncludedMembers().add(im);
			}
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableIndexLoader#initIndexMember(org.eclipse.datatools.modelbase.sql.constraints.IndexMember, org.eclipse.datatools.modelbase.sql.tables.Column, java.sql.ResultSet)
	 */
	protected void initIndexMember(IndexMember im, Column column, ResultSet rs)
			throws SQLException {
		im.setColumn(column);
		String ascOrDesc = rs.getString(COLUMN_ASC_OR_DESC);
		im.setIncrementType(getIncrementType(ascOrDesc != null ? ascOrDesc
				.trim() : null));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableIndexLoader#initIndex(org.eclipse.datatools.modelbase.sql.constraints.Index, java.sql.ResultSet)
	 */
	protected void initIndex(Index index, ResultSet rs) throws SQLException {
		String columnIndexName = rs.getString(COLUMN_INDEX_NAME);
		index.setName(columnIndexName != null ? columnIndexName.trim() : null);

		index.setUnique(!rs.getBoolean(COLUMN_NON_UNIQUE));
		index.setSchema(findSchema(null));
		index.setClustered(false);
	}
}
