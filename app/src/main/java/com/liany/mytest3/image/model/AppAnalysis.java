package com.liany.mytest3.image.model;

import com.google.gson.annotations.Expose;

public class AppAnalysis {
    private String footId;
    private String analyId;
    private long regTime = -1l;

    @Expose
    private String sex;
    @Expose
    private String sexVal;
    @Expose
    private String age;
    @Expose
    private String hardAge;
    @Expose
    private String hardAgeRange;
    @Expose
    private String stressAge;
    @Expose
    private String height;
    @Expose
    private String heightVal;
    @Expose
    private String posture;
    @Expose
    private String postureVal;
    @Expose
    private String barefoot;
    @Expose
    private String footwear;
    @Expose
    private String walking;
    @Expose
    private String profession;


    public String getFootId() {
        return footId;
    }

    public void setFootId(String footId) {
        this.footId = footId;
    }

    public String getAnalyId() {
        return analyId;
    }

    public void setAnalyId(String analyId) {
        this.analyId = analyId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSexVal() {
        return sexVal;
    }

    public void setSexVal(String sexVal) {
        this.sexVal = sexVal;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHardAge() {
        return hardAge;
    }

    public void setHardAge(String hardAge) {
        this.hardAge = hardAge;
    }

    public String getHardAgeRange() {
        return hardAgeRange;
    }

    public void setHardAgeRange(String hardAgeRange) {
        this.hardAgeRange = hardAgeRange;
    }

    public String getStressAge() {
        return stressAge;
    }

    public void setStressAge(String stressAge) {
        this.stressAge = stressAge;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getHeightVal() {
        return heightVal;
    }

    public void setHeightVal(String heightVal) {
        this.heightVal = heightVal;
    }

    public String getPosture() {
        return posture;
    }

    public void setPosture(String posture) {
        this.posture = posture;
    }

    public String getPostureVal() {
        return postureVal;
    }

    public void setPostureVal(String postureVal) {
        this.postureVal = postureVal;
    }

    public String getBarefoot() {
        return barefoot;
    }

    public void setBarefoot(String barefoot) {
        this.barefoot = barefoot;
    }

    public String getFootwear() {
        return footwear;
    }

    public void setFootwear(String footwear) {
        this.footwear = footwear;
    }

    public String getWalking() {
        return walking;
    }

    public void setWalking(String walking) {
        this.walking = walking;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

 }
