package org.apache.ddlutils.alteration;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

public class ColumnCommentChange implements ModelChange {
    private final Table targetTable;
    private final Column targetColumn;

    public ColumnCommentChange(Table targetTable, Column targetColumn) {
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }

    public String getChangedTable() {
        return targetTable.getName();
    }

    public Column getNewColumn() {
        return targetColumn;
    }

    public void apply(Database database, boolean caseSensitive) {

    }
}
