package org.eclipse.datatools.enablement.firebird;

import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Roman Rokytskyy
 * 
 */
public class FirebirdConversionUtil {

	private static Map charSets = new HashMap();
	private static Map charSetsInversed = new HashMap();
	static {
		charSets.put("NONE", new Integer(0));
		charSets.put("OCTETS", new Integer(1));
		charSets.put("ASCII", new Integer(2));
		charSets.put("UNICODE_FSS", new Integer(3));
		charSets.put("UTF8", new Integer(4));
		charSets.put("SJIS_0208", new Integer(5));
		charSets.put("EUCJ_0208", new Integer(6));
		charSets.put("DOS437", new Integer(10));
		charSets.put("DOS850", new Integer(11));
		charSets.put("DOS865", new Integer(12));
		charSets.put("ISO8859_1", new Integer(21));
		charSets.put("ISO8859_2", new Integer(22));
		charSets.put("ISO8859_3", new Integer(23));
		charSets.put("ISO8859_4", new Integer(34));
		charSets.put("ISO8859_5", new Integer(35));
		charSets.put("ISO8859_6", new Integer(36));
		charSets.put("ISO8859_7", new Integer(37));
		charSets.put("ISO8859_8", new Integer(38));
		charSets.put("ISO8859_9", new Integer(39));
		charSets.put("ISO8859_13 ", new Integer(40));
		charSets.put("DOS852", new Integer(45));
		charSets.put("DOS857", new Integer(46));
		charSets.put("DOS860", new Integer(13));
		charSets.put("DOS861", new Integer(47));
		charSets.put("DOS863", new Integer(14));
		charSets.put("CYRL", new Integer(50));
		charSets.put("DOS737", new Integer(9));
		charSets.put("DOS775", new Integer(15));
		charSets.put("DOS858", new Integer(16));
		charSets.put("DOS862", new Integer(17));
		charSets.put("DOS864", new Integer(18));
		charSets.put("DOS866", new Integer(48));
		charSets.put("DOS869", new Integer(49));
		charSets.put("WIN1250", new Integer(51));
		charSets.put("WIN1251", new Integer(52));
		charSets.put("WIN1252", new Integer(53));
		charSets.put("WIN1253", new Integer(54));
		charSets.put("WIN1254", new Integer(55));
		charSets.put("NEXT", new Integer(19));
		charSets.put("WIN1255", new Integer(58));
		charSets.put("WIN1256", new Integer(59));
		charSets.put("WIN1257", new Integer(60));
		charSets.put("KSC_5601 ", new Integer(44));
		charSets.put("BIG_5", new Integer(56));
		charSets.put("GB_2312", new Integer(57));
		charSets.put("KOI8R", new Integer(63));
		charSets.put("KOI8U", new Integer(64));
		charSets.put("WIN1258", new Integer(65));

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
		return (String) charSetsInversed.get(new Integer(charSetId));
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

		if (sqlscale < 0 && sqltype != blob_type) {
			type = type + "(" + sqlprecision + ", " + (-sqlscale) + ")";
		} else if (sqltype == int64_type
				&& (sqlsubtype == 1 || sqlsubtype == 2)) {
			type = type + "(" + sqlprecision + ")";
		} else if (sqltype == blob_type && sqlsubtype != 0) {
			type = type + " SUB_TYPE " + sqlsubtype;
		} else if (sqltype == char_type || sqltype == varchar_type
				|| sqltype == cstring_type) {
			type = type + "(" + charLength + ")";
			if (charSetId != 0)
				type += " CHARACTER SET " + getCharacterSet(charSetId);
		}

		return type;
	}
}
