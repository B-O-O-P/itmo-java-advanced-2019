package ru.ifmo.rain.chizhikov.statistic;

import java.util.ListResourceBundle;

/**
 * Instance of {@link ListResourceBundle} for English in {@link TextStatistics}.
 */
public class UsageResourceBundle_en extends ListResourceBundle {
    private static final Object[][] CONTENTS = {
            {"analyzedFile", "Analyzed file"},
            {"commonStat", "Common statistic"},
            {"wordsStat", "Words stat"},
            {"sentencesStat", "Sentence stat"},
            {"linesStat", "Lines stat"},
            {"datesStat", "Dates stat"},
            {"currencyStat", "Currency stat"},
            {"numbersStat", "Numbers stat"},
            {"Number", "Number of"},
            {"number", "number"},
            {"numbers", "numbers"},
            {"numberel", "numbers"},
            {"word", "word"},
            {"words","words"},
            {"wordo","word"},
            {"sentence", "sentence"},
            {"sentences", "sentences"},
            {"sentencesya", "sentence"},
            {"line", "line"},
            {"lines", "lines"},
            {"linea", "line"},
            {"date", "date"},
            {"dates", "dates"},
            {"datea", "date"},
            {"currency", "currency"},
            {"sum", "sum"},
            {"min", "Minimum"},
            {"minya", "Minimum"},
            {"max", "Maximum"},
            {"maxya", "Maximum"},
            {"average", "Average"},
            {"averageya", "Average"},
            {"length", "length of"},
            {"unique", "unique"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
