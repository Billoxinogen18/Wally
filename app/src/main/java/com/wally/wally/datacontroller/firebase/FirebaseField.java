package com.wally.wally.datacontroller.firebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatConversionException;

public class FirebaseField {
    private Object value;

    public FirebaseField(Object value) {
        if (value instanceof Double[]) {
            value = arrayToList((Double[]) value);
        }
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return value == null ? null : (String) value;
    }

    public Integer toInteger() {
        return value == null ? null : (int) (long) value;
    }

    public Date toData() {
        return value == null ? null : (Date) value;
    }

    public Boolean toBoolean() {
        return value == null ? null : (Boolean) value;
    }

    public Double toDouble() {
        if (value instanceof Double)
            return (double) value;

        if (value instanceof Long)
            return (double) (long) value;

        if (value instanceof Integer)
            return (double) (int) value;

        throw new UnknownFormatConversionException("Cannot convert to double");
    }

    @SuppressWarnings("unchecked")
    public Double[] toDoubleArray() {
        return value == null ? null : listToArray((List<Double>) value);
    }

    @SuppressWarnings("unchecked")
    public Map<? extends String, ?> toStringMap() {
        return value == null ? null : (Map<? extends String, ?>) value;
    }

    private static List<Double> arrayToList(Double[] array) {
        List<Double> list = new ArrayList<>();
        Collections.addAll(list, array);
        return list;
    }

    private static Double[] listToArray(List<Double> list) {
        Double[] array = new Double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public FirebaseObject toFirebaseObject() {
        return (FirebaseObject) value;
    }
}
