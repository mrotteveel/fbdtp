/*
 * Copyright (C) 2007 - 2010 members of the Firebird development team
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
import java.text.MessageFormat;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCTemporaryTable;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader;
import org.eclipse.datatools.enablement.firebird.Activator;
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

	private static final String SYSTEM_TABLE_SELECT = 
	          "SELECT "
			+ " NULL AS table_cat,"
			+ " NULL AS table_schem,"
			+ " r.rdb$relation_name AS table_name,"
			+ " '" + TYPE_SYSTEM_TABLE + "' AS table_type,"
			+ " r.rdb$description AS remarks,"
			+ " r.rdb$owner_name AS owner_name,"
			+ " (SELECT count(*) FROM rdb$triggers t"
			+ "  WHERE t.rdb$relation_name = r.rdb$relation_name) AS trigger_count "
			+ "FROM"
			+ " rdb$relations r "
			+ "WHERE"
			+ " r.rdb$system_flag = 1";

	// TODO Still valid with existence of GTT in 2.1 and higher?
	private static final String USER_TABLE_SELECT = 
	          "SELECT "
			+ " NULL AS table_cat,"
			+ " NULL AS table_schem,"
			+ " r.rdb$relation_name AS table_name,"
			+ " CASE"
			+ "  WHEN r.rdb$view_source IS NOT NULL THEN '" + TYPE_VIEW + "'"
			+ "  ELSE '" + TYPE_TABLE + "'"
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

	/**
     * @param catalogObject the Catalog object upon which this loader operates.
     * @param connectionFilterProvider the filter provider used for filtering
     *        the "schema" objects being loaded
     * @param systemTables true: load system objects, false: load user objects
     */
	public FirebirdTableLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider,
			boolean systemTables) {

		super(catalogObject, connectionFilterProvider);

		this.systemTables = systemTables;

		replaceTableFactories();
	}

	/**
     * @param catalogObject the Catalog object upon which this loader operates.
     * @param systemTables true: load system objects, false: load user objects
     */
	public FirebirdTableLoader(ICatalogObject catalogObject,
			boolean systemTables) {
		super(catalogObject);

		this.systemTables = systemTables;

		replaceTableFactories();
	}

	/**
     * Creates a result set to be used by the loading logic.
     * 
     * @return a result containing the information used to initialize Routine
     *         objects
     * 
     * @throws SQLException if an error occurs
     */
	protected ResultSet createResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();
			return connection.createStatement().executeQuery(
					systemTables ? SYSTEM_TABLE_SELECT : USER_TABLE_SELECT);
		} catch (RuntimeException e) {
			SQLException error = new SQLException(
			        Activator.getResourceString("error.table.loading")); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader#processRow(java.sql.ResultSet)
	 */
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

	/**
	 * Replaces the default table factories with Firebird specific implementations
	 */
	protected void replaceTableFactories() {
		super.registerTableFactory(TYPE_TABLE, new FBTableFactory());
		super.registerTableFactory(TYPE_VIEW, new FBViewFactory());
		super.registerTableFactory(TYPE_GLOBAL_TEMPORARY,
				new FBGlobalTempTableFactory());
	}

	/**
	 * TableFactory implementation for normal tables in Firebird.
	 * 
	 * @author Roman Rokytskyy
	 * @author Mark Rotteveel
	 *
	 */
	public class FBTableFactory extends JDBCTableLoader.TableFactory {
	    
	    /*
	     * (non-Javadoc)
	     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader.TableFactory#newTable()
	     */
		protected Table newTable() {
			return new FirebirdTable(systemTables);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader.TableFactory#createTable(java.sql.ResultSet)
		 */
		public Table createTable(ResultSet rs) throws SQLException {
			Table result = super.createTable(rs);
			result.setName(result.getName() != null ? result.getName().trim()
					: null);
			return result;
		}
	}

	/**
	 * TableFactory implementation for views in Firebird.
	 * 
	 * @author Roman Rokytskyy
	 * @author Mark Rotteveel
	 *
	 */
	public class FBViewFactory extends FBTableFactory {
	    
	    /*
	     * (non-Javadoc)
	     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader.TableFactory#newTable()
	     */
		protected Table newTable() {
			return new FirebirdView();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader.TableFactory#createTable(java.sql.ResultSet)
		 */
		public Table createTable(ResultSet rs) throws SQLException {
			ViewTable result = (ViewTable) super.createTable(rs);

			int triggerCount = rs.getInt(COLUMN_TRIGGER_COUNT);

			if (triggerCount > 0)
				result.setCheckType(CheckType.CASCADED_LITERAL);
			else
				result.setCheckType(CheckType.NONE_LITERAL);

			return result;
		}

	}

	/**
	 * TableFactory implementation for GTT in Firebird.
	 * 
	 * @author Roman Rokytskyy
	 * @author Mark Rotteveel
	 *
	 */
	public class FBGlobalTempTableFactory extends FBTableFactory {
	    
	    /*
	     * (non-Javadoc)
	     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableLoader.TableFactory#newTable()
	     */
		protected Table newTable() {
			return new JDBCTemporaryTable();
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

	/**
	 * Load the triggers for the give table.
	 * 
	 * @param connection Connection to use for retrieving triggers
	 * @param schema Schema object
	 * @param table Table object
	 * @param triggerList List of triggers
	 * @throws SQLException if anything goes wrong
	 */
	public static void loadTriggers(Connection connection, Schema schema,
			Table table, EList triggerList) throws SQLException {
	    // TODO Verify if we need to empty or update the triggerList
		PreparedStatement stmt = connection.prepareStatement(TRIGGER_SELECT);
		try {
			stmt.setString(1, table.getName());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				FirebirdTrigger trigger = new FirebirdTrigger();

				trigger.setName(rs.getString("TRIGGER_NAME").trim());
				trigger.setPosition(rs.getInt("TRIGGER_SEQ"));
				trigger.setActive(rs.getInt("TRIGGER_INACTIVE") == 0
				        && !rs.wasNull());
				trigger.setFirebirdTriggerType(rs.getInt("TRIGGER_TYPE"));
				trigger.setSchema(schema);
				triggerList.add(trigger);
			}
		} finally {
		    try {
		        stmt.close();
		    } catch (SQLException ex) {}
		}
	}
	
	private static final String VIEW_QUERY_SELECT =
	      "SELECT"
	    + " r.rdb$view_source view_source "
	    + "FROM"
	    + " rdb$relations r "
	    + "WHERE"
	    + " r.rdb$relation_name = ?";

	/**
	 * Load the source of the query backing the view.
	 * 
	 * @param connection Connection to use for retrieving the source.
	 * @param schema Schema object
	 * @param view View object
	 * @return Source of the backing query.
	 * @throws SQLException if anything goes wrong.
	 */
	public static String loadViewQuery(Connection connection, Schema schema,
			ViewTable view) throws SQLException {

	    PreparedStatement stmt = connection.prepareStatement(VIEW_QUERY_SELECT);
		try {
			stmt.setString(1, view.getName());
			ResultSet rs = stmt.executeQuery();
			if (!rs.next())
				throw new SQLException(
				        MessageFormat.format(
				                Activator.getResourceString("error.view.noquery"),
				                new Object[] { view.getName() }));

			String query = rs.getString(1);
			if (rs.next())
			    throw new SQLException(
                        MessageFormat.format(
                                Activator.getResourceString("error.view.multiplequery"),
                                new Object[] { view.getName() }));
			return query;
		} finally {
	         try {
	             stmt.close();
	         } catch (SQLException ex) {}
		}
	}
}
