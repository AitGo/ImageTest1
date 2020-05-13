package com.liany.mytest3.image.widget;

public interface IImagePlotting {

    float getFootLength();

    float getFrontWidth();

    float getMiddleWidth();

    float getHeelWidth();

    float getHardEdgeLength();

    float getStressLength();

    /**
     * 绘制比例尺
     */
    void drawPlottingScale();

    /**
     * 绘制自由测量线
     */
    void drawFreeLine();

    /**
     * 绘制足长测量线
     */
    void drawFootLengthLine();

    /**
     * 绘制前掌测量线
     */
    void drawFrontWidthLine();

    /**
     * 绘制中腰测量线
     */
    void drawMiddleWidthLine();

    /**
     * 绘制后跟测量线
     */
    void drawHeelWidthLine();

    /**
     * 绘制实边长
     */
    void drawHardEdgeLengthLine();

    /**
     * 绘制重压面长
     */
    void drawStressLengthLine();

    /**
     * 绘制矩形框
     */
    void drawRectangle();

    /**
     * 删除当前选中的测量线
     */
    void removeLine();

    void showMeasureLine();

    void hideMeasureLine();
}
