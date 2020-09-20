package com.sfinias.telegram.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Data
public class Menu {

    public static final int MAX_ROW_LENGTH = 2;

    private String description;
    private String query;
    private Map<String, Menu> buttons;

    public List<String> getNextOptions() {

        return new ArrayList<>(buttons.keySet());
    }

    public Menu getNextMenu(String path) {

        if (!path.contains("_")) {
            return this;
        }
        String[] paths = StringUtils.split(path, "_", 1);
        return buttons.get(paths[0]).getNextMenu(paths[1]);
    }

    public Map<String, InlineKeyboardMarkup> createKeyboardMarkupMap() {

        return createKeyboardMarkupMap(new HashMap<>(), "main");
    }

    private Map<String, InlineKeyboardMarkup> createKeyboardMarkupMap(Map<String, InlineKeyboardMarkup> map, String path) {


        int subLevel = StringUtils.countMatches(path, '_');
        if (subLevel >= 1 && MapUtils.isEmpty(this.buttons)) {
            return map;
        }
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Entry<String,Menu> menuEntry : this.buttons.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton().setText(menuEntry.getValue().description).setCallbackData(path + "_" + menuEntry.getKey().toLowerCase());
            buttons.add(button);
            menuEntry.getValue().createKeyboardMarkupMap(map, path + "_" + menuEntry.getKey());
        }
        if (subLevel >= 1) {
            buttons.add(new InlineKeyboardButton().setText("<< Back").setCallbackData(StringUtils.substringBeforeLast(path, "_")));
            if (subLevel > 1) {
                buttons.add(new InlineKeyboardButton().setText("^ Main Menu").setCallbackData("main"));
            }
        }
        for (int i = 0; i < buttons.size(); i += 2) {
            if (i + 1 < buttons.size()) {
                rows.add(buttons.subList(i, i + 2));
            } else {
                rows.add(buttons.subList(i, i + 1));
            }
        }
        map.put(path, new InlineKeyboardMarkup(rows));
        return map;
    }
}
