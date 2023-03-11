package com.example.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private Node root = new Node();

    @PostConstruct //在服务启动时SensitiveFilter被构造为Bean,构造后执行初始化init方法,将敏感词文件构造到该bean的前缀树中
    public void init() {
        //从任意一个对象中获取类加载器（类加载器从类路径classpath（程序编译后的classes目录）中加载资源。resource目录下的资源也在classes中
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             // 将字节流转化为缓存字符流
            BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyword;
            while ((keyword = br.readLine()) != null) {
                // 添加到前缀树中
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词失败: ", e.getMessage());
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过率的文本
     * @return 过滤后的文本
     * */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        Node tempNode = root;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                //若指针1出于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == root) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }
            //检查下级节点
            tempNode = tempNode.map_node.get(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置;
                position = ++begin;
                // 重新指向根节点
                tempNode = root;
            } else if (tempNode.isWord) {
                // 发现敏感词，将begin-position字符串替换
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = root;
            } else {
                // 检查下一个字符
                position++;
            }

        }
        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }




    // 判断是否为符号
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 前缀树
    private class Node {
        // 关键词结束标识
        boolean isWord;
        String word;
        // 子节点<下级节点字符，下级节点>
        Map<Character, Node> map_node;

        public Node() {
            isWord = false;
            map_node = new HashMap<>();
        }
    }

    // 迭代法将一个铭感词添加到前缀树中
    private void addKeyword(String word) {
        Node temp = root;
        for (int i= 0; i < word.length(); i++) {
            Node child = root.map_node.get(word.charAt(i));
            if (child == null) {
                child = new Node();
                temp.map_node.put(word.charAt(i), child);
            }
            temp = child;
        }
        temp.isWord = true;
        temp.word = word;
    }

    // 递归的方法将敏感词添加到前缀树中
    private void put(String word) {
        if (word == null) return;
        root = put(root, word, 0);
    }

    private Node put(Node t, String word, int i) {
        if (t == null) t = new Node();
        if (i == word.length()) {
            t.isWord = true;
            t.word = word;
            return t;
        }
        Node next = t.map_node.get(word.charAt(i));
        next = put(next, word, i + 1);
        t.map_node.put(word.charAt(i), next);
        return t;
    }

}
