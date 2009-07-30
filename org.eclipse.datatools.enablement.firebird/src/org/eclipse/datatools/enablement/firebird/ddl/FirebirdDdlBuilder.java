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

package org.eclipse.datatools.enablement.firebird.ddl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.fe.GenericDdlBuilder;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdSchema;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdTrigger;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdUDF;
import org.eclipse.datatools.modelbase.sql.constraints.Index;
import org.eclipse.datatools.modelbase.sql.constraints.IndexMember;
import org.eclipse.datatools.modelbase.sql.constraints.ReferenceConstraint;
import org.eclipse.datatools.modelbase.sql.constraints.TableConstraint;
import org.eclipse.datatools.modelbase.sql.routines.Function;
import org.eclipse.datatools.modelbase.sql.routines.Parameter;
import org.eclipse.datatools.modelbase.sql.routines.Procedure;
import org.eclipse.datatools.modelbase.sql.routines.Routine;
import org.eclipse.datatools.modelbase.sql.routines.UserDefinedFunction;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.schema.Sequence;
import org.eclipse.datatools.modelbase.sql.schema.TypedElement;
import org.eclipse.datatools.modelbase.sql.tables.ActionTimeType;
import org.eclipse.datatools.modelbase.sql.tables.CheckType;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.datatools.modelbase.sql.tables.Trigger;
import org.eclipse.datatools.modelbase.sql.tables.ViewTable;
import org.eclipse.emf.common.util.EList;


