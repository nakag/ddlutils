package org.apache.ddlutils.platform;

/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;

/**
 * Reads a database model from a Derby database.
 *
 * @author Thomas Dudziak
 * @version $Revision: $
 */
public class DerbyModelReader extends JdbcModelReader
{
    /**
     * {@inheritDoc}
     */
    protected Column readColumn(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Column column       = super.readColumn(metaData, values);
        String defaultValue = column.getDefaultValue();

        if (defaultValue != null)
        {
            // we check for these strings
            //   GENERATED_BY_DEFAULT               -> 'GENERATED BY DEFAULT AS IDENTITY'
            //   AUTOINCREMENT: start 1 increment 1 -> 'GENERATED ALWAYS AS IDENTITY'
            if ("GENERATED_BY_DEFAULT".equals(defaultValue) || defaultValue.startsWith("AUTOINCREMENT:"))
            {
                column.setDefaultValue(null);
                column.setAutoIncrement(true);
            }
        }
        return column;
    }

    /**
     * {@inheritDoc}
     */
    protected Table readTable(DatabaseMetaDataWrapper metaData, Map values) throws SQLException
    {
        Table    table       = super.readTable(metaData, values);
        Column[] pks         = table.getPrimaryKeyColumns();
        List     columnNames = new ArrayList();

        for (int columnIdx = 0; columnIdx < pks.length; columnIdx++)
        {
            columnNames.add(pks[columnIdx].getName());
        }

        // Derby returns a unique index for the pk which we don't want however
        int indexIdx = findMatchingInternalIndex(table, columnNames, true);

        if (indexIdx >= 0)
        {
            table.removeIndex(indexIdx);
        }

        // Likewise, Derby returns a non-unique index for every foreign key
        for (int fkIdx = 0; fkIdx < table.getForeignKeyCount(); fkIdx++)
        {
            ForeignKey fk = table.getForeignKey(fkIdx);

            columnNames.clear();
            for (int columnIdx = 0; columnIdx < fk.getReferenceCount(); columnIdx++)
            {
                columnNames.add(fk.getReference(columnIdx).getLocalColumnName());
            }
            indexIdx = findMatchingInternalIndex(table, columnNames, false);
            if (indexIdx >= 0)
            {
                table.removeIndex(indexIdx);
            }
        }
        return table;
    }

    /**
     * Tries to find an internal index that matches the given columns.
     * 
     * @param table              The table
     * @param columnsToSearchFor The names of the columns that the index should be for
     * @param unique             Whether to search for an unique index
     * @return The position of the index or <code>-1</code> if no such index was found
     */
    private int findMatchingInternalIndex(Table table, List columnsToSearchFor, boolean unique)
    {
        for (int indexIdx = 0; indexIdx < table.getIndexCount(); indexIdx++)
        {
            Index index = table.getIndex(indexIdx);

            if ((unique == index.isUnique()) && (index.getColumnCount() == columnsToSearchFor.size()))
            {
                boolean found = true;

                for (int columnIdx = 0; found && (columnIdx < index.getColumnCount()); columnIdx++)
                {
                    if (!columnsToSearchFor.get(columnIdx).equals(index.getColumn(columnIdx).getName()))
                    {
                        found = false;
                    }
                }

                // if the index seems to be internal, we immediately return it
                if (found && mightBeInternalIndex(index))
                {
                    return indexIdx;
                }
            }
        }
        return -1;
    }

    /**
     * Guesses whether the index might be an internal index, i.e. one created by Derby.
     * 
     * @param index The index to check
     * @return <code>true</code> if the index seems to be an internal one
     */
    private boolean mightBeInternalIndex(Index index)
    {
        String name = index.getName();

        // Internal names normally have the form "SQL051228005030780"
        if ((name != null) && name.startsWith("SQL"))
        {
            try
            {
                Long.parseLong(name.substring(3));
                return true;
            }
            catch (NumberFormatException ex)
            {
                // we ignore it
            }
        }
        return false;
    }
}
