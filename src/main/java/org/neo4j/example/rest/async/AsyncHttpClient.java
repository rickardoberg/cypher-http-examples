package org.neo4j.example.rest.async;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;

import org.neo4j.example.rest.ExecutionResult;
import org.neo4j.example.rest.Util;

public class AsyncHttpClient
    implements AsyncCypherClient, AutoCloseable
{
    private final CloseableHttpAsyncClient httpclient;
    private final String uri;

    public AsyncHttpClient(String baseUri)
    {
        this.uri = Util.toCypherUri( baseUri );

        httpclient = HttpAsyncClients.
                custom().
                setDefaultRequestConfig( RequestConfig.copy( RequestConfig.DEFAULT ).build() ).
                build();
        httpclient.start();
    }

    public void query( String statement, Map<String, Object> params, final Consumer<ExecutionResult> consumer )
    {
        HttpPost httpRequest = new HttpPost( uri );
        httpRequest.setEntity( new StringEntity( Util.toJson(Util.createPostData(statement, params)), ContentType.APPLICATION_JSON) );

        httpclient.execute( HttpAsyncMethods.create( httpRequest ), new AsyncCharConsumer<String>()
        {
                    StringBuffer buffer = new StringBuffer(  );

                    @Override
                    protected void onCharReceived( CharBuffer buf, IOControl ioctrl ) throws IOException
                    {
                        buffer.append( buf.toString() );
                    }

                    @Override
                    protected void onResponseReceived( HttpResponse response ) throws HttpException, IOException
                    {
                    }

                    @Override
                    protected String buildResult( HttpContext context ) throws Exception
                    {
                        return buffer.toString();
                    }
                }, new FutureCallback<String>()

        {
            public void completed( String result )
            {
                ExecutionResult executionResult = Util.toResult( 200, result );

                consumer.accept( executionResult );
            }

            public void failed( Exception ex )
            {
            }

            public void cancelled()
            {
            }
        });


    }

    public void close() throws Exception
    {
        httpclient.close();
    }
}
