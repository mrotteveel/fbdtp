package org.eclipse.datatools.sqltools.db.firebird.editor.actions;

import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataAction;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataWizard;
import org.eclipse.datatools.sqltools.db.firebird.editor.wizard.FirebirdLoadDataWizard;
import org.eclipse.jface.wizard.WizardDialog;

public class FirebirdLoadDataAction extends LoadDataAction {

    public FirebirdLoadDataAction() {
        super();
    }

    public void run() {
        if (table == null)
            return;

        LoadDataWizard wiz = new FirebirdLoadDataWizard(table);
        WizardDialog dialog = new WizardDialog(org.eclipse.swt.widgets.Display.getCurrent().getActiveShell(), wiz);
        dialog.create();
        dialog.open();
    }

}
