package org.eclipse.datatools.sqltools.db.firebird.core;

import java.util.Map;

import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.ProcIdentifierImpl;

public class FirebirdProcIdentifier extends ProcIdentifierImpl {

    public FirebirdProcIdentifier(int type, DatabaseIdentifier db, Map map) {
        super(type, db, map);
    }

    public String getCallableStringWithoutGroupNumber(boolean quoted_id) {
        // String call = super.getCallableStringWithoutGroupNumber(quoted_id);
        // Firebird doesn't support catalog name
        // return call.substring(call.indexOf(".")+ 1);
        if (quoted_id)
            return "\"" + getProcName() + "\"";
        else
            return getProcName();
    }

    public String getLongDisplayString() {
        String s = "";

        if (getType() == TYPE_TRIGGER && getTableName() != null
                && getTableName().length() > 0) {
            s = "(" + getProfileName() + ")" + getOwnerName() + "."
                    + getTableName() + "." + getDisplayString();
        }
        else {
            s = "(" + getProfileName() + ")" + getOwnerName() + "."
                    + getDisplayString();
        }
        return s;
    }
}
