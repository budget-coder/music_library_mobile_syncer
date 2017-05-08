import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.DirectoryChooser;

public class UI extends JFrame {
    /**
     * Serial user ID
     */
    private static final long serialVersionUID = 8597395032893667211L;
    private JTextField txtSrcDir;
    private JTextField txtDstDir;
    private static JTextPane statusText;
    private static StyledDocument statusTextDoc;
    private MusicSyncer musicSyncer;
    private Thread musicSyncerThread;
    private DirectoryChooser dirChooser;
    private final String srcFolderAttr;
    private final int srcFolderIndex;
    private final String dstFolderAttr;
    private final int dstFolderIndex;
    private final String addNewMusicAttr;
    private final int addNewMusicIndex;
    private final String deleteOrphanedAttr;
    private final int deleteOrphanedIndex;
    private final String searchInSubdirectoriesAttr;
    private final int searchInSubdirectoriesIndex;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UI frame = new UI();
                    frame.setVisible(true);
                } catch (Exception e) {
                    System.out.println("FATAL: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public UI() {
        // For reading the settings correctly
        srcFolderAttr = "srcFolder=";
        srcFolderIndex = 0;
        dstFolderAttr = "dstFolder=";
        dstFolderIndex = 1;
        addNewMusicAttr = "addNewMusic=";
        addNewMusicIndex = 2;
        deleteOrphanedAttr= "deleteOrphaned=";
        deleteOrphanedIndex = 3;
        searchInSubdirectoriesAttr = "searchInSubdirectories=";
        searchInSubdirectoriesIndex = 4;
        setTitle("Music Library Mobile Syncer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        
        JPanel topPanel = new JPanel();
        getContentPane().add(topPanel, BorderLayout.NORTH);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        
        JPanel dirPanel = new JPanel();
        topPanel.add(dirPanel);
        dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.Y_AXIS));
        
        JScrollPane centerPanel = new JScrollPane();
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        // Make a text area with customizable text. The document reflects the changes.
        statusTextDoc = new DefaultStyledDocument();
        statusText = new JTextPane(statusTextDoc);
        statusText.setEditable(false);
        /* For JTextArea
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        */
        statusText.setText("Status window:\n");
        centerPanel.setViewportView(statusText);
        
        // Load MLMS_Settings.txt if available.
        String[] arrayOfSettings = tryToLoadPreviousSettings();
        txtSrcDir = new JTextField();
        dirPanel.add(txtSrcDir);
        txtDstDir = new JTextField();
        dirPanel.add(txtDstDir);
        if (!arrayOfSettings[srcFolderIndex].equals("")) {
            txtSrcDir.setText(arrayOfSettings[srcFolderIndex]);
            txtDstDir.setText(arrayOfSettings[dstFolderIndex]);
        } else {
            txtSrcDir.setText("Input the location of your music library?");
            txtDstDir.setText("Input the location of where to sync it");
        }
        txtSrcDir.setColumns(10);
        txtDstDir.setColumns(10);

        JPanel browsePanel = new JPanel();
        topPanel.add(browsePanel);
        browsePanel.setLayout(new BoxLayout(browsePanel, BoxLayout.Y_AXIS));
        /*
        // "Prettify" the FileChooser dialog.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            System.err.println("FATAL: Could not open the browse dialog. Please input the destination of source folder manually.");
        } */
        new JFXPanel(); // Initialize JavaFX thread when using its FileChooser (ideally called once)
        dirChooser = new DirectoryChooser();
        JButton srcBrowseButton = new JButton("Browse...");
        srcBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String srcFolderString = "";
                try {
                    srcFolderString = browseDialog();
                } catch (InterruptedException | ExecutionException ex) {
                    System.err.println("FATAL: " + ex.getMessage());
                } finally {
                    // If the user did not choose a folder, then keep the current folder.
                    if (!srcFolderString.equals("")) {
                        txtSrcDir.setText(srcFolderString);
                    }
                }
            }
        });
        browsePanel.add(srcBrowseButton);
        
        JButton dstBrowseButton = new JButton("Browse...");
        dstBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String dstFolderString = "";
                try {
                    dstFolderString = browseDialog();
                } catch (InterruptedException | ExecutionException ex) {
                    System.err.println("FATAL: " + ex.getMessage());
                } finally {
                    // If the user did not choose a folder, then keep the current folder.
                    if (!dstFolderString.equals("")) {
                        txtDstDir.setText(dstFolderString);
                    }
                }
            }
        });
        browsePanel.add(dstBrowseButton);
        
        JPanel westPanel = new JPanel();
        getContentPane().add(westPanel, BorderLayout.WEST);
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        
        JCheckBox addNewMusicCheckBox = new JCheckBox("Add new music");
        // Load previous settings and only care about whether "true" was written correctly.
        addNewMusicCheckBox.setSelected(Boolean.valueOf(arrayOfSettings[addNewMusicIndex]));
        addNewMusicCheckBox.setForeground(Color.BLUE);
        westPanel.add(addNewMusicCheckBox);
        JCheckBox deleteOrphanedCheckBox = new JCheckBox("Delete orphaned music");
        deleteOrphanedCheckBox.setSelected(Boolean.valueOf(arrayOfSettings[deleteOrphanedIndex]));
        deleteOrphanedCheckBox.setForeground(Color.ORANGE);
        westPanel.add(deleteOrphanedCheckBox);
        JCheckBox searchInSubdirectoriesCheckBox = new JCheckBox("Search in subdirectories");
        searchInSubdirectoriesCheckBox.setSelected(Boolean.valueOf(arrayOfSettings[searchInSubdirectoriesIndex]));
        westPanel.add(searchInSubdirectoriesCheckBox);
        
        JPanel bottomPanel = new JPanel();
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        JButton startButton = new JButton("Start!");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // Stop execution if the button was pressed while it was running.
                // TODO Actually add places where interrupts in MusicSyncer.
                if (startButton.getText().equals("Stop!")) {
                    musicSyncerThread.interrupt();
                } else {
                    // Adding stopwatch for easier visualization of algorithm effectiveness.
                    long timeStart = System.currentTimeMillis();
                    // Change the functionality of the button so that it stops
                    // execution when pressed again.
                    startButton.setText("Stop!");
                    // Clear text
                    statusText.setText("");
                    // It does not make sense to be able to change the dirs.
                    srcBrowseButton.setEnabled(false);
                    dstBrowseButton.setEnabled(false);
                    /*
                     * When the start button is pressed, we make a thread of the
                     * main program. The reasons are two-fold: 1) To ensure the
                     * user can still interact with the application (this
                     * includes aborting the operation or closing the program)
                     * 2) Show progression with status messages and the progress
                     * bar.
                     */
                    Runnable musicSyncerRunnable = new Runnable() {
                        @Override
                        public void run() {
                            musicSyncer = new MusicSyncer(txtSrcDir.getText(), txtDstDir.getText());
                            // Include the state of the checkboxes.
                            musicSyncer.setAddNewMusicOption(addNewMusicCheckBox.isSelected());
                            musicSyncer.setDeleteOrphanedMusic(deleteOrphanedCheckBox.isSelected());
                            musicSyncer.setSearchInSubdirectories(searchInSubdirectoriesCheckBox.isSelected());
                            try {
                                musicSyncer.initiate();
                            } catch (InterruptedException e) {
                                SimpleAttributeSet attr = new SimpleAttributeSet();
                                StyleConstants.setForeground(attr, Color.BLUE);
                                writeStatusMessage("Execution was stopped.", attr);
                            } finally {
                                // Restore everything to its default value.
                                SimpleAttributeSet attr = new SimpleAttributeSet();
                                StyleConstants.setForeground(attr, Color.BLUE);
                                writeStatusMessage("Finished. Time taken: " + (System.currentTimeMillis() - timeStart) + " ms.", attr);
                                startButton.setText("Start!");
                                srcBrowseButton.setEnabled(true);
                                dstBrowseButton.setEnabled(true);
                            }
                        }
                    };
                    musicSyncerThread = new Thread(musicSyncerRunnable);
                    musicSyncerThread.start();
                }
            }
        });
        bottomPanel.add(startButton);
        
        JProgressBar progressBar = new JProgressBar();
        bottomPanel.add(progressBar);
        // Save settings before exiting the application.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<String> settings = Arrays.asList(srcFolderAttr
                            + txtSrcDir.getText() + "\n" + dstFolderAttr
                            + txtDstDir.getText() + "\n" + addNewMusicAttr
                            + addNewMusicCheckBox.isSelected() + "\n"
                            + deleteOrphanedAttr
                            + deleteOrphanedCheckBox.isSelected() + "\n"
                            + searchInSubdirectoriesAttr
                            + searchInSubdirectoriesCheckBox.isSelected());
                    Files.write(Paths.get("MLMS_Settings.txt"), settings, Charset.forName("UTF-8"));
                } catch (IOException e) {
                    System.err.println("ERROR: Could not save a list of the music to a .txt file!");
                }
            }
        }));
    }
    
    private String[] tryToLoadPreviousSettings() {
        String srcFolder = "";
        String dstFolder = "";
        String addNewMusic = "";
        String deleteOrphaned = "";
        String searchInSubdirectories = "";
        final String[] returnArray;
        File settings = new File("MLMS_Settings.txt");
        if (!settings.exists()) {
            returnArray = new String[]{"", ""};
        } else {
            // Try-with-ressources to ensure that the stream is closed.
            try (BufferedReader br = new BufferedReader(new FileReader(settings))) {
                String line = br.readLine();
    
                while (line != null) {
                    if (line.startsWith(srcFolderAttr)) {
                        srcFolder = line.substring(srcFolderAttr.length());
                    } else if (line.startsWith(dstFolderAttr)) {
                        dstFolder = line.substring(dstFolderAttr.length());
                    } else if (line.startsWith(addNewMusicAttr)) {
                        addNewMusic = line.substring(addNewMusicAttr.length());
                    } else if (line.startsWith(deleteOrphanedAttr)) {
                        deleteOrphaned = line.substring(deleteOrphanedAttr.length());
                    } else if (line.startsWith(searchInSubdirectoriesAttr)) {
                        searchInSubdirectories = line.substring(searchInSubdirectoriesAttr.length());
                    }
                    line = br.readLine();
                }
            } catch (FileNotFoundException e) {
                SimpleAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, Color.BLUE);
                writeStatusMessage("No previous settings were found.", attr);
            } catch (IOException e) {
                System.err.println("Error when loading settings: " + e.getMessage());
            }
            returnArray = new String[]{srcFolder, dstFolder, addNewMusic, deleteOrphaned, searchInSubdirectories};
        }
        return returnArray;
    }

    /**
     * ...
     * Only package visible to avoid calls from, for instance, the test package.
     * @param message
     * @param attributeSet
     */
    static void writeStatusMessage(String message, MutableAttributeSet attributeSet) {
        try {
            statusTextDoc.insertString(statusTextDoc.getLength(), message + "\n", attributeSet);
        } catch (BadLocationException e) {
            // This should not happen
            System.err.println(
                    "FATAL: Could not write status message because of an invalid position. The error message: "
                            + e.getMessage());
        }
    }
    
    /**
     * Opens up a FileChooser dialog where you can open a file.
     * 
     * @return the path to the folder chosen by the user, if any. Otherwise, it
     *         will be the empty string.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private String browseDialog() throws InterruptedException, ExecutionException {
        /* The old way with JFileChooser
        final String returnString;
        final int returnVal = jfc.showOpenDialog(UI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            returnString = jfc.getSelectedFile().toString();
        } else {
            returnString = "";
        }
        
        return returnString;
        */
        
        /*
         * TODO Javadoc for this awesome code is needed. Source: http://stackoverflow.com/a/13804542
         */
        final FutureTask<String> queryFolder = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() {
                // Show open directory dialog
                final File folder = dirChooser.showDialog(null);
                final String folderString;
                if (folder != null) {
                    dirChooser.setInitialDirectory(folder);
                    folderString = folder.getAbsolutePath();
                } else {
                    folderString = "";
                }
                return folderString;
            }
        });
        Platform.runLater(queryFolder);
        // The following method is synchronous; it will block until queryFolder is done.
        return queryFolder.get();
    }
}
