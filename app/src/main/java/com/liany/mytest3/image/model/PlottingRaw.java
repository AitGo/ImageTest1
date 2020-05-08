package com.liany.mytest3.image.model;

public class PlottingRaw {

    String name;
    int type;
    float x1;
    float y1;
    float x2;
    float y2;
    float px;
    float py;
    float[] matrix;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public float getPx() {
        return px;
    }

    public void setPx(float px) {
        this.px = px;
    }

    public float getPy() {
        return py;
    }

    public void setPy(float py) {
        this.py = py;
    }

    public void setPy(int py) {
        this.py = py;
    }

    public void setPosition(float x, float y) {
        this.px = x;
        this.py = y;
    }

    public void setBegin(float x, float y) {
        this.x1 = x;
        this.y2 = y;
    }

    public void setEnd(float x, float y) {
        this.x2 = x;
        this.y2 = y;
    }

    public float[] getMatrix() {
        return matrix;
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

}
