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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableConstraintLoader;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.modelbase.sql.constraints.CheckConstraint;
import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.constraints.PrimaryKey;
import org.eclipse.datatools.modelbase.sql.constraints.SQLConstraintsFactory;
import org.eclipse.datatools.modelbase.sql.constraints.UniqueConstraint;
import org.eclipse.datatools.modelbase.sql.expressions.SQLExpressionsFactory;
import org.eclipse.datatools.modelbase.sql.expressions.SearchCondition;
import org.eclipse.datatools.modelbase.sql.schema.ReferentialActionType;

/**
 * Constraint loader for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdConstraintLoader extends JDBCTableConstraintLoader {
    
    private static final Map CASCADE_TYPE_MAP;
    static {
        Map cascadeMap = new HashMap();
        cascadeMap.put("CASCADE", ReferentialActionType.CASCADE_LITERAL);
        cascadeMap.put("RESTRICT", ReferentialActionType.RESTRICT_LITERAL);
        cascadeMap.put("SET DEFAULT", ReferentialActionType.SET_DEFAULT_LITERAL);
        cascadeMap.put("SET NULL", ReferentialActionType.SET_NULL_LITERAL);
        cascadeMap.put("NO ACTION", ReferentialActionType.NO_ACTION_LITERAL);
        CASCADE_TYPE_MAP = Collections.unmodifiableMap(cascadeMap);
    }

    /**
     * Constructs the constraint loader with a filter.
     * 
     * @param catalogObject the Catalog object upon which this loader operates.
     * @param connectionFilterProvider the filter provider used for filtering 
     *          the "constraint" objects being loaded.
     */
	public FirebirdConstraintLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider) {

		super(catalogObject, connectionFilterProvider);
	}

	/**
	 * Constructs the constraint loader using no filter.
	 * 
	 * @param catalogObject the Catalog object upon which this loader operates.
	 */
	public FirebirdConstraintLoader(ICatalogObject catalogObject) {
		super(catalogObject);
	}

	private static final String GET_IMPORTED_KEYS = 
	          "SELECT"
			+ " null as PKTABLE_CAT," 
			+ " null as PKTABLE_SCHEM,"
			+ " PK.RDB$RELATION_NAME as PKTABLE_NAME,"
			+ " ISP.RDB$FIELD_NAME as PKCOLUMN_NAME,"
			+ " null as FKTABLE_CAT,"
			+ " null as FKTABLE_SCHEM,"
			+ " FK.RDB$RELATION_NAME as FKTABLE_NAME,"
			+ " ISF.RDB$FIELD_NAME as FKCOLUMN_NAME,"
			+ " CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ,"
			+ " RC.RDB$UPDATE_RULE as UPDATE_RULE,"
			+ " RC.RDB$DELETE_RULE as DELETE_RULE,"
			+ " PK.RDB$CONSTRAINT_NAME as PK_NAME,"
			+ " FK.RDB$CONSTRAINT_NAME as FK_NAME,"
			+ " null as DEFERRABILITY "
			+ "FROM"
			+ " RDB$RELATION_CONSTRAINTS PK,"
			+ " RDB$RELATION_CONSTRAINTS FK,"
			+ " RDB$REF_CONSTRAINTS RC,"
			+ " RDB$INDEX_SEGMENTS ISP,"
			+ " RDB$INDEX_SEGMENTS ISF "
			+ "WHERE"
			+ " FK.RDB$RELATION_NAME = ?"
			+ " AND FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME"
			+ " AND PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ"
			+ " AND ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME "
			+ " AND ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME "
			+ " AND ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
			+ " ORDER BY 3, 9";

	/**
	 * Creates a result set to be used by the foreign key constraint loading logic.
	 * 
     * @return a result containing the information used to initialize ForeignKey
     *         objects
     * 
     * @throws SQLException if an error occurs
	 */
	protected ResultSet createForeignKeyResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_IMPORTED_KEYS);
			stmt.setString(1, getTable().getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
			SQLException error = new SQLException(MessageFormat.format(
                    Activator.getResourceString("error.constraint.loading"),
					new Object[] { 
                        Activator.getResourceString("constraint.foreign") })); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableConstraintLoader#loadForeignKeys(java.util.List, java.util.Collection)
	 */
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

					fk.setOnUpdate(getCascadeAction(rs.getString(COLUMN_UPDATE_RULE)));
					fk.setOnDelete(getCascadeAction(rs.getString(COLUMN_DELETE_RULE)));

					fk.setDeferrable(false);

					fk.setUniqueConstraint(findUniqueConstraint(
					        rs.getString(COLUMN_PKTABLE_CAT),
					        rs.getString(COLUMN_PKTABLE_SCHEM),
					        rs.getString(COLUMN_PKTABLE_NAME).trim(),
					        rs.getString(COLUMN_PK_NAME).trim()));
					constraints.put(fkName, fk);
					constraintColumns.put(fkName, new TreeMap());
				}
				((Map) constraintColumns.get(fkName)).put(
				        Integer.valueOf(rs.getShort(COLUMN_KEY_SEQ)),
				        findColumn(rs.getString(COLUMN_FKCOLUMN_NAME).trim()));
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
	
	/**
	 * Resolve tha ReferentialActionType based on the actionName.
	 * 
	 * @param actionName Name of the action
	 * @return Resolved ReferentialActionType (defaults to 
	 *             ReferentialActionType.NO_ACTION_LITERAL if unresolvable)
	 */
	private ReferentialActionType getCascadeAction(String actionName) {
	    if(actionName != null) {
	        actionName = actionName.trim();
	    }
	    ReferentialActionType actionType = (ReferentialActionType)CASCADE_TYPE_MAP.get(actionName);
	    if(actionType == null) {
	        actionType = ReferentialActionType.NO_ACTION_LITERAL;
	    }
	    return actionType;
	}

	private static final String GET_PRIMARY_KEYS = 
	          "SELECT "
			+ " null as TABLE_CAT,"
			+ " null as TABLE_SCHEM,"
			+ " RC.RDB$RELATION_NAME as TABLE_NAME,"
			+ " ISGMT.RDB$FIELD_NAME as COLUMN_NAME,"
			+ " CAST ((ISGMT.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ,"
			+ " RC.RDB$CONSTRAINT_NAME as PK_NAME "
			+ "FROM"
			+ " RDB$RELATION_CONSTRAINTS RC,"
			+ " RDB$INDEX_SEGMENTS ISGMT "
			+ "WHERE"
			+ " RC.RDB$RELATION_NAME = ?"
			+ " AND RC.RDB$INDEX_NAME = ISGMT.RDB$INDEX_NAME"
			+ " AND RC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' "
			+ "ORDER BY ISGMT.RDB$FIELD_NAME";

	/**
     * Creates a result set to be used by the primary key loading logic.
     * 
     * @return a result containing the information used to initialize PrimaryKey
     * 
     * @throws SQLException if an error occurs
     */
	protected ResultSet createPrimaryKeyResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_PRIMARY_KEYS);
			stmt.setString(1, getTable().getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
            SQLException error = new SQLException(MessageFormat.format(
                    Activator.getResourceString("error.constraint.loading"),
                    new Object[] { 
                        Activator.getResourceString("constraint.primary") })); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableConstraintLoader#loadPrimaryKey(org.eclipse.datatools.modelbase.sql.constraints.PrimaryKey)
	 */
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
				columns.put(Integer.valueOf(rs.getShort(COLUMN_KEY_SEQ)),
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

	private static final String GET_EXPORTED_KEYS = 
	          "SELECT "
			// +"  null as PKTABLE_CAT "
			// +" ,null as PKTABLE_SCHEM "
			+ " PK.RDB$RELATION_NAME as PKTABLE_NAME,"
			+ " ISP.RDB$FIELD_NAME as PKCOLUMN_NAME,"
			// +" ,null as FKTABLE_CAT "
			// +" ,null as FKTABLE_SCHEM "
			+ " FK.RDB$RELATION_NAME as FKTABLE_NAME,"
			+ " ISF.RDB$FIELD_NAME as FKCOLUMN_NAME,"
			+ " CAST ((ISP.RDB$FIELD_POSITION + 1) as SMALLINT) as KEY_SEQ,"
			+ " RC.RDB$UPDATE_RULE as UPDATE_RULE,"
			+ " RC.RDB$DELETE_RULE as DELETE_RULE,"
			+ " PK.RDB$CONSTRAINT_NAME as PK_NAME,"
			+ " FK.RDB$CONSTRAINT_NAME as FK_NAME "
			// +" ,null as DEFERRABILITY "
			+ "FROM "
			+ " RDB$RELATION_CONSTRAINTS PK,"
			+ " RDB$RELATION_CONSTRAINTS FK,"
			+ " RDB$REF_CONSTRAINTS RC,"
			+ " RDB$INDEX_SEGMENTS ISP,"
			+ " RDB$INDEX_SEGMENTS ISF "
			+ "WHERE"
			+ "  PK.RDB$RELATION_NAME = ?"
			+ " AND FK.RDB$CONSTRAINT_NAME = RC.RDB$CONSTRAINT_NAME"
			+ " AND PK.RDB$CONSTRAINT_NAME = RC.RDB$CONST_NAME_UQ"
			+ " AND ISP.RDB$INDEX_NAME = PK.RDB$INDEX_NAME"
			+ " AND ISF.RDB$INDEX_NAME = FK.RDB$INDEX_NAME"
			+ " AND ISP.RDB$FIELD_POSITION = ISF.RDB$FIELD_POSITION "
			+ "ORDER BY 7, 9";

	/**
     * Creates a result set to be used by the unique constraint loading logic.
     * 
     * @return a result containing the information used to initialize
     *         UniqueConstraint objects
     * 
     * @throws SQLException if an error occurs
     */
	protected ResultSet createUniqueConstraintResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_EXPORTED_KEYS);
			stmt.setString(1, getTable().getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
            SQLException error = new SQLException(MessageFormat.format(
                    Activator.getResourceString("error.constraint.loading"),
                    new Object[] { 
                        Activator.getResourceString("constraint.unique") })); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCTableConstraintLoader#loadUniqueConstraints(org.eclipse.datatools.modelbase.sql.constraints.PrimaryKey, java.util.List, java.util.Collection)
	 */
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
				((Map) constraintColumns.get(ucName)).put(Integer.valueOf(rs
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

	private static final String GET_CHECK_CONSTRAINTS = 
	          "SELECT"
			+ " rc.rdb$constraint_name AS " + CHECK_CONSTRAINT_NAME + ","
			+ " tr.rdb$trigger_source AS " + CHECK_CONSTRAINT_SOURCE + " "
			+ "FROM"
			+ " rdb$triggers tr "
			+ "INNER JOIN rdb$check_constraints cc" 
			+ " ON cc.rdb$trigger_name = tr.rdb$trigger_name "
			+ "INNER JOIN rdb$relation_constraints rc"
			+ " ON cc.rdb$constraint_name = rc.rdb$constraint_name "
			+ "WHERE"
			+ " rc.rdb$constraint_type = 'CHECK'"
			+ " AND rc.rdb$relation_name = ?";

	/**
     * Creates a result set to be used by the check constraint loading logic.
     * 
     * @return a result containing the information used to initialize CheckConstraint
     *         objects
     * 
     * @throws SQLException if an error occurs
     */
	protected ResultSet createCheckConstraintResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();

			PreparedStatement stmt = connection
					.prepareStatement(GET_CHECK_CONSTRAINTS);
			stmt.setString(1, getTable().getName());

			return stmt.executeQuery();

		} catch (RuntimeException e) {
            SQLException error = new SQLException(MessageFormat.format(
                    Activator.getResourceString("error.constraint.loading"),
                    new Object[] { 
                        Activator.getResourceString("constraint.check") })); //$NON-NLS-1$
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
	 * @param existingCheckConstraints
	 *            the catalog objects which were previously loaded
	 * 
	 * @throws SQLException
	 *             if an error occurred during loading.
	 */
	public void loadCheckConstraints(List containmentList,
			Collection existingCheckConstraints) throws SQLException {
		ResultSet rs = null;
		try {
			Map constraints = new HashMap();
			for (rs = createCheckConstraintResultSet(); rs.next();) {
				String constraintName = rs.getString(CHECK_CONSTRAINT_NAME);

				if (constraintName != null)
					constraintName = constraintName.trim();

				if (!constraints.containsKey(constraintName)) {
					CheckConstraint checkConstraint = (CheckConstraint) getAndRemoveSQLObject(
							existingCheckConstraints, constraintName);

					if (checkConstraint == null) {
						// create the next check constraint
						checkConstraint = SQLConstraintsFactory.eINSTANCE.createCheckConstraint();
						checkConstraint.setName(constraintName);
					}

					SearchCondition searchCondition = SQLExpressionsFactory.eINSTANCE.createSearchConditionDefault();
					searchCondition.setSQL(rs.getString(CHECK_CONSTRAINT_SOURCE));
					checkConstraint.setSearchCondition(searchCondition);

					containmentList.add(checkConstraint);
					constraints.put(constraintName, checkConstraint);
				}
			}
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}
}
