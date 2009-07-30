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

package org.eclipse.datatools.enablement.firebird.catalog;

import org.eclipse.datatools.modelbase.sql.tables.ActionTimeType;
import org.eclipse.datatools.modelbase.sql.tables.impl.TriggerImpl;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class FirebirdTrigger extends TriggerImpl {

	private boolean active;
	private int position;

    /* 
	    BI -    1 -     00 00 00 1 
	    BIU -   17 -    00 10 00 1 
	    BIUD -  113 -   11 10 00 1  
	    BUD -   27 -    00 11 01 1 
	    BU -    3 -     00 00 01 1 
	    BD -    5 -     00 00 10 1 
	    
	    AI -    2 -     00 00 01 0
	    AIU -   18 -    00 10 01 0
	    AIUD -  114 -   11 10 01 0
	    AUD -   28 -    00 11 10 0
	    AU -    4 -     00 00 10 0
	    AD -    6 -     00 00 11 0
    */
	public void setFirebirdTriggerType(int type) {
		int beforeAfter = type & 0x01;
		type = type + 1;
		int slot1 = (type & 0x06) >>> 1; // (type & 00000110) >> 1
		int slot2 = (type & 0x18) >>> 3; // (type & 00011000) >> 3
		int slot3 = (type & 0x60) >>> 5; // (type & 01100000) >> 5

		setActionTime(beforeAfter == 1 ? ActionTimeType.BEFORE_LITERAL
				: ActionTimeType.AFTER_LITERAL);

		if (slot1 == 1 || slot2 == 1 || slot3 == 1)
			setInsertType(true);
		else if (slot1 == 2 || slot2 == 2 || slot3 == 2)
			setUpdateType(true);
		else if (slot1 == 3 || slot2 == 3 || slot3 == 3)
			setDeleteType(true);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
