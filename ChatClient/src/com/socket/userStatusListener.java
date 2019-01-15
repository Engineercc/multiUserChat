package com.socket;

public interface userStatusListener {
    public void online(String login);
    public void offline(String login);
}
