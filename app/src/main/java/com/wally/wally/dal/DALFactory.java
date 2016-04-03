package com.wally.wally.dal;

public class DALFactory {
    public static DataAccessLayer create() {
        return new FirebaseDAL();
    }
}
