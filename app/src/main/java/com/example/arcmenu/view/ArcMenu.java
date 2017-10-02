package com.example.arcmenu.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

import com.example.arcmenu.R;

/**
 * Created by jack on 2017/10/2.
 */

public class ArcMenu  extends ViewGroup implements View.OnClickListener {
    private final int CLOSE = 0;
    private final int OPEN = 1;

    private int mRadius;
    private float mAngle;
    private String[] colorArr = {"#13A1EA","#B024F8","#F68929","#F8248D","#F8B024"};
    /**
     * 菜单的状态
     */
    private int mCurrentStatus = CLOSE;
    /**
     * 菜单的主按钮
     */
    private View mMainButton;

    private OnMenuItemClickListener mMenuItemClickListener;
    /**
     * 点击子菜单项的回调接口
     */
    public interface OnMenuItemClickListener
    {
        void onClick(View view, int pos);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener mMenuItemClickListener){
        this.mMenuItemClickListener = mMenuItemClickListener;
    }

    public ArcMenu(Context context) {
        this(context,null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 360, getResources().getDisplayMetrics());
        // 获取自定义属性的值
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ArcMenu, defStyleAttr, 0);
        mRadius = (int) a.getDimension(R.styleable.ArcMenu_radius, TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 360,
                        getResources().getDisplayMetrics()));
        mAngle = (float) (a.getFloat(R.styleable.ArcMenu_angle, 45) * Math.PI/180);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int count = getChildCount();
        for (int i = 0; i < count; i++){
            // 测量child
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed)
        {
            int count = getChildCount();
            //设置第三个子View为主按钮
            mMainButton = getChildAt(count/2);
            mMainButton.setOnClickListener(this);
            for (int i = 0; i < count; i++)
            {
                View child = getChildAt(i);
                //开始时隐藏非主按钮
                if(!child.equals(mMainButton)){
                    child.setVisibility(View.GONE);
                }
                //子View的左上角坐标（cl,ct）
                int cl = (int) (mRadius * Math.sin(mAngle / (count - 1) * (i-count/2)))
                        + getMeasuredWidth()/2 - child.getMeasuredWidth()/2;
                int ct = (int) (mRadius * Math.cos(mAngle / (count - 1) * (i-count/2))) ;
                //测量的子View的宽，高
                int cWidth = child.getMeasuredWidth();
                int cHeight = child.getMeasuredHeight();
                //设置子view的位置
                child.layout(cl, ct, cl + cWidth, ct + cHeight);
                GradientDrawable myGrad = (GradientDrawable)child.getBackground();
                myGrad.setColor(Color.parseColor(colorArr[i%colorArr.length]));
            }
        }
    }

    @Override
    public void onClick(View v) {
        toggleMenu(300);
    }
    /**
     * 按钮开关，控制按钮的显示与隐藏
     * @param duration 执行动画时间
     */
    private void toggleMenu(int duration) {
        // 为menuItem添加旋转动画
        int count = getChildCount();
        for (int i = 0; i < count; i++){
            if(i!=count/2){
                final View childView = getChildAt(i);
                childView.setVisibility(View.VISIBLE);
                float startAngle = 0;
                float endAngle = (float) (mAngle / (count - 1) * (i-count/2));
                ValueAnimator anim = null;
                // 若当前状态为关闭，则打开按钮 to open
                if (mCurrentStatus == CLOSE){
                    anim = ValueAnimator.ofObject(new AngleEvaluator(), startAngle, endAngle);
                } else {// 若当前状态为打开，则关闭按钮 to close
                    anim = ValueAnimator.ofObject(new AngleEvaluator(), endAngle, startAngle);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            childView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }
                    });
                }
                //监听动画传回的结果，重新设置子View位置
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currentAngle = (Float) animation.getAnimatedValue();
                        int cl = (int) (mRadius * Math.sin(currentAngle)) + getMeasuredWidth()/2 - childView.getMeasuredWidth()/2;
                        int ct = (int) (mRadius * Math.cos(currentAngle)) ;

                        int cWidth = childView.getMeasuredWidth();
                        int cHeight = childView.getMeasuredHeight();
                        childView.layout(cl, ct, cl + cWidth, ct + cHeight);
                    }
                });
                anim.setInterpolator(new AnticipateOvershootInterpolator());
                anim.setDuration(duration);
                anim.start();

                final int pos = i;
                childView.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if (mMenuItemClickListener != null)
                            mMenuItemClickListener.onClick(childView, pos);
                    }
                });
            }
        }
        // 切换菜单状态
        changeStatus();
    }

    private void changeStatus() {
        mCurrentStatus = (mCurrentStatus == CLOSE ? OPEN
                : CLOSE);
    }

    public void setColorArr(String[] colors){
        this.colorArr = colors;
    }
}
