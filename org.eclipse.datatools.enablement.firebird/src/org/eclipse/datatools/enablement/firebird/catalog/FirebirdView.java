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
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCView;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdTableLoader;
import org.eclipse.datatools.modelbase.sql.expressions.QueryExpression;
import org.eclipse.datatools.modelbase.sql.expressions.SQLExpressionsFactory;
import org.eclipse.datatools.modelbase.sql.tables.SQLTablesPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * View implementation for Firebird
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdView extends JDBCView {

	private final Object queryMutex = new Object();
	private boolean queryLoaded = false;
	private String query;

	private final Object triggersMutex = new Object();
	private boolean triggersLoaded = false;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCView#refresh()
	 */
	public void refresh() {
		synchronized (triggersMutex) {
			triggersLoaded = false;
		}
		synchronized (queryMutex) {
			queryLoaded = false;
		}
		super.refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.modelbase.sql.tables.impl.TableImpl#getTriggers()
	 */
	public EList getTriggers() {
		synchronized (triggersMutex) {
			if (!triggersLoaded)
				loadTriggers();
		}
		return super.getTriggers();
	}

	/**
	 * Loads the triggers for this view object.
	 */
	protected void loadTriggers() {
	    synchronized (triggersMutex) {
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
    							MessageFormat.format(
    							        Activator.getResourceString("error.view.trigger.loading"),
    							        new Object[] { getName() })
    							, e));
    		}
    		this.eSetDeliver(deliver);
	    }
	}

	public boolean isInsertable() {
	    // TODO Implement firebird specific handling of insertable views
		return super.isInsertable();
	}

	public boolean isUpdatable() {
	    // TODO Implement firebird specific handling of updatable views
		return super.isUpdatable();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCView#eIsSet(org.eclipse.emf.ecore.EStructuralFeature)
	 */
    public boolean eIsSet(EStructuralFeature eFeature) {
        switch (eDerivedStructuralFeatureID(eFeature)) {
        case SQLTablesPackage.VIEW_TABLE__TRIGGERS:
            getTriggers();
            break;
        case SQLTablesPackage.VIEW_TABLE__QUERY_EXPRESSION:
            getQueryExpression();
            break;
        }
        return super.eIsSet(eFeature);
     }

    /*
     * (non-Javadoc)
     * @see org.eclipse.datatools.modelbase.sql.tables.impl.DerivedTableImpl#getQueryExpression()
     */
	 public QueryExpression getQueryExpression() {
		synchronized (queryMutex) {
			if (!queryLoaded)
				loadQueryExpression();
		}
		return super.getQueryExpression();
	 }
	 
	 /**
	  * Load the query defining this view.
	  */
	 protected void loadQueryExpression() {
	    synchronized (queryMutex) {
    		Connection connection = this.getConnection();
    
    		boolean deliver = this.eDeliver();
    		this.eSetDeliver(false);
    
    		try {
    			query = FirebirdTableLoader.loadViewQuery(connection, getSchema(),
    					this);
    			QueryExpression expression = SQLExpressionsFactory.eINSTANCE.createQueryExpressionDefault();
    			expression.setSQL(query);
    			setQueryExpression(expression);
    
    			queryLoaded = true;
    		} catch (Exception e) {
                Activator.getDefault().getLog().log(
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
                                MessageFormat.format(
                                        Activator.getResourceString("error.view.query.loading"),
                                        new Object[] { getName() })
                                , e));
    		}
    
    		this.eSetDeliver(deliver);
	    }
	 }
}
