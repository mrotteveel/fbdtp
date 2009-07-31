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

package org.eclipse.datatools.enablement.firebird.catalog;

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCUserDefinedFunction;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCUDFColumnLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdUDFColumnLoader;

/**
 * User Defined Function implementation for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdUDF extends JDBCUserDefinedFunction {

	private String entryPoint;
	private String moduleName;
	private int returnArgument;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCUserDefinedFunction#createParameterLoader()
	 */
	protected JDBCUDFColumnLoader createParameterLoader() {
		return new FirebirdUDFColumnLoader(this);
	}

	/**
	 * Entrypoint within the function library for this function.
	 * 
	 * @return entryPoint of the UDF
	 */
	public String getEntryPoint() {
		return entryPoint;
	}

	/**
	 * 
	 * @param entryPoint the entryPoint of the UDF
	 */
	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	/**
	 * Modulename, the function library where the executable function is stored.
	 * 
	 * @return the moduleName of the UDF
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * 
	 * @param moduleName the moduleName of the UDF
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * The position of the return argument in relation to other arguments.
	 * 
	 * @return position of the return argument
	 */
	public int getReturnArgument() {
		return returnArgument;
	}

	/**
	 * Sets the position of the return argument.
	 * 
	 * @param returnArgument position of the return argument
	 */
	public void setReturnArgument(int returnArgument) {
		this.returnArgument = returnArgument;
	}
}
