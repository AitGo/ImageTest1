package com.liany.mytest3.image;

import com.liany.mytest3.image.model.AppAnalysis;
import com.liany.mytest3.image.model.AppBasic;
import com.liany.mytest3.image.model.AppFootprint;

public interface IFootprintAnalyze {

    final static int MODE_NEW = 0x1;
    final static int MODE_EDIT = 0x2;
    final static int MODE_VIEW = 0x3;
    final static int MODE_NEW_ANALYSIS = 0x4;

    AppBasic getAppBasic();

    AppFootprint getAppFootprint();

    AppAnalysis getAppAnalysis();

    void doAnalyze();

    boolean isMode(int mode);
}
