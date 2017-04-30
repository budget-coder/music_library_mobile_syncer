import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        // "Prettify" the FileChooser dialog.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            System.err.println("FATAL: Could not open the browse dialog. Please input the destination of source folder manually.");
        }
        JButton srcBrowseButton = new JButton("Browse...");
        srcBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /* TODO JavaFX has a much better FileChooser, which needs to be integrated in this Swing application...
                 * Sources: http://stackoverflow.com/questions/28920758/javafx-filechooser-in-swing
                 * Sources: https://www.reddit.com/r/javahelp/comments/2lypn4/trying_to_use_javafxstagefilechooser_in_a_swing/
                FileChooser openDialog = new FileChooser();
                openDialog.showOpenDialog(null);
                */
                final JFileChooser openDialog = new JFileChooser();
                openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                final String srcFolderString = browseDialog(openDialog);
                // If the user did not choose a folder, then keep the current folder.
                if (!srcFolderString.equals("")) {
                    txtSrcDir.setText(srcFolderString);
                }
            }
        });
        browsePanel.add(srcBrowseButton);
        
        JButton dstBrowseButton = new JButton("Browse...");
        dstBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser openDialog = new JFileChooser();
                openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                final String dstFolderString = browseDialog(openDialog);
                // If the user did not choose a folder, then keep the current folder.
                if (!dstFolderString.equals("")) {
                    txtDstDir.setText(dstFolderString);
                }
            }
        });
        browsePanel.add(dstBrowseButton);
        
        JPanel westPanel = new JPanel();
        getContentPane().add(westPanel, BorderLayout.WEST);
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        
        JCheckBox checkBox1 = new JCheckBox("Option 1");
        westPanel.add(checkBox1);
        JCheckBox checkBox2 = new JCheckBox("Option 2");
        westPanel.add(checkBox2);
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
                            musicSyncer = new MusicSyncer(txtSrcDir.getText(),
                                    txtDstDir.getText());
                            try {
                                musicSyncer.initiate();
                            } catch (InterruptedException e) {
                                // Restore everything to its default value.
                                startButton.setText("Start!");
                                srcBrowseButton.setEnabled(true);
                                dstBrowseButton.setEnabled(true);
                                statusText.append("Execution was stopped.\n");
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
     */
    private String browseDialog(JFileChooser jfc) {
        final String returnString;
        final int returnVal = jfc.showOpenDialog(UI.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            returnString = jfc.getSelectedFile().toString();
        } else {
            returnString = "";
        }
        
        return returnString;
    }

}
