/**
* @author  Raja SP
*/
package com.aws.dgac.api.interceptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BusinessCatalog {
    private JsonArray businessCatalog = null;

    public BusinessCatalog() {
        this.businessCatalog = com.aws.dgac.api.App.store.get( "businessGlossary" );
    }

    public JsonArray getBusinessCatalog() {
        return this.businessCatalog;
    }

    public String getDGaCColumn(String columnName, String bcItem, String roleId) {
        JsonObject bcObject = com.aws.dgac.api.App.store.get( "businessGlossary", bcItem );
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
}