package com.liany.mytest3.image.shape;

import android.graphics.Matrix;

import com.liany.mytest3.image.model.PlottingRaw;


public interface IPlottingSaveable {
    /**
     * 图形的类型
     * @return
     */
    ShapeType getType();

    /**
     * 图形的名称
     * @return
     */
    String getName();

    /**
     * 结构化为JAVA对象
     * @return
     */
    PlottingRaw structuring();

    /**
     * 反结构化
     * @param raw
     * @param referenceMatrix
     */
    void destructuring(PlottingRaw raw, Matrix referenceMatrix);
}
