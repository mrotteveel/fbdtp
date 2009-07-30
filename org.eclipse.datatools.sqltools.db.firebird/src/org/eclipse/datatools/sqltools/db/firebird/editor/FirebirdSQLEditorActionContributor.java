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

import org.eclipse.datatools.sqltools.sqleditor.ISQLEditorActionConstants;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditor;
import org.eclipse.datatools.sqltools.sqleditor.internal.SQLEditorActionContributor;
import org.eclipse.datatools.sqltools.sqleditor.internal.SQLEditorResources;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdSQLEditorActionContributor extends
        SQLEditorActionContributor {

    private RetargetTextEditorAction execSQLActionTarget;

    public FirebirdSQLEditorActionContributor() {
        super();

        ResourceBundle bundle = SQLEditorResources.getResourceBundle();
        execSQLActionTarget = new RetargetTextEditorAction(bundle,
                ISQLEditorActionConstants.EXECUTE_SQL_ACTION_ID);
    }

    public void contributeToCoolBar(ICoolBarManager coolBarManager) {
        super.contributeToCoolBar(coolBarManager);

        coolBarManager.add(execSQLActionTarget);
    }

    public void contributeToToolBar(IToolBarManager toolBarManager) {
        super.contributeToToolBar(toolBarManager);

        toolBarManager.add(execSQLActionTarget);
    }

    public void setActiveEditor(IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);

        if (!(targetEditor instanceof SQLEditor))
            return;

        SQLEditor editor = (SQLEditor) targetEditor;

        execSQLActionTarget.setAction(getAction(editor,
                ISQLEditorActionConstants.EXECUTE_SQL_ACTION_ID));
    }

}
