package com.guying.controller;

import com.guying.dto.MessageVO;
import com.guying.pojo.Result;
import com.guying.repository.ChatHistoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {
    @Autowired
    private ChatHistoryRepository chatHistoryRepository;
    @Autowired
    private ChatMemory chatMemory;

    /**
     * 会话记录列表
     * @return
     */
    @GetMapping
    public List<String> getChatIds(){
        return chatHistoryRepository.get();
    }

    /**
     * 会话记录详情
     * @param chatId
     * @return
     */
    @GetMapping("/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable("chatId")String chatId){
        List<Message> messages = chatMemory.get(chatId);
        return messages.stream().map(MessageVO::new).toList();
    }

    @DeleteMapping("/{chatId}")
    public Result deleteChatHistory(@PathVariable("chatId")String chatId){
        chatHistoryRepository.delete(chatId);
        chatMemory.clear(chatId);
        return Result.success();
    }
}
