package com.example.taskwiserebirth.utils;

import java.util.HashSet;
import java.util.Set;

public class ExplicitContentFilter {

    private static final Set<String> explicitWords;

    static {
        explicitWords = new HashSet<>();

        // Profanity
        explicitWords.add("damn");
        explicitWords.add("hell");
        explicitWords.add("shit");
        explicitWords.add("fuck");
        explicitWords.add("bitch");
        explicitWords.add("bastard");
        explicitWords.add("asshole");
        explicitWords.add("dick");
        explicitWords.add("pussy");
        explicitWords.add("cunt");
        explicitWords.add("whore");
        explicitWords.add("slut");
        explicitWords.add("fucker");
        explicitWords.add("motherfucker");
        explicitWords.add("ass");
        explicitWords.add("bollocks");

        // Adult content
        explicitWords.add("porn");
        explicitWords.add("sex");
        explicitWords.add("sexy");
        explicitWords.add("naked");
        explicitWords.add("nude");
        explicitWords.add("boobs");
        explicitWords.add("penis");
        explicitWords.add("vagina");
        explicitWords.add("breast");
        explicitWords.add("masturbate");
        explicitWords.add("orgasm");
        explicitWords.add("stripper");

        // Hate speech
        explicitWords.add("nigger");
        explicitWords.add("faggot");
        explicitWords.add("chink");
        explicitWords.add("spic");
        explicitWords.add("kike");
        explicitWords.add("cracker");

        // Dangerous activities
        explicitWords.add("kill");
        explicitWords.add("murder");
        explicitWords.add("assassinate");
        explicitWords.add("suicide");
        explicitWords.add("bomb");
        explicitWords.add("attack");
        explicitWords.add("violence");
        explicitWords.add("terrorism");
        explicitWords.add("rape");
        explicitWords.add("abuse");
        explicitWords.add("drugs");
        explicitWords.add("weapon");
        explicitWords.add("gun");
        explicitWords.add("knife");
        explicitWords.add("explosive");
        explicitWords.add("poison");
        explicitWords.add("strangle");
        explicitWords.add("harm");
        explicitWords.add("fight");
        explicitWords.add("stab");
        explicitWords.add("shoot");
        explicitWords.add("robbery");
        explicitWords.add("assault");
    }

    public static boolean containsExplicitContent(String input) {
        String lowerCaseInput = input.toLowerCase();
        for (String word : explicitWords) {
            if (lowerCaseInput.contains(word)) {
                return true;
            }
        }
        return false;
    }
}