package com.wally.wally.dal;

/**
 * Created by Viper on 4/3/2016.
 */
public class DALFactory {
    public static DataAccessLayer create() {
        return new FirebaseDAL();
    }
}
