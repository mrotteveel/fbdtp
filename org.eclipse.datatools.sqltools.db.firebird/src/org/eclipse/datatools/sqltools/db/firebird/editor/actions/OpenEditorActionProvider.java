package org.eclipse.datatools.sqltools.db.firebird.editor.actions;

import org.eclipse.datatools.sqltools.routineeditor.ui.actions.RoutineActionProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

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
