package com.blog.service;

import com.blog.entity.Message;

import java.util.List;

public interface MessageService {
    public String getLastMessageById(int userId, int sendId);

    public String saveMessageById(int id, int toId, String message);

    public List<Message> getMessage(int id, int sid);

    public List<Message> getAllMessage(int id);
}
