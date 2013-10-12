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

package org.eclipse.datatools.sqltools.db.firebird.core.services;

import org.eclipse.datatools.sqltools.core.DBHelper;
import org.eclipse.datatools.sqltools.core.services.SQLService;
import org.eclipse.datatools.sqltools.db.firebird.core.FirebirdHelper;
import org.eclipse.datatools.sqltools.db.generic.sql.GenericSQLSyntax;
import org.eclipse.datatools.sqltools.sql.ISQLSyntax;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 *
 */
public class FirebirdSQLService extends SQLService {

    public ISQLSyntax getSQLSyntax() {
        return new GenericSQLSyntax();
    }
    
    public DBHelper getDBHelper() {
        return new FirebirdHelper();
    }
}
