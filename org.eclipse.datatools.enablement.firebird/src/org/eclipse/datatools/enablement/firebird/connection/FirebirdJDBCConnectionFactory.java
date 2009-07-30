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
 *     Brian Fitzpatrick  - Initial implementation
 *     Mark Rotteveel   - Code cleanup, further development
 */ 

package org.eclipse.datatools.enablement.firebird.connection;

import org.eclipse.datatools.connectivity.IConnection;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.drivers.jdbc.JDBCConnectionFactory;

/**
 * JDBCConnectionFactory implementation for the Firebird database.
 * 
 * @author brianf
 * @author Mark Rotteveel
 *
 */
public class FirebirdJDBCConnectionFactory extends JDBCConnectionFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.db.generic.JDBCConnectionFactory#createConnection(org.eclipse.datatools.connectivity.IConnectionProfile)
	 */
	public IConnection createConnection(IConnectionProfile profile) {
	    FirebirdJDBCConnection connection = new FirebirdJDBCConnection(profile, getClass());
	    connection.open();
		return connection;
	}

}
