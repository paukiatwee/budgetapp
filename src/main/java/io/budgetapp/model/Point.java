package io.budgetapp.model;

import java.io.Serializable;

/**
 *
 */
public class Point implements Serializable {

    private static final long serialVersionUID = -4254482540288351126L;
    private String label;
    private long key;
    private double value;
    private PointType pointType;

    public Point(String label, long key, double value, PointType pointType) {
        this.label = label;
        this.key = key;
        this.value = value;
        this.pointType = pointType;
    }

    public String getLabel() {
        return label;
    }

    public long getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    public PointType getPointType() {
        return pointType;
    }
}
