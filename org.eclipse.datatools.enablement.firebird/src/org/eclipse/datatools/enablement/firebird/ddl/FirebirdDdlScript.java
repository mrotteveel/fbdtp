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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdDdlScript {
	protected List dropForeignKeyStatements = new ArrayList();
	protected List dropViewStatements = new ArrayList();
	protected List dropConstraintStatements = new ArrayList();
    
	protected List dropTriggerStatements = new ArrayList();
    protected List dropProcedureStatements = new ArrayList();
    
	protected List createTableStatements = new ArrayList();
	protected List alterTableStatements = new ArrayList();
    protected List dropTableStatements = new ArrayList();
    protected List addForeignKeyStatements = new ArrayList();
	
	protected List createViewStatements = new ArrayList();

	protected List createIndexStatements = new ArrayList();
    protected List dropIndexStatements = new ArrayList();
	
    protected List createTriggerStatements = new ArrayList();
    protected List alterTriggerStatements = new ArrayList();
    
    protected List createProcedureStatements = new ArrayList();
    protected List alterProcedureStatements = new ArrayList();
    
    protected List createFunctionStatements = new ArrayList();
    protected List dropFunctionStatements = new ArrayList();
    
	protected List addConstraintStatements = new ArrayList();
    
	protected List createSequenceStatements = new ArrayList();
	protected List dropSequenceStatements = new ArrayList();
	
	public void addDropTableStatement(String statement) {
		dropTableStatements.add(statement);
	}

	public void addCreateTableStatement(String statement) {
		createTableStatements.add(statement);
	}

	public void addAlterTableStatement(String statement) {
		createTableStatements.add(statement);
	}

	public void addDropViewStatement(String statement) {
		dropViewStatements.add(statement);
	}

	public void addCreateViewStatement(String statement) {
		createViewStatements.add(statement);
	}

	public void addAlterTableDropForeignKeyStatement(String statement) {
		dropForeignKeyStatements.add(statement);
	}

	public void addAlterTableAddForeignKeyStatement(String statement) {
		addForeignKeyStatements.add(statement);
	}

	public void addDropIndexStatement(String statement) {
		dropIndexStatements.add(statement);
	}

	public void addCreateIndexStatement(String statement) {
		createIndexStatements.add(statement);
	}

	public void addDropTriggerStatement(String statement) {
		dropTriggerStatements.add(statement);
	}

	public void addCreateTriggerStatement(String statement) {
		createTriggerStatements.add(statement);
	}

    public void addDropProcedureStatement(String statement) {
        dropProcedureStatements.add(statement);
    }
    public void addCreateProcedureStatement(String statement) {
        createProcedureStatements.add(statement);
    }
    public void addAlterProcedureStatement(String statement) {
        alterProcedureStatements.add(statement);
    }

    public void addCreateFunctionStatement(String statement) {
        createFunctionStatements.add(statement);
    }
    public void addDropFunctionStatement(String statement) {
        dropFunctionStatements.add(statement);
    }
    
	public void addAlterTableDropConstraintStatement(String statement) {
		dropConstraintStatements.add(statement);
	}

	public void addAlterTableAddConstraintStatement(String statement) {
		addConstraintStatements.add(statement);
	}
	
    public void addDropSequence(String statement) {
        createSequenceStatements.add(statement);
    }

    public void addCreateSequence(String statement) {
        dropSequenceStatements.add(statement);
    }

    public String[] getStatements() {
    	List script = new ArrayList();
        script.addAll(dropTriggerStatements);
        script.addAll(dropViewStatements);
        script.addAll(dropProcedureStatements);
		script.addAll(dropForeignKeyStatements);
		script.addAll(dropConstraintStatements);
		script.addAll(dropIndexStatements);
		script.addAll(dropTableStatements);
        script.addAll(dropFunctionStatements);
		script.addAll(dropSequenceStatements);

		script.addAll(createSequenceStatements);
        script.addAll(createFunctionStatements);
		script.addAll(createTableStatements);
		script.addAll(alterTableStatements);
		script.addAll(createViewStatements);
		script.addAll(createIndexStatements);
		script.addAll(addConstraintStatements);
		script.addAll(addForeignKeyStatements);
        script.addAll(createProcedureStatements);
		script.addAll(createTriggerStatements);
        script.addAll(alterProcedureStatements);

		String[] scripts = new String[script.size()];
		script.toArray(scripts);
		return scripts;
	}

}
