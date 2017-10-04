package com.example.arcmenu.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.arcmenu.R;

public class ArcDragMenu extends ViewGroup {

	/**
	 * 当每秒移动角度达到该值时，认为是快速移动
	 */
	private static final double FLINGABLE_VALUE = Math.PI/60f;//3

	/**
	 * 如果移动角度达到该值，则屏蔽点击
	 */
	private static final double NOCLICK_VALUE = Math.PI / 90f;//2
	/**
	 * 菜单项的图标
	 */
	private int[] mItemImgs;

	/**
	 * 菜单的个数
	 */
	private int mMenuItemCount;
	
	private int mVisiableItemCount;
	
	/**
	 * 检测按下到抬起时旋转的角度
	 */
	private float mTmpAngle;
	/**
	 * 检测按下时的时间
	 */
	private long mDownTime;
	/**
	 * 判断是否正在自动滚动
	 */
	private boolean isFling;
	/**
	 * 自动滚动的Runnable
	 */
	private AutoFlingRunnable mFlingRunnable;
	/**
	 * 半径
	 */
	private int mRadius;
	/**
	 * 布局时的开始角度
	 */
	private double mInitialAngle = 0;
	/**
	 * 当前角度
	 */
	private double mCurrAngle = 0;
	/**
	 * 计算间隔角度
	 */
	double angleDelay;
	
	private int mMenuItemLayoutId = R.layout.item_arcdragmenu;
	
	private OnMenuItemClickListener mMenuItemClickListener;
	/**
	 * 点击子菜单项的回调接口
	 */
	public interface OnMenuItemClickListener{
		void onItemClick(View view, int position);
	}

	public void setOnMenuItemClickListener(OnMenuItemClickListener mMenuItemClickListener) {
		this.mMenuItemClickListener = mMenuItemClickListener;
	}

	public ArcDragMenu(Context context) {
		this(context,null);
	}

