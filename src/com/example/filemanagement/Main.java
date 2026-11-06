package com.example.filemanagement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("TP 12 : Gestion des fichiers en Java");
        System.out.println("Environnement prêt pour les tests.\n");

        prepareData();

        // --- Lecture avec BufferedReader ---
        titre("1. Lecture avec BufferedReader");
        readFileWithBufferedReader("data/employees.txt");
        System.out.println("-- Test fichier inexistant --");
        readFileWithBufferedReader("data/inexistant.txt");

        // --- Écriture avec BufferedWriter ---
        titre("2. Écriture avec BufferedWriter");
        writeFileWithBufferedWriter("data/output.txt", "Contenu de test");
        readFileWithBufferedReader("data/output.txt");

        // --- PrintWriter ---
        titre("3. Écriture avec PrintWriter");
        writeWithPrintWriter("data/report.txt");
        readFileWithBufferedReader("data/report.txt");

        // --- API Files ---
        titre("4. API java.nio.file.Files");
        readWithFilesApi("data/employees.txt");
        writeWithFilesApi("data/nio_output.txt", "Écrit avec l'API Files");
        readWithFilesApi("data/nio_output.txt");

        // --- Copie / déplacement ---
        titre("5. Copie et déplacement");
        copyFile("data/employees.txt", "data/employees_backup.txt");
        moveFile("data/nio_output.txt", "data/moved_output.txt");

        // --- Suppression ---
        titre("6. Suppression");
        deleteFile("data/moved_output.txt");
        deleteFile("data/fichier_qui_nexiste_pas.txt");

        // --- Binaire : lecture ---
        titre("7. Lecture binaire");
        readBinaryFile("data/test.jpg");

        // --- Binaire : écriture (copie des octets de test.jpg) ---
        titre("8. Écriture binaire");
        try {
            byte[] data = Files.readAllBytes(Paths.get("data/test.jpg"));
            writeBinaryFile("data/copy_binary.bin", data);
        } catch (IOException e) {
            System.err.println("Impossible de lire test.jpg : " + e.getMessage());
        }

        // --- Métadonnées ---
        titre("9. Métadonnées");
        getFileMetadata("data/employees.txt");
    }

    // ------------------------------------------------------------------
    // Préparation de l'environnement de test
    // ------------------------------------------------------------------
    private static void prepareData() {
        try {
            Files.createDirectories(Paths.get("data"));

            Path employees = Paths.get("data/employees.txt");
            if (!Files.exists(employees)) {
                String contenu = "Alice,25,Engineer\nBob,30,Manager\nCharlie,28,Designer\n";
                Files.write(employees, contenu.getBytes(StandardCharsets.UTF_8));
            }

            // Si aucune image n'est fournie, on génère un fichier binaire de test
            Path image = Paths.get("data/test.jpg");
            if (!Files.exists(image)) {
                byte[] fake = new byte[2048];
                for (int i = 0; i < fake.length; i++) {
                    fake[i] = (byte) (i % 256);
                }
                Files.write(image, fake);
                System.out.println("(data/test.jpg absent : fichier binaire de test généré)");
            }
        } catch (IOException e) {
            System.err.println("Erreur de préparation : " + e.getMessage());
        }
    }

    private static void titre(String texte) {
        System.out.println("\n===== " + texte + " =====");
    }

    // ------------------------------------------------------------------
    // Lecture : FileReader + BufferedReader
    // ------------------------------------------------------------------
    public static void readFileWithBufferedReader(String filePath) {
        try (BufferedReader br = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) { // Java 11+
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Écriture : FileWriter + BufferedWriter
    // ------------------------------------------------------------------
    public static void writeFileWithBufferedWriter(String filePath, String content) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(filePath, StandardCharsets.UTF_8))) {
            bw.write(content);
            bw.newLine();
            bw.write("Ligne ajoutée avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Écriture formatée : PrintWriter
    // ------------------------------------------------------------------
    public static void writeWithPrintWriter(String filePath) {
        try (PrintWriter pw = new PrintWriter(
                new FileOutputStream(filePath), false, StandardCharsets.UTF_8)) { // Java 10+
            pw.println("=== Rapport des employés ===");
            pw.printf("Nom: %s, Âge: %d, Poste: %s%n", "Alice", 25, "Engineer");
            pw.printf("Nom: %s, Âge: %d, Poste: %s%n", "Bob", 30, "Manager");
            pw.println("Fin du rapport.");

            // PrintWriter n'émet pas d'IOException : on vérifie l'état du flux
            if (pw.checkError()) {
                System.err.println("Une erreur s'est produite pendant l'écriture !");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // API java.nio.file.Files
    // ------------------------------------------------------------------
    public static void readWithFilesApi(String filePath) {
        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture : " + e.getMessage());
        }
    }

    public static void writeWithFilesApi(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Fichier écrit avec succès : " + filePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Copie et déplacement
    // ------------------------------------------------------------------
    public static void copyFile(String source, String destination) {
        try {
            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(destination);
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier copié de " + source + " vers " + destination);
        } catch (IOException e) {
            System.err.println("Erreur lors de la copie : " + e.getMessage());
        }
    }

    public static void moveFile(String source, String destination) {
        try {
            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(destination);
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Fichier déplacé de " + source + " vers " + destination);
        } catch (IOException e) {
            System.err.println("Erreur lors du déplacement : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Suppression
    // ------------------------------------------------------------------
    public static void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("Fichier supprimé : " + filePath);
            } else {
                System.out.println("Le fichier n'existe pas : " + filePath);
            }
        } catch (NoSuchFileException e) {
            System.err.println("Le fichier a été supprimé entre-temps : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Fichiers binaires
    // ------------------------------------------------------------------
    public static void readBinaryFile(String filePath) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = bis.read(buffer)) != -1) {
                totalBytes += bytesRead;
            }
            System.out.println("Fichier binaire lu : " + filePath);
            System.out.println("Taille totale : " + totalBytes + " octets");
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture binaire : " + e.getMessage());
        }
    }

    public static void writeBinaryFile(String filePath, byte[] data) {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            bos.write(data);
            bos.flush();
            System.out.println("Fichier binaire écrit : " + filePath);
            System.out.println("Taille écrite : " + data.length + " octets");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture binaire : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Métadonnées
    // ------------------------------------------------------------------
    public static void getFileMetadata(String filePath) {
        try {
            Path path = Paths.get(filePath);
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            System.out.println("=== Métadonnées du fichier : " + filePath + " ===");
            System.out.println("Taille : " + attrs.size() + " octets");
            System.out.println("Dernière modification : " + attrs.lastModifiedTime().toInstant());
            System.out.println("Est un répertoire : " + attrs.isDirectory());
            System.out.println("Est un fichier : " + attrs.isRegularFile());
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des métadonnées : " + e.getMessage());
        }
    }
}
