package org.eclipse.datatools.sqltools.db.firebird.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.datatools.sqltools.routineeditor.internal.messages";//$NON-NLS-1$

    private Messages() {
        // Do not instantiate
    }

    public static String sqleditor_SQLEditor_saveToDBActionError;
    public static String sqleditor_SQLEditor_refreshError;
    public static String sqleditor_SQLEditor_pasteSQLError;
    public static String sqleditor_SQLEditor_sqlFromPainter;
    public static String sqleditor_SQLEditorInput_initError;
    public static String sqleditor_SQLEditor_pasteSQLvariablePair;
    public static String sqleditor_SQLEditor_pasteSQLjdbcString;
    public static String sqleditor_SQLEditor_pasteSQLvarString;
    public static String sqleditor_SQLEditor_pasteSQLvariableIncorrectSyntax;
    public static String sqlEditor_SQLEditorContributor_bundleNull;
    public static String sqlEditor_SQLEditorDocumentProvider_failGetSource;
    public static String sqlEditor_SQLEditorDocumentProvider_saveDbFail;
    public static String sqlEditor_SQLEditorDocumentProvider_getTextFileBuffer;
    public static String RoutineAnnotationModel_resourcechanged;
    public static String SQLEditor_error_save_title;
    public static String SQLEditor_error_save_notsupport;
    public static String SQLEditor_profile_disconnected;
    public static String SQLEditor_profile_information;
    public static String Editor_error_save_message;
    public static String Editor_error_save_title;
    public static String Editor_warning_save_delete;
    public static String plugin_internal_error;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
}
