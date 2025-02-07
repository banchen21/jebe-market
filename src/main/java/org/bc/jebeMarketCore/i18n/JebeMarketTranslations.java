package org.bc.jebeMarketCore.i18n;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.ResourceBundle;

public class JebeMarketTranslations {
    private static final String LANG_MESSAGES = "lang.messages";
    private static Locale locale = Locale.CHINA;

    public JebeMarketTranslations(String language) {
        setupTranslations(language);
    }

    private void setupTranslations(@Nullable String language) {
        TranslationRegistry registry = TranslationRegistry.create(Key.key("jebemarket", "i18n"));
        if (language != null) {
            locale = Locale.forLanguageTag(language);
            ResourceBundle default_zhBundle = ResourceBundle.getBundle(LANG_MESSAGES, locale, UTF8ResourceBundleControl.get());
            registry.registerAll(locale, default_zhBundle, true);
        } else {
            locale = Locale.CHINA;
        }
        GlobalTranslator.translator().addSource(registry);
    }


    public static String getLocalizedMessage(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(LANG_MESSAGES, locale, UTF8ResourceBundleControl.get());
        return bundle.getString(key);
    }

}
