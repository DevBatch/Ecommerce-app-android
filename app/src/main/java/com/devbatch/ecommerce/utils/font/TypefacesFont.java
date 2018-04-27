package com.devbatch.ecommerce.utils.font;


public enum TypefacesFont implements TypefaceUtil.TypefaceId {
    ROBOTO_BLACK("roboto_black.ttf"),
    ROBOTO_BLACK_ITALIC("roboto_black_italic.ttf"),
    ROBOTO_BOLD("roboto_bold.ttf"),
    ROBOTO_BOLD_CONDENSED("roboto_bold_condensed.ttf"),
    ROBOTO_BOLD_CONDENSED_ITALIC("roboto_bold_condensed_italic.ttf"),
    ROBOTO_BOLD_ITALIC("roboto_bold_italic.ttf"),
    ROBOTO_CONDENSED("roboto_condensed.ttf"),
    ROBOTO_CONDENSED_ITALIC("roboto_condensed_italic.ttf"),
    ROBOTO_ITALIC("roboto_italic.ttf"),
    ROBOTO_LIGHT("roboto_light.ttf"),
    ROBOTO_LIGHT_ITALIC("roboto_light_italic.ttf"),
    ROBOTO_MEDIUM("roboto_medium.ttf"),
    ROBOTO_MEDIUM_ITALIC("roboto_medium_italic.ttf"),
    ROBOTO_RAGULAR("roboto_regular.ttf"),
    ROBOTO_THIN("roboto_thin.ttf"),
    ROBOTO_THIN_ITALIC("roboto_thin_italic.ttf");


    public static final String BASE_PATH = "fonts/";

    private String filename;

    TypefacesFont(String filename) {
        this.filename = filename;
    }

    public static TypefacesFont from(int index) {
        switch (index) {
            case 0:
                return ROBOTO_BLACK;
            case 1:
                return ROBOTO_BLACK_ITALIC;
            case 2:
                return ROBOTO_BOLD;
            case 3:
                return ROBOTO_BOLD_CONDENSED;
            case 4:
                return ROBOTO_BOLD_CONDENSED_ITALIC;
            case 5:
                return ROBOTO_BOLD_ITALIC;
            case 6:
                return ROBOTO_CONDENSED;
            case 7:
                return ROBOTO_CONDENSED_ITALIC;
            case 8:
                return ROBOTO_ITALIC;
            case 9:
                return ROBOTO_LIGHT;
            case 10:
                return ROBOTO_LIGHT_ITALIC;
            case 11:
                return ROBOTO_MEDIUM;
            case 12:
                return ROBOTO_MEDIUM_ITALIC;
            case 13:
                return ROBOTO_RAGULAR;
            case 14:
                return ROBOTO_THIN;
            case 15:
                return ROBOTO_THIN_ITALIC;
            default:
                return ROBOTO_RAGULAR;
        }
    }

    @Override
    public android.graphics.Typeface get() {
        return TypefaceUtil.getTypeface(this);
    }

    @Override
    public String getFilePath() {
        return BASE_PATH + filename;
    }
}