package com.aws.dgac.api;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class Query extends ServerResource {

    private String id;

    @Override
    protected void doInit() throws ResourceException {
        id = (String) getRequest().getAttributes().get("id");
    }

    @Override
    protected JsonRepresentation post(Representation entity) throws ResourceException {
        System.out.println( "Resource Ased for " + getRequest().getResourceRef().getPath() );
        return new JsonRepresentation( new JsonObject().toString() );
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        System.out.println( "Resource Ased for " + getRequest().getResourceRef().getPath() );
        System.out.println( "Target Reference Ased for " + getRequest().getResourceRef().getTargetRef() );
        System.out.println( "Query  Ased for " + getRequest().getResourceRef().getQuery() );
        System.out.println( "Entity  Ased for " + getRequest().getEntityAsText() );
        System.out.println( "Id  Ased for " + id );

        return new JsonRepresentation( new JsonObject().toString() );
    }
}