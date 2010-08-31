package it.unitn.disi.smatch.gui;

import com.ikayzo.swing.icon.IconUtils;
import com.ikayzo.swing.icon.JIconFile;
import com.ikayzo.swing.icon.LayeredIcon;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * GUI for S-Match.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SMatchGUI extends Observable {

    private static Logger log;

    static {
        log = Logger.getLogger(SMatchGUI.class);
        String log4jConf = System.getProperty("log4j.configuration");
        if (log4jConf != null) {
            PropertyConfigurator.configure(log4jConf);
        } else {
            System.err.println("No log4j.configuration property specified.");
        }
    }

    private static final String MAIN_ICON_FILE = "/s-match.ico";
    private static final String CONF_FILE = ".." + File.separator + "conf" + File.separator + "SMatchGUI.properties";
    private Properties properties;

    private String lookAndFeel = null;

    private IMatchManager mm = null;
    private IContext source = null;
    private String sourceLocation = null;
    private IContext target = null;
    private String targetLocation = null;
    private IContextMapping<INode> mapping = null;
    private String mappingLocation = null;
    private JTree lastFocusedTree;

    private String configFileName;
    private Properties commandProperties;

    private static final String TANGO_ICONS_PATH = "/tango-icon-theme-0.8.90/";

    public static JIconFile loadIconFile(String name) {
        JIconFile icon = null;
        try {
            icon = new JIconFile(SMatchGUI.class.getResource(name + ".jic"));
        } catch (IOException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Error loading icon " + name, e);
            }
        }
        return icon;
    }

    private static Icon documentOpenSmall;
    private static Icon documentOpenLarge;
    private static Icon documentSaveSmall;
    private static Icon documentSaveLarge;
    private static Icon documentSaveAsSmall;
    private static Icon documentSaveAsLarge;
    private static Icon folderSmall;
    private static Icon folderOpenSmall;
    private static Icon iconDJ;
    private static Icon iconEQ;
    private static Icon iconMG;
    private static Icon iconLG;
    private static Icon smallIconDJ;
    private static Icon smallIconEQ;
    private static Icon smallIconMG;
    private static Icon smallIconLG;
    private static Icon iconAddNodeLarge;
    private static Icon iconAddChildNodeLarge;
    private static Icon iconAddLinkLarge;
    private static Icon iconDeleteLarge;
    private static Icon iconAddNodeSmall;
    private static Icon iconAddChildNodeSmall;
    private static Icon iconAddLinkSmall;
    private static Icon iconDeleteSmall;
    private static Icon iconContextCreateLarge;
    private static Icon iconContextCreateSmall;
    private static Icon iconUncoalesceSmall;
    private static Icon iconUncoalesceLarge;

    private static final int VERY_SMALL_ICON_SIZE = 12;
    private static final int SMALL_ICON_SIZE = 16;
    private static final int LARGE_ICON_SIZE = 32;

    private static final String EMPTY_ROOT_NODE_LABEL = "Create or open context";
    private static final String ELLIPSIS = "...";

    static {
        JIconFile icon = loadIconFile(TANGO_ICONS_PATH + "actions/document-open");
        documentOpenSmall = icon.getIcon(SMALL_ICON_SIZE);
        documentOpenLarge = icon.getIcon(LARGE_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/document-save");
        documentSaveSmall = icon.getIcon(SMALL_ICON_SIZE);
        documentSaveLarge = icon.getIcon(LARGE_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/document-save-as");
        documentSaveAsSmall = icon.getIcon(SMALL_ICON_SIZE);
        documentSaveAsLarge = icon.getIcon(LARGE_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "/places/folder");
        folderSmall = icon.getIcon(SMALL_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "/status/folder-open");
        folderOpenSmall = icon.getIcon(SMALL_ICON_SIZE);

        icon = loadIconFile("/relations/disjoint");
        iconDJ = icon.getIcon(SMALL_ICON_SIZE);
        smallIconDJ = icon.getIcon(VERY_SMALL_ICON_SIZE);

        icon = loadIconFile("/relations/equivalent");
        iconEQ = icon.getIcon(SMALL_ICON_SIZE);
        smallIconEQ = icon.getIcon(VERY_SMALL_ICON_SIZE);

        icon = loadIconFile("/relations/less-general");
        iconLG = icon.getIcon(SMALL_ICON_SIZE);
        smallIconLG = icon.getIcon(VERY_SMALL_ICON_SIZE);

        icon = loadIconFile("/relations/more-general");
        iconMG = icon.getIcon(SMALL_ICON_SIZE);
        smallIconMG = icon.getIcon(VERY_SMALL_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/go-jump");
        ImageIcon iconGoJumpLarge = icon.getIcon(LARGE_ICON_SIZE);
        ImageIcon iconGoJumpSmall = icon.getIcon(SMALL_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/list-add");
        ImageIcon iconListAddLarge = icon.getIcon(LARGE_ICON_SIZE);
        ImageIcon iconListAddSmall = icon.getIcon(SMALL_ICON_SIZE);
        ImageIcon iconListAddSmallest = icon.getIcon(SMALL_ICON_SIZE / 2);

        iconAddLinkLarge = new LayeredIcon(iconGoJumpLarge, iconListAddSmall, SwingConstants.RIGHT, SwingConstants.BOTTOM, 4, 4);
        iconAddLinkSmall = new LayeredIcon(iconGoJumpSmall, iconListAddSmallest, SwingConstants.RIGHT, SwingConstants.BOTTOM, 2, 2);

        // convert to ImageIcon, otherwise disabled icon does not render due to restriction in LookAndFeel.java
        iconAddLinkLarge = IconUtils.makeIconFromComponent(new JLabel(iconAddLinkLarge), LARGE_ICON_SIZE, LARGE_ICON_SIZE, true);
        iconAddLinkSmall = IconUtils.makeIconFromComponent(new JLabel(iconAddLinkSmall), SMALL_ICON_SIZE, SMALL_ICON_SIZE, true);

        iconAddNodeSmall = iconListAddSmall;
        iconAddNodeLarge = iconListAddLarge;

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/list-remove");
        iconDeleteSmall = icon.getIcon(SMALL_ICON_SIZE);
        iconDeleteLarge = icon.getIcon(LARGE_ICON_SIZE);

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/document-new");
        iconContextCreateSmall = icon.getIcon(SMALL_ICON_SIZE);
        iconContextCreateLarge = icon.getIcon(LARGE_ICON_SIZE);

        iconAddChildNodeLarge = new LayeredIcon(iconListAddLarge, iconListAddSmall, SwingConstants.RIGHT, SwingConstants.BOTTOM, 4, 4);
        iconAddChildNodeSmall = new LayeredIcon(iconListAddSmall, iconListAddSmallest, SwingConstants.RIGHT, SwingConstants.BOTTOM, 2, 2);

        // convert to ImageIcon, otherwise disabled icon does not render due to restriction in LookAndFeel.java
        iconAddChildNodeLarge = IconUtils.makeIconFromComponent(new JLabel(iconAddChildNodeLarge), LARGE_ICON_SIZE, LARGE_ICON_SIZE, true);
        iconAddChildNodeSmall = IconUtils.makeIconFromComponent(new JLabel(iconAddChildNodeSmall), SMALL_ICON_SIZE, SMALL_ICON_SIZE, true);

        icon = loadIconFile(TANGO_ICONS_PATH + "actions/view-fullscreen");
        iconUncoalesceSmall = icon.getIcon(SMALL_ICON_SIZE);
        iconUncoalesceLarge = icon.getIcon(LARGE_ICON_SIZE);
    }

    private class ActionSourceCreate extends AbstractAction implements Observer {
        public ActionSourceCreate() {
            super("Create");
            putValue(Action.SHORT_DESCRIPTION, "Creates Source");
            putValue(Action.LONG_DESCRIPTION, "Creates Source Context");
            putValue(Action.SMALL_ICON, iconContextCreateSmall);
            putValue(Action.LARGE_ICON_KEY, iconContextCreateLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            source = mm.createContext();
            source.createRoot("Top");
            createTree(source, tSource, null);
            setChanged();
            notifyObservers();
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm);
        }
    }

    private abstract class ActionTreeOpen extends AbstractAction implements Observer {

        protected ActionTreeOpen(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getContextLoader().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showOpenDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (acMappingClose.isEnabled()) {
                    acMappingClose.actionPerformed(actionEvent);
                }
                open(file);
            }
        }

        protected abstract void open(File file);

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != mm.getContextLoader());
        }
    }

    private abstract class ActionTreePreprocess extends AbstractAction implements Observer {

        protected ActionTreePreprocess(String name) {
            super(name);
        }

        protected void preprocess(IContext context) {
            try {
                mm.offline(context);
                setChanged();
                notifyObservers();
            } catch (SMatchException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Error while preprocessing context", e);
                }
            }
        }
    }


    private class ActionSourceOpen extends ActionTreeOpen implements Observer {
        public ActionSourceOpen() {
            super("Open...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(Action.SHORT_DESCRIPTION, "Opens Source");
            putValue(Action.LONG_DESCRIPTION, "Opens Source Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentOpenSmall);
            putValue(Action.LARGE_ICON_KEY, documentOpenLarge);
        }

        @Override
        protected void open(File file) {
            openSource(file);
        }
    }

    private class ActionSourcePreprocess extends ActionTreePreprocess implements Observer {
        public ActionSourcePreprocess() {
            super("Preprocess");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(Action.SHORT_DESCRIPTION, "Preprocesses Source");
            putValue(Action.LONG_DESCRIPTION, "Preprocesses Source Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            preprocess(source);
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != source && null != mm.getContextPreprocessor());
        }
    }

    private class ActionSourceClose extends AbstractAction implements Observer {
        public ActionSourceClose() {
            super("Close");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(Action.SHORT_DESCRIPTION, "Closes Source");
            putValue(Action.LONG_DESCRIPTION, "Closes Source Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (acMappingClose.isEnabled()) {
                acMappingClose.actionPerformed(actionEvent);
            }
            source = null;
            sourceLocation = null;
            createTree(source, tSource, null);
            setChanged();
            notifyObservers();
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != source);
        }
    }

    private class ActionSourceSave extends AbstractAction implements Observer {
        public ActionSourceSave() {
            super("Save");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(Action.SHORT_DESCRIPTION, "Saves Source");
            putValue(Action.LONG_DESCRIPTION, "Saves Source Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentSaveSmall);
            putValue(Action.LARGE_ICON_KEY, documentSaveLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (null == sourceLocation) {
                ff.setDescription(mm.getContextRenderer().getDescription());
                fc.addChoosableFileFilter(ff);
                final int returnVal = fc.showSaveDialog(mainPanel);
                fc.removeChoosableFileFilter(ff);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    sourceLocation = file.getAbsolutePath();
                }
            }

            if (null != sourceLocation) {
                log.info("Saving source: " + sourceLocation);
                try {
                    mm.renderContext(source, sourceLocation);
                } catch (SMatchException e) {
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Error while saving source context", e);
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != source && null != mm.getContextRenderer());
        }
    }

    private class ActionSourceSaveAs extends AbstractAction implements Observer {
        public ActionSourceSaveAs() {
            super("Save As...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(Action.SHORT_DESCRIPTION, "Saves Source");
            putValue(Action.LONG_DESCRIPTION, "Saves Source Context");
            putValue(Action.SMALL_ICON, documentSaveAsSmall);
            putValue(Action.LARGE_ICON_KEY, documentSaveAsLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getContextRenderer().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showSaveDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                sourceLocation = file.getAbsolutePath();
            }

            if (null != sourceLocation) {
                log.info("Saving source: " + sourceLocation);
                try {
                    mm.renderContext(source, sourceLocation);
                } catch (SMatchException e) {
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Error while saving source context", e);
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != source && null != mm.getContextRenderer());
        }
    }

    private class ActionTargetCreate extends AbstractAction implements Observer {
        public ActionTargetCreate() {
            super("Create");
            putValue(Action.SHORT_DESCRIPTION, "Creates Target");
            putValue(Action.LONG_DESCRIPTION, "Creates Target Context");
            putValue(Action.SMALL_ICON, iconContextCreateSmall);
            putValue(Action.LARGE_ICON_KEY, iconContextCreateLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            target = mm.createContext();
            target.createRoot("Top");
            createTree(target, tTarget, null);
            setChanged();
            notifyObservers();
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm);
        }
    }

    private class ActionTargetOpen extends ActionTreeOpen implements Observer {
        public ActionTargetOpen() {
            super("Open...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(Action.SHORT_DESCRIPTION, "Opens Target");
            putValue(Action.LONG_DESCRIPTION, "Opens Target Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentOpenSmall);
            putValue(Action.LARGE_ICON_KEY, documentOpenLarge);
        }

        @Override
        protected void open(File file) {
            openTarget(file);
        }
    }

    private class ActionTargetPreprocess extends ActionTreePreprocess implements Observer {
        public ActionTargetPreprocess() {
            super("Preprocess");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(Action.SHORT_DESCRIPTION, "Preprocesses Target");
            putValue(Action.LONG_DESCRIPTION, "Preprocesses Target Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            preprocess(target);
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != target && null != mm.getContextPreprocessor());
        }
    }

    private class ActionTargetClose extends AbstractAction implements Observer {
        public ActionTargetClose() {
            super("Close");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(Action.SHORT_DESCRIPTION, "Closes Target");
            putValue(Action.LONG_DESCRIPTION, "Closes Target Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (acMappingClose.isEnabled()) {
                acMappingClose.actionPerformed(actionEvent);
            }
            target = null;
            targetLocation = null;
            createTree(target, tTarget, null);
            setChanged();
            notifyObservers();
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != target);
        }
    }

    private class ActionTargetSave extends AbstractAction implements Observer {
        public ActionTargetSave() {
            super("Save");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(Action.SHORT_DESCRIPTION, "Saves Target");
            putValue(Action.LONG_DESCRIPTION, "Saves Target Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentSaveSmall);
            putValue(Action.LARGE_ICON_KEY, documentSaveLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (null == targetLocation) {
                ff.setDescription(mm.getContextRenderer().getDescription());
                fc.addChoosableFileFilter(ff);
                final int returnVal = fc.showSaveDialog(mainPanel);
                fc.removeChoosableFileFilter(ff);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    targetLocation = file.getAbsolutePath();
                }
            }

            if (null != targetLocation) {
                log.info("Saving target: " + targetLocation);
                try {
                    mm.renderContext(target, targetLocation);
                } catch (SMatchException e) {
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Error while saving target context", e);
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != target && null != mm.getContextRenderer());
        }
    }

    private class ActionTargetSaveAs extends AbstractAction implements Observer {
        public ActionTargetSaveAs() {
            super("Save As...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(Action.SHORT_DESCRIPTION, "Saves Target");
            putValue(Action.LONG_DESCRIPTION, "Saves Target Context");
            putValue(Action.SMALL_ICON, documentSaveAsSmall);
            putValue(Action.LARGE_ICON_KEY, documentSaveAsLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getContextRenderer().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showSaveDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                targetLocation = file.getAbsolutePath();
            }

            if (null != targetLocation) {
                log.info("Saving target: " + targetLocation);
                try {
                    mm.renderContext(target, targetLocation);
                } catch (SMatchException e) {
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Error while saving target context", e);
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != target && null != mm.getContextRenderer());
        }
    }

    private abstract class ActionTreeEdit extends AbstractAction implements Observer {
        protected JTree tree;

        public ActionTreeEdit(String name) {
            super(name);
            tree = null;
        }

        protected abstract void doAction(JTree tree);

        public void actionPerformed(ActionEvent actionEvent) {
            if (null == tree) {
                doAction(lastFocusedTree);
            } else {
                doAction(tree);
            }
        }

        protected abstract void setEnabled(JTree tree);

        public void update(Observable o, Object arg) {
            if (null == tree) {
                setEnabled(lastFocusedTree);
            } else {
                setEnabled(tree);
            }
        }

    }

    private class ActionEditAddNode extends ActionTreeEdit implements Observer {

        public ActionEditAddNode() {
            super("Add Node");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(Action.SHORT_DESCRIPTION, "Adds a node");
            putValue(Action.LONG_DESCRIPTION, "Adds a node");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
            putValue(Action.SMALL_ICON, iconAddNodeSmall);
            putValue(Action.LARGE_ICON_KEY, iconAddNodeLarge);
        }

        public ActionEditAddNode(JTree tree) {
            this();
            this.tree = tree;
            putValue(Action.ACCELERATOR_KEY, null);
        }

        @Override
        protected void doAction(JTree tree) {
            addNode(tree);
        }

        @Override
        public void setEnabled(JTree tree) {
            setEnabled(null != tree &&
                    1 == tree.getSelectionCount() && (tree.getSelectionPath().getLastPathComponent() instanceof INode) && ((INode) tree.getSelectionPath().getLastPathComponent()).hasParent()
            );
        }
    }

    private class ActionEditAddChildNode extends ActionTreeEdit implements Observer {

        public ActionEditAddChildNode() {
            super("Add Child Node");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(Action.SHORT_DESCRIPTION, "Adds a child node");
            putValue(Action.LONG_DESCRIPTION, "Adds a child node to a node");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.ALT_DOWN_MASK));
            putValue(Action.SMALL_ICON, iconAddChildNodeSmall);
            putValue(Action.LARGE_ICON_KEY, iconAddChildNodeLarge);
        }

        public ActionEditAddChildNode(JTree tree) {
            this();
            this.tree = tree;
            putValue(Action.ACCELERATOR_KEY, null);
        }

        @Override
        protected void doAction(JTree tree) {
            addChildNode(tree);
        }

        @Override
        public void setEnabled(JTree tree) {
            setEnabled(null != tree &&
                    1 == tree.getSelectionCount() && (tree.getSelectionPath().getLastPathComponent() instanceof INode)
            );
        }
    }

    private class ActionEditAddLink extends AbstractAction implements Observer {
        public ActionEditAddLink() {
            super("Add Link");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
            putValue(Action.SHORT_DESCRIPTION, "Adds a link");
            putValue(Action.LONG_DESCRIPTION, "Adds a link");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
            putValue(Action.SMALL_ICON, iconAddLinkSmall);
            putValue(Action.LARGE_ICON_KEY, iconAddLinkLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if ((tSource.getSelectionPath().getLastPathComponent() instanceof INode)
                    && (tTarget.getSelectionPath().getLastPathComponent() instanceof INode)) {
                INode sourceNode = (INode) tSource.getSelectionPath().getLastPathComponent();
                INode targetNode = (INode) tTarget.getSelectionPath().getLastPathComponent();

                TreePath sourcePath = createPathToRoot(sourceNode);
                TreePath targetPath = createPathToRoot(targetNode);

                if (sourceNode.getNodeData().getUserObject() instanceof List
                        && targetNode.getNodeData().getUserObject() instanceof List) {

                    @SuppressWarnings("unchecked")
                    List<DefaultMutableTreeNode> sourceLinkNodes = (List<DefaultMutableTreeNode>) sourceNode.getNodeData().getUserObject();
                    @SuppressWarnings("unchecked")
                    List<DefaultMutableTreeNode> targetLinkNodes = (List<DefaultMutableTreeNode>) targetNode.getNodeData().getUserObject();
                    DefaultMutableTreeNode sourceLinkNode;
                    DefaultMutableTreeNode targetLinkNode;

                    // check mapping
                    char existingRel = mapping.getRelation(sourceNode, targetNode);
                    if (IMappingElement.IDK == existingRel) {
                        // add the mapping
                        mapping.setRelation(sourceNode, targetNode, IMappingElement.EQUIVALENCE);
                        IMappingElement<INode> me = new MappingElement<INode>(sourceNode, targetNode, IMappingElement.EQUIVALENCE);
                        // add the link nodes
                        sourceLinkNode = new DefaultMutableTreeNode(me);
                        sourceLinkNodes.add(sourceLinkNode);
                        targetLinkNode = new DefaultMutableTreeNode(me);
                        targetLinkNodes.add(targetLinkNode);

                        // signal insertion
                        if (tSource.getModel() instanceof DefaultTreeModel
                                && tTarget.getModel() instanceof DefaultTreeModel) {
                            DefaultTreeModel dtmSource = (DefaultTreeModel) tSource.getModel();
                            DefaultTreeModel dtmTarget = (DefaultTreeModel) tTarget.getModel();
                            int sIdx = dtmSource.getIndexOfChild(sourceNode, sourceLinkNode);
                            int tIdx = dtmTarget.getIndexOfChild(targetNode, targetLinkNode);
                            dtmSource.nodesWereInserted(sourceNode, new int[]{sIdx});
                            dtmTarget.nodesWereInserted(targetNode, new int[]{tIdx});
                        }
                    } else {
                        // find them
                        sourceLinkNode = findLinkNode(sourceNode, targetNode, sourceLinkNodes, existingRel);
                        targetLinkNode = findLinkNode(sourceNode, targetNode, targetLinkNodes, existingRel);
                    }

                    sourcePath = sourcePath.pathByAddingChild(sourceLinkNode);
                    targetPath = targetPath.pathByAddingChild(targetLinkNode);
                    // start editing
                    if (null != lastFocusedTree) {
                        if (lastFocusedTree == tSource) {
                            tSource.scrollPathToVisible(sourcePath);
                            tSource.startEditingAtPath(sourcePath);
                        } else if (lastFocusedTree == tTarget) {
                            tTarget.scrollPathToVisible(targetPath);
                            tTarget.startEditingAtPath(targetPath);
                        }
                    }
                }
            }
        }

        private DefaultMutableTreeNode findLinkNode(INode sourceNode, INode targetNode, List<DefaultMutableTreeNode> linkNodes, char existingRel) {
            DefaultMutableTreeNode result = null;
            for (DefaultMutableTreeNode linkNode : linkNodes) {
                if (linkNode.getUserObject() instanceof IMappingElement) {
                    @SuppressWarnings("unchecked")
                    IMappingElement<INode> me = (IMappingElement<INode>) linkNode.getUserObject();
                    if (me.getSource() == sourceNode && me.getTarget() == targetNode && me.getRelation() == existingRel) {
                        result = linkNode;
                        break;
                    }
                }
            }
            return result;
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != source && 1 == tSource.getSelectionCount() && (tSource.getSelectionPath().getLastPathComponent() instanceof INode)
                    &&
                    null != target && 1 == tTarget.getSelectionCount() && (tTarget.getSelectionPath().getLastPathComponent() instanceof INode)
            );
        }
    }

    private class ActionEditDelete extends ActionTreeEdit implements Observer {

        public ActionEditDelete() {
            super("Delete");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
            putValue(Action.SHORT_DESCRIPTION, "Deletes a node or a link");
            putValue(Action.LONG_DESCRIPTION, "Deletes a node or a link");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            putValue(Action.SMALL_ICON, iconDeleteSmall);
            putValue(Action.LARGE_ICON_KEY, iconDeleteLarge);
        }

        public ActionEditDelete(JTree tree) {
            this();
            this.tree = tree;
            putValue(Action.ACCELERATOR_KEY, null);
        }

        @Override
        protected void doAction(JTree tree) {
            deleteNode(tree);
        }

        @Override
        public void setEnabled(JTree tree) {
            boolean result = null != tree
                    && 1 == tree.getSelectionCount()
                    && null != tree.getSelectionPath().getParentPath()
                    && null != tree.getSelectionPath().getParentPath().getLastPathComponent();

            // disable deletion for subs (...) nodes
            if (result) {
                if (tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                    result = dmtn.getUserObject() instanceof IMappingElement;
                }
            }

            setEnabled(result);
        }
    }

    private class ActionViewUncoalesce extends ActionTreeEdit implements Observer {
        public ActionViewUncoalesce() {
            super("Uncoalesce");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
            putValue(Action.SHORT_DESCRIPTION, "Uncoalesces a node");
            putValue(Action.LONG_DESCRIPTION, "Uncoalesces a node");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0));
            putValue(Action.SMALL_ICON, iconUncoalesceSmall);
            putValue(Action.LARGE_ICON_KEY, iconUncoalesceLarge);
        }

        public ActionViewUncoalesce(JTree tree) {
            this();
            this.tree = tree;
            putValue(Action.ACCELERATOR_KEY, null);
        }

        @Override
        protected void doAction(JTree tree) {
            uncoalesceNode(tree);
        }

        @Override
        public void setEnabled(JTree tree) {
            boolean result = null != tree
                    && 1 == tree.getSelectionCount()
                    && null != tree.getSelectionPath().getParentPath()
                    && null != tree.getSelectionPath().getParentPath().getLastPathComponent();

            if (result) {
                result = tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode;
                if (result) {
                    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                    result = dmtn.getUserObject() instanceof MappingTreeModel.Coalesce;
                }
            }

            setEnabled(result);
        }
    }

    private class ActionViewUncoalesceAll extends ActionTreeEdit implements Observer {
        public ActionViewUncoalesceAll() {
            super("Uncoalesce All");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(Action.SHORT_DESCRIPTION, "Uncoalesces all nodes");
            putValue(Action.LONG_DESCRIPTION, "Uncoalesces all nodes");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, InputEvent.ALT_DOWN_MASK));
        }

        public ActionViewUncoalesceAll(JTree tree) {
            this();
            this.tree = tree;
            putValue(Action.ACCELERATOR_KEY, null);
        }

        @Override
        protected void doAction(JTree tree) {
            uncoalesceTree(tree);
        }

        @Override
        public void setEnabled(JTree tree) {
            boolean result = null != tree
                    && tree.getModel() instanceof MappingTreeModel;

            if (result) {
                MappingTreeModel mtm = (MappingTreeModel) tree.getModel();
                result = mtm.hasCoalescedNode();
            }

            setEnabled(result);
        }
    }

    private class ActionMappingCreate extends AbstractAction implements Observer {
        public ActionMappingCreate() {
            super("Create");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(Action.SHORT_DESCRIPTION, "Creates Mapping");
            putValue(Action.LONG_DESCRIPTION, "Creates Mapping between Contexts");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (acMappingClose.isEnabled()) {
                acMappingClose.actionPerformed(actionEvent);
            }
            try {
                mapping = mm.online(source, target);
                createTree(source, tSource, mapping);
                createTree(target, tTarget, mapping);
                setChanged();
                notifyObservers();
            } catch (SMatchException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Error while creating a mapping between source and target contexts", e);
                    log.debug(e);
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != source && null != target
                    && source.getRoot().getNodeData().isSubtreePreprocessed()
                    && target.getRoot().getNodeData().isSubtreePreprocessed());
        }
    }

    private class ActionMappingOpen extends AbstractAction implements Observer {
        public ActionMappingOpen() {
            super("Open...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(Action.SHORT_DESCRIPTION, "Opens Mapping");
            putValue(Action.LONG_DESCRIPTION, "Opens Mapping Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentOpenSmall);
            putValue(Action.LARGE_ICON_KEY, documentOpenLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getMappingLoader().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showOpenDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                openMapping(file);
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != source && null != target && null != mm.getMappingLoader());
        }
    }

    private class ActionMappingClose extends AbstractAction implements Observer {
        public ActionMappingClose() {
            super("Close");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(Action.SHORT_DESCRIPTION, "Closes Mapping");
            putValue(Action.LONG_DESCRIPTION, "Closes Mapping Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            mapping = null;
            mappingLocation = null;
            createTree(source, tSource, mapping);
            createTree(target, tTarget, mapping);
            pnContexts.repaint();
            setChanged();
            notifyObservers();
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mapping);
        }
    }

    private class ActionMappingSave extends AbstractAction implements Observer {
        public ActionMappingSave() {
            super("Save");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(Action.SHORT_DESCRIPTION, "Saves Mapping");
            putValue(Action.LONG_DESCRIPTION, "Saves Mapping Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentSaveSmall);
            putValue(Action.LARGE_ICON_KEY, documentSaveLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (null == mappingLocation) {
                ff.setDescription(mm.getMappingRenderer().getDescription());
                fc.addChoosableFileFilter(ff);
                final int returnVal = fc.showSaveDialog(mainPanel);
                fc.removeChoosableFileFilter(ff);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    mappingLocation = file.getAbsolutePath();
                }
            }

            if (null != mappingLocation) {
                log.info("Saving mapping: " + mappingLocation);
                try {
                    mm.renderMapping(mapping, mappingLocation);
                } catch (SMatchException e) {
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Error while saving mapping", e);
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != mapping && null != mm.getMappingRenderer());
        }
    }

    private class ActionMappingSaveAs extends AbstractAction implements Observer {
        public ActionMappingSaveAs() {
            super("Save As...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
            putValue(Action.SHORT_DESCRIPTION, "Saves Mapping");
            putValue(Action.LONG_DESCRIPTION, "Saves Mapping Context");
            putValue(Action.SMALL_ICON, documentSaveAsSmall);
            putValue(Action.LARGE_ICON_KEY, documentSaveAsLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getMappingRenderer().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showSaveDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                mappingLocation = file.getAbsolutePath();
            }

            if (null != mappingLocation) {
                log.info("Saving mapping: " + mappingLocation);
                try {
                    mm.renderMapping(mapping, mappingLocation);
                } catch (SMatchException e) {
                    if (log.isEnabledFor(Level.ERROR)) {
                        log.error("Error while saving mapping", e);
                    }
                }
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != mapping && null != mm.getMappingRenderer());
        }
    }

    private class ActionViewClearLog extends AbstractAction {
        public ActionViewClearLog() {
            super("Clear Log");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
            putValue(Action.SHORT_DESCRIPTION, "Clears log");
            putValue(Action.LONG_DESCRIPTION, "Clears log window");
        }

        public void actionPerformed(ActionEvent actionEvent) {
            taLog.setText("");
        }
    }

    //listener for config files combobox
    private final ItemListener configCombolistener = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            if ((e.getSource() == cbConfig) && (e.getStateChange() == ItemEvent.SELECTED)) {
                if (null != mm) {
                    String configFile = (new File(CONF_FILE)).getParent() + File.separator + e.getItem();
                    try {
                        Properties config = new Properties();
                        config.load(new FileInputStream(configFileName));
                        // override from command line
                        config.putAll(commandProperties);
                        mm.setProperties(config);
                        setChanged();
                        notifyObservers();
                    } catch (ConfigurableException exc) {
                        if (log.isEnabledFor(Level.ERROR)) {
                            log.error("Error while loading configuration from " + configFile, exc);
                        }
                    } catch (FileNotFoundException e1) {
                        //TODO exc handling
                    } catch (IOException e1) {
                        //TODO exc handling
                    }
                }
            }
        }
    };

    private final FocusListener treeFocusListener = new FocusListener() {
        public void focusGained(FocusEvent e) {
            if (!e.isTemporary()) {
                final Component c = e.getComponent();
                if (c instanceof JTree) {
                    final JTree t = (JTree) c;
                    if (t == tSource || t == tTarget) {
                        lastFocusedTree = t;
                        t.addTreeSelectionListener(treeSelectionListener);
                        // fire the event for the first time
                        TreeSelectionEvent tse = new TreeSelectionEvent(t, t.getSelectionPath(), true, null, t.getSelectionPath());
                        treeSelectionListener.valueChanged(tse);
                    }
                }
            }
        }

        public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
                final Component c = e.getComponent();
                if (c instanceof JTree) {
                    final JTree t = (JTree) c;
                    if (t == tSource || t == tTarget) {
                        t.removeTreeSelectionListener(treeSelectionListener);
                    }
                }
            }
        }
    };

    private final TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            if (e.getSource() instanceof JTree) {
                TreePath p = e.getNewLeadSelectionPath();
                if (null != p) {
                    Object o = p.getLastPathComponent();
                    if (o instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
                        if (dmtn.getUserObject() instanceof IMappingElement) {
                            @SuppressWarnings("unchecked")
                            IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                            if (e.getSource() == tSource) {
                                adjustTargetTree(tSource, tTarget, spSource, spTarget, me.getSource(), me.getTarget());
                            } else if (e.getSource() == tTarget) {
                                adjustTargetTree(tTarget, tSource, spTarget, spSource, me.getTarget(), me.getSource());
                            }
                        }
                    }
                }
            }

            setChanged();
            notifyObservers();
        }

        private void adjustTargetTree(JTree tSource, JTree tTarget, JScrollPane spSource, JScrollPane spTarget, INode source, INode target) {
            if (tTarget.getModel() instanceof MappingTreeModel) {
                MappingTreeModel mtm = (MappingTreeModel) tTarget.getModel();
                mtm.uncoalesceParents(target);

                // construct path to root
                TreePath pp = createPathToRoot(target);

                tTarget.makeVisible(pp);
                tTarget.setSelectionPath(pp);
                tTarget.scrollPathToVisible(pp);

                // scroll to match vertical position
                if (1 == tSource.getSelectionCount() && 1 == tTarget.getSelectionCount()) {
                    int sourceSelRowIdx = tSource.getSelectionRows()[0];
                    int targetSelRowIdx = tTarget.getSelectionRows()[0];
                    Rectangle sr = tSource.getRowBounds(sourceSelRowIdx);
                    Rectangle tr = tTarget.getRowBounds(targetSelRowIdx);
                    Point sp = spSource.getViewport().getViewPosition();
                    Point tp = spTarget.getViewport().getViewPosition();
                    int delta = (tr.y - tp.y) - (sr.y - sp.y);
                    spTarget.getViewport().setViewPosition(new Point(tp.x, tp.y + delta));
                }

                spTarget.repaint();
            }
        }
    };

    private class MappingTreeCellRenderer extends DefaultTreeCellRenderer {
        public MappingTreeCellRenderer() {
            super();
            setLeafIcon(folderSmall);
            setClosedIcon(folderSmall);
            setOpenIcon(folderOpenSmall);
        }

        public Component getTreeCellRendererComponent(final JTree tree, final Object value,
                                                      final boolean sel,
                                                      final boolean expanded,
                                                      final boolean leaf, final int row,
                                                      final boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof INode) {
                INode node = (INode) value;
                if (0 == node.getChildCount()) {
                    setIcon(folderSmall);
                } else if (expanded) {
                    setIcon(folderOpenSmall);
                } else {
                    setIcon(folderSmall);
                }
            } else {
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
                    if (dmtn.getUserObject() instanceof IMappingElement) {
                        @SuppressWarnings("unchecked")
                        IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                        final char relation = mapping.getRelation(me.getSource(), me.getTarget());
                        if (tree == tSource) {
                            setText(me.getTarget().getNodeData().getName());
                            //TODO links with the same named target in different contexts - add tooltips
                            switch (relation) {
                                case IMappingElement.LESS_GENERAL: {
                                    setIcon(iconLG);
                                    break;
                                }
                                case IMappingElement.MORE_GENERAL: {
                                    setIcon(iconMG);
                                    break;
                                }
                            }
                        } else {
                            setText(me.getSource().getNodeData().getName());
                            //TODO links with the same named target in different contexts - add tooltips
                            switch (relation) {
                                case IMappingElement.LESS_GENERAL: {
                                    setIcon(iconMG);
                                    break;
                                }
                                case IMappingElement.MORE_GENERAL: {
                                    setIcon(iconLG);
                                    break;
                                }
                            }
                        }
                        switch (relation) {
                            case IMappingElement.EQUIVALENCE: {
                                setIcon(iconEQ);
                                break;
                            }
                            case IMappingElement.DISJOINT: {
                                setIcon(iconDJ);
                                break;
                            }
                        }
                    } else if (dmtn.getUserObject() instanceof MappingTreeModel.Coalesce) {
                        MappingTreeModel.Coalesce c = (MappingTreeModel.Coalesce) dmtn.getUserObject();
                        setText(ELLIPSIS);
                        setIcon(iconUncoalesceSmall);
                        StringBuilder tip = new StringBuilder();
                        for (int i = c.range.x; i <= c.range.y; i++) {
                            tip.append(c.parent.getChildAt(i).getNodeData().getName());
                            if (i < c.range.y) {
                                tip.append(", ");
                            }
                        }
                        if (50 < tip.length()) {
                            tip.delete(25, tip.length() - 25);
                            tip.insert(25, "...");
                        }
                        setToolTipText(tip.toString());
                    }
                }
            }

            return this;

        }

    }

    private final MappingTreeCellRenderer mappingTreeCellRenderer = new MappingTreeCellRenderer();

    private class CustomFileFilter extends javax.swing.filechooser.FileFilter {

        private String description;

        public String getDescription() {
            return description;
        }

        public boolean accept(File file) {
            String ext = getExtension(file);
            return null != description && null != ext && -1 < description.indexOf(ext);
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }

    private final CustomFileFilter ff = new CustomFileFilter();

    private class NodeTreeCellEditor extends DefaultTreeCellEditor {

        private final String NAME_EQ = "equivalent";
        private final String NAME_LG = "less general";
        private final String NAME_MG = "more general";
        private final String NAME_DJ = "disjoint";

        private String[] relStrings = {NAME_EQ, NAME_LG, NAME_MG, NAME_DJ};

        private final HashMap<Character, String> relationToDescription = new HashMap<Character, String>(4);
        private final HashMap<String, Character> descriptionToRelation = new HashMap<String, Character>(4);
        private final HashMap<String, Icon> descriptionToIcon = new HashMap<String, Icon>(4);

        private TreeCellEditor oldRealEditor;
        private TreeCellEditor comboEditor;
        private DefaultComboBox combo;

        private class DefaultComboBox extends JComboBox implements FocusListener {//lifted from DefaultTreeCellEditor

            class ComboBoxRenderer extends JLabel implements ListCellRenderer {

                public ComboBoxRenderer() {
                    setOpaque(true);
                    setVerticalAlignment(CENTER);
                }

                public Component getListCellRendererComponent(
                        JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus) {

                    String relDesc = (String) value;

                    if (isSelected) {
                        setBackground(list.getSelectionBackground());
                        setForeground(list.getSelectionForeground());
                    } else {
                        setBackground(list.getBackground());
                        setForeground(list.getForeground());
                    }

                    setIcon(descriptionToIcon.get(relDesc));
                    setText(relDesc);
                    setFont(list.getFont());

                    return this;
                }
            }

            protected Border border;

            public DefaultComboBox(Border border) {
                setBorder(border);
                addFocusListener(this);
                setRenderer(new ComboBoxRenderer());
            }

            public void setBorder(Border border) {
                super.setBorder(border);
                this.border = border;
            }

            public Border getBorder() {
                return border;
            }

            public Font getFont() {
                Font font = super.getFont();

                if (font instanceof FontUIResource) {
                    Container parent = getParent();

                    if (parent != null && parent.getFont() != null)
                        font = parent.getFont();
                }
                return font;
            }

            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();

                // If not font has been set, prefer the renderers height.
                if (NodeTreeCellEditor.this.renderer != null && NodeTreeCellEditor.this.getFont() == null) {
                    Dimension rSize = NodeTreeCellEditor.this.renderer.getPreferredSize();
                    size.height = rSize.height;
                }
                return size;
            }

            public void focusGained(FocusEvent e) {
                if (!e.isTemporary()) {
                    this.showPopup();
                }
            }

            public void focusLost(FocusEvent e) {
                //nop
            }
        }

        private class LinkCellEditor extends DefaultCellEditor {
            public LinkCellEditor(final JComboBox comboBox) {
                super(comboBox);
                comboBox.removeActionListener(delegate);
                delegate = new EditorDelegate() {
                    @Override
                    public void setValue(Object value) {
                        if (value instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
                            if (dmtn.getUserObject() instanceof IMappingElement) {
                                @SuppressWarnings("unchecked")
                                IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                                comboBox.setSelectedItem(relationToDescription.get(mapping.getRelation(me.getSource(), me.getTarget())));
                            }
                        }
                    }

                    @Override
                    public Object getCellEditorValue() {
                        char relation = IMappingElement.EQUIVALENCE;
                        Object oo = comboBox.getSelectedItem();
                        if (oo instanceof String) {
                            String relDescr = (String) oo;
                            relation = descriptionToRelation.get(relDescr);
                        }

                        return relation;
                    }

                    @Override
                    public boolean shouldSelectCell(EventObject anEvent) {
                        if (anEvent instanceof MouseEvent) {
                            MouseEvent e = (MouseEvent) anEvent;
                            return e.getID() != MouseEvent.MOUSE_DRAGGED;
                        }
                        return true;
                    }

                    @Override
                    public boolean stopCellEditing() {
                        if (comboBox.isEditable()) {
                            // Commit edited value.
                            comboBox.actionPerformed(new ActionEvent(LinkCellEditor.this, 0, ""));
                        }
                        boolean result = super.stopCellEditing();
                        if (tree == tSource) {
                            tTarget.repaint();
                        } else if (tree == tTarget) {
                            tSource.repaint();
                        }
                        return result;

                    }
                };
                comboBox.addActionListener(delegate);
            }

            @Override
            public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                        boolean isSelected,
                                                        boolean expanded,
                                                        boolean leaf, int row) {
                delegate.setValue(value);
                return editorComponent;
            }

        }

        public NodeTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
            relationToDescription.put(IMappingElement.EQUIVALENCE, NAME_EQ);
            relationToDescription.put(IMappingElement.LESS_GENERAL, NAME_LG);
            relationToDescription.put(IMappingElement.MORE_GENERAL, NAME_MG);
            relationToDescription.put(IMappingElement.DISJOINT, NAME_DJ);

            descriptionToRelation.put(NAME_EQ, IMappingElement.EQUIVALENCE);
            descriptionToRelation.put(NAME_LG, IMappingElement.LESS_GENERAL);
            descriptionToRelation.put(NAME_MG, IMappingElement.MORE_GENERAL);
            descriptionToRelation.put(NAME_DJ, IMappingElement.DISJOINT);

            descriptionToIcon.put(NAME_EQ, smallIconEQ);
            descriptionToIcon.put(NAME_LG, smallIconLG);
            descriptionToIcon.put(NAME_MG, smallIconMG);
            descriptionToIcon.put(NAME_DJ, smallIconDJ);

            comboEditor = createTreeCellComboEditor();
            oldRealEditor = realEditor;
        }

        private TreeCellEditor createTreeCellComboEditor() {
            Border aBorder = UIManager.getBorder("Tree.editorBorder");
            combo = new DefaultComboBox(aBorder);

            ComboBoxModel cbm = new DefaultComboBoxModel(relStrings);
            combo.setModel(cbm);
            LinkCellEditor editor = new LinkCellEditor(combo) {
                public boolean shouldSelectCell(EventObject event) {
                    return super.shouldSelectCell(event);
                }
            };

            editor.setClickCountToStart(1);
            return editor;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            if (null != event) {
                if (event.getSource() instanceof JTree) {
                    if (null != lastPath) {
                        Object o = lastPath.getLastPathComponent();
                        if (o instanceof INode) {
                            realEditor = oldRealEditor;
                        } else if (o instanceof DefaultMutableTreeNode) {
                            realEditor = comboEditor;
                        }
                    }
                }
            }

            boolean result = super.isCellEditable(event);

            if (result) {
                Object node = tree.getLastSelectedPathComponent();
                result = (null != node) && ((node instanceof INode) || (node instanceof DefaultMutableTreeNode));
            }
            return result;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                    boolean isSelected,
                                                    boolean expanded,
                                                    boolean leaf, int row) {
            if (value instanceof INode) {
                realEditor = oldRealEditor;
            } else if (value instanceof DefaultMutableTreeNode) {
                realEditor = comboEditor;
            }

            return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        }

        public void addCellEditorListener(CellEditorListener l) {
            comboEditor.addCellEditorListener(l);
            super.addCellEditorListener(l);
        }

        public void removeCellEditorListener(CellEditorListener l) {
            comboEditor.removeCellEditorListener(l);
            super.removeCellEditorListener(l);
        }

    }

    // GUI static elements
    private JPanel mainPanel;
    private JMenuBar mainMenu;
    private JTextArea taLog;
    private DefaultComboBoxModel cmConfigs;
    private JComboBox cbConfig;
    private JFileChooser fc;
    private JTree tSource;
    private JTree tTarget;
    private JSplitPane spnContexts;
    private JPanel pnContexts;
    private JScrollPane spSource;
    private JScrollPane spTarget;
    private JSplitPane spnContextsLog;


    // actions
    private Action acSourceCreate;
    private Action acSourceAddNode;
    private Action acSourceAddChildNode;
    private Action acSourceDelete;
    private Action acSourceUncoalesce;
    private Action acSourceUncoalesceAll;
    private Action acSourceOpen;
    private Action acSourcePreprocess;
    private Action acSourceClose;
    private Action acSourceSave;
    private Action acSourceSaveAs;

    private Action acTargetCreate;
    private Action acTargetAddNode;
    private Action acTargetAddChildNode;
    private Action acTargetDelete;
    private Action acTargetUncoalesce;
    private Action acTargetUncoalesceAll;
    private Action acTargetOpen;
    private Action acTargetPreprocess;
    private Action acTargetClose;
    private Action acTargetSave;
    private Action acTargetSaveAs;

    private Action acMappingCreate;
    private Action acMappingOpen;
    private Action acMappingClose;
    private Action acMappingSave;
    private Action acMappingSaveAs;

    private Action acEditAddNode;
    private Action acEditAddChildNode;
    private Action acEditAddLink;
    private Action acEditDelete;

    private Action acViewUncoalesce;
    private Action acViewUncoalesceAll;

    private void addNode(JTree tree) {
        Object o = tree.getSelectionPath().getLastPathComponent();
        if (o instanceof INode) {
            INode node = (INode) o;
            INode parent = node.getParent();
            TreeModel m = tree.getModel();
            if (m instanceof DefaultTreeModel) {
                DefaultTreeModel dtm = (DefaultTreeModel) m;
                int nodeIdx = parent.getChildIndex(node);
                INode child = parent.createChild();
                child.getNodeData().setName("New Node");
                parent.removeChild(child);
                parent.addChild(nodeIdx, child);
                dtm.nodesWereInserted(parent, new int[]{dtm.getIndexOfChild(parent, child)});
                TreePath p = createPathToRoot(child);
                tree.scrollPathToVisible(p);
                tree.setSelectionPath(p);
                tree.startEditingAtPath(p);
            }
        }
    }

    private void addChildNode(JTree tree) {
        Object o = tree.getSelectionPath().getLastPathComponent();
        if (o instanceof INode) {
            INode node = (INode) o;
            INode child = node.createChild();
            child.getNodeData().setName("New Node");
            TreeModel m = tree.getModel();
            if (m instanceof DefaultTreeModel) {
                DefaultTreeModel dtm = (DefaultTreeModel) m;
                dtm.nodesWereInserted(node, new int[]{dtm.getIndexOfChild(node, child)});
                TreePath p = createPathToRoot(child);
                tree.scrollPathToVisible(p);
                tree.setSelectionPath(p);
                tree.startEditingAtPath(p);
            }
        }
    }

    private void deleteNode(JTree tree) {
        Object o = tree.getSelectionPath().getLastPathComponent();
        TreePath parentPath = tree.getSelectionPath().getParentPath();
        // node
        if (o instanceof INode) {
            INode node = (INode) o;
            // remove all links from this node and any node below it
            removeLinks(tree, node);
            for (INode child : node.getDescendantsList()) {
                removeLinks(tree, child);
            }

            INode parent = node.getParent();
            TreeModel m = tree.getModel();
            if (m instanceof DefaultTreeModel) {
                DefaultTreeModel dtm = (DefaultTreeModel) m;
                int idx = dtm.getIndexOfChild(parent, node);
                parent.removeChild(node);
                dtm.nodesWereRemoved(parent, new int[]{idx}, new Object[]{node});
            }
        } else if (o instanceof DefaultMutableTreeNode) {
            // link
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
            if (dmtn.getUserObject() instanceof IMappingElement) {
                if (null != parentPath && parentPath.getLastPathComponent() instanceof INode) {
                    // remove from own tree
                    INode parent = (INode) parentPath.getLastPathComponent();
                    if (parent.getNodeData().getUserObject() instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) parent.getNodeData().getUserObject();
                        TreeModel m = tree.getModel();
                        if (m instanceof DefaultTreeModel) {
                            DefaultTreeModel dtm = (DefaultTreeModel) m;
                            int idx = dtm.getIndexOfChild(parent, dmtn);
                            linkNodes.remove(dmtn);
                            dtm.nodesWereRemoved(parent, new int[]{idx}, new Object[]{dmtn});
                        }
                    }

                    @SuppressWarnings("unchecked")
                    IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                    // remove from mapping
                    mapping.setRelation(me.getSource(), me.getTarget(), IMappingElement.IDK);
                    // remove link node from the target tree
                    removeLinkFromTargetTree(tree, me);
                }
            }
        }

        // select parent
        tree.setSelectionPath(parentPath);
        tree.scrollPathToVisible(parentPath);
    }

    private void removeLinks(final JTree tree, final INode node) {
        // does not delete the links themselves, because it is called from the deleteNode, which
        // deletes the container node anyway
        if (node.getNodeData().getUserObject() instanceof List) {
            @SuppressWarnings("unchecked")
            List<DefaultMutableTreeNode> linkNodes = (List<DefaultMutableTreeNode>) node.getNodeData().getUserObject();
            for (DefaultMutableTreeNode linkNode : linkNodes) {
                // delete from mapping and from target
                if (linkNode.getUserObject() instanceof IMappingElement) {
                    // remove from mapping
                    @SuppressWarnings("unchecked")
                    IMappingElement<INode> me = (IMappingElement<INode>) linkNode.getUserObject();
                    mapping.setRelation(me.getSource(), me.getTarget(), IMappingElement.IDK);
                    // remove link node from the target tree
                    removeLinkFromTargetTree(tree, me);
                }
            }
        }
    }

    private void removeLinkFromTargetTree(final JTree tree, final IMappingElement<INode> me) {
        JTree oppositeTree = getOppositeTree(tree);
        INode targetNode = getTargetNode(tree, me);
        if (targetNode.getNodeData().getUserObject() instanceof List) {
            @SuppressWarnings("unchecked")
            List<DefaultMutableTreeNode> targetLinkNodes = (List<DefaultMutableTreeNode>) targetNode.getNodeData().getUserObject();
            DefaultMutableTreeNode targetLinkNodeToDelete = null;
            for (DefaultMutableTreeNode targetLinkNode : targetLinkNodes) {
                // if this is the same link we're deleting in the source, delete it from the target too
                if (targetLinkNode.getUserObject() instanceof IMappingElement) {
                    @SuppressWarnings("unchecked")
                    IMappingElement<INode> targetME = (IMappingElement<INode>) targetLinkNode.getUserObject();
                    if (targetME.equals(me)) {
                        targetLinkNodeToDelete = targetLinkNode;
                        break;
                    }
                }
            }//for
            if (null != targetLinkNodeToDelete) {
                if (oppositeTree.getModel() instanceof MappingTreeModel) {
                    MappingTreeModel mtm = (MappingTreeModel) oppositeTree.getModel();
                    int idx = mtm.getIndexOfChild(targetNode, targetLinkNodeToDelete);
                    targetLinkNodes.remove(targetLinkNodeToDelete);
                    if (!mtm.isCoalescedInAnyParent(targetNode)) {
                        mtm.nodesWereRemoved(targetNode, new int[]{idx}, new Object[]{targetLinkNodeToDelete});
                    }
                }
            } else {
                log.error("Cannot find symmetric link while deleting");
            }
        }
    }

    private INode getTargetNode(JTree tree, IMappingElement<INode> me) {
        if (tree == tSource) {
            return me.getTarget();
        } else if (tree == tTarget) {
            return me.getSource();
        } else {
            return null;
        }
    }

    private JTree getOppositeTree(final JTree tree) {
        if (tree == tSource) {
            return tTarget;
        } else if (tree == tTarget) {
            return tSource;
        } else {
            return null;
        }
    }

    private void uncoalesceNode(JTree tree) {
        Object o = tree.getSelectionPath().getLastPathComponent();
        TreePath parentPath = tree.getSelectionPath().getParentPath();
        if (o instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
            if (dmtn.getUserObject() instanceof MappingTreeModel.Coalesce) {
                if (null != parentPath && parentPath.getLastPathComponent() instanceof INode) {
                    INode parent = (INode) parentPath.getLastPathComponent();
                    TreeModel m = tree.getModel();
                    if (m instanceof MappingTreeModel) {
                        MappingTreeModel mtm = (MappingTreeModel) m;
                        mtm.uncoalesce(parent);
                    }
                }
            }
        }

        // select parent
        tree.setSelectionPath(parentPath);
        tree.scrollPathToVisible(parentPath);
    }

    private void uncoalesceTree(JTree tree) {
        if (tree.getModel() instanceof MappingTreeModel) {
            MappingTreeModel mtm = (MappingTreeModel) tree.getModel();
            mtm.uncoalesceAll();
        }
    }

    private void openSource(File file) {
        log.info("Opening source: " + file.getAbsolutePath() + "");

        source = loadTree(file.getAbsolutePath());
        createTree(source, tSource, mapping);
        setChanged();
        notifyObservers();
    }

    private void openTarget(File file) {
        log.info("Opening target: " + file.getAbsolutePath() + "");

        target = loadTree(file.getAbsolutePath());
        createTree(target, tTarget, mapping);
        setChanged();
        notifyObservers();
    }

    private void openMapping(File file) {
        log.info("Opening mapping: " + file.getAbsolutePath() + "");

        try {
            mapping = mm.loadMapping(source, target, file.getAbsolutePath());
            createTree(source, tSource, mapping);
            createTree(target, tTarget, mapping);
            pnContexts.repaint();
            setChanged();
            notifyObservers();
        } catch (SMatchException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Error while loading a mapping", e);
            }
        }
    }

    private TreePath createPathToRoot(INode node) {
        Deque<INode> pathToRoot = new ArrayDeque<INode>();
        INode curNode = node;
        while (null != curNode) {
            pathToRoot.push(curNode);
            curNode = curNode.getParent();
        }
        TreePath pp = new TreePath(pathToRoot.pop());
        while (!pathToRoot.isEmpty()) {
            pp = pp.pathByAddingChild(pathToRoot.pop());
        }
        return pp;
    }

    private void buildMenu() {
        mainMenu = new JMenuBar();

        JMenu jmSource = new JMenu("Source");
        jmSource.setMnemonic('S');
        jmSource.add(acSourceCreate);
        jmSource.addSeparator();
        jmSource.add(acSourceAddNode);
        jmSource.add(acSourceAddChildNode);
        jmSource.add(acSourceDelete);
        jmSource.addSeparator();
        jmSource.add(acSourceUncoalesce);
        jmSource.add(acSourceUncoalesceAll);
        jmSource.addSeparator();
        jmSource.add(acSourceOpen);
        jmSource.add(acSourcePreprocess);
        jmSource.add(acSourceClose);
        jmSource.add(acSourceSave);
        jmSource.add(acSourceSaveAs);
        mainMenu.add(jmSource);

        JMenu jmTarget = new JMenu("Target");
        jmTarget.setMnemonic('T');
        jmTarget.add(acTargetCreate);
        jmTarget.addSeparator();
        jmTarget.add(acTargetAddNode);
        jmTarget.add(acTargetAddChildNode);
        jmTarget.add(acTargetDelete);
        jmTarget.addSeparator();
        jmTarget.add(acTargetUncoalesce);
        jmTarget.add(acTargetUncoalesceAll);
        jmTarget.addSeparator();
        jmTarget.add(acTargetOpen);
        jmTarget.add(acTargetPreprocess);
        jmTarget.add(acTargetClose);
        jmTarget.add(acTargetSave);
        jmTarget.add(acTargetSaveAs);
        mainMenu.add(jmTarget);

        JMenu jmMapping = new JMenu("Mapping");
        jmMapping.setMnemonic('M');
        jmMapping.add(acMappingCreate);
        jmMapping.add(acMappingOpen);
        jmMapping.add(acMappingClose);
        jmMapping.add(acMappingSave);
        jmMapping.add(acMappingSaveAs);
        mainMenu.add(jmMapping);

        JMenu jmEdit = new JMenu("Edit");
        jmEdit.setMnemonic('E');
        jmEdit.add(acEditAddNode);
        jmEdit.add(acEditAddChildNode);
        jmEdit.add(acEditAddLink);
        jmEdit.add(acEditDelete);
        mainMenu.add(jmEdit);

        JMenu jmView = new JMenu("View");
        jmMapping.setMnemonic('V');
        final Action acViewClearLog = new ActionViewClearLog();
        jmView.add(acViewUncoalesce);
        jmView.add(acViewUncoalesceAll);
        jmView.addSeparator();
        jmView.add(acViewClearLog);
        mainMenu.add(jmView);
    }

    private void buildStaticGUI() {
        acSourceCreate = new ActionSourceCreate();
        acSourceOpen = new ActionSourceOpen();
        acSourcePreprocess = new ActionSourcePreprocess();
        acSourceClose = new ActionSourceClose();
        acSourceSave = new ActionSourceSave();
        acSourceSaveAs = new ActionSourceSaveAs();

        acTargetCreate = new ActionTargetCreate();
        acTargetOpen = new ActionTargetOpen();
        acTargetPreprocess = new ActionTargetPreprocess();
        acTargetClose = new ActionTargetClose();
        acTargetSave = new ActionTargetSave();
        acTargetSaveAs = new ActionTargetSaveAs();

        acMappingCreate = new ActionMappingCreate();
        acMappingOpen = new ActionMappingOpen();
        acMappingClose = new ActionMappingClose();
        acMappingSave = new ActionMappingSave();
        acMappingSaveAs = new ActionMappingSaveAs();

        acEditAddNode = new ActionEditAddNode();
        acEditAddChildNode = new ActionEditAddChildNode();
        acEditAddLink = new ActionEditAddLink();
        acEditDelete = new ActionEditDelete();

        acViewUncoalesce = new ActionViewUncoalesce();
        acViewUncoalesceAll = new ActionViewUncoalesceAll();

        String layoutColumns = "fill:default:grow";
        String layoutRows = "top:d:noGrow,top:4dlu:noGrow,fill:max(d;100px):grow";

        FormLayout layout = new FormLayout(layoutColumns, layoutRows);
        //PanelBuilder builder = new PanelBuilder(layout, new FormDebugPanel());
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        //build main toolbar
        JToolBar tbMain = new JToolBar();
        tbMain.setFloatable(false);
        builder.add(tbMain, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel lbMapping = new JLabel();
        lbMapping.setText("Mapping:  ");
        tbMain.add(lbMapping);
        JButton btMappingOpen = new JButton(acMappingOpen);
        btMappingOpen.setHideActionText(true);
        JButton btMappingSave = new JButton(acMappingSave);
        btMappingSave.setHideActionText(true);
        tbMain.add(btMappingOpen);
        tbMain.add(btMappingSave);
        tbMain.addSeparator();
        JButton btEditAddLink = new JButton(acEditAddLink);
        btEditAddLink.setHideActionText(true);
        tbMain.add(btEditAddLink);
        final JLabel lbConfig = new JLabel();
        lbConfig.setText("    Config:  ");
        tbMain.add(lbConfig);
        cbConfig = new JComboBox();
        cmConfigs = new DefaultComboBoxModel();
        // read config files
        File f = new File(CONF_FILE);
        File configFolder = f.getParentFile();
        String[] configFiles = configFolder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".properties") && name.startsWith("s-match");
            }
        });
        for (String config : configFiles) {
            cmConfigs.addElement(config);
        }
        String configName = (new File(configFileName)).getName();
        int defConfigIndex = cmConfigs.getIndexOf(configName);
        if (-1 != defConfigIndex) {
            cmConfigs.setSelectedItem(cmConfigs.getElementAt(defConfigIndex));
        }
        cbConfig.setModel(cmConfigs);
        cbConfig.addItemListener(configCombolistener);
        tbMain.add(cbConfig);


        //build trees panel
        spnContextsLog = new JSplitPane();
        spnContextsLog.setContinuousLayout(true);
        spnContextsLog.setOrientation(JSplitPane.VERTICAL_SPLIT);
        spnContextsLog.setOneTouchExpandable(true);

        builder.add(spnContextsLog, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        spnContexts = new JSplitPane();


        pnContexts = new JPanel();
        pnContexts.setLayout(new FormLayout("fill:d:grow", "fill:d:grow"));

        spnContextsLog.setLeftComponent(pnContexts);
        pnContexts.add(spnContexts, cc.xy(1, 1));

        //build source
        JPanel pnSource = new JPanel();
        pnSource.setLayout(new FormLayout("fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:d:grow"));
        spnContexts.setLeftComponent(pnSource);
        JToolBar tbSource = new JToolBar();
        tbSource.setFloatable(false);
        pnSource.add(tbSource, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        JButton btSourceCreate = new JButton(acSourceCreate);
        btSourceCreate.setHideActionText(true);
        tbSource.add(btSourceCreate);
        tbSource.addSeparator();
        JButton btSourceOpen = new JButton(acSourceOpen);
        btSourceOpen.setHideActionText(true);
        JButton btSourceSave = new JButton(acSourceSave);
        btSourceSave.setHideActionText(true);
        tbSource.add(btSourceOpen);
        tbSource.add(btSourceSave);
        spSource = new JScrollPane();
        pnSource.add(spSource, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.FILL));
        tSource = new JTree(new DefaultMutableTreeNode(EMPTY_ROOT_NODE_LABEL));
        tSource.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        spSource.setViewportView(tSource);
        tbSource.addSeparator();
        acSourceAddNode = new ActionEditAddNode(tSource);
        acSourceAddChildNode = new ActionEditAddChildNode(tSource);
        acSourceDelete = new ActionEditDelete(tSource);
        JButton btSourceEditAddNode = new JButton(acSourceAddNode);
        JButton btSourceEditAddChildNode = new JButton(acSourceAddChildNode);
        JButton btSourceEditDelete = new JButton(acSourceDelete);
        btSourceEditAddNode.setHideActionText(true);
        btSourceEditAddChildNode.setHideActionText(true);
        btSourceEditDelete.setHideActionText(true);
        tbSource.add(btSourceEditAddNode);
        tbSource.add(btSourceEditAddChildNode);
        tbSource.add(btSourceEditDelete);
        acSourceUncoalesce = new ActionViewUncoalesce(tSource);
        acSourceUncoalesceAll = new ActionViewUncoalesceAll(tSource);
        JButton btSourceUncoalesce = new JButton(acSourceUncoalesce);
        btSourceUncoalesce.setHideActionText(true);
        tbSource.addSeparator();
        tbSource.add(btSourceUncoalesce);

        //build target
        JPanel pnTarget = new JPanel();
        pnTarget.setLayout(new FormLayout("fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:d:grow"));
        spnContexts.setRightComponent(pnTarget);
        JToolBar tbTarget = new JToolBar();
        tbTarget.setFloatable(false);
        pnTarget.add(tbTarget, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        JButton btTargetCreate = new JButton(acTargetCreate);
        btTargetCreate.setHideActionText(true);
        tbTarget.add(btTargetCreate);
        tbTarget.addSeparator();
        JButton btTargetOpen = new JButton(acTargetOpen);
        btTargetOpen.setHideActionText(true);
        JButton btTargetSave = new JButton(acTargetSave);
        btTargetSave.setHideActionText(true);
        tbTarget.add(btTargetOpen);
        tbTarget.add(btTargetSave);
        spTarget = new JScrollPane();
        pnTarget.add(spTarget, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.FILL));
        tTarget = new JTree(new DefaultMutableTreeNode(EMPTY_ROOT_NODE_LABEL));
        tTarget.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        spTarget.setViewportView(tTarget);
        tbTarget.addSeparator();
        acTargetAddNode = new ActionEditAddNode(tTarget);
        acTargetAddChildNode = new ActionEditAddChildNode(tTarget);
        acTargetDelete = new ActionEditDelete(tTarget);
        JButton btTargetEditAddNode = new JButton(acTargetAddNode);
        JButton btTargetEditAddChildNode = new JButton(acTargetAddChildNode);
        JButton btTargetEditDelete = new JButton(acTargetDelete);
        btTargetEditAddNode.setHideActionText(true);
        btTargetEditAddChildNode.setHideActionText(true);
        btTargetEditDelete.setHideActionText(true);
        tbTarget.add(btTargetEditAddNode);
        tbTarget.add(btTargetEditAddChildNode);
        tbTarget.add(btTargetEditDelete);
        acTargetUncoalesce = new ActionViewUncoalesce(tTarget);
        acTargetUncoalesceAll = new ActionViewUncoalesceAll(tTarget);
        JButton btTargetUncoalesce = new JButton(acTargetUncoalesce);
        btTargetUncoalesce.setHideActionText(true);
        tbTarget.addSeparator();
        tbTarget.add(btTargetUncoalesce);

        //build log panel
        JPanel pnLog = new JPanel();
        pnLog.setLayout(new FormLayout("fill:d:grow", "fill:d:grow"));
        spnContextsLog.setRightComponent(pnLog);
        JScrollPane spLog = new JScrollPane();
        pnLog.add(spLog, cc.xy(1, 1));
        taLog = new JTextArea();
        taLog.setEditable(false);
        spLog.setViewportView(taLog);
        //to make the JScrollPane wrapping the target component (e.g. JTextArea) automatically scroll down to show the latest log entries
        org.apache.log4j.lf5.viewer.LF5SwingUtils.makeVerticalScrollBarTrack(spLog);
        SMatchGUILog4Appender.setTextArea(taLog);

        //build status bar

        //FormDebugUtils.dumpAll(builder.getPanel());
        mainPanel = builder.getPanel();

        fc = new JFileChooser();

        buildMenu();

        Action[] actions = new Action[]{
                acSourceCreate, acSourceAddNode, acSourceAddChildNode, acSourceDelete, acSourceUncoalesce, acSourceUncoalesceAll,
                acSourceOpen, acSourcePreprocess, acSourceClose, acSourceSave, acSourceSaveAs,
                acTargetCreate, acTargetAddNode, acTargetAddChildNode, acTargetDelete, acTargetUncoalesce, acTargetUncoalesceAll,
                acTargetOpen, acTargetPreprocess, acTargetClose, acTargetSave, acTargetSaveAs,
                acMappingCreate, acMappingOpen, acMappingClose, acMappingSave, acMappingSaveAs,
                acEditAddNode, acEditAddChildNode, acEditAddLink, acEditDelete,
                acViewUncoalesce, acViewUncoalesceAll
        };

        for (Action a : actions) {
            if (a instanceof Observer) {
                this.addObserver((Observer) a);
            }
        }
    }

    private IContext loadTree(String fileName) {
        IContext context = null;
        try {
            context = mm.loadContext(fileName);
        } catch (SMatchException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Error while loading context from " + fileName, e);
            }
        }

        return context;
    }

    /**
     * Creates the tree from a context and a mapping.
     *
     * @param context context
     * @param jTree   a JTree
     * @param mapping a mapping
     */
    private void createTree(final IContext context, final JTree jTree, final IContextMapping<INode> mapping) {
        if (null == context) {
            String label;
            label = EMPTY_ROOT_NODE_LABEL;
            jTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(label)));
            jTree.removeTreeSelectionListener(treeSelectionListener);
            jTree.removeFocusListener(treeFocusListener);
            jTree.setCellRenderer(new DefaultTreeCellRenderer());
            jTree.setEditable(false);
        } else {
            TreeModel treeModel;
            clearUserObjects(context.getRoot());
            if (null == mapping) {
                treeModel = new NodeTreeModel(context.getRoot());
                jTree.addTreeSelectionListener(treeSelectionListener);
                jTree.addFocusListener(treeFocusListener);
            } else {
                treeModel = new MappingTreeModel(context.getRoot(), jTree == tSource, mapping);
                jTree.addFocusListener(treeFocusListener);
            }

            jTree.setModel(treeModel);

            //expand all the nodes initially
            if (context.getNodesList().size() < 60) {
                for (int i = 0; i < jTree.getRowCount(); i++) {
                    jTree.expandRow(i);
                }
            } else {
                //expand first level
                for (INode node : context.getRoot().getChildrenList()) {
                    TreePath p = new TreePath(context.getRoot());
                    p = p.pathByAddingChild(node);
                    jTree.expandPath(p);
                }
            }

            jTree.setCellEditor(new NodeTreeCellEditor(jTree, mappingTreeCellRenderer));
            jTree.setEditable(true);
            jTree.setCellRenderer(mappingTreeCellRenderer);
        }
        if (jTree == lastFocusedTree) {
            lastFocusedTree = null;
        }
    }

    private void clearUserObjects(INode root) {
        root.getNodeData().setUserObject(null);
        for (INode node : root.getDescendantsList()) {
            node.getNodeData().setUserObject(null);
        }
    }

    private void createMatchManager() {
        String configFile = new File(CONF_FILE).getParent() + File.separator + cmConfigs.getSelectedItem();
        log.info("Creating MatchManager with config: " + configFile);
        try {
            Properties config = new Properties();
            config.load(new FileInputStream(configFileName));

            if (log.isEnabledFor(Level.DEBUG)) {
                for (String k : commandProperties.stringPropertyNames()) {
                    log.debug("property override: " + k + "=" + commandProperties.getProperty(k));
                }
            }

            // override from command line
            config.putAll(commandProperties);

            mm = new MatchManager(config);
        } catch (SMatchException e) {
            log.info("Failed to create MatchManager: " + e);
        } catch (FileNotFoundException e) {
            //TODO exc handling
        } catch (IOException e) {
            //TODO exc handling
        }
        setChanged();
        notifyObservers();
    }

    private void applyLookAndFeel() {
        if (null != lookAndFeel) {
            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("ClassNotFoundException", e);
                }
            } catch (InstantiationException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("InstantiationException", e);
                }
            } catch (IllegalAccessException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("IllegalAccessException", e);
                }
            } catch (UnsupportedLookAndFeelException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("UnsupportedLookAndFeelException", e);
                }
            }
        }
    }

    private void showLFIs() {
        System.out.println("Available LookAndFeels:");
        for (UIManager.LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
            System.out.println(lfi.getName() + "=" + lfi.getClassName());
        }
    }

    private void readProperties() throws IOException {
        File configFile = new File(CONF_FILE);
        properties = new Properties();
        if (configFile.exists()) {
            log.info("Reading properties " + CONF_FILE);
            properties.load(new BufferedReader(new InputStreamReader(new FileInputStream(configFile))));
        }
        parseProperties();
    }

    private void parseProperties() {
        if (properties.containsKey("LookAndFeel")) {
            lookAndFeel = properties.getProperty("LookAndFeel");
        }
    }

    public void startup(String[] args) throws IOException {
        // initialize property file
        configFileName = MatchManager.DEFAULT_CONFIG_FILE_NAME;
        ArrayList<String> cleanArgs = new ArrayList<String>();
        for (String arg : args) {
            if (arg.startsWith(MatchManager.configFileCmdLineKey)) {
                configFileName = arg.substring(MatchManager.configFileCmdLineKey.length());
            } else {
                cleanArgs.add(arg);
            }
        }

        args = cleanArgs.toArray(new String[cleanArgs.size()]);
        cleanArgs.clear();

        // collect properties specified on the command line
        commandProperties = new Properties();
        for (String arg : args) {
            if (arg.startsWith(MatchManager.propCmdLineKey)) {
                String[] props = arg.substring(MatchManager.propCmdLineKey.length()).split("=");
                if (0 < props.length) {
                    String key = props[0];
                    String value = "";
                    if (1 < props.length) {
                        value = props[1];
                    }
                    commandProperties.put(key, value);
                }
            } else {
                cleanArgs.add(arg);
            }
        }

        args = cleanArgs.toArray(new String[cleanArgs.size()]);

        showLFIs();
        readProperties();
        applyLookAndFeel();
        buildStaticGUI();
        createMatchManager();

        JFrame frame = new JFrame("SMatch GUI");
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setLocation(100, 100);
        frame.setContentPane(mainPanel);
        //to check for matching in progress
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setJMenuBar(mainMenu);
        frame.addWindowListener(windowListener);
        frame.pack();

        spnContextsLog.setDividerLocation(.8);
        spnContexts.setDividerLocation(.5);

        //try to set an icon
        try {
            nl.ikarus.nxt.priv.imageio.icoreader.lib.ICOReaderSpi.registerIcoReader();
            System.setProperty("nl.ikarus.nxt.priv.imageio.icoreader.autoselect.icon", "true");
            ImageInputStream in = ImageIO.createImageInputStream(SMatchGUI.class.getResourceAsStream(MAIN_ICON_FILE));
            ArrayList<Image> icons = new ArrayList<Image>();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader r = readers.next();
                r.setInput(in);
                int nr = r.getNumImages(true);
                for (int i = 0; i < nr; i++) {
                    try {
                        icons.add(r.read(i));
                    } catch (Exception e) {
                        //silently fail
                    }
                }
                frame.setIconImages(icons);
            }
        } catch (Exception e) {
            //silently fail
        }

        //load the contexts and the mapping from the command line
        if (0 < args.length) {
            openSource(new File(args[0]));
            if (1 < args.length) {
                openTarget(new File(args[1]));
                if (2 < args.length) {
                    openMapping(new File(args[2]));
                }
            }
        }

        setChanged();
        notifyObservers();
        frame.setVisible(true);
    }

    private final WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            //TODO check matching in progress
            e.getWindow().dispose();
        }
    };

    public static void main(String[] args) throws IOException {
        SMatchGUI gui = new SMatchGUI();
        gui.startup(args);
    }
}