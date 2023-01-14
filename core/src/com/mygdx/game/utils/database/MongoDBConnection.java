package com.mygdx.game.utils.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mygdx.game.secrets.Secrets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MongoDBConnection {
    private ConnectionString connString;
    private MongoClient client;
    private MongoDatabase database;

    public MongoDBConnection() {
        this(Secrets.MONGODB_USERNAME, Secrets.MONGODB_PASSWORD, Secrets.MONGODB_CLUSTER);
    }

    public MongoDBConnection(String username, String password, String cluster) {
        try {
            username = URLEncoder.encode(username, "UTF-8");
            password = URLEncoder.encode(password, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String connectionString = "mongodb+srv://" + username + ":" + password + "@" + cluster + "/?retryWrites=true&w=majority";
        this.connString = new ConnectionString(connectionString);
        connect();
    }

    public void connect() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        client = MongoClients.create(settings);
        database = client.getDatabase("test");

        System.out.println("Connected to MongoDB!");
    }

    public void disconnect() {
        client.close();
    }
}
