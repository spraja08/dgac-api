/**
* @author  Raja SP
*/
package com.aws.dgac.api.interceptor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public final class Interceptor {

    private BusinessCatalog businessCatalog;
    private TechnicalMetaData technicalMetaData;

    private static Connection connection = null;

    public Interceptor() throws IOException, ClassNotFoundException, SQLException {
        businessCatalog = new BusinessCatalog();
        technicalMetaData = new TechnicalMetaData();
        if (connection == null)
            connection = getPrestoConnection();
    }

    public String getQueryResults(String sql, String roleId) throws JSQLParserException, IOException {
        String gacQuery = applyDGaC(sql, roleId);
        System.out.println( "Incoming Query : " + sql );
        System.out.println( "DGaC Query : " + gacQuery );
        java.sql.Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(gacQuery);
            return getCSV( res );   
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCSV( ResultSet res) throws SQLException, IOException {
        StringWriter writer = new StringWriter(); // put your own writer here
        CSVWriter csvWriter = new CSVWriter( writer );
        csvWriter.writeAll( res, true, true, false ); 
        return writer.toString();
    }

    public static void main(String[] args)
            throws JSQLParserException, IOException, NoSuchAlgorithmException, ClassNotFoundException, SQLException {

        if (args.length < 2) {
            System.err.println(
                    "Usage : java -jar DGaC-1.0-SNAPSHOT-jar-with-dependencies.jar <path to configs> <roleid>");
            return;
        }
        Interceptor app = new Interceptor();
        Connection prestoConnection = app.getPrestoConnection();
        Scanner sn = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.print("DGaC:query > ");
            String input = sn.nextLine();
            if (input.equals("exit")) {
                prestoConnection.close();
                return;
            }
            if (input.startsWith("select") || input.startsWith("SELECT")) {
                System.out.println();
                System.out.println("DGaC Query :" + app.applyDGaC(input, args[1]));
                System.out.println();
                app.testJDBC(prestoConnection, app.applyDGaC(input, args[1]));
            } else if (input.startsWith("dump") || input.startsWith("DUMP")) {
                String[] tokens = input.split(" ");
                if (tokens[1].startsWith("business"))
                    System.out.println(new GsonBuilder().setPrettyPrinting().create()
                            .toJson(app.businessCatalog.getBusinessCatalog()));
                else
                    System.out.println(new GsonBuilder().setPrettyPrinting().create()
                            .toJson(app.technicalMetaData.getTechnicalMetaData()));
            }
        }
        // String select = "select name, msisdn, mailId, startTime, cellId, calledPartyNumber from cdr limit 10";
    }

    private Connection getPrestoConnection() throws SQLException, ClassNotFoundException {
        System.out.println( "*****************************Attempting Connecting to Presto*************************" );
        final String JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
        final String DB_URL = "jdbc:presto://ec2-54-254-182-252.ap-southeast-1.compute.amazonaws.com:8080/postgresql/tcbschema";
        // Database credentials
        Properties properties = new Properties();
        properties.setProperty("user", "test");
        Connection conn = null;
        // Register JDBC driver
        Class.forName(JDBC_DRIVER);
        // Open a connection
        conn = DriverManager.getConnection(DB_URL, properties);
        System.out.println( "*****************************Got Presto Connection*************************" );
        return conn;
    }

    private void testJDBC(Connection conn, String sql) {
        java.sql.Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            DBTablePrinter.printResultSet(res);
            res.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
    }

    private String applyDGaC(String sql, String roleId) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        List<String> tables = new TablesNamesFinder().getTableList(select);

        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public void visit(Column tableColumn) {
                String tableName = null;
                String columnName = tableColumn.getColumnName();
                if (tableColumn.getTable() != null)
                    tableName = tableColumn.getTable().getName();
                else
                    tableName = technicalMetaData.getTableGivenColumn(columnName, tables);

                String bcMapping = technicalMetaData.getBusinessCatalogMapping(columnName, tableName);
                if (bcMapping != null) {
                    String gacColumn = businessCatalog.getDGaCColumn(columnName, bcMapping, roleId);
                    if (gacColumn != null) {
                        tableColumn.setColumnName(gacColumn + " as " + columnName);
                    }
                }
                super.visit(tableColumn);
            }
        };

        SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {
        };
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);
        return buffer.toString();
    }

    private static void replaceTableName(String sql) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public void visit(Column tableColumn) {
                if (tableColumn.getTable() != null) {
                    tableColumn.getTable().setName(tableColumn.getTable().getName() + "_mytest");
                }
                if (tableColumn.getColumnName().equals("id")) {
                    tableColumn.setColumnName("changedId");
                }
                super.visit(tableColumn);
            }
        };

        SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer) {
            @Override
            public void visit(Table tableName) {
                tableName.setName(tableName.getName() + "_mytest");
                super.visit(tableName);
            }
        };
        expressionDeParser.setSelectVisitor(deparser);
        expressionDeParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);

        System.out.println(buffer.toString());
    }

    public static List<String> getColumns(String sql) throws JSQLParserException {
        CCJSqlParserManager parserRealSql = new CCJSqlParserManager();
        Statement stmt = parserRealSql.parse(new StringReader(sql)); // create a jSqlParser Statement from the sql
        List<String> list = new ArrayList<String>(); // contains the columns result
        if (stmt instanceof Select) { // only parse select sql
            Select selectStatement = (Select) stmt; // convert to Select Statement
            PlainSelect ps = (PlainSelect) selectStatement.getSelectBody();
            List<SelectItem> selectitems = ps.getSelectItems();
            // add the selected items to result
            selectitems.stream().forEach(selectItem -> list.add(selectItem.toString()));
        }
        return list;
    }

    public static List<String> getTables(String sql) throws JSQLParserException {
        CCJSqlParserManager parserRealSql = new CCJSqlParserManager();
        Statement stmt = parserRealSql.parse(new StringReader(sql)); // create a jSqlParser Statement from the sql
        return new TablesNamesFinder().getTableList(stmt);
    }
}
