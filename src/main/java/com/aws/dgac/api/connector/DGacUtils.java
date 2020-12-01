/**
* @author  Raja SP
*/

package com.aws.dgac.api.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class DGacUtils {
    public static String readFile(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        buf.close();
        String fileAsString = sb.toString();
        return fileAsString;
    }

    public static void writeFile( String path, JsonObject content ) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson( content );
        BufferedWriter writer = new BufferedWriter(new FileWriter( path ));
        writer.write( jsonOutput );
        writer.close();
    }
}