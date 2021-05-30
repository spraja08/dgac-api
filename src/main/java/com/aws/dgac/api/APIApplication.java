/**
*
* @author  Raja SP
*/

package com.aws.dgac.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.service.CorsService;
import com.aws.dgac.api.server_resources.*;


public class APIApplication extends Application {

    public APIApplication() {
        CorsService corsService = new CorsService();
        corsService.setAllowingAllRequestedHeaders(true);
        corsService.setAllowedOrigins(new HashSet(Arrays.asList("*")));
        corsService.setAllowedCredentials(true);
        corsService.setSkippingResourceForCorsOptions(true);

        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("Authorization");
        allowHeaders.add("Content-Type");
        allowHeaders.add("Content-Range");
        allowHeaders.add("X-Requested-With");
        corsService.setAllowedHeaders(allowHeaders);

        Set<String> exposeHeaders = new HashSet<>();
        exposeHeaders.add("Authorization");
        exposeHeaders.add("Link");
        exposeHeaders.add("X-RateLimit-Limit");
        exposeHeaders.add("X-RateLimit-Remaining");
        exposeHeaders.add("X-OAuth-Scopes");
        exposeHeaders.add("X-Accepted-OAuth-Scopes");
        exposeHeaders.add("Content-Range");

        corsService.setExposedHeaders(exposeHeaders);

        getServices().add(corsService);
    }

    @Override
    public Restlet createInboundRoot() {
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of items"
        router.attach( "/businessDomains", BusinessDomains.class );
        router.attach( "/businessDomains/{id}", BusinessDomains.class );
        router.attach( "/dataStores/{id}", DataStores.class );
        router.attach( "/users", Users.class );
        router.attach( "/users/{id}", Users.class );
        router.attach( "/roles", Roles.class );
        router.attach( "/roles/{id}", Roles.class );
        router.attach( "/dgac", DGaC.class );
        router.attach( "/dgac/{id}", DGaC.class );
        router.attach( "/ontologyAttributes", OntologyAttributes.class );
        router.attach( "/ontologyAttributes/{id}", OntologyAttributes.class );
        router.attach( "/dataProducts", DataProducts.class );
        router.attach( "/dataProducts/{id}", DataProducts.class );
        router.attach( "/query", Query.class );
        router.attach( "/contract", Contract.class );
        router.attach( "/dataSourceTypes", DataSourceTypes.class );
        router.attach( "/ontologyElementTypes", OntologyElementTypes.class );
        router.attach( "/ontologyEntities", OntologyEntities.class );
        router.attach( "/ontologyEntities/{id}", OntologyEntities.class );
        return router;
    }
}