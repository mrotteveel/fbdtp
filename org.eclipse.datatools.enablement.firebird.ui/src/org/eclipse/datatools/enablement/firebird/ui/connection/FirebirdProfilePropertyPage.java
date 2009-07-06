package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleProfileDetailsPropertyPage;
import org.eclipse.datatools.enablement.firebird.IFBConstants;

public class FirebirdProfilePropertyPage extends
		ExtensibleProfileDetailsPropertyPage {

	public FirebirdProfilePropertyPage() {
		super(IFBConstants.FIREBIRD_CATEGORY_ID);
	}
}