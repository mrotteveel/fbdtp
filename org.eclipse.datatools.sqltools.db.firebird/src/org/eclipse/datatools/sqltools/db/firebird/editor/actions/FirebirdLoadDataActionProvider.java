package org.eclipse.datatools.sqltools.db.firebird.editor.actions;

import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.popup.AbstractAction;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataAction;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataActionProvider;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class FirebirdLoadDataActionProvider extends LoadDataActionProvider {
    public static final String DATA_MENU_ID = 
        "org.eclipse.datatools.connectivity.sqm.server.ui.data"; //$NON-NLS-1$

    private static final AbstractAction action = new FirebirdLoadDataAction();
    private static final AbstractAction oldAction = new LoadDataAction();

    protected String getSubMenuId()
    {
        return DATA_MENU_ID;
    }

    protected AbstractAction getAction()
    {
        return action;
    }

    protected ActionContributionItem getActionContributionItem()
    {
        return ITEM;
    }
    
    public void fillContextMenu(IMenuManager menu)
    {
        IMenuManager subMenu = (IMenuManager) menu.find(getSubMenuId());
        getAction().setCommonViewer(this.viewer);
        getAction().selectionChanged(new SelectionChangedEvent(this.selectionProvider, this.getContext().getSelection()));
        subMenu.remove(new ActionContributionItem(oldAction));
        subMenu.add(getActionContributionItem());
    }
}
