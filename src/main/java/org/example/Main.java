import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ConsoleFileManager {
    private static Path currentDirectory = Paths.get(System.getProperty("user.dir"));
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command;

        do {
            System.out.print(currentDirectory + "> ");
            command = scanner.nextLine().trim();

            String[] tokens = command.split("\\s+");
            String keyword = tokens[0].toLowerCase();

            try {
                switch (keyword) {
                    case "ls":
                        listFiles(tokens);
                        break;
                    case "cd":
                        changeDirectory(tokens);
                        break;
                    case "mkdir":
                        createDirectory(tokens);
                        break;
                    case "rm":
                        deleteFile(tokens);
                        break;
                    case "mv":
                        moveFile(tokens);
                        break;
                    case "cp":
                        copyFile(tokens);
                        break;
                    case "finfo":
                        fileInfo(tokens);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "find":
                        findFile(tokens);
                        break;
                    case "exit":
                        System.out.println("Программа завершена.");
                        break;
                    default:
                        System.out.println("Неверная команда. Введите 'help' для списка команд.");
                }
            } catch (IOException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }

        } while (!command.equalsIgnoreCase("exit"));

        scanner.close();
    }

    private static void listFiles(String[] tokens) throws IOException {
        boolean detailed = false;

        if (tokens.length > 1 && tokens[1].equals("-l")) {
            detailed = true;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDirectory)) {
            for (Path entry : stream) {
                if (detailed) {
                    displayFileDetails(entry);
                } else {
                    System.out.println(entry.getFileName());
                }
            }
        }
    }

    private static void displayFileDetails(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        String formattedDateTime = dateFormat.format(new Date(attributes.lastModifiedTime().toMillis()));

        System.out.printf("%-30s%-15s%s\n", path.getFileName(), attributes.size(), formattedDateTime);
    }

    private static void changeDirectory(String[] tokens) throws IOException {
        if (tokens.length < 2) {
            System.out.println("Не указана директория для перехода.");
            return;
        }

        String target = tokens[1];
        if (target.equals("..")) {
            currentDirectory = currentDirectory.getParent();
        } else {
            Path newDir = currentDirectory.resolve(target);
            if (Files.isDirectory(newDir)) {
                currentDirectory = newDir;
            } else {
                System.out.println("Указанный путь не является директорией.");
            }
        }
    }

    private static void createDirectory(String[] tokens) throws IOException {
        if (tokens.length < 2) {
            System.out.println("Не указано имя для новой директории.");
            return;
        }

        String newDirName = tokens[1];
        Path newDir = currentDirectory.resolve(newDirName);
        Files.createDirectory(newDir);
    }

    private static void deleteFile(String[] tokens) throws IOException {
        if (tokens.length < 2) {
            System.out.println("Не указан файл или директория для удаления.");
            return;
        }

        String target = tokens[1];
        Path fileToDelete = currentDirectory.resolve(target);
        if (Files.exists(fileToDelete)) {
            if (Files.isDirectory(fileToDelete)) {
                Files.walkFileTree(fileToDelete, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(fileToDelete);
            }
        } else {
            System.out.println("Указанный файл или директория не существует.");
        }
    }

    private static void moveFile(String[] tokens) throws IOException {
        if (tokens.length < 3) {
            System.out.println("Не указан исходный или целевой путь.");
            return;
        }

        String sourcePath = tokens[1];
        String destinationPath = tokens[2];

        Path source = currentDirectory.resolve(sourcePath);
        Path destination = currentDirectory.resolve(destinationPath);

        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void copyFile(String[] tokens) throws IOException {
        if (tokens.length < 3) {
            System.out.println("Не указан исходный или целевой путь.");
            return;
        }

        String sourcePath = tokens[1];
        String destinationPath = tokens[2];

        Path source = currentDirectory.resolve(sourcePath);
        Path destination = currentDirectory.resolve(destinationPath);

        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void fileInfo(String[] tokens) throws IOException {
        if (tokens.length < 2) {
            System.out.println("Не указан файл для получения информации.");
            return;
        }

        String target = tokens[1];
        Path file = currentDirectory.resolve(target);

        if (Files.exists(file)) {
            displayFileDetails(file);
        } else {
            System.out.println("Указанный файл не существует.");
        }
    }

    private static void printHelp() {
        System.out.println("Список команд:");
        System.out.println("ls [-l] - отобразить список файлов в текущей директории");
        System.out.println("cd [путь] - изменить текущую директорию");
        System.out.println("mkdir [имя] - создать новую директорию");
        System.out.println("rm [имя] - удалить файл или директорию");
        System.out.println("mv [исходный] [целевой] - переместить/переименовать файл или директорию");
        System.out.println("cp [исходный] [целевой] - скопировать файл");
        System.out.println("finfo [имя] - получить информацию о файле");
        System.out.println("find [имя] - найти файл с указанным именем в текущей директории и поддиректориях");
        System.out.println("help - отобразить список команд");
        System.out.println("exit - завершить работу файлового менеджера");
    }

    private static void findFile(String[] tokens) throws IOException {
        if (tokens.length < 2) {
            System.out.println("Не указано имя файла для поиска.");
            return;
        }

        String fileName = tokens[1];

        Files.walkFileTree(currentDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals(fileName)) {
                    System.out.println(file.toAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
