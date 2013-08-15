package org.neo4j.example.rest.async;

import java.util.Map;
import java.util.function.Consumer;

import org.neo4j.example.rest.ExecutionResult;

public interface AsyncCypherClient
{
    void query( String statement, Map<String, Object> params, Consumer<ExecutionResult> consumer );
}
