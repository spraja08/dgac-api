/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.server_resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class OntologyAttributes extends AbstractResource {

    @Override
    protected void doInit() throws ResourceException {
        resource = "ontologyAttributes";
        super.doInit();
    }

    @Override
    protected JsonRepresentation post( Representation ent ) throws ResourceException {
        return super.post( ent );
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        String strFilter = getQueryValue("filter");
        if (strFilter == null)
            return super.get();
        JsonObject filter = JsonParser.parseString(strFilter).getAsJsonObject();    
        if( filter.keySet().size() == 0 )
            return super.get();
        String filterValue = filter.get("type").getAsString();
        return this.get("type", filterValue);
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