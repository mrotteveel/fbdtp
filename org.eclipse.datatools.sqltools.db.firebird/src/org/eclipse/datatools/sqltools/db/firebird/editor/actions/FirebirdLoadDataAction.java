/*
 * Copyright (C) 2007 - 2009 members of the Firebird development team
 * and others.
 * This file was created by members of the Firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals. Contributors to this file are either listed here or
 * can be obtained from a source control (eg CVS) history command.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roman Rokytskyy  - Initial implementation
 */ 

package org.eclipse.datatools.sqltools.db.firebird.editor.actions;

import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataAction;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataWizard;
import org.eclipse.datatools.sqltools.db.firebird.editor.wizard.FirebirdLoadDataWizard;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
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
