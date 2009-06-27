package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleProfileDetailsPropertyPage;
import org.eclipse.datatools.enablement.firebird.IFirebirdConnectionProfileConstants;

public class FirebirdProfilePropertyPage extends
		ExtensibleProfileDetailsPropertyPage {

	public FirebirdProfilePropertyPage() {
		super(IFirebirdConnectionProfileConstants.FIREBIRD_CATEGORY_ID);
	}
}