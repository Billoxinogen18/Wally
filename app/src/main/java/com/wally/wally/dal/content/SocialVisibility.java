package com.wally.wally.dal.content;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SocialVisibility implements Serializable {
    private Map<SocialNetwork, List<String>> socialNetworkMap;

    private SocialVisibility(){

    }
}
