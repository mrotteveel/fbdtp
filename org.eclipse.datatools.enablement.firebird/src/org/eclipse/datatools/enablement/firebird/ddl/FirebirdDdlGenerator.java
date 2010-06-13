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

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.datatools.connectivity.sqm.core.definition.EngineeringOptionID;
import org.eclipse.datatools.connectivity.sqm.core.rte.EngineeringOption;
import org.eclipse.datatools.connectivity.sqm.core.rte.IEngineeringCallBack;
import org.eclipse.datatools.connectivity.sqm.core.rte.fe.GenericDdlGenerator;
import org.eclipse.datatools.connectivity.sqm.internal.core.rte.EngineeringOptionCategory;
import org.eclipse.datatools.modelbase.sql.constraints.CheckConstraint;
import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.constraints.Index;
import org.eclipse.datatools.modelbase.sql.constraints.UniqueConstraint;
import org.eclipse.datatools.modelbase.sql.datatypes.UserDefinedType;
import org.eclipse.datatools.modelbase.sql.routines.Procedure;
import org.eclipse.datatools.modelbase.sql.routines.UserDefinedFunction;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.SQLObject;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.schema.Sequence;
import org.eclipse.datatools.modelbase.sql.tables.BaseTable;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.eclipse.datatools.modelbase.sql.tables.Trigger;
import org.eclipse.datatools.modelbase.sql.tables.ViewTable;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdDdlGenerator extends GenericDdlGenerator {
	private final FirebirdDdlBuilder builder;

	public FirebirdDdlGenerator() {
		this.builder = new FirebirdDdlBuilder();

		// set the builder to the superclass
		setDdlBuilder(builder);
	}

	protected String[] createStatements(SQLObject[] elements,
			boolean quoteIdentifiers, boolean qualifyNames,
			IProgressMonitor progressMonitor, int task) {

		// ensure that no schema will be added to the names
		qualifyNames = false;

		FirebirdDdlScript script = new FirebirdDdlScript();
		EngineeringOption[] options = this.getSelectedOptions(elements);

		Iterator it = this.getAllContainedDisplayableElementSet(elements)
				.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof PersistentTable) {
				if (!this.generateTables(options))
					continue;
				String statement = builder.createTable((BaseTable) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateTableStatement(statement);
			} else if (o instanceof ViewTable) {
				if (!this.generateViews(options))
					continue;
				String statement = builder.createView((ViewTable) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateViewStatement(statement);
			} else if (o instanceof Trigger) {
				if (!this.generateTriggers(options))
					continue;
				String statement = builder.createTrigger((Trigger) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateTriggerStatement(statement);
			} else if (o instanceof CheckConstraint) {
				if (!this.generateCKConstraints(options))
					continue;
				String statement = builder.addCheckConstraint(
						(CheckConstraint) o, quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addAlterTableAddConstraintStatement(statement);
			} else if (o instanceof UniqueConstraint) {
				if (!this.generatePKConstraints(options))
					continue;
				String statement = builder.addUniqueConstraint(
						(UniqueConstraint) o, quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addAlterTableAddConstraintStatement(statement);
			} else if (o instanceof ForeignKey) {
				if (!this.generateFKConstraints(options))
					continue;
				String statement = builder.addForeignKey((ForeignKey) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addAlterTableAddForeignKeyStatement(statement);
			} else if (o instanceof Index) {
				if (!this.generateIndexes(options))
					continue;
				String statement = builder.createIndex((Index) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateIndexStatement(statement);
			} else if (o instanceof Procedure) {
				if (!this.generateStoredProcedures(options))
					continue;
				String statement = builder.createProcedure((Procedure) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateProcedureStatement(statement);
			} else if (o instanceof UserDefinedFunction) {
				if (!this.generateFunctions(options))
					continue;
				String statement = builder
						.createUserDefinedFunction((UserDefinedFunction) o,
								quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateFunctionStatement(statement);
			} else if (o instanceof Sequence) {
				if (!this.generateSequences(options))
					continue;
				String statement = builder.createSequence(
						(Sequence) o, quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addCreateSequence(statement);
			}
		}
		return script.getStatements();
	}

	protected String[] dropStatements(SQLObject[] elements,
			boolean quoteIdentifiers, boolean qualifyNames,
			IProgressMonitor progressMonitor, int task) {

		// ensure that no schema will be added to the names
		qualifyNames = false;

		FirebirdDdlScript script = new FirebirdDdlScript();

		EngineeringOption[] options = this.getSelectedOptions(elements);

		Iterator it = this.getAllContainedDisplayableElementSet(elements)
				.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof PersistentTable) {
				if (!this.generateTables(options))
					continue;
				String statement = builder.dropTable((PersistentTable) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addDropTableStatement(statement);
			} else if (o instanceof ViewTable) {
				if (!this.generateViews(options))
					continue;
				String statement = builder.dropView((ViewTable) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addDropViewStatement(statement);
			} else if (o instanceof Trigger) {
				if (!this.generateTriggers(options))
					continue;
				String statement = builder.dropTrigger((Trigger) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addDropTriggerStatement(statement);
			} else if (o instanceof CheckConstraint) {
				if (!this.generateCKConstraints(options))
					continue;
				String statement = builder.dropTableConstraint(
						(CheckConstraint) o, quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addAlterTableDropConstraintStatement(statement);
			} else if (o instanceof UniqueConstraint) {
				if (!this.generatePKConstraints(options))
					continue;
				String statement = builder.dropTableConstraint(
						(UniqueConstraint) o, quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addAlterTableDropConstraintStatement(statement);
			} else if (o instanceof ForeignKey) {
				if (!this.generateFKConstraints(options))
					continue;
				String statement = builder.dropTableConstraint((ForeignKey) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addAlterTableDropForeignKeyStatement(statement);
			} else if (o instanceof Index) {
				if (!this.generateIndexes(options))
					continue;
				String statement = builder.dropIndex((Index) o,
						quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addDropIndexStatement(statement);
			} else if (o instanceof Procedure) {
				if (!this.generateStoredProcedures(options))
					continue;
				String dropStatement = builder.dropProcedure((Procedure) o,
						quoteIdentifiers, qualifyNames);
				if (dropStatement != null)
					script.addDropProcedureStatement(dropStatement);
			} else if (o instanceof UserDefinedFunction) {
				if (!this.generateFunctions(options))
					continue;
				String statement = builder
						.dropFunction((UserDefinedFunction) o,
								quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addDropFunctionStatement(statement);
			} else if (o instanceof Sequence) {
				if (!this.generateSequences(options))
					continue;
				String statement = builder.dropSequence(
						(Sequence) o, quoteIdentifiers, qualifyNames);
				if (statement != null)
					script.addDropSequence(statement);
			}
		}
		return script.getStatements();
	}

	public String[] createSQLObjects(SQLObject[] elements,
			boolean quoteIdentifiers, boolean qualifyNames,
			IProgressMonitor progressMonitor) {
		return super.createSQLObjects(elements, quoteIdentifiers, false,
				progressMonitor);
	}

	public String[] createSQLObjects(SQLObject[] elements,
			boolean quoteIdentifiers, boolean qualifyNames,
			IProgressMonitor progressMonitor, IEngineeringCallBack callback) {
		return super.createSQLObjects(elements, quoteIdentifiers, false,
				progressMonitor, callback);
	}

	public String[] dropSQLObjects(SQLObject[] elements,
			boolean quoteIdentifiers, boolean qualifyNames,
			IProgressMonitor progressMonitor) {
		return super.dropSQLObjects(elements, quoteIdentifiers, false,
				progressMonitor);
	}

	public String[] dropSQLObjects(SQLObject[] elements,
			boolean quoteIdentifiers, boolean qualifyNames,
			IProgressMonitor progressMonitor, IEngineeringCallBack callback) {
		return super.dropSQLObjects(elements, quoteIdentifiers, false,
				progressMonitor, callback);
	}
	
	protected EngineeringOption getEngineeringOption(String id, EngineeringOptionCategory general_options, EngineeringOptionCategory additional_element) {
	    ResourceBundle resource = ResourceBundle.getBundle("org.eclipse.datatools.enablement.firebird.ddl.FirebirdDdlGeneration"); //$NON-NLS-1$
	    
	    try {
	        // Work around for missing resource in DTP
	        if (id.equalsIgnoreCase(EngineeringOptionID.GENERATE_SEQUENCES))
                return new EngineeringOption(id,resource.getString("GENERATE_SEQUENCE"), resource.getString("GENERATE_SEQUENCE_DES"),true,additional_element); //$NON-NLS-1$ //$NON-NLS-2$
	        else
	            return super.getEngineeringOption(id, general_options, additional_element);
	    } catch (Exception e) {
            //The resource was not found
            e.printStackTrace();
        }
        return null;
	}
	
	protected Set getAllContainedDisplayableElementSetDepedency(SQLObject[] elements) {
	    Set s = new TreeSet();
	    
	    for(int i=0; i<elements.length; ++i) {
	        if(elements[i] instanceof Database || elements[i] instanceof Schema) {
                s.add(EngineeringOptionID.GENERATE_TABLES);
                s.add(EngineeringOptionID.GENERATE_INDICES);
                s.add(EngineeringOptionID.GENERATE_VIEWS);
                s.add(EngineeringOptionID.GENERATE_TRIGGERS);
                s.add(EngineeringOptionID.GENERATE_PK_CONSTRAINTS);
                s.add(EngineeringOptionID.GENERATE_CK_CONSTRAINTS);
                s.add(EngineeringOptionID.GENERATE_FK_CONSTRAINTS);
                s.add(EngineeringOptionID.GENERATE_SEQUENCES);
            } else if (elements[i] instanceof PersistentTable) {
                s.add(EngineeringOptionID.GENERATE_DATABASE);
                s.add(EngineeringOptionID.GENERATE_TABLES);
                s.add(EngineeringOptionID.GENERATE_INDICES);
                s.add(EngineeringOptionID.GENERATE_TRIGGERS);
                s.add(EngineeringOptionID.GENERATE_PK_CONSTRAINTS);
                s.add(EngineeringOptionID.GENERATE_CK_CONSTRAINTS);
                s.add(EngineeringOptionID.GENERATE_FK_CONSTRAINTS);
                // TODO find out how to resolve dependencies etc
                //s.add(EngineeringOptionID.GENERATE_SEQUENCES);
            } else if (elements[i] instanceof Index) {
                s.add(EngineeringOptionID.GENERATE_INDICES);
            } else if (elements[i] instanceof Procedure) {
                s.add(EngineeringOptionID.GENERATE_STOREDPROCEDURES);
            } else if (elements[i] instanceof UserDefinedFunction) {
                s.add(EngineeringOptionID.GENERATE_FUNCTIONS);
            } else if (elements[i] instanceof ViewTable) {
                s.add(EngineeringOptionID.GENERATE_VIEWS);
                s.add(EngineeringOptionID.GENERATE_TRIGGERS);
            } else if (elements[i] instanceof Trigger) {
                s.add(EngineeringOptionID.GENERATE_TRIGGERS);
            } else if (elements[i] instanceof Sequence) {
                s.add(EngineeringOptionID.GENERATE_SEQUENCES);
            } else if (elements[i] instanceof UserDefinedType) {
                s.add(EngineeringOptionID.GENERATE_USER_DEFINED_TYPE);
            } else if (elements[i] instanceof UniqueConstraint) {
                s.add(EngineeringOptionID.GENERATE_PK_CONSTRAINTS);
            } else if(elements[i] instanceof CheckConstraint) {
                s.add(EngineeringOptionID.GENERATE_CK_CONSTRAINTS);
            } else if(elements[i] instanceof ForeignKey) {
                s.add(EngineeringOptionID.GENERATE_FK_CONSTRAINTS);
            }
	    }
	    return s;
	}
}
