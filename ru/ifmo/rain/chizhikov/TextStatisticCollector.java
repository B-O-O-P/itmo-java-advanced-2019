package ru.ifmo.rain.chizhikov.statistic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.*;

/**
 * Class for getting Report from given {@link java.io.File} into HTML
 */
public class TextStatisticCollector {

    private static String getTextLocale(String[] args) {
        String locales = Arrays.stream(args).limit(args.length - 2).reduce((s1, s2) -> s1 + " " + s2).orElse("");
        String[] locs = locales.split("\\) ");
        if (locs.length == 1) {
            return Objects.requireNonNull(args[0]);
        } else {
            return locs[0] + ")";
        }
    }
    private static String getReportLocale(String[] args) {
        String locales = Arrays.stream(args).limit(args.length - 2).reduce((s1, s2) -> s1 + " " + s2).orElse("");
        String[] locs = locales.split("\\) ");
        if (locs.length == 1) {
            return Arrays.stream(args).skip(1).limit(args.length - 3).reduce((s1, s2) -> s1 + " " + s2).orElse("");
        } else {
            return locs[1];
        }
    }

    private static String getTextFile(String[] args) {
        return Objects.requireNonNull(args[args.length - 2]);
    }

    private static String getReportFile(String[] args) {
        return Objects.requireNonNull(args[args.length - 1]);
    }

