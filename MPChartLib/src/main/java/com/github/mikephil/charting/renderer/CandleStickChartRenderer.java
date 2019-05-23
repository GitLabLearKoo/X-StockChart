package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class CandleStickChartRenderer extends LineScatterCandleRadarRenderer {
    protected CandleDataProvider mChart;
    private static final float OFFSET = 0.5f;

    private float[] mShadowBuffers = new float[8];
    private float[] mBodyBuffers = new float[4];
    private float[] mRangeBuffers = new float[4];
    private float[] mOpenBuffers = new float[4];
    private float[] mCloseBuffers = new float[4];

    public CandleStickChartRenderer(CandleDataProvider chart, ChartAnimator animator,
                                    ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
    }

    @Override
    public void initBuffers() {
    }

    @Override
    public void drawData(Canvas c) {
        final CandleData candleData = mChart.getCandleData();
        for (ICandleDataSet set : candleData.getDataSets()) {
            if (set.isVisible()) {
                drawDataSet(c, set);
            }
        }
    }

    @SuppressWarnings("ResourceAsColor")
    protected void drawDataSet(Canvas c, ICandleDataSet dataSet) {
        final Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
        final float phaseY = mAnimator.getPhaseY();
        final float barSpace = dataSet.getBarSpace();
        final boolean showCandleBar = dataSet.getShowCandleBar();

        mXBounds.set(mChart, dataSet);
        mRenderPaint.setStrokeWidth(dataSet.getShadowWidth());

        // draw the body
        for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {
            // get the entry
            CandleEntry e = null;
            try {
                e = dataSet.getEntryForIndex(j);
            } catch (Exception ignored) {
            }
            if (e == null) {
                continue;
            }

            final float xPos = e.getX() + OFFSET;
            final float open = e.getOpen();
            final float close = e.getClose();
            final float high = e.getHigh();
            final float low = e.getLow();

            if (showCandleBar) {
                // calculate the shadow
                mShadowBuffers[0] = xPos;
                mShadowBuffers[2] = xPos;
                mShadowBuffers[4] = xPos;
                mShadowBuffers[6] = xPos;

                if (open > close) {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = open * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = close * phaseY;
                } else if (open < close) {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = close * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = open * phaseY;
                } else {
                    mShadowBuffers[1] = high * phaseY;
                    mShadowBuffers[3] = open * phaseY;
                    mShadowBuffers[5] = low * phaseY;
                    mShadowBuffers[7] = mShadowBuffers[3];
                }
                trans.pointValuesToPixel(mShadowBuffers);

                // draw the shadows
                if (dataSet.getShadowColorSameAsCandle()) {
                    if (open > close) {
                        if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getDecreasingColor());
                        }
                    } else if (open < close) {
                        if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getIncreasingColor());
                        }
                    } else {
                        if (dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE) {
                            mRenderPaint.setColor(dataSet.getColor(j));
                        } else {
                            mRenderPaint.setColor(dataSet.getNeutralColor());
                        }
                    }
                } else {
                    if (dataSet.getShadowColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getShadowColor());
                    }
                }
                mRenderPaint.setStyle(Paint.Style.STROKE);
                c.drawLines(mShadowBuffers, mRenderPaint);

                // calculate the body
                mBodyBuffers[0] = xPos - OFFSET + barSpace;
                mBodyBuffers[1] = close * phaseY;
                mBodyBuffers[2] = (xPos + OFFSET - barSpace);
                mBodyBuffers[3] = open * phaseY;
                trans.pointValuesToPixel(mBodyBuffers);

                // draw body differently for increasing and decreasing entry
                if (open > close) { // decreasing
                    if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getDecreasingColor());
                    }
                    mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());
                    c.drawRect(
                            mBodyBuffers[0], mBodyBuffers[3],
                            mBodyBuffers[2], mBodyBuffers[1],
                            mRenderPaint);
                } else if (open < close) {
                    if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getIncreasingColor());
                    }
                    mRenderPaint.setStyle(dataSet.getIncreasingPaintStyle());
                    c.drawRect(
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            mRenderPaint);
                } else { // equal values
                    if (dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE) {
                        mRenderPaint.setColor(dataSet.getColor(j));
                    } else {
                        mRenderPaint.setColor(dataSet.getNeutralColor());
                    }
                    mRenderPaint.setStyle(dataSet.getNeutralPaintStyle());
                    c.drawLine(
                            mBodyBuffers[0], mBodyBuffers[1],
                            mBodyBuffers[2], mBodyBuffers[3],
                            mRenderPaint);
                }
            } else {
                mRangeBuffers[0] = xPos;
                mRangeBuffers[1] = high * phaseY;
                mRangeBuffers[2] = xPos;
                mRangeBuffers[3] = low * phaseY;

                mOpenBuffers[0] = xPos - OFFSET + barSpace;
                mOpenBuffers[1] = open * phaseY;
                mOpenBuffers[2] = xPos;
                mOpenBuffers[3] = open * phaseY;

                mCloseBuffers[0] = xPos + OFFSET - barSpace;
                mCloseBuffers[1] = close * phaseY;
                mCloseBuffers[2] = xPos;
                mCloseBuffers[3] = close * phaseY;

                trans.pointValuesToPixel(mRangeBuffers);
                trans.pointValuesToPixel(mOpenBuffers);
                trans.pointValuesToPixel(mCloseBuffers);

                // draw the ranges
                int barColor;
                if (open > close) {
                    barColor = dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getDecreasingColor();
                } else if (open < close) {
                    barColor = dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getIncreasingColor();
                } else {
                    barColor = dataSet.getNeutralColor() == ColorTemplate.COLOR_NONE
                            ? dataSet.getColor(j)
                            : dataSet.getNeutralColor();
                }
                mRenderPaint.setColor(barColor);
                c.drawLine(
                        mRangeBuffers[0], mRangeBuffers[1],
                        mRangeBuffers[2], mRangeBuffers[3],
                        mRenderPaint);
                c.drawLine(
                        mOpenBuffers[0], mOpenBuffers[1],
                        mOpenBuffers[2], mOpenBuffers[3],
                        mRenderPaint);
                c.drawLine(
                        mCloseBuffers[0], mCloseBuffers[1],
                        mCloseBuffers[2], mCloseBuffers[3],
                        mRenderPaint);
            }
        }
    }

    @Override
    public void drawValues(Canvas c) {
        final List<ICandleDataSet> dataSets = mChart.getCandleData().getDataSets();
        for (int i = 0; i < dataSets.size(); i++) {
            final ICandleDataSet dataSet = dataSets.get(i);
            if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1) {
                continue;
            }
            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet);

            final Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
            mXBounds.set(mChart, dataSet);

            final float[] positions = trans.generateTransformedValuesCandle(
                    dataSet, mAnimator.getPhaseX(), mAnimator.getPhaseY(), mXBounds.min, mXBounds.max);

            final ValueFormatter formatter = dataSet.getValueFormatter();
            final MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

            //计算最大值和最小值
            float maxValue = 0, minValue = 0;
            int maxIndex = 0, minIndex = 0;
            CandleEntry maxEntry = null, minEntry = null;
            boolean isFirstInit = true;
            for (int j = 0; j < positions.length; j += 2) {
                final float x = positions[j];
                final float y = positions[j + 1];
                if (!mViewPortHandler.isInBoundsRight(x)) {
                    break;
                }
                if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y)) {
                    continue;
                }

                final CandleEntry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);
                if (isFirstInit) {
                    isFirstInit = false;
                    maxValue = entry.getHigh();
                    minValue = entry.getLow();
                    maxEntry = entry;
                    minEntry = entry;
                } else {
                    if (entry.getHigh() > maxValue) {
                        maxValue = entry.getHigh();
                        maxIndex = j;
                        maxEntry = entry;
                    }
                    if (entry.getLow() < minValue) {
                        minValue = entry.getLow();
                        minIndex = j;
                        minEntry = entry;
                    }
                }
                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {
                    final Drawable icon = entry.getIcon();
                    Utils.drawImage(
                            c,
                            icon,
                            (int) (x + iconsOffset.x),
                            (int) (y + iconsOffset.y),
                            icon.getIntrinsicWidth(),
                            icon.getIntrinsicHeight());
                }
            }
            MPPointF.recycleInstance(iconsOffset);

            //绘制最大值和最小值
            if (maxIndex > minIndex) {
                //画右边
                final String highString = formatter.getFormattedValue(minValue);
                //计算显示位置
                //计算文本宽度
                final int highStringWidth = Utils.calcTextWidth(mValuePaint, "← " + highString);
                final int highStringHeight = Utils.calcTextHeight(mValuePaint, "← " + highString);

                final float[] tPosition = new float[2];
                tPosition[0] = minEntry.getX() + OFFSET;
                tPosition[1] = minEntry.getLow();
                trans.pointValuesToPixel(tPosition);
                if (tPosition[0] + highStringWidth / 2 > mViewPortHandler.contentRight()) {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                highString + " →",
                                tPosition[0] - (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                } else {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                "← " + highString,
                                tPosition[0] + (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                }
            } else {
                //画左边
                final String highString = formatter.getFormattedValue(minValue);
                final int highStringWidth = Utils.calcTextWidth(mValuePaint, highString + " →");
                final int highStringHeight = Utils.calcTextHeight(mValuePaint, highString + " →");

                final float[] tPosition = new float[2];
                tPosition[0] = minEntry == null ? 0f : minEntry.getX() + OFFSET;
                tPosition[1] = minEntry == null ? 0f : minEntry.getLow();
                trans.pointValuesToPixel(tPosition);
                if (tPosition[0] - highStringWidth / 2 < mViewPortHandler.contentLeft()) {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                "← " + highString,
                                tPosition[0] + (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                } else {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                highString + " →",
                                tPosition[0] - (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                }
            }

            //这里画的是上面两个点
            if (maxIndex > minIndex) {
                //画左边
                final String highString = formatter.getFormattedValue(maxValue);
                final int highStringWidth = Utils.calcTextWidth(mValuePaint, highString + " →");
                final int highStringHeight = Utils.calcTextHeight(mValuePaint, highString + " →");

                final float[] tPosition = new float[2];
                tPosition[0] = maxEntry.getX() + OFFSET;
                tPosition[1] = maxEntry.getHigh();
                trans.pointValuesToPixel(tPosition);
                if ((tPosition[0] - highStringWidth / 2) < mViewPortHandler.contentLeft()) {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                "← " + highString,
                                tPosition[0] + (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                } else {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                highString + " →",
                                tPosition[0] - (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                }
            } else {
                //画右边
                final String highString = formatter.getFormattedValue(maxValue);
                final int highStringWidth = Utils.calcTextWidth(mValuePaint, "← " + highString);
                final int highStringHeight = Utils.calcTextHeight(mValuePaint, "← " + highString);

                final float[] tPosition = new float[2];
                tPosition[0] = maxEntry == null ? 0f : maxEntry.getX() + OFFSET;
                tPosition[1] = maxEntry == null ? 0f : maxEntry.getHigh();
                trans.pointValuesToPixel(tPosition);
                if (tPosition[0] + highStringWidth / 2 > mViewPortHandler.contentRight()) {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                highString + " →",
                                tPosition[0] - (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                } else {
                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c,
                                "← " + highString,
                                tPosition[0] + (float) highStringWidth / 2,
                                tPosition[1] + (float) highStringHeight / 2,
                                dataSet.getCandleDataTextColor());
                    }
                }
            }
        }
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }

    @Override
    public void drawExtras(Canvas c) {
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        final CandleData candleData = mChart.getCandleData();
        for (Highlight high : indices) {
            final ICandleDataSet set = candleData.getDataSetByIndex(high.getDataSetIndex());
            if (set == null || !set.isHighlightEnabled()) {
                continue;
            }

            final CandleEntry e = set.getEntryForXValue(high.getX(), high.getY());
            if (!isInBoundsX(e, set)) {
                continue;
            }

            final float lowValue = e.getLow() * mAnimator.getPhaseY();
            final float highValue = e.getHigh() * mAnimator.getPhaseY();
            final float y = (lowValue + highValue) / 2f;

            final MPPointD pix = mChart.getTransformer(set.getAxisDependency()).getPixelForValues(e.getX() + OFFSET, y);
            high.setDraw((float) pix.x, (float) pix.y);

            // draw the lines
            drawHighlightLines(c, (float) pix.x, (float) pix.y, set);
        }
    }
}
