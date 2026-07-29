package com.nti.nice_gallery.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JsonUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ==========================================
    // Creation
    // ==========================================

    public static JSONObject newJsonObject() {
        return new JSONObject();
    }
    public static JSONObject newJsonObject(String jsonStr) {
        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // Primitives
    // ==========================================

    public static Boolean getBoolean(JSONObject json, String key, Boolean defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            return json.getBoolean(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addBoolean(JSONObject json, String key, Boolean value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer getInt(JSONObject json, String key, Integer defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            return json.getInt(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addInt(JSONObject json, String key, Integer value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Long getLong(JSONObject json, String key, Long defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            return json.getLong(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addLong(JSONObject json, String key, Long value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Double getDouble(JSONObject json, String key, Double defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            return json.getDouble(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addDouble(JSONObject json, String key, Double value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getString(JSONObject json, String key, String defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            return json.getString(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addString(JSONObject json, String key, String value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // Arrays of primitives
    // ==========================================

    public static <T> List<T> getArrayOfPrimitives(JSONObject json, String key, List<T> defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            JSONArray array = json.getJSONArray(key);
            List<T> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                if (array.isNull(i)) {
                    result.add(null);
                } else {
                    result.add((T) array.get(i));
                }
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void addArrayOfPrimitives(JSONObject json, String key, Iterable<T> list) {
        try {
            if (list == null) {
                json.put(key, JSONObject.NULL);
                return;
            }
            JSONArray array = new JSONArray();
            for (T item : list) {
                if (item == null) array.put(JSONObject.NULL);
                else array.put(item);
            }
            json.put(key, array);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArrayOfPrimitives(String jsonStr) {
        try {
            JSONArray array = new JSONArray(jsonStr);
            List<T> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                if (array.isNull(i)) {
                    result.add(null);
                } else {
                    result.add((T) array.get(i));
                }
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String stringifyArrayOfPrimitives(Iterable<T> list) {
        JSONArray array = new JSONArray();
        if (list == null) {
            return array.toString();
        }
        for (T item : list) {
            if (item == null) array.put(JSONObject.NULL);
            else array.put(item);
        }
        return array.toString();
    }

    // ==========================================
    // Enums
    // ==========================================

    public static <E extends Enum<E>> E getEnum(JSONObject json, String key, Class<E> enumClass, E defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            String name = json.getString(key);
            try {
                return Enum.valueOf(enumClass, name);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Enum<E>> void addEnum(JSONObject json, String key, E value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value.name());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Enum<E>> List<E> getArrayOfEnums(JSONObject json, String key, Class<E> enumClass, List<E> defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            JSONArray array = json.getJSONArray(key);
            List<E> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                if (array.isNull(i)) {
                    result.add(null);
                } else {
                    result.add(Enum.valueOf(enumClass, array.getString(i)));
                }
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Enum<E>> void addArrayOfEnums(JSONObject json, String key, Iterable<E> list) {
        try {
            if (list == null) {
                json.put(key, JSONObject.NULL);
                return;
            }
            JSONArray array = new JSONArray();
            for (E item : list) {
                if (item == null) array.put(JSONObject.NULL);
                else array.put(item.name());
            }
            json.put(key, array);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // Date & time
    // ==========================================

    public static LocalDateTime getLocalDateTime(JSONObject json, String key, LocalDateTime defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            String dateStr = json.getString(key);
            try {
                return LocalDateTime.parse(dateStr, ISO_FORMATTER);
            } catch (Exception e) {
                return defaultValue;
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addLocalDateTime(JSONObject json, String key, LocalDateTime value) {
        try {
            if (value == null) json.put(key, JSONObject.NULL);
            else json.put(key, value.format(ISO_FORMATTER));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // ==========================================
    // Arrays
    // ==========================================

    /**
     * Получает список объектов, используя лямбду-маппер для конвертации JSONObject -> T.
     */
    public static <T> List<T> getArray(JSONObject json, String key, Function<JSONObject, T> mapper, List<T> defaultValue) {
        try {
            if (!json.has(key) || json.isNull(key)) return defaultValue;
            JSONArray array = json.getJSONArray(key);
            List<T> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                if (array.isNull(i)) {
                    result.add(null);
                } else {
                    result.add(mapper.apply(array.getJSONObject(i)));
                }
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Записывает список объектов в JSON, используя лямбду для сериализации T -> JSONObject.
     */
    public static <T> void addArray(JSONObject json, String key, Iterable<T> list, BiConsumer<T, JSONObject> serializer) {
        try {
            if (list == null) {
                json.put(key, JSONObject.NULL);
                return;
            }
            JSONArray array = new JSONArray();
            for (T item : list) {
                if (item == null) {
                    array.put(JSONObject.NULL);
                } else {
                    JSONObject itemJson = new JSONObject();
                    serializer.accept(item, itemJson);
                    array.put(itemJson);
                }
            }
            json.put(key, array);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
