package com.rhythm.louie.services.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.primitives.Ints;
import com.google.protobuf.ByteString;

import org.slf4j.LoggerFactory;

import com.rhythm.louie.sql.SqlProtos.*;

import com.rhythm.louie.DAO;
import com.rhythm.louie.jdbc.DatasourceFactory;
import com.rhythm.louie.jdbc.JdbcFactory;
import com.rhythm.louie.jdbc.JdbcService;
import com.rhythm.louie.jdbc.MysqlConnectionFactory;
import com.rhythm.louie.jdbc.StandardJdbcFactory;
import com.rhythm.louie.server.ServiceProperties;

@DAO
public class SqlDAO implements SqlService {
    private final JdbcFactory jdbcFactory;
    
    private static final String HOST = "host";
    private static final String DATABASE = "database";
    private static final String DATASOURCE = "datasource";
    private static final String LOGKEY = "logkey";
    
    public SqlDAO() {
         
        ServiceProperties props = ServiceProperties.getServiceProperties(SERVICE_NAME);
        
        String logkey = props.getCustomProperty(LOGKEY, "Sql");
        
        String datasource = props.getCustomProperty(DATASOURCE,"");
        if (!datasource.isEmpty()) {
            jdbcFactory = new DatasourceFactory(datasource, logkey);
        } else {
            String host = props.getCustomProperty(HOST,"localhost");
            String database = props.getCustomProperty(DATABASE,"");
            if (database.isEmpty()) {
                LoggerFactory.getLogger(SqlDAO.class).error("Must specify a datasource or database for sql service!");
                jdbcFactory = null;
            } else {
                jdbcFactory = new StandardJdbcFactory(new MysqlConnectionFactory(host,database), logkey);
            }
        }
    }

    @Override
    public List<SqlResultPB> query(SqlStatementPB statement) throws Exception {
        try (JdbcService jdbc = jdbcFactory.newService(statement.getSql())) {
            boolean b = jdbc.execute();
            if (!b) {
                SqlResultPB.Builder header = SqlResultPB.newBuilder();
                header.addColumnsBuilder()
                        .setValue(ByteString.copyFromUtf8(Integer.toString(jdbc.getUpdateCount())));
                return Collections.singletonList(header.build());
            }
            
            ResultSet rst = jdbc.getResultSet();
            ResultSetMetaData meta = rst.getMetaData();
            int columns = meta.getColumnCount();
            
            List<SqlResultPB> results = new ArrayList<>();
            
            SqlResultPB.Builder header = SqlResultPB.newBuilder();
            for (int i=1;i<=columns;i++) {
                header.addColumnsBuilder()
                        .setType(meta.getColumnType(i))
                        .setValue(ByteString.copyFromUtf8(meta.getColumnLabel(i)));
            }
            results.add(header.build());
            
            while (rst.next()) {
                SqlResultPB.Builder row = SqlResultPB.newBuilder();
                for (int i=1;i<=columns;i++) {
                    SqlColumnPB.Builder column = row.addColumnsBuilder();
                    byte[] bytes = rst.getBytes(i);
                    if (bytes!=null && bytes.length>0) {
                        column.setValue(ByteString.copyFrom(bytes));
                    }
                }
                results.add(row.build());
            }
            return results;
        }
    }
    
}

