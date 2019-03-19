package es.joapingut.eshoe.dto;

import android.graphics.Color;
import android.graphics.Point;

public class EShoeColorPoint extends Point {

    private int color = Color.BLACK;

    private float force;

    public EShoeColorPoint() {
    }

    public EShoeColorPoint(int x, int y) {
        super(x, y);
    }

    public EShoeColorPoint(Point src) {
        super(src);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getForce() {
        return force;
    }

    public void setForce(float force) {
        this.force = force;
    }
}
