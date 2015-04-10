package com.horoscope.exception;

/**
 * Created by Костя on 30.12.2014.
 */
public class AppNotInstalledException extends Exception {
    //Parameterless Constructor
    public AppNotInstalledException() {
    }

    //Constructor that accepts a message
    public AppNotInstalledException(String message) {
        super(message);
    }
}
