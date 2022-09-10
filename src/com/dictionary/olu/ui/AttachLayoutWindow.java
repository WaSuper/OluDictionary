package com.dictionary.olu.ui;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

/**
 * 作为添加到WindowManager的根布局
 */
public class AttachLayoutWindow extends LinearLayout {

    public static final int DELAY_MILLIS_GO_EDGE = 3000;
    private static final String TAG = "AttachButton";
    protected float mLastRawX;
    protected float mLastRawY;
    protected boolean isDrug = false;
    protected int mRootMeasuredWidth = 0;
    protected int mRootMeasuredHeight = 0;
    protected int mRootTopY = 0;
    protected int customAttachDirect; /*-1不吸附 0所有的边 1左 2上 3右 4下 5左右 6上下 */
    protected boolean customIsDrag;
    protected boolean touchIsTargetView = true;
    protected View targetView;
    private WindowManager windowManager;
    private Handler handler;
    private GoEdgeRunnable goEdge;
    private boolean isNeedGoEdge = false;

    public AttachLayoutWindow(Context context) {
        this(context, null, 0);
    }

    public AttachLayoutWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AttachLayoutWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        windowManager = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int statusBarHeight = getStatusBarHeight();
        mRootMeasuredHeight = displayMetrics.heightPixels - statusBarHeight;
        mRootMeasuredWidth = displayMetrics.widthPixels;
        mRootTopY = statusBarHeight;
        customAttachDirect = 5;
        customIsDrag = true;
        handler = new android.os.Handler();
        goEdge = new GoEdgeRunnable();

    }
    
    public void setIsNeedGoEdge(boolean isNeed) {
    	isNeedGoEdge = isNeed;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }

    private void log(String s) {
        //System.out.println("AttachLayoutWindow" + s);
    }

    protected void doTouch(MotionEvent ev) {
        log("-----------------------------------");
        log("doTouch() called with: ev = [" + ev + "]");
        if (customIsDrag) {
            //当前手指的坐标
            float mRawX = ev.getRawX();
            float mRawY = ev.getRawY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN://手指按下
                    if (isNeedGoEdge) handler.removeCallbacks(goEdge);
                    isDrug = false;
                    //记录按下的位置
                    mLastRawX = mRawX;
                    mLastRawY = mRawY;
                    break;
                case MotionEvent.ACTION_MOVE://手指滑动
                    if (mRawX >= 0 && mRawX <= mRootMeasuredWidth && mRawY >= mRootTopY && mRawY <= (mRootMeasuredHeight + mRootTopY)) {
                        //手指X轴滑动距离
                        float differenceValueX = mRawX - mLastRawX;
                        //手指Y轴滑动距离
                        float differenceValueY = mRawY - mLastRawY;
                        log("doTouch()  move called with: differenceValueX = [" + differenceValueX + "]");
                        log("doTouch()  move called with: differenceValueY = [" + differenceValueY + "]");
                        //判断是否为拖动操作
                        if (!isDrug) {
                            if (Math.sqrt(differenceValueX * differenceValueX + differenceValueY * differenceValueY) < 2) {
                                isDrug = false;
                            } else {
                                isDrug = true;
                            }
                        }
                        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
                        //获取手指按下的距离与控件本身X轴的距离
                        float ownX = layoutParams.x;
                        //获取手指按下的距离与控件本身Y轴的距离
                        float ownY = layoutParams.y;
                        log("doTouch()  move called with: ownX = [" + ownX + "]");
                        log("doTouch()  move called with: ownY = [" + ownY + "]");
                        //理论中X轴拖动的距离
                        float endX = ownX + differenceValueX;
                        //理论中Y轴拖动的距离
                        float endY = ownY + differenceValueY;
                        log("doTouch()  move called with: endX = [" + endX + "]");
                        log("doTouch()  move called with: endX = [" + endX + "]");

                        log("doTouch()  move called with: getWidth() = [" + getWidth() + "]");
                        log("doTouch()  move called with: getHeight() = [" + getHeight() + "]");
                        //X轴可以拖动的最大距离
                        float maxX = mRootMeasuredWidth - getWidth();
                        //Y轴可以拖动的最大距离
                        float maxY = mRootMeasuredHeight - getHeight();
                        log("doTouch()  move called with: maxX = [" + maxX + "]");
                        log("doTouch()  move called with: maxY = [" + maxY + "]");
                        //X轴边界限制
                        endX = endX < 0 ? 0 : endX > maxX ? maxX : endX;
                        //Y轴边界限制
                        endY = endY < 0 ? 0 : endY > maxY ? maxY : endY;
                        //开始移动
                        layoutParams.x = (int) endX;
                        layoutParams.y = (int) endY;
                        windowManager.updateViewLayout(this, layoutParams);
                        //记录位置
                        mLastRawX = mRawX;
                        mLastRawY = mRawY;
                    }

                    break;
                case MotionEvent.ACTION_UP://手指离开
                	if (isNeedGoEdge) handler.postDelayed(goEdge, DELAY_MILLIS_GO_EDGE);
                    //根据自定义属性判断是否需要贴边
                    if (customAttachDirect >= 0 && isDrug && false) {
                        float[] edgePosition = getEdgePosition();
                        goEdge(edgePosition);
                    }
                    break;
            }
        }

    }

    /**
     * @param attachDirect 0所有的边 1左 2上 3右 4下 5左右 6上下
     */
    public void setCustomAttachDirect(int attachDirect) {
        this.customAttachDirect = attachDirect;
    }

    /**
     * 返回最终的边距值
     *
     * @return
     */
    private float[] getEdgePosition() {
        //判断是否为点击事件 0所有的边 1左 2上 3右 4下 5左右 6上下
        float centerX = mRootMeasuredWidth / 2;
        float centerY = mRootMeasuredHeight / 2;
        WindowManager.LayoutParams layoutParams_up = (WindowManager.LayoutParams) getLayoutParams();
        int startX = layoutParams_up.x;
        int startY = layoutParams_up.y;
        float x = -1, y = -1;
        if (customAttachDirect == 1) { /*左*/
            x = 0;
            y = -1;
        } else if (customAttachDirect == 2) { /*上*/
            x = -1;
            y = 0;
        } else if (customAttachDirect == 3) { /*右*/
            x = mRootMeasuredWidth - getWidth();
            y = -1;
        } else if (customAttachDirect == 4) {
            x = -1;
            y = mRootMeasuredWidth - getHeight();
        } else if (customAttachDirect == 5) { /*左右*/
            x = startX <= centerX ? 0 : (mRootMeasuredWidth - getWidth());
            y = startY;
        } else if (customAttachDirect == 6) { /*上下*/
            x = startX;
            y = startY <= centerY ? 0 : (mRootMeasuredHeight - getHeight());
        } else if (customAttachDirect == 0) { /*距那个边近去那个*/
        }
        return new float[]{x, y};

    }

    private void goEdge(float[] edgePosition) {
        if (edgePosition == null && edgePosition.length < 2) {
            return;
        }
        final float endX_ = edgePosition[0];
        final float endY_ = edgePosition[1];
        final WindowManager.LayoutParams layoutParams_up = (WindowManager.LayoutParams) getLayoutParams();
        final int startX = layoutParams_up.x;
		final int startY = layoutParams_up.y;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				try {
					float value = (Float) animation.getAnimatedValue();
		            /*1->0*/
		            float _x = (endX_ - startX) * value + startX;
		            float _y = (endY_ - startY) * value + startY;
		            layoutParams_up.x = (int) _x;
		            layoutParams_up.y = (int) _y;
		            windowManager.updateViewLayout(AttachLayoutWindow.this, layoutParams_up);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
        valueAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        log("onAttachedToWindow() called");
        /*右上角*/
        WindowManager.LayoutParams layoutParams_up = (WindowManager.LayoutParams) getLayoutParams();
        if (layoutParams_up != null) {
            if (getMeasuredWidth() == 0) {
                measure(0, 0);
            }
            layoutParams_up.x = mRootMeasuredWidth - getMeasuredWidth();
            layoutParams_up.y = (int) (30 * getResources().getDisplayMetrics().density);
            windowManager.updateViewLayout(this, layoutParams_up);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        log("onFinishInflate() called");
        /*(WindowManager.LayoutParams) getLayoutParams() 此时获取到的是 null*/
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        log("dispatchTouchEvent() called with: event = [" + event + "]");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchIsTargetView = true;
        }
        if (targetView != null) {
            // /*判断是否有目标view,若有不在目标view内不执行*/
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN://手指按下
                    Rect rect = new Rect();
                    targetView.getHitRect(rect);
                    if (!rect.contains((int) event.getX(), (int) event.getY())) {
                        touchIsTargetView = false;
                    }
            }
        }
        if (touchIsTargetView) {
            doTouch(event);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        log("onTouchEvent() called with: ev = [" + ev + "]");
        //判断是否需要滑动
//        doTouch(ev); /*不能放在这，放在这子view收不到事件了*/
        //是否拦截事件
        if (isDrug) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 自动贴边
     */
    private class GoEdgeRunnable implements Runnable {
        @Override
        public void run() {
            float[] edgePosition = getEdgePosition();
            goEdge(edgePosition);
        }
    }
}
