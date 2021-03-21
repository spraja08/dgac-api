/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.server_resources;

import java.io.IOException;

import com.aws.dgac.api.App;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.restlet.data.Range;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class AbstractResource extends ServerResource {

    protected String id;
    protected static String resource = "businessDomains";
    protected int rangeStart;
    protected int rangeEnd;
    protected String searchQuery;

    @Override
    protected void doInit() throws ResourceException {
        id = (String) getRequest().getAttributes().get("id");
    }

    @Override
    protected JsonRepresentation post( Representation ent ) throws ResourceException {
        JsonObject input = null;
        try {
            input = (JsonObject )JsonParser.parseString( ent.getText() );
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            App.store.create(resource, input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JsonRepresentation( input.toString() );
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        if (this.id == null) {
            JsonArray result = null;
            try {
                result = App.store.get(resource);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Range range = new Range();
            range.setUnitName(resource);
            range.setIndex(0);
            range.setSize(20);
            range.setInstanceSize(100);
            JsonRepresentation output = new JsonRepresentation(result.toString());
            output.setRange(range);
            System.out.println(output.toString());
            return output;
        }
        try {
            return new JsonRepresentation(App.store.get(resource, this.id).toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected JsonRepresentation get(String attribute, String value) throws ResourceException {
        if (this.id == null) {
            JsonArray result = null;
            try {
                result = App.store.filteredGet(resource, attribute, value);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Range range = new Range();
            range.setUnitName(resource);
            range.setIndex(0);
            range.setSize(20);
            range.setInstanceSize(100);
            JsonRepresentation output = new JsonRepresentation(result.toString());
            output.setRange(range);
            System.out.println(output.toString());
            return output;
        }
        try {
            return new JsonRepresentation(App.store.get(resource, this.id).toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected JsonRepresentation search() throws ResourceException {
        if (this.id == null) {
            JsonArray result = null;
            try {
                result = App.store.search(resource, this.searchQuery, this.rangeStart, this.rangeEnd);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Range range = new Range();
            range.setUnitName(resource);
            range.setIndex(0);
            range.setSize(20);
            range.setInstanceSize(100);
            JsonRepresentation output = new JsonRepresentation(result.toString());
            output.setRange(range);
            System.out.println(output.toString());
            return output;
        }
        try {
            return new JsonRepresentation(App.store.get(resource, this.id).toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Representation delete() throws ResourceException {
        JsonObject obj = null;
        try {
            obj = App.store.get(resource, this.id);
            App.store.delete( resource, obj );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new JsonRepresentation( obj.toString() );
    }

    @Override
    protected Representation put(Representation entity) throws ResourceException {
        JsonObject input = null;
        try {
            input = (JsonObject) JsonParser.parseString(entity.getText());
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
        try {
            App.store.update(resource, input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JsonRepresentation( input.toString() );
    }
}