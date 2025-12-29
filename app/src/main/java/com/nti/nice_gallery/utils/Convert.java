package com.nti.nice_gallery.utils;

import android.content.Context;
import android.util.Size;

import com.nti.nice_gallery.R;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Convert {

    private final Context context;

    public Convert(Context context) {
        this.context = context;
    }

    public int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public String weightToString(Long weight) {

        final int BASE = 1000;

        if (weight == null) {
            weight = 0L;
        }

        double value = weight;
        if (value < BASE) {
            return String.format(context.getResources().getString(R.string.format_weight_bytes), value);
        }

        value /= BASE;
        if (value < BASE) {
            return String.format(context.getResources().getString(R.string.format_weight_kilobytes), value);
        }

        value /= BASE;
        if (value < BASE) {
            return String.format(context.getResources().getString(R.string.format_weight_megabytes), value);
        }

        value /= BASE;
        if (value < BASE) {
            return String.format(context.getResources().getString(R.string.format_weight_gigabytes), value);
        }

        return String.format(context.getResources().getString(R.string.format_weight_terabytes), value);
    }

    public String sizeToString(Integer width, Integer height) {
        return String.format(context.getResources().getString(R.string.format_size_2d), width, height);
    }

    public String dateToFullNumericDateString(LocalDateTime date) {
        if (date == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(context.getString(R.string.format_java_simple_date_full_numeric));
        return date.format(formatter);
    }

    public String durationToTimeString(Integer duration) {
        if (duration == null) {
            return null;
        }

        int div = duration;
        int seconds = div % 60;
        div /= 60;
        int minutes = div % 60;
        div /= 60;
        int hours = div;

        if (hours == 0) {
            return String.format(context.getResources().getString(R.string.format_duration_short), minutes, seconds);
        }

        return String.format(context.getResources().getString(R.string.format_duration_full), hours, minutes, seconds);
    }
}
