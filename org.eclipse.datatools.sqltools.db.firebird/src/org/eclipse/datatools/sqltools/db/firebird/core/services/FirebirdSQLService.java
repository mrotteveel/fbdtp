package org.eclipse.datatools.sqltools.db.firebird.core.services;

import org.eclipse.datatools.connectivity.sqm.core.definition.DatabaseDefinition;
import org.eclipse.datatools.modelbase.sql.schema.SQLObject;
import org.eclipse.datatools.sqltools.core.DBHelper;
import org.eclipse.datatools.sqltools.core.services.SQLService;
import org.eclipse.datatools.sqltools.db.firebird.core.FirebirdHelper;
import org.eclipse.datatools.sqltools.db.generic.sql.GenericSQLSyntax;
import org.eclipse.datatools.sqltools.editor.template.GenericSQLContextType;
import org.eclipse.datatools.sqltools.sql.ISQLSyntax;
import org.eclipse.datatools.sqltools.sql.identifier.IIdentifierValidator;
import org.eclipse.datatools.sqltools.sql.updater.ProceduralObjectSourceUpdater;

public class FirebirdSQLService extends SQLService {

    public IIdentifierValidator getIdentifierValidator() {
        // TODO Auto-generated method stub
        return super.getIdentifierValidator();
    }

    public ProceduralObjectSourceUpdater getProceduralObjectSourceUpdater(
            SQLObject object, DatabaseDefinition dbDefinition) {
        // TODO Auto-generated method stub
        return super.getProceduralObjectSourceUpdater(object, dbDefinition);
    }

    public GenericSQLContextType getSQLContextType() {
        // TODO Auto-generated method stub
        return super.getSQLContextType();
    }

    public ISQLSyntax getSQLSyntax() {
        return new GenericSQLSyntax();
    }
    
    public DBHelper getDBHelper() {
        return new FirebirdHelper();
    }

    public String[] splitSQL(String arg0) {
        // TODO Auto-generated method stub
        return super.splitSQL(arg0);
    }
}
