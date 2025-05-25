package main;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import javax.sound.sampled.*;

public class GameUI {

    // Dimensions
    private final int boardWidth = 1500;
    private final int boardHeight = 750;

    // GUI Components
    private final JFrame frame = new JFrame("Mario: Whac-A-Mole");
    private final ScorePanel scorePanel = new ScorePanel();
    private final JPanel boardPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();
    private final JButton restartButton = new JButton("Restart");
    private final JButton[] moleButtons = new JButton[9];

    // Game State
    private int score = 0;
    private int montyIndex = -1;
    private int piranhaIndex = -1;
    private int heartIndex = -1;
    private int timeLeft = 30;
    private boolean gameOver = false;
    private boolean speedIncreased = false;
    private boolean hasExtraLife = false;

    // Timers
    private Timer montyTimer;
    private Timer piranhaTimer;
    private Timer gameTimer;
    private Timer countdownTimer;
    private Timer heartTimer;

    // Icons
    private final ImageIcon montyIcon;
    private final ImageIcon piranhaIcon;
    private final ImageIcon heartIcon;

    public GameUI() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setCursor(new Cursor(Cursor.HAND_CURSOR));
        frame.getContentPane().setBackground(Color.WHITE);

        // Score Panel
        scorePanel.setPreferredSize(new Dimension(boardWidth, 60));
        frame.add(scorePanel, BorderLayout.NORTH);

        // Board Panel
        boardPanel.setLayout(new GridLayout(3, 3));
        frame.add(boardPanel, BorderLayout.CENTER);

        // Bottom Panel
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(restartButton, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Load Icons
        int iconSize = 200;
        montyIcon = loadIcon("/images/monty.png", iconSize);
        piranhaIcon = loadIcon("/images/piranha.png", iconSize);
        heartIcon = loadIcon("/images/—Pngtree—beautiful red heart love_20419930.png", iconSize);

        // Create Buttons
        createMoleButtons();

        // Restart Button
        restartButton.addActionListener(e -> restartGame());

        // Start Game
        startGame();
        playSound("mixkit-martial-arts-fast-punch-2047.wav");
        frame.setVisible(true);
    }

    private void createMoleButtons() {
        for (int i = 0; i < 9; i++) {
            moleButtons[i] = new JButton();
            moleButtons[i].setFocusPainted(false);
            moleButtons[i].setContentAreaFilled(true);
            moleButtons[i].setOpaque(true);
            moleButtons[i].setBackground(Color.LIGHT_GRAY);
            boardPanel.add(moleButtons[i]);

            int index = i;
            moleButtons[i].addActionListener(e -> handleButtonClick(index));
        }
    }

    private void handleButtonClick(int index) {
        if (gameOver) return;

        if (index == heartIndex) {
            hasExtraLife = true;
           // playSound("Extra-life-sound-effect.mp3");
            flashButton(moleButtons[index], Color.PINK);
            moleButtons[heartIndex].setIcon(null);
            heartIndex = -1;
        } else if (index == montyIndex) {
            score += 10;
            playSound("mixkit-achievement-bell-600.wav");
            flashButton(moleButtons[index], Color.GREEN);
            scorePanel.repaint();
            checkSpeedIncrease();
        } else if (index == piranhaIndex) {
            if (hasExtraLife) {
                hasExtraLife = false;
                flashButton(moleButtons[index], Color.ORANGE);
                //playSound("Extra-life-sound-effect.mp3");
                moleButtons[piranhaIndex].setIcon(null);
                piranhaIndex = -1;
            } else {
                playSound("mixkit-sad-game-over-trombone-471.wav");
                flashButton(moleButtons[index], Color.RED);
                endGame("Game Over! You clicked the plant.\nFinal Score: " + score);
            }
        } else {
            score -= 5;
            playSound("mixkit-wrong-answer-bass-buzzer-948.wav");
            flashButton(moleButtons[index], Color.LIGHT_GRAY);
            scorePanel.repaint();
            checkSpeedIncrease();
        }
    }

    private void startGame() {
        score = 0;
        timeLeft = 60;
        gameOver = false;
        speedIncreased = false;
        hasExtraLife = false;

        scorePanel.repaint();
        clearIcons();

        startMontyMovement();
        startPiranhaMovement();
        startHeartPowerUp();
        startCountdownTimer();
        startGameTimer();
    }

    private void restartGame() {
        stopAllTimers();
        startGame();
        playSound("mixkit-martial-arts-fast-punch-2047.wav");
    }

