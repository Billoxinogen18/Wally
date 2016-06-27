package com.wally.wally.datacontroller.utils;


public abstract class Predicate<T> {

    public abstract boolean test(T target);

    public Predicate<T> not() {
        return new Predicate<T>() {
            @Override
            public boolean test(T target) {
                return !Predicate.this.test(target);
            }
        };
    }

    public Predicate<T> and(final Predicate<T> other) {
        return new Predicate<T>() {
            @Override
            public boolean test(T target) {
                return Predicate.this.test(target) && other.test(target);
            }
        };
    }

    public Predicate<T> or(final Predicate other) {
        return new Predicate<T>() {
            @Override
            public boolean test(T target) {
                return Predicate.this.test(target) || other.test(target);
            }
        };
    }
}
