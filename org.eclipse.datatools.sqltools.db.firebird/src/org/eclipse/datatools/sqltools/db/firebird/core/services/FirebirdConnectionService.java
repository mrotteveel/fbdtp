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

package org.eclipse.datatools.sqltools.db.firebird.core.services;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.datatools.connectivity.IConnection;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.IManagedConnection;
import org.eclipse.datatools.connectivity.sqm.core.connection.ConnectionInfo;
import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.EditorCorePlugin;
import org.eclipse.datatools.sqltools.core.IControlConnection;
import org.eclipse.datatools.sqltools.core.profile.NoSuchProfileException;
import org.eclipse.datatools.sqltools.core.profile.ProfileUtil;
import org.eclipse.datatools.sqltools.core.services.ConnectionService;
import org.eclipse.datatools.sqltools.db.firebird.core.FirebirdControlConnection;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdConnectionService extends ConnectionService {
 
    public IControlConnection createControlConnection(
            DatabaseIdentifier databaseIdentifier) throws SQLException {
        return new FirebirdControlConnection(EditorCorePlugin
                .getControlConnectionManager(), databaseIdentifier);
    }

    public Connection createConnection(String profileName, String dbName) {
        try {
            Connection jdbcConn = ProfileUtil
                    .getReusableConnection(new DatabaseIdentifier(profileName,
                            dbName));
            IConnectionProfile profile = ProfileUtil.getProfile(profileName);
            IManagedConnection managedConn = profile
                    .getManagedConnection(ConnectionInfo.class.getName());
            IConnection iConn = managedConn.getConnection();
            ConnectionInfo connInfo = (ConnectionInfo) iConn.getRawConnection();

            if (jdbcConn == connInfo.getSharedConnection()) {
                return jdbcConn;
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchProfileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return super.createConnection(profileName, dbName);
    }
}
