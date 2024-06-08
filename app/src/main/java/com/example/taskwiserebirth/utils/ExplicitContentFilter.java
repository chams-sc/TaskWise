package com.example.taskwiserebirth.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ExplicitContentFilter {

    private static final Set<Pattern> explicitPatterns = new HashSet<>();

    static {
        addPatterns();
    }

    private static void addPatterns() {
        // Profanity
        addPattern("damn");
        addPattern("hell");
        addPattern("shit");
        addPattern("fuck");
        addPattern("bitch");
        addPattern("bastard");
        addPattern("asshole");
        addPattern("dick");
        addPattern("pussy");
        addPattern("cunt");
        addPattern("whore");
        addPattern("slut");
        addPattern("fucker");
        addPattern("motherfucker");
        addPattern("ass");
        addPattern("bollocks");

        // Adult content
        addPattern("porn");
        addPattern("sex");
        addPattern("sexy");
        addPattern("naked");
        addPattern("nude");
        addPattern("boobs");
        addPattern("penis");
        addPattern("vagina");
        addPattern("breast");
        addPattern("masturbate");
        addPattern("orgasm");
        addPattern("stripper");

        // Hate speech
        addPattern("nigger");
        addPattern("faggot");

        // Dangerous activities
        addPattern("kill");
        addPattern("murder");
        addPattern("assassinate");
        addPattern("suicide");
        addPattern("bomb");
        addPattern("violence");
        addPattern("terrorism");
        addPattern("rape");
        addPattern("abuse");
        addPattern("drugs");
        addPattern("weapon");
        addPattern("gun");
        addPattern("knife");
        addPattern("explosive");
        addPattern("poison");
        addPattern("strangle");
        addPattern("harm");
        addPattern("fight");
        addPattern("stab");
        addPattern("shoot");
        addPattern("robbery");
        addPattern("assault");
    }

    private static void addPattern(String word) {
        explicitPatterns.add(Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE));
    }

    public static boolean containsExplicitContent(String input) {
        // Check for censored words represented by asterisks
        if (input.contains("*")) {
            return true;
        }

        // Check for explicit words
        for (Pattern pattern : explicitPatterns) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }
        return false;
    }
}
