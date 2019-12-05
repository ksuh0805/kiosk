package com.samilcts.ui.view;

//import com.samilcts.util.android.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class SignPad extends View {
	private static final String TAG = "MyView";
	
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;

    private float mX, mY;

    final static private int mMovePoint = 100;

    int mWidth;
    int mHeight;
    
    private boolean mIsTouched = false;
    
    private Paint mPaint;
    private Paint mOuterPaint;
    private RectF mOuterRectF;
    
	public int PAINT_LINE_SIZE = 5;
	/*
	public String LCT_touch_x = "Default val.LCT_touch_x"; // real measured LCT_touch_x value,;
    public String LCT_touch_y = "Default val.LCT_touch_y"; // real measured LCT_touch_y value,;
    */
    public SignPad(Context c) {
        super(c);
       // Log.i(TAG, "c");
    }

    public SignPad(Context c, AttributeSet attrs) {
    	super(c, attrs, 0);
    	//Log.i(TAG, "c2");
    }
    
    public SignPad(Context c, AttributeSet attrs, int defStyle) {
    	super(c, attrs, defStyle);
    	//Log.i(TAG, "c3");
    }
    
    private void init() {
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        
		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(PAINT_LINE_SIZE);
        
        mOuterPaint = new Paint();
        //mOuterPaint.setAntiAlias(true);
        //mOuterPaint.setDither(true);
        mOuterPaint.setColor(0xFFE6E6E6);
        mOuterPaint.setStyle(Paint.Style.STROKE);
        //mOuterPaint.setStrokeJoin(Paint.Join.ROUND);
       // mOuterPaint.setStrokeCap(Paint.Cap.ROUND);

        //mOuterPaint.setStrokeJoin(Paint.Join.BEVEL);
        //mOuterPaint.setStrokeCap(Paint.Cap.SQUARE);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        if ( px < 1) px = 1;

        mOuterPaint.setStrokeWidth(px);
        mOuterRectF = new RectF(px, px, mWidth-px, mHeight-px);
    }
    
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void touch_start(float x, float y) {
        mX = x;
        mY = y;

    }

    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	  	int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	  	int heightSize = 0;
	  	switch(heightMode) {
	  	case MeasureSpec.UNSPECIFIED:
	  		heightSize = heightMeasureSpec;
	  		break;
	  	case MeasureSpec.AT_MOST:
	  		heightSize = 20;
	  		break;
	  	case MeasureSpec.EXACTLY:
	  		heightSize = MeasureSpec.getSize(heightMeasureSpec);
	  		break;
	  	}
      
	  	int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	  	int widthSize = 0;
	  	switch(widthMode) {
	  	case MeasureSpec.UNSPECIFIED:
	  		widthSize = widthMeasureSpec;
	  		break;
	  	case MeasureSpec.AT_MOST:
	  		widthSize = 100;
	  		break;
	  	case MeasureSpec.EXACTLY:
	  		widthSize = MeasureSpec.getSize(widthMeasureSpec);
	  		break;
	  	}

		setMeasuredDimension(widthSize, heightSize);
		 
		mWidth = widthSize;
        mHeight = heightSize;
        
		init();
	}

  
	@Override
	protected void onDraw(Canvas canvas) {
        //canvas.drawRoundRect(mOuterRectF, 20, 20, mOuterPaint);
        canvas.drawRect(mOuterRectF, mOuterPaint);
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
	}

	private void touch_move(float x, float y) {
        if (mX == x && mY == y)
        {
            return;
        }


//        Log.d(TAG, "x=" + x + ", Math.abs( mX - x )=" + Math.abs(mX - x));
//        Log.d(TAG, "y=" + y + ", Math.abs( mY - y )=" + Math.abs(mY - y));
        if (Math.abs(mX - x) > mMovePoint || Math.abs(mY - y) > mMovePoint)
        {
            mX = x;
            mY = y;
        }

        mCanvas.drawLine(mX, mY, x, y, mPaint);
        mX = x;
        mY = y;
  }

	public void clear() {
		mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        invalidate();
	}
	
	private void touch_up() { }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);

/*
                LCT_touch_x = String.format("(down)LCT_touch_x : %6.2f", x);
//                Log.d(TAG, "[ACTION_DOWN] LCT_touch_x value : " + LCT_touch_x);
                LCT_touch_y = String.format("(down)LCT_touch_y : %6.2f", y);
//                Log.d(TAG, "[ACTION_DOWN] LCT_touch_y value : " + LCT_touch_y);
*/

                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);

/*
                LCT_touch_x = String.format("(move)LCT_touch_x : %6.2f", x);
//                Log.d(TAG, "[ACTION_MOVE] LCT_touch_x value : " + LCT_touch_x);
                LCT_touch_y = String.format("(move)LCT_touch_y : %6.2f", y);
//                Log.d(TAG, "[ACTION_MOVE] LCT_touch_y value : " + LCT_touch_y);
*/

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touch_up();

/*
                LCT_touch_x = String.format("(up)LCT_touch_x : %6.2f", x);
//                Log.d(TAG, "[ACTION_UP] LCT_touch_x value : " + LCT_touch_x);
                LCT_touch_y = String.format("(up)LCT_touch_y : %6.2f", y);
//                Log.d(TAG, "[ACTION_UP] LCT_touch_y value : " + LCT_touch_y);
*/

                mIsTouched = true;
                
                invalidate();
                break;

            default:
                break;
        }
        return true;
    }
	
	public boolean isTouched() {
		return mIsTouched;
	}
}

