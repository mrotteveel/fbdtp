package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleProfileDetailsWizardPage;
import org.eclipse.datatools.enablement.firebird.IFirebirdConnectionProfileConstants;

public class FirebirdProfileDetailsWizardPage extends
		ExtensibleProfileDetailsWizardPage {

	public FirebirdProfileDetailsWizardPage(String pageName) {
		super(pageName, IFirebirdConnectionProfileConstants.FIREBIRD_CATEGORY_ID);
	}
}