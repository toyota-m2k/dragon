package com.michael.dragon.logic;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author M.TOYOTA 13/09/10 Created.
 * @author Copyright (C) 2013 MetaMoJi Corp. All Rights Reserved.
 */
public class DcWordSplitter {
    class Block {
        public String word;     // 単語
        public String chunk;    // 単語＋デリミタ

        Block(String word, String chunk) {
            this.word = word;
            this.chunk = chunk;
        }
    }
    ArrayList<Block> mComplete;
    ArrayList<String> mRandom;
    int mChecking = 0;

    public DcWordSplitter() {
        mComplete = new ArrayList<Block>();
        mRandom = new ArrayList<String>();
    }

    static final Pattern pattern = Pattern.compile("(([^,.?:; \\t\\n\\s\\r]+)([,.?:; \\t\\n\\s\\r]+))");

    public ArrayList<String> purse(String src){
        mComplete.clear();
        mRandom.clear();
        mChecking = 0;

        int i = 0;
        Matcher m = pattern.matcher(src);
        while( m.find() ) {
            if( m.groupCount()<3 ) {
                return null;
            }
            String w = m.group(2), c = m.group(0);

            mComplete.add( new Block(w,c));
            mRandom.add(w);
        }
        Collections.shuffle(mRandom);
        return mRandom;
    }

    public boolean hasValue() {
        return mChecking < mComplete.size();
    }

    public boolean check(String word){
        return mComplete.get(mChecking).word.equalsIgnoreCase(word);
    }

    public String peekNextWord() {
        return mComplete.get(mChecking).word;
    }
    public String getChunkAndNext() {
        String r =  mComplete.get(mChecking).chunk;
        mChecking++;
        return r;
    }

    final String SAVE_RANDOM = "RandomArray";
    final String SAVE_CHECKING = "Checking";

    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(SAVE_RANDOM, mRandom.toArray(new String[0]));
        outState.putInt(SAVE_CHECKING, mChecking);
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        ArrayList<String> random = new ArrayList<String>(Arrays.asList(savedInstanceState.getStringArray(SAVE_RANDOM)));
        int checking = savedInstanceState.getInt(SAVE_CHECKING, -1);
        if( mComplete.size() != random.size()) {
            return;
        }
        if( checking <0 || checking >mComplete.size()) {
            return;
        }
        mChecking = checking;
        mRandom = random;
    }

    public ArrayList<String> getRandomList() {
        return mRandom;
    }

    public int getChecking() {
        return mChecking;
    }

    public String getCurrentAnswer() {
        if( mChecking == 0 ) {
            return "";
        } else if( mChecking == 1) {
            return mComplete.get(0).chunk;
        } else {
            StringBuilder sb = new StringBuilder();
            for( int i=0 ; i<mChecking ; i++ ) {
                sb.append(mComplete.get(i).chunk);
            }
            return sb.toString();
        }
    }

}
