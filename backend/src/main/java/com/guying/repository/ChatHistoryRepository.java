package com.guying.repository;

import java.util.List;

public interface ChatHistoryRepository {
    /**
     * 保存会话记录
     * @param
     * @param
     */
    void save(String chatId);


    /**
     * 获取会话记录
     * @param
     * @param
     * @return
     */
    List<String> get();

    void delete(String chatId);
}
