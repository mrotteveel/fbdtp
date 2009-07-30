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

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.datatools.sqltools.data.internal.core.DataCorePlugin;
import org.eclipse.datatools.sqltools.data.internal.core.load.DataFileTokenizer;
import org.eclipse.datatools.sqltools.data.internal.core.load.DataFormatException;
import org.eclipse.datatools.sqltools.data.internal.core.load.LoadData;
import org.eclipse.datatools.sqltools.data.internal.core.load.TableLoader;

/**
 * 
 * @author Roman Rokytskyy
 *
 */
public class TransactionalLoadData extends LoadData {

    protected int commitIterval;

    public TransactionalLoadData(Table table, String filePath, String colDelim,
            String stringDelim, boolean replace, int commitInterval) {
        super(table, filePath);
        setDelims(colDelim, stringDelim);
        setReplace(replace);
    }

    public void doLoad1() throws Exception {
        DataFileTokenizer tokens = new DataFileTokenizer(filePath, colDelim
                + stringDelim);
        loader = new TransactionalTableLoader(table, commitIterval);

        boolean commit = true;
        try {
            tokens.open();
            loader.open();
            if (replace)
                loader.emptyTable();
            parseFile(tokens);
        }
        catch (Exception ex) {
            commit = false;
            throw ex;
        }
        finally {
            tokens.close();
            ((TransactionalTableLoader) loader).close(commit);
        }
    }

    private static class TransactionalTableLoader extends TableLoader {

        private boolean oldAutoCommit;
        private int commitInterval;
        private int loadedRows;

        public TransactionalTableLoader(Table table, int commitInterval) {
            super(table);
            this.commitInterval = commitInterval;
        }

        public void close() throws SQLException {
            close(true);
        }

        public void close(boolean commit) throws SQLException {
            if (commit)
                con.commit();
            else
                con.rollback();

            if (insertStmt != null)
                insertStmt.close();

            con.setAutoCommit(oldAutoCommit);
        }

        public void open() throws SQLException {
            con = ((ICatalogObject) table).getConnection();

            this.oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            tableName = DataCorePlugin.getQualifiedTableName(table);

            String q = "insert into " + tableName + " values(?"; //$NON-NLS-1$ //$NON-NLS-2$
            for (int i = 0; i < table.getColumns().size() - 1; ++i)
                q += ",?"; //$NON-NLS-1$
            q += ")"; //$NON-NLS-1$

            insertStmt = con.prepareStatement(q);

            Statement stmt = con.createStatement();
            try {
                ResultSetMetaData md = stmt.executeQuery(
                        "select * from " + tableName).getMetaData(); //$NON-NLS-1$
                int cc = md.getColumnCount();
                colNames = new String[cc];
                colTypes = new int[cc];
                for (int i = 0; i < colNames.length; ++i) {
                    colNames[i] = md.getColumnName(i + 1);
                    colTypes[i] = md.getColumnType(i + 1);
                }
            }
            finally {
                stmt.close();
            }

            this.loadedRows = 0;
        }

        public void loadRow(String[] row) throws SQLException,
                DataFormatException, IOException {
            super.loadRow(row);

            loadedRows++;

            if (commitInterval > 0 && (loadedRows % commitInterval == 0))
                con.commit();
        }
    }
}
