package com.aws.dgac.api;

import java.io.IOException;
import java.sql.SQLException;

import com.aws.dgac.api.interceptor.Interceptor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import net.sf.jsqlparser.JSQLParserException;

public class Query extends ServerResource {

    private String id;

    @Override
    protected void doInit() throws ResourceException {
        id = (String) getRequest().getAttributes().get("id");
    }

    @Override
    protected JsonRepresentation post(Representation entity) throws ResourceException {
        String text = null;
        try {
            text = entity.getText();
            JsonObject jInput = JsonParser.parseString(text).getAsJsonObject();
            String sql = jInput.get( "sql" ).getAsString();
            String role = jInput.get( "role" ).getAsString();
            Interceptor interceptor = new Interceptor();
            String results = interceptor.getQueryResults(sql, role);
            JsonObject jResults = new JsonObject();
            jResults.addProperty( "results", results );
            return new JsonRepresentation( jResults.toString() );
        } catch (IOException | ClassNotFoundException | SQLException | JSQLParserException  e) {
            e.printStackTrace();
            return new JsonRepresentation( e.toString() );
        }
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        return null;
    }
}