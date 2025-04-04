package com.example.moodmento;

/**
 * FilterCriteria encapsulates user-selected filters for mood history.
 * Includes emotion, time range, and optional search keyword.
 * @author israelmajek
 */
public class FilterCriteria {
    public String emotion;       // e.g., "HAPPY", "ALL"
    public String timeRange;     // e.g., "Last 7 days", "All time"
    public String filterWord;    // Optional keyword

    /**
     * Constructs a FilterCriteria object.
     *
     * @param emotion    The selected emotion filter.
     * @param timeRange  The selected time range filter.
     * @param filterWord The entered search keyword.
     */
    public FilterCriteria(String emotion, String timeRange, String filterWord) {
        this.emotion = emotion;
        this.timeRange = timeRange;
        this.filterWord = filterWord;
    }
}
