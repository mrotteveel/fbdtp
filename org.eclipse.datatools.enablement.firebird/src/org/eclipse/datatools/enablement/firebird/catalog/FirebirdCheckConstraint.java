package org.eclipse.datatools.enablement.firebird.catalog;

import org.eclipse.datatools.modelbase.sql.constraints.impl.CheckConstraintImpl;
import org.eclipse.datatools.modelbase.sql.expressions.SearchCondition;
import org.eclipse.datatools.modelbase.sql.expressions.impl.SearchConditionDefaultImpl;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdCheckConstraint extends CheckConstraintImpl {

	public String getSQL() {
		SearchCondition condition = getSearchCondition();

		if (condition == null)
			return null;

		return condition.getSQL();
	}

	public void setSQL(String sql) {
		setSearchCondition(new CheckConstraintCondition(sql));
	}

	private static class CheckConstraintCondition extends
			SearchConditionDefaultImpl {

		public CheckConstraintCondition(String sql) {
			setSQL(sql);
		}
	}
}
