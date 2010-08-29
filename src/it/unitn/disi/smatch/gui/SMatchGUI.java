package it.unitn.disi.smatch.gui;

import com.ikayzo.swing.icon.JIconFile;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import it.unitn.disi.smatch.IMatchManager;
import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
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
import javax.swing.event.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;

/**
 * GUI for S-Match.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SMatchGUI extends Observable implements ComponentListener, AdjustmentListener, TreeExpansionListener, PropertyChangeListener {

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

    private static ImageIcon documentOpenSmall;
    private static ImageIcon documentOpenLarge;
    private static ImageIcon documentSaveSmall;
    private static ImageIcon documentSaveLarge;
    private static ImageIcon documentSaveAsSmall;
    private static ImageIcon documentSaveAsLarge;
    private static ImageIcon folderSmall;
    private static ImageIcon folderOpenSmall;
    private static ImageIcon iconDJ;
    private static ImageIcon iconEQ;
    private static ImageIcon iconMG;
    private static ImageIcon iconLG;

    private static final int SMALL_ICON_SIZE = 16;
    private static final int LARGE_ICON_SIZE = 32;


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

        icon = loadIconFile("/relations/equivalent");
        iconEQ = icon.getIcon(SMALL_ICON_SIZE);

        icon = loadIconFile("/relations/less-general");
        iconLG = icon.getIcon(SMALL_ICON_SIZE);

        icon = loadIconFile("/relations/more-general");
        iconMG = icon.getIcon(SMALL_ICON_SIZE);
    }

    private class ActionSourceOpen extends AbstractAction implements Observer {
        public ActionSourceOpen() {
            super("Open...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(Action.SHORT_DESCRIPTION, "Opens Source");
            putValue(Action.LONG_DESCRIPTION, "Opens Source Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentOpenSmall);
            putValue(Action.LARGE_ICON_KEY, documentOpenLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getContextLoader().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showOpenDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                openSource(file);
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != mm.getContextLoader());
        }
    }

    private class ActionSourcePreprocess extends AbstractAction implements Observer {
        public ActionSourcePreprocess() {
            super("Preprocess");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(Action.SHORT_DESCRIPTION, "Preprocesses Source");
            putValue(Action.LONG_DESCRIPTION, "Preprocesses Source Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                mm.offline(source);
                setChanged();
                notifyObservers();
            } catch (SMatchException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Error while preprocessing source context", e);
                }
            }
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
            acMappingClose.actionPerformed(actionEvent);
            source = null;
            sourceLocation = null;
            tSource.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Load source")));
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

    private class ActionTargetOpen extends AbstractAction implements Observer {
        public ActionTargetOpen() {
            super("Open...");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(Action.SHORT_DESCRIPTION, "Opens Target");
            putValue(Action.LONG_DESCRIPTION, "Opens Target Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            putValue(Action.SMALL_ICON, documentOpenSmall);
            putValue(Action.LARGE_ICON_KEY, documentOpenLarge);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            ff.setDescription(mm.getContextLoader().getDescription());
            fc.addChoosableFileFilter(ff);
            final int returnVal = fc.showOpenDialog(mainPanel);
            fc.removeChoosableFileFilter(ff);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                openTarget(file);
            }
        }

        public void update(Observable o, Object arg) {
            setEnabled(null != mm && null != mm.getContextLoader());
        }
    }

    private class ActionTargetPreprocess extends AbstractAction implements Observer {
        public ActionTargetPreprocess() {
            super("Preprocess");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(Action.SHORT_DESCRIPTION, "Preprocesses Target");
            putValue(Action.LONG_DESCRIPTION, "Preprocesses Target Context");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                mm.offline(target);
                setChanged();
                notifyObservers();
            } catch (SMatchException e) {
                if (log.isEnabledFor(Level.ERROR)) {
                    log.error("Error while preprocessing target context", e);
                }
            }
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
            acMappingClose.actionPerformed(actionEvent);
            target = null;
            targetLocation = null;
            tTarget.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Load target")));
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

    private class ActionMappingCreate extends AbstractAction implements Observer {
        public ActionMappingCreate() {
            super("Create");
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(Action.SHORT_DESCRIPTION, "Creates Mapping");
            putValue(Action.LONG_DESCRIPTION, "Creates Mapping between Contexts");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            acMappingClose.actionPerformed(actionEvent);
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
            TreePath p = e.getNewLeadSelectionPath();
            if (null != p) {
                Object o = p.getLastPathComponent();
                if (o instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
                    @SuppressWarnings("unchecked")
                    IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                    if (e.getSource() == tSource) {

                        // construct path root
                        TreePath pp = createPathToRoot(me.getTarget());

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

                    } else if (e.getSource() == tTarget) {
                        // construct path root
                        TreePath pp = createPathToRoot(me.getSource());

                        tSource.makeVisible(pp);
                        tSource.setSelectionPath(pp);
                        tSource.scrollPathToVisible(pp);

                        // scroll to match vertical position
                        if (1 == tSource.getSelectionCount() && 1 == tTarget.getSelectionCount()) {
                            int sourceSelRowIdx = tSource.getSelectionRows()[0];
                            int targetSelRowIdx = tTarget.getSelectionRows()[0];
                            Rectangle sr = tSource.getRowBounds(sourceSelRowIdx);
                            Rectangle tr = tTarget.getRowBounds(targetSelRowIdx);
                            Point sp = spSource.getViewport().getViewPosition();
                            Point tp = spTarget.getViewport().getViewPosition();
                            int delta = (sr.y - sp.y) - (tr.y - tp.y);
                            spSource.getViewport().setViewPosition(new Point(sp.x, sp.y + delta));
                        }
                    }
                }
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
                    @SuppressWarnings("unchecked")
                    IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                    if (tree == tSource) {
                        setText(me.getTarget().getNodeData().getName());
                        switch (mapping.getRelation(me.getSource(), me.getTarget())) {
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
                        switch (mapping.getRelation(me.getSource(), me.getTarget())) {
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
                    switch (mapping.getRelation(me.getSource(), me.getTarget())) {
                        case IMappingElement.EQUIVALENCE: {
                            setIcon(iconEQ);
                            break;
                        }
                        case IMappingElement.DISJOINT: {
                            setIcon(iconDJ);
                            break;
                        }
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

        public String getDescriptions() {
            return description;
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

        private final String NAME_EQ = "= equivalent";
        private final String NAME_LG = "< less general";
        private final String NAME_MG = "> more general";
        private final String NAME_DJ = "! disjoint";

        private final HashMap<Character, String> relationToDescription = new HashMap<Character, String>(4);
        private final HashMap<String, Character> descriptionToRelation = new HashMap<String, Character>(4);

        private TreeCellEditor oldRealEditor;
        private TreeCellEditor comboEditor;
        private DefaultComboBox combo;

        private class DefaultComboBox extends JComboBox implements FocusListener {//lifted from DefaultTreeCellEditor
            protected Border border;

            public DefaultComboBox(Border border) {
                setBorder(border);
                addFocusListener(this);
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
                            @SuppressWarnings("unchecked")
                            IMappingElement<INode> me = (IMappingElement<INode>) dmtn.getUserObject();
                            comboBox.setSelectedItem(relationToDescription.get(mapping.getRelation(me.getSource(), me.getTarget())));
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

            comboEditor = createTreeCellComboEditor();
            oldRealEditor = realEditor;
        }

        private TreeCellEditor createTreeCellComboEditor() {
            Border aBorder = UIManager.getBorder("Tree.editorBorder");
            combo = new DefaultComboBox(aBorder);

            ComboBoxModel cbm = new DefaultComboBoxModel(new String[] {NAME_EQ, NAME_LG, NAME_MG, NAME_DJ});
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
    private JToolBar tbSource;
    private JToolBar tbTarget;
    private JScrollPane spSource;
    private JScrollPane spTarget;
    private JSplitPane spnContextsLog;


    // actions
    private Action acSourceOpen = new ActionSourceOpen();
    private Action acSourcePreprocess = new ActionSourcePreprocess();
    private Action acSourceClose = new ActionSourceClose();
    private Action acSourceSave = new ActionSourceSave();
    private Action acSourceSaveAs = new ActionSourceSaveAs();

    private Action acTargetOpen = new ActionTargetOpen();
    private Action acTargetPreprocess = new ActionTargetPreprocess();
    private Action acTargetClose = new ActionTargetClose();
    private Action acTargetSave = new ActionTargetSave();
    private Action acTargetSaveAs = new ActionTargetSaveAs();

    private Action acMappingCreate = new ActionMappingCreate();
    private Action acMappingOpen = new ActionMappingOpen();
    private Action acMappingClose = new ActionMappingClose();
    private Action acMappingSave = new ActionMappingSave();
    private Action acMappingSaveAs = new ActionMappingSaveAs();

    private final Action[] actions = {
            acSourceOpen, acSourcePreprocess, acSourceClose, acSourceSave, acSourceSaveAs,
            acTargetOpen, acTargetPreprocess, acTargetClose, acTargetSave, acTargetSaveAs,
            acMappingCreate, acMappingOpen, acMappingClose, acMappingSave, acMappingSaveAs};

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
        jmSource.add(acSourceOpen);
        jmSource.add(acSourcePreprocess);
        jmSource.add(acSourceClose);
        jmSource.add(acSourceSave);
        jmSource.add(acSourceSaveAs);
        mainMenu.add(jmSource);

        JMenu jmTarget = new JMenu("Target");
        jmTarget.setMnemonic('T');
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

        JMenu jmView = new JMenu("View");
        jmMapping.setMnemonic('V');
        final Action acViewClearLog = new ActionViewClearLog();
        jmView.add(acViewClearLog);
        mainMenu.add(jmView);
    }

    private void buildStaticGUI() {
        JToolBar tbMain;
        JButton btMappingOpen;
        JButton btMappingSave;
        JScrollPane spLog;
        JPanel pnSource;
        JPanel pnTarget;
        JButton btSourceOpen;
        JButton btSourceSave;
        JButton btTargetOpen;
        JButton btTargetSave;
        JPanel pnLog;

        String layoutColumns = "fill:default:grow";
        String layoutRows = "top:d:noGrow,top:4dlu:noGrow,fill:max(d;100px):grow";

        FormLayout layout = new FormLayout(layoutColumns, layoutRows);
        //PanelBuilder builder = new PanelBuilder(layout, new FormDebugPanel());
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        //build main toolbar
        tbMain = new JToolBar();
        tbMain.setFloatable(false);
        builder.add(tbMain, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel lbMapping = new JLabel();
        lbMapping.setText("Mapping:  ");
        tbMain.add(lbMapping);
        btMappingOpen = new JButton(acMappingOpen);
        btMappingSave = new JButton(acMappingSave);
        tbMain.add(btMappingOpen);
        tbMain.add(btMappingSave);
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
        spnContextsLog.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

        builder.add(spnContextsLog, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        spnContexts = new JSplitPane();
        spnContexts.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);


        pnContexts = new JPanel();
        pnContexts.setLayout(new FormLayout("fill:d:grow", "fill:d:grow"));

        spnContextsLog.setLeftComponent(pnContexts);
        pnContexts.add(spnContexts, cc.xy(1, 1));

        //build source
        pnSource = new JPanel();
        pnSource.setLayout(new FormLayout("fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:d:grow"));
        spnContexts.setLeftComponent(pnSource);
        tbSource = new JToolBar();
        tbSource.setFloatable(false);
        pnSource.add(tbSource, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        btSourceOpen = new JButton(acSourceOpen);
        btSourceSave = new JButton(acSourceSave);
        tbSource.add(btSourceOpen);
        tbSource.add(btSourceSave);
        spSource = new JScrollPane();
        pnSource.add(spSource, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.FILL));
        tSource = new JTree(new DefaultMutableTreeNode("Load source"));
        tSource.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tSource.addTreeExpansionListener(this);
        spSource.getHorizontalScrollBar().addAdjustmentListener(this);
        spSource.getVerticalScrollBar().addAdjustmentListener(this);
        spSource.setViewportView(tSource);


        //build target
        pnTarget = new JPanel();
        pnTarget.setLayout(new FormLayout("fill:d:grow", "center:d:noGrow,top:4dlu:noGrow,center:d:grow"));
        spnContexts.setRightComponent(pnTarget);
        tbTarget = new JToolBar();
        tbTarget.setFloatable(false);
        pnTarget.add(tbTarget, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        btTargetOpen = new JButton(acTargetOpen);
        btTargetSave = new JButton(acTargetSave);
        tbTarget.add(btTargetOpen);
        tbTarget.add(btTargetSave);
        spTarget = new JScrollPane();
        pnTarget.add(spTarget, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.FILL));
        tTarget = new JTree(new DefaultMutableTreeNode("Load target"));
        tTarget.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tTarget.addTreeExpansionListener(this);
        spTarget.getHorizontalScrollBar().addAdjustmentListener(this);
        spTarget.getVerticalScrollBar().addAdjustmentListener(this);
        spTarget.setViewportView(tTarget);

        spnContexts.addComponentListener(this);

        //build log panel
        pnLog = new JPanel();
        pnLog.setLayout(new FormLayout("fill:d:grow", "fill:d:grow"));
        spnContextsLog.setRightComponent(pnLog);
        spLog = new JScrollPane();
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
            if (jTree == tSource) {
                label = "Load source";
            } else {
                label = "Load target";
            }
            jTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(label)));
            jTree.removeTreeSelectionListener(treeSelectionListener);
            jTree.removeFocusListener(treeFocusListener);
            jTree.setEditable(false);
        } else {
            TreeModel treeModel;
            clearUserObjects(context.getRoot());
            if (null == mapping) {
                treeModel = new NodeTreeModel(context.getRoot());
                jTree.removeTreeSelectionListener(treeSelectionListener);
                jTree.removeFocusListener(treeFocusListener);
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
        }
        jTree.setCellRenderer(mappingTreeCellRenderer);
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

    public void treeCollapsed(TreeExpansionEvent event) {
        pnContexts.repaint();
    }

    public void treeExpanded(TreeExpansionEvent event) {
        pnContexts.repaint();
    }

    public void componentMoved(ComponentEvent arg0) {
        pnContexts.repaint();
    }

    public void componentResized(ComponentEvent arg0) {
        pnContexts.repaint();
    }

    public void componentShown(ComponentEvent arg0) {
        pnContexts.repaint();
    }

    public void componentHidden(ComponentEvent arg0) {

    }

    public void adjustmentValueChanged(AdjustmentEvent arg0) {
        pnContexts.repaint();
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        pnContexts.repaint();
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