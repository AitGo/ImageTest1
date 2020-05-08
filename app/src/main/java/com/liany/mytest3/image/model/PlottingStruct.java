package com.liany.mytest3.image.model;

import java.util.LinkedList;
import java.util.List;

public class PlottingStruct {
    int plottingScaleUnit = 1;
    List<PlottingRaw> plottingDatas = new LinkedList<PlottingRaw>();

    public int getPlottingScaleUnit() {
        return plottingScaleUnit;
    }

    public void setPlottingScaleUnit(int scaleUnit) {
        this.plottingScaleUnit = scaleUnit;
    }

    public List<PlottingRaw> getPlottingDatas() {
        return plottingDatas;
    }

    public void setPlottingDatas(List<PlottingRaw> datas) {
        this.plottingDatas = datas;
    }

    public void addPlottingRaw(PlottingRaw raw){
        this.plottingDatas.add(raw);
    }

}
