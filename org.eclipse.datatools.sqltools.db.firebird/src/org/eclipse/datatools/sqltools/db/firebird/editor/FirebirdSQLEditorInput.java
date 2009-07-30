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

import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.sqltools.core.profile.ProfileUtil;
import org.eclipse.datatools.sqltools.routineeditor.ProcEditorInput;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditorConnectionInfo;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditorStorage;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditorStorageEditorInput;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdSQLEditorInput extends SQLEditorStorageEditorInput {

    boolean _isEditable = true;

    String name;

    /**
     * 
     */
    public FirebirdSQLEditorInput(IConnectionProfile profile, String name) {
        super(name, "");
        this.name = name;
        setStorage(new SQLEditorStorage(name, ""));
        setConnectionInfo(new SQLEditorConnectionInfo(ProfileUtil
                .getDatabaseVendorDefinitionId(profile.getName()), profile
                .getName(), name));
    }

    /**
     * Judges whether this IEditorInput is editable
     * 
     * @author hpgu
     * @return _isEditable
     */
    public boolean isEditable() {
        return _isEditable;
    }

    /**
     * Set isEditable status
     * 
     * @author hpgu
     * @param isEditable
     */
    public void setEditable(boolean isEditable) {
        _isEditable = isEditable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        // XXX:
        // this is for whether to show in recent file list.
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof ProcEditorInput) {
            FirebirdSQLEditorInput input = (FirebirdSQLEditorInput) obj;
            return this.name.equals(input.name);
        }
        else {
            return false;
        }
    }

    /**
     * Convenience method for
     * ((SQLEditorStorage)getStorage()).getContentsString()
     * 
     * @return
     */
    public String getSourceCode() {
        return ((SQLEditorStorage) getStorage()).getContentsString();
    }

    /**
     * Convenience method for setStorage(new
     * SQLEditorStorage(_procIdentifier.getDisplayString(), code))
     * 
     * @param code
     */
    public void setSourceCode(String code) {
        setStorage(new SQLEditorStorage(name, code));
    }

    public boolean isConnectionRequired() {
        return true;
    }

    public String getId() {
        return getClass().getName() + "(" + name + ")";
    }

}
