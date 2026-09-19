package fr.iban.common.chat;

public final class ChatItemConstants {

    /** Marqueur sentinelle (zone privee Unicode U+E000) insere par le backend a la place
     *  du token [i]/[item], puis remplace par le Component de l'item sur le proxy. */
    public static final String ITEM_PLACEHOLDER = "\uE000";

    /** Permission requise pour partager l'item tenu en main dans le chat. */
    public static final String PERMISSION = "servercore.chat.item";

    /** Taille max du JSON de l'item (octets UTF-8). writeUTF plafonne a 65535 ; on reste en deca. */
    public static final int MAX_ITEM_BYTES = 25000;

    private ChatItemConstants() {
    }
}
