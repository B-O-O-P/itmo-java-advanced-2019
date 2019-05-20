package ru.ifmo.rain.chizhikov.statistic;

import java.util.ListResourceBundle;

/**
 * Instance of {@link ListResourceBundle} for Russian in {@link TextStatistics}.
 */
public class UsageResourceBundle_ru extends ListResourceBundle {
    private static final Object[][] CONTENTS = {
            {"analyzedFile", "Анализируемый файл"},
            {"commonStat", "Сводная статистика"},
            {"wordsStat", "Статистика по словам"},
            {"sentencesStat", "Статистика по предложениям"},
            {"linesStat", "Статистика по строкам"},
            {"datesStat", "Статистика по датам"},
            {"currencyStat", "Статистика по валюте"},
            {"numbersStat", "Статистика по числам"},
            {"Number", "Число"},
            {"number", "число"},
            {"numbers", "числа"},
            {"numberel", "чисел"},
            {"words", "слов"},
            {"word","слова"},
            {"wordo","слово"},
            {"sentence", "предложение"},
            {"sentences", "предложений"},
            {"sentencesya", "предложения"},
            {"lines", "строк"},
            {"line", "строки"},
            {"linea", "строка"},
            {"date", "даты"},
            {"dates", "дат"},
            {"datea", "дата"},
            {"currencyet","валют"},
            {"currency", "валюты"},
            {"sum", "сумма"},
            {"min", "Минимальное"},
            {"minya", "Минимальная"},
            {"max", "Максимальное"},
            {"maxya", "Максимальная"},
            {"average", "Среднее"},
            {"averageya", "Средняя"},
            {"length", "длина"},
            {"unique", "уникальных"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
