package com.syy.timeselectview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import android.widget.ScrollView;

public class TimeSelect extends View {

    public static final int SCALE = 2;
    public static final int LINE_COUNT = 24;
    public static final String TAG = "TimeSelect";
    public static int SCALE_LINE_HEIGHT = Utils.dp2px(18);
    public static int VERTICAL_OFFSET = Utils.dp2px(10);
    public static int HORIZONTAL_OFFSET = Utils.dp2px(50);
    public static int LINE_LENGTH = Utils.dp2px(285);
    public static int touchSlop;

    /**
     * parent滚动距离
     */
    public static int SCROLL_LENGTH = SCALE_LINE_HEIGHT * 3;
    /**
     * 提前滚动的位置
     */
    public static int SCROLL_PRE_POS = 1;


    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int roundSize = Utils.dp2px(4);
    RectF selectRect = new RectF();
    //上下圆圈
    int pointRadius = Utils.dp2px(4);
    Point topPoint = null;
    Point bottomPoint = null;
    int extendPointRadius = Utils.dp2px(12);
    RectF topExtendPoint = null;
    RectF bottomExtendPoint = null;


    float lastDownY;
    int lastStartIndex;
    int lastEndIndex;

    boolean clickSelect;
    boolean clickTopPoint;
    boolean clickBottomPoint;

    boolean isScrolling;
    OverScroller overScroller;

    /**
     * 0~24
     */
    int currentStartTimeInx = 0;
    /**
     * 0~24
     */
    int currentEndTimeInd = 0;


    public TimeSelect(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);


