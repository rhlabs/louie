package com.rhythm.louie.services.sql;

import java.util.List;

import com.rhythm.louie.sql.SqlProtos.*;

import com.rhythm.louie.Service;

@Service
public interface SqlService {
    public static final String SERVICE_NAME = "sql";

    /**
     * Performs the sql lookup returning the rows that were found. The first row
     * returned contains the column headers and datatypes
     * 
     * @param statement
     * @return List of SqlResultPB
     * @throws Exception
     */
    List<SqlResultPB> query(SqlStatementPB statement) throws Exception;

}

