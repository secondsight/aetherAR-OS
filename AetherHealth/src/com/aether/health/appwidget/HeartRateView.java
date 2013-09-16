package com.aether.health.appwidget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class HeartRateView extends View {
    
    private Paint mBackGroundPaint;
    private Paint mPaint;
    List<Float> mPoints = new ArrayList<Float>();

    public HeartRateView(Context context) {
        super(context);
        init(context);
    }

    public HeartRateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawHeartRateLine(canvas);
    }
    
    private void init(Context context){
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(220);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(3f);
        
        mBackGroundPaint = new Paint();
        mBackGroundPaint.setColor(Color.WHITE);
        mBackGroundPaint.setAlpha(127);
        mBackGroundPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    }

    private final static int STEPS_WIDTH = 20;
    private void drawBackground(Canvas canvas) {
        Rect rect = new Rect();
        canvas.getClipBounds(rect);
        int top = rect.top;
        int left = rect.left;
        int width = rect.width();
        int height = rect.height();
        
        Paint paint = new Paint();
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0x7F58677A);
        canvas.drawRoundRect(new RectF(rect), 20f, 20f, paint);
        
        for (int i = left+STEPS_WIDTH; i<(left+width); i+=STEPS_WIDTH) {
            canvas.drawLine(i, top, i, top+height, mBackGroundPaint);
        }
        for (int i = top+STEPS_WIDTH; i<(top+height); i+=STEPS_WIDTH) {
            canvas.drawLine(left, i, left+width, i, mBackGroundPaint);
        }
        
    }

    private void drawHeartRateLine(Canvas canvas) {
        updateHeartRateData(canvas);
        
        drawPath(canvas, mPoints, mPaint, false);
    }

    float[] xv = new float[STEPS_WIDTH];
    float[] yv = new float[STEPS_WIDTH];
    private void updateHeartRateData(Canvas canvas) {
        Rect rect = new Rect();
        canvas.getClipBounds(rect);
        int top = rect.top;
        int left = rect.left;
        int width = rect.width();
        int height = rect.height();
        int midHeight = (top+height)/2;
        int random = (int) (Math.random()*100);
        if (random<60) {
            random = random + (int)(Math.random()*40);
        }
        int offset = random - 65;

        int length =mPoints.size();
        if (length > STEPS_WIDTH*2) {
            mPoints.remove(0);
            mPoints.remove(0);
        }
        length =mPoints.size();
        int count = 0;
        for (int i = 0; i<length; i=i+2) {
            xv[count] = mPoints.get(i)-STEPS_WIDTH;
            yv[count] = mPoints.get(i+1);
            count++;
        }
        
        mPoints.clear();
        for (int k=0; k<count; k++) {
            mPoints.add(xv[k]);
            mPoints.add(yv[k]);
        }
        mPoints.add((float) (left+width));
        mPoints.add((float) (midHeight+offset));
    }

//draw path utils
    /**
     * The graphical representation of a path.
     * 
     * @param canvas the canvas to paint to
     * @param points the points that are contained in the path to paint
     * @param paint the paint to be used for painting
     * @param circular if the path ends with the start point
     */
    protected void drawPath(Canvas canvas, List<Float> points, Paint paint, boolean circular) {
      Path path = new Path();
      int height = canvas.getHeight();
      int width = canvas.getWidth();

      float[] tempDrawPoints;
      if (points.size() < 4) {
        return;
      }
      tempDrawPoints = calculateDrawPoints(points.get(0), points.get(1), points.get(2),
          points.get(3), height, width);
      path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
      path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);

      int length = points.size();
      for (int i = 4; i < length; i += 2) {
        if ((points.get(i - 1) < 0 && points.get(i + 1) < 0)
            || (points.get(i - 1) > height && points.get(i + 1) > height)) {
          continue;
        }
        tempDrawPoints = calculateDrawPoints(points.get(i - 2), points.get(i - 1), points.get(i),
            points.get(i + 1), height, width);
        if (!circular) {
          path.moveTo(tempDrawPoints[0], tempDrawPoints[1]);
        }
        path.lineTo(tempDrawPoints[2], tempDrawPoints[3]);
      }
      if (circular) {
        path.lineTo(points.get(0), points.get(1));
      }
      canvas.drawPath(path, paint);
    }

    private static float[] calculateDrawPoints(float p1x, float p1y, float p2x, float p2y,
            int screenHeight, int screenWidth) {
          float drawP1x;
          float drawP1y;
          float drawP2x;
          float drawP2y;

          if (p1y > screenHeight) {
            // Intersection with the top of the screen
            float m = (p2y - p1y) / (p2x - p1x);
            drawP1x = (screenHeight - p1y + m * p1x) / m;
            drawP1y = screenHeight;

            if (drawP1x < 0) {
              // If Intersection is left of the screen we calculate the intersection
              // with the left border
              drawP1x = 0;
              drawP1y = p1y - m * p1x;
            } else if (drawP1x > screenWidth) {
              // If Intersection is right of the screen we calculate the intersection
              // with the right border
              drawP1x = screenWidth;
              drawP1y = m * screenWidth + p1y - m * p1x;
            }
          } else if (p1y < 0) {
            float m = (p2y - p1y) / (p2x - p1x);
            drawP1x = (-p1y + m * p1x) / m;
            drawP1y = 0;
            if (drawP1x < 0) {
              drawP1x = 0;
              drawP1y = p1y - m * p1x;
            } else if (drawP1x > screenWidth) {
              drawP1x = screenWidth;
              drawP1y = m * screenWidth + p1y - m * p1x;
            }
          } else {
            // If the point is in the screen use it
            drawP1x = p1x;
            drawP1y = p1y;
          }

          if (p2y > screenHeight) {
            float m = (p2y - p1y) / (p2x - p1x);
            drawP2x = (screenHeight - p1y + m * p1x) / m;
            drawP2y = screenHeight;
            if (drawP2x < 0) {
              drawP2x = 0;
              drawP2y = p1y - m * p1x;
            } else if (drawP2x > screenWidth) {
              drawP2x = screenWidth;
              drawP2y = m * screenWidth + p1y - m * p1x;
            }
          } else if (p2y < 0) {
            float m = (p2y - p1y) / (p2x - p1x);
            drawP2x = (-p1y + m * p1x) / m;
            drawP2y = 0;
            if (drawP2x < 0) {
              drawP2x = 0;
              drawP2y = p1y - m * p1x;
            } else if (drawP2x > screenWidth) {
              drawP2x = screenWidth;
              drawP2y = m * screenWidth + p1y - m * p1x;
            }
          } else {
            // If the point is in the screen use it
            drawP2x = p2x;
            drawP2y = p2y;
          }

          return new float[] { drawP1x, drawP1y, drawP2x, drawP2y };
        }
}
