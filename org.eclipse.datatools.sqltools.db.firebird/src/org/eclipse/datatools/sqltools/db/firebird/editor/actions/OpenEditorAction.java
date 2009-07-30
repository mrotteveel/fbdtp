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

import org.eclipse.datatools.enablement.firebird.catalog.FirebirdSchema;
import org.eclipse.datatools.sqltools.db.firebird.editor.FirebirdSQLEditor;
import org.eclipse.datatools.sqltools.db.firebird.editor.FirebirdSQLEditorInput;
import org.eclipse.datatools.sqltools.routineeditor.ui.actions.Messages;
import org.eclipse.datatools.sqltools.routineeditor.ui.actions.RoutineAction;
import org.eclipse.datatools.sqltools.sql.util.ModelUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class OpenEditorAction extends RoutineAction {

    public static final String ACTION_ID = "OpenEditorAction";

    public OpenEditorAction() {
        init();
    }

    public OpenEditorAction(Object selectedResource) {
        super(selectedResource);
        init();
    }

    protected void init() {
        setId(ACTION_ID);
        setText(Messages.EditRoutineAction_label);
        setToolTipText(Messages.EditRoutineAction_tooltip);
        // setImageDescriptor(RoutineEditorImages.getImageDescriptor("routine_editor"));
    }

    protected void initSQLObject(IAction action, Object selectedResource) {
        if (selectedResource instanceof FirebirdSchema) {
            FirebirdSchema schema = (FirebirdSchema) selectedResource;
            _sqlObject = schema;
            _database = schema.getDatabase();
            _database = ModelUtil.getDatabase(schema);
            action.setEnabled(true);
        }

        if (selectedResource instanceof EObject) {
            _dbName = ModelUtil.getDatabaseName((EObject) selectedResource);
        }
        // TODO check Routine.sourceVisible
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        run();
    }

    public void run() {
        if (_sqlObject != null && _connectionProfile != null) {
            IWorkbenchWindow activeWorkbenchWindow = 
                PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            
            IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
            try {
                FirebirdSQLEditor editor = (FirebirdSQLEditor) page.openEditor(
                    new FirebirdSQLEditorInput(_connectionProfile,
                            getDatabaseName()), FirebirdSQLEditor.ID);

            } catch (Exception e) {
                // RoutineEditorActivator.getDefault().log(e);
            }
        }
    }
}
