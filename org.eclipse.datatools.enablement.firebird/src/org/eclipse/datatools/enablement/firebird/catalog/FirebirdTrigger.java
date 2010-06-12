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
 *     Mark Rotteveel   - Code cleanup, further development
 */ 

package org.eclipse.datatools.enablement.firebird.catalog;

import org.eclipse.datatools.modelbase.sql.tables.ActionTimeType;
import org.eclipse.datatools.modelbase.sql.tables.impl.TriggerImpl;

/**
 * Trigger implementation for the Firebird database.
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 *
 */
public class FirebirdTrigger extends TriggerImpl {
    // TODO Implement database triggers (+ loading) (separate class?)

	private boolean active;
	private int position;
	
	/**
	 * Array with the supported ACTION_TIME_TYPES<br>
	 * index 0: AFTER_LITERAL<br>
	 * index 1: BEFORE_LITERAL
	 */
	private static final ActionTimeType[] ACTION_TIME_TYPE = {
	    ActionTimeType.AFTER_LITERAL, ActionTimeType.BEFORE_LITERAL };

    /*
     * type     v       s3 s2 s1 t
     * BI   -   1   -   00 00 00 1
     * BU   -   3   -   00 00 01 1
     * BD   -   5   -   00 00 10 1
     * BIU  -   17  -   00 10 00 1
     * BID  -   25  -   00 11 00 1
     * BUD  -   27  -   00 11 01 1
     * BIUD -   113 -   11 10 00 1
     * 
     * AI   -   2   -   00 00 01 0
     * AU   -   4   -   00 00 10 0
     * AD   -   6   -   00 00 11 0
     * AIU  -   18  -   00 10 01 0
     * AID  -   26  -   00 11 01 0
     * AUD  -   28  -   00 11 10 0
     * AIUD -   114 -   11 10 01 0
     * 
     * Actual value (v) depends on definition order (eg order BUID = 107, BDIU = 77)
     */
	public void setFirebirdTriggerType(int type) {
	    // Select time of trigger (t)
		final int beforeAfter = type & 0x01;
		setActionTime(ACTION_TIME_TYPE[beforeAfter]);
		
		// Transform before triggers to be identical to after triggers for further processing
		type += beforeAfter;
		// Select slot1 (s1)
		int slot1 = (type & 0x06) >>> 1; // (type & 00000110) >> 1
		// Select slot2 (s2)
		int slot2 = (type & 0x18) >>> 3; // (type & 00011000) >> 3
		// Select slot3 (s3)
		int slot3 = (type & 0x60) >>> 5; // (type & 01100000) >> 5
		
		processSlot(slot1);
		processSlot(slot2);
		processSlot(slot3);
	}
	
	/**
	 * Process the slot value and set the appropriate trigger type (update, delete or insert).
	 * 
	 * @param slot Slot value (0 : None, 1: Insert, 2: Update, 3: Delete)
	 */
	private void processSlot(int slot) {
	    switch (slot) {
	        case 0:
	            // Do nothing
	            break;
	        case 1:
	            setInsertType(true);
	            break;
	        case 2:
	            setUpdateType(true);
	            break;
	        case 3:
	            setDeleteType(true);
	            break;
            default:
                throw new IllegalArgumentException("Unexpected value for slot (expecting 0, 1, 2 or 3): " + slot);
	    }
	}

	/**
	 * 
	 * @return the state of the trigger (active/inactive)
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * 
	 * @param active the state of the trigger (active/inactive)
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Position of the trigger relative to other triggers.
	 * 
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * 
	 * @param position the position
	 */
	public void setPosition(int position) {
		this.position = position;
	}

}