/**
 * This class generates the actual sql statements for Firebird.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdDdlBuilder extends GenericDdlBuilder {

	protected static final String DECLARE = "DECLARE";
	protected static final String BY_VALUE = "BY VALUE";
	protected static final String PARAMETER = "PARAMETER";
	protected static final String BY_DESCRIPTOR = "BY DESCRIPTOR";
	protected static final String FREE_IT = "FREE_IT";
	protected static final String MODULE_NAME = "MODULE_NAME";
	protected static final String ENTRY_POINT = "ENTRY_POINT";
	protected static final String EXTERNAL = "EXTERNAL";
	protected static final String SEQUENCE = "SEQUENCE";
	protected final static String OR = "OR"; //$NON-NLS-1$
	protected final static String INACTIVE = "INACTIVE"; //$NON-NLS-1$
	protected final static String ACTIVE = "ACTIVE"; //$NON-NLS-1$
	protected final static String POSITION = "POSITION"; //$NON-NLS-1$

	public String createView(ViewTable view, boolean quoteIdentifiers,
			boolean qualifyNames) {
		String viewDefinition = CREATE + SPACE;
		viewDefinition += VIEW + SPACE
				+ getName(view, quoteIdentifiers, qualifyNames) + SPACE;

		String columns = getViewColumnList(view);
		if (columns != null) {
			viewDefinition += LEFT_PARENTHESIS + columns + RIGHT_PARENTHESIS
					+ SPACE;
		}
		viewDefinition += AS + NEWLINE;
		viewDefinition += view.getQueryExpression().getSQL();
		CheckType checkType = view.getCheckType();
		if (checkType == CheckType.CASCADED_LITERAL) {
			// do nothing here - WITH CHECK OPTION is part of the SQL source
			// code
			// viewDefinition += NEWLINE + WITH + SPACE + SPACE + CHECK + SPACE
			// + OPTION;
		} else if (checkType == CheckType.LOCAL_LITERAL) {
			// we don't support it
			throw new IllegalStateException();
		}
		return viewDefinition;
	}
	
	public String createTrigger(Trigger trigger,
			boolean quoteIdentifiers, boolean qualifyNames) {
		if (trigger instanceof FirebirdTrigger) {
			return createFirebirdTrigger((FirebirdTrigger)trigger, quoteIdentifiers, qualifyNames);
		} else {
			return super.createTrigger(trigger, quoteIdentifiers, qualifyNames);
		}
	}

	public String createFirebirdTrigger(FirebirdTrigger trigger,
			boolean quoteIdentifiers, boolean qualifyNames) {
		StringBuffer statement = new StringBuffer();
		statement.append(CREATE).append(SPACE).append(TRIGGER).append(SPACE);
		statement.append(getName(trigger, quoteIdentifiers, qualifyNames))
				.append(SPACE);
		statement.append(FOR).append(SPACE).append(
				getName(trigger.getSubjectTable(), quoteIdentifiers,
						qualifyNames)).append(SPACE);

		if (!trigger.isActive()) {
			statement.append(INACTIVE).append(SPACE);
		} else {
			statement.append(ACTIVE).append(SPACE);
		}
		
		statement.append(NEWLINE);

		final ActionTimeType actionTime = trigger.getActionTime();
		if (actionTime == ActionTimeType.AFTER_LITERAL) {
			statement.append(AFTER);
		} else if (actionTime == ActionTimeType.BEFORE_LITERAL) {
			statement.append(BEFORE);
		} else if (actionTime == ActionTimeType.INSTEADOF_LITERAL) {
			statement.append(INSTEAD_OF);
		}
		statement.append(SPACE);

		if (trigger.isDeleteType()) {
			statement.append(DELETE);

			if (trigger.isInsertType() || trigger.isUpdateType())
				statement.append(OR);
		}

		if (trigger.isInsertType()) {
			statement.append(INSERT);

			if (trigger.isUpdateType())
				statement.append(OR);
		}

		if (trigger.isUpdateType()) {
			statement.append(UPDATE);
		}

		statement.append(SPACE).append(POSITION).append(SPACE).append(
				trigger.getPosition()).append(SPACE);

		statement.append(NEWLINE);

		//Avoid hardcoded reference to Jaybird driver
		try {
			DatabaseMetaData dbmd = ((ICatalogObject)trigger.getSchema()).getConnection().getMetaData();
			Class metaClass = dbmd.getClass();
			Method getTriggerSourceCode = metaClass.getMethod("getTriggerSourceCode", new Class[] {String.class});
			Object result = getTriggerSourceCode.invoke(dbmd, new Object[] {trigger.getName()});
			statement.append((String)result);
		} catch (SQLException e) {
			statement.append("/* WARNING! ERROR while loading trigger body occured! */");
		} catch (SecurityException e) {
			statement.append("/* WARNING! ERROR while loading trigger body occured! (security exception) */");
		} catch (NoSuchMethodException e) {
			statement.append("/* WARNING! ERROR while loading trigger body occured! (connection not a FBConnection) */");
		} catch (IllegalArgumentException e) {
			statement.append("/* WARNING! ERROR while loading trigger body occured! */");
		} catch (IllegalAccessException e) {
			statement.append("/* WARNING! ERROR while loading trigger body occured! */");
		} catch (InvocationTargetException e) {
			statement.append("/* WARNING! ERROR while loading trigger body occured! */");
		}
		return statement.toString();
	}
	
	public String createProcedure(Procedure procedure, boolean quoteIdentifiers, boolean qualifyNames) {
	    if (procedure.getSchema() instanceof FirebirdSchema) {
			return createFirebirdProcedure(procedure, quoteIdentifiers, qualifyNames);
		} else {
			return super.createProcedure(procedure, quoteIdentifiers, qualifyNames);
		}
	}

	public String createFirebirdProcedure(Procedure procedure,
			boolean quoteIdentifiers, boolean qualifyNames) {
		StringBuffer statement = new StringBuffer();

		statement.append(CREATE).append(SPACE).append(PROCEDURE).append(SPACE);
		statement.append(getName(procedure, quoteIdentifiers, qualifyNames))
				.append(SPACE);

		// generate input parameters
		if (!procedure.getInputParameters().isEmpty()) {
			statement.append(LEFT_PARENTHESIS);
			appendProcedureParameterList(procedure.getSchema(), statement,
					procedure.getInputParameters());
			statement.append(RIGHT_PARENTHESIS);
		}
		statement.append(NEWLINE);

		if (!procedure.getOutputParameters().isEmpty()) {
			statement.append(RETURNS);
			// generate input parameters
			statement.append(LEFT_PARENTHESIS);
			appendProcedureParameterList(procedure.getSchema(), statement,
					procedure.getOutputParameters());
			statement.append(RIGHT_PARENTHESIS).append(NEWLINE);
		}
		statement.append(AS).append(NEWLINE);
		statement.append(procedure.getSource().getBody());

		return statement.toString();
	}

	protected void appendProcedureParameterList(Schema schema,
			StringBuffer statement, List params) {
		for (Iterator iter = params.iterator(); iter.hasNext();) {
			TypedElement element = (TypedElement) iter.next();
			if (element instanceof Column) {
				Column column = (Column) element;

				String name = getDoubleQuotedString(column.getName());
				String typeName = getDataTypeString(column, schema);

				statement.append(name).append(SPACE).append(typeName);
			} else if (element instanceof Parameter) {
				Parameter parameter = (Parameter) element;

				String name = getDoubleQuotedString(parameter.getName());
				String typeName = getDataTypeString(parameter, schema);

				statement.append(name).append(SPACE).append(typeName);
			}

			if (iter.hasNext())
				statement.append(COMMA);
		}
	}

	public String createUserDefinedFunction(UserDefinedFunction function, boolean quoteIdentifiers,
			boolean qualifyNames) {
		if(function instanceof FirebirdUDF) {
			return createFirebirdFunction((FirebirdUDF)function, quoteIdentifiers, qualifyNames);
		} else {
			return super.createUserDefinedFunction(function, quoteIdentifiers, qualifyNames);
		}
	}
	
	public String createFirebirdFunction(FirebirdUDF udf, boolean quoteIdentifiers,
			boolean qualifyNames) {
		StringBuffer statement = new StringBuffer();

		statement.append(DECLARE).append(SPACE).append(EXTERNAL).append(SPACE);
		statement.append(FUNCTION).append(SPACE);
		statement.append(getName(udf, quoteIdentifiers, qualifyNames)).append(
				SPACE);

		EList params = udf.getParameters();

		FirebirdUDF.Parameter outParam = (FirebirdUDF.Parameter) udf
				.getReturnScalar();
		int outMechanism = Math.abs(outParam.getMechanism());

		List inParams = new ArrayList();
		inParams.addAll(params);

		if (outMechanism == 2 && outParam.getArgumentPosition() != 0)
			inParams.add(outParam.getArgumentPosition() - 1, outParam);

		statement.append(NEWLINE);
		for (Iterator iter = inParams.iterator(); iter.hasNext();) {
			FirebirdUDF.Parameter param = (FirebirdUDF.Parameter) iter.next();

			// if (udf.getReturnArgument() == param.getArgumentPosition())
			// continue;

			String fullType = FirebirdConversionUtil.getFullTypeAsString(param
					.getFieldType(), param.getFieldSubType(), param
					.getFieldPrecision(), param.getFieldScale(), param
					.getCharLength(), param.getCharSetId());

			statement.append(TAB).append(fullType);

			// check whether NULL should be added
			int mechanism = Math.abs(param.getMechanism());
			switch (mechanism) {
			case 1:
				statement.append(SPACE).append(BY_VALUE);
				break;
			case 2:
				statement.append(SPACE).append(BY_DESCRIPTOR);
				break;
			case 5:
				statement.append(SPACE).append(NULL);
				break;
			default:
				// empty
			}

			if (iter.hasNext())
				statement.append(COMMA);

			statement.append(NEWLINE);
		}

		statement.append(RETURNS).append(SPACE);

		String outFullType = FirebirdConversionUtil.getFullTypeAsString(
				outParam.getFieldType(), outParam.getFieldSubType(), outParam
						.getFieldPrecision(), outParam.getFieldScale(),
				outParam.getCharLength(), outParam.getCharSetId());

		switch (outMechanism) {

		case 0:
			statement.append(outFullType).append(SPACE).append(BY_VALUE);
			break;

		case 2:
			if (outParam.getArgumentPosition() != 0)
				statement.append(PARAMETER).append(SPACE).append(
						outParam.getArgumentPosition());
			else
				statement.append(outFullType).append(SPACE).append(
						BY_DESCRIPTOR);
			break;

		case 1:
		case 3:
		case 4:
		case 5:
		default:
			statement.append(outFullType);
		}

		if (outParam.getMechanism() < 0)
			statement.append(SPACE).append(FREE_IT);

		statement.append(NEWLINE);

		statement.append(ENTRY_POINT).append(SPACE);
		statement.append("'").append(udf.getEntryPoint()).append("'").append(
				SPACE);

		statement.append(MODULE_NAME).append(SPACE);
		statement.append("'").append(udf.getModuleName()).append("'");

		return statement.toString();
	}

	public String dropFunction(UserDefinedFunction function, boolean quoteIdentifiers,
			boolean qualifyNames) {
		if (function instanceof FirebirdUDF) {
			return dropFirebirdFunction((FirebirdUDF)function, quoteIdentifiers, qualifyNames);
		} else {
			return super.dropFunction(function, quoteIdentifiers, qualifyNames);
		}
	}
	
	public String dropFirebirdFunction(FirebirdUDF function, boolean quoteIdentifiers,
			boolean qualifyNames) {
		StringBuffer result = new StringBuffer();

		result.append(DROP).append(SPACE).append(EXTERNAL).append(SPACE);
		result.append(FUNCTION).append(
				getName(function, quoteIdentifiers, qualifyNames));

		return result.toString();
	}
	
	public String dropSequence(Sequence o, boolean quoteIdentifiers,
			boolean qualifyNames) {
		StringBuffer result = new StringBuffer();

		result.append(DROP).append(SPACE).append(SEQUENCE).append(SPACE);
		result.append(getName(o, quoteIdentifiers, qualifyNames));

		return result.toString();
	}

	public String createSequence(Sequence o, boolean quoteIdentifiers,
			boolean qualifyNames) {
		StringBuffer result = new StringBuffer();

		result.append(CREATE).append(SPACE).append(SEQUENCE).append(SPACE);
		result.append(getName(o, quoteIdentifiers, qualifyNames));

		return result.toString();
	}

	protected String getName(Index index, boolean quoteIdentifiers,
			boolean qualifyNames) {
		quoteIdentifiers = !isUpperCase(index.getName());
		return super.getName(index, quoteIdentifiers, qualifyNames);
	}

	protected String getName(Routine routine, boolean quoteIdentifiers,
			boolean qualifyNames) {
		quoteIdentifiers = !isUpperCase(routine.getName());
		return super.getName(routine, quoteIdentifiers, qualifyNames);
	}

	protected String getName(Schema schema, boolean quoteIdentifiers,
			boolean qualifyNames) {
		quoteIdentifiers = !isUpperCase(schema.getName());
		return super.getName(schema, quoteIdentifiers, qualifyNames);
	}

	protected String getName(Sequence sequence, boolean quoteIdentifiers,
			boolean qualifyNames) {
		quoteIdentifiers = !isUpperCase(sequence.getName());
		return super.getName(sequence, quoteIdentifiers, qualifyNames);
	}

	protected String getName(Table table, boolean quoteIdentifiers,
			boolean qualifyNames) {
		quoteIdentifiers = !isUpperCase(table.getName());
		return super.getName(table, quoteIdentifiers, qualifyNames);
	}

	protected String getName(TableConstraint constraint,
			boolean quoteIdentifiers) {
		quoteIdentifiers = !isUpperCase(constraint.getName());
		return super.getName(constraint, quoteIdentifiers);
	}

	protected String getName(Trigger trigger, boolean quoteIdentifiers,
			boolean qualifyNames) {
		quoteIdentifiers = !isUpperCase(trigger.getName());
		return super.getName(trigger, quoteIdentifiers, qualifyNames);
	}

	protected String getParameterListClause(Routine routine,
			boolean quoteIdentifiers) {
		return super.getParameterListClause(routine, quoteIdentifiers);
	}

	protected String getParentKeyColumns(Index index, boolean quoteIdentifiers) {
		return super.getParentKeyColumns(index, quoteIdentifiers);
	}

	protected String getReturnsClause(Function function,
			boolean quoteIdentifiers) {
		return super.getReturnsClause(function, quoteIdentifiers);
	}

	protected String getColumnString(Column column, boolean quoteIdentifiers) {
		quoteIdentifiers = !isUpperCase(column.getName());
		return super.getColumnString(column, quoteIdentifiers);
	}

	protected String getName(Column column, boolean quoteIdentifiers) {
		quoteIdentifiers = !isUpperCase(column.getName());
		String columnName = column.getName();
		if (quoteIdentifiers) {
			columnName = this.getQuotedIdentifierString(column);
		}
		return columnName;
	}

	protected String getIndexKeyColumns(Index index, boolean quoteIdentifiers) {
		StringBuffer result = new StringBuffer();
		Iterator it = index.getIncludedMembers().iterator();
		while (it.hasNext()) {
			IndexMember m = (IndexMember) it.next();
			result.append(getName(m.getColumn(), true));
			result.append(SPACE);
			result.append(m.getIncrementType().getName());

			if (it.hasNext())
				result.append(COMMA).append(SPACE);
		}

		return result.length() != 0 ? result.toString() : null;
	}

	protected String getViewColumnList(ViewTable view) {
		StringBuffer result = new StringBuffer();
		Iterator it = view.getColumns().iterator();
		while (it.hasNext()) {
			Column c = (Column) it.next();
			result.append(getName(c, true));

			if (it.hasNext())
				result.append(COMMA).append(SPACE);
		}

		return result.length() != 0 ? result.toString() : null;
	}

	protected String getKeyColumns(ReferenceConstraint constraint,
			boolean quoteIdentifiers) {
		StringBuffer result = new StringBuffer();
		Iterator it = constraint.getMembers().iterator();
		while (it.hasNext()) {
			Column c = (Column) it.next();
			result.append(getName(c, true));

			if (it.hasNext())
				result.append(COMMA).append(SPACE);
		}

		return result.length() != 0 ? result.toString() : null;
	}

	protected boolean isUpperCase(String identifier) {
		char[] chars = identifier.toCharArray();
		boolean upperCase = true;
		for (int i = 0; i < chars.length; i++) {
			if (Character.isLetter(chars[i])
					&& !Character.isUpperCase(chars[i])) {
				upperCase = false;
				break;
			}
		}
		return upperCase;
	}

}
