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
import org.eclipse.datatools.enablement.firebird.FirebirdConversionUtil;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdParameter extends JDBCParameter {

    private int argumentPosition;
    private int mechanism;
    private int fieldType;
    private int fieldScale;
    private int fieldPrecision;
    private int fieldLength;
    private int fieldSubType;
    private int charSetId;
    private int charLength;

    // TODO Check usage
    public String getDataTypeName() {
        return FirebirdConversionUtil.getTypeAsString(fieldType, fieldSubType,
                fieldScale);
    }

    /**
     * Argument position in relation to other arguments.
     * 
     * @return the argumentPosition
     */
    public int getArgumentPosition() {
        return argumentPosition;
    }

    /**
     * 
     * @param argumentPosition
     *            the argumentPosition
     */
    public void setArgumentPosition(int argumentPosition) {
        this.argumentPosition = argumentPosition;
    }

    /**
     * The lenght of a CHAR or VARCHAR field (if this parameter defines that
     * type).
     * 
     * @return the charLength
     */
    public int getCharLength() {
        return charLength;
    }

    /**
     * 
     * @param charLength
     *            the charLength
     */
    public void setCharLength(int charLength) {
        this.charLength = charLength;
    }

    /**
     * The unique numeric identifier of the characterset as defined for
     * Firebird.
     * 
     * @return the charSetId
     */
    public int getCharSetId() {
        return charSetId;
    }

    /**
     * 
     * @param charSetId
     *            the charSetId
     */
    public void setCharSetId(int charSetId) {
        this.charSetId = charSetId;
    }

    /**
     * The (byte) length of the parameter
     * 
     * @return the fieldLength
     */
    public int getFieldLength() {
        return fieldLength;
    }

    /**
     * 
     * @param fieldLength
     *            the fieldLength
     */
    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

    /**
     * The precision of a NUMERIC or DECIMAL type (if this parameter defines
     * that type).
     * 
     * @return the fieldPrecision
     */
    public int getFieldPrecision() {
        return fieldPrecision;
    }

    /**
     * 
     * @param fieldPrecision
     *            the fieldPrecision
     */
    public void setFieldPrecision(int fieldPrecision) {
        this.fieldPrecision = fieldPrecision;
    }

    /**
     * The scale factor for a NUMERIC or DECIMAL type (if this parameter defines
     * that type).
     * 
     * @return the fieldScale
     */
    public int getFieldScale() {
        return fieldScale;
    }

    /**
     * 
     * @param fieldScale
     *            the fieldScale
     */
    public void setFieldScale(int fieldScale) {
        this.fieldScale = fieldScale;
    }

    /**
     * Defines how the type must be interpreted (Firebird implementation).
     * 
     * @return the fieldSubType
     */
    public int getFieldSubType() {
        return fieldSubType;
    }

    /**
     * 
     * @param fieldSubType
     *            the fieldSubType
     */
    public void setFieldSubType(int fieldSubType) {
        this.fieldSubType = fieldSubType;
    }

    /**
     * Datatype of the parameter using the Firebird type identity.
     * 
     * @return the fieldType
     */
    public int getFieldType() {
        return fieldType;
    }

    /**
     * 
     * @param fieldType
     *            the fieldType
     */
    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Mechanism for passing parameters. By value: 0, by reference: 1.
     * 
     * @return the mechanism
     */
    public int getMechanism() {
        return mechanism;
    }

    /**
     * 
     * @param mechanism
     *            the mechanism
     */
    public void setMechanism(int mechanism) {
        this.mechanism = mechanism;
    }

}
