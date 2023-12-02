package org.superchat.server.common.utils;

import java.util.*;
/**
 *@author CtrlCver
 *@data 2023/11/21
 *@description:  字典树，ac自动机，kmp  敏感词过滤
 */
public class ACTrieUtil {
    private static Word  root;
    
    static class Word{
        boolean end=false;
        Word failOver=null;
        int depth=0;
        Map<Character,Word> next=new HashMap<>();


        public boolean hasChild(char c) {
            return next.containsKey(c);
        }
    } 
    
    static {
        Set<String> set = new HashSet<>();
        try {
            set.add("sb");
            set.add("SB");
            set.add("she");
            set.add("shr");
        }catch (Exception e){
            System.out.println("----------------敏感词库加载失败------------------");
        }
        createAC(set);
        initFailOver();
        System.out.println("----------------敏感词库已加载------------------");
    }

    public  static void createAC(Set<String> set){
        Word currentNode = new Word();
        root=currentNode;
        for(String key : set)
        {
            int i=0;
            currentNode=root;
            for(int j=0;j<key.length();j++) //abcd
            {
                if(currentNode.next!=null&&currentNode.next.containsKey(key.charAt(j))){
                    //daqt
                    currentNode= currentNode.next.get(key.charAt(j));
                    //防止乱序输入改变end,比如da，dadac，dadac先进入，第二个a为false,da进入后把a设置为true
                    if(j==key.length()-1){
                        currentNode.end=true;
                    }
                }else {
                    Word map = new Word();
                    if(j==key.length()-1){
                        map.end=true;
                    }
                    currentNode.next.put(key.charAt(j), map);
                    currentNode=map;
                }
                currentNode.depth = j+1;
            }
        }
    }
    public static void initFailOver(){
        Queue<Word> queue=new LinkedList<>();
        Map<Character,Word> children=root.next;
        for(Word node:children.values())
        {
            node.failOver=root;
            queue.offer(node);
        }
        while(!queue.isEmpty())
        {
            Word parentNode=queue.poll();
            for(Map.Entry<Character,Word> entry:parentNode.next.entrySet())
            {
                Word childNode=entry.getValue();
                Word failOver=parentNode.failOver;
                while(failOver!=null&&(!failOver.next.containsKey(entry.getKey()))){
                    failOver=failOver.failOver;
                }
                if(failOver==null){
                    childNode.failOver=root;
                }else{
                    childNode.failOver=failOver.next.get(entry.getKey());
                }
                queue.offer(childNode);
            }
        }
    }
    public static String match(String matchWord)
    {
        Word walkNode=root;
        char[] wordArray=matchWord.toCharArray();
        for(int i=0;i<wordArray.length;i++)
        {
            while(!walkNode.hasChild(wordArray[i]) && walkNode.failOver!=null)
            {
                walkNode=walkNode.failOver;
            }
            if(walkNode.hasChild(wordArray[i])) {
                walkNode=walkNode.next.get(wordArray[i]);
                if(walkNode.end){
                    Word tempNode = walkNode;
                    Word tempNode2 = walkNode; //记录end节点
                    int k = i+1;
                    boolean flag=false;
                    //判断end是不是最终end即敏感词是否存在包含关系
                    while(k < wordArray.length && tempNode.hasChild(wordArray[k])) {
                        tempNode = tempNode.next.get(wordArray[k]);
                        k++;
                        if(tempNode.end)
                        {
                            tempNode2=tempNode;
                            flag=true;
                        }
                    }
                    //根据结果去替换*
                    if(flag){
                        int length=tempNode2.depth;
                        while(length>0)
                        {
                            length--;
                            wordArray[i+length]='*';
                        }
                        i=i+length;
                        walkNode = tempNode2.failOver;
                    }else{
                        int length=walkNode.depth;
                        while (length>0){
                            length--;
                            wordArray[i-length]='*';
                        }
                        walkNode = walkNode.failOver;
                    }
                }
            }
        }
        return new String(wordArray);
    }
}
