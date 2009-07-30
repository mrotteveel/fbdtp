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

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCParameter;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCUserDefinedFunction;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCUDFColumnLoader;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdUDFColumnLoader;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdUDF extends JDBCUserDefinedFunction {

	private String entryPoint;
	private String moduleName;
	private int returnArgument;

	protected JDBCUDFColumnLoader createParameterLoader() {
		return new FirebirdUDFColumnLoader(this);
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public int getReturnArgument() {
		return returnArgument;
	}

	public void setReturnArgument(int returnArgument) {
		this.returnArgument = returnArgument;
	}

	public static class Parameter extends JDBCParameter {

		private int argumentPosition;
		private int mechanism;
		private int fieldType;
		private int fieldScale;
		private int fieldPrecision;
		private int fieldLength;
		private int fieldSubType;
		private int charSetId;
		private int charLength;

		public String getDataTypeName() {
			return FirebirdConversionUtil.getTypeAsString(fieldType,
					fieldSubType, fieldScale);
		}

		public int getArgumentPosition() {
			return argumentPosition;
		}

		public void setArgumentPosition(int argumentPosition) {
			this.argumentPosition = argumentPosition;
		}

		public int getCharLength() {
			return charLength;
		}

		public void setCharLength(int charLength) {
			this.charLength = charLength;
		}

		public int getCharSetId() {
			return charSetId;
		}

		public void setCharSetId(int charSetId) {
			this.charSetId = charSetId;
		}

		public int getFieldLength() {
			return fieldLength;
		}

		public void setFieldLength(int fieldLength) {
			this.fieldLength = fieldLength;
		}

		public int getFieldPrecision() {
			return fieldPrecision;
		}

		public void setFieldPrecision(int fieldPrecision) {
			this.fieldPrecision = fieldPrecision;
		}

		public int getFieldScale() {
			return fieldScale;
		}

		public void setFieldScale(int fieldScale) {
			this.fieldScale = fieldScale;
		}

		public int getFieldSubType() {
			return fieldSubType;
		}

		public void setFieldSubType(int fieldSubType) {
			this.fieldSubType = fieldSubType;
		}

		public int getFieldType() {
			return fieldType;
		}

		public void setFieldType(int fieldType) {
			this.fieldType = fieldType;
		}

		public int getMechanism() {
			return mechanism;
		}

		public void setMechanism(int mechanism) {
			this.mechanism = mechanism;
		}

	}

}
