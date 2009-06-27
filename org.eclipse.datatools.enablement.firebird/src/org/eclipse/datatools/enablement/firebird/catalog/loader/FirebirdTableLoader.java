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
			+ "    NULL AS table_cat, "
			+ "    NULL AS table_schem, "
			+ "    r.rdb$relation_name AS table_name, "
			+ "    'SYSTEM TABLE' AS table_type, "
			+ "    r.rdb$description AS remarks, "
			+ "    r.rdb$owner_name AS owner_name, "
			+ "    (SELECT count(*) FROM rdb$triggers t "
			+ "    WHERE t.rdb$relation_name = r.rdb$relation_name) AS trigger_count "
			+ "FROM " + "    rdb$relations r " + "WHERE "
			+ "    r.rdb$system_flag = 1";

	private static final String USER_TABLE_SELECT = ""
			+ "SELECT "
			+ "    NULL AS table_cat, "
			+ "    NULL AS table_schem, "
			+ "    r.rdb$relation_name AS table_name, "
			+ "    CASE "
			+ "        WHEN r.rdb$view_source IS NOT NULL THEN 'VIEW' "
			+ "        ELSE 'TABLE' "
			+ "    END AS table_type, "
			+ "    r.rdb$description AS remarks, "
			+ "    r.rdb$owner_name AS owner_name, "
			+ "    (SELECT count(*) FROM rdb$triggers t "
			+ "    WHERE t.rdb$relation_name = r.rdb$relation_name) AS trigger_count "
			+ "FROM " + "    rdb$relations r " + "WHERE "
			+ "    r.rdb$system_flag = 0";

	private boolean systemTables;

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

	protected void closeResultSet(ResultSet rs) {
		try {
			rs.getStatement().close();
		} catch (SQLException ex) {
			ex.printStackTrace();
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
		super.registerTableFactory(TYPE_TABLE, new TableFactory());
		super.registerTableFactory(TYPE_VIEW, new ViewFactory());
		super.registerTableFactory(TYPE_GLOBAL_TEMPORARY,
				new GlobalTempTableFactory());
	}

	public static class TableFactory extends JDBCTableLoader.TableFactory {
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

	public static class ViewFactory extends JDBCTableLoader.TableFactory {
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

	public static class GlobalTempTableFactory extends
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

	public static void loadTriggers(Connection connection, Schema schema,
			Table table, EList triggerList) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ").append(" ").append(
				"t.rdb$trigger_name trigger_name,").append(" ").append(
				"t.rdb$trigger_sequence trigger_seq,").append(" ").append(
				"t.rdb$trigger_type trigger_type,").append(" ").append(
				"t.rdb$trigger_inactive trigger_inactive,").append(" ").append(
				"t.rdb$system_flag system_flag ").append(" ").append(
				"FROM rdb$triggers t").append(" ").append(
				"WHERE t.rdb$relation_name = ?").append(" ").append(
				"AND t.rdb$system_flag = 0").append(" ").append("ORDER BY 2");

		PreparedStatement stmt = connection.prepareStatement(sb.toString());
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

	public static String loadViewQuery(Connection connection, Schema schema,
			ViewTable view) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ").append(" ")
				.append("r.rdb$view_source view_source").append(" ").append(
						"FROM rdb$relations r").append(" ").append(
						"WHERE r.rdb$relation_name = ?");

		PreparedStatement stmt = connection.prepareStatement(sb.toString());
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
