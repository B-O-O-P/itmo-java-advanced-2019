package ru.ifmo.rain.chizhikov.statistic;

import org.w3c.dom.Text;

import java.text.*;
import java.util.*;
import java.util.stream.Collectors;


class TextStatistics {
    private Locale locale;
    private String text;

    private class CurrencyStatistic {
        private Number value;
        private String source;

        CurrencyStatistic(Number value, String source) {
            this.value = value;
            this.source = source;
        }

        Number getValue() {
            return value;
        }

        String getSource() {
            return source;
        }

    }

    private class DateStatistic {
        private Calendar value;
        private String source;

        DateStatistic(Calendar value, String name) {
            this.value = value;
            this.source = name;
        }

        Calendar getValue() {
            return value;
        }

        String getSource() {
            return source;
        }

    }

    TextStatistics(Locale locale, String text) {
        this.text = text;
        this.locale = locale;
    }

    Locale getLocale() {
        return locale;
    }

    Statistic getStatistic(BreakIterator it, boolean type) {
        Statistic statistic = new Statistic();

        double lengthSum = 0;
        Set<String> uniqueStrings = new HashSet<>();
        Collator comparator = Collator.getInstance(locale);

        it.setText(text);
        int startOfString = it.first();
        int endOfString = it.next();

        while (endOfString != BreakIterator.DONE) {
            String currentElement = (type) ?
                    text.substring(startOfString, endOfString) :
                    text.substring(startOfString, endOfString).replaceAll("\\s", "");

            if (!currentElement.isEmpty()) {

                statistic.numberOfElements++;
                lengthSum += currentElement.length();
                uniqueStrings.add(currentElement);

                if (!(statistic.minLengthElement != null && currentElement.length() >= statistic.minLength)) {
                    statistic.minLengthElement = currentElement;
                    statistic.minLength = currentElement.length();
                }

                if (!(statistic.maxLengthElement != null && currentElement.length() <= statistic.maxLength)) {
                    statistic.maxLengthElement = currentElement;
                    statistic.maxLength = currentElement.length();
                }

                if (!(statistic.minElement != null && comparator.compare(currentElement, statistic.minElement) >= 0)) {
                    statistic.minElement = currentElement;
                }

                if (!(statistic.maxElement != null && comparator.compare(statistic.minElement, currentElement) >= 0)) {
                    statistic.maxElement = currentElement;
                }

            }

            startOfString = endOfString;
            endOfString = it.next();
        }

        statistic.numberOfUniqueElements = uniqueStrings.size();
        statistic.averageLength = lengthSum / statistic.numberOfElements;

        return statistic;
    }


