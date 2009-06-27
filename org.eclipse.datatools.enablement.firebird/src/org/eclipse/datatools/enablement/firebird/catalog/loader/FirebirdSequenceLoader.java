package org.eclipse.datatools.enablement.firebird.catalog.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionFilter;
import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCBaseLoader;
import org.eclipse.datatools.connectivity.sqm.loader.SchemaObjectFilterProvider;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdSequence;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.schema.Sequence;

/**
 * 
 * @author Roman Rokytskyy
 * 
 */
public class FirebirdSequenceLoader extends JDBCBaseLoader {

	private static final String SEQUENCE_NAME = "SEQUENCE_NAME";

	private boolean systemSequences;

	public FirebirdSequenceLoader(ICatalogObject catalogObject,
			boolean systemSequences) {
		super(catalogObject, new SchemaObjectFilterProvider(
				ConnectionFilter.SEQUENCE_FILTER));

		this.systemSequences = systemSequences;
	}

	/**
	 * Utility method.
	 * 
	 * @return returns the catalog object being operated upon as a Schema (i.e.
	 *         (Schema) getCatalogObject()).
	 */
	protected Schema getSchema() {
		return (Schema) getCatalogObject();
	}

	/**
	 * Creates a result set to be used by the loading logic. The default version
	 * uses of the JDBC DatabaseMetaData.getUDTs() to create the result set.
	 * This method may be overridden to use a vendor specific query. However,
	 * the default logic requires the columns named by the "COLUMN_*" fields.
	 * Keep this in mind if you plan to reuse the default logic (e.g.
	 * StructTypeFactory.initialize())
	 * 
	 * @return a result containing the information used to initialize Routine
	 *         objects
	 * 
	 * @throws SQLException
	 *             if an error occurs
	 */
	protected ResultSet createResultSet() throws SQLException {
		try {
			Connection connection = getCatalogObject().getConnection();
			Statement stmt = connection.createStatement();

			String query;

			String filterPattern = getJDBCFilterPattern();
			if (filterPattern == null)
				query = "SELECT rdb$generator_name AS " + SEQUENCE_NAME + " "
						+ "FROM rdb$generators " + "WHERE rdb$system_flag = "
						+ (systemSequences ? '1' : '0');
			else
				query = "SELECT rdb$generator_name AS " + SEQUENCE_NAME + " "
						+ "FROM rdb$generators " + "WHERE rdb$system_flag =  "
						+ (systemSequences ? '1' : '0')
						+ "AND rdb$generator_name LIKE '" + filterPattern + "'";

			return stmt.executeQuery(query);
		} catch (RuntimeException e) {
			// FIXME correct errormessage
			SQLException error = new SQLException(/*
												 * MessageFormat.format(
												 * Messages.
												 * Error_Unsupported_DatabaseMetaData_Method
												 * , new Object[] {
												 * "java.sql.DatabaseMetaData.getUDTs()"
												 * })
												 */); //$NON-NLS-1$
			error.initCause(e);
			throw error;
		}
	}

	/**
	 * Loads the "Sequence" objects from the database. This method uses the result
	 * set from createResultSet() to load the "Sequence" objects from the server.
	 * This method first checks the name of the "Sequence" to determine whether or
	 * not it should be filtered. If it is not filtered, it checks to see if an
	 * object with that name was loaded previously. If it finds an existing
	 * object, it refreshes that object and adds it to the containment list. If
	 * the named object does not exist, the result set is passed to
	 * processRow(). UserDefinedType objects are created and initialized using
	 * one of the registered factories.
	 * 
	 * This method should only be overridden as a last resort when the desired
	 * behavior cannot be acheived by overriding createResultSet(),
	 * closeResultSet(), processRow(), and a specialized Struct, Distinct and
	 * Java type factories.
	 * 
	 * @param existingSequences
	 *            the catalog objects which were previously loaded
	 * @param containmentList
	 *            the containment list held by parent
	 * @throws SQLException
	 *             if an error occurred during loading.
	 */
	public void loadSequences(List containmentList, Collection existingSequences)
			throws SQLException {
		ResultSet rs = null;
		try {
			initActiveFilter();
			for (rs = createResultSet(); rs.next();) {
				String sequenceName = rs.getString(SEQUENCE_NAME);
				if (sequenceName == null || isFiltered(sequenceName.trim())) {
					continue;
				}
				Sequence sequence = (Sequence) getAndRemoveSQLObject(
						existingSequences, sequenceName);

				sequence = processRow(rs);
				if (sequence != null) {
					containmentList.add(sequence);
				}
			}
		} finally {
			if (rs != null) {
				closeResultSet(rs);
			}
		}
	}

	/**
	 * Closes the result set used for catalog object loading. This method is
	 * implemented as rs.close(). However, if you used a Statement object to
	 * create the result set, this is where you would close that Statement.
	 * 
	 * @param rs
	 *            the result set to close. This will be the result set created
	 *            by createResultSet().
	 */
	protected void closeResultSet(ResultSet rs) {
		try {
			rs.getStatement().close();
		} catch (SQLException e) {
		}
	}

	/**
	 * Processes a single row in the result set. By default, this method
	 * determines whether or not the UDT is a struct, distinct or Java type and
	 * invokes createUDT() on the appropriate factory., returning the newly
	 * created, initialized UserDefinedType object.
	 * 
	 * @param rs
	 *            the result set
	 * @return a new UserDefinedType object
	 * @throws SQLException
	 *             if anything goes wrong
	 */
	protected Sequence processRow(ResultSet rs) throws SQLException {
		String name = rs.getString(SEQUENCE_NAME).trim();

		FirebirdSequence sequence = new FirebirdSequence();
		sequence.setName(name);

		return sequence;
	}

	/**
	 * Removes the specified Sequences from the model.
	 * 
	 * @param existingSequences
	 *            the Sequences to be removed from the model.
	 */
	public void clearSequences(List existingSequences) {
		existingSequences.clear();
	}
}
