package com.liany.mytest3.image;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.liany.mytest3.R;
import com.liany.mytest3.base.BaseFragmentActivity;
import com.liany.mytest3.image.model.AppAnalysis;
import com.liany.mytest3.image.model.AppBasic;
import com.liany.mytest3.image.model.AppFootprint;

import java.io.File;

/**
 * @创建者 ly
 * @创建时间 2020/5/8
 * @描述
 * @更新者 $
 * @更新时间 $
 * @更新描述
 */
public class ImageProcessActivity extends BaseFragmentActivity implements IFootprintAnalyze{

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private MeasureFragment mMeasureFragment;
    private File file;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    protected void initView() {
        addFragment();
        showFragment(mMeasureFragment);
    }

    @Override
    protected void initData() {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        file = (File) getIntent().getSerializableExtra("IntentKey_ImgFile");
    }

    public void addFragment() {
        transaction = fragmentManager.beginTransaction();
        if (mMeasureFragment == null) {
            mMeasureFragment = new MeasureFragment();
            transaction.add(R.id.fragment_container, mMeasureFragment);
        }
        transaction.commit();
    }

    public void showFragment(Fragment fragment) {
        transaction = fragmentManager.beginTransaction();
        transaction.show(fragment);
        transaction.commit();
    }


    @Override
    public AppBasic getAppBasic() {
        return null;
    }

    @Override
    public AppFootprint getAppFootprint() {
        AppFootprint footprint = new AppFootprint();
        footprint.setImage(file.getAbsolutePath());
        return footprint;
    }

    @Override
    public AppAnalysis getAppAnalysis() {
        return null;
    }

    @Override
    public void doAnalyze() {

    }

    @Override
    public boolean isMode(int mode) {
        return false;
    }
}
