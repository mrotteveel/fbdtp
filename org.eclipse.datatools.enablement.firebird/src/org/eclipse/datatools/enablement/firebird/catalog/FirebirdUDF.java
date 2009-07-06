package org.eclipse.datatools.enablement.firebird.catalog;

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCParameter;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCUserDefinedFunction;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCUDFColumnLoader;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdUDFColumnLoader;
import org.eclipse.datatools.modelbase.sql.schema.Schema;

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

	// have to cache here, otherwise we get StackOverflowError
	private FirebirdSchema schema;

	public FirebirdUDF(Schema schema) {
		this.schema = (FirebirdSchema) schema;
	}

	public Schema getSchema() {
		return schema;
	}

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
