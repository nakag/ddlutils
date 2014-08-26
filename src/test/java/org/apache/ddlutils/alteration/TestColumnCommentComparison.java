package org.apache.ddlutils.alteration;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;

import java.util.List;

public class TestColumnCommentComparison extends TestComparisonBase {

    public void testAddColumn()
    {
        final String MODEL1 =
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
                        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                        "  <table name='TableA'>\n" +
                        "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
                        "  </table>\n" +
                        "</database>";
        final String MODEL2 =
                "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
                        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
                        "  <table name='TableA'>\n" +
                        "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true' description='description example'/>\n" +
                        "  </table>\n" +
                        "</database>";

        Database model1  = parseDatabaseFromString(MODEL1);
        Database model2  = parseDatabaseFromString(MODEL2);
        List changes = getPlatform(false).getChanges(model1, model2);

        assertEquals(1,
                changes.size());

        ColumnCommentChange change = (ColumnCommentChange)changes.get(0);

        assertEquals("TableA",
                change.getChangedTable());

        assertEquals("ColPK",
                change.getNewColumn().getName());
    }

}
