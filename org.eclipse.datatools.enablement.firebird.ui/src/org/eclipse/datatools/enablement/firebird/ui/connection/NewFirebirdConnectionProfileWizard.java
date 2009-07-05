package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.db.generic.GenericDBPlugin;
import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleNewConnectionProfileWizard;

public class NewFirebirdConnectionProfileWizard extends
        ExtensibleNewConnectionProfileWizard {

    public NewFirebirdConnectionProfileWizard() {
        super(new FirebirdProfileDetailsWizardPage("detailsPage"));
        setWindowTitle(GenericDBPlugin.getDefault().getResourceString(
                "NewConnectionProfileWizard.title")); //$NON-NLS-1$
    }
}