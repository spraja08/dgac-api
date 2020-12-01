/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.connector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface Store {
    public JsonArray get( String resource );
    public JsonObject get( String resource, String id );
    public void create( String resource, JsonObject content );
    public void update( String resource, JsonObject content );
    public void delete( String resource, JsonObject content );
}
