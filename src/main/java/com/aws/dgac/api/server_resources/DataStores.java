/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.server_resources;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class DataStores extends AbstractResource {
    @Override
    protected void doInit() throws ResourceException {
        resource = "businessDomains";
        super.doInit();
    }

    @Override
    protected JsonRepresentation post(Representation ent) throws ResourceException {
        return null;
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        JsonRepresentation result = super.get();
        String text;
        try {
            text = result.getText();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceException(0, e.toString());
        }
        JsonObject jObj = JsonParser.parseString(text).getAsJsonObject();
        return new JsonRepresentation(jObj.get( "dataStores" ).getAsJsonArray().toString());
    }

    @Override
    protected Representation delete() throws ResourceException {
        return null;
    }

    @Override
    protected Representation put(Representation entity) throws ResourceException {
        return null;
    }
}