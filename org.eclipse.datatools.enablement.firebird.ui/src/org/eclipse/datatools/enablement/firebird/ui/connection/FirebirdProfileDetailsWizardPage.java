package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleProfileDetailsWizardPage;
import org.eclipse.datatools.enablement.firebird.IFBConstants;

public class FirebirdProfileDetailsWizardPage extends
		ExtensibleProfileDetailsWizardPage {

	public FirebirdProfileDetailsWizardPage(String pageName) {
		super(pageName, IFBConstants.FIREBIRD_CATEGORY_ID);
	}
}