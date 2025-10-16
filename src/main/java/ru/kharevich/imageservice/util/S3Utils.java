package ru.kharevich.imageservice.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class S3Utils {

    /**
     * Извлекает имя файла из полного S3 ключа
     * Пример: "icons/heart.svg" -> "heart.svg"
     * Пример: "avatars/user123.jpg" -> "user123.jpg"
     */
    public static String extractFileName(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return s3Key;
        }

        if (s3Key.contains("/")) {
            return s3Key.substring(s3Key.lastIndexOf("/") + 1);
        }
        return s3Key;
    }

    /**
     * Проверяет, является ли файл SVG иконкой
     */
    public static boolean isSvgIcon(String s3Key) {
        if (s3Key == null) {
            return false;
        }
        return s3Key.startsWith("icons/") && s3Key.endsWith(".svg");
    }

    /**
     * Нормализует имя иконки, добавляя расширение .svg если нужно
     */
    public static String normalizeIconName(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return iconName;
        }

        if (!iconName.endsWith(".svg")) {
            return iconName + ".svg";
        }
        return iconName;
    }

    /**
     * Формирует полный S3 ключ для иконки
     */
    public static String buildIconKey(String iconName) {
        return "icons/" + normalizeIconName(iconName);
    }

}