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

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCTemporaryTable;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdTable;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdTrigger;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdView;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.tables.CheckType;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.datatools.modelbase.sql.tables.ViewTable;
import org.eclipse.emf.common.util.EList;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdTableLoader extends JDBCTableLoader {

	private static final String COLUMN_TRIGGER_COUNT = "TRIGGER_COUNT";

// TODO Remove or find usage?
//	private static final String[] USER_TABLE_TYPES = { TYPE_TABLE, TYPE_VIEW,
//			TYPE_GLOBAL_TEMPORARY };
//
//	private static final String[] SYSTEM_TABLE_TYPES = { TYPE_SYSTEM_TABLE };

	private static final String SYSTEM_TABLE_SELECT = ""
			+ "SELECT "
			+ " NULL AS table_cat,"
			+ " NULL AS table_schem,"
			+ " r.rdb$relation_name AS table_name,"
			+ " 'SYSTEM TABLE' AS table_type,"
			+ " r.rdb$description AS remarks,"
			+ " r.rdb$owner_name AS owner_name,"
			+ " (SELECT count(*) FROM rdb$triggers t"
			+ "  WHERE t.rdb$relation_name = r.rdb$relation_name) AS trigger_count "
			+ "FROM"
			+ " rdb$relations r "
			+ "WHERE"
			+ " r.rdb$system_flag = 1";

	private static final String USER_TABLE_SELECT = ""
			+ "SELECT "
			+ " NULL AS table_cat,"
			+ " NULL AS table_schem,"
			+ " r.rdb$relation_name AS table_name,"
			+ " CASE"
			+ "  WHEN r.rdb$view_source IS NOT NULL THEN 'VIEW'"
			+ "  ELSE 'TABLE'"
			+ " END AS table_type,"
			+ " r.rdb$description AS remarks,"
			+ " r.rdb$owner_name AS owner_name,"
			+ " (SELECT count(*) FROM rdb$triggers t"
			+ "  WHERE t.rdb$relation_name = r.rdb$relation_name) AS trigger_count "
			+ "FROM"
			+ " rdb$relations r "
			+ "WHERE"
			+ " r.rdb$system_flag = 0";

	private final boolean systemTables;

	public FirebirdTableLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider,
			boolean systemTables) {

		super(catalogObject, connectionFilterProvider);

		this.systemTables = systemTables;

		replaceTableFactories();
	}

	public FirebirdTableLoader(ICatalogObject catalogObject,
			boolean systemTables) {
		super(catalogObject);

		this.systemTables = systemTables;

		replaceTableFactories();
	}

	protected ResultSet createResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();
			return connection.createStatement().executeQuery(
					systemTables ? SYSTEM_TABLE_SELECT : USER_TABLE_SELECT);
		} catch (RuntimeException e) {
			SQLException error = new SQLException(/*
												 * MessageFormat.format(
												 * Messages.
												 * Error_Unsupported_DatabaseMetaData_Method
												 * , new Object[] {
												 * "java.sql.DatabaseMetaData.getTables()"
												 * })
												 */); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	protected Table processRow(ResultSet rs) throws SQLException {
		String tableName = rs.getString(COLUMN_TABLE_NAME);
		if (tableName == null || isFiltered(tableName)) {
			return null;
		}

		tableName = tableName.trim();

		String tableType = rs.getString(COLUMN_TABLE_TYPE).trim();
		ITableFactory tableFactory = (ITableFactory) getTableFactory(tableType);
		Table table = tableFactory.createTable(rs);
		return table;
	}

	protected void replaceTableFactories() {
		super.registerTableFactory(TYPE_TABLE, new FBTableFactory());
		super.registerTableFactory(TYPE_VIEW, new FBViewFactory());
		super.registerTableFactory(TYPE_GLOBAL_TEMPORARY,
				new FBGlobalTempTableFactory());
	}

	public static class FBTableFactory extends JDBCTableLoader.TableFactory {
		protected Table newTable() {
			return new FirebirdTable();
		}

		public Table createTable(ResultSet rs) throws SQLException {
			Table result = super.createTable(rs);
			result.setName(result.getName() != null ? result.getName().trim()
					: null);
			return result;
		}

	}

	public static class FBViewFactory extends JDBCTableLoader.TableFactory {
		protected Table newTable() {
			return new FirebirdView();
		}

		public Table createTable(ResultSet rs) throws SQLException {
			ViewTable result = (ViewTable) super.createTable(rs);

			result.setName(result.getName() != null ? result.getName().trim()
					: null);

			int triggerCount = rs.getInt(COLUMN_TRIGGER_COUNT);

			if (triggerCount > 0)
				result.setCheckType(CheckType.CASCADED_LITERAL);
			else
				result.setCheckType(CheckType.NONE_LITERAL);

			return result;
		}

	}

	public static class FBGlobalTempTableFactory extends
			JDBCTableLoader.TableFactory {
		protected Table newTable() {
			return new JDBCTemporaryTable();
		}

		public Table createTable(ResultSet rs) throws SQLException {
			Table result = super.createTable(rs);
			result.setName(result.getName() != null ? result.getName().trim()
					: null);
			return result;
		}
	}
	
	private static final String TRIGGER_SELECT =
	      "SELECT"
	    + " t.rdb$trigger_name trigger_name,"
	    + " t.rdb$trigger_sequence trigger_seq,"
	    + " t.rdb$trigger_type trigger_type,"
	    + " t.rdb$trigger_inactive trigger_inactive,"
	    + " t.rdb$system_flag system_flag "
	    + "FROM"
	    + " rdb$triggers t "
	    + "WHERE"
	    + " t.rdb$relation_name = ?"
	    + " AND t.rdb$system_flag = 0 "
	    + " ORDER BY 2";

	public static void loadTriggers(Connection connection, Schema schema,
			Table table, EList triggerList) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(TRIGGER_SELECT);
		try {
			stmt.setString(1, table.getName());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				FirebirdTrigger trigger = new FirebirdTrigger();

				trigger.setName(rs.getString("TRIGGER_NAME").trim());
				trigger.setPosition(rs.getInt("TRIGGER_SEQ"));

				trigger.setActive(rs.getObject("TRIGGER_INACTIVE") != null
						&& rs.getInt("TRIGGER_INACTIVE") == 0);

				trigger.setFirebirdTriggerType(rs.getInt("TRIGGER_TYPE"));
				trigger.setSchema(schema);
				triggerList.add(trigger);
			}
		} finally {
			stmt.close();
		}
	}
	
	private static final String VIEW_QUERY_SELECT =
	      "SELECT"
	    + " r.rdb$view_source view_source "
	    + "FROM"
	    + " rdb$relations r "
	    + "WHERE"
	    + " r.rdb$relation_name = ?";

	public static String loadViewQuery(Connection connection, Schema schema,
			ViewTable view) throws SQLException {

	    PreparedStatement stmt = connection.prepareStatement(VIEW_QUERY_SELECT);
		try {
			stmt.setString(1, view.getName());
			ResultSet rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException();

			String query = rs.getString(1);

			if (rs.next())
				throw new SQLException();
			return query;
		} finally {
			stmt.close();
		}
	}
}
