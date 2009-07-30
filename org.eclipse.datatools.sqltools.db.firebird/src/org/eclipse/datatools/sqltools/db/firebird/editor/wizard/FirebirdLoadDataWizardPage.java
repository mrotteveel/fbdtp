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

package org.eclipse.datatools.sqltools.db.firebird.editor.wizard;

import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdLoadDataWizardPage extends LoadDataWizardPage {

    protected Spinner transactionCommitInterval;
    protected Button interrimCommit;
    protected Group interrimCommitGroup;

    public FirebirdLoadDataWizardPage(String pageName) {
        super(pageName);
    }

    protected void createControl1(Composite parent) {
        super.createControl1(parent);

        Composite c = (Composite) getControl();

        interrimCommit = new Button(c, SWT.CHECK);
        interrimCommit.setText(Messages
                .getString("FirebirdLoadDataWizardPage.IterrimCommit")); //$NON-NLS-1$
        interrimCommit.setSelection(false);
        interrimCommit.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                interrimCommitGroup.setEnabled(interrimCommit.getSelection());
            }
        });

        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        interrimCommit.setLayoutData(gd);

        interrimCommitGroup = new Group(c, SWT.NONE);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 3;
        interrimCommitGroup.setLayoutData(gd);
        interrimCommitGroup
                .setText(Messages
                        .getString("FirebirdLoadDataWizardPage.IterrimCommitProperies")); //$NON-NLS-1$
        interrimCommitGroup.setLayout(new GridLayout(2, false));
        interrimCommitGroup.setEnabled(false);

        Label l = new Label(interrimCommitGroup, SWT.NONE);
        l.setText(Messages
                .getString("FirebirdLoadDataWizardPage.CommitInterval")); //$NON-NLS-1$

        transactionCommitInterval = new Spinner(interrimCommitGroup, SWT.BORDER);
        transactionCommitInterval.setIncrement(100);
        transactionCommitInterval.setMinimum(0);
        transactionCommitInterval.setMaximum(Integer.MAX_VALUE);
        transactionCommitInterval.setSelection(1000);

        transactionCommitInterval.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_BEGINNING));
        transactionCommitInterval.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

    }

    public boolean isInterrimCommit() {
        return interrimCommit.getSelection();
    }

    public int getCommitInterval() {
        if (!isInterrimCommit())
            return 0;

        return transactionCommitInterval.getSelection();
    }
}
