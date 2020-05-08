package com.liany.mytest3.image.model;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import java.util.HashSet;
import java.util.Iterator;

public class AppBasic {

    private String basicId;     //基础信息
    private String analyId;     //分析结果UUID
    private String drawings;    //JSON格式化的测量线绘制结构

    private boolean changed = false;
    private boolean analyzed = false;   //用于判断是否依据此基础信息完成分析操作

    @Expose
    private String feet; //足迹类型（左右脚）
    @Expose
    private String barefoot; //是否赤脚
    @Expose
    private String footwear; //鞋子类型
    @Expose
    private float edgeLength; //鞋边长度
    @Expose
    private float footLength; //足迹长度（cm）
    @Expose
    private float frontWidth; //前掌宽度（cm）
    @Expose
    private float middleWidth; //中腰宽度（cm）
    @Expose
    private float heelWidth; //后跟宽度（cm）

    @Expose
    private HashSet<String> gait; //痕迹属性
    @Expose
    private String impress;   //压痕种类
    @Expose
    private String lift;   //起脚类型
    @Expose
    private String stay;   //落脚类型
    @Expose
    private String scair;   //压痕种类

    @Expose
    private String remnant;   //遗留问题
    @Expose
    private String walking;   //行走动作
    @Expose
    private String unsettled;   //未尽事宜


    @Expose
    private String fptype;      //足迹类型
    @Expose
    private float elevation;      //立体平面差
    @Expose
    private float hardEdgeLength;  //实边长
    @Expose
    private String ageRange;      //年龄段
    @Expose
    private float stressLength;   //重压面长度

