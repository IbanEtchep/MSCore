package fr.iban.common.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder().hexColors().build();

    private static String legacy(Component component) {
        return LEGACY.serialize(component);
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    // --- conversion legacy -> MiniMessage ---

    @Test
    void laisseIntactUnTexteSansCode() {
        assertEquals("Bonjour tout le monde", MessageParser.legacyToMiniMessage("Bonjour tout le monde"));
    }

    @Test
    void convertitLesCouleursNommees() {
        assertEquals("<reset><green>", MessageParser.legacyToMiniMessage("&a"));
        assertEquals("<reset><dark_gray>", MessageParser.legacyToMiniMessage("&8"));
        assertEquals("<reset><white>", MessageParser.legacyToMiniMessage("&f"));
    }

    @Test
    void accepteLesCodesEnMajuscule() {
        assertEquals("<reset><red>", MessageParser.legacyToMiniMessage("&C"));
    }

    @Test
    void accepteLeMarqueurSection() {
        assertEquals("<reset><dark_gray>[<reset><red>DÉSACTIVÉ<reset><dark_gray>]<reset>",
                MessageParser.legacyToMiniMessage("§8[§CDÉSACTIVÉ§8]§r"));
    }

    @Test
    void convertitLesDecorationsSansReset() {
        assertEquals("<bold>", MessageParser.legacyToMiniMessage("&l"));
        assertEquals("<italic>", MessageParser.legacyToMiniMessage("&o"));
        assertEquals("<strikethrough>", MessageParser.legacyToMiniMessage("&m"));
        assertEquals("<underlined>", MessageParser.legacyToMiniMessage("&n"));
        assertEquals("<obfuscated>", MessageParser.legacyToMiniMessage("&k"));
    }

    @Test
    void convertitLeReset() {
        assertEquals("<reset>", MessageParser.legacyToMiniMessage("&r"));
    }

    // --- formats hexadecimaux ---

    @Test
    void convertitLHexEssentials() {
        assertEquals("<reset><color:#fdcb6e>", MessageParser.legacyToMiniMessage("&#fdcb6e"));
    }

    @Test
    void consommeLeMarqueurFinalDeLHexMineDown() {
        // Valeur historique de ping-prefix : le & final fait partie de la syntaxe MineDown.
        assertEquals("<reset><color:#fdcb6e>@", MessageParser.legacyToMiniMessage("&#fdcb6e&@"));
    }

    @Test
    void neConsommePasUnMarqueurFinalQuiOuvreUnAutreCode() {
        assertEquals("<reset><color:#ff0000><bold>Gras", MessageParser.legacyToMiniMessage("&#ff0000&lGras"));
    }

    @Test
    void convertitLHexSpigot() {
        assertEquals("<reset><color:#fdcb6e>", MessageParser.legacyToMiniMessage("&x&f&d&c&b&6&e"));
    }

    // --- cohabitation avec MiniMessage ---

    @Test
    void laisseIntactesLesBalisesMiniMessage() {
        String input = "<green>Salut</green> <click:run_command:'/spawn'><bold>ici</bold></click>";
        assertEquals(input, MessageParser.legacyToMiniMessage(input));
    }

    @Test
    void accepteLesDeuxSyntaxesDansLaMemeChaine() {
        assertEquals("<bold>gras</bold> <reset><red>rouge",
                MessageParser.legacyToMiniMessage("<bold>gras</bold> &crouge"));
    }

    // --- echappement et cas limites ---

    @Test
    void echappeLeMarqueurAvecUnAntislash() {
        assertEquals("&a", MessageParser.legacyToMiniMessage("\\&a"));
    }

    @Test
    void laisseIntactUnMarqueurQuiNOuvrePasUnCode() {
        assertEquals("Tom & Jerry", MessageParser.legacyToMiniMessage("Tom & Jerry"));
        assertEquals("&z", MessageParser.legacyToMiniMessage("&z"));
        assertEquals("fin &", MessageParser.legacyToMiniMessage("fin &"));
    }

    @Test
    void toleraLeNull() {
        assertEquals("", MessageParser.legacyToMiniMessage(null));
        assertEquals(Component.empty(), MessageParser.parse(null));
    }

    // --- rendu final ---

    @Test
    void rendUneChaineLegacyPureAlIdentique() {
        assertEquals("§aVert", legacy(MessageParser.parse("&aVert")));
        assertEquals("§#fdcb6e@Notch", legacy(MessageParser.parse("&#fdcb6e&@Notch")));
        assertEquals("§#fdcb6e@Notch", legacy(MessageParser.parse("&x&f&d&c&b&6&e@Notch")));
    }

    @Test
    void uneCouleurLegacyCoupeLesDecorationsPrecedentes() {
        Component component = MessageParser.parse("&lGras&aVert");
        assertEquals("§lGras§aVert", legacy(component));
    }

    @Test
    void uneCouleurMiniMessageNeCoupePasLesDecorations() {
        Component component = MessageParser.parse("<bold>Gras<green>Vert");
        assertEquals("§lGras§a§lVert", legacy(component));
    }

    @Test
    void resoutLesResolvers() {
        Component component = MessageParser.parse("&aSalut <player> !",
                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("player", "Notch"));
        assertEquals("Salut Notch !", plain(component));
    }

    @Test
    void neLaissePasUnResolverInjecterDuMarkup() {
        Component component = MessageParser.parse("&a<player>",
                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("player", "<red>&cfaux"));
        assertEquals("<red>&cfaux", plain(component));
        assertFalse(component.children().stream().anyMatch(c -> c.hasDecoration(TextDecoration.BOLD)));
    }
}
