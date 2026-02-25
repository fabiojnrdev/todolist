package com.todoapp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitário para operações com arquivos.
 * 
 * Fornece métodos convenientes para:
 * - Criar diretórios
 * - Verificar existência de arquivos
 * - Criar backups
 * - Limpar arquivos temporários
 * - Validar permissões
 * 
 * @author Fábio Júnior
 * @version 1.0.0
 */
public class FileHandler {
    
    // === CONSTANTES ===
    
    /**
     * Extensão padrão para backups
     */
    private static final String BACKUP_EXTENSION = ".bak";
    
    /**
     * Formato de timestamp para backups
     */
    private static final DateTimeFormatter BACKUP_TIMESTAMP = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    // === CONSTRUTORES ===
    
    /**
     * Construtor privado - classe utilitária (só métodos estáticos)
     */
    private FileHandler() {
        throw new UnsupportedOperationException("Classe utilitária - não instanciar");
    }
    
    // === MÉTODOS DE DIRETÓRIO ===
    
    /**
     * Garante que um diretório existe, criando se necessário
     * 
     * @param dirPath caminho do diretório
     * @return true se diretório existe ou foi criado
     * @throws IOException se erro ao criar diretório
     */
    public static boolean ensureDirectoryExists(String dirPath) throws IOException {
        if (dirPath == null || dirPath.isBlank()) {
            throw new IllegalArgumentException("Caminho do diretório não pode ser vazio");
        }
        
        File dir = new File(dirPath);
        
        // Já existe
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return true;
            } else {
                throw new IOException("Caminho existe mas não é um diretório: " + dirPath);
            }
        }
        
        // Criar diretório (e pais se necessário)
        boolean created = dir.mkdirs();
        if (!created) {
            throw new IOException("Falha ao criar diretório: " + dirPath);
        }
        
        return true;
    }
    
    /**
     * Garante que o diretório pai de um arquivo existe
     * 
     * @param filePath caminho do arquivo
     * @return true se diretório pai existe ou foi criado
     * @throws IOException se erro ao criar diretório
     */
    public static boolean ensureParentDirectoryExists(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo não pode ser vazio");
        }
        
        File file = new File(filePath);
        File parent = file.getParentFile();
        
        if (parent != null) {
            return ensureDirectoryExists(parent.getAbsolutePath());
        }
        
        return true;
    }
    
    // === MÉTODOS DE VERIFICAÇÃO ===
    
    /**
     * Verifica se arquivo existe
     * 
     * @param filePath caminho do arquivo
     * @return true se arquivo existe
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        return new File(filePath).exists();
    }
    
    /**
     * Verifica se diretório existe
     * 
     * @param dirPath caminho do diretório
     * @return true se diretório existe
     */
    public static boolean directoryExists(String dirPath) {
        if (dirPath == null || dirPath.isBlank()) {
            return false;
        }
        File dir = new File(dirPath);
        return dir.exists() && dir.isDirectory();
    }
    
    /**
     * Verifica se arquivo é gravável
     * 
     * @param filePath caminho do arquivo
     * @return true se arquivo pode ser escrito
     */
    public static boolean isWritable(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        
        File file = new File(filePath);
        
        // Se não existe, verifica se diretório pai é gravável
        if (!file.exists()) {
            File parent = file.getParentFile();
            return parent != null && parent.canWrite();
        }
        
        return file.canWrite();
    }
    
    /**
     * Verifica se arquivo é legível
     * 
     * @param filePath caminho do arquivo
     * @return true se arquivo pode ser lido
     */
    public static boolean isReadable(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.canRead();
    }
    
    // === MÉTODOS DE BACKUP ===
    
    /**
     * Cria backup de um arquivo
     * 
     * Exemplo:
     * - Arquivo: tasks.json
     * - Backup: tasks_20260225_143000.json.bak
     * 
     * @param filePath caminho do arquivo original
     * @return caminho do arquivo de backup criado
     * @throws IOException se erro ao criar backup
     */
    public static String createBackup(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo não pode ser vazio");
        }
        
        File original = new File(filePath);
        
        if (!original.exists()) {
            throw new IOException("Arquivo não existe: " + filePath);
        }
        
        if (!original.isFile()) {
            throw new IOException("Caminho não é um arquivo: " + filePath);
        }
        
        // Gerar nome do backup
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP);
        String backupPath = filePath + "_" + timestamp + BACKUP_EXTENSION;
        
        // Copiar arquivo
        Path source = Paths.get(filePath);
        Path target = Paths.get(backupPath);
        
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        
        return backupPath;
    }
    
    /**
     * Cria backup com nome customizado
     * 
     * @param filePath arquivo original
     * @param backupName nome do backup (sem extensão)
     * @return caminho do backup criado
     * @throws IOException se erro ao criar backup
     */
    public static String createBackupWithName(String filePath, String backupName) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Caminho do arquivo não pode ser vazio");
        }
        
        if (backupName == null || backupName.isBlank()) {
            throw new IllegalArgumentException("Nome do backup não pode ser vazio");
        }
        
        File original = new File(filePath);
        
        if (!original.exists()) {
            throw new IOException("Arquivo não existe: " + filePath);
        }
        
        File parent = original.getParentFile();
        String backupPath = parent != null 
            ? parent.getAbsolutePath() + File.separator + backupName + BACKUP_EXTENSION
            : backupName + BACKUP_EXTENSION;
        
        Path source = Paths.get(filePath);
        Path target = Paths.get(backupPath);
        
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        
        return backupPath;
    }
    
    /**
     * Restaura arquivo de um backup
     * 
     * @param backupPath caminho do backup
     * @param targetPath caminho do arquivo a restaurar
     * @throws IOException se erro ao restaurar
     */
    public static void restoreFromBackup(String backupPath, String targetPath) throws IOException {
        if (backupPath == null || backupPath.isBlank()) {
            throw new IllegalArgumentException("Caminho do backup não pode ser vazio");
        }
        
        if (targetPath == null || targetPath.isBlank()) {
            throw new IllegalArgumentException("Caminho do destino não pode ser vazio");
        }
        
        File backup = new File(backupPath);
        
        if (!backup.exists()) {
            throw new IOException("Backup não existe: " + backupPath);
        }
        
        Path source = Paths.get(backupPath);
        Path target = Paths.get(targetPath);
        
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
    
    // === MÉTODOS DE LIMPEZA ===
    
    /**
     * Deleta arquivo se existir
     * 
     * @param filePath caminho do arquivo
     * @return true se deletou ou não existia
     */
    public static boolean deleteFileIfExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        
        File file = new File(filePath);
        
        if (!file.exists()) {
            return true;  // Já não existe
        }
        
        return file.delete();
    }
    
    /**
     * Deleta todos os arquivos com extensão específica em um diretório
     * 
     * @param dirPath caminho do diretório
     * @param extension extensão dos arquivos (ex: ".bak")
     * @return quantidade de arquivos deletados
     */
    public static int deleteFilesByExtension(String dirPath, String extension) {
        if (dirPath == null || dirPath.isBlank() || extension == null) {
            return 0;
        }
        
        File dir = new File(dirPath);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(extension));
        
        if (files == null) {
            return 0;
        }
        
        int count = 0;
        for (File file : files) {
            if (file.delete()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Deleta arquivos de backup antigos (mais de X dias)
     * 
     * @param dirPath diretório onde estão os backups
     * @param daysOld idade mínima em dias para deletar
     * @return quantidade de backups deletados
     */
    public static int deleteOldBackups(String dirPath, int daysOld) {
        if (dirPath == null || dirPath.isBlank() || daysOld < 0) {
            return 0;
        }
        
        File dir = new File(dirPath);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);
        
        File[] backups = dir.listFiles((d, name) -> name.endsWith(BACKUP_EXTENSION));
        
        if (backups == null) {
            return 0;
        }
        
        int count = 0;
        for (File backup : backups) {
            if (backup.lastModified() < cutoffTime) {
                if (backup.delete()) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    // === MÉTODOS DE INFORMAÇÃO ===
    
    /**
     * Retorna tamanho do arquivo em bytes
     * 
     * @param filePath caminho do arquivo
     * @return tamanho em bytes, ou -1 se não existir
     */
    public static long getFileSize(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return -1;
        }
        
        File file = new File(filePath);
        
        if (!file.exists() || !file.isFile()) {
            return -1;
        }
        
        return file.length();
    }
    
    /**
     * Retorna tamanho do arquivo formatado (KB, MB, GB)
     * 
     * @param filePath caminho do arquivo
     * @return string formatada (ex: "1.5 MB")
     */
    public static String getFileSizeFormatted(String filePath) {
        long bytes = getFileSize(filePath);
        
        if (bytes < 0) {
            return "N/A";
        }
        
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }
        
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.2f MB", mb);
        }
        
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }
    
    /**
     * Retorna caminho absoluto de um arquivo
     * 
     * @param filePath caminho relativo ou absoluto
     * @return caminho absoluto
     */
    public static String getAbsolutePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        return new File(filePath).getAbsolutePath();
    }
}