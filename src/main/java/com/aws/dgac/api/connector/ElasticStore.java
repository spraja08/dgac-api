/**
*
* @author  Raja SP
*/

package com.aws.dgac.api.connector;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.aws.dgac.api.App;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class ElasticStore implements Store {

    private RestHighLevelClient client = null;

    public ElasticStore(Properties props) throws IOException {
        String elasticSearchIP = App.properties.getProperty("aws.dgac.search_server_ip");
        int elasticSearchPort = Integer.parseInt(App.properties.getProperty("aws.dgac.search_server_port"));

        client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticSearchIP, elasticSearchPort, "http")));
        String strData = null;
        try {
            strData = DGacUtils.readFile(App.properties.getProperty("aws.dgac.fileStorePath"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject config = JsonParser.parseString(strData).getAsJsonObject();
        Set<String> keys = config.keySet();
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            String resource = itr.next();
            createIndex(resource);
            JsonArray docs = config.get(resource).getAsJsonArray();
            for (int i = 0; i < docs.size(); i++) {
                JsonObject doc = docs.get(i).getAsJsonObject();
                this.create(resource, doc);
            }
        }
    }

    private void createIndex(String resource) throws IOException {
        try{ 
            DeleteIndexRequest req = new DeleteIndexRequest(resource.toLowerCase());
            org.elasticsearch.action.support.master.AcknowledgedResponse deleteIndexResponse = client.indices().delete(req,
                    RequestOptions.DEFAULT);
        } catch( Exception e ) {}       
        CreateIndexRequest request = new CreateIndexRequest(resource.toLowerCase());
        CreateIndexResponse getIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Override
    public void create(String resource, JsonObject content) throws IOException {
        IndexRequest request = new IndexRequest(resource.toLowerCase());
        request.id(content.get("id").getAsString());
        request.source(content.toString(), XContentType.JSON);
        IndexResponse indexResponse = this.client.index(request, RequestOptions.DEFAULT);
    }

    @Override
    public JsonArray get(String resource) throws IOException {
        JsonArray results = new JsonArray();
        SearchRequest searchRequest = new SearchRequest(resource.toLowerCase());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (int i = 0; i < hits.length; i++) {
            SearchHit thisHit = hits[i];
            results.add(JsonParser.parseString(thisHit.getSourceAsString()).getAsJsonObject());
        }
        return results;
    }

    @Override
    public JsonArray filteredGet(String resource, String attribute, String value) throws IOException {
        JsonArray results = new JsonArray();
        SearchRequest searchRequest = new SearchRequest(resource.toLowerCase());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery( attribute, value ));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (int i = 0; i < hits.length; i++) {
            SearchHit thisHit = hits[i];
            results.add(JsonParser.parseString(thisHit.getSourceAsString()).getAsJsonObject());
        }
        return results;
    }

    @Override
    public JsonArray search(String resource, String query, int rangeStart, int rangeEnd ) throws IOException {
        JsonArray results = new JsonArray();
        SearchRequest searchRequest = new SearchRequest(resource.toLowerCase());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SimpleQueryStringBuilder qbuilder = QueryBuilders.simpleQueryStringQuery( query );
        qbuilder.fuzzyMaxExpansions();
        searchSourceBuilder.query(qbuilder);
        searchSourceBuilder.from( rangeStart );
        searchSourceBuilder.size( rangeEnd - rangeStart );
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for( int i=0; i<hits.length; i++ ) {
            SearchHit thisHit = hits[i];
            results.add( JsonParser.parseString( thisHit.getSourceAsString() ).getAsJsonObject() );
        }
        return results;
    }

    @Override
    public JsonObject get(String resource, String id) throws IOException {
        // getOne
        GetRequest getRequest = new GetRequest( resource.toLowerCase(), id );
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        if( getResponse.isExists() ) {
            String res = getResponse.getSourceAsString();
            return JsonParser.parseString( res ).getAsJsonObject();
        }
        return null;
    }

    @Override
    public void delete( String resource, JsonObject content ) throws IOException {
        DeleteRequest delRequest = new DeleteRequest( resource.toLowerCase(), content.get( "id").getAsString() );
        DeleteResponse deleteResponse = client.delete( delRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void update(String resource, JsonObject content) throws IOException {
        delete(resource.toLowerCase(), content);
        create(resource.toLowerCase(), content);
    }
}
