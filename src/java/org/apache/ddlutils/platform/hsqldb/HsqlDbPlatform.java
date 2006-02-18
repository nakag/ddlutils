package org.apache.ddlutils.platform.hsqldb;

/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.sql.Connection;
import java.sql.Types;

import org.apache.ddlutils.DynaSqlException;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.platform.PlatformImplBase;

/**
 * The platform implementation for the HsqlDb database.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class HsqlDbPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "HsqlDb";
    /** The standard Hsqldb jdbc driver. */
    public static final String JDBC_DRIVER      = "org.hsqldb.jdbcDriver";
    /** The subprotocol used by the standard Hsqldb driver. */
    public static final String JDBC_SUBPROTOCOL = "hsqldb";

    /**
     * Creates a new instance of the Hsqldb platform.
     */
    public HsqlDbPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setSupportingNonPKIdentityColumns(false);

        info.addNativeTypeMapping(Types.BIT,         "BOOLEAN");
        info.addNativeTypeMapping(Types.ARRAY,       "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BLOB,        "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.CLOB,        "LONGVARCHAR",   Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DISTINCT,    "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.FLOAT,       "DOUBLE",        Types.DOUBLE);
        info.addNativeTypeMapping(Types.JAVA_OBJECT, "OBJECT");
        info.addNativeTypeMapping(Types.NULL,        "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.OTHER,       "OTHER");
        info.addNativeTypeMapping(Types.REF,         "LONGVARBINARY", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,      "LONGVARBINARY", Types.LONGVARBINARY);
        // JDBC's TINYINT requires a value range of -255 to 255, but HsqlDb's is only -128 to 127
        info.addNativeTypeMapping(Types.TINYINT,     "SMALLINT",      Types.SMALLINT);

        // when using JDBC3, BIT will be back-mapped to BOOLEAN
        info.addNativeTypeMapping("BIT",      "BOOLEAN",       "BOOLEAN");
        info.addNativeTypeMapping("DATALINK", "LONGVARBINARY", "LONGVARBINARY");

        info.addDefaultSize(Types.CHAR,      Integer.MAX_VALUE);
        info.addDefaultSize(Types.VARCHAR,   Integer.MAX_VALUE);
        info.addDefaultSize(Types.BINARY,    Integer.MAX_VALUE);
        info.addDefaultSize(Types.VARBINARY, Integer.MAX_VALUE);

        setSqlBuilder(new HsqlDbBuilder(info));
        setModelReader(new HsqlDbModelReader(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }

    /**
     * {@inheritDoc}
     */
    public void shutdownDatabase(Connection connection) throws DynaSqlException
    {
        // TODO: Determine whether we're running in embedded mode (from the url ?)
//        
//        try
//        {
//            Statement stmt = connection.createStatement();
//
//            stmt.executeUpdate("SHUTDOWN");
//            stmt.close();
//        }
//        catch (SQLException ex)
//        {
//            throw new DynaSqlException(ex);
//        }
    }
}
