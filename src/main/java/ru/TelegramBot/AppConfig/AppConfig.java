package ru.TelegramBot.AppConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import ru.TelegramBot.BotConfig.TelegramBotConfig;
import ru.TelegramBot.BotLogic.HQDTelegramBot;
import ru.TelegramBot.BotLogic.TelegramFacade;

@Configuration
public class AppConfig {
    private final TelegramBotConfig botConfig;

    public AppConfig(TelegramBotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(botConfig.getWebHookPath()).build();
    }

    @Primary
    @Bean(name = "createTelegramBot")
    public HQDTelegramBot createTelegramBot(TelegramFacade telegramFacade) {
        HQDTelegramBot bot = new HQDTelegramBot(telegramFacade);
        bot.setBotToken(botConfig.getBotToken());
        bot.setBotUserName(botConfig.getUserName());
        bot.setBotPath(botConfig.getWebHookPath());

        return bot;
    }
}
