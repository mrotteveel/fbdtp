package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleNewConnectionProfileWizard;

public class NewFirebirdConnectionProfileWizard extends
		ExtensibleNewConnectionProfileWizard {
	public NewFirebirdConnectionProfileWizard() {
		super(
				new FirebirdProfileDetailsWizardPage(
						"org.eclipse.datatools.enablement.firebird.ui.connection.FirebirdProfileDetailsWizardPage")); //$NON-NLS-1$
	}
}