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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCTable;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableConstraintLoader;
import org.eclipse.datatools.connectivity.sqm.loader.JDBCTableIndexLoader;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdConstraintLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdIndexLoader;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdTableLoader;
import org.eclipse.datatools.modelbase.sql.constraints.CheckConstraint;
import org.eclipse.datatools.modelbase.sql.constraints.Constraint;
import org.eclipse.datatools.modelbase.sql.constraints.PrimaryKey;
import org.eclipse.emf.common.util.EList;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdTable extends JDBCTable {

	private boolean showSystemObjects;

	private final Object checkConstraintsMutex = new Object();
	private boolean checkConstraintsLoaded = false;

	private final Object primaryKeyMutex = new Object();
	private boolean primaryKeyLoaded = false;

	private final Object triggersMutex = new Object();
	private boolean triggersLoaded = false;

	protected JDBCTableConstraintLoader createConstraintLoader() {
		return new FirebirdConstraintLoader(this);
	}

	protected JDBCTableIndexLoader createIndexLoader() {
		return new FirebirdIndexLoader(this, showSystemObjects);
	}

	public void refresh() {
		synchronized (triggersMutex) {
			triggersLoaded = false;
		}
		synchronized (primaryKeyMutex) {
		    primaryKeyLoaded = false;
		}
		synchronized (checkConstraintsMutex) {
            checkConstraintsLoaded = false;
        }

		super.refresh();
	}

	public EList getTriggers() {

		synchronized (triggersMutex) {
			if (!triggersLoaded)
				loadTriggers();
		}

		return super.getTriggers();
	}

	protected void loadTriggers() {
		EList triggerList = super.getTriggers();
		triggerList.clear();
		Connection connection = this.getConnection();

		boolean deliver = this.eDeliver();
		this.eSetDeliver(false);

		try {

			FirebirdTableLoader.loadTriggers(connection, getSchema(), this,
					triggerList);

			triggersLoaded = true;

		} catch (Exception e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
							"Could not load the triggers for table "
									+ this.getName(), e));
		}

		this.eSetDeliver(deliver);
	}

	public EList getConstraints() {

		synchronized (checkConstraintsMutex) {
			if (!checkConstraintsLoaded)
				loadCheckConstraints();
		}

		synchronized (primaryKeyMutex) {
			if (!primaryKeyLoaded)
				loadPrimaryKey();
		}

		return super.getConstraints();
	}

	protected void loadCheckConstraints() {
		boolean deliver = eDeliver();
		try {
			List container = super.getConstraints();

			List existingCheckConstraints = internalGetCheckConstraints(container);
			container.removeAll(existingCheckConstraints);

			((FirebirdConstraintLoader) getConstraintLoader())
					.loadCheckConstraints(container, existingCheckConstraints);

			checkConstraintsLoaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			eSetDeliver(deliver);
		}
	}

	protected void loadPrimaryKey() {
		boolean deliver = eDeliver();
		try {
			List container = super.getConstraints();

			PrimaryKey existingPrimaryKey = internalGetPrimaryKey(container);

			FirebirdConstraintLoader firebirdConstraintLoader = ((FirebirdConstraintLoader) getConstraintLoader());

			PrimaryKey newPrimaryKey = firebirdConstraintLoader
					.loadPrimaryKey(existingPrimaryKey);

			if (newPrimaryKey != null)
				container.add(newPrimaryKey);

			primaryKeyLoaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			eSetDeliver(deliver);
		}
	}

	private List internalGetCheckConstraints(Collection constraints) {
		List tmpConstraints = new ArrayList();
		for (Iterator it = constraints.iterator(); it.hasNext();) {
			Constraint currentConstraint = (Constraint) it.next();
			if (currentConstraint instanceof CheckConstraint) {
				tmpConstraints.add(currentConstraint);
			}
		}
		return tmpConstraints;
	}

	private PrimaryKey internalGetPrimaryKey(Collection constraints) {
		for (Iterator it = constraints.iterator(); it.hasNext();) {
			Constraint currentConstraint = (Constraint) it.next();
			if (currentConstraint instanceof PrimaryKey) {
				return (PrimaryKey) currentConstraint;
			}
		}

		return null;
	}
}
