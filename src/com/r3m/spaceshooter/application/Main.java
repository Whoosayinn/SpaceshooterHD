package com.r3m.spaceshooter.application;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMainMenu();
            System.out.print("Choose an option (1-4): ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    System.out.println("\nLaunching Space Shooter HD...");
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
        System.out.println();
        System.out.println("================================================================================================");
        System.out.println(" ____  ____   _    ____ _____     ____  _   _  ___   ___ _____ _____ ____    _   _ ____  ");
        System.out.println("/ ___||  _ \\ / \\  / ___| ____|   / ___|| | | |/ _ \\ / _ \\_   _| ____|  _ \\  | | | |  _ \\ ");
        System.out.println("\\___ \\| |_) / _ \\| |   |  _|     \\___ \\| |_| | | | | | | || | |  _| | |_) | | |_| | | | |");
        System.out.println(" ___) |  __/ ___ \\ |___| |___     ___) |  _  | |_| | |_| || | | |___|  _ <  |  _  | |_| |");
        System.out.println("|____/|_| /_/   \\_\\____|_____|   |____/|_| |_|\\___/ \\___/ |_| |_____|_| \\_\\ |_| |_|____/ ");
        System.out.println("================================================================================================");
        System.out.println();
        System.out.println("                              [1] START GAME");
        System.out.println("                              [2] INSTRUCTIONS");
        System.out.println("                              [3] VIEW CONTROLS");
        System.out.println("                              [4] EXIT");
        System.out.println();
        System.out.println("================================================================================================");
    }

    private static void printInstructions() {
        System.out.println();
        System.out.println("============================== INSTRUCTIONS ==============================");
        System.out.println("Pilot your spaceship through the asteroid field.");
        System.out.println("Avoid incoming asteroids and survive for as long as possible.");
        System.out.println("Every collision costs one life. You start with three lives.");
        System.out.println("=========================================================================");
    }

    private static void printControls() {
        System.out.println();
        System.out.println("================================ CONTROLS ================================");
        System.out.println("W     - Move up");
        System.out.println("A     - Move left");
        System.out.println("S     - Move down");
        System.out.println("D     - Move right");
        System.out.println("ESC   - Exit game");
        System.out.println("=========================================================================");
    }

    private static void waitForEnter(Scanner scanner) {
        System.out.print("\nPress Enter to return to the main menu...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        System.out.println();
    }

    private static void startGame() {
        GameFrame window = new GameFrame();
        window.setVisible(true);
        window.startGame();
    }
}
