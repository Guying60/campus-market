package com.guying;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class CppApplicationTests {


    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Test
    void contextLoads() {
        float[] helloWorlds = openAiEmbeddingModel.embed("hello world");
        System.out.println(Arrays.toString(helloWorlds));
    }



}
