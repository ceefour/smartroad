package com.hendyirawan.smartroad.core;

import org.opencv.core.Mat;

import javax.annotation.Nullable;

/**
 * Created by ceefour on 5/13/15.
 */
public class RoadAnalysis {
    public Mat original;
    public Mat blurred;
    public Mat edges;
    public Mat augmented;

    public RoadDamageLevel damageLevel;
    public RoadDamageKind damageKind;
    public Integer potholeCount;
    @Nullable
    public Double totalPotholeWidth;
    @Nullable
    public Double totalPotholeLength;
    @Nullable
    public Double totalPotholeDepth;
    @Nullable
    public Double totalPotholeArea;
}
