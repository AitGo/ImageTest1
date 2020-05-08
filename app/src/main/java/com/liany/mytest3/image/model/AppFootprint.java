package com.liany.mytest3.image.model;

public class AppFootprint {
    private String userId;      //用户ID
    private String footId;      //足迹资料ID
    private String footNum;     //足迹编号
    private String image;       //足迹图像路径
    private float ratio = 1f;        // ??
    private long regTime = -1l;       //添加时间

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFootId() {
        return footId;
    }

    public void setFootId(String footId) {
        this.footId = footId;
    }

    public String getFootNum() {
        return footNum;
    }

    public void setFootNum(String footNum) {
        this.footNum = footNum;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }
}
