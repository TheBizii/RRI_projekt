package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.utils.Geolocation;
import com.mygdx.game.utils.MapRasterTiles;
import com.mygdx.game.utils.PixelPosition;
import com.mygdx.game.utils.ZoomXY;
import com.mygdx.game.utils.map.Road;
import com.mygdx.game.utils.map.geometry.Line;

import java.io.IOException;
import java.util.ArrayList;

public class ProjectTest extends ApplicationAdapter implements GestureDetector.GestureListener {

    private ShapeRenderer shapeRenderer;
    private Vector3 touchPosition;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    private final int NUM_TILES = 3;
    private final int ZOOM = 15;
    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);
    private final int WIDTH = MapRasterTiles.TILE_SIZE * NUM_TILES;
    private final int HEIGHT = MapRasterTiles.TILE_SIZE * NUM_TILES;

    private ArrayList<Road> roads;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        camera.viewportWidth = WIDTH / 2f;
        camera.viewportHeight = HEIGHT / 2f;
        camera.zoom = 2f;
        camera.update();

        touchPosition = new Vector3();
        Gdx.input.setInputProcessor(new GestureDetector(this));

        try {
            //in most cases, geolocation won't be in the center of the tile because tile borders are predetermined (geolocation can be at the corner of a tile)
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, NUM_TILES);
            //you need the beginning tile (tile on the top left corner) to convert geolocation to a location in pixels.
            beginTile = new ZoomXY(ZOOM, centerTile.x - ((NUM_TILES - 1) / 2), centerTile.y - ((NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(NUM_TILES, NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);

        roads = Road.retrieveRoadsFromDatabase();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers();
        drawRoads(roads);
    }

    private void drawRoads(ArrayList<Road> roads) {
        for (Road road : roads) {
            for (Line line : road.getGeometry()) {
                PixelPosition fromMarker = MapRasterTiles.getPixelPosition(line.getFrom().lat, line.getFrom().lng, MapRasterTiles.TILE_SIZE, ZOOM, beginTile.x, beginTile.y, HEIGHT);
                PixelPosition toMarker = MapRasterTiles.getPixelPosition(line.getTo().lat, line.getTo().lng, MapRasterTiles.TILE_SIZE, ZOOM, beginTile.x, beginTile.y, HEIGHT);

                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.setColor(line.getColor());
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.rectLine(fromMarker.x, fromMarker.y, toMarker.x, toMarker.y, 5);
                shapeRenderer.end();
            }
        }
    }

    private void drawMarkers() {
        PixelPosition marker = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, MapRasterTiles.TILE_SIZE, ZOOM, beginTile.x, beginTile.y, HEIGHT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(marker.x, marker.y, 10);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX, deltaY);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance)
            camera.zoom += 0.02;
        else
            camera.zoom -= 0.02;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, WIDTH - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, HEIGHT - effectiveViewportHeight / 2f);
    }
}
