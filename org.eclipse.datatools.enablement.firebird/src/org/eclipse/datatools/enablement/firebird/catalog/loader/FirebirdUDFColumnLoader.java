package org.eclipse.datatools.enablement.firebird.catalog.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.loader.IConnectionFilterProvider;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCUDFColumnLoader;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdUDF;
import org.eclipse.datatools.modelbase.dbdefinition.PredefinedDataTypeDefinition;
import org.eclipse.datatools.modelbase.sql.datatypes.PredefinedDataType;
import org.eclipse.datatools.modelbase.sql.routines.Parameter;
import org.eclipse.datatools.modelbase.sql.routines.ParameterMode;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * 
 * @author Roman Rokytskyy
 * 
 */
public class FirebirdUDFColumnLoader extends JDBCUDFColumnLoader {

    public FirebirdUDFColumnLoader(ICatalogObject catalogObject, IConnectionFilterProvider connectionFilterProvider) {
        super(catalogObject, connectionFilterProvider);
    }

    public FirebirdUDFColumnLoader(ICatalogObject catalogObject) {
        super(catalogObject);
    }

    protected void initParameter(Parameter parameter, ResultSet rs) throws SQLException {
        FirebirdUDF udf = (FirebirdUDF)getRoutine();
        
        FirebirdUDF.Parameter udfParameter = (FirebirdUDF.Parameter)parameter;
        
        int argumentPosition = rs.getInt("argument_position");
        int mechanism = rs.getInt("mechanism");
        int fieldType = rs.getInt("field_type");
        int fieldScale = rs.getInt("field_scale");
        int fieldLength = rs.getInt("field_length");
        int fieldSubType = rs.getInt("field_subtype");
        int charSetId = rs.getInt("charset_id");
        int fieldPrecision = rs.getInt("field_precision");
        int charLength = rs.getInt("char_len");
        
        udfParameter.setName("PARAM_" + argumentPosition);
        
        udfParameter.setMode(
            argumentPosition == udf.getReturnArgument() ? 
                    ParameterMode.OUT_LITERAL : ParameterMode.IN_LITERAL);
        
        udfParameter.setArgumentPosition(argumentPosition);
        udfParameter.setMechanism(mechanism);
        udfParameter.setFieldType(fieldType);
        udfParameter.setFieldScale(fieldScale);
        udfParameter.setFieldLength(fieldLength);
        udfParameter.setFieldSubType(fieldSubType);
        udfParameter.setFieldPrecision(fieldPrecision);
        udfParameter.setCharSetId(charSetId);
        udfParameter.setCharLength(charLength);
        
        // db definition types are always upper case: make sure the typeName is
        // upper too
        String typeName = FirebirdConversionUtil.getTypeAsString(fieldType, fieldSubType, fieldScale);
        int typeCode = FirebirdConversionUtil.getJdbcType(fieldType);

        // See if it's a predefined type
        List pdtds = getDatabaseDefinition().getPredefinedDataTypeDefinitionsByJDBCEnumType(typeCode);
        
        if (pdtds.size() == 0) 
            return; 
        
        PredefinedDataTypeDefinition pdtd = null;
        for (Iterator it = pdtds.iterator(); pdtd == null && it.hasNext();) {
            
            PredefinedDataTypeDefinition curPDTD = (PredefinedDataTypeDefinition) it.next();
            for (Iterator nameIt = curPDTD.getName().iterator(); nameIt.hasNext();) {
                
                String name = (String) nameIt.next();
                if (typeName.equals(name)) {
                    pdtd = curPDTD;
                    break;
                }
            }
        }

        if (pdtd == null) {
            pdtd = getDatabaseDefinition().getPredefinedDataTypeDefinition(typeName);
        }

        if (pdtd == null)
            return;
        
        if (typeCode == Types.OTHER || typeCode == Types.REF)
            return;
            
        if (pdtd == null) {
            // Use the first element by default
            pdtd = (PredefinedDataTypeDefinition) pdtds.get(0);
        }

        PredefinedDataType pdt = getDatabaseDefinition().getPredefinedDataType(pdtd);
        
        if (pdtd.isLengthSupported()) {
            EStructuralFeature feature = pdt.eClass().getEStructuralFeature("length"); //$NON-NLS-1$
            pdt.eSet(feature, new Integer(charLength));
        }
        
        if (pdtd.isPrecisionSupported()) {
            EStructuralFeature feature = pdt.eClass().getEStructuralFeature("precision"); //$NON-NLS-1$
            pdt.eSet(feature, new Integer(fieldPrecision));
        }
        
        if (pdtd.isScaleSupported()) {
            EStructuralFeature feature = pdt.eClass().getEStructuralFeature("scale"); //$NON-NLS-1$
            pdt.eSet(feature, new Integer(-fieldScale));
        }
        udfParameter.setDataType(pdt);
    }

    private static final String UDF_PARAMETERS = ""
        + "SELECT "
        + "  cast('PARAM_' || rdb$argument_position AS varchar(31)) AS column_name, "
        + "  fa.rdb$argument_position AS argument_position, "
        + "  fa.rdb$mechanism AS mechanism, "
        + "  fa.rdb$field_type AS field_type, "
        + "  fa.rdb$field_scale AS field_scale, "
        + "  fa.rdb$field_length AS field_length, "
        + "  fa.rdb$field_sub_type AS field_subtype, "
        + "  fa.rdb$character_set_id AS charset_id, "
        + "  fa.rdb$field_precision AS field_precision, "
        + "  fa.rdb$character_length AS char_len "
        + "FROM "
        + "  rdb$function_arguments fa "
        + "WHERE "
        + "  fa.rdb$function_name = ?"
        ;
    
    protected ResultSet createParametersResultSet() throws SQLException {
        try {
            Connection connection = getCatalogObject().getConnection();
            
            FirebirdUDF udf = (FirebirdUDF)getRoutine();
            
            PreparedStatement stmt = connection.prepareStatement(UDF_PARAMETERS);
            stmt.setString(1, udf.getName());
            
            return stmt.executeQuery();
        }
        catch (RuntimeException e) {
        	//FIXME Fix message
            SQLException error = new SQLException(/*MessageFormat.format(
                Messages.Error_Unsupported_DatabaseMetaData_Method,
                new Object[] { "java.sql.DatabaseMetaData.getProcedures()"})*/); //$NON-NLS-1$
            
            error.initCause(e);
            throw error;
        }
    }

    protected boolean isParameter(ResultSet rs) throws SQLException {
        return true;
    }

    protected Parameter createParameter() {
        return new FirebirdUDF.Parameter();
    }

    protected void closeParametersResultSet(ResultSet rs) {
        try {
            rs.getStatement().close();
        }
        catch (SQLException e) {
        }
    }
    
    protected void closeRoutineResultTableResultSet(ResultSet rs) {
        try {
            rs.getStatement().close();
        }
        catch (SQLException e) {
        }
    }
}
