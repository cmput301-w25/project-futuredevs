package com.example.myapplication;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to map moods to their corresponding emoji and color representations.
 */
public class MoodUtils {
    private static final Map<String, String> emojiMap = new HashMap<>();
    private static final Map<String, Integer> colorMap = new HashMap<>();

    static {
        // emoji representations for moods
        emojiMap.put("ANGER", "😡🔴");
        emojiMap.put("CONFUSED", "😕🟠");
        emojiMap.put("DISGUSTED", "🤢🟢");
        emojiMap.put("FEAR", "😨⚫");
        emojiMap.put("HAPPY", "😊🟡");
        emojiMap.put("SHAME", "😳⚪️");
        emojiMap.put("SADNESS", "😭🔵");
        emojiMap.put("SURPRISED", "😮🟣");



        // color representations for moods
        //colorMap.put("ANGER", Color.RED);
        //colorMap.put("CONFUSED", Color.GRAY);
        //colorMap.put("DISGUSTED", Color.GREEN);
        //colorMap.put("FEAR", Color.DKGRAY);
        //colorMap.put("HAPPY", Color.YELLOW);
        //colorMap.put("SHAME", Color.LTGRAY);
        //colorMap.put("SADNESS", Color.BLUE);
        //colorMap.put("SURPRISED", Color.MAGENTA);
    }

    public static String getEmoji(String mood) {
        return emojiMap.getOrDefault(mood, "❓"); // Default emoji if mood not found
    }

    //public static int getColor(String mood) {
    //   return colorMap.getOrDefault(mood, Color.LTGRAY); // Default color if mood not found
    //}
}
