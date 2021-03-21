/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.server_resources;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class OntologyElementTypes extends AbstractResource {

    @Override
    protected void doInit() throws ResourceException {
        resource = "ontologyElementTypes";
        super.doInit();
    }

    @Override
    protected JsonRepresentation post( Representation ent ) throws ResourceException {
        return super.post( ent );
    }

    @Override
    protected JsonRepresentation get() throws ResourceException {
        return super.get();
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