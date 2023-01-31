package ru.TelegramBot.NavigationButtons;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emojis {

    HOUSE(EmojiParser.parseToUnicode(":house:")),
    FILE_FOLDER(EmojiParser.parseToUnicode(":file_folder:")),
    SHOPPING_TROLLEY(EmojiParser.parseToUnicode(":shopping_trolley:")),
    GEAR(EmojiParser.parseToUnicode(":gear:")),
    QUESTION(EmojiParser.parseToUnicode(":question:")),
    MEMO(EmojiParser.parseToUnicode(":memo:")),
    BUST_IN_SILHOUETTE(EmojiParser.parseToUnicode(":bust_in_silhouette:")),
    MAILBOX_WITH_MAIL(EmojiParser.parseToUnicode(":mailbox_with_mail:")),
    TELEPHONE_RECEIVER(EmojiParser.parseToUnicode(":telephone_receiver:")),
    ARROW_LEFT(EmojiParser.parseToUnicode(":arrow_left:")),
    PACKAGE(EmojiParser.parseToUnicode(":package:")),
    CONFUSED(EmojiParser.parseToUnicode(":confused:")),
    BEER(EmojiParser.parseToUnicode(":beer:")),
    DASH(EmojiParser.parseToUnicode(":dash:")),
    X(EmojiParser.parseToUnicode(":x:")),
    SMALL_RED_TRIANGLE_DOWN(EmojiParser.parseToUnicode(":small_red_triangle_down:")),
    SMALL_RED_TRIANGLE(EmojiParser.parseToUnicode(":small_red_triangle:")),
    ARROW_BACKWARD(EmojiParser.parseToUnicode(":arrow_backward:")),
    ARROW_FORWARD(EmojiParser.parseToUnicode(":arrow_forward:")),
    WHITE_CHECK_MARK(EmojiParser.parseToUnicode(":white_check_mark:")),
    ARROW_DOWN(EmojiParser.parseToUnicode(":arrow_down:")),
    RELAXED(EmojiParser.parseToUnicode(":relaxed:")),
    PENSIVE(EmojiParser.parseToUnicode(":pensive:")),
    ROUND_PUSHPIN(EmojiParser.parseToUnicode(":round_pushpin:")),
    SELFIE(EmojiParser.parseToUnicode(":selfie:")),
    CRY(EmojiParser.parseToUnicode(":cry:")),
    BEERS(EmojiParser.parseToUnicode(":beers:")),
    TRUMPET(EmojiParser.parseToUnicode(":trumpet:")),
    SMILEY(EmojiParser.parseToUnicode(":smiley:")),
    CAMERA(EmojiParser.parseToUnicode(":camera:"));
    private String emojiName;

    @Override
    public String toString() {
        return emojiName;
    }
}
