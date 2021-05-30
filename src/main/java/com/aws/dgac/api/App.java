/**
* REST API - Restlet main entry point
*
* @author  Raja SP
*/

package com.aws.dgac.api;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.aws.dgac.api.connector.ElasticStore;
import com.aws.dgac.api.connector.Store;
import com.aws.dgac.api.interceptor.Interceptor;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class App {

    public static Store store = null;
    public static Properties properties;

    public static void main(String[] args) throws Exception {

        if( args.length < 2 ) {
                System.err.println( "Usage : java -jar entity-analytics.jar <path to the properties file> <port>" );
                return;
        }
        
        /*
        args = new String[2];
        args[0] = "/Users/rspamzn/Documents/DAML/Assets/DGaC/app/dgac-api/resources/dgac-api.properties";
        args[1] = "5000";
        */

        Component component = new Component();
        int port = Integer.parseInt( args[1] );
        component.getServers().add(Protocol.HTTP, port);

        InputStream input = new FileInputStream( args[0] );
        properties = new Properties();
        properties.load( input );
        //store = new FileStore( properties );
        store = new ElasticStore( properties );

        component.getDefaultHost().attach(new APIApplication());
        component.start();

//      String sql = "SELECT customer_id, customer_name, customer_email, customer_contact, transaction_time, station_id, station_name, station_location_lat, station_location_long, fuel_quantity, price, fuel_type, payment_method FROM fuel_transactions";
//      String sql = "select customer_id, customer_name, customer_contact, customer_email, startTime, cellId, called_party_number, balance_after, charge, customer_type from postgresql.tcbschema.cdr as cdr inner join bigquery.fedgovds.custprofile as custprofile on customerid = customer_id";
      String sql = "SELECT customer_id, customer_name, customer_contact, customer_email, customer_type FROM bigquery.fedgovds.custprofile AS custprofile ORDER BY customer_id";

       Interceptor inter = new Interceptor();
       System.out.println( inter.getQueryResults( sql, "marketing") );
//       System.out.println( inter.applyDGaC( sql, "marketing") );
       System.out.println( inter.getContract(sql, "marketing" ) );
    }
}

