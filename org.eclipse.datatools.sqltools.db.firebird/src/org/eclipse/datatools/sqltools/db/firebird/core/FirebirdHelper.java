package org.eclipse.datatools.sqltools.db.firebird.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.sqltools.core.DBHelper;
import org.eclipse.datatools.sqltools.core.DatabaseIdentifier;
import org.eclipse.datatools.sqltools.core.ProcIdentifier;

public class FirebirdHelper extends DBHelper {
    
    public ProcIdentifier getProcIdentifier(DatabaseIdentifier databaseIdentifier, String dbObjectName, int dbObjectType, String tableName, String ownerName) {
        Map map = new HashMap();

        //don't put it null values which will cause problem when encoding/decoding
        if (ownerName != null)
        {
            map.put(ProcIdentifier.PROP_OWNER, ownerName);
        }
        if (dbObjectName != null)
        {
            map.put(ProcIdentifier.PROP_NAME, dbObjectName);
        }
        if (tableName != null)
        {
            map.put(ProcIdentifier.PROP_TABLENAME, tableName);
        }

        return new FirebirdProcIdentifier(dbObjectType, databaseIdentifier, map);
    }
}
