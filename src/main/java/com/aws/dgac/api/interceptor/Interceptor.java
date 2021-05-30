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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.aws.dgac.api.App;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
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
        System.out.println( "Incoming Query : " + sql );
        String gacQuery = applyDGaC(sql, roleId);
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

    public String getContract(String sql, String roleId) throws JSQLParserException, IOException {
        System.out.println( "Incoming Query : " + sql );
        StringWriter writer = new StringWriter(); // put your own writer here
        CSVWriter csvWriter = new CSVWriter( writer );
        String[] aLine = new String[3];
        aLine[0] = "COLUMN";
        aLine[1] = "ONTOLOGY MAPPING";
        aLine[2] = "DGaC CONSTRUCT";
        csvWriter.writeNext( aLine, false );

        Select select = (Select) CCJSqlParserUtil.parse(sql);
        List<String> tables = new TablesNamesFinder().getTableList(select);
        List< String > columns = getColumnsFromSelectClause( sql, tables );
        
        for( int i=0; i<tables.size(); i++ ) {
            String thisTable = tables.get(i);
            if( thisTable.contains( "." ) ) { //Fully qualified Table Name
                String[] fqn = thisTable.split( "\\." );
                thisTable = fqn[ fqn.length - 1 ];
            }    
            JsonObject tableMeta = technicalMetaData.getTechnicalMetaData(thisTable);
            JsonArray colArr = tableMeta.get( "columns" ).getAsJsonArray();
            for( int j=0; j<colArr.size(); j++ ) {
                String thisCol = colArr.get( j ).getAsJsonObject().get( "id" ).getAsString();
                if( columns.contains( thisCol ) ) {
                    String bcMapping = technicalMetaData.getBusinessCatalogMapping(thisCol, thisTable);
                    aLine = new String[ 3 ];
                    aLine[ 0 ] = thisTable + "." + thisCol;
                    if( bcMapping != null ) {
                        String gacConstruct = businessCatalog.getDGaCConstruct(thisCol, bcMapping, roleId);
                        aLine[ 1 ] = bcMapping;
                        aLine[ 2 ] = gacConstruct;
                    } else {
                        aLine[ 1 ] = "Not Governed";
                        aLine[ 2 ] = "View in Clear";
                    }
                    csvWriter.writeNext( aLine, false );
                }
            }
        }
        csvWriter.flush();
        return writer.toString();
    }

    private List<String> getColumnsFromSelectClause( String sql, List<String> tables ) {
        String lowerSql = sql.toLowerCase();
        String selectColumnns = lowerSql.substring( lowerSql.indexOf( "select" ) + 6, lowerSql.indexOf( "from" ) );
        String[] arrCols = selectColumnns.split( "," );
        List< String > cols = new ArrayList< String >();
        for( int i=0; i<arrCols.length; i++ )
            cols.add( arrCols[i].trim() );
        return cols;        
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
        final String JDBC_DRIVER = App.properties.getProperty( "aws.dgac.query_engine_driver" );
        final String DB_URL = App.properties.getProperty( "aws.dgac.query_engine_url" );
        Properties properties = new Properties();
        properties.setProperty("user", App.properties.getProperty( "aws.dgac.query_engine_user") );
        Connection conn = null;
        Class.forName(JDBC_DRIVER);
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

    public String applyDGaC(String sql, String roleId) throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        List<String> tables = new TablesNamesFinder().getTableList(select);
        List< String > whereClauses = new ArrayList< String >();

        ExpressionDeParser columnParser = new ExpressionDeParser() {
            @Override
            public void visit(Column tableColumn) {
                String tableName = null;
                String columnName = tableColumn.getColumnName();
                String fullColumnName = tableColumn.getFullyQualifiedName();
                if (tableColumn.getTable() != null)
                    tableName = tableColumn.getTable().getFullyQualifiedName();
                else
                    tableName = technicalMetaData.getTableGivenColumn(columnName, tables);

                String bcMapping = null;
                try{
                    bcMapping = technicalMetaData.getBusinessCatalogMapping(columnName, tableName);
                    if (bcMapping != null) {
                        String gacColumn = businessCatalog.getDGaCColumn(fullColumnName, bcMapping, roleId);
                        if (gacColumn != null) {
                            tableColumn.setColumnName(gacColumn + " as " + columnName);
                        }
                    }
                } catch( IOException e ) {
                    e.printStackTrace();
                }
                super.visit(tableColumn);
            }
        };

        ExpressionDeParser rowFilterParserParser = new ExpressionDeParser() {
            @Override
            public void visit(Column tableColumn) {
                String tableName = null;
                String columnName = tableColumn.getColumnName();
                if (tableColumn.getTable() != null)
                    tableName = tableColumn.getTable().getFullyQualifiedName();
                else
                    tableName = technicalMetaData.getTableGivenColumn(columnName, tables);

                String bcMapping = null;
                try{
                    bcMapping = technicalMetaData.getBusinessCatalogMapping(columnName, tableName);
                    if (bcMapping != null) {
                        String gacRowFilter = businessCatalog.getDGaCRowFilter(columnName, bcMapping, roleId);
                        if (gacRowFilter != null) {
                            whereClauses.add( gacRowFilter );
                        }
                    }
                } catch( IOException e ) {
                    e.printStackTrace();
                }
                super.visit(tableColumn);
            }
        };

        //This part is to just fill the  list of where clauses
        StringBuilder buffer = new StringBuilder();
        SelectDeParser deparser = new SelectDeParser(rowFilterParserParser, buffer) {};
        rowFilterParserParser.setSelectVisitor(deparser);
        rowFilterParserParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);

        buffer = new StringBuilder();
        deparser = new SelectDeParser(columnParser, buffer) {};
        columnParser.setSelectVisitor(deparser);
        columnParser.setBuffer(buffer);
        select.getSelectBody().accept(deparser);

        String where = null;
        for( int i=0; i<whereClauses.size(); i++ ) {
            if( i == 0 )
                where = whereClauses.get( i );
            else    
                where += " and " + whereClauses.get( i );    
        }

        if( where != null ) {
            Expression whereExp = CCJSqlParserUtil.parseCondExpression( where );
            ((PlainSelect) select.getSelectBody()).setWhere( whereExp );
        }  
      
        return select.getSelectBody().toString();
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
