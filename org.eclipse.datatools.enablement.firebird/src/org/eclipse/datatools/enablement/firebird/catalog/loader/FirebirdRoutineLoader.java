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
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdProcedure;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdSchema;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdUDF;
import org.eclipse.datatools.modelbase.sql.routines.Routine;
import org.eclipse.datatools.modelbase.sql.routines.SQLRoutinesPackage;
import org.eclipse.emf.ecore.EClass;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdRoutineLoader extends JDBCRoutineLoader {

	public static final String COLUMN_ENTRY_POINT = "routine_entry_point";
	public static final String COLUMN_MODULE_NAME = "routine_module_name";
	public static final String COLUMN_RETURN_ARGUMENT = "routine_return_argument";

	private boolean systemSchema;

	public FirebirdRoutineLoader(ICatalogObject catalogObject,
			IConnectionFilterProvider connectionFilterProvider,
			boolean systemSchema) {
		super(catalogObject, connectionFilterProvider);

		setUserDefinedFunctionFactory(new FirebirdUDFFactory());
		setProcedureFactory(new FirebirdProcedureFactory());

		this.systemSchema = systemSchema;
	}

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

	private static final String LOAD_FILTER_ROUTINES_SQL = 
		      "SELECT"
			+ " cast('PROC' AS VARCHAR(4)) AS routine_type,"
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
			+ " cast('FUNC' AS VARCHAR(4)) AS routine_type,"
			+ " f.rdb$function_name as procedure_name,"
			+ " cast(null as blob sub_type text) as routine_source,"
			+ " f.rdb$module_name as routine_module_name,"
			+ " f.rdb$entrypoint as routine_entry_point,"
			+ " f.rdb$return_argument as routine_return_argument,"
			+ " cast(null as varchar(31)) as remarks "
			+ "FROM rdb$functions f " 
			+ "WHERE f.rdb$system_flag = ?"
			+ " AND f.rdb$function_name LIKE ?";

	protected ResultSet createResultSet() throws SQLException {
		try {
			String filterPattern = getJDBCFilterPattern();

			Connection connection = getCatalogObject().getConnection();

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
			//FIXME Fix message format
			SQLException error = new SQLException(/*
					MessageFormat
							.format(
									Messages.Error_Unsupported_DatabaseMetaData_Method,
									new Object[] { "java.sql.DatabaseMetaData.getProcedures()" })*/); //$NON-NLS-1$

			error.initCause(e);
			throw error;
		}
	}

	protected void closeResultSet(ResultSet rs) {
		try {
			rs.getStatement().close();
		} catch (SQLException e) {
		}
	}

	public static final String COLUMN_ROUTINE_TYPE = "routine_type";
	public static final String COLUMN_ROUTINE_NAME = "procedure_name";
	public static final String COLUMN_ROUTINE_DESCRIPTION = "routine_description";
	public static final String COLUMN_ROUTINE_SOURCE = "routine_source";
	public static final String COLUMN_ROUTINE_MODULE_NAME = "routine_module_name";
	public static final String COLUMN_ROUTINE_ENTRY_POINT = "routine_entry_point";

	protected boolean isProcedure(ResultSet rs) throws SQLException {
		return "PROC".equals(rs.getString(COLUMN_ROUTINE_TYPE));
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
	 * Base Factory for creating routines.
	 *
	 */
	protected abstract class FBRoutineFactory implements IRoutineFactory {
		/**
		 * Creates and initializes a new Procedure object from the meta-data in
		 * the result set.
		 * 
		 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader.IRoutineFactory#createRoutine(java.sql.ResultSet)
		 */
		public Routine createRoutine(ResultSet rs) throws SQLException {
			Routine routine = newRoutine();
			initialize(routine, rs);
			// routine.setSchema(schema);
			return routine;
		}
		
		/**
		 * Internal factory method.
		 * 
		 * @return a new Routine object
		 */
		protected abstract Routine newRoutine();
	}

	/**
	 * Factory implementation for stored procedures.
	 */
	public class FirebirdProcedureFactory extends FBRoutineFactory {

		/**
		 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader.IRoutineFactory#getRoutineEClass()
		 * 
		 * @return SQLRoutinesPackage.eINSTANCE.getProcedure()
		 */
		public EClass getRoutineEClass() {
			return SQLRoutinesPackage.eINSTANCE.getProcedure();
		}


		protected Routine newRoutine() {
			return new FirebirdProcedure((FirebirdSchema)FirebirdRoutineLoader.this.getSchema());
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
			
			FirebirdProcedure fbProc = (FirebirdProcedure)routine;

			fbProc.setName(rs.getString(COLUMN_ROUTINE_NAME).trim());
			fbProc.setDescription(rs.getString(COLUMN_ROUTINE_DESCRIPTION));
			fbProc.setSourceCode(rs.getString(COLUMN_ROUTINE_SOURCE));

			// routine.getParameters();
		}
	}

	/**
	 * Factory implementation for User Defined Functions (UDF).
	 *
	 */
	public class FirebirdUDFFactory extends FBRoutineFactory {
		/**
		 * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineLoader.IRoutineFactory#getRoutineEClass()
		 * 
		 * @return SQLRoutinesPackage.eINSTANCE.getProcedure()
		 */
		public EClass getRoutineEClass() {
			return SQLRoutinesPackage.eINSTANCE.getUserDefinedFunction();
		}

		protected Routine newRoutine() {
			return new FirebirdUDF(FirebirdRoutineLoader.this.getSchema());
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

			// routine.getParameters();
		}
	}
}
