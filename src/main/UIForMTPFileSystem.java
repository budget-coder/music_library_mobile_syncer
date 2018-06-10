package main;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import iconHandlers.IconCellRenderer;
import iconHandlers.IconData;
import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIForMTPFileSystem extends JFrame {
	/**
	 * Serial user ID
	 */
	private static final long serialVersionUID = 5882386163684767108L;
	private JPanel contentPane;
	private static final String BASE_PATH_ICONS = System.getProperty("user.dir") + "\\icons\\";
	private static final ImageIcon ICON_DEVICE = new ImageIcon(BASE_PATH_ICONS + "device.png");
	private static final ImageIcon ICON_STORAGE = new ImageIcon(BASE_PATH_ICONS + "storage.png");
	private static final ImageIcon ICON_FOLDER = new ImageIcon(BASE_PATH_ICONS + "folder.png");
	private static final ImageIcon ICON_FOLDER_EXP = new ImageIcon(BASE_PATH_ICONS + "folder-exp.png");
	private static final int DEVICE_ROW = 0;
	// TEST STUFF 
	/*
	final int maxNewNodes = 3;
	int countOfNewNodes = 0;
	*/
	private List<Integer> nodeHashCodesList = new ArrayList<>();
	private Map<TreePath, PortableDeviceStorageObject> pathStorageMap = new HashMap<>();
	private Map<TreePath, PortableDeviceFolderObject> pathFolderMap = new HashMap<>();
	
	// TODO DELETE MAIN AFTER TESTING
	/*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIForMTPFileSystem frame = new UIForMTPFileSystem(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	*/
	
	
	/**
	 * Create the frame.
	 */
	public UIForMTPFileSystem(PortableDevice device) {
		// When closing, clean up WITHOUT exiting the whole application
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		// Create the root node and pack it into a JTree. 
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				new IconData(ICON_DEVICE, null, device.getFriendlyName()));
		JTree tree = new JTree(top);
		// Insert the detected storage units
		ArrayList<PortableDeviceStorageObject> deviceStorages = MTPUtil.getDeviceStorages(device);
		// Sort list of storages in alphabetical order
		deviceStorages.sort((storage1, storage2)->storage1.getName().compareTo(storage2.getName()));
		for (int i = 0; i < deviceStorages.size(); i++) {
			PortableDeviceStorageObject storage = deviceStorages.get(i);
			addChildToTree(new IconData(ICON_STORAGE, null, storage.getName()), tree, top);
		}
		tree.expandRow(DEVICE_ROW); // Expand the device, effectively showing the newly added storages.
		nodeHashCodesList.add(tree.getModel().getRoot().hashCode()); // Make sure the device isn't visited again
		visitStorages(deviceStorages, tree);
		// Specify a custom cell renderer for icons etc.
		tree.setCellRenderer(new IconCellRenderer());
		contentPane.add(tree, BorderLayout.CENTER);
		// Attach a mouse listener which, when a node is double-clicked, displays its
		// children if double-clicked (i.e. expanded) for the first time.
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 2) {
					return;
				}
				int nodeRow = tree.getRowForLocation(e.getX(), e.getY());
				if (nodeRow != -1) { // A node has been double-clicked. Check if we've seen it before.
					TreePath pathToNode = tree.getPathForLocation(e.getX(), e.getY());
					if (!nodeHashCodesList.contains(pathToNode.hashCode())) {
						// The node has not been visited before. Visit its children, if any.
						System.out.println("Visiting path " + pathToNode);
						visitChild(pathToNode, tree);
						nodeHashCodesList.add(pathToNode.hashCode()); // Mark node as visited.
					}
				}
			}
		});
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) { // Do nothing
			}
			
			@Override
			public void treeExpanded(TreeExpansionEvent selectedNode) {
				System.out.println("Expanding tree " + selectedNode.getPath());
				if (!nodeHashCodesList.contains(selectedNode.getPath().hashCode())) {
					nodeHashCodesList.add(selectedNode.getPath().hashCode());
				}
			}
		});
		// Attach a listener to every node which traverses a selected node and displays its children. 
		/*
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent selectedNode) {
				// Check if the node has been marked before. If yes, then do nothing.
				final int selectedNodeHashCode = selectedNode.getPath().getLastPathComponent().hashCode();
				if (!nodeHashCodesList.contains(selectedNodeHashCode)) {
					// Mark node as ready to be visited.
//					nodeHashCodesList.add(selectedNodeHashCode);
				} else {
					System.out.println("TEST: Old visited node at " + selectedNode.getPath() + ". No. of rows: " + tree.getRowCount());
				}
			}
		});
		*/
		// Get path of selected folder before exiting
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				DefaultMutableTreeNode currNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (currNode != null) {
					TreeNode[] pathToSelectedNode = currNode.getPath();
					String currPath = "";
					for (int i = 0; i < pathToSelectedNode.length; ++i) {
				        currPath += pathToSelectedNode[i];
				        if (i+1 < pathToSelectedNode.length) {
				            currPath += File.separator;
				        }
				    }
					System.out.println("Path to selected node: " + currPath);
				}
			}
		});
	}

	private void addChildToTree(IconData iconData, JTree tree, DefaultMutableTreeNode parentNode) {
		MutableTreeNode newNode = new DefaultMutableTreeNode(iconData);
		((DefaultTreeModel) tree.getModel()).insertNodeInto(newNode, parentNode, parentNode.getChildCount());
	}
	
	private void visitChild(TreePath pathToNode, JTree tree) {
		TreePath pathWithoutChild = new TreePath(
				Arrays.copyOfRange(pathToNode.getPath(), DEVICE_ROW, pathToNode.getPathCount() - 1));
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) pathToNode.getLastPathComponent();
		// Check if we are visiting a storage's children or a child on a deeper level/column. 
		if (pathWithoutChild.getPathCount() <= 2) {
			PortableDeviceStorageObject storage = pathStorageMap.get(pathWithoutChild);
			// TODO If storages are added correctly, then this check can be optimized away.
			if (storage != null) {
				System.out.println("Visiting storage " + storage.getName() + " and child " + childNode);
				PortableDeviceFolderObject parentFolder = (PortableDeviceFolderObject) MTPUtil.getChildByName(storage,
						childNode.toString());
				if (parentFolder == null) {
					// TODO Display to user or something.
					System.err.println("ERROR: Folder " + childNode + " not found!");
				} else {
					PortableDeviceObject[] children = parentFolder.getChildObjects();
					if (children.length > 0) {
						insertFoldersIntoTree(parentFolder.getChildObjects(), tree, childNode);
						tree.expandRow(tree.getRowForPath(pathToNode));
						// Store path of visited parent folder so that we can visit its children.
						pathFolderMap.put(pathToNode, parentFolder);
					}
				}
			}
			else {
				System.err.println("Error with storage child thingy. This path was not found: " + pathWithoutChild);
				System.err.println(". No. of elements in path: " + pathToNode.getPath());
			}
		} else {
			PortableDeviceFolderObject parentFolder = MTPUtil.getChildByName(pathFolderMap.get(pathWithoutChild),
					childNode.toString());
			if (parentFolder == null) {
				// TODO Display to user or something.
				System.err.println("ERROR: Folder " + childNode + " not found!");
			} else {
				PortableDeviceObject[] children = parentFolder.getChildObjects();
				if (children.length > 0) {
					insertFoldersIntoTree(parentFolder.getChildObjects(), tree, childNode);
					tree.expandRow(tree.getRowForPath(pathToNode));
					// Like before, store path of this parent folder in case we visit its children.
					pathFolderMap.put(pathToNode, parentFolder);
				}
			}
		}
		
		
	}

	/**
	 * Visits and adds the children of each storage to a tree.
	 * 
	 * @param deviceStorages
	 *            an <b> alphabetically sorted</b> list of storages in ascending
	 *            order.
	 * @param tree
	 *            a tree with the storages in {@code deviceStorages} already
	 *            inserted.
	 */
	private void visitStorages(ArrayList<PortableDeviceStorageObject> deviceStorages, JTree tree) {
		for (int i = 0; i < deviceStorages.size(); i++) {
			// Get children for each storage (are NOT guaranteed to appear in alphabetical order)
			PortableDeviceObject[] storageChildren = deviceStorages.get(i).getChildObjects();
			if (storageChildren.length > 0) {
				DefaultMutableTreeNode storageNode = (DefaultMutableTreeNode) tree.getModel()
						.getChild(tree.getModel().getRoot(), i);
				insertFoldersIntoTree(storageChildren, tree, storageNode);
				nodeHashCodesList.add(storageNode.hashCode()); // Add the storage to the list of visited nodes.
				// Store storage path for when visiting its children.
				pathStorageMap.put(tree.getPathForRow(i + 1), deviceStorages.get(i));
			}
			/*
			List<PortableDeviceFolderObject> listOfFolders = new ArrayList<>();
			for (int j = 0; j < storageChildren.length; j++) {
				// Only include the child in the tree if it's a folder. 
				if (storageChildren[j] instanceof PortableDeviceFolderObject) {
					PortableDeviceFolderObject newFolder = (PortableDeviceFolderObject) storageChildren[j];
					listOfFolders.add(newFolder);
				}
			}
			// Sort folders alphabetically with lambda expression
			listOfFolders.sort((folder1, folder2)->folder1.getName().compareTo(folder2.getName()));
			// Insert the folders into the tree structure
			DefaultMutableTreeNode storageNode = (DefaultMutableTreeNode) tree.getModel()
					.getChild(tree.getModel().getRoot(), i);
			for (int j = 0; j < listOfFolders.size(); j++) {
				addChildToTree(new IconData(ICON_FOLDER, ICON_FOLDER_EXP, listOfFolders.get(j).getName()), tree,
						storageNode);
			}
			*/
		}
	}

	private void insertFoldersIntoTree(PortableDeviceObject[] folders, JTree tree, DefaultMutableTreeNode parentNode) {
		List<PortableDeviceFolderObject> listOfFolders = new ArrayList<>();
		for (int j = 0; j < folders.length; j++) {
			// Only include the child in the tree if it's a folder. 
			if (folders[j] instanceof PortableDeviceFolderObject) {
				PortableDeviceFolderObject newFolder = (PortableDeviceFolderObject) folders[j];
				listOfFolders.add(newFolder);
			}
		}
		// Sort folders alphabetically with lambda expression
		listOfFolders.sort((folder1, folder2)->folder1.getName().compareTo(folder2.getName()));
		// Insert the folders into the tree structure
		for (int j = 0; j < listOfFolders.size(); j++) {
			addChildToTree(new IconData(ICON_FOLDER, ICON_FOLDER_EXP, listOfFolders.get(j).getName()), tree,
					parentNode);
		}
	}
} // End class
