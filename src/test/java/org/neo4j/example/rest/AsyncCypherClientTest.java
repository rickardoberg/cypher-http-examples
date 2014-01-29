package org.neo4j.example.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.neo4j.example.rest.async.AsyncCypherClient;
import org.neo4j.example.rest.async.AsyncHttpClient;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class AsyncCypherClientTest
{

    private static int NODE_ID;
    private static LocalTestServer server;
    private final AsyncCypherClient client;

    @BeforeClass
    public static void beforeClass()
    {
        server = new LocalTestServer();
        server.start();
        NODE_ID = (int) createNode();
    }

    private static long createNode()
    {
        GraphDatabaseService db = server.getGraphDatabase();
        Transaction tx = db.beginTx();
        long id = db.createNode().getId();
        tx.success();
        tx.finish();
        return id;
    }

    @AfterClass
    public static void afterClass()
    {
        server.stop();
    }

    public AsyncCypherClientTest( Clients clients )
    {
        client = clients.create( server.getBaseUrl() );
    }

    enum Clients
    {
        HTTPCLIENT()
                {
                    public AsyncCypherClient create( String url )
                    {
                        return new AsyncHttpClient( url );
                    }
                };

        public abstract AsyncCypherClient create( String url );
    }

    @Parameterized.Parameters(name = "Client: {0}")
    public static Iterable<Object[]> parameters()
    {
        return Arrays.asList( new Object[][]{{Clients.HTTPCLIENT}} );
    }

    @Test
    public void testAsyncQuery()
    {
        final CompletableFuture<ExecutionResult> resultFuture = new CompletableFuture<ExecutionResult>();

        client.query( "start n=node({id}) return id(n) as id", Collections.<String, Object>singletonMap( "id",
                NODE_ID ), new Consumer<ExecutionResult>()
        {
            public void accept( ExecutionResult lists )
            {
                resultFuture.complete( lists );
            }
        } );

        ExecutionResult result = resultFuture.join();

        assertEquals( asList( "id" ), result.getColumns() );

        assertEquals( NODE_ID, result.iterator().next().get( 0 ) );

        Iterator<Map<String, Object>> rowIterator = result.rowIterator();
        assertEquals( true, rowIterator.hasNext() );
        Map<String, Object> row = rowIterator.next();
        assertEquals( 1, row.size() );
        assertEquals( true, row.containsKey( "id" ) );
        assertEquals( NODE_ID, row.get( "id" ) );
    }
}
