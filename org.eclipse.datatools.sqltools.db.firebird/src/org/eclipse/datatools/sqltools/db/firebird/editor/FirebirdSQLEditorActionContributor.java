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
