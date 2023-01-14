package com.mygdx.game.utils.map.geometry;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.utils.Geolocation;

public class Line {
    private final Geolocation from, to;
    private final Color color;

    public Line(Geolocation from, Geolocation to, Color color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }

    public Geolocation getFrom() {
        return from;
    }

    public Geolocation getTo() {
        return to;
    }

    public Color getColor() {
        return color;
    }
}
