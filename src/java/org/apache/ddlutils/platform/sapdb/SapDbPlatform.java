package org.apache.ddlutils.platform.sapdb;

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

import java.sql.Types;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.platform.PlatformImplBase;

/**
 * The SapDB platform implementation.
 * 
 * @author Thomas Dudziak
 * @version $Revision: 231306 $
 */
public class SapDbPlatform extends PlatformImplBase
{
    /** Database name of this platform. */
    public static final String DATABASENAME     = "SapDB";
    /** The standard SapDB/MaxDB jdbc driver. */
    public static final String JDBC_DRIVER      = "com.sap.dbtech.jdbc.DriverSapDB";
    /** The subprotocol used by the standard SapDB/MaxDB driver. */
    public static final String JDBC_SUBPROTOCOL = "sapdb";

    /**
     * Creates a new platform instance.
     */
    public SapDbPlatform()
    {
        PlatformInfo info = new PlatformInfo();

        info.setMaxIdentifierLength(32);
        info.setRequiringNullAsDefaultValue(false);
        info.setPrimaryKeyEmbedded(true);
        info.setForeignKeysEmbedded(false);
        info.setIndicesEmbedded(false);
        info.setCommentPrefix("/*");
        info.setCommentSuffix("*/");

        // BIGINT is also handled by the model reader
        // Unfortunately there is no way to distinguish between REAL, and FLOAT/DOUBLE when
        // reading back via JDBC, because they all have the same size of 8
        info.addNativeTypeMapping(Types.ARRAY,         "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.BIGINT,        "FIXED(38,0)");
        info.addNativeTypeMapping(Types.BINARY,        "CHAR{0} BYTE");
        info.addNativeTypeMapping(Types.BIT,           "BOOLEAN");
        info.addNativeTypeMapping(Types.BLOB,          "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.CLOB,          "LONG",      Types.LONGVARCHAR);
        info.addNativeTypeMapping(Types.DECIMAL,       "FIXED");
        info.addNativeTypeMapping(Types.DISTINCT,      "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.DOUBLE,        "FLOAT(38)", Types.FLOAT);
        info.addNativeTypeMapping(Types.FLOAT,         "FLOAT(38)");
        info.addNativeTypeMapping(Types.JAVA_OBJECT,   "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.LONGVARBINARY, "LONG BYTE");
        info.addNativeTypeMapping(Types.LONGVARCHAR,   "LONG");
        info.addNativeTypeMapping(Types.NULL,          "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.NUMERIC,       "FIXED",     Types.DECIMAL);
        info.addNativeTypeMapping(Types.OTHER,         "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.REAL,          "FLOAT(16)", Types.FLOAT);
        info.addNativeTypeMapping(Types.REF,           "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.STRUCT,        "LONG BYTE", Types.LONGVARBINARY);
        info.addNativeTypeMapping(Types.TINYINT,       "SMALLINT",  Types.SMALLINT);
        info.addNativeTypeMapping(Types.VARBINARY,     "VARCHAR{0} BYTE");

        info.addNativeTypeMapping("BOOLEAN",  "BOOLEAN",   "BIT");
        info.addNativeTypeMapping("DATALINK", "LONG BYTE", "LONGVARBINARY");

        info.addDefaultSize(Types.CHAR,      254);
        info.addDefaultSize(Types.VARCHAR,   254);
        info.addDefaultSize(Types.BINARY,    254);
        info.addDefaultSize(Types.VARBINARY, 254);

        setSqlBuilder(new SapDbBuilder(info));
        setModelReader(new SapDbModelReader(info));
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return DATABASENAME;
    }
}
