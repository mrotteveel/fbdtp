package org.eclipse.datatools.enablement.firebird.catalog;

import java.sql.Connection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCView;
import org.eclipse.datatools.enablement.firebird.Activator;
import org.eclipse.datatools.enablement.firebird.catalog.loader.FirebirdTableLoader;
import org.eclipse.datatools.modelbase.sql.expressions.QueryExpression;
import org.eclipse.datatools.modelbase.sql.expressions.impl.QueryExpressionDefaultImpl;
import org.eclipse.emf.common.util.EList;

/**
 * 
 * @author Roman Rokytskyy
 * 
 */
public class FirebirdView extends JDBCView {

// TODO Remove or find usage
//	private boolean showSystemObjects;

	private Object queryMutex = new Object();
	private boolean queryLoaded = false;
	private String query;

	private Object triggersMutex = new Object();
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
		EList triggerList = super.getTriggers();
		triggerList.clear();
		Connection connection = this.getConnection();

		boolean deliver = this.eDeliver();
		this.eSetDeliver(false);

		try {

			FirebirdTableLoader.loadTriggers(connection, getSchema(), this,
					triggerList);

			this.triggersLoaded = true;

		} catch (Exception e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
							"Could not load the triggers for table "
									+ this.getName(), e));
		}

		this.eSetDeliver(deliver);
	}

	public boolean isInsertable() {
		return super.isInsertable();
	}

	public boolean isUpdatable() {
		return super.isUpdatable();
	}

	public QueryExpression getQueryExpression() {
		synchronized (queryMutex) {
			if (!queryLoaded)
				loadQueryExpression();
		}

		return super.getQueryExpression();
	}

	protected void loadQueryExpression() {
		Connection connection = this.getConnection();

		boolean deliver = this.eDeliver();
		this.eSetDeliver(false);

		try {

			query = FirebirdTableLoader.loadViewQuery(connection, getSchema(),
					this);

			setQueryExpression(new ViewQueryExpression(query));

			this.queryLoaded = true;

		} catch (Exception e) {
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
							"Could not load the query for view "
									+ this.getName(), e));
		}

		this.eSetDeliver(deliver);
	}

	private static class ViewQueryExpression extends QueryExpressionDefaultImpl {

		public ViewQueryExpression(String query) {
			super();

			setSQL(query);
		}

	}
}
