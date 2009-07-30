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

package org.eclipse.datatools.sqltools.db.firebird.editor;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.sqltools.editor.core.connection.ISQLEditorConnectionInfo;
import org.eclipse.datatools.sqltools.sql.parser.SQLParserConstants;
import org.eclipse.datatools.sqltools.sqleditor.EditorConstants;
import org.eclipse.datatools.sqltools.sqleditor.ISQLEditorActionConstants;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditor;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditorStorageEditorInput;
import org.eclipse.datatools.sqltools.sqleditor.internal.SQLEditorPlugin;
import org.eclipse.datatools.sqltools.sqleditor.internal.actions.ExecuteSQLAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdSQLEditor extends SQLEditor {

    public static final String ID = "org.eclipse.datatools.sqltools.db.firebird.sqleditor.FirebirdSQLEditor";

    private ExecuteSQLAction execSQLAction;

    public FirebirdSQLEditor() {
        super();

        execSQLAction = new ExecuteSQLAction(this);
    }

    protected void createActions() {
        super.createActions();
        IActionBars bars = ((IEditorSite) getSite()).getActionBars();

        // replace the execution action with our own
        setAction(ISQLEditorActionConstants.EXECUTE_SQL_ACTION_ID,
            execSQLAction);
        bars.setGlobalActionHandler(
            ISQLEditorActionConstants.EXECUTE_SQL_ACTION_ID,
            getAction(ISQLEditorActionConstants.EXECUTE_SQL_ACTION_ID));

    }

    protected void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
    }

    public void doSave(IProgressMonitor monitor) {
        if (getEditorInput() instanceof FirebirdSQLEditorInput) {
            // This is caused by closing the connection profile without saving
            // the
            // routine
            if (!isConnected()) {
                doSaveAs();
            } else {
                super.doSave(monitor);
            }
        }

    }

    public void doSaveAs() {
        ISQLEditorConnectionInfo connInfo = getConnectionInfo();
        Shell shell = getSite().getShell();
        IProgressMonitor progressMonitor = getProgressMonitor();
        IEditorInput input = getEditorInput();

        SaveAsDialog dialog = new SaveAsDialog(shell);

        if (input instanceof SQLEditorStorageEditorInput) {
            dialog.setOriginalName(((SQLEditorStorageEditorInput) input).getName()
                    + ".sql");
        }

        dialog.create();

        IDocumentProvider provider = getDocumentProvider();
        if (provider == null) {
            // editor has programmatically been closed while the dialog was open
            return;
        }

        if (dialog.open() == Window.CANCEL) {
            if (progressMonitor != null) {
                progressMonitor.setCanceled(true);
            }
            return;
        }

        IPath filePath = dialog.getResult();
        if (filePath == null) {
            if (progressMonitor != null) {
                progressMonitor.setCanceled(true);
            }
            return;
        }

        boolean success = false;
        IDocumentProvider newProvider = null;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IFile file = workspace.getRoot().getFile(filePath);
        final IEditorInput newInput = new FileEditorInput(file);
        try {
            // InputStream inputStream = ((SQLEditorStorageEditorInput)
            // input).getStorage().getContents();
            // if (file.exists())
            // {
            // //overwrite
            // file.setContents(inputStream, true, false, progressMonitor);
            // inputStream.close();
            // }
            // else
            // {
            // file.create(inputStream, true, progressMonitor);
            // }
            IWorkbenchPage page = SQLEditorPlugin.getActiveWorkbenchPage();
            SQLEditor editor = (SQLEditor) page.openEditor(newInput,
                EditorConstants.SQLFILE_EDITOR_ID);
            newProvider = editor.getDocumentProvider();
            newProvider.aboutToChange(newInput);
            IDocument newDoc = newProvider.getDocument(newInput);
            newDoc.set(provider.getDocument(input).get());
            newProvider.saveDocument(progressMonitor, newInput, newDoc, true);
            editor.setConnectionInfo(connInfo);
            this.close(false);
            success = true;
        } catch (CoreException x) {
            IStatus status = x.getStatus();
            if (status == null || status.getSeverity() != IStatus.CANCEL) {
                String title = Messages.Editor_error_save_title; //$NON-NLS-1$
                String msg = NLS.bind(Messages.Editor_error_save_message,
                    new Object[] { x.getMessage()}); //$NON-NLS-1$

                if (status != null) {
                    switch (status.getSeverity()) {
                        case IStatus.INFO:
                            MessageDialog.openInformation(shell, title, msg);
                            break;
                        case IStatus.WARNING:
                            MessageDialog.openWarning(shell, title, msg);
                            break;
                        default:
                            MessageDialog.openError(shell, title, msg);
                    }
                } else {
                    MessageDialog.openError(shell, title, msg);
                }
            }
        } catch (Exception x) {
            String title = Messages.Editor_error_save_title; //$NON-NLS-1$
            String msg = NLS.bind(Messages.Editor_error_save_message,
                new Object[] { x.getMessage()}); //$NON-NLS-1$
            MessageDialog.openError(shell, title, msg);
        } finally {
            newProvider.changed(newInput);
        }

        if (progressMonitor != null) {
            progressMonitor.setCanceled(!success);
        }
    }

    public int getSQLType() {
        /** The default sqlType is root */
        int sqlType = SQLParserConstants.TYPE_SQL_ROOT;
        return sqlType;
    }

    /**
     * Gets the resource bundle associated with this editor.
     * 
     * @return the resource bundle associated with this editor.
     */
    public ResourceBundle getConstructedResourceBundle() {
        return org.eclipse.datatools.sqltools.routineeditor.ui.actions.Messages.getResourceBundle();
    }

    public void setConnectionInfo(ISQLEditorConnectionInfo connInfo) {
        super.setConnectionInfo(connInfo);
    }

    public boolean isConnected() {
        return super.isConnected();
    }

}
