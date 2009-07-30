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

import org.eclipse.datatools.sqltools.routineeditor.ui.actions.RoutineActionProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class OpenEditorActionProvider extends RoutineActionProvider {

    public int i;

    public OpenEditorActionProvider() {
        i = 0; // debug breakpoint
    }

    public void fillContextMenu(IMenuManager manager) {
        IStructuredSelection selection = getSelection();
        if (selection == null)
            return;

        Object firstElement = selection.getFirstElement();
        if (firstElement != null) {
            OpenEditorAction routineAction = new OpenEditorAction(firstElement);
            manager.insertAfter("slot2", (IAction) routineAction);
        }
    }

}
