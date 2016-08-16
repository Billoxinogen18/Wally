package com.wally.wally.tip;

import android.content.Context;
import android.content.SharedPreferences;

import com.wally.wally.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LocalTipService implements TipService {
    public static final String TAG = LocalTipService.class.getSimpleName();
    private SharedPreferences preferences;
    private Set<String> disabled;
    private JSONObject tips;
    private Map<String, List<String>> tipKeysForTags;

    public static LocalTipService getInstance(Context context){
        return new LocalTipService(
                Utils.getAssetContentAsString(context, "tips.json"),
                context.getSharedPreferences("tips", Context.MODE_PRIVATE));
    }

    public LocalTipService(String json, SharedPreferences preferences) {
        this.preferences = preferences;
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject tags = obj.getJSONObject("tags");
            tips = obj.getJSONObject("tips");
            tipKeysForTags = new HashMap<>();
            setUpDisabled();
            setUpTags(tags);
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

    private void setUpDisabled(){
        disabled = new HashSet<>(preferences.getStringSet("disabled_tips", new HashSet<String>()));
    }

    private List<String> getTipKeys(JSONArray keyArray) throws JSONException {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < keyArray.length(); i++) {
            String key = keyArray.getString(i);
            if(!disabled.contains(key))
                keys.add(key);
        }
        return keys;
    }

    @Override
    public Tip getRandom(String tag) {
        if (!tipKeysForTags.containsKey(tag)) {
            return null;
        }
        return getTip(getRandom(tipKeysForTags.get(tag)));
    }

    @Override
    public void disableTip(String id) {
        disabled.add(id);

        for(String key : tipKeysForTags.keySet()){
            tipKeysForTags.get(key).remove(id);
        }

        preferences.edit().putStringSet("disabled_tips", disabled).apply();

    }

    private Tip getTip(String tipKey) {
        try {
            JSONObject tipObject = tips.getJSONObject(tipKey);
            return new Tip()
                    .withId(tipKey)
                    .withTitle(tipObject.getString("title"))
                    .withMessage(tipObject.getString("message"));
        } catch (JSONException e) {
            return null;
        }
    }


    @Override
    public Tip getRandom() {
        String tag = getRandom(new ArrayList<>(tipKeysForTags.keySet()));
        return getRandom(tag);
    }

    private <T> T getRandom(List<T> list) {
        if (list.size() < 1) {
            return null;
        }
        int index = new Random().nextInt(list.size());
        return list.get(index);
    }
}