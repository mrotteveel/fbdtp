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
