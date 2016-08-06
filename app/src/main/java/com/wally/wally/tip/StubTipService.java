package com.wally.wally.tip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StubTipService implements TipService {

    private Map<Tag, List<Tip>> tips;

    public StubTipService() {
        tips = new HashMap<>();
    }

    @Override
    public Tip getRandom(String  tag) {
        switch (tag) {
            case Tag.LEARNING:
                return new Tip()
                        .withTitle("Tip")
                        .withMessage("Learning tip");
            case Tag.LOCALIZATION:
                return new Tip()
                        .withTitle("Tip")
                        .withMessage("Localization tip");
            default:
                return null;
        }
    }

    @Override
    public Tip getRandom() {
        return getRandom(new String[] {Tag.LEARNING, Tag.LOCALIZATION}[new Random().nextInt(1)]);
    }
}
