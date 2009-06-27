package org.eclipse.datatools.enablement.firebird.catalog;

import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.rte.RefreshManager;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCParameter;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCUDFColumnLoader;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdUDFColumnLoader;
import org.eclipse.datatools.modelbase.sql.routines.ParameterMode;
import org.eclipse.datatools.modelbase.sql.routines.impl.UserDefinedFunctionImpl;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.emf.common.util.EList;

/**
 * 
 * @author Roman Rokytskyy
 * 
 */
public class FirebirdUDF extends UserDefinedFunctionImpl implements
		ICatalogObject {

	private Boolean resultTableLoaded = Boolean.FALSE;
	private Boolean parametersLoaded = Boolean.FALSE;
	private SoftReference paremeterLoaderRef;

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

	public void refresh() {
		synchronized (parametersLoaded) {
			if (parametersLoaded.booleanValue()) {
				setReturnScalar(null);
				parametersLoaded = Boolean.FALSE;
			}
		}

		synchronized (resultTableLoaded) {
			if (resultTableLoaded.booleanValue()) {
				setReturnTable(null);
				resultTableLoaded = Boolean.FALSE;
			}
		}

		RefreshManager.getInstance().referesh(this);
	}

	public Connection getConnection() {
		Database db = getCatalogDatabase();
		if (db instanceof ICatalogObject) {
			return ((ICatalogObject) db).getConnection();
		}
		return null;
	}

	public Database getCatalogDatabase() {
		return getSchema().getDatabase();
	}

	protected JDBCUDFColumnLoader createParameterLoader() {
		return new FirebirdUDFColumnLoader(this);
	}

	protected final JDBCUDFColumnLoader getParameterLoader() {
		if (paremeterLoaderRef == null || paremeterLoaderRef.get() == null) {
			paremeterLoaderRef = new SoftReference(createParameterLoader());
		}
		return (JDBCUDFColumnLoader) paremeterLoaderRef.get();
	}

	public EList getParameters() {
		synchronized (parametersLoaded) {
			if (!parametersLoaded.booleanValue())
				loadParameters();
		}
		return super.getParameters();
	}

	public org.eclipse.datatools.modelbase.sql.routines.Parameter getReturnScalar() {
		synchronized (parametersLoaded) {
			if (!parametersLoaded.booleanValue())
				loadParameters();
		}
		return super.getReturnScalar();
	}

	private void loadParameters() {
		boolean deliver = eDeliver();
		try {
			List parametersContainer = super.getParameters();
			List existingParameters = new ArrayList(parametersContainer);

			eSetDeliver(false);

			parametersContainer.clear();
			setReturnScalar(null);

			getParameterLoader().loadParameters(parametersContainer,
					existingParameters);
			getParameterLoader().clearColumns(existingParameters);

			parametersLoaded = Boolean.TRUE;

			Parameter outParam = null;
			for (Iterator it = parametersContainer.iterator(); it.hasNext();) {
				Parameter p = (Parameter) it.next();
				if (p.getMode() == ParameterMode.OUT_LITERAL) {
					outParam = p;
					// it.remove();
					break;
				}
			}

			setReturnScalar(outParam);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			eSetDeliver(deliver);
		}
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
