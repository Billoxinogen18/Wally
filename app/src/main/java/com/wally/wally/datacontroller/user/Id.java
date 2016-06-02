package com.wally.wally.datacontroller.user;

import java.io.Serializable;

public class Id implements Serializable{
    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_FIREBASE = "firebase";
    public static final String PROVIDER_FACEBOOK = "facebook";

    private String provider;
    private String id;

    public Id(){

    }

    public Id(String provider, String id){
        // TODO validate
        this.provider = provider;
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return provider + ":" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Id id1 = (Id) o;

        if (provider != null ? !provider.equals(id1.provider) : id1.provider != null) return false;
        return id != null ? id.equals(id1.id) : id1.id == null;

    }

    @Override
    public int hashCode() {
        int result = provider != null ? provider.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
