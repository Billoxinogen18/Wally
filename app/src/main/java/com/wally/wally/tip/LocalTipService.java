package com.wally.wally.tip;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LocalTipService implements TipService {
    public static final String TAG = LocalTipService.class.getSimpleName();
    JSONObject tips;
    private Map<String, List<String>> tipKeysForTags;

    public LocalTipService(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject tags  = obj.getJSONObject("tags");
            tips = obj.getJSONObject("tips");
            tipKeysForTags = new HashMap<>();
            setUpTags(tags);
            Log.d(TAG, tipKeysForTags.toString());
            Log.d(TAG, getRandom().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpTags(JSONObject tagsObject) throws JSONException {
        Iterator<String> tags = tagsObject.keys();
        while (tags.hasNext()) {
            String tag = tags.next();
            tipKeysForTags.put(tag, getTipKeys(tagsObject.getJSONArray(tag)));
        }
    }

    private List<String> getTipKeys(JSONArray keyArray) throws JSONException {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < keyArray.length(); i++) {
            keys.add((String) keyArray.get(i));
        }
        return keys;
    }

    @Override
    public Tip getRandom(String tag) {
        if (!tipKeysForTags.containsKey(tag)) { return null; }
        return getTip(getRandom(tipKeysForTags.get(tag)));
    }

    private Tip getTip(String tipKey) {
        try {
            JSONObject tipObject = tips.getJSONObject(tipKey);
            return new Tip()
                    .withTitle(tipObject.getString("title"))
                    .withMessage(tipObject.getString("message"));
        } catch (JSONException e) {
            Log.d(TAG, "incorrectly formatted tips file");
            return null;
        }
    }


    @Override
    public Tip getRandom() {
        String tag = getRandom(new ArrayList<>(tipKeysForTags.keySet()));
        return getRandom(tag);
    }

    private <T> T getRandom(List<T> list) {
        if (list.size() < 1) { return null; }
        int index = new Random().nextInt(list.size());
        return list.get(index);
    }
}