    public AppBasic() {
        gait = new HashSet<>();

        PlottingStruct struct = new PlottingStruct();
        Gson gson = new Gson();
        String json = gson.toJson(struct, PlottingStruct.class);
        setDrawings(json);
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    public String getBasicId() {
        return basicId;
    }

    public void setBasicId(String basicId) {
        this.basicId = basicId;
    }

    public String getAnalyId() {
        return analyId;
    }

    public void setAnalyId(String analyId) {
        this.analyId = analyId;
    }

    public String getDrawings() {
        return drawings;
    }

    public void setDrawings(String drawings) {
        this.drawings = drawings;
    }

    public String getFeet() {
        return feet;
    }

    public void setFeet(String feet) {
        propertyChange(this.feet == null ? "" : this.feet, feet == null ? "" : feet);
        this.feet = feet;
    }

    public String getFootwear() {
        return footwear;
    }

    public void setFootwear(String footwear) {
        propertyChange(this.footwear == null ? "" : this.footwear, footwear == null ? "" : footwear);
        this.footwear = footwear;
    }

    public float getEdgeLength() {
        return edgeLength;
    }

    public void setEdgeLength(float edgeLength) {
        propertyChange(Float.valueOf(this.edgeLength), Float.valueOf(edgeLength));
        this.edgeLength = edgeLength;
    }

    public float getFootLength() {
        return footLength;
    }

    public void setFootLength(float footLength) {
        propertyChange(Float.valueOf(this.footLength), Float.valueOf(footLength));
        this.footLength = footLength;
    }

    public float getFrontWidth() {
        return frontWidth;
    }

    public void setFrontWidth(float frontWidth) {
        propertyChange(Float.valueOf(this.frontWidth), Float.valueOf(frontWidth));
        this.frontWidth = frontWidth;
    }

    public float getMiddleWidth() {
        return middleWidth;
    }

    public void setMiddleWidth(float middleWidth) {
        propertyChange(Float.valueOf(this.middleWidth), Float.valueOf(middleWidth));
        this.middleWidth = middleWidth;
    }

    public float getHeelWidth() {
        return heelWidth;
    }

    public void setHeelWidth(float heelWidth) {
        propertyChange(Float.valueOf(this.heelWidth), Float.valueOf(heelWidth));
        this.heelWidth = heelWidth;
    }

    public HashSet<String> getGait() {
        return gait;
    }

    public void setGait(String gait, boolean checked) {
        String old = getGaitAsString();
        if (checked) {
            this.gait.add(gait);
        } else {
            this.gait.remove(gait);
        }
        String val = getGaitAsString();
        propertyChange(old, val);
    }

    public String getImpress() {
        return impress;
    }

    public void setImpress(String impress) {
        propertyChange(this.impress == null ? "" : this.impress, impress == null ? "" : impress);
        this.impress = impress;
    }

    public String getLift() {
        return lift;
    }

    public void setLift(String lift) {
        propertyChange(this.lift == null ? "" : this.lift, lift == null ? "" : lift);
        this.lift = lift;
    }

    public String getStay() {
        return stay;
    }

    public void setStay(String stay) {
        propertyChange(this.stay == null ? "" : this.stay, stay == null ? "" : stay);
        this.stay = stay;
    }

    public String getScair() {
        return scair;
    }

    public void setScair(String scair) {
        propertyChange(this.scair == null ? "" : this.scair, scair == null ? "" : scair);
        this.scair = scair;
    }

    public String getRemnant() {
        return remnant;
    }

    public void setRemnant(String remnant) {
        this.remnant = remnant;
        setChanged(true);
    }

    public String getWalking() {
        return walking;
    }

    public void setWalking(String walking) {
        this.walking = walking;
        setChanged(true);
    }

    public String getUnsettled() {
        return unsettled;
    }

    public void setUnsettled(String unsettled) {
        this.unsettled = unsettled;
        setChanged(true);
    }

    public String getBarefoot() {
        return barefoot;
    }

    public void setBarefoot(String barefoot) {
        propertyChange(this.barefoot == null ? "" : this.barefoot, barefoot == null ? "" : barefoot);
        this.barefoot = barefoot;
    }

    public String getGaitAsString() {
        Iterator<String> iterator = gait.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String s = iterator.next();
            sb.append(s);
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    public String getFptype() {
        return fptype;
    }

    public void setFptype(String fptype) {
        propertyChange(this.fptype == null ? "" : this.fptype, fptype == null ? "" : fptype);
        this.fptype = fptype;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        propertyChange(Float.valueOf(this.elevation), Float.valueOf(elevation));
        this.elevation = elevation;
    }

    public float getHardEdgeLength() {
        return hardEdgeLength;
    }

    public void setHardEdgeLength(float hardEdgeLength) {
        propertyChange(Float.valueOf(this.hardEdgeLength), Float.valueOf(hardEdgeLength));
        this.hardEdgeLength = hardEdgeLength;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        propertyChange(this.ageRange == null ? "" : this.ageRange, ageRange == null ? "" : ageRange);
        this.ageRange = ageRange;
    }

    public float getStressLength() {
        return stressLength;
    }

    public void setStressLength(float stressLength) {
        propertyChange(Float.valueOf(this.stressLength), Float.valueOf(stressLength));
        this.stressLength = stressLength;
    }

    private void propertyChange(Object val1, Object val2) {
        boolean same = val1.equals(val2);
        if (this.analyzed) {
            this.analyzed = same;
        }
        if (!same) {
            setChanged(true);
        }
    }

    @Override
    public String toString() {
        return "AppBasic{" +
                "basicId='" + basicId + '\'' +
                ", analyId='" + analyId + '\'' +
                ", drawings='" + drawings + '\'' +
                ", changed=" + changed +
                ", analyzed=" + analyzed +
                ", feet='" + feet + '\'' +
                ", barefoot='" + barefoot + '\'' +
                ", footwear='" + footwear + '\'' +
                ", edgeLength=" + edgeLength +
                ", footLength=" + footLength +
                ", frontWidth=" + frontWidth +
                ", middleWidth=" + middleWidth +
                ", heelWidth=" + heelWidth +
                ", gait=" + gait +
                ", impress='" + impress + '\'' +
                ", lift='" + lift + '\'' +
                ", stay='" + stay + '\'' +
                ", scair='" + scair + '\'' +
                ", remnant='" + remnant + '\'' +
                ", walking='" + walking + '\'' +
                ", unsettled='" + unsettled + '\'' +
                ", fptype='" + fptype + '\'' +
                ", elevation=" + elevation +
                ", hardEdgeLength=" + hardEdgeLength +
                ", ageRange='" + ageRange + '\'' +
                ", stressLength=" + stressLength +
                '}';
    }
}
