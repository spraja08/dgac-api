/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileStore implements Store {

    private Properties props;
    private java.util.Map<String, JsonObject> memMap = null;

    public FileStore(Properties props) {
        this.props = props;
        // read from the file system and initialise the internal map.
        memMap = new HashMap<String, JsonObject>();
        String strData = null;
        try {
            strData = DGacUtils.readFile(this.props.getProperty("aws.dgac.fileStorePath"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        memMap.put("data", JsonParser.parseString(strData).getAsJsonObject());
    }

    @Override
    public void create(String resource, JsonObject content) {
        memMap.get("data").get(resource).getAsJsonArray().add(content);
        persistStore();
    }

    private void persistStore() {
        try {
            DGacUtils.writeFile(this.props.getProperty("aws.dgac.fileStorePath"), memMap.get("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public JsonArray get(String resource) {
        // getList
        return memMap.get("data").get(resource).getAsJsonArray();
    }

    @Override
    public JsonObject get(String resource, String id) {
        // getOne
        JsonArray records = memMap.get("data").get(resource).getAsJsonArray();
        return getById(records, id);
    }

    private JsonObject getById(JsonArray records, String id) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getAsJsonObject().get("id").getAsString().equals(id))
                return records.get(i).getAsJsonObject();
        }
        return null;
    }

    @Override
    public void delete( String resource, JsonObject content ) {
        String id = content.get("id").getAsString();
        JsonArray res = memMap.get("data").get(resource).getAsJsonArray();
        Iterator<JsonElement> iter = res.iterator();
        while( iter.hasNext() ) {
            JsonObject thisObject = iter.next().getAsJsonObject();
            if( thisObject.get( "id").getAsString().equals( id ) )
                iter.remove();
        }
        persistStore();
    }

    @Override
    public void update(String resource, JsonObject content) {
        delete(resource, content);
        create(resource, content);
        persistStore();
    }
}
