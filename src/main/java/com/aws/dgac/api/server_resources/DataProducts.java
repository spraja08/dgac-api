/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.server_resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class DataProducts extends AbstractResource {

    @Override
    protected void doInit() throws ResourceException {
        resource = "dataProducts";
        super.doInit();
    }

    @Override
    protected JsonRepresentation post(Representation ent) throws ResourceException {
        return super.post(ent);
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        String strFilter = getQueryValue("filter");
        if (strFilter == null)
            return super.get();
        JsonObject filter = JsonParser.parseString(strFilter).getAsJsonObject();
        JsonArray range = JsonParser.parseString(getQueryValue("range")).getAsJsonArray();
        if( filter.keySet().size() == 0 )
            return super.get();
        this.searchQuery = filter.get("q").getAsString();
        this.rangeStart = range.get(0).getAsInt();
        this.rangeEnd = range.get(1).getAsInt();
        return super.search();
    }

    @Override
    protected Representation delete() throws ResourceException {
        return super.delete();
    }

    @Override
    protected Representation put(Representation entity) throws ResourceException {
        return super.put( entity );
    }
}