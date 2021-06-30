package de.lucavon.btbicon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton class. Creates a blinking tray icon and manages it. The tray icon alternates between two generated
 * images.
 */
public class BlinkingTrayIcon {

    private static BlinkingTrayIcon INSTANCE = null;

    /**
     * The dimensions of the tray icon's image. The dimensions are the same for height and width. Feel free to adjust
     * this if it is necessary.
     */
    private static int IMAGE_DIMENSION = 64;

    /**
     * Contains the colors used to create the two images for the tray icon. RGB. Feel free to adjust this.
     */
    private static final Color[] IMAGE_COLORS = new Color[] {
            new Color(0, 0, 0), // Black
            new Color(250, 250, 250)  // Looks black, but is a very dark gray
    };

    /**
     * Contains the images that are shown in the tray icon.
     */
    private static final Image[] IMAGES = new Image[2];

    /**
     * The amount of milliseconds between image updates. Feel free to adjust this. The L in the end is necessary.
     */
    private static final Long MILLISECOND_DELAY = 1000L;

    private TrayIcon trayIconInstance;
    private Timer timerInstance;

    /**
     * Used to switch between the image shown.
     */
    private boolean firstImage = true;

    /**
     * Whether the program should exit on the next tick.
     */
    private boolean exit = false;

    /**
     * Private constructor to ensure this class cannot be instantiated from any other places than itself.
     * The getInstance() method follows the Singleton pattern, meaning it instantiates an instance of this class
     * one single time.
     */
    private BlinkingTrayIcon() {}

    /**
     * Prepares and starts the blinking tray icon.
     */
    public void start() {
        checkIfSupported();
        createImages();
        trayIconInstance = createTrayIcon();
        addIconToTray();
        startTimer();
    }

    /**
     * Checks whether the task bar is supported and attempts to shows an error message if this is not the case.
     */
    private void checkIfSupported() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "The system tray is not supported on this system.",
                    "Failed to add tray icon", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("The system tray is not supported on this system.");
        }
    }

    /**
     * Creates the two images by filling blank images with the colors defined in {@link #IMAGE_COLORS}.
     */
    private void createImages() {
        // For convenience, store max dimension in a variable. Because coordinates go from 0 to X, the dimension - 1 is
        // the "highest" reachable pixel within the bounds of this image.
        final int maxDimension = IMAGE_DIMENSION - 1;
        // IMAGES array has two slots -> create two images.
        for (int i = 0; i < 2; i++) {
            IMAGES[i] = new BufferedImage(IMAGE_DIMENSION, IMAGE_DIMENSION, BufferedImage.TYPE_INT_RGB);
            final Graphics g = IMAGES[i].getGraphics();
            g.setColor(IMAGE_COLORS[i]);
            // Fill the entire image with a single color.
            g.fillRect(0, 0, maxDimension, maxDimension);
            // Free up system resources.
            g.dispose();
        }
    }

    /**
     * Instantiates a TrayIcon with {@link #createAndPopulatePopupMenu()}'s result as the popup menu.
     * The standard image is the first entry in the {@link #IMAGES} array.
     *
     * @return  The newly instantiated TrayIcon.
     */
    private TrayIcon createTrayIcon() {
        return new TrayIcon(IMAGES[0], "Blinking Tray Icon", createAndPopulatePopupMenu());
    }

    /**
     * Instantiates a PopupMenu and adds a MenuItem with an ActionListener that sets {@link #exit} to true if triggered.
     * This method constructs a popup menu with a single item - Exit.
     *
     * @return  A newly instantiated PopupMenu with an Exit item.
     */
    private PopupMenu createAndPopulatePopupMenu() {
        final PopupMenu popupMenu = new PopupMenu();
        final MenuItem exitItem = new MenuItem("Exit");
        popupMenu.add(exitItem);
        // When the exit option is clicked, exit is set to true. This means that the program will end next time
        // the image change happens (once a second).
        popupMenu.addActionListener(actionEvent -> exit = true);
        return popupMenu;
    }

    /**
     * Attempts to add the icon to the system tray or shows an error message if applicable.
     */
    private void addIconToTray() {
        final SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIconInstance);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(null, "The tray icon could not be added to the system tray!",
                    "Failed to add tray icon", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("The tray icon could not be added to the system tray!");
        }
    }

    /**
     * Creates a timer that executes {@link #update()} every {@link #MILLISECOND_DELAY} ms.
     */
    private void startTimer() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };

        // false: The timer is not a daemon timer. This means the program will not exit if only this timer is left.
        timerInstance = new Timer(false);
        timerInstance.scheduleAtFixedRate(task, MILLISECOND_DELAY, MILLISECOND_DELAY);
    }

    /**
     * Updates the tray icon every {@link #MILLISECOND_DELAY} ms. If {@link #exit} is true, this halts the timer,
     * removes the tray icon and ends the program.
     */
    private void update() {
        if (exit) {
            SystemTray.getSystemTray().remove(trayIconInstance);
            timerInstance.cancel();
            System.exit(0);
            return;
        }

        // If we currently have the first image, we want the second image (index 1), and if we currently don't have
        // the first image, we want the first image (index 0).
        int index = firstImage ? 1 : 0;
        firstImage = !firstImage;
        trayIconInstance.setImage(IMAGES[index]);
    }

    /**
     * Singleton pattern - return the existing instance or create and return it if necessary.
     * @return  The singleton {@link BlinkingTrayIcon} instance.
     */
    public static BlinkingTrayIcon getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BlinkingTrayIcon();
        }

        return INSTANCE;
    }
}