    /**
     * Main method for getting {@link TextStatistics}. 
     * Usage: <Text Locale> <Report Locale> <Text file> <Report file>.
     * @param args Arguments: Text {@link Locale}, Report {@link Locale}, Text {@link java.io.File},Report {@link java.io.File}
     */
    public static void main(String[] args) {
        if (args == null || args.length < 4) {
            System.err.println("Usage: <Text Locale> <Report Locale> <Text file> <Report file>.");
            Arrays.stream(Locale.getAvailableLocales()).map(Locale::getDisplayName).sorted().forEachOrdered(System.err::println);
            return;
        }

        String textLocale, reportLocale, textFile, reportFile;

        textLocale = getTextLocale(args);
        reportLocale = getReportLocale(args);
        textFile = getTextFile(args);
        reportFile = getReportFile(args);
        Locale txtLocale;
        Locale repLocale;

        try {
            txtLocale = Arrays.stream(Locale.getAvailableLocales()).filter(locale -> locale.getDisplayName()
                    .equals(textLocale)).findFirst().get();
            repLocale = Arrays.stream(Locale.getAvailableLocales()).filter(locale -> locale.getDisplayName()
                    .equals(reportLocale)).findFirst().get();
        } catch (NoSuchElementException e) {
            System.err.println("ERROR: Invalid locale.");
            return;
        }

        String text;

        try {
            text = Files.readString(Paths.get(textFile));
        } catch (IOException e) {
            System.err.println("ERROR: I/O Exception with input file.");
            return;
        } catch (InvalidPathException e) {
            System.err.println("ERROR: Invalid path of input file.");
            return;
        }

        TextStatistics statistics = new TextStatistics(txtLocale, text);
        Statistic sentencesStatistic = statistics.getStatistic(BreakIterator.getSentenceInstance(statistics.getLocale()), true);
        Statistic linesStatistic = statistics.getStatistic(BreakIterator.getLineInstance(statistics.getLocale()), false);
        Statistic wordsStatistic = statistics.getStatistic(BreakIterator.getWordInstance(statistics.getLocale()), false);

        List<Statistic> list = statistics.getStatisticsReport();
        Statistic currencyStatistic = list.get(0);
        Statistic numbersStatistic = list.get(1);
        Statistic dateStatistic = list.get(2);

        ResourceBundle bundle;
        switch (repLocale.getLanguage()) {
            case "en":
                bundle = ResourceBundle.getBundle("ru.ifmo.rain.chizhikov.statistic.UsageResourceBundle_en");
                break;
            case "ru":
                bundle = ResourceBundle.getBundle("ru.ifmo.rain.chizhikov.statistic.UsageResourceBundle_ru");
                break;
            default:
                System.err.println("ERROR: This locale is unavailable.");
                return;
        }

        String out = String.format("<html>\n" +
                        " <head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <title>Text statistic</title>\n" +
                        " </head>\n" +
                        " <body>\n" +
                        "\n" +
                        "  <h1>%s: %s</h1>\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n" +
                        "%s %s: %d<br>\n</p>" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s: %f<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s: %f<br>\n</p>" +
                        "\n" +

                        "<p><b>%s:</b><br>\n" +
                        "%s %s: %d (%d %s)<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s: %s<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "%s %s %s: %d (%s)<br>\n" +
                        "\n" +
                        " </body>\n" +
                        "</html>",
                bundle.getString("analyzedFile"),
                textFile,

                bundle.getString("commonStat"),
                bundle.getString("Number"),
                bundle.getString("words"),
                wordsStatistic.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("sentences"),
                sentencesStatistic.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("lines"),
                linesStatistic.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("dates"),
                dateStatistic.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("numberel"),
                numbersStatistic.numberOfElements,
                bundle.getString("Number"),
                bundle.getString("currencyet"),
                currencyStatistic.numberOfElements,

                bundle.getString("sentencesStat"),
                bundle.getString("Number"),
                bundle.getString("sentences"),
                sentencesStatistic.numberOfElements,
                sentencesStatistic.numberOfUniqueElements,
                bundle.getString("unique"),
                bundle.getString("min"),
                bundle.getString("sentence"),
                sentencesStatistic.minElement,
                bundle.getString("max"),
                bundle.getString("sentence"),
                sentencesStatistic.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("sentencesya"),
                sentencesStatistic.maxLength,
                sentencesStatistic.maxLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("sentencesya"),
                sentencesStatistic.minLength,
                sentencesStatistic.minLengthElement,
                bundle.getString("averageya"),
                bundle.getString("length"),
                bundle.getString("sentencesya"),
                ((int) sentencesStatistic.averageLength),

                bundle.getString("wordsStat"),
                bundle.getString("Number"),
                bundle.getString("words"),
                wordsStatistic.numberOfElements,
                wordsStatistic.numberOfUniqueElements,
                bundle.getString("unique"),
                bundle.getString("min"),
                bundle.getString("wordo"),
                wordsStatistic.minElement,
                bundle.getString("max"),
                bundle.getString("wordo"),
                wordsStatistic.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("word"),
                wordsStatistic.maxLength,
                wordsStatistic.maxLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("word"),
                wordsStatistic.minLength,
                wordsStatistic.minLengthElement,
                bundle.getString("averageya"),
                bundle.getString("length"),
                bundle.getString("word"),
                ((int) wordsStatistic.averageLength),

                bundle.getString("linesStat"),
                bundle.getString("Number"),
                bundle.getString("lines"),
                linesStatistic.numberOfElements,
                linesStatistic.numberOfUniqueElements,
                bundle.getString("unique"),
                bundle.getString("minya"),
                bundle.getString("linea"),
                linesStatistic.minElement,
                bundle.getString("maxya"),
                bundle.getString("linea"),
                linesStatistic.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("line"),
                linesStatistic.maxLength,
                linesStatistic.maxLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("line"),
                linesStatistic.minLength,
                linesStatistic.minLengthElement,
                bundle.getString("averageya"),
                bundle.getString("length"),
                bundle.getString("line"),
                ((int) linesStatistic.averageLength),

                bundle.getString("currencyStat"),
                bundle.getString("Number"),
                bundle.getString("currencyet"),
                currencyStatistic.numberOfElements,
                currencyStatistic.numberOfUniqueElements,
                bundle.getString("unique"),
                bundle.getString("minya"),
                bundle.getString("sum"),
                currencyStatistic.minElement,
                bundle.getString("maxya"),
                bundle.getString("sum"),
                currencyStatistic.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("currency"),
                currencyStatistic.maxLength,
                currencyStatistic.maxLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("currency"),
                currencyStatistic.minLength,
                currencyStatistic.minLengthElement,
                bundle.getString("averageya"),
                bundle.getString("sum"),
                currencyStatistic.averageLength,

                bundle.getString("numbersStat"),
                bundle.getString("Number"),
                bundle.getString("numberel"),
                numbersStatistic.numberOfElements,
                numbersStatistic.numberOfUniqueElements,
                bundle.getString("unique"),
                bundle.getString("min"),
                bundle.getString("number"),
                numbersStatistic.minElement,
                bundle.getString("max"),
                bundle.getString("number"),
                numbersStatistic.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("numbers"),
                numbersStatistic.maxLength,
                numbersStatistic.maxLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("numbers"),
                numbersStatistic.minLength,
                numbersStatistic.minLengthElement,
                bundle.getString("average"),
                bundle.getString("number"),
                numbersStatistic.averageLength,

                bundle.getString("datesStat"),
                bundle.getString("Number"),
                bundle.getString("dates"),
                dateStatistic.numberOfElements,
                dateStatistic.numberOfUniqueElements,
                bundle.getString("unique"),
                bundle.getString("minya"),
                bundle.getString("datea"),
                dateStatistic.minElement,
                bundle.getString("maxya"),
                bundle.getString("datea"),
                dateStatistic.maxElement,
                bundle.getString("maxya"),
                bundle.getString("length"),
                bundle.getString("date"),
                dateStatistic.maxLength,
                dateStatistic.maxLengthElement,
                bundle.getString("minya"),
                bundle.getString("length"),
                bundle.getString("date"),
                dateStatistic.minLength,
                dateStatistic.minLengthElement);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile));
            writer.write(out);
            writer.close();
        } catch (IOException ignored) {
        }
    }
}

