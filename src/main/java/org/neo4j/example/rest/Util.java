package org.neo4j.example.rest;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class Util {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toCypherUri( String baseUri ) {
        try {
            return new URL(new URL(baseUri), "/db/data/cypher").toString();
        } catch (Exception e) {
            throw new RuntimeException("Error constructing cypher uri from " + baseUri, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map createPostData( String statement, Map<String, Object> params ) {
        Map postData = new HashMap();
        postData.put("query", statement);
        postData.put("params", params == null ? Collections.EMPTY_MAP : params);
        return postData;
    }

    public static String toJson( Map postData ) {
        try {
            return OBJECT_MAPPER.writeValueAsString(postData);
        } catch (IOException e) {
            throw new RuntimeException("Can't convert " + postData + " to json", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static ExecutionResult toResult(int status, String resultString) {
        if (status != 200) throw new IllegalStateException("Response status " + status + "\n" + resultString);
        Map map = toMap(resultString);
        ExecutionResultImpl result = new ExecutionResultImpl((List<String>) map.get("columns"));
        result.addRows((List<List<Object>>) map.get("data"));
        return result;
    }

    private static Map toMap(String value) {
        try {
            return OBJECT_MAPPER.readValue(value, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Can't deserialize to Map:\n"+value,e);
        }
    }
}
