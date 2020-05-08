package com.liany.mytest3.image.widget;

public interface IImageEffect {

    /**
     *  图像锐化处理
     */
    void sharpeningProcess();

    /**
     * 图像灰度处理
     */
    void grayProcess();

    /**
     * 图像二值化处理
     */
    void twoValueProcess();

    /**
     * 图像边缘检测处理
     */
    void edgeDetectorProcess();
}
