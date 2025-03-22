
package com.example.myapplication;

import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;

public class MoodAttribute {
    public enum Mood {
        HAPPY, SAD, ANGRY, CALM, ANXIOUS
    }

    private static final Map<Mood, Integer> moodColors = new HashMap<>();
    private static final Map<Mood, String> moodEmojis = new HashMap<>();

    static {
        // Using predefined Android colors
        moodColors.put(Mood.HAPPY, Color.YELLOW);
        moodColors.put(Mood.SAD, Color.BLUE);
        moodColors.put(Mood.ANGRY, Color.RED);
        moodColors.put(Mood.CALM, Color.GREEN);
        moodColors.put(Mood.ANXIOUS, Color.GRAY);

        moodEmojis.put(Mood.HAPPY, "😀");
        moodEmojis.put(Mood.SAD, "😢");
        moodEmojis.put(Mood.ANGRY, "😡");
        moodEmojis.put(Mood.CALM, "😌");
        moodEmojis.put(Mood.ANXIOUS, "😰");
    }

    public static int getMoodColor(Mood mood) {
        return moodColors.get(mood);
    }

    public static String getMoodEmoji(Mood mood) {
        return moodEmojis.get(mood);
    }
}
