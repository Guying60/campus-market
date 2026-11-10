package com.guying.controller;


import com.guying.repository.ChatHistoryRepository;
import com.guying.utils.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ChatHistoryRepository chatHistoryRepository;
    @Autowired
    private ProductTools productTools;

    @RequestMapping (value = "/service", produces = "text/html; charset=utf-8")
    public Flux<String> service(String prompt,String chatId) {
        //保存会话ID
        chatHistoryRepository.save(chatId);
        //请求模型
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .tools(productTools)
                .stream()
                .content();
    }
}