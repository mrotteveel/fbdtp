package org.eclipse.datatools.enablement.firebird.catalog;

import java.sql.Connection;

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

	public void refresh() {
		synchronized (triggersMutex) {
			triggersLoaded = false;
		}
		synchronized (queryMutex) {
			queryLoaded = false;
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
       		    //TODO Externalize string
    			Activator.getDefault().getLog().log(
    					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
    							"Could not load the triggers for view "
    									+ this.getName(), e));
    		}
    		this.eSetDeliver(deliver);
	    }
	}

	public boolean isInsertable() {
		return super.isInsertable();
	}

	public boolean isUpdatable() {
		return super.isUpdatable();
	}
	
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

	public QueryExpression getQueryExpression() {
		synchronized (queryMutex) {
			if (!queryLoaded)
				loadQueryExpression();
		}
		return super.getQueryExpression();
	}

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
    		    //TODO Externalize string
    			Activator.getDefault().getLog().log(
    					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
    							"Could not load the query for view "
    									+ this.getName(), e));
    		}
    
    		this.eSetDeliver(deliver);
	    }
	}
}
