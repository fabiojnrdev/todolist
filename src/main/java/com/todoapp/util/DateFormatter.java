package com.todoapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilitário para formatação de datas e horários.
 * 
 * Fornece métodos convenientes para formatar LocalDateTime
 * em diferentes formatos usados na aplicação.
 * 
 * Formatos suportados:
 * - Brasileiro: dd/MM/yyyy HH:mm
 * - Internacional: yyyy-MM-dd HH:mm:ss
 * - Curto: dd/MM/yyyy
 * - Relativo: "há 2 horas", "ontem", etc
 * 
 * @author Fábio Júnior
 * @version 1.0.1
 */
public class DateFormatter {
    
    // === FORMATADORES PRÉ-DEFINIDOS ===
    
    /**
     * Formato brasileiro completo: "25/02/2026 14:30"
     */
    private static final DateTimeFormatter BRAZILIAN_FULL = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Formato brasileiro curto: "25/02/2026"
     */
    private static final DateTimeFormatter BRAZILIAN_SHORT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Formato internacional: "2026-02-25 14:30:00"
     */
    private static final DateTimeFormatter INTERNATIONAL = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Formato para hora: "14:30"
     */
    private static final DateTimeFormatter TIME_ONLY = 
        DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Formato ISO (mesmo usado no JSON): "2026-02-25T14:30:00"
     */
    private static final DateTimeFormatter ISO = 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    // === CONSTRUTORES ===
    
    /**
     * Construtor privado - classe utilitária (só métodos estáticos)
     */
    private DateFormatter() {
        throw new UnsupportedOperationException("Classe utilitária - não instanciar");
    }
    
    // === MÉTODOS DE FORMATAÇÃO ===
    
    /**
     * Formata data no padrão brasileiro completo
     * 
     * @param dateTime data/hora a formatar
     * @return string no formato "25/02/2026 14:30"
     */
    public static String formatBrazilian(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(BRAZILIAN_FULL);
    }
    
    /**
     * Formata data no padrão brasileiro curto (sem hora)
     * 
     * @param dateTime data/hora a formatar
     * @return string no formato "25/02/2026"
     */
    public static String formatBrazilianShort(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(BRAZILIAN_SHORT);
    }
    
    /**
     * Formata data no padrão internacional
     * 
     * @param dateTime data/hora a formatar
     * @return string no formato "2026-02-25 14:30:00"
     */
    public static String formatInternational(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(INTERNATIONAL);
    }
    
    /**
     * Formata apenas a hora
     * 
     * @param dateTime data/hora a formatar
     * @return string no formato "14:30"
     */
    public static String formatTimeOnly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(TIME_ONLY);
    }
    
    /**
     * Formata no padrão ISO (usado no JSON)
     * 
     * @param dateTime data/hora a formatar
     * @return string no formato "2026-02-25T14:30:00"
     */
    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO);
    }
    
    /**
     * Formata data de forma relativa ao momento atual
     * 
     * Exemplos:
     * - "agora" (menos de 1 minuto)
     * - "há 5 minutos"
     * - "há 2 horas"
     * - "ontem às 14:30"
     * - "25/02/2026" (mais de 7 dias)
     * 
     * @param dateTime data/hora a formatar
     * @return string formatada relativamente
     */
    public static String formatRelative(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Futuro
        if (dateTime.isAfter(now)) {
            return "no futuro";
        }
        
        // Calcular diferenças
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        
        // Menos de 1 minuto
        if (minutes < 1) {
            return "agora";
        }
        
        // Menos de 1 hora
        if (minutes < 60) {
            return "há " + minutes + " minuto" + (minutes != 1 ? "s" : "");
        }
        
        // Menos de 24 horas
        if (hours < 24) {
            return "há " + hours + " hora" + (hours != 1 ? "s" : "");
        }
        
        // Ontem
        if (days == 1) {
            return "ontem às " + formatTimeOnly(dateTime);
        }
        
        // Menos de 7 dias
        if (days < 7) {
            return "há " + days + " dia" + (days != 1 ? "s" : "");
        }
        
        // Mais de 7 dias - mostra data completa
        return formatBrazilianShort(dateTime);
    }
    
    /**
     * Formata data com descrição amigável
     * 
     * Exemplos:
     * - "Hoje, 14:30"
     * - "Ontem, 09:15"
     * - "25/02/2026, 14:30"
     * 
     * @param dateTime data/hora a formatar
     * @return string formatada de forma amigável
     */
    public static String formatFriendly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());
        
        String timeStr = formatTimeOnly(dateTime);
        
        return switch ((int) daysDiff) {
            case 0 -> "Hoje, " + timeStr;
            case 1 -> "Ontem, " + timeStr;
            case -1 -> "Amanhã, " + timeStr;
            default -> formatBrazilianShort(dateTime) + ", " + timeStr;
        };
    }
    
    /**
     * Calcula tempo decorrido desde uma data
     * 
     * @param dateTime data inicial
     * @return string descrevendo o tempo decorrido
     */
    public static String timeSince(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now) % 24;
        long minutes = ChronoUnit.MINUTES.between(dateTime, now) % 60;
        
        if (days > 0) {
            return days + " dia(s), " + hours + " hora(s)";
        } else if (hours > 0) {
            return hours + " hora(s), " + minutes + " minuto(s)";
        } else {
            return minutes + " minuto(s)";
        }
    }
    
    /**
     * Parseia string no formato brasileiro para LocalDateTime
     * 
     * @param dateStr string no formato "25/02/2026 14:30"
     * @return LocalDateTime parseado
     * @throws java.time.format.DateTimeParseException se formato inválido
     */
    public static LocalDateTime parseBrazilian(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, BRAZILIAN_FULL);
    }
    
    /**
     * Parseia string no formato ISO para LocalDateTime
     * 
     * @param dateStr string no formato "2026-02-25T14:30:00"
     * @return LocalDateTime parseado
     * @throws java.time.format.DateTimeParseException se formato inválido
     */
    public static LocalDateTime parseISO(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, ISO);
    }
}