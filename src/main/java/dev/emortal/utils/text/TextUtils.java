package dev.emortal.utils.text;

import java.util.Iterator;

public class TextUtils {

    private static char[] chars = new char[] {
            'ᴀ', 'ʙ', 'ᴄ', 'ᴅ', 'ᴇ', 'ꜰ', 'ɢ', 'ʜ', 'ɪ', 'ᴊ', 'ᴋ', 'ʟ', 'ᴍ', 'ɴ', 'ᴏ', 'ᴘ', 'ǫ', 'ʀ', 'ѕ', 'ᴛ', 'ᴜ', 'ᴠ', 'ᴡ', 'х', 'ʏ', 'ᴢ',
            '₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'
    };

    public static String smallText(String string) {
        StringBuilder sb = new StringBuilder(string.length());

        Iterator<Integer> iter = string.chars().iterator();
        while (iter.hasNext()) {
            Integer next = iter.next();
            if (97 <= next && 122 >= next) {
                sb.append(chars[next - 97]);
            } else if (48 <= next && 57 >= next) {
                sb.append(chars[next + 26 - 48]);
            } else {
                sb.append((char)next.intValue());
            }
        }

        return sb.toString();
    }

}
