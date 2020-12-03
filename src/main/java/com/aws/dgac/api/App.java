/**
* REST API - Restlet main entry point
*
* @author  Raja SP
*/

package com.aws.dgac.api;

import java.util.Properties;
import com.aws.dgac.api.connector.FileStore;
import com.aws.dgac.api.connector.Store;
import com.aws.dgac.api.interceptor.Interceptor;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class App {

    public static Store store = null;

    public static void main(String[] args) throws Exception {
        /*
        if( args.length < 2 ) {
                System.err.println( "Usage : java -jar entity-analytics.jar <path to the properties file> <port>" );
                return;
        }*/
        Component component = new Component();
        //int port = Integer.parseInt( args[1] );
        int port = 5000;
        component.getServers().add(Protocol.HTTP, port);

        //InputStream input = new FileInputStream( args[0] );
        Properties props = new Properties();
        props.setProperty( "aws.dgac.fileStorePath", "/Users/rspamzn/Documents/DAML/Assets/DGaC/app/dgac-api/resources/db.json" );
        //props.load( input );
        store = new FileStore( props );
        component.getDefaultHost().attach(new APIApplication());
        component.start();

//       String sql = "SELECT customer_id, customer_name, customer_email, customer_contact, transaction_time, station_id, station_name, station_location_lat, station_location_long, fuel_quantity, price, fuel_type, payment_method FROM fuel_transactions";
//      String sql = "SELECT customer_id, customer_name, customer_email, customer_contact, transaction_time, station_id, station_name FROM fuel_transactions";
//        Interceptor inter = new Interceptor();
//        System.out.println( inter.getQueryResults( sql, "superUser") );
    }
}