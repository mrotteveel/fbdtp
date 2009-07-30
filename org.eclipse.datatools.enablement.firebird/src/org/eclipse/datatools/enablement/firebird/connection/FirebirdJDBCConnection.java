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

package org.eclipse.datatools.enablement.firebird.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.Version;
import org.eclipse.datatools.connectivity.drivers.jdbc.JDBCConnection;

/**
 * JDBCConnection implementation for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 *
 */
public class FirebirdJDBCConnection extends JDBCConnection {

    public static final String TECHNOLOGY_ROOT_KEY = "firebird_jdbc";
    public static final String TECHNOLOGY_NAME = "Firebird JDBC Connection";

    private Version mTechVersion = Version.NULL_VERSION;
    private Version mServerVersion = Version.NULL_VERSION;
    private String mServerName;

    public FirebirdJDBCConnection(IConnectionProfile profile, Class factoryClass) {
        super(profile, factoryClass);
    }

    public String getProviderName() {
        return mServerName;
    }

    public Version getProviderVersion() {
        return mServerVersion;
    }

    protected String getTechnologyRootKey() {
        return TECHNOLOGY_ROOT_KEY;
    }

    public String getTechnologyName() {
        return TECHNOLOGY_NAME;
    }

    public Version getTechnologyVersion() {
        return mTechVersion;
    }

    protected void initVersions() {
    	mServerName = "Firebird";
   		try {
			DatabaseMetaData dbmd = ((Connection)getRawConnection()).getMetaData();
			//TODO Add release and build info
			mServerVersion = new Version(dbmd.getDatabaseMajorVersion(), dbmd.getDatabaseMinorVersion(), 0, "");
			mTechVersion = new Version(dbmd.getDriverMajorVersion(), dbmd.getDatabaseMinorVersion(), 0, "");
		} catch (SQLException e) {
			// Defaults
		}
    }
}
