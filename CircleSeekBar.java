package circleseekbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import test.io.feeeei.circleseekbar.R;


/**
 * Created by gaopengfei on 15/11/15.
 */
public class CircleSeekBar extends View {

    private static final double RADIAN = 180 / Math.PI;

    private static final String INATANCE_STATE = "state";
    private static final String INSTANCE_MAX_PROCESS = "max_process";
    private static final String INSTANCE_CUR_PROCESS = "cur_process";
    private static final String INSTANCE_CUR_PROCESS_2 = "cur_process_2";
    private static final String INSTANCE_REACHED_COLOR = "reached_color";
    private static final String INSTANCE_REACHED_WIDTH = "reached_width";
    private static final String INSTANCE_REACHED_CORNER_ROUND = "reached_corner_round";
    private static final String INSTANCE_UNREACHED_COLOR = "unreached_color";
    private static final String INSTANCE_UNREACHED_WIDTH = "unreached_width";
    private static final String INSTANCE_POINTER_COLOR = "pointer_color";
    private static final String INSTANCE_POINTER_RADIUS = "pointer_radius";
    private static final String INSTANCE_POINTER_SHADOW = "pointer_shadow";
    private static final String INSTANCE_POINTER_SHADOW_RADIUS = "pointer_shadow_radius";
    private static final String INSTANCE_WHEEL_SHADOW = "wheel_shadow";
    private static final String INSTANCE_WHEEL_SHADOW_RADIUS = "wheel_shadow_radius";
    private static final String INSTANCE_WHEEL_CAN_TOUCH = "wheel_can_touch";
    private static final String INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE = "wheel_scroll_only_one_circle";

    Paint mWheelPaint;

    Paint mReachedPaint;

    Paint mReachedEdgePaint;

    Paint mStartPointerPaint;

    Paint mPointerPaint;

    private int mMaxProcess;
    private int mOffProcess;
    private int mOnProcess; //设置锚点2进度
    private float mUnreachedRadius;
    private int mReachedColor, mUnreachedColor;
    private float mReachedWidth, mUnreachedWidth;
    private boolean isHasReachedCornerRound;
    private int mPointerColor;
    private float mPointerRadius;

    private double mOffAngle;
    private double mOnAngle; //当前锚点2的旋转角度
    private float mOffWheelCurX, mOffWheelCurY;
    private float mOnWheelCurX, mOnWheelCurY;

    double lastTabPoint = 0.0;

    private Bitmap mOnBitmap;
    private Bitmap mOffBitmap;

    private boolean isHasWheelShadow, isHasPointerShadow;
    private float mWheelShadowRadius, mPointerShadowRadius;

    private boolean isCanTouch;

    private boolean isScrollOneCircle;

    float mDefShadowOffset;

    private OnSeekBarChangeListener mOffChangListener;
    private OnSeekBarChangeListener mOnChangListener;

    private Context context;

    private RectF rectF = new RectF(0, 0, 0, 0);

    public CircleSeekBar(Context context) {
        this(context, null);
    }

