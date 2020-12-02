/**
* @author  Raja SP
*/
package com.aws.dgac.api.interceptor;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TechnicalMetaData {
    private JsonArray technicalMetaData = null;

    public TechnicalMetaData() {
        technicalMetaData = com.aws.dgac.api.App.store.get( "dataProducts" );
    }

    public JsonArray getTechnicalMetaData() {
        return this.technicalMetaData;
    }

    public JsonObject getTechnicalMetaData(String tableName) {
        return com.aws.dgac.api.App.store.get( "dataProducts", tableName );
    }

    public String getTableGivenColumn(String columnName) {
        for( int i=0; i<technicalMetaData.size(); i++ ) {
            JsonObject thisTable = technicalMetaData.get(i).getAsJsonObject();
            if( getColumnDef( thisTable, columnName ) != null )
                return thisTable.get( "id" ).getAsString();
        }
        return null;
    }

    public String getTableGivenColumn(String columnName, List<String> tables) {
        for( int i=0; i<technicalMetaData.size(); i++ ) {
            JsonObject thisTable = technicalMetaData.get(i).getAsJsonObject();
            String thisTableId = thisTable.get( "id" ).getAsString();
            if( tables.contains( thisTableId) && getColumnDef( thisTable, columnName ) != null )
                return thisTableId;
        }
        return null;
    }

    public String getBusinessCatalogMapping(String columnName, String tableName) {
        JsonObject table = getTechnicalMetaData( tableName );
        JsonElement mapping = getDgacColumnDef( table, columnName );
        if (mapping == null)
            return null;
        return mapping.getAsJsonObject().get( "DGaCMapping" ).getAsString();
    }

    private JsonObject getColumnDef( JsonObject table, String columnName ) {
        JsonArray colArr = table.get( "columns" ).getAsJsonArray();
        for( int i=0; i<colArr.size(); i++ ) {
            if( colArr.get(i).getAsJsonObject().get( "id" ).getAsString().equals( columnName ) )
                return colArr.get(i).getAsJsonObject();
        }
        return null;
    }

    private JsonObject getDgacColumnDef( JsonObject table, String columnName ) {
        JsonArray colArr = table.get( "dgacMapping" ).getAsJsonArray();
        for( int i=0; i<colArr.size(); i++ ) {
            if( colArr.get(i).getAsJsonObject().get( "columnId" ).getAsString().equals( columnName ) )
                return colArr.get(i).getAsJsonObject();
        }
        return null;
    }
}
