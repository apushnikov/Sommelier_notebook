package com.apushnikov.sommelier_notebook.utilities;

public class Utilities {

    //=============================================================================
    // Метод возвращает Строку результата в формате ДД.ММ.ГГГГ
    // Если хотя бы одно поле даты равно 0, то
    //      - dateMode == 1: возвращает "Выберите дату"
    //      - dateMode != 1: возвращает "нет даты"
    //=============================================================================
    public static String getDateWine (int dateMode, int dateDay, int dateMonth, int dateYear) {

        // Если все поля даты не равны 0
        if (dateDay != 0 && dateMonth != 0 && dateYear != 0) {
            // то возвращаем Строку результата в формате ДД.ММ.ГГГГ
            return " " + dateDay + "." +  dateMonth + "." + dateYear + " ";
        } else {
            if (dateMode == 1) {
                // Если хотя бы одно поле даты равно 0
                return "Выберите дату";
            } else {
                return "нет даты";
            }
        }
    }

    //=============================================================================
    // Преобразует голосовую строку в строку типа float
    // Распознает в качестве разделителя точку или запятую. Примеры преобразования
    //        "15 градусов"         -> "15.0";
    //        ",12 градусов"        -> "0.12";
    //        ", градусов"          -> "0.0";
    //        ","                   -> "0.0";
    //        "12,05 градусов"      -> "12.05";
    //        "литры,05 градусов"   -> "0.05";
    //        "12, градусов"        -> "12.0";
    //=============================================================================
    public static String voiceLineToLineFloat (String dirtyText) {

        float i = 0.0F;

        // Проверяем вхождение точки
        String dot = ".";
        int indexDot = dirtyText.indexOf(dot);
        // Проверяем вхождение запятой
        String comma = ",";
        int indexComma = dirtyText.indexOf(comma);

        // TODO: Очень запутанный алгорит, непрозрачный. Нужно упростить!
        if (indexDot < 0) // Если точки нет
        {
            if (indexComma < 0) // Если запятой нет
            {
                // точки нет, запятой нет
                // Остаток строки преобразум в float (отбрасывая нечисловые символы)
                return dirtyText.replaceAll("[\\D]", "");
            } else // запятае есть
            {
                if (indexComma == 0) // Если запятая на нулевой позиции
                {
                    // точки нет, запятая на нулевой позиции
                    // извлекаем строку-число
                    String s = dirtyText.replaceAll("[\\D]", "");
                    if (s.isEmpty()) s = "0";
                    // конструируем float
                    String strFloat = "0." + s;
                    return strFloat;
                } else // Запятая не на нулевой
                {
                    // точки нет, запятая не на нулевой позиции
                    // извлекаем строку-число до разделителя
                    String sBegin = dirtyText.substring(0,indexComma);
                    sBegin = sBegin.replaceAll("[\\D]", "");
                    if (sBegin.isEmpty()) sBegin = "0";
                    // извлекаем строку-число после разделителя
                    String sEnd = dirtyText.substring(indexComma);
                    sEnd = sEnd.replaceAll("[\\D]", "");
                    if (sEnd.isEmpty()) sEnd = "0";
                    // конструируем float
                    String strFloat = sBegin + "." + sEnd;
                    return strFloat;
                }

            }

        } else // Если точка есть
        {
            if (indexDot == 0) // Если точка на нулевой позиции
            {
                // Точка на нулевой позиции
                // извлекаем строку-число
                String s = dirtyText.replaceAll("[\\D]", "");
                if (s.isEmpty()) s = "0";
                // конструируем float
                String strFloat = "0." + s;
                return strFloat;
            }
            else // Если точка не на нулевой позиции
            {
                // Точка не на нулевой позиции
                if (indexDot < indexComma) // Если точка раньше запятой
                {
                    // Точка не на нулевой позиции, точка раньше запятой
                    // извлекаем строку-число до разделителя
                    String sBegin = dirtyText.substring(0,indexDot);
                    sBegin = sBegin.replaceAll("[\\D]", "");
                    if (sBegin.isEmpty()) sBegin = "0";
                    // извлекаем строку-число после разделителя
                    String sEnd = dirtyText.substring(indexDot);
                    sEnd = sEnd.replaceAll("[\\D]", "");
                    if (sEnd.isEmpty()) sEnd = "0";
                    // конструируем float
                    String strFloat = sBegin + "." + sEnd;
                    return strFloat;
                }
                else // Если точка позже запятой
                {
                    if (indexComma < 0) // Если запятой нет
                    {
                        // Точка не на нулевой позиции
                        // Если точка позже запятой
                        // Если запятой нет
                        // Реагируем на точку
                        // извлекаем строку-число до разделителя
                        String sBegin = dirtyText.substring(0,indexDot);
                        sBegin = sBegin.replaceAll("[\\D]", "");
                        if (sBegin.isEmpty()) sBegin = "0";
                        // извлекаем строку-число после разделителя
                        String sEnd = dirtyText.substring(indexDot);
                        sEnd = sEnd.replaceAll("[\\D]", "");
                        if (sEnd.isEmpty()) sEnd = "0";
                        // конструируем float
                        String strFloat = sBegin + "." + sEnd;
                        return strFloat;
                    }
                    else // Если запятая есть (раньше точки)
                    {
                        // Точка не на нулевой позиции
                        // Если точка позже запятой
                        // Если запятая есть (раньше точки)
                        // Реагируем на запятую
                        String sBegin = dirtyText.substring(0,indexComma);
                        sBegin = sBegin.replaceAll("[\\D]", "");
                        if (sBegin.isEmpty()) sBegin = "0";
                        // извлекаем строку-число после разделителя
                        String sEnd = dirtyText.substring(indexComma);
                        sEnd = sEnd.replaceAll("[\\D]", "");
                        if (sEnd.isEmpty()) sEnd = "0";
                        // конструируем float
                        String strFloat = sBegin + "." + sEnd;
                        return strFloat;
                    }
                }
            }
        }
    }

}
