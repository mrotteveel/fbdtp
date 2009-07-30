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
 */ 

package org.eclipse.datatools.sqltools.db.firebird.core;

import java.util.Map;

import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.ProcIdentifierImpl;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdProcIdentifier extends ProcIdentifierImpl {

    public FirebirdProcIdentifier(int type, DatabaseIdentifier db, Map map) {
        super(type, db, map);
    }

    public String getCallableStringWithoutGroupNumber(boolean quoted_id) {
        // String call = super.getCallableStringWithoutGroupNumber(quoted_id);
        // Firebird doesn't support catalog name
        // return call.substring(call.indexOf(".")+ 1);
        if (quoted_id)
            return "\"" + getProcName() + "\"";
        else
            return getProcName();
    }

    public String getLongDisplayString() {
        String s = "";

        if (getType() == TYPE_TRIGGER && getTableName() != null
                && getTableName().length() > 0) {
            s = "(" + getProfileName() + ")" + getOwnerName() + "."
                    + getTableName() + "." + getDisplayString();
        }
        else {
            s = "(" + getProfileName() + ")" + getOwnerName() + "."
                    + getDisplayString();
        }
        return s;
    }
}
