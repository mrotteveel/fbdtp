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
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdParameter;
import org.eclipse.datatools.enablement.firebird.catalog.FirebirdUDF;
import org.eclipse.datatools.modelbase.dbdefinition.PredefinedDataTypeDefinition;
import org.eclipse.datatools.modelbase.sql.datatypes.PredefinedDataType;
import org.eclipse.datatools.modelbase.sql.routines.Parameter;
import org.eclipse.datatools.modelbase.sql.routines.ParameterMode;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdUDFColumnLoader extends JDBCUDFColumnLoader {

    /**
     * @param catalogObject the Procedure object upon which this loader
     *        operates.
     * @param connectionFilterProvider the filter provider used for filtering
     *        the "column" objects being loaded
     */
    public FirebirdUDFColumnLoader(ICatalogObject catalogObject, IConnectionFilterProvider connectionFilterProvider) {
        super(catalogObject, connectionFilterProvider);
    }

    /**
     * Constructs the loader using no filtering.
     * 
     * @param catalogObject the Database object upon which this loader operates.
     */
    public FirebirdUDFColumnLoader(ICatalogObject catalogObject) {
        super(catalogObject);
    }

    protected void initParameter(Parameter parameter, ResultSet rs) throws SQLException {
        FirebirdUDF udf = (FirebirdUDF)getRoutine();
        
        FirebirdParameter udfParameter = (FirebirdParameter)parameter;
        
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
        
        // db definition types are always upper case: make sure the typeName is upper too
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
        // TODO Statement below conflicts with statement 7 lines down
        if (pdtd == null)
            return;
        
        if (typeCode == Types.OTHER || typeCode == Types.REF)
            return;
        // TODO statement below conflicts with statement 5 lines above   
        if (pdtd == null) {
            // Use the first element by default
            pdtd = (PredefinedDataTypeDefinition) pdtds.get(0);
        }

        PredefinedDataType pdt = getDatabaseDefinition().getPredefinedDataType(pdtd);
        
        if (pdtd.isLengthSupported()) {
            EStructuralFeature feature = pdt.eClass().getEStructuralFeature("length"); //$NON-NLS-1$
            pdt.eSet(feature, Integer.valueOf(charLength));
        }
        
        if (pdtd.isPrecisionSupported()) {
            EStructuralFeature feature = pdt.eClass().getEStructuralFeature("precision"); //$NON-NLS-1$
            pdt.eSet(feature, Integer.valueOf(fieldPrecision));
        }
        
        if (pdtd.isScaleSupported()) {
            EStructuralFeature feature = pdt.eClass().getEStructuralFeature("scale"); //$NON-NLS-1$
            pdt.eSet(feature, Integer.valueOf(-fieldScale));
        }
        udfParameter.setDataType(pdt);
    }

    private static final String UDF_PARAMETERS =
          "SELECT"
        + " cast('PARAM_' || rdb$argument_position AS varchar(31)) AS column_name,"
        + " fa.rdb$argument_position AS argument_position,"
        + " fa.rdb$mechanism AS mechanism,"
        + " fa.rdb$field_type AS field_type,"
        + " fa.rdb$field_scale AS field_scale,"
        + " fa.rdb$field_length AS field_length,"
        + " fa.rdb$field_sub_type AS field_subtype,"
        + " fa.rdb$character_set_id AS charset_id,"
        + " fa.rdb$field_precision AS field_precision,"
        + " fa.rdb$character_length AS char_len "
        + "FROM "
        + " rdb$function_arguments fa "
        + "WHERE "
        + " fa.rdb$function_name = ?";
    
    /**
     * Creates a result set containing the procedure parameters which will be
     * used by the loading logic.
     * 
     * @return a result containing the information used to initialize Parameter
     *         objects
     * 
     * @throws SQLException if anything goes wrong
     */
    protected ResultSet createParametersResultSet() throws SQLException {
        try {
            Connection connection = getCatalogObject().getConnection();
            
            PreparedStatement stmt = connection.prepareStatement(UDF_PARAMETERS);
            stmt.setString(1, getRoutine().getName());
            
            return stmt.executeQuery();
        }
        catch (RuntimeException e) {
            SQLException error = new SQLException(
                    Activator.getResourceString("error.udf.parameter.loading")); //$NON-NLS-1$
            error.initCause(e);
            throw error;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineColumnLoader#isParameter(java.sql.ResultSet)
     */
    protected boolean isParameter(ResultSet rs) throws SQLException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineColumnLoader#createParameter()
     */
    protected Parameter createParameter() {
        return new FirebirdParameter();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineColumnLoader#closeParametersResultSet(java.sql.ResultSet)
     */
    protected void closeParametersResultSet(ResultSet rs) {
        try {
            // TODO: Why close statement, not resultset?
            rs.getStatement().close();
        }
        catch (SQLException e) {
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.sqm.loader.JDBCRoutineColumnLoader#closeRoutineResultTableResultSet(java.sql.ResultSet)
     */
    protected void closeRoutineResultTableResultSet(ResultSet rs) {
        try {
            // TODO: Why close statement, not resultset?
            rs.getStatement().close();
        }
        catch (SQLException e) {
        }
    }
}
