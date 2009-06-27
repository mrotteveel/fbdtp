package org.eclipse.datatools.enablement.firebird.catalog.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableConstraintLoader;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdCheckConstraint;
import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.constraints.PrimaryKey;
import org.eclipse.datatools.modelbase.sql.constraints.UniqueConstraint;
import org.eclipse.datatools.modelbase.sql.schema.ReferentialActionType;
import org.eclipse.datatools.modelbase.sql.tables.Table;

/**
 * 
 * @author Roman Rokytskyy
 * 
 */
public class FirebirdConstraintLoader extends JDBCTableConstraintLoader {

// TODO Remove or find usage
//	private boolean showSystemObjects;

	public FirebirdConstraintLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider,
			boolean showSystemObjects) {

		super(catalogObject, connectionFilterProvider);

//		this.showSystemObjects = showSystemObjects;
	}

	public FirebirdConstraintLoader(ICatalogObject catalogObject,
			boolean showSystemObjects) {

		super(catalogObject);

//		this.showSystemObjects = showSystemObjects;
	}

	protected void closeResultSet(ResultSet rs) {
		try {
			rs.getStatement().close();
		} catch (SQLException e) {
		}
	}

	private static final String GET_IMPORTED_KEYS_START = "select"
			+ " null as PKTABLE_CAT " + " ,null as PKTABLE_SCHEM "
			+ " ,PK.RDB$RELATION_NAME as PKTABLE_NAME "
			+ " ,ISP.RDB$FIELD_NAME as PKCOLUMN_NAME "
			+ " ,null as FKTABLE_CAT " + " ,null as FKTABLE_SCHEM "
			+ " ,FK.RDB$RELATION_NAME as FKTABLE_NAME "
			+ " ,ISF.RDB$FIELD_NAME as FKCOLUMN_NAME "
			+ " ,CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ "
			+ " ,RC.RDB$UPDATE_RULE as UPDATE_RULE "
			+ " ,RC.RDB$DELETE_RULE as DELETE_RULE "
			+ " ,PK.RDB$CONSTRAINT_NAME as PK_NAME "
			+ " ,FK.RDB$CONSTRAINT_NAME as FK_NAME "
			+ " ,null as DEFERRABILITY " + " from "
			+ " RDB$RELATION_CONSTRAINTS PK "
			+ " ,RDB$RELATION_CONSTRAINTS FK " + " ,RDB$REF_CONSTRAINTS RC "
			+ " ,RDB$INDEX_SEGMENTS ISP " + " ,RDB$INDEX_SEGMENTS ISF "
			+ " WHERE " + "  FK.RDB$RELATION_NAME = ? " + "AND "
			+ " FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME "
			+ " and PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ "
			+ " and ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
			+ " and ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
			+ " and ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
			+ " order by 3, 9 ";

	protected ResultSet createForeignKeyResultSet() throws SQLException {
		try {
			Table table = getTable();
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_IMPORTED_KEYS_START);
			stmt.setString(1, table.getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
			//FIXME Fix message
			SQLException error = new SQLException(/*MessageFormat.format(
					Messages.Error_Unsupported_DatabaseMetaData_Method,
					new Object[] { "FirebirdMetaData.$getPrimaryKeys()" })*/); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	public void loadForeignKeys(List containmentList, Collection existingFKs)
			throws SQLException {
		ResultSet rs = null;
		try {
			Map constraints = new HashMap();
			Map constraintColumns = new HashMap();
			for (rs = createForeignKeyResultSet(); rs.next();) {
				String fkName = rs.getString(COLUMN_FK_NAME);

				if (fkName != null)
					fkName = fkName.trim();

				if (!constraints.containsKey(fkName)) {
					ForeignKey fk = (ForeignKey) getAndRemoveSQLObject(
							existingFKs, fkName);
					if (fk == null) {
						// create the next FK
						fk = createForeignKey();
						fk.setName(fkName);
					} else {
						fk.getMembers().clear();
						if (fk instanceof ICatalogObject) {
							((ICatalogObject) fk).refresh();
						}
					}
					containmentList.add(fk);

					String updateRule = rs.getString(COLUMN_UPDATE_RULE);
					if (updateRule != null)
						updateRule = updateRule.trim();

					if ("CASCADE".equals(updateRule))
						fk.setOnUpdate(ReferentialActionType.CASCADE_LITERAL);
					else if ("RESTRICT".equals(updateRule))
						fk.setOnUpdate(ReferentialActionType.RESTRICT_LITERAL);
					else if ("SET DEFAULT".equals(updateRule))
						fk
								.setOnUpdate(ReferentialActionType.SET_DEFAULT_LITERAL);
					else if ("SET NULL".equals(updateRule))
						fk.setOnUpdate(ReferentialActionType.SET_NULL_LITERAL);
					else if ("NO ACTION".equals(updateRule))
						fk.setOnUpdate(ReferentialActionType.NO_ACTION_LITERAL);
					else
						fk.setOnUpdate(ReferentialActionType.NO_ACTION_LITERAL);

					String deleteRule = rs.getString(COLUMN_DELETE_RULE);
					if ("CASCADE".equals(deleteRule))
						fk.setOnDelete(ReferentialActionType.CASCADE_LITERAL);
					else if ("RESTRICT".equals(deleteRule))
						fk.setOnDelete(ReferentialActionType.RESTRICT_LITERAL);
					else if ("SET DEFAULT".equals(deleteRule))
						fk
								.setOnDelete(ReferentialActionType.SET_DEFAULT_LITERAL);
					else if ("SET NULL".equals(deleteRule))
						fk.setOnDelete(ReferentialActionType.SET_NULL_LITERAL);
					else if ("NO ACTION".equals(deleteRule))
						fk.setOnDelete(ReferentialActionType.NO_ACTION_LITERAL);
					else
						fk.setOnDelete(ReferentialActionType.NO_ACTION_LITERAL);

					fk.setDeferrable(false);

					fk.setUniqueConstraint(findUniqueConstraint(rs
							.getString(COLUMN_PKTABLE_CAT), rs
							.getString(COLUMN_PKTABLE_SCHEM), rs.getString(
							COLUMN_PKTABLE_NAME).trim(), rs.getString(
							COLUMN_PK_NAME).trim()));

					constraints.put(fkName, fk);
					constraintColumns.put(fkName, new TreeMap());

				}
				((Map) constraintColumns.get(fkName)).put(new Integer(rs
						.getShort(COLUMN_KEY_SEQ)), findColumn(rs.getString(
						COLUMN_FKCOLUMN_NAME).trim()));
			}
			for (Iterator it = constraints.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				ForeignKey fk = (ForeignKey) entry.getValue();
				for (Iterator colIt = ((Map) constraintColumns
						.get(fk.getName())).values().iterator(); colIt
						.hasNext();) {
					fk.getMembers().add(colIt.next());
				}
				initReferenceAnnotation(fk);
			}
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}

	private static final String GET_PRIMARY_KEYS_START = ""
			+ "SELECT "
			+ "  null as TABLE_CAT, "
			+ "  null as TABLE_SCHEM, "
			+ "  RC.RDB$RELATION_NAME as TABLE_NAME, "
			+ "  ISGMT.RDB$FIELD_NAME as COLUMN_NAME, "
			+ "  CAST ((ISGMT.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ, "
			+ "  RC.RDB$CONSTRAINT_NAME as PK_NAME " + "FROM "
			+ "  RDB$RELATION_CONSTRAINTS RC, " + "  RDB$INDEX_SEGMENTS ISGMT "
			+ "WHERE " + "  RC.RDB$RELATION_NAME = ? " + "AND "
			+ "  RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME " + "AND "
			+ "  RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' " + "ORDER BY "
			+ "  ISGMT.RDB$FIELD_NAME ";

	protected ResultSet createPrimaryKeyResultSet() throws SQLException {
		try {
			Table table = getTable();
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_PRIMARY_KEYS_START);
			stmt.setString(1, table.getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
			//FIXME Fix message
			SQLException error = new SQLException(/*MessageFormat.format(
					Messages.Error_Unsupported_DatabaseMetaData_Method,
					new Object[] { "FirebirdMetaData.$getPrimaryKeys()" })*/); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	public PrimaryKey loadPrimaryKey(PrimaryKey existingPK) throws SQLException {
		ResultSet rs = null;
		try {
			Map columns = new TreeMap();
			PrimaryKey pk = null;
			for (rs = createPrimaryKeyResultSet(); rs.next();) {
				if (pk == null) {
					String pkName = rs.getString(COLUMN_PK_NAME);
					if (pkName == null) {
						return null;
					}

					pkName = pkName.trim();

					if (existingPK != null
							&& pkName.equals(existingPK.getName())) {
						pk = existingPK;
						pk.getMembers().clear();
						if (existingPK instanceof ICatalogObject) {
							((ICatalogObject) pk).refresh();
						}
					} else {
						pk = createPrimaryKey();
						pk.setName(pkName);
					}
				}
				columns.put(new Integer(rs.getShort(COLUMN_KEY_SEQ)),
						findColumn(rs.getString(COLUMN_COLUMN_NAME).trim()));
			}
			for (Iterator it = columns.values().iterator(); it.hasNext();) {
				pk.getMembers().add(it.next());
			}
			return pk;
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}

	private static final String GET_EXPORTED_KEYS_START = ""
			+ "SELECT "
			// +"  null as PKTABLE_CAT "
			// +" ,null as PKTABLE_SCHEM "
			+ " PK.RDB$RELATION_NAME as PKTABLE_NAME "
			+ " ,ISP.RDB$FIELD_NAME as PKCOLUMN_NAME "
			// +" ,null as FKTABLE_CAT "
			// +" ,null as FKTABLE_SCHEM "
			+ " ,FK.RDB$RELATION_NAME as FKTABLE_NAME "
			+ " ,ISF.RDB$FIELD_NAME as FKCOLUMN_NAME "
			+ " ,CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ "
			+ " ,RC.RDB$UPDATE_RULE as UPDATE_RULE "
			+ " ,RC.RDB$DELETE_RULE as DELETE_RULE "
			+ " ,PK.RDB$CONSTRAINT_NAME as PK_NAME "
			+ " ,FK.RDB$CONSTRAINT_NAME as FK_NAME "
			// +" ,null as DEFERRABILITY "
			+ "FROM " + "  RDB$RELATION_CONSTRAINTS PK "
			+ " ,RDB$RELATION_CONSTRAINTS FK " + " ,RDB$REF_CONSTRAINTS RC "
			+ " ,RDB$INDEX_SEGMENTS ISP " + " ,RDB$INDEX_SEGMENTS ISF "
			+ "WHERE " + "  PK.RDB$RELATION_NAME = ? " + "AND "
			+ "  FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME " + "AND "
			+ "  PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ " + "AND "
			+ "  ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME " + "AND "
			+ "  ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME " + "AND "
			+ "  ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
			+ "ORDER BY " + "  7, 9 ";

	protected ResultSet createUniqueConstraintResultSet() throws SQLException {
		try {
			Table table = getTable();
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_EXPORTED_KEYS_START);
			stmt.setString(1, table.getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
			//FIXME Fix message
			SQLException error = new SQLException(/*
					MessageFormat
							.format(
									Messages.Error_Unsupported_DatabaseMetaData_Method,
									new Object[] { "FirebirdMetaData.$getUniqueConstraints()" })*/); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	public void loadUniqueConstraints(PrimaryKey pk, List containmentList,
			Collection existingUCs) throws SQLException {
		ResultSet rs = null;
		if (pk != null) {
			// Remove this guy from the list.
			existingUCs.remove(pk);
		}
		try {
			Map constraints = new HashMap();
			Map constraintColumns = new HashMap();
			for (rs = createUniqueConstraintResultSet(); rs.next();) {
				String ucName = rs.getString(COLUMN_PK_NAME);

				if (ucName != null)
					ucName = ucName.trim();

				if (ucName.equals(pk == null ? null : pk.getName())) {
					// Already seen this guy
					continue;
				} else if (!constraints.containsKey(ucName)) {
					UniqueConstraint uc = (UniqueConstraint) getAndRemoveSQLObject(
							existingUCs, ucName);
					if (uc == null) {
						// create the next UC
						uc = createUniqueConstraint();
						uc.setName(ucName);
					} else {
						uc.getMembers().clear();
						if (uc instanceof ICatalogObject) {
							((ICatalogObject) uc).refresh();
						}
					}
					containmentList.add(uc);
					constraints.put(ucName, uc);
					constraintColumns.put(ucName, new TreeMap());
				}
				((Map) constraintColumns.get(ucName)).put(new Integer(rs
						.getShort(COLUMN_KEY_SEQ)), findColumn(rs.getString(
						COLUMN_PKCOLUMN_NAME).trim()));
			}
			for (Iterator it = constraints.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				UniqueConstraint uc = (UniqueConstraint) entry.getValue();
				for (Iterator colIt = ((Map) constraintColumns
						.get(uc.getName())).values().iterator(); colIt
						.hasNext();) {
					uc.getMembers().add(colIt.next());
				}
			}
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}

	private static final String CHECK_CONSTRAINT_NAME = "CONSTRAINT_NAME";
	private static final String CHECK_CONSTRAINT_SOURCE = "CONSTRAINT_SOURCE";

	private static final String GET_CHECK_CONSTRAINTS = "" + "SELECT "
			+ "  rc.rdb$constraint_name AS " + CHECK_CONSTRAINT_NAME + " "
			+ ", tr.rdb$trigger_source AS " + CHECK_CONSTRAINT_SOURCE + " "
			+ "FROM " + "  rdb$triggers tr " + "INNER JOIN "
			+ "  rdb$check_constraints cc " + "ON "
			+ "  cc.rdb$trigger_name = tr.rdb$trigger_name " + "INNER JOIN "
			+ "  rdb$relation_constraints rc " + "ON "
			+ "  cc.rdb$constraint_name = rc.rdb$constraint_name " + "WHERE "
			+ "  rc.rdb$constraint_type = 'CHECK' " + "AND "
			+ "  rc.rdb$relation_name = ?";

	protected ResultSet createCheckConstraintResultSet() throws SQLException {
		try {
			Table table = getTable();
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_CHECK_CONSTRAINTS);
			stmt.setString(1, table.getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
			// FIXME Fix message
			SQLException error = new SQLException(/*MessageFormat.format(
					Messages.Error_Unsupported_DatabaseMetaData_Method,
					new Object[] { "FirebirdMetaData.$getCheckConstraints()" })*/); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/**
	 * Loads the "check constraint" objects from the database. This method uses
	 * the result set from createCheckConstraintResultSet() to load the "check
	 * constraint" objects from the server.
	 * 
	 * @param containmentList
	 *            the containment list held by parent
	 * @param existingUCs
	 *            the catalog objects which were previously loaded
	 * 
	 * @throws SQLException
	 *             if an error occurred during loading.
	 */
	public void loadCheckConstraints(List containmentList,
			Collection existingUCs) throws SQLException {
		ResultSet rs = null;
		try {
			Map constraints = new HashMap();
			for (rs = createCheckConstraintResultSet(); rs.next();) {
				String ucName = rs.getString(CHECK_CONSTRAINT_NAME);

				if (ucName != null)
					ucName = ucName.trim();

				if (!constraints.containsKey(ucName)) {
					FirebirdCheckConstraint uc = (FirebirdCheckConstraint) getAndRemoveSQLObject(
							existingUCs, ucName);

					if (uc == null) {
						// create the next UC
						uc = createCheckConstraint();
						uc.setName(ucName);
					}

					String sourceSql = rs.getString(CHECK_CONSTRAINT_SOURCE);
					uc.setSQL(sourceSql);

					containmentList.add(uc);
					constraints.put(ucName, uc);
				}
			}
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}

	protected FirebirdCheckConstraint createCheckConstraint() {
		return new FirebirdCheckConstraint();
	}
}
