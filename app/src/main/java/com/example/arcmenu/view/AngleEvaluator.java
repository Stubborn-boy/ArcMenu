package com.example.arcmenu.view;

import android.animation.TypeEvaluator;

@SuppressWarnings("rawtypes")
public class AngleEvaluator implements TypeEvaluator {
    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        float start = (Float) startValue;
        float end = (Float) endValue;
        float angle = start + fraction * (end-start);  
        return angle;  
    }  
}