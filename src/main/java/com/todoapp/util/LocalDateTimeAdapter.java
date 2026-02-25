package com.todoapp.util;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Adapter para serialização/deserialização de LocalDateTime com Gson.
 * 
 * Por padrão, Gson não sabe como converter LocalDateTime para JSON.
 * Este adapter ensina o Gson a:
 * - Serializar: LocalDateTime → String ISO-8601
 * - Deserializar: String ISO-8601 → LocalDateTime
 * 
 * Formato usado: "2026-01-27T14:30:45.123"
 * 
 * @author Fábio Júnior
 * @version 1.0.0
*/
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, 
                                             JsonDeserializer<LocalDateTime> {
    
    // Formato ISO-8601 (padrão internacional)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Converte LocalDateTime para JSON (String)
     * 
     * Exemplo:
     * LocalDateTime.of(2026, 1, 27, 14, 30) 
     * → "2026-01-27T14:30:00"
     * 
     * @param src LocalDateTime a serializar
     * @param typeOfSrc tipo do objeto (não usado)
     * @param context contexto de serialização
     * @return JsonElement contendo a string formatada
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, 
                                 JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;  // null em JSON
        }
        return new JsonPrimitive(src.format(FORMATTER));
    }
    
    /**
     * Converte JSON (String) para LocalDateTime
     * 
     * Exemplo:
     * "2026-01-27T14:30:00" 
     * → LocalDateTime.of(2026, 1, 27, 14, 30, 0)
     * 
     * @param json elemento JSON contendo a string
     * @param typeOfT tipo esperado (LocalDateTime)
     * @param context contexto de deserialização
     * @return LocalDateTime parseado
     * @throws JsonParseException se string inválida
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, 
                                     JsonDeserializationContext context) 
                                     throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        
        try {
            String dateStr = json.getAsString();
            return LocalDateTime.parse(dateStr, FORMATTER);
            
        } catch (Exception e) {
            throw new JsonParseException("Erro ao parsear data: " + json.getAsString(), e);
        }
    }
}