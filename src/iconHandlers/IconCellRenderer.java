package iconHandlers;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class IconCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * Serial user id
	 */
	private static final long serialVersionUID = -990399320582096823L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		setText(obj.toString());

		if (obj instanceof Boolean) {
			setText("Retrieving data...");
		}
		if (obj instanceof IconData) {
			IconData idata = (IconData) obj;
			if (expanded) {
				setIcon(idata.getExpandedIcon());
			} else {
				setIcon(idata.getIcon());
			}
		} else {
			setIcon(null);
		}
		return this;
	}
}