        overScroller = new OverScroller(context);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredHeight = LINE_COUNT * SCALE * SCALE_LINE_HEIGHT + VERTICAL_OFFSET * 2;
        setMeasuredDimension(getMeasuredWidth(), measuredHeight);
    }

    public float getSelectStart() {
        return currentStartTimeInx * 1.0f / SCALE;
    }

    public float getSelectEnd() {
        return currentEndTimeInd * 1.0f / SCALE;
    }

    /**
     * select 0~24
     *
     * @param start
     * @param end
     */
    public void setSelect(float start, float end) {
        int oldStart = (int) (start * SCALE);
        int oldEnd = (int) (end * SCALE);
        if (this.currentStartTimeInx == oldStart && this.currentEndTimeInd == oldEnd) {
            return;
        }

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        this.currentStartTimeInx = oldStart;
        this.currentEndTimeInd = oldEnd;

        selectRect.set(HORIZONTAL_OFFSET + Utils.dp2px(8),
                VERTICAL_OFFSET + this.currentStartTimeInx * SCALE_LINE_HEIGHT,
                HORIZONTAL_OFFSET + LINE_LENGTH - Utils.dp2px(8),
                VERTICAL_OFFSET + this.currentEndTimeInd * SCALE_LINE_HEIGHT);

        topPoint = new Point((int) (selectRect.left + Utils.dp2px(16)), (int) selectRect.top);
        bottomPoint = new Point((int) (selectRect.right - Utils.dp2px(16)), (int) (selectRect.bottom));

        topExtendPoint = new RectF(topPoint.x - extendPointRadius, topPoint.y - extendPointRadius,
                topPoint.x + extendPointRadius, topPoint.y + extendPointRadius);
        bottomExtendPoint = new RectF(bottomPoint.x - extendPointRadius, bottomPoint.y - extendPointRadius,
                bottomPoint.x + extendPointRadius, bottomPoint.y + extendPointRadius);

        invalidate();
    }

    private int getScrollTopIndex() {
        ScrollView parent = (ScrollView) (getParent());
        int dy = parent.getScrollY() - VERTICAL_OFFSET;
        //没有滑到0刻度，下一刻度为0
        if (dy < 0) {
            return 0;
        }
        //已经滑过=dy / SCALE_LINE_HEIGHT，下一个即+1
        return dy / SCALE_LINE_HEIGHT + 1;
    }

    private int getScrollBottomIndex() {
        ScrollView parent = (ScrollView) (getParent());
        int dy = parent.getScrollY() - VERTICAL_OFFSET + parent.getMeasuredHeight();
        int maxScrollY = getMeasuredHeight() - 2 * VERTICAL_OFFSET;
        if (dy > maxScrollY) {
            dy = maxScrollY;
        }
        return dy / SCALE_LINE_HEIGHT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.parseColor("#CFCFCF"));
        paint.setStrokeWidth(Utils.dp2px(1));
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(Utils.dp2px(12));
        for (int i = 0; i <= LINE_COUNT * SCALE; i++) {
            if (i % 2 == 1) {
                continue;
            }
            //画线
            int newY = VERTICAL_OFFSET + i * SCALE_LINE_HEIGHT;
            canvas.drawLine(HORIZONTAL_OFFSET, newY, HORIZONTAL_OFFSET + LINE_LENGTH, newY, paint);

            //画字
            float baseLIne = newY - paint.ascent() / 2 - paint.descent() / 2;
            canvas.drawText(timeLineString(i), HORIZONTAL_OFFSET - Utils.dp2px(40), baseLIne, paint);
        }

        //画选择区域
        paint.setColor(Color.parseColor(isEnabled() ? "#6176E7" : "#336176E7"));
        canvas.drawRoundRect(selectRect, roundSize, roundSize, paint);

        //画时间段
        paint.setColor(Color.parseColor("#ffffff"));
        String text = timeString(currentStartTimeInx) + " - " + timeString(currentEndTimeInd) + "   " +
                "共" + (currentEndTimeInd - currentStartTimeInx) * 1.0f / SCALE + "小时";
        int dp = currentEndTimeInd - currentStartTimeInx > 1 ? 12 : 2;
        float baseLIne = selectRect.top + Utils.dp2px(dp) + -paint.ascent();
        canvas.drawText(text, selectRect.left + Utils.dp2px(12), baseLIne, paint);

        //画上下圆圈
        canvas.drawCircle(topPoint.x, topPoint.y, pointRadius, paint);
        canvas.drawCircle(bottomPoint.x, bottomPoint.y, pointRadius, paint);

        paint.setColor(Color.parseColor("#6176E7"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Utils.dp2px(2));
        canvas.drawCircle(topPoint.x, topPoint.y, pointRadius, paint);
        canvas.drawCircle(bottomPoint.x, bottomPoint.y, pointRadius, paint);
    }

    private String timeLineString(int index) {
        float idx = index * 1.0f / SCALE;
        if (idx <= 12) {
            return (int) idx + " AM";

        } else if (idx >= 13 && idx <= 24) {
            return ((int) (idx) - 12) + " PM";
        }
        return "";

    }

    public String timeString(int index) {
        float idx = index * 1.0f / SCALE;
        if (idx <= 12) {
            int fen = (int) ((idx - (int) (idx)) * 60);
            return (int) (idx) + ":" + (fen == 0 ? "00" : fen) + "AM";
        } else if (idx > 12 && idx <= 24) {
            int fen = (int) (((idx - 12) - (int) (idx - 12)) * 60);
            return (int) (idx - 12) + ":" + (fen == 0 ? "00" : fen) + "PM";
        }
        return "";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        Log.d(TAG, "onTouchEvent() called with: event = [" + event + "]");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastDownY = event.getY();
                lastStartIndex = currentStartTimeInx;
                lastEndIndex = currentEndTimeInd;

                if (topExtendPoint.contains(event.getX(), event.getY())) {
                    clickTopPoint = true;
                    break;
                }

                if (bottomExtendPoint.contains(event.getX(), event.getY())) {
                    clickBottomPoint = true;
                    break;
                }

                if (selectRect.contains(event.getX(), event.getY())) {
                    clickSelect = true;
                    break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (event.getY() - lastDownY);

                int newStartIndex = 0;
                int newEndIndex = 0;
                if (clickTopPoint) {
                    newStartIndex = lastStartIndex + (dy / SCALE_LINE_HEIGHT);
                    if (newStartIndex < 0) {
                        newStartIndex = 0;
                    }
                    //newStartIndex必须小于于lastStartIndex一个刻度
                    if (newStartIndex > lastEndIndex - 1) {
                        newStartIndex = lastEndIndex - 1;
                    }
                    newEndIndex = currentEndTimeInd;
                } else if (clickBottomPoint) {
                    newEndIndex = lastEndIndex + (dy / SCALE_LINE_HEIGHT);
                    if (newEndIndex > LINE_COUNT * SCALE) {
                        newEndIndex = LINE_COUNT * SCALE;
                    }
                    //newEndIndex必须大于lastStartIndex一个刻度
                    if (newEndIndex < lastStartIndex + 1) {
                        newEndIndex = lastStartIndex + 1;
                    }
                    newStartIndex = currentStartTimeInx;
                } else if (clickSelect) {
                    newStartIndex = lastStartIndex + (dy / SCALE_LINE_HEIGHT);
                    //start上限
                    if (newStartIndex < 0) {
                        newStartIndex = 0;
                    }
                    int maxIndex = LINE_COUNT * SCALE - (currentEndTimeInd - currentStartTimeInx);
                    //start下限
                    if (newStartIndex > maxIndex) {
                        newStartIndex = maxIndex;
                    }
                    newEndIndex = newStartIndex + (currentEndTimeInd - currentStartTimeInx);
                }

                setSelect(newStartIndex * 1.0f / SCALE, newEndIndex * 1.0f / SCALE);
                //显示不全，滚动parent
                ScrollView parent = (ScrollView) (getParent());
                scrollParent(parent, dy, newStartIndex, newEndIndex);

                break;
            case MotionEvent.ACTION_UP:
                clickSelect = false;
                clickTopPoint = false;
                clickBottomPoint = false;
                break;
        }
        boolean isHandled = clickTopPoint || clickBottomPoint || clickSelect;
        return isEnabled() && isHandled;
    }

    private void scrollParent(ScrollView parent, int dy, int newStartIndex, int newEndIndex) {
        //向上滚动
        if (dy < 0 && Math.abs(dy) > touchSlop
                && newStartIndex <= getScrollTopIndex() + SCROLL_PRE_POS) {

            int scrollDy = -SCROLL_LENGTH;
            if (SCROLL_LENGTH >= parent.getScrollY()) {
                scrollDy = -parent.getScrollY();
            }
            Log.e(TAG, "onTouchEvent: " +
                    "start scrollDy=" + scrollDy + ",scrollY=" + parent.getScaleY());

            if (!isScrolling) {
                isScrolling = true;
                overScroller.startScroll(parent.getScrollX(), parent.getScrollY(),
                        0, scrollDy, 1);
                postInvalidateOnAnimation();
            }
        }

        //向下滚动
        if (dy > 0 && Math.abs(dy) > touchSlop
                && newEndIndex >= getScrollBottomIndex() - SCROLL_PRE_POS) {

            int scrollDy = SCROLL_LENGTH;
            if (parent.getScrollY() > getMeasuredHeight() - parent.getMeasuredHeight()) {
                scrollDy = getMeasuredHeight() - parent.getMeasuredHeight();
            }

            Log.e(TAG, "onTouchEvent: "
                    + "end scrollDy=" + scrollDy + ",scrollY=" + parent.getScaleY());
            if (!isScrolling) {
                isScrolling = true;
                overScroller.startScroll(parent.getScrollX(), parent.getScrollY(),
                        0, scrollDy, 1);
                postInvalidateOnAnimation();
            }
        }
    }

    public void scrollParent() {
        if (currentStartTimeInx <= 2) {
            return;
        }
        ScrollView parent = (ScrollView) getParent();
        int scrollDy = (currentStartTimeInx - 2) * SCALE_LINE_HEIGHT;
        overScroller.startScroll(parent.getScrollX(), parent.getScrollY(),
                0, scrollDy, 100);
    }

    @Override
    public void computeScroll() {
        if (!overScroller.computeScrollOffset()) {
            isScrolling = false;
            return;
        }
        ScrollView parent = (ScrollView) (getParent());
        parent.scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
        postInvalidateOnAnimation();
    }

    public interface Listener {
        void clickSelect(int start, int end);

        void changeSelect(int start, int end);
    }
}
