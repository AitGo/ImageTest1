package com.liany.mytest3.image.shape;

public enum ShapeType {
    UNKNOW(-1),
    PLOTTING_SCALE_RULE(0),
    PLOTTING_FREE(1),
    PLOTTING_FOOT_LEN(11),
    PLOTTING_FRONT_WIDTH(12),
    PLOTTING_MIDDLE_WIDTH(13),
    PLOTTING_HEEL_WIDTH(14),
    PLOTTING_HARD_EDGE_LEN(15),
    PLOTTING_STRESS_LEN(16),
    PLOTTING_STRESS_rectangle(17);

    private int type;

    ShapeType(int type) {
        this.type = type;
    }

    public int getValue() {
        return this.type;
    }

    public static ShapeType fetch(int code) {
        for(ShapeType _enum : ShapeType.class.getEnumConstants())
            if(code == _enum.getValue())
                return _enum;
        return null;
    }
}
