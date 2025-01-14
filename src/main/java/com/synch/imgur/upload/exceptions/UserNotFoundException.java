package com.synch.imgur.upload.exceptions;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String message)
    {
        super(message);
    }
}
