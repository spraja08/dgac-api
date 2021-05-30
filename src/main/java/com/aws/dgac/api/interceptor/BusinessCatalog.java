/**
* @author  Raja SP
*/
package com.aws.dgac.api.interceptor;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BusinessCatalog {
    private JsonArray businessCatalog = null;

    public BusinessCatalog() {
        try {
            this.businessCatalog = com.aws.dgac.api.App.store.get("ontologyAttributes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonArray getBusinessCatalog() {
        return this.businessCatalog;
    }

    public String getDGaCColumn(String columnName, String bcItem, String roleId) throws IOException {
        JsonObject bcObject = com.aws.dgac.api.App.store.get( "ontologyAttributes", bcItem );
        JsonObject gacs = bcObject.get( "DGac" ).getAsJsonObject();
        JsonArray gacConstructs = gacs.get("columnMasking").getAsJsonArray();
        String dgacId = null;
        for( int i=0; i<gacConstructs.size(); i++ ) {
            if( gacConstructs.get( i ).getAsJsonObject().get( "roles" ).getAsJsonArray().contains( new JsonPrimitive( roleId ) ) )
                dgacId = gacConstructs.get( i ).getAsJsonObject().get( "dgac" ).getAsJsonArray().get( 0 ).getAsString();
        }
         if( dgacId != null ) {       
             String gac = com.aws.dgac.api.App.store.get( "dgac", dgacId ).get( "udf" ).getAsString();
             if( ! gac.equals("aws_dgac_clear"))
                 return gac + "(" + columnName + ")";
             else
                 return columnName;
        }    
         return "aws_dgac_block" + "(" + columnName + ")";   
    }

    public String getDGaCConstruct(String columnName, String bcItem, String roleId) throws IOException {
        JsonObject bcObject = com.aws.dgac.api.App.store.get( "ontologyAttributes", bcItem );
        JsonObject gacs = bcObject.get( "DGac" ).getAsJsonObject();
        JsonArray maskingConstructs = gacs.get("columnMasking").getAsJsonArray();

        JsonArray filteringConstructs = new JsonArray();
        if( gacs.has( "rowFiltering") && ! gacs.get("rowFiltering").isJsonNull() )
        filteringConstructs = gacs.get("rowFiltering").getAsJsonArray();

        String maskingDgacId = null;
        for( int i=0; i<maskingConstructs.size(); i++ ) {
            if( maskingConstructs.get( i ).getAsJsonObject().get( "roles" ).getAsJsonArray().contains( new JsonPrimitive( roleId ) ) )
                maskingDgacId = maskingConstructs.get( i ).getAsJsonObject().get( "dgac" ).getAsJsonArray().get( 0 ).getAsString();
        }
        
        String masking = null;
        if( maskingDgacId != null ) {       
            masking = com.aws.dgac.api.App.store.get( "dgac", maskingDgacId ).get( "name" ).getAsString();
        } else 
            masking = "Block the Value";   
        
        String filterValue = null;
        for( int i=0; i<filteringConstructs.size(); i++ ) {
            if( filteringConstructs.get( i ).getAsJsonObject().get( "roles" ).getAsJsonArray().contains( new JsonPrimitive( roleId ) ) ) {
                filterValue = filteringConstructs.get( i ).getAsJsonObject().get( "filterValues" ).getAsString();
                break;
            }   
        }
        return masking + ( ( filterValue != null ) ? ( " and FILTER ROWS BY : " + filterValue ) : "" );
    }


    public String getDGaCRowFilter(String columnName, String bcItem, String roleId) throws IOException {
        JsonObject bcObject = com.aws.dgac.api.App.store.get( "ontologyAttributes", bcItem );
        JsonObject gacs = bcObject.get( "DGac" ).getAsJsonObject();
        if( ! gacs.has( "rowFiltering") || gacs.get("rowFiltering").isJsonNull() )
            return null;
        JsonArray gacConstructs = gacs.get("rowFiltering").getAsJsonArray();
        String filterValue = null;
        for( int i=0; i<gacConstructs.size(); i++ ) {
            if( gacConstructs.get( i ).getAsJsonObject().get( "roles" ).getAsJsonArray().contains( new JsonPrimitive( roleId ) ) ) {
                filterValue = gacConstructs.get( i ).getAsJsonObject().get( "filterValues" ).getAsString();
                filterValue = columnName + " != " + filterValue;  
                break;
            }    
        }
        return filterValue;
    }
}