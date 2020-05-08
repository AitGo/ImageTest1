package com.liany.mytest3.image.shape;

public interface IEventful {
    final static int LONEPRESS = 800;

    void onDelete();

    void beforeDraw();

    void onTransforming();

    void afterDragDraw(DrawableShape shape);

    void onPress(float x, float y);

    void onDoubleClick();

    void onLongPress(float x, float y);

    void onPressHandler(float x, float y);

    void onLongPressHandler(float x, float y);

    void onHandlerMove(float x, float y);
}
