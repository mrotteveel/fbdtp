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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\w{2})-V(\\d+)\\.(\\d+).(\\d+).(\\d+)");
    private static final int IDX_PLATFORM = 1;
    private static final int IDX_MAJOR = 2;
    private static final int IDX_MINOR = 3;
    private static final int IDX_RELEASE = 4;
    private static final int IDX_BUILD = 5;

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
			String version = dbmd.getDatabaseProductVersion();
			Matcher matcher = VERSION_PATTERN.matcher(version);
			try {
    			if (matcher.find()) {
    			    String platform = matcher.group(IDX_PLATFORM);
    			    int major = Integer.parseInt(matcher.group(IDX_MAJOR));
    			    int minor = Integer.parseInt(matcher.group(IDX_MINOR));
    			    int release = Integer.parseInt(matcher.group(IDX_RELEASE));
    			    String build = matcher.group(IDX_BUILD);
    			    mServerVersion = new Version(major, minor, release, build + "(" + platform + ")");
    			} else {
    			    mServerVersion = new Version(dbmd.getDatabaseMajorVersion(), dbmd.getDatabaseMinorVersion(), 0, "");
    			}
			} catch (SQLException e) {
			    // Defaults
			}
			try {
			    mTechVersion = new Version(dbmd.getJDBCMajorVersion(), dbmd.getJDBCMinorVersion(), 0, "");
			} catch (SQLException e) {
			    // Defaults
			}
		} catch (SQLException e) {
			// Defaults
		}
    }
}
