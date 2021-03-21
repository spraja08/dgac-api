/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.connector;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface Store {
    public JsonArray get( String resource ) throws IOException;
    public JsonObject get( String resource, String id ) throws IOException;
    public JsonArray filteredGet(String resource, String attribute, String value) throws IOException;
    public JsonArray search( String resource, String searchQuery, int rangeStart, int rangeEnd ) throws IOException;
    public void create( String resource, JsonObject content ) throws IOException;
    public void update( String resource, JsonObject content ) throws IOException;
    public void delete( String resource, JsonObject content ) throws IOException;

}
