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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.datatools.sqltools.data.internal.core.load.LoadData;
import org.eclipse.datatools.sqltools.data.internal.ui.OutputItemAdapter;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataWizard;
import org.eclipse.datatools.sqltools.data.internal.ui.load.Messages;
import org.eclipse.datatools.sqltools.db.firebird.editor.actions.TransactionalLoadData;
import org.eclipse.datatools.sqltools.result.OperationCommand;
import org.eclipse.datatools.sqltools.result.ResultsViewAPI;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdLoadDataWizard extends LoadDataWizard {

    public FirebirdLoadDataWizard(Table table) {
        super(table);
    }

    public void addPages() {
        addPage(new FirebirdLoadDataWizardPage(
                "org.eclipse.wst.rdb.data.ui.loadData"));
    }

    public String getWindowTitle() {
        return super.getWindowTitle() + " (Firebird)";
    }

    public boolean performFinish() {
        FirebirdLoadDataWizardPage page = (FirebirdLoadDataWizardPage) this.page;

        page.saveSettings();

        final LoadData load = new TransactionalLoadData(table, page
                .getFilePath(), page.getColumnDelimiter(), page
                .getStringDelimiter(), page.getReplace(), page
                .getCommitInterval());

        final OperationCommand item = initDbOutputItem();

        Job job = new Job(Messages.getString("LoadDataWizard.DataLoading")) { //$NON-NLS-1$
            protected IStatus run(IProgressMonitor monitor) {
                int ret = load.doLoad(new OutputItemAdapter(item));
                ResultsViewAPI.getInstance().updateStatus(item, ret);
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.LONG);
        job.schedule();

        return true;
    }

}