	public ArcDragMenu(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public ArcDragMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 360, getResources().getDisplayMetrics());
		// 获取自定义属性的值
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ArcDragMenu, defStyleAttr, 0);
		mRadius = (int) a.getDimension(R.styleable.ArcDragMenu_mradius, TypedValue
				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 360,
						getResources().getDisplayMetrics()));
		mVisiableItemCount = (int) a.getInteger(R.styleable.ArcDragMenu_visibleitemcount, 5);
		a.recycle();
		setWillNotDraw(false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		int count = getChildCount();
		for (int i = 0; i < count; i++){
			// 测量child
			measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		angleDelay = Math.asin((getMeasuredWidth()/2.0)/mRadius)*2/ mVisiableItemCount;
		mInitialAngle = angleDelay *(-(mVisiableItemCount /2.0-0.5));
		if(mCurrAngle ==0){
			mCurrAngle = mInitialAngle;
		}
		double angle = mCurrAngle;
		int count = getChildCount();
		for (int i = 0; i < count; i++){
			View child = getChildAt(i);
			//子View的左上角坐标（cl,ct）
			int cl = (int) (mRadius * Math.sin(angle)) + getMeasuredWidth()/2 - child.getMeasuredWidth()/2;
			int ct = (int) (mRadius * Math.cos(angle)) ;
			//测量的子View的宽，高
			int cWidth = child.getMeasuredWidth();
			int cHeight = child.getMeasuredHeight();
			//设置子view的位置
			child.layout(cl, ct, cl + cWidth, ct + cHeight);
			angle += angleDelay;
		}
	}
	
	/**
	 * 记录上一次的x，y坐标
	 */
	private float mLastX;
	private float mLastY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float x = ev.getRawX();
		float y = ev.getRawY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastX = x;
				mLastY = y;
				mDownTime = System.currentTimeMillis();
				mTmpAngle = 0;
				// 如果当前已经在快速滚动
				if (isFling){
					// 移除快速滚动的回调
					removeCallbacks(mFlingRunnable);
					isFling = false;
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				/**
				 * 获得开始的角度
				 */
				float start = getAngle(mLastX, mLastY);
				/**
				 * 获得当前的角度
				 */
				float end = getAngle(x, y);
				float dr = end - start;
				//防止超出范围，左滑到最后一个，右滑到第一个就不能再滑了
				if(mCurrAngle + dr <= mInitialAngle && mCurrAngle + dr >= mInitialAngle - (mMenuItemCount- mVisiableItemCount)*angleDelay){
					mCurrAngle += dr;
				}

				mTmpAngle += end - start;
				// 重新布局
				requestLayout();

				mLastX = x;
				mLastY = y;
				break;
			case MotionEvent.ACTION_UP:
				// 计算每秒移动的角度
				float anglePerSecond = mTmpAngle * 1000
						/ (System.currentTimeMillis() - mDownTime);
				// 如果达到该值认为是快速移动
				if (Math.abs(anglePerSecond) > FLINGABLE_VALUE && !isFling) {
					// post一个任务，去自动滚动
					post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));

					return true;
				}

				// 如果当前旋转角度超过NOCLICK_VALUE屏蔽点击
				if (Math.abs(mTmpAngle) > NOCLICK_VALUE || System.currentTimeMillis()-mDownTime >500) {
					return true;
				}
				break;

			default:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private float getAngle(float xTouch, float yTouch) {
		double x = xTouch - getMeasuredWidth()/2;
		double y = yTouch;
		return (float) (Math.asin(x / Math.hypot(x, y)));//其中Math.hypot(x, y)为sqrt(x2 +y2)
	}
	
	/**
	 * 设置菜单条目的图标和文本
	 * 
	 * @param resIds
	 */
	public void setMenuItemIcons(int[] resIds) {
		mItemImgs = resIds;
		// 参数检查
		if (resIds == null) {
			throw new IllegalArgumentException("菜单项至少设置一项");
		}
		// 设置mMenuCount
		if (resIds != null) {
			mMenuItemCount = resIds.length;
		}
		addMenuItems();
	}

	/**
	 * 设置MenuItem的布局文件，必须在setMenuItemIcons之前调用
	 * 
	 * @param mMenuItemLayoutId
	 */
	public void setMenuItemLayoutId(int mMenuItemLayoutId){
		this.mMenuItemLayoutId = mMenuItemLayoutId;
	}

	/**
	 * 添加菜单项
	 */
	private void addMenuItems(){
		LayoutInflater mInflater = LayoutInflater.from(getContext());

		/**
		 * 根据用户设置的参数，初始化view
		 */
		for (int i = 0; i < mMenuItemCount; i++){
			final int j = i;
			View view = mInflater.inflate(mMenuItemLayoutId, this, false);
			ImageView iv = (ImageView) view.findViewById(R.id.iv_item);
			if (iv != null){
				iv.setVisibility(View.VISIBLE);
				iv.setImageResource(mItemImgs[i]);
				iv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v){
						if (mMenuItemClickListener != null){
							mMenuItemClickListener.onItemClick(v, j);
						}
					}
				});
			}
			// 添加view到容器中
			addView(view);
		}
	}
	
	/**
	 * 自动滚动的任务
	 */
	private class AutoFlingRunnable implements Runnable{

		private float angelPerSecond;

		public AutoFlingRunnable(float velocity)
		{
			this.angelPerSecond = velocity;
		}

		public void run(){
			// 如果小于0.1,则停止
			if (Math.abs(angelPerSecond) < 0.1f){
				isFling = false;
				return;
			}
			isFling = true;
			// 不断改变mCurrAngle ，让其滚动，/60为了避免滚动太快
			float dr = (angelPerSecond / 60);
			if(mCurrAngle + dr <= mInitialAngle && mCurrAngle + dr >= mInitialAngle - (mMenuItemCount- mVisiableItemCount)*angleDelay){
				mCurrAngle += dr;
			}else if(mCurrAngle + dr <= mInitialAngle){
				mCurrAngle = mInitialAngle - (mMenuItemCount- mVisiableItemCount)*angleDelay;
			}else if(mCurrAngle + dr >= mInitialAngle - (mMenuItemCount- mVisiableItemCount)*angleDelay){
				mCurrAngle = mInitialAngle;
			}
			// 逐渐减小这个值
			angelPerSecond /= 1.066f;
			postDelayed(this, 10);
			// 重新布局
			requestLayout();
		}
	}

	/**
	 *  判断一个点是否在封闭的Path内或不规则的图形内
	 * http://blog.csdn.net/nn955/article/details/49784341
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isInViewRect(float x,float y) {
		Path mPath = new Path();
		RectF mRectF = new RectF(getMeasuredWidth()/2-mRadius, -mRadius, getMeasuredWidth()/2+mRadius, mRadius);
		mPath.arcTo(mRectF, 60, 60);
		RectF mRectF1 = new RectF(getMeasuredWidth()/2-(mRadius+60), -(mRadius+60), getMeasuredWidth()/2+(mRadius+60), mRadius+60);
		mPath.arcTo(mRectF1, 120, -60);

		Region re=new Region();

		RectF r=new RectF();
		//计算控制点的边界
		mPath.computeBounds(r, true);

		//设置区域路径和剪辑描述的区域
		re.setPath(mPath, new Region((int)r.left,(int)r.top,(int)r.right,(int)r.bottom));

		return re.contains((int)x, (int)y);
	}

}
