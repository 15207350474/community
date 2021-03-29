package com.example.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***"; //替换符

    private TrieNode root = new TrieNode();


    @PostConstruct
    public void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {

            String keyWord;
            while ((keyWord = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyWord(keyWord);
            }


        } catch (IOException e) {
            logger.error("加载敏感词操作失败：" + e.getMessage());
        }
    }

    //前缀树
    private class TrieNode {
        private boolean isEnd = false; //结束标志

        public void setIsEnd(boolean isEnd) {
            this.isEnd = isEnd;
        }

        private Map<Character, TrieNode> subNodes = new HashMap<>(); //子节点

        public boolean isEnd() {
            return isEnd;
        }

        public void addSubNode(Character c, TrieNode trieNode) {
            subNodes.put(c, trieNode);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }


    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) return text;

        TrieNode curNode = root;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)) {
                if (curNode == root) {
                    sb.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }
            curNode = curNode.getSubNode(c);
            if (curNode == null) {
                sb.append(text.charAt(begin));
                position = ++begin;
                curNode = root;
            } else if (curNode.isEnd()) {
                sb.append(REPLACEMENT);
                begin = ++position;
                curNode = root;
            } else {
                ++position;
            }

            sb.append(text.substring(begin));

        }

        return sb.toString();

    }


    //判断是否是符号
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    private void addKeyWord(String keyWord) {
        TrieNode curNode = root;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);

            TrieNode subNode = curNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                curNode.addSubNode(c, subNode);
            }
            curNode = subNode;
            if (i == keyWord.length() - 1) curNode.setIsEnd(true);
        }
    }
}
