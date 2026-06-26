package com.countmyh.service.calendar;

/**
 * Maps a BCP-47 language tag to its holiday calendar resource file.
 *
 * Convention: the tag is derived directly from the locale tag by replacing
 * hyphens with underscores, then wrapping with the standard prefix/suffix:
 *   "pt-BR"  →  holidays_pt_BR.properties
 *   "en-GB"  →  holidays_en_GB.properties
 *   "ja-JP"  →  holidays_ja_JP.properties
 *
 * Adding a new country requires only a new file in
 * resources/com/countmyh/holidays/ — no code change needed here.
 * If the file is absent, HolidayCalendarLoader returns an empty calendar.
 */
public class HolidayCalendarFactory {

    static final String _PREFIX_FILE_LANGUAGE = "holidays_";
    static final String _HIFEN = "-";
    static final String _UNDERSCORE = "_";
    static final String _EXTENDSION_DOCS = ".properties";

    public static HolidayCalendar forLocale(String languageTag) {
        // "pt-BR" → "holidays_pt_BR.properties"
        var language = languageTag.replace(_HIFEN, _UNDERSCORE);
        String file = _PREFIX_FILE_LANGUAGE + language + _EXTENDSION_DOCS;
        return HolidayCalendarLoader.load(file);
    }
}
