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

import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.popup.AbstractAction;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataAction;
import org.eclipse.datatools.sqltools.data.internal.ui.load.LoadDataActionProvider;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
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