    private void stopAllTimers() {
        if (montyTimer != null) montyTimer.stop();
        if (piranhaTimer != null) piranhaTimer.stop();
        if (gameTimer != null) gameTimer.stop();
        if (countdownTimer != null) countdownTimer.stop();
        if (heartTimer != null) heartTimer.stop();
    }

    private void startMontyMovement() {
        montyTimer = new Timer(1500, e -> {
            if (gameOver) return;
            updateCharacterPosition(montyIndex, montyIcon, true);
        });
        montyTimer.start();
    }

    private void startPiranhaMovement() {
        piranhaTimer = new Timer(2000, e -> {
            if (gameOver) return;
            updateCharacterPosition(piranhaIndex, piranhaIcon, false);
        });
        piranhaTimer.start();
    }

    private void updateCharacterPosition(int currentIndex, ImageIcon icon, boolean isMonty) {
        Random rand = new Random();
        int newIndex;
        do {
            newIndex = rand.nextInt(9);
        } while (newIndex == (isMonty ? piranhaIndex : montyIndex));

        if (currentIndex != -1) {
            moleButtons[currentIndex].setIcon(null);
        }

        if (isMonty) {
            montyIndex = newIndex;
            moleButtons[montyIndex].setIcon(icon);
        } else {
            piranhaIndex = newIndex;
            moleButtons[piranhaIndex].setIcon(icon);
        }
    }

    private void checkSpeedIncrease() {
        if (speedIncreased || score < 50) return;

        speedIncreased = true;
        stopAllTimers();

        montyTimer = new Timer(1000, e -> updateCharacterPosition(montyIndex, montyIcon, true));
        montyTimer.start();

        piranhaTimer = new Timer(1200, e -> updateCharacterPosition(piranhaIndex, piranhaIcon, false));
        piranhaTimer.start();

        flashAllButtons(Color.YELLOW);
        playSound("mixkit-game-level-completed-2059.wav");
        startHeartPowerUp();
        startCountdownTimer();
        startGameTimer();
    }

    private void startHeartPowerUp() {
        heartTimer = new Timer(10000, e -> {
            if (gameOver || hasExtraLife) return;

            Random rand = new Random();
            int newIndex;
            do {
                newIndex = rand.nextInt(9);
            } while (newIndex == montyIndex || newIndex == piranhaIndex);

            heartIndex = newIndex;
            moleButtons[heartIndex].setIcon(heartIcon);
        });
        heartTimer.start();
    }

    private void startCountdownTimer() {
        countdownTimer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                scorePanel.repaint();
            }
        });
        countdownTimer.start();
    }

    private void startGameTimer() {
        gameTimer = new Timer(60000, e -> {
            if (score >= 100) {
                playSound("mixkit-video-game-win-2016.wav");
                endGame("Congratulations! You won with score: " + score);
            } else {
                playSound("mixkit-wrong-answer-bass-buzzer-948.wav");
                endGame("Time's up! You lost with score less than 100: " + score);
            }
        });
        gameTimer.setRepeats(false);
        gameTimer.start();
    }

    private void endGame(String message) {
        gameOver = true;
        stopAllTimers();
        JOptionPane.showMessageDialog(frame, message);
    }

    private void clearIcons() {
        for (JButton btn : moleButtons) {
            btn.setIcon(null);
        }
        montyIndex = -1;
        piranhaIndex = -1;
        heartIndex = -1;
    }

    private void flashButton(JButton button, Color color) {
        Color original = button.getBackground();
        button.setBackground(color);
        Timer timer = new Timer(300, e -> button.setBackground(original));
        timer.setRepeats(false);
        timer.start();
    }

    private void flashAllButtons(Color flashColor) {
        for (JButton btn : moleButtons) {
            btn.setBackground(flashColor);
        }

        Timer restoreTimer = new Timer(400, e -> {
            for (JButton btn : moleButtons) {
                btn.setBackground(Color.LIGHT_GRAY);
            }
        });
        restoreTimer.setRepeats(false);
        restoreTimer.start();
    }

    private ImageIcon loadIcon(String path, int size) {
        return new ImageIcon(
                new ImageIcon(getClass().getResource(path))
                        .getImage()
                        .getScaledInstance(size, size, Image.SCALE_SMOOTH)
        );
    }

    private void playSound(String soundFileName) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(
                    getClass().getResource("/sounds/" + soundFileName)
            );
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Score Panel Inner Class
    class ScorePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 25));
            g.drawString("Score: " + score, 200, 40);
            g.drawString("Time: " + timeLeft, 1100, 40);
        }
    }
}

