    List<Statistic> getStatisticsReport() {
        Statistic currencyStatistic = new Statistic();
        Statistic numberStatistic = new Statistic();
        Statistic dateStatistic = new Statistic();

        BreakIterator it = BreakIterator.getWordInstance(locale);
        BreakIterator linesIt = BreakIterator.getLineInstance(locale);

        NumberFormat isCurrency = NumberFormat.getCurrencyInstance(locale);
        NumberFormat isNumber = NumberFormat.getNumberInstance(locale);
        DateFormat isDate = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        List<CurrencyStatistic> currencyList = new ArrayList<>();
        List<Double> numberList = new ArrayList<>();
        List<DateStatistic> dateList = new ArrayList<>();

        it.setText(text);
        int startOfString = it.first();
        int endOfString = it.next();

        linesIt.setText(text);
        int startOfLine = linesIt.first();
        int endOfLine = linesIt.next();

        while (endOfLine != BreakIterator.DONE) {
            String currentLine = text.substring(startOfLine, endOfLine);

            startOfLine = endOfLine;
            endOfLine = linesIt.next();

            try {
                Calendar current = Calendar.getInstance(locale);
                current.setTime(isDate.parse(currentLine));

                dateList.add(new DateStatistic(current, currentLine));
            } catch (ParseException ignored) {
            }
        }

        while (endOfString != BreakIterator.DONE) {
            String currentElement = text.substring(startOfString, endOfString);

            startOfString = endOfString;
            endOfString = it.next();

            try {
                Number current = isCurrency.parse(currentElement);
                currencyList.add(new CurrencyStatistic(current, currentElement));
                numberList.add(current.doubleValue());
                continue;
            } catch (ParseException ignored) {
            }

            try {
                Number current = isNumber.parse(currentElement);
                numberList.add(current.doubleValue());
            } catch (ParseException ignored) {
            }
        }

        if (!currencyList.isEmpty()) {
            currencyStatistic.numberOfElements = currencyList.size();
            currencyStatistic.numberOfUniqueElements = currencyList.stream().distinct().collect(Collectors.toList()).size();
            currencyStatistic.minElement = currencyList.stream()
                    .reduce((e1, e2) -> (e1.getValue().doubleValue() < e2.getValue().doubleValue()) ? e1 : e2).get().getSource();
            currencyStatistic.maxElement = currencyList.stream()
                    .reduce((e1, e2) -> (e1.getValue().doubleValue() > e2.getValue().doubleValue()) ? e1 : e2).get().getSource();
            currencyStatistic.minLengthElement = currencyList.stream().map(CurrencyStatistic::getSource)
                    .min(Comparator.comparing(String::length)).get();
            currencyStatistic.maxLengthElement = currencyList.stream().map(CurrencyStatistic::getSource)
                    .max(Comparator.comparing(String::length)).get();
            currencyStatistic.minLength = currencyStatistic.minLengthElement.length();
            currencyStatistic.maxLength = currencyStatistic.maxLengthElement.length();
            currencyStatistic.averageLength = currencyList.stream().map(e -> e.getValue().doubleValue())
                    .reduce((d1, d2) -> d1 + d2).get() / currencyStatistic.numberOfElements;
        }

        if (!numberList.isEmpty()) {
            numberStatistic.numberOfElements = numberList.size();
            numberStatistic.numberOfUniqueElements = numberList.stream().distinct().collect(Collectors.toList()).size();
            numberStatistic.minElement = numberList.stream().min(Double::compare).get().toString();
            numberStatistic.maxElement = numberList.stream().max(Double::compare).get().toString();
            numberStatistic.minLengthElement = numberList.stream()
                    .map(d -> ((d == Math.floor(d)) && !Double.isInfinite(d)) ? Integer.toString(d.intValue()) : d.toString())
                    .min(Comparator.comparing(String::length)).get();
            numberStatistic.maxLengthElement = numberList.stream()
                    .map(d -> ((d == Math.floor(d)) && !Double.isInfinite(d)) ? Integer.toString(d.intValue()) : d.toString())
                    .max(Comparator.comparing(String::length)).get();
            numberStatistic.minLength = numberStatistic.minLengthElement.length();
            numberStatistic.maxLength = numberStatistic.maxLengthElement.length();
            numberStatistic.averageLength = numberList.stream()
                    .reduce((d1, d2) -> d1 + d2).get() / numberStatistic.numberOfElements;
        }

        if (!dateList.isEmpty()) {
            dateStatistic.numberOfElements = dateList.size();
            dateStatistic.numberOfUniqueElements = dateList.stream().distinct().collect(Collectors.toList()).size();
            dateStatistic.minElement = dateList.stream()
                    .reduce((e1, e2) -> (e1.getValue().before(e2.getValue())) ? e1 : e2).get().getSource();
            dateStatistic.maxElement = dateList.stream()
                    .reduce((e1, e2) -> (e1.getValue().after(e2.getValue())) ? e1 : e2).get().getSource();
            dateStatistic.minLengthElement = dateList.stream().map(DateStatistic::getSource)
                    .min(Comparator.comparing(String::length)).get();
            dateStatistic.maxLengthElement = dateList.stream().map(DateStatistic::getSource)
                    .max(Comparator.comparing(String::length)).get();
            dateStatistic.minLength = dateStatistic.minLengthElement.length();
            dateStatistic.maxLength = dateStatistic.maxLengthElement.length();
            dateStatistic.averageLength = dateList.stream().map(e -> (double) e.getValue().getTime().getTime())
                    .reduce((d1, d2) -> d1 + d2).get() / dateStatistic.numberOfElements;
        }

        List<Statistic> statisticList = new ArrayList<>();
        statisticList.add(currencyStatistic);
        statisticList.add(numberStatistic);
        statisticList.add(dateStatistic);

        return statisticList;
    }

}
