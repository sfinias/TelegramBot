package com.sfinias.telegram;

import static java.lang.Math.toIntExact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sfinias.telegram.menu.Menu;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

//Prekss chat id: 931465266
//Sfinias chat id: 987275960
//Gakias id : 1027781267

public class PreksfiBot extends TelegramLongPollingBot {

    private Menu menu;
    private Map<String, InlineKeyboardMarkup> keyboardMenuMap;

    public PreksfiBot() {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            this.menu = mapper.readValue(PreksfiBot.class.getClassLoader().getResourceAsStream("menu.yml"), Menu.class);
            this.keyboardMenuMap = this.menu.createKeyboardMarkupMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBotToken() {

        return "1043479055:AAHhn2XR0H4j_NGCiUz_b7iX96Wb-hMmFn4";
    }

    public void onUpdateReceived(Update update) {

        User user = extractUser(update);

        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("[" + LocalDateTime.now() + "]" + user.getFirstName() + " " + user.getLastName() + "(" + user.getId() +
                    ") : " + update.getMessage().getText());
        }

        if (unauthorizedAccess(user)) {
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {

            long chatId = update.getMessage().getChatId();
            String command = update.getMessage().getText();
            switch (command) {
                case "/help":
                    help(update.getMessage().getChatId());
                    break;
                case "/start":
                default:
                    start(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            // Set variables
            String callData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String inlineChatId = update.getCallbackQuery().getInlineMessageId();
            System.out.println("MessageId : " + messageId + "\tChatId : " + chatId + "\tInlineChatId : " + inlineChatId);
            if (callData.equals("update_msg_text")) {
                updateMessage(messageId, chatId);
            } else {
//                Menu menu = this.menu.getNextMenu(callData);
                chooseMenu(this.keyboardMenuMap.get(callData), chatId, messageId);
            }
        }
    }

    private void updateMessage(long messageId, long chatId) {

        String answer = "Updated reply command";
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Updated reply command").setCallbackData("Updated reply command"));
        EditMessageText newMessage = new EditMessageText()
                .setChatId(chatId)
                .setMessageId(toIntExact(messageId))
                .setText(answer)
                .setReplyMarkup(markupInline);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void start(long id) {

        SendMessage reply = new SendMessage().setText("Choose 1 of the below options").setChatId(id)
                .setReplyMarkup(this.keyboardMenuMap.get("main"));
        try {
            execute(reply);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void chooseMenu(InlineKeyboardMarkup keyboardMenu, long id, long messageId) {

        EditMessageReplyMarkup reply = new EditMessageReplyMarkup()
                .setChatId(id).setReplyMarkup(keyboardMenu).setMessageId(toIntExact(messageId));
        try {
            execute(reply);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void help(long id) {

        SendMessage reply = new SendMessage();
        reply.setText("Choose 1 of the below options");
        reply.setChatId(id);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Update reply command").setCallbackData("update_msg_text"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        // Add it to the reply
        markupInline.setKeyboard(rowsInline);
        reply.setReplyMarkup(markupInline);
        try {
            execute(reply);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {

        return "PresfiBot";
    }

    private boolean unauthorizedAccess(User user) {

        if (!Arrays.asList("sfinias", "prekss", "AsAboveSoBelow33").contains(user.getUserName())) {
            SendAnimation animation = new SendAnimation();
            animation.setChatId(user.getId().longValue()).setAnimation("https://media1.tenor.com/images/0a691bb0ff447ac0a6a3b1e3bfa46265/tenor.gif");
            try {
                execute(animation);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private User extractUser(Update update) {

        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else {
            return update.getCallbackQuery().getFrom();
        }
    }
}
