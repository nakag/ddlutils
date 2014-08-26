package org.apache.ddlutils.alteration;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

public class TableCommentChange implements ModelChange {
    private final Table targetTable;

    public TableCommentChange(Table targetTable) {
        this.targetTable = targetTable;
    }

    public Table getModifiedTable() {
        return targetTable;
    }

    public void apply(Database database, boolean caseSensitive) {

    }
}
