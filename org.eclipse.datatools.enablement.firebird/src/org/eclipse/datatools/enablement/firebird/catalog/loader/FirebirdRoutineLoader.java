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

import org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionFilter;
import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader;
import org.eclipse.datatools.connectivity.sqm.loader.SchemaObjectFilterProvider;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdUDF;
import org.eclipse.datatools.modelbase.sql.routines.Routine;
import org.eclipse.datatools.modelbase.sql.routines.Source;
import org.eclipse.datatools.modelbase.sql.routines.impl.SQLRoutinesFactoryImpl;

/**
 * Routine loader for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdRoutineLoader extends JDBCRoutineLoader {

	public static final String COLUMN_ENTRY_POINT = "routine_entry_point";
	public static final String COLUMN_MODULE_NAME = "routine_module_name";
	public static final String COLUMN_RETURN_ARGUMENT = "routine_return_argument";

	private final boolean systemSchema;

	/**
	 * Constructs the routine loader.
	 * 
	 * @param catalogObject the Schema object upon which this loader operates.
	 * @param connectionFilterProviderthe filter provider used for filtering
     *        the "routine" objects being loaded
	 * @param systemSchema true: load system objects, false: load user objects
	 */
	public FirebirdRoutineLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider,
			boolean systemSchema) {
		super(catalogObject, connectionFilterProvider);

		setUserDefinedFunctionFactory(new FirebirdUDFFactory());
		setProcedureFactory(new FirebirdProcedureFactory());

		this.systemSchema = systemSchema;
	}

	/**
     * Constructs the routine loader without filter.
     * 
     * @param catalogObject the Schema object upon which this loader operates.
     * @param systemSchema true: load system objects, false: load user objects
     */
	public FirebirdRoutineLoader(ICatalogObject catalogObject,
			boolean systemSchema) {
		this(catalogObject, new SchemaObjectFilterProvider(
				ConnectionFilter.STORED_PROCEDURE_FILTER), systemSchema);
	}

	private static final String LOAD_ALL_ROUTINES_SQL = 
		      "SELECT"
			+ " cast('PROC' AS VARCHAR(4)) AS routine_type,"
			+ " p.rdb$procedure_name as procedure_name,"
			+ " p.rdb$description as routine_description,"
			+ " p.rdb$procedure_source as routine_source,"
			+ " cast(null as varchar(31)) as routine_module_name,"
			+ " cast(null as varchar(31)) as routine_entry_point,"
			+ " cast(null as integer) as routine_return_argument,"
			+ " cast(null as varchar(31)) as remarks " 
			+ "FROM rdb$procedures p "
			+ "WHERE p.rdb$system_flag = ? "
			+ "UNION SELECT"
			+ " cast('FUNC' AS VARCHAR(4)) AS routine_type,"
			+ " f.rdb$function_name as procedure_name,"
			+ " f.rdb$description as routine_description,"
			+ " cast(null as blob sub_type text) as routine_source,"
			+ " f.rdb$module_name as routine_module_name,"
			+ " f.rdb$entrypoint as routine_entry_point,"
			+ " f.rdb$return_argument as routine_return_argument,"
			+ " cast(null as varchar(31)) as remarks "
			+ "FROM rdb$functions f " 
			+ "WHERE f.rdb$system_flag = ? ";

	private static final String PROCEDURE_ID = "PROC";
	private static final String UDF_ID = "FUNC";
	private static final String LOAD_FILTER_ROUTINES_SQL = 
		      "SELECT"
			+ " cast('" + PROCEDURE_ID + "' AS VARCHAR(4)) AS routine_type,"
			+ " p.rdb$procedure_name as procedure_name,"
			+ " p.rdb$procedure_source as routine_source,"
			+ " cast(null as varchar(31)) as routine_module_name,"
			+ " cast(null as varchar(31)) as routine_entry_point,"
			+ " cast(null as integer) as routine_return_argument,"
			+ " cast(null as varchar(31)) as remarks "
			+ "FROM rdb$procedures p "
			+ "WHERE p.rdb$system_flag = ?"
			+ " AND p.rdb$procedure_name LIKE ? " 
			+ "UNION SELECT"
			+ " cast('" + UDF_ID + "' AS VARCHAR(4)) AS routine_type,"
			+ " f.rdb$function_name as procedure_name,"
			+ " cast(null as blob sub_type text) as routine_source,"
			+ " f.rdb$module_name as routine_module_name,"
			+ " f.rdb$entrypoint as routine_entry_point,"
			+ " f.rdb$return_argument as routine_return_argument,"
			+ " cast(null as varchar(31)) as remarks "
			+ "FROM rdb$functions f " 
			+ "WHERE f.rdb$system_flag = ?"
			+ " AND f.rdb$function_name LIKE ?";

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

			String filterPattern = getJDBCFilterPattern();
			PreparedStatement stmt = connection
					.prepareStatement(filterPattern == null ? LOAD_ALL_ROUTINES_SQL
							: LOAD_FILTER_ROUTINES_SQL);
			final int useSystemSchema = systemSchema ? 1 : 0;
			stmt.setInt(1, useSystemSchema);
			if (filterPattern == null) {
				stmt.setInt(2, useSystemSchema);
			} else {
				stmt.setString(2, filterPattern);
				stmt.setInt(3, useSystemSchema);
				stmt.setString(4, filterPattern);
			}

			return stmt.executeQuery();
		} catch (RuntimeException e) {
			SQLException error = new SQLException(
			        Activator.getResourceString("error.routine.loading")); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	public static final String COLUMN_ROUTINE_TYPE = "routine_type";
	public static final String COLUMN_ROUTINE_NAME = "procedure_name";
	public static final String COLUMN_ROUTINE_DESCRIPTION = "routine_description";
	public static final String COLUMN_ROUTINE_SOURCE = "routine_source";
	public static final String COLUMN_ROUTINE_MODULE_NAME = "routine_module_name";
	public static final String COLUMN_ROUTINE_ENTRY_POINT = "routine_entry_point";

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader#isProcedure(java.sql.ResultSet)
	 */
	protected boolean isProcedure(ResultSet rs) throws SQLException {
		return PROCEDURE_ID.equals(rs.getString(COLUMN_ROUTINE_TYPE));
	}

	protected Routine processRow(ResultSet rs) throws SQLException {
		String routineName = rs.getString(COLUMN_ROUTINE_NAME);
		if (routineName == null || isFiltered(routineName)) {
			return null;
		}
		routineName = routineName.trim();

		IRoutineFactory routineFactory = (isProcedure(rs) ? getProcedureFactory()
				: getUserDefinedFunctionFactory());

		return routineFactory.createRoutine(rs);
	}

	/**
	 * Factory implementation for stored procedures.
	 */
	public class FirebirdProcedureFactory extends ProcedureFactory {

		/**
		 * Initializes the new Routine object using the meta-data in the result
		 * set. This method initializes the name and description of the
		 * procedure.
		 * 
		 * @param routine
		 *            a new Routine object
		 * @param rs
		 *            the result set
		 * @throws SQLException
		 *             if anything goes wrong
		 */
		public void initialize(Routine routine, ResultSet rs)
				throws SQLException {
			
			Routine fbProc = routine;

			fbProc.setName(rs.getString(COLUMN_ROUTINE_NAME).trim());
			fbProc.setDescription(rs.getString(COLUMN_ROUTINE_DESCRIPTION));
			Source source = SQLRoutinesFactoryImpl.eINSTANCE.createSource();
			source.setBody(rs.getString(COLUMN_ROUTINE_SOURCE));
			fbProc.setSource(source);
		}
	}

	/**
	 * Factory implementation for User Defined Functions (UDF).
	 *
	 */
	public class FirebirdUDFFactory extends UserDefinedFunctionFactory {

		protected Routine newRoutine() {
			return new FirebirdUDF();
		}

		/**
		 * Initializes the new Routine object using the meta-data in the result
		 * set. This method initializes the name and description of the
		 * procedure.
		 * 
		 * @param routine
		 *            a new Routine object
		 * @param rs
		 *            the result set
		 * @throws SQLException
		 *             if anything goes wrong
		 */
		public void initialize(Routine routine, ResultSet rs)
				throws SQLException {

			FirebirdUDF fbUDF = (FirebirdUDF)routine;
			
			fbUDF.setName(rs.getString(COLUMN_ROUTINE_NAME).trim());
			fbUDF.setDescription(rs.getString(COLUMN_ROUTINE_DESCRIPTION));
			fbUDF.setEntryPoint(rs.getString(COLUMN_ENTRY_POINT).trim());
			fbUDF.setModuleName(rs.getString(COLUMN_MODULE_NAME).trim());
			fbUDF.setReturnArgument(rs.getInt(COLUMN_RETURN_ARGUMENT));
		}
	}
}
