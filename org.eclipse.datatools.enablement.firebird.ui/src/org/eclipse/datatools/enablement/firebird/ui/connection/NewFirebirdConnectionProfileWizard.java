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
 *     Mark Rotteveel  - Initial implementation
 */ 

package org.eclipse.datatools.enablement.firebird.ui.connection;

import org.eclipse.datatools.connectivity.db.generic.GenericDBPlugin;
import org.eclipse.datatools.connectivity.ui.wizards.ExtensibleNewConnectionProfileWizard;

/**
 * 
 * @author Mark Rotteveel
 *
 */
public class NewFirebirdConnectionProfileWizard extends
        ExtensibleNewConnectionProfileWizard {

    public NewFirebirdConnectionProfileWizard() {
        super(new FirebirdProfileDetailsWizardPage("detailsPage"));
        setWindowTitle(GenericDBPlugin.getDefault().getResourceString(
                "NewConnectionProfileWizard.title")); //$NON-NLS-1$
    }
}