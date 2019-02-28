package com.github.mikephil.charting.utils;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.List;

/**
 * Transformer class that contains all matrices and is responsible for
 * transforming values into pixels on the screen and backwards.
 *
 * @author Philipp Jahoda
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Transformer {

    /**
     * matrix to map the values to the screen pixels
     */
    protected Matrix mMatrixValueToPx = new Matrix();

    /**
     * matrix for handling the different offsets of the chart
     */
    protected Matrix mMatrixOffset = new Matrix();

    protected ViewPortHandler mViewPortHandler;

    public Transformer(ViewPortHandler viewPortHandler) {
        this.mViewPortHandler = viewPortHandler;
    }

    /**
     * Prepares the matrix that transforms values to pixels. Calculates the
     * scale factors from the charts size and offsets.
     */
    public void prepareMatrixValuePx(float xChartMin, float deltaX, float deltaY, float yChartMin) {
        float scaleX = (mViewPortHandler.contentWidth() / deltaX);
        float scaleY = (mViewPortHandler.contentHeight() / deltaY);
        if (Float.isInfinite(scaleX)) {
            scaleX = 0;
        }
        if (Float.isInfinite(scaleY)) {
            scaleY = 0;
        }

        // setup all matrices
        mMatrixValueToPx.reset();
        mMatrixValueToPx.postTranslate(-xChartMin, -yChartMin);
        mMatrixValueToPx.postScale(scaleX, -scaleY);
    }

    /**
     * Prepares the matrix that contains all offsets.
     */
    public void prepareMatrixOffset(boolean inverted) {
        mMatrixOffset.reset();
        if (!inverted) {
            mMatrixOffset.postTranslate(mViewPortHandler.offsetLeft(),
                    mViewPortHandler.getChartHeight() - mViewPortHandler.offsetBottom());
        } else {
            mMatrixOffset
                    .setTranslate(mViewPortHandler.offsetLeft(), -mViewPortHandler.offsetTop());
            mMatrixOffset.postScale(1.0f, -1.0f);
        }
    }

    protected float[] valuePointsForGenerateTransformedValuesScatter = new float[1];

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the SCATTERCHART.
     */
    public float[] generateTransformedValuesScatter(IScatterDataSet data, float phaseX,
                                                    float phaseY, int from, int to) {

        final int count = (int) ((to - from) * phaseX + 1) * 2;
        if (valuePointsForGenerateTransformedValuesScatter.length != count) {
            valuePointsForGenerateTransformedValuesScatter = new float[count];
        }
        final float[] valuePoints = valuePointsForGenerateTransformedValuesScatter;
        for (int j = 0; j < count; j += 2) {
            final Entry e = data.getEntryForIndex(j / 2 + from);
            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);
        return valuePoints;
    }

    protected float[] valuePointsForGenerateTransformedValuesBubble = new float[1];

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the BUBBLECHART.
     */
    public float[] generateTransformedValuesBubble(IBubbleDataSet data, float phaseY, int from, int to) {
        final int count = (to - from + 1) * 2; // (int) Math.ceil((to - from) * phaseX) * 2;
        if (valuePointsForGenerateTransformedValuesBubble.length != count) {
            valuePointsForGenerateTransformedValuesBubble = new float[count];
        }
        final float[] valuePoints = valuePointsForGenerateTransformedValuesBubble;
        for (int j = 0; j < count; j += 2) {
            final Entry e = data.getEntryForIndex(j / 2 + from);
            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);
        return valuePoints;
    }

    protected float[] valuePointsForGenerateTransformedValuesLine = new float[1];

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the LINECHART.
     */
    public float[] generateTransformedValuesLine(ILineDataSet data,
                                                 float phaseX, float phaseY,
                                                 int min, int max) {

        final int count = ((int) ((max - min) * phaseX) + 1) * 2;
        if (valuePointsForGenerateTransformedValuesLine.length != count) {
            valuePointsForGenerateTransformedValuesLine = new float[count];
        }
        final float[] valuePoints = valuePointsForGenerateTransformedValuesLine;
        for (int j = 0; j < count; j += 2) {
            final Entry e = data.getEntryForIndex(j / 2 + min);
            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);
        return valuePoints;
    }

    protected float[] valuePointsForGenerateTransformedValuesCandle = new float[1];

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the CANDLESTICKCHART.
     */
    public float[] generateTransformedValuesCandle(ICandleDataSet data,
                                                   float phaseX, float phaseY, int from, int to) {

        final int count = (int) ((to - from) * phaseX + 1) * 2;
        if (valuePointsForGenerateTransformedValuesCandle.length != count) {
            valuePointsForGenerateTransformedValuesCandle = new float[count];
        }
        final float[] valuePoints = valuePointsForGenerateTransformedValuesCandle;
        for (int j = 0; j < count; j += 2) {
            final CandleEntry e = data.getEntryForIndex(j / 2 + from);
            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getHigh() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);
        return valuePoints;
    }

    /**
     * transform a path with all the given matrices VERY IMPORTANT: keep order
     * to value-touch-offset
     */
    public void pathValueToPixel(Path path) {
        path.transform(mMatrixValueToPx);
        path.transform(mViewPortHandler.getMatrixTouch());
        path.transform(mMatrixOffset);
    }

    /**
     * Transforms multiple paths will all matrices.
     */
    public void pathValuesToPixel(List<Path> paths) {
        for (int i = 0; i < paths.size(); i++) {
            pathValueToPixel(paths.get(i));
        }
    }

    /**
     * Transform an array of points with all matrices. VERY IMPORTANT: Keep
     * matrix order "value-touch-offset" when transforming.
     */
    public void pointValuesToPixel(float[] pts) {
        mMatrixValueToPx.mapPoints(pts);
        mViewPortHandler.getMatrixTouch().mapPoints(pts);
        mMatrixOffset.mapPoints(pts);
    }

    /**
     * Transform a rectangle with all matrices.
     */
    public void rectValueToPixel(RectF r) {
        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     */
    public void rectToPixelPhase(RectF r, float phaseY) {
        // multiply the height of the rect with the phase
        r.top *= phaseY;
        r.bottom *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void rectToPixelPhaseHorizontal(RectF r, float phaseY) {
        // multiply the height of the rect with the phase
        r.left *= phaseY;
        r.right *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     */
    public void rectValueToPixelHorizontal(RectF r) {
        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public void rectValueToPixelHorizontal(RectF r, float phaseY) {
        // multiply the height of the rect with the phase
        r.left *= phaseY;
        r.right *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    /**
     * transforms multiple rects with all matrices
     */
    public void rectValuesToPixel(List<RectF> rects) {
        final Matrix m = getValueToPixelMatrix();
        for (int i = 0; i < rects.size(); i++) {
            m.mapRect(rects.get(i));
        }
    }

    protected Matrix mPixelToValueMatrixBuffer = new Matrix();

    /**
     * Transforms the given array of touch positions (pixels) (x, y, x, y, ...)
     * into values on the chart.
     */
    public void pixelsToValue(float[] pixels) {
        final Matrix tmp = mPixelToValueMatrixBuffer;
        tmp.reset();

        // invert all matrixes to convert back to the original value
        mMatrixOffset.invert(tmp);
        tmp.mapPoints(pixels);

        mViewPortHandler.getMatrixTouch().invert(tmp);
        tmp.mapPoints(pixels);

        mMatrixValueToPx.invert(tmp);
        tmp.mapPoints(pixels);
    }

    /**
     * buffer for performance
     */
    float[] ptsBuffer = new float[2];

    /**
     * Returns a recyclable MPPointD instance.
     * returns the x and y values in the chart at the given touch point
     * (encapsulated in a MPPointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelForValues(...).
     */
    public MPPointD getValuesByTouchPoint(float x, float y) {
        final MPPointD result = MPPointD.getInstance(0, 0);
        getValuesByTouchPoint(x, y, result);
        return result;
    }

    public void getValuesByTouchPoint(float x, float y, MPPointD outputPoint) {
        ptsBuffer[0] = x;
        ptsBuffer[1] = y;

        pixelsToValue(ptsBuffer);

        outputPoint.x = ptsBuffer[0];
        outputPoint.y = ptsBuffer[1];
    }

    /**
     * Returns a recyclable MPPointD instance.
     * Returns the x and y coordinates (pixels) for a given x and y value in the chart.
     */
    public MPPointD getPixelForValues(float x, float y) {
        ptsBuffer[0] = x;
        ptsBuffer[1] = y;

        pointValuesToPixel(ptsBuffer);

        double xPx = ptsBuffer[0];
        double yPx = ptsBuffer[1];

        return MPPointD.getInstance(xPx, yPx);
    }

    public Matrix getValueMatrix() {
        return mMatrixValueToPx;
    }

    public Matrix getOffsetMatrix() {
        return mMatrixOffset;
    }

    private Matrix mMBuffer1 = new Matrix();

    public Matrix getValueToPixelMatrix() {
        mMBuffer1.set(mMatrixValueToPx);
        mMBuffer1.postConcat(mViewPortHandler.mMatrixTouch);
        mMBuffer1.postConcat(mMatrixOffset);
        return mMBuffer1;
    }

    private Matrix mMBuffer2 = new Matrix();

    public Matrix getPixelToValueMatrix() {
        getValueToPixelMatrix().invert(mMBuffer2);
        return mMBuffer2;
    }
}
