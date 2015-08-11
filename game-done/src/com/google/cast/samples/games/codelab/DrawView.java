// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.cast.samples.games.codelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This View is the canvas on which the user can paint. Every time the user touches this view,
 * the corresponding pixel's color will be changed to the currently active drawing color. This
 * View simulates a 10x10 array of 'macro pixels' that the user can color.
 * Based on https://github.com/playgameservices/8bitartist
 */
public class DrawView extends View implements OnTouchListener {

    protected static final int GRID_SIZE = 20;
    private static final String TAG = "DrawView";

    private short[][] mGrid;
    private double mHeightInPixels;
    private short mSelectedColor = 1;
    private int mLastGridX = -1;
    private int mLastGridY = -1;
    private DrawViewListener mListener;

    // These are the four colors provided for painting.
    // If years of classic has taught me anything, these
    // are enough colors for anything. Anything at all.
    public static final int COLOR_MAP[] = {0xFF000000, 0xFF0000FF, 0xFFFF0000, 0xFF00FF00};

    // Interface for the Activity to know when a square is drawn
    public interface DrawViewListener {

        public void onDrawEvent(int gridX, int gridY, short colorIndex);
    }

    // Some temporary variables so we don't allocate while rendering
    private Rect mRect = new Rect();
    private Paint mPaint = new Paint();

    private Boolean mKeepAnimating = false;

    private boolean mTouchEnabled = true;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGrid = new short[GRID_SIZE][GRID_SIZE];

        setOnTouchListener(this);

        setAnimating(true);
    }

    public void setListener(DrawViewListener listener) {
        mListener = listener;
    }

    public void setAnimating(Boolean val) {
        mKeepAnimating = val;
        if (val) {
            invalidate();
        }
    }

    /**
     * Convert from screen space.
     *
     * @param screenSpaceCoordinate the coordinate in screen space.
     * @return the pixel coordinate in the View.
     */
    public int sp(float screenSpaceCoordinate) {
        return (int) Math.floor(screenSpaceCoordinate * mHeightInPixels);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Assume this is a square (as we will make it so in onMeasure()
        // and figure out how many pixels there are.
        mHeightInPixels = this.getHeight();

        // Now, draw with 0,0 in upper left and 9,9 in lower right
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                mPaint.setColor(COLOR_MAP[mGrid[x][y]]);

                mRect.top = sp(((float) y) / GRID_SIZE);
                mRect.left = sp(((float) x) / GRID_SIZE);
                mRect.right = sp(((float) (x + 1)) / GRID_SIZE);
                mRect.bottom = sp(((float) (y + 1)) / GRID_SIZE);

                canvas.drawRect(mRect, mPaint);
            }
        }

        if (mKeepAnimating) {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // We would like a square working area, so at layout time, we'll figure out how much room we
        // have grow to fit the larger of the parent's measure. This way we'll completely fill the
        // parent in one dimension.
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (parentWidth > parentHeight) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.EXACTLY);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    public void setTouchEnabled(boolean touchEnabled) {
        this.mTouchEnabled = touchEnabled;
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent me) {
        if (!mTouchEnabled) {
            return false;
        }

        switch (me.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Find where the touch event, which is in pixels, maps
                // to our 10x10 grid. (0,0) is in the upper left, (9, 9)
                // is in the lower right.
                int gridX = (int) Math.floor(1.0 * me.getX() / mHeightInPixels * GRID_SIZE);
                int gridY = (int) Math.floor(1.0 * me.getY() / mHeightInPixels * GRID_SIZE);

                Log.d(TAG, "You touched " + gridX + " " + gridY + "/" + me.getY());

                if (gridX < GRID_SIZE && gridY < GRID_SIZE && gridX >= 0 && gridY >= 0) {

                    short oldColor = mGrid[gridX][gridY];
                    mGrid[gridX][gridY] = mSelectedColor;

                    // Don't double-draw or send messages where the color does not change
                    boolean notSameSpot = (mLastGridX != gridX) || (mLastGridY != gridY);
                    boolean notSameColor = oldColor != mSelectedColor;
                    if (notSameSpot && notSameColor) {
                        if (mListener != null) {
                            mListener.onDrawEvent(gridX, gridY, mSelectedColor);
                        }
                        mLastGridX = gridX;
                        mLastGridY = gridY;
                    }
                }

                return true;
        }

        return false;
    }

    /**
     * Paint a pixel with the currently selected color.
     *
     * @param gridX      the column of the pixel to paint.
     * @param gridY      the row of the pixel to paint.
     * @param colorIndex the index into the color array to paint with.
     */
    public void setMacroPixel(int gridX, int gridY, short colorIndex) {
        // paint that pixel with the currently selected color
        mGrid[gridX][gridY] = colorIndex;
    }

    /**
     * Clear paint from all pixels.
     */
    public void clear() {
        mLastGridX = -1;
        mLastGridY = -1;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                mGrid[x][y] = 0;
            }
        }
    }

}
