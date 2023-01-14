package com.mygdx.game.utils.map;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mygdx.game.utils.Geolocation;
import com.mygdx.game.utils.database.MongoDBConnection;
import com.mygdx.game.utils.map.geometry.Line;

import org.bson.Document;

import java.util.ArrayList;

public class Road {
    private final String description;
    private final ArrayList<Line> geometry;

    public Road(String description, ArrayList<Line> geometry) {
        this.description = description;
        this.geometry = geometry;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Line> getGeometry() {
        return geometry;
    }

    public static ArrayList<Road> retrieveRoadsFromDatabase() {
        ArrayList<Road> roads = new ArrayList<>();

        MongoDBConnection database = new MongoDBConnection();
        MongoCollection<Document> roadsCollection = database.getDatabase().getCollection("roads");

        for (Document document : roadsCollection.find()) {
            JsonObject obj = new Gson().fromJson(document.toJson(), JsonObject.class);
            JsonArray features = obj.getAsJsonArray("features");
            for(JsonElement feature : features) {
                JsonObject featureObject = feature.getAsJsonObject();
                JsonObject properties = featureObject.getAsJsonObject("properties");
                JsonObject geometry = featureObject.getAsJsonObject("geometry");

                String roadDescription = properties.get("OPIS").getAsString();
                ArrayList<Line> roadGeometry = new ArrayList<>();
                JsonArray coordinates = geometry.getAsJsonArray("coordinates");

                for(JsonElement coordinateArray : coordinates) {
                    JsonArray coordArr = coordinateArray.getAsJsonArray();
                    for(int i = 0; i < coordArr.size(); i++) {
                        if(coordArr.size() - 1 == i) break;

                        JsonElement start = coordArr.get(i);
                        JsonElement end = coordArr.get(i + 1);
                        JsonArray startCoordinate = start.getAsJsonArray();
                        JsonArray endCoordinate = end.getAsJsonArray();
                        double lng = startCoordinate.get(0).getAsDouble();
                        double lat = startCoordinate.get(1).getAsDouble();
                        Geolocation startPoint = new Geolocation(lat, lng);
                        lng = endCoordinate.get(0).getAsDouble();
                        lat = endCoordinate.get(1).getAsDouble();
                        Geolocation endPoint = new Geolocation(lat, lng);
                        Line line = new Line(startPoint, endPoint, Color.BLUE);
                        roadGeometry.add(line);
                    }
                }

                Road road = new Road(roadDescription, roadGeometry);
                roads.add(road);
            }
        }

        database.disconnect();

        return roads;
    }
}
