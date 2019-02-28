package com.github.mikephil.charting.listener;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;

/**
 * Listener for callbacks when drawing on the chart.
 *
 * @author Philipp
 */
@SuppressWarnings("unused")
public interface OnDrawListener {

    /**
     * Called whenever an entry is added with the finger. Note this is also called for entries that are generated by the
     * library, when the touch gesture is too fast and skips points.
     *
     * @param entry the last drawn entry
     */
    void onEntryAdded(Entry entry);

    /**
     * Called whenever an entry is moved by the user after beeing highlighted
     */
    void onEntryMoved(Entry entry);

    /**
     * Called when drawing finger is lifted and the draw is finished.
     *
     * @param dataSet the last drawn DataSet
     */
    void onDrawFinished(DataSet<?> dataSet);

}
