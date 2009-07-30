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

package org.eclipse.datatools.enablement.firebird;

import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 * 
 */
public class FirebirdConversionUtil {

	private static Map charSets = new HashMap();
	private static Map charSetsInversed = new HashMap();
	static {
		charSets.put("NONE", Integer.valueOf(0));
		charSets.put("OCTETS", Integer.valueOf(1));
		charSets.put("ASCII", Integer.valueOf(2));
		charSets.put("UNICODE_FSS", Integer.valueOf(3));
		charSets.put("UTF8", Integer.valueOf(4));
		charSets.put("SJIS_0208", Integer.valueOf(5));
		charSets.put("EUCJ_0208", Integer.valueOf(6));
		charSets.put("DOS437", Integer.valueOf(10));
		charSets.put("DOS850", Integer.valueOf(11));
		charSets.put("DOS865", Integer.valueOf(12));
		charSets.put("ISO8859_1", Integer.valueOf(21));
		charSets.put("ISO8859_2", Integer.valueOf(22));
		charSets.put("ISO8859_3", Integer.valueOf(23));
		charSets.put("ISO8859_4", Integer.valueOf(34));
		charSets.put("ISO8859_5", Integer.valueOf(35));
		charSets.put("ISO8859_6", Integer.valueOf(36));
		charSets.put("ISO8859_7", Integer.valueOf(37));
		charSets.put("ISO8859_8", Integer.valueOf(38));
		charSets.put("ISO8859_9", Integer.valueOf(39));
		charSets.put("ISO8859_13 ", Integer.valueOf(40));
		charSets.put("DOS852", Integer.valueOf(45));
		charSets.put("DOS857", Integer.valueOf(46));
		charSets.put("DOS860", Integer.valueOf(13));
		charSets.put("DOS861", Integer.valueOf(47));
		charSets.put("DOS863", Integer.valueOf(14));
		charSets.put("CYRL", Integer.valueOf(50));
		charSets.put("DOS737", Integer.valueOf(9));
		charSets.put("DOS775", Integer.valueOf(15));
		charSets.put("DOS858", Integer.valueOf(16));
		charSets.put("DOS862", Integer.valueOf(17));
		charSets.put("DOS864", Integer.valueOf(18));
		charSets.put("DOS866", Integer.valueOf(48));
		charSets.put("DOS869", Integer.valueOf(49));
		charSets.put("WIN1250", Integer.valueOf(51));
		charSets.put("WIN1251", Integer.valueOf(52));
		charSets.put("WIN1252", Integer.valueOf(53));
		charSets.put("WIN1253", Integer.valueOf(54));
		charSets.put("WIN1254", Integer.valueOf(55));
		charSets.put("NEXT", Integer.valueOf(19));
		charSets.put("WIN1255", Integer.valueOf(58));
		charSets.put("WIN1256", Integer.valueOf(59));
		charSets.put("WIN1257", Integer.valueOf(60));
		charSets.put("KSC_5601 ", Integer.valueOf(44));
		charSets.put("BIG_5", Integer.valueOf(56));
		charSets.put("GB_2312", Integer.valueOf(57));
		charSets.put("KOI8R", Integer.valueOf(63));
		charSets.put("KOI8U", Integer.valueOf(64));
		charSets.put("WIN1258", Integer.valueOf(65));

		// create also an inverted map
		for (Iterator iter = charSets.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();

			charSetsInversed.put(entry.getValue(), entry.getKey());
		}
	};

	//TODO Add additional types
	private static final short smallint_type = 7;
	private static final short integer_type = 8;
	private static final short quad_type = 9;
	private static final short float_type = 10;
	private static final short d_float_type = 11;
	private static final short date_type = 12;
	private static final short time_type = 13;
	private static final short char_type = 14;
	private static final short int64_type = 16;
	private static final short double_type = 27;
	private static final short timestamp_type = 35;
	private static final short varchar_type = 37;
	private static final short cstring_type = 40;
	private static final short blob_type = 261;

	public static String getCharacterSet(int charSetId) {
		return (String) charSetsInversed.get(Integer.valueOf(charSetId));
	}

	public static int getJdbcType(int firebirdType) {
		switch (firebirdType) {

		case smallint_type:
			return Types.SMALLINT;

		case integer_type:
			return Types.INTEGER;

		case quad_type:
			return Types.ARRAY;

		case float_type:
			return Types.FLOAT;

		case d_float_type:
			return Types.DECIMAL;

		case date_type:
			return Types.DATE;

		case time_type:
			return Types.TIME;

		case char_type:
			return Types.CHAR;

		case int64_type:
			return Types.BIGINT;

		case double_type:
			return Types.DOUBLE;

		case timestamp_type:
			return Types.TIMESTAMP;

		case varchar_type:
			return Types.VARCHAR;

		case cstring_type:
			return Types.OTHER;

		case blob_type:
			return Types.BLOB;

		default:
			return Types.OTHER;
		}
	}

	public static String getTypeAsString(int sqltype, int sqlsubtype,
			int sqlscale) {
		// Must return the same value as DatabaseMetaData getColumns Type_Name
		if (sqlscale < 0) {
			switch (sqltype) {
			case smallint_type:
			case integer_type:
			case int64_type:
			case double_type:
				// NOTE: can't be BIGINT because of scale
				if (sqlsubtype == 2)
					return "DECIMAL";
				else
					return "NUMERIC";
			default:
				break;
			}
		}

		switch (sqltype) {
		case smallint_type:
			return "SMALLINT";

		case integer_type:
			return "INTEGER";

		case double_type:
		case d_float_type:
			return "DOUBLE PRECISION";

		case float_type:
			return "FLOAT";

		case char_type:
			return "CHAR";

		case varchar_type:
			return "VARCHAR";

		case cstring_type:
			return "CSTRING";

		case timestamp_type:
			return "TIMESTAMP";

		case time_type:
			return "TIME";

		case date_type:
			return "DATE";

		case int64_type:
			// this might need some help for long mapping
			if (sqlsubtype == 1)
				return "NUMERIC";
			else if (sqlsubtype == 2)
				return "DECIMAL";
			else
				return "BIGINT";

		case blob_type:
			return "BLOB";

		case quad_type:
			return "ARRAY";

		default:
			return "NULL";
		}
	}

	public static String getFullTypeAsString(int sqltype, int sqlsubtype,
			int sqlprecision, int sqlscale, int charLength, int charSetId) {
		String type;

		type = getTypeAsString(sqltype, sqlsubtype, sqlscale);
		
		if (sqltype != blob_type && sqlscale <0) {
		    return type + "(" + sqlprecision + ", " + (-sqlscale) + ")";
		} else {
    		switch(sqltype) {
    		    case int64_type:
    		        if (sqlsubtype == 1 || sqlsubtype == 2) {
    		            type = type + "(" + sqlprecision + ")";
    		        }
    		        break;
    		    case blob_type:
    		        // TODO Check necessary? BLOB SUB_TYPE 0 is valid.
    		        if (sqlsubtype != 0) {
    		            type = type + " SUB_TYPE " + sqlsubtype;
    		        }
    		        break;
    		    case char_type:
    		    case varchar_type:
    		    case cstring_type:
    		        type = type + "(" + charLength + ")";
    	            if (charSetId != 0) {
    	                type += " CHARACTER SET " + getCharacterSet(charSetId);
    	            }
    	            break;
    	        default:
    	            break;
    		}
		}
		return type;
	}
}
