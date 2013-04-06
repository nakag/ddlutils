package org.apache.ddlutils.platform.sqlite3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.util.Map;
import java.sql.Types;

import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.model.ModelException;
import org.apache.ddlutils.model.TypeMap;

/**
 * The SQL Builder for SQLite3.
 * 
 * @version $Revision$
 */
public class SQLite3Builder extends SqlBuilder
{
    /**
     * Creates a new builder instance.
     * 
     * @param platform The plaftform this builder belongs to
     */
    public SQLite3Builder(Platform platform)
    {
        super(platform);
        addEscapedCharSequence("'", "''");
    }

    public void dropTable(Table table) throws IOException {
        print("DROP TABLE IF EXISTS ");
        printIdentifier(getTableName(table));
        printEndOfStatement();
    }


    /**
     * {@inheritDoc}
     */
    public void dropIndex(Table table, Index index) throws IOException
    {
        print("DROP INDEX IF EXISTS ");
        printIdentifier(getIndexName(index));
        printEndOfStatement();
    }

    /**
     * {@inheritDoc}
     */
    public void createTable(Database database, Table table, Map parameters) throws IOException
    {
        for (int idx = 0; idx < table.getColumnCount(); idx++)
        {
            Column column = table.getColumn(idx);
        }
        super.createTable(database, table, parameters);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException
    {
        if (!column.isPrimaryKey()){
            print("PRIMARY KEY AUTOINCREMENT");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectLastIdentityValues(Table table)
    {
        Column[] columns = table.getAutoIncrementColumns();

        if (columns.length == 0)
        {
            return null;
        }
        else
        {
            StringBuffer result = new StringBuffer();
    
            result.append("SELECT ");
            for (int idx = 0; idx < columns.length; idx++)
            {
                if (idx > 0)
                {
                    result.append(", ");
                }
                result.append("currval('");
                result.append(getDelimitedIdentifier(getConstraintName(null, table, columns[idx].getName(), "seq")));
                result.append("') AS ");
                result.append(getDelimitedIdentifier(columns[idx].getName()));
            }
            return result.toString();
        }
    }

    /**
     * Writes the SQL to drop a column.
     * 
     * @param table  The table
     * @param column The column to drop
     */
    public void dropColumn(Table table, Column column) throws IOException
    {
        print("ALTER TABLE ");
        printlnIdentifier(getTableName(table));
        printIndent();
        print("DROP COLUMN ");
        printIdentifier(getColumnName(column));
        printEndOfStatement();
    }
    
    protected void writeColumnDefaultValueStmt(Table table, Column column) throws IOException {
        Object parsedDefault = column.getParsedDefaultValue();

        if (parsedDefault != null) {
            if (!getPlatformInfo().isDefaultValuesForLongTypesSupported()
                    && ((column.getTypeCode() == Types.LONGVARBINARY) || (column.getTypeCode() == Types.LONGVARCHAR))) {
                throw new ModelException(
                        "The platform does not support default values for LONGVARCHAR or LONGVARBINARY columns");
            }
            // we write empty default value strings only if the type is not a
            // numeric or date/time type
            if (isValidDefaultValue(column.getDefaultValue(), column.getTypeCode())) {
                print(" DEFAULT ");
                writeColumnDefaultValue(table, column);
            }
        } else if (getPlatformInfo().isDefaultValueUsedForIdentitySpec() && column.isAutoIncrement()) {
            print(" DEFAULT ");
            writeColumnDefaultValue(table, column);
        } else if (!StringUtils.isBlank(column.getDefaultValue())) {
            print(" DEFAULT ");
            writeColumnDefaultValue(table, column);
        }
    }
    
    protected void printDefaultValue(Object defaultValue, int typeCode) throws IOException {
        if (defaultValue != null) {
            String defaultValueStr = defaultValue.toString();
            boolean shouldUseQuotes = !TypeMap.isNumericType(typeCode) && !defaultValueStr.startsWith("TO_DATE(")
                    && !defaultValue.equals("CURRENT_TIMESTAMP") && !defaultValue.equals("CURRENT_TIME")
                    && !defaultValue.equals("CURRENT_DATE");
            ;

            if (shouldUseQuotes) {
                // characters are only escaped when within a string literal
                print(getPlatformInfo().getValueQuoteToken());
                print(escapeStringValue(defaultValueStr));
                print(getPlatformInfo().getValueQuoteToken());
            } else {
                print(defaultValueStr);
            }
        }
    }    
}
