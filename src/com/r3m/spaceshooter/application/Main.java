package com.r3m.spaceshooter.application;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMainMenu();
            System.out.print("Choose an option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("\nLaunching Space Shooter HD...\n");
                    startGame();
                    return;
                }
                case "2" -> {
                    printInstructions();
                    waitForEnter(scanner);
                }
                case "3" -> {
                    printControls();
                    waitForEnter(scanner);
                }
                case "4" -> {
                    System.out.println("\nThanks for playing Space Shooter HD!");
                    return;
                }
                default -> {
                    System.out.println("\nInvalid input. Please enter a number from 1 to 4.");
                    waitForEnter(scanner);
                }
            }
        }
    }

    private static void printMainMenu() {
        clearConsoleSpacing();
        System.out.println("+==================================================+");
        System.out.println("|                                                  |");
        System.out.println("|    ____  ____   _    ____ _____                  |");
        System.out.println("|   / ___||  _ \\ / \\  / ___| ____|                 |");
        System.out.println("|   \\___ \\| |_) / _ \\| |   |  _|                   |");
        System.out.println("|    ___) |  __/ ___ \\ |___| |___                  |");
        System.out.println("|   |____/|_| /_/   \\_\\____|_____|                 |");
        System.out.println("|                                                  |");
        System.out.println("|             S H O O T E R   H D                  |");
        System.out.println("|                                                  |");
        System.out.println("+==================================================+");
        System.out.println("|                                                  |");
        System.out.println("|               [1] START GAME                     |");
        System.out.println("|               [2] INSTRUCTIONS                   |");
        System.out.println("|               [3] VIEW CONTROLS                  |");
        System.out.println("|               [4] EXIT                           |");
        System.out.println("|                                                  |");
        System.out.println("+==================================================+");
        System.out.println();
    }

    private static void printInstructions() {
        clearConsoleSpacing();
        System.out.println("+==================== INSTRUCTIONS ====================+");
        System.out.println("|                                                      |");
        System.out.println("| Pilot your spaceship through the asteroid field.     |");
        System.out.println("| Avoid incoming asteroids and survive for as long     |");
        System.out.println("| as possible. You have three lives.                   |");
        System.out.println("|                                                      |");
        System.out.println("+======================================================+");
    }

    private static void printControls() {
        clearConsoleSpacing();
        System.out.println("+====================== CONTROLS ======================+");
        System.out.println("|                                                     |");
        System.out.println("| W  - Move up                                        |");
        System.out.println("| A  - Move left                                      |");
        System.out.println("| S  - Move down                                      |");
        System.out.println("| D  - Move right                                     |");
        System.out.println("| ESC - Exit game                                     |");
        System.out.println("|                                                     |");
        System.out.println("+=====================================================+");
    }

    private static void waitForEnter(Scanner scanner) {
        System.out.print("\nPress Enter to return to the main menu...");
        scanner.nextLine();
    }

    private static void clearConsoleSpacing() {
        System.out.println("\n".repeat(3));
    }

    private static void startGame() {
        GameFrame window = new GameFrame();
        window.setVisible(true);
        window.startGame();
    }
}
