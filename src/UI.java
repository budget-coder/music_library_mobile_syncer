import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class UI extends JFrame {
    /**
     * Serial user ID
     */
    private static final long serialVersionUID = 8597395032893667211L;
    private JTextField txtSrcDir;
    private JTextField txtDstDir;
    private JTextArea statusText;
    private MusicSyncer musicSyncer;
    private Thread musicSyncerThread;
    DirectoryChooser dirChooser;

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
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public UI() {
        setTitle("Your mom");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        
        JPanel topPanel = new JPanel();
        getContentPane().add(topPanel, BorderLayout.NORTH);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        
        JPanel dirPanel = new JPanel();
        topPanel.add(dirPanel);
        dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.Y_AXIS));
        
        txtSrcDir = new JTextField();
        dirPanel.add(txtSrcDir);
        txtSrcDir.setText("src dir");
        txtSrcDir.setColumns(10);
        
        txtDstDir = new JTextField();
        dirPanel.add(txtDstDir);
        txtDstDir.setText("target dir");
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
                        txtSrcDir.setText(dstFolderString);
                    }
                }
            }
        });
        browsePanel.add(dstBrowseButton);
        
        JPanel westPanel = new JPanel();
        getContentPane().add(westPanel, BorderLayout.WEST);
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        
        JCheckBox addNewMusicCheckBox = new JCheckBox("Add new music");
        westPanel.add(addNewMusicCheckBox);
        JCheckBox deleteOrphanedCheckBox = new JCheckBox("Delete orphaned music");
        westPanel.add(deleteOrphanedCheckBox);
        JCheckBox checkBox3 = new JCheckBox("Option 3");
        westPanel.add(checkBox3);
        
        JScrollPane centerPanel = new JScrollPane();
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        statusText = new JTextArea();
        statusText.setEditable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setText("Status window:\n");
        centerPanel.setViewportView(statusText);
        
        JPanel bottomPanel = new JPanel();
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        
        JButton startButton = new JButton("Start!");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                // Stop execution if the button was pressed while it was running.
                if (startButton.getText().equals("Stop!")) {
                    musicSyncerThread.interrupt();
                } else {
                    // Change the functionality of the button so that it stops
                    // execution when pressed again.
                    startButton.setText("Stop!");
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
                            musicSyncer.setRENAMEME(checkBox3.isSelected());
                            try {
                                musicSyncer.initiate();
                            } catch (InterruptedException e) {
                                statusText.append("Execution was stopped.\n");
                            } finally {
                                // Restore everything to its default value.
                                startButton.setText("Start!");
                                srcBrowseButton.setEnabled(true);
                                dstBrowseButton.setEnabled(true);
                            }
                            // When done, change the text back
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
    }
    
    /** 
     * Opens up a FileChooser dialog where you can open a file.
     * @param jfc
     * @return
     * @throws ExecutionException 
     * @throws InterruptedException 
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
        return queryFolder.get();
    }
}