    public CircleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initAttrs(attrs, defStyleAttr);
        initPadding();
        initPaints();
    }

    private void initPaints() {
        mDefShadowOffset = getDimen(R.dimen.def_shadow_offset);

        // 圆环画笔
        mWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWheelPaint.setColor(mUnreachedColor);
        mWheelPaint.setStyle(Paint.Style.STROKE);
        mWheelPaint.setStrokeWidth(mUnreachedWidth);
        if (isHasWheelShadow) {
            mWheelPaint.setShadowLayer(mWheelShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
        }

        // 选中区域画笔
        mReachedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedPaint.setColor(mReachedColor);
        mReachedPaint.setStyle(Paint.Style.STROKE);
        mReachedPaint.setStrokeWidth(mReachedWidth);
        if (isHasReachedCornerRound) {
            mReachedPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        // 开始锚点画笔
        mPointerPaint = new Paint();

        /**
         * 开始锚点画笔
         */
        mStartPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStartPointerPaint.setColor(mPointerColor);
        //mStartPointerPaint.set
        mStartPointerPaint.setStyle(Paint.Style.FILL);
        if (isHasPointerShadow) {
            mStartPointerPaint.setShadowLayer(mPointerShadowRadius, mDefShadowOffset, mDefShadowOffset, Color.DKGRAY);
        }

        // 选中区域两头的圆角画笔
        mReachedEdgePaint = new Paint(mReachedPaint);
        mReachedEdgePaint.setStyle(Paint.Style.FILL);

        mOffBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ctrl_time_off);
        mOnBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ctrl_time_on);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleSeekBar, defStyle, 0);
        mMaxProcess = a.getInt(R.styleable.CircleSeekBar_wheel_max_process, 100);
        mOffProcess = a.getInt(R.styleable.CircleSeekBar_wheel_cur_process, 30);
        mOnProcess = a.getInt(R.styleable.CircleSeekBar_wheel_cur_process_2, 0);

        if (mOffProcess > mMaxProcess) mOffProcess = mMaxProcess;
        if (mOnProcess > mMaxProcess) mOnProcess = mMaxProcess;

        mReachedColor = a.getColor(R.styleable.CircleSeekBar_wheel_reached_color, getColor(R.color.def_reached_color));
        mUnreachedColor = a.getColor(R.styleable.CircleSeekBar_wheel_unreached_color,
                getColor(R.color.def_wheel_color));
        mUnreachedWidth = a.getDimension(R.styleable.CircleSeekBar_wheel_unreached_width,
                getDimen(R.dimen.def_wheel_width));
        isHasReachedCornerRound = a.getBoolean(R.styleable.CircleSeekBar_wheel_reached_has_corner_round, true);
        mReachedWidth = a.getDimension(R.styleable.CircleSeekBar_wheel_reached_width, mUnreachedWidth);
        mPointerColor = a.getColor(R.styleable.CircleSeekBar_wheel_pointer_color, getColor(R.color.def_pointer_color));
        mPointerRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_pointer_radius, mReachedWidth / 2);
        isHasWheelShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_wheel_shadow, false);
        if (isHasWheelShadow) {
            mWheelShadowRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_shadow_radius,
                    getDimen(R.dimen.def_shadow_radius));
        }
        isHasPointerShadow = a.getBoolean(R.styleable.CircleSeekBar_wheel_has_pointer_shadow, false);
        if (isHasPointerShadow) {
            mPointerShadowRadius = a.getDimension(R.styleable.CircleSeekBar_wheel_pointer_shadow_radius,
                    getDimen(R.dimen.def_shadow_radius));
        }

        isCanTouch = a.getBoolean(R.styleable.CircleSeekBar_wheel_can_touch, true);
        isScrollOneCircle = a.getBoolean(R.styleable.CircleSeekBar_wheel_scroll_only_one_circle, false);

        if (isHasPointerShadow | isHasWheelShadow) {
            setSoftwareLayer();
        }
        a.recycle();
    }

    //初始化边距
    private void initPadding() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int paddingStart = 0, paddingEnd = 0;
        if (Build.VERSION.SDK_INT >= 17) {
            paddingStart = getPaddingStart();
            paddingEnd = getPaddingEnd();
        }
        int maxPadding = Math.max(paddingLeft, Math.max(paddingTop,
                Math.max(paddingRight, Math.max(paddingBottom, Math.max(paddingStart, paddingEnd)))));
        setPadding(maxPadding, maxPadding, maxPadding, maxPadding);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private int getColor(int colorId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return getContext().getColor(colorId);
        } else {
            return ContextCompat.getColor(getContext(), colorId);
        }
    }

    private float getDimen(int dimenId) {
        return getResources().getDimension(dimenId);
    }

    private void setSoftwareLayer() {

        setLayerType(LAYER_TYPE_SOFTWARE, null);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        refreshPosition();
    }


    private void refreshOffWheelCurPosition(double cos) {
        mOffWheelCurX = calcXLocationInWheel(mOffAngle, cos);
        mOffWheelCurY = calcYLocationInWheel(cos);
    }

    private void refreshOnWheelCurPosition(double cos) {
        mOnWheelCurX = calcXLocationInWheel(mOnAngle, cos);
        mOnWheelCurY = calcYLocationInWheel(cos);
    }

    private void refreshPosition() {


        mOffAngle = (double) mOffProcess / mMaxProcess * 360.0;
        mOnAngle = (double) mOnProcess / mMaxProcess * 360.0;

        Log.i("refreshPosition: ", "mOffProcess=" + mOffProcess + ", mOffAngle=" + mOffAngle);
        Log.i("refreshPosition: ", "mOnProcess=" + mOnProcess + ", mOnAngle=" + mOnAngle);

        double cos = -Math.cos(Math.toRadians(mOffAngle));
        double cos2 = -Math.cos(Math.toRadians(mOnAngle));
        refreshOffWheelCurPosition(cos);
        refreshOnWheelCurPosition(cos2);
        //确定圆环的半径
        mUnreachedRadius = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - mUnreachedWidth) / 2;
    }

    // 确定x点的坐标
    private float calcXLocationInWheel(double angle, double cos) {
        if (angle < 180) {
            return (float) (getMeasuredWidth() / 2 + Math.sqrt(1 - cos * cos) * mUnreachedRadius);
        } else {
            return (float) (getMeasuredWidth() / 2 - Math.sqrt(1 - cos * cos) * mUnreachedRadius);
        }
    }

    // 确定y点的坐标
    private float calcYLocationInWheel(double cos) {
        return getMeasuredWidth() / 2 + mUnreachedRadius * (float) cos;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float left = getPaddingLeft() + mUnreachedWidth / 2;
        float top = getPaddingTop() + mUnreachedWidth / 2;
        float right = canvas.getWidth() - getPaddingRight() - mUnreachedWidth / 2;
        float bottom = canvas.getHeight() - getPaddingBottom() - mUnreachedWidth / 2;
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;

        float wheelRadius = (canvas.getWidth() - getPaddingLeft() - getPaddingRight()) / 2 - mUnreachedWidth / 2;
        canvas.drawCircle(centerX, centerY, wheelRadius, mWheelPaint);

        float begin; // 圆弧的起点位置
        float sweepAngle; // 圆弧扫过的角度
        if (mOffAngle > mOnAngle) {
            begin = (float) mOnAngle - 90;
            sweepAngle = (float) (mOffAngle - mOnAngle);
        } else {
            begin = (float) mOnAngle - 90;
            sweepAngle = 360 - (float) Math.abs(mOffAngle - mOnAngle);
        }

        Log.i("onDraw", "mOnAngle:" + mOnAngle);
        Log.i("onDraw", "mOffAngle:" + mOffAngle);
        Log.i("onDraw", "sweepAngle=" + sweepAngle);
        rectF.set(left, top, right, bottom);
        //画选中区域
        canvas.drawArc(rectF, begin, sweepAngle, false, mReachedPaint);

        //画锚点
        canvas.drawBitmap(mOffBitmap, mOffWheelCurX - mOffBitmap.getWidth() / 2, mOffWheelCurY - mOffBitmap.getHeight() / 2, mPointerPaint);

        //画锚点
        canvas.drawBitmap(mOnBitmap, mOnWheelCurX - mOnBitmap.getWidth() / 2, mOnWheelCurY - mOnBitmap.getHeight() / 2, mPointerPaint);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        int flag = 0;
        //判断是否触控到两个点中的其中某个点
        if (isMoveOff(x, y)) {
            flag = 2;
        } else if (isMoveOn(x, y)) {
            flag = 1;
        }

        if (event.getAction()== MotionEvent.ACTION_UP){
            lastTabPoint = 0.0;
        }

        if (isCanTouch && (event.getAction() == MotionEvent.ACTION_MOVE) || isMoveOn(x, y) || isMoveOff(x, y) || isTouch(x, y)) {

            // 通过当前触摸点搞到cos角度值
            float cos = computeCos(x, y);
            // 通过反三角函数获得角度值
            double angle;
            if (x < getWidth() / 2) { // 滑动超过180度
                angle = Math.PI * RADIAN + Math.acos(cos) * RADIAN;
            } else { // 没有超过180度
                angle = Math.PI * RADIAN - Math.acos(cos) * RADIAN;
            }
            if (flag == 2) {
                mOffAngle = angle;
                if (mOffAngle < 0) {
                    mOffAngle = 360 - Math.abs(mOffAngle);
                }
                if (mOffAngle > 360) {
                    mOffAngle = mOffAngle - 360;
                }
                mOffProcess = getSelectedValue(mOffAngle);

                refreshOffWheelCurPosition(cos);
                if (mOffChangListener != null) {
                    mOffChangListener.onChanged(this, mOffProcess);
                }
            } else if (flag == 1) {
                mOnAngle = angle;
                if (mOnAngle < 0) {
                    mOnAngle = 360 - Math.abs(mOnAngle);
                }
                if (mOnAngle > 360) {
                    mOnAngle = mOnAngle - 360;
                }
                mOnProcess = getSelectedValue(mOnAngle);

                refreshOnWheelCurPosition(cos);
                if (mOnChangListener != null) {
                    mOnChangListener.onChanged(this, mOnProcess);
                }
            } else {

                boolean isOnArc;
                if (mOffAngle >= mOnAngle) {
                    isOnArc = angle >= mOnAngle && angle <= mOffAngle;
                } else {
                    if(angle >= mOnAngle && angle <= 360.0 ||
                            angle >= 0.0 && angle <= mOffAngle ){
                        isOnArc = true;
                    }else{
                        isOnArc = false;
                    }
                }

                Log.i("onTouchEvent", "angel:" + angle);
                Log.i("onTouchEvent", "mOnAngle:" + mOnAngle);
                Log.i("onTouchEvent", "mOffAngle:" + mOffAngle);
                Log.i("onTouchEvent", "isOnArc:" + isOnArc);
                if (isOnArc) {
                    if (lastTabPoint >= -0.000000000001 && lastTabPoint <= 0.000000000001) {
                        lastTabPoint = angle;
                    } else {
                        double result = angle - lastTabPoint;
                        mOnAngle += result;
                        mOffAngle += result;
                    }
                    if (mOnAngle > 360) {
                        mOnAngle = mOnAngle - 360;
                    }
                    if (mOffAngle > 360) {
                        mOffAngle = mOffAngle - 360;
                    }

                    if (mOnAngle < 0) {
                        mOnAngle = 360 - Math.abs(mOnAngle);
                    }

                    if (mOffAngle < 0) {
                        mOffAngle = 360 - Math.abs(mOffAngle);
                    }

                    // 头尾联动, 头尾角度不变
                    mOnProcess = getSelectedValue(mOnAngle);
                    mOffProcess = getSelectedValue(mOffAngle);

                    double cosOff = -Math.cos(Math.toRadians(mOffAngle));
                    double cosOn = -Math.cos(Math.toRadians(mOnAngle));
                    refreshOffWheelCurPosition(cosOff);
                    refreshOnWheelCurPosition(cosOn);
                    lastTabPoint = angle;
                }
            }
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }


    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private boolean isMoveOn(float x, float y) {
        float dot1x = Math.abs(mOnWheelCurX - x);
        float dot1y = Math.abs(mOnWheelCurY - y);
        return (dot1x < mOnBitmap.getWidth() / 2 && dot1y < mOnBitmap.getHeight() / 2);
    }

    private boolean isMoveOff(float x, float y) {
        float dot1x = Math.abs(mOffWheelCurX - x);
        float dot1y = Math.abs(mOffWheelCurY - y);
        return (dot1x < mOffBitmap.getWidth() / 2 && dot1y < mOffBitmap.getHeight() / 2);
    }

    private boolean isTouch(float x, float y) {
        double radius = (getWidth() - getPaddingLeft() - getPaddingRight() + getCircleWidth() / 2) / 2;
        double minRadius = (getWidth() - getPaddingLeft() - getPaddingRight() - getCircleWidth()) / 2;

        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        return Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2) < radius * radius &&
                Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2) > minRadius * minRadius;
    }

    private float getCircleWidth() {
        return Math.max(mUnreachedWidth, Math.max(mReachedWidth, mOffBitmap.getWidth()));
    }


    /**
     * 拿到倾斜的cos值
     */
    private float computeCos(float x, float y) {
        float width = x - getWidth() / 2;
        float height = y - getHeight() / 2;
        float slope = (float) Math.sqrt(width * width + height * height);
        return height / slope;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INATANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_MAX_PROCESS, mMaxProcess);
        bundle.putInt(INSTANCE_CUR_PROCESS, mOffProcess);
        bundle.putInt(INSTANCE_CUR_PROCESS_2, mOnProcess);
        bundle.putInt(INSTANCE_REACHED_COLOR, mReachedColor);
        bundle.putFloat(INSTANCE_REACHED_WIDTH, mReachedWidth);
        bundle.putBoolean(INSTANCE_REACHED_CORNER_ROUND, isHasReachedCornerRound);
        bundle.putInt(INSTANCE_UNREACHED_COLOR, mUnreachedColor);
        bundle.putFloat(INSTANCE_UNREACHED_WIDTH, mUnreachedWidth);
        bundle.putInt(INSTANCE_POINTER_COLOR, mPointerColor);
        bundle.putFloat(INSTANCE_POINTER_RADIUS, mPointerRadius);
        bundle.putBoolean(INSTANCE_POINTER_SHADOW, isHasPointerShadow);
        bundle.putFloat(INSTANCE_POINTER_SHADOW_RADIUS, mPointerShadowRadius);
        bundle.putBoolean(INSTANCE_WHEEL_SHADOW, isHasWheelShadow);
        bundle.putFloat(INSTANCE_WHEEL_SHADOW_RADIUS, mPointerShadowRadius);
        bundle.putBoolean(INSTANCE_WHEEL_CAN_TOUCH, isCanTouch);
        bundle.putBoolean(INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE, isScrollOneCircle);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(INATANCE_STATE));
            mMaxProcess = bundle.getInt(INSTANCE_MAX_PROCESS);
            mOffProcess = bundle.getInt(INSTANCE_CUR_PROCESS);
            mOnProcess = bundle.getInt(INSTANCE_CUR_PROCESS_2);
            mReachedColor = bundle.getInt(INSTANCE_REACHED_COLOR);
            mReachedWidth = bundle.getFloat(INSTANCE_REACHED_WIDTH);
            isHasReachedCornerRound = bundle.getBoolean(INSTANCE_REACHED_CORNER_ROUND);
            mUnreachedColor = bundle.getInt(INSTANCE_UNREACHED_COLOR);
            mUnreachedWidth = bundle.getFloat(INSTANCE_UNREACHED_WIDTH);
            mPointerColor = bundle.getInt(INSTANCE_POINTER_COLOR);
            mPointerRadius = bundle.getFloat(INSTANCE_POINTER_RADIUS);
            isHasPointerShadow = bundle.getBoolean(INSTANCE_POINTER_SHADOW);
            mPointerShadowRadius = bundle.getFloat(INSTANCE_POINTER_SHADOW_RADIUS);
            isHasWheelShadow = bundle.getBoolean(INSTANCE_WHEEL_SHADOW);
            mPointerShadowRadius = bundle.getFloat(INSTANCE_WHEEL_SHADOW_RADIUS);
            isCanTouch = bundle.getBoolean(INSTANCE_WHEEL_CAN_TOUCH);
            isScrollOneCircle = bundle.getBoolean(INSTANCE_WHEEL_SCROLL_ONLY_ONE_CIRCLE);
            initPaints();
        } else {
            super.onRestoreInstanceState(state);
        }

        if (mOffChangListener != null) {
            mOffChangListener.onChanged(this, mOffProcess);
        }
        if (mOnChangListener != null) {
            mOnChangListener.onChanged(this, mOnProcess);
        }
    }

    private int getSelectedValue(double mCurAngle) {
        return Math.round(mMaxProcess * ((float) mCurAngle / 360));
    }

    public void setOnSeekBarChangeListenerOff(OnSeekBarChangeListener listener) {
        mOffChangListener = listener;
    }

    public interface OnSeekBarChangeListener {
        void onChanged(CircleSeekBar seekbar, int curValue);
    }

    public void setOnSeekBarChangeListenerOn(OnSeekBarChangeListener listener) {
        mOnChangListener = listener;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mOffChangListener = listener;
    }


    public void setCurProcess(int curProcess) {
        this.mOffProcess = curProcess > mMaxProcess ? mMaxProcess : curProcess;
        if (mOffChangListener != null) {
            mOffChangListener.onChanged(this, curProcess);
        }
        refreshPosition();
        invalidate();
    }

    public int getCurProcess() {
        return mOffProcess;
    }

    public void recycleBitmap() {
        if (mOffBitmap != null) {
            mOffBitmap.recycle();
            mOffBitmap = null;
        }
        if (mOnBitmap != null) {
            mOnBitmap.recycle();
            mOnBitmap = null;
        }
    }
}
