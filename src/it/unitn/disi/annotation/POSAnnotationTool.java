package it.unitn.disi.annotation;

import com.ikayzo.swing.icon.JIconFile;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import it.unitn.disi.annotation.data.INLPContext;
import it.unitn.disi.annotation.data.INLPNode;
import it.unitn.disi.annotation.loaders.context.INLPContextLoader;
import it.unitn.disi.annotation.renderers.context.INLPContextRenderer;
import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.utils.MiscUtils;
import it.unitn.disi.nlptools.NLPToolsConstants;
import it.unitn.disi.nlptools.NLPToolsException;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.Label;
import it.unitn.disi.nlptools.data.Token;
import it.unitn.disi.nlptools.pipelines.ILabelPipelineComponent;
import it.unitn.disi.smatch.data.trees.IBaseNode;
import it.unitn.disi.smatch.data.trees.IBaseNodeData;
import it.unitn.disi.smatch.gui.SMatchGUI;
import it.unitn.disi.smatch.loaders.context.ContextLoaderException;
import it.unitn.disi.smatch.renderers.context.ContextRendererException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

/**
 * POS annotation GUI for labels. Also corrects tokenization: use space to split tokens, use + to join tokens.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class POSAnnotationTool extends Configurable {

    private static Logger log;

    static {
        MiscUtils.configureLog4J();
        log = Logger.getLogger(POSAnnotationTool.class);
        JIconFile icon = SMatchGUI.loadIconFile(SMatchGUI.TANGO_ICONS_PATH + "actions/view-fullscreen");
        iconUncoalesceSmall = icon.getIcon(SMatchGUI.SMALL_ICON_SIZE);
    }

    private static final String CONF_FILE = ".." + File.separator + "conf" + File.separator + "annotation.properties";
    private static final String LOOK_AND_FEEL_KEY = "LookAndFeel";
    private String lookAndFeel = null;

    private static final String TAG_ORDER = "tagOrder";
    List<String> tagOrder = Arrays.asList(NLPToolsConstants.ARR_POS_ALL);

    private static final String CONTEXT_LOADER_KEY = "contextLoader";
    private INLPContextLoader contextLoader;

    private static final String CONTEXT_RENDERER_KEY = "contextRenderer";
    private INLPContextRenderer contextRenderer;

    private static final String TOKENIZER_KEY = "tokenizer";
    private ILabelPipelineComponent tokenizer;

    private static final String POS_TAGGER_KEY = "postagger";
    private ILabelPipelineComponent postagger;

    private String inputFileName = null;
    //allows loading other datasets to be used as label dictionaries. semicolon-separated list
    private static final String loadAlsoCmdLineToken = "-loadAlso=";

    private INLPContext context;
    private java.util.List<INLPNode> data;
    private boolean dataModified = false;
    private int curIndex = -1;

    //panels with interface for phrases to avoid flicker when going force-n-back
    private final ArrayList<JPanel> phrasePanels = new ArrayList<JPanel>();
    private JPanel curPhrasePanel = null;

    private String datasetSizeString = "";

    private static Icon iconUncoalesceSmall;

    private Action prevAction = new PrevAction("Prev", "Go to previous item", KeyEvent.VK_P);
    private Action nextAction = new NextAction("Next", "Go to next item", KeyEvent.VK_N);
    private Action nextNNPAction = new NextNNPAction("NNP && Next", "Mark all NNP and go to next item", KeyEvent.VK_X);
    private Action prevTagsAction = new PrevTagsAction("Prev Tags", "Load tags from previous item", KeyEvent.VK_T);

    //magics used in binding components for data elements
    //ILabel, binds phrase objects to interface objects
    private static final String MAGIC_LABEL = "LABEL";
    //IToken, binds token to panel
    private static final String MAGIC_TOKEN = "TOKEN";
    //panel -> token pos list
    private static final String MAGIC_TOKEN_POS_LIST = "TOKENPOS_";

    private static JPanel mainPanel;
    private static JButton btNext;
    private static JButton btNextNNP;
    private static JButton btPrevTags;
    private static JTextField lbLabel;
    private static JTextField lbPath;
    private static JScrollPane tokensScroll;
    private static JProgressBar progressBar;
    private static JTree tSource;
    private static JScrollPane spSource;

    //label -> tagged label:  token \t tag \t\t ...
    private static final HashMap<String, String> taggedPOS = new HashMap<String, String>();

    class NavAction extends AbstractAction {
        public NavAction(String text, String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            loadLabelAndPath();
            updateActions();
            loadPanel();
            updateProgressBar();
            focusNodeInATree(tSource, spSource, data.get(curIndex));
        }

        public void actionPerforming(ActionEvent e) {
        }
    }

    class PrevAction extends NavAction {
        public PrevAction(String text, String desc, Integer mnemonic) {
            super(text, desc, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerforming(e);
            curIndex--;
            super.actionPerformed(e);
        }
    }

    class NextAction extends NavAction {
        public NextAction(String text, String desc, Integer mnemonic) {
            super(text, desc, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerforming(e);

            //add previous one to cache
            ILabel curLabel;
            if (-1 != curIndex) {
                curLabel = data.get(curIndex).getNodeData().getLabel();
                if (null != curLabel) {
                    taggedPOS.put(curLabel.getText(), toTabText(curLabel));
                }
            }

            //next
            curIndex++;

            curLabel = data.get(curIndex).getNodeData().getLabel();
            if (null == curLabel) {
                //create label
                curLabel = new Label(data.get(curIndex).getNodeData().getName());
                data.get(curIndex).getNodeData().setLabel(curLabel);

                //process label
                try {
                    tokenizer.process(curLabel);
                    postagger.process(curLabel);
                } catch (PipelineComponentException exc) {
                    log.error(exc.getMessage(), exc);
                }
            }

            //load annotation from cache into a new label
            String label = curLabel.getText();
            String tabText = taggedPOS.get(label);
            while (null != tabText && curIndex < (data.size() - 1)) {
                fromTabText(data.get(curIndex).getNodeData().getLabel(), tabText);
                log.info("Skipping: " + label);
                super.actionPerformed(e);
                curIndex++;

                curLabel = data.get(curIndex).getNodeData().getLabel();
                if (null == curLabel) {
                    //create label
                    curLabel = new Label(data.get(curIndex).getNodeData().getName());
                    data.get(curIndex).getNodeData().setLabel(curLabel);
                    //process label
                    try {
                        tokenizer.process(curLabel);
                        postagger.process(curLabel);
                    } catch (PipelineComponentException exc) {
                        log.error(exc.getMessage(), exc);
                    }
                }
                label = data.get(curIndex).getNodeData().getLabel().getText();
                tabText = taggedPOS.get(label);
            }

            super.actionPerformed(e);
        }
    }

    private final MouseListener treeMouseListener = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JTree tree = tSource;
                boolean result = null != tree
                        && 1 == tree.getSelectionCount()
                        && null != tree.getSelectionPath().getParentPath()
                        && null != tree.getSelectionPath().getParentPath().getLastPathComponent();

                if (result) {
                    result = tree.getSelectionPath().getLastPathComponent() instanceof DefaultMutableTreeNode;
                    if (result) {
                        DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                        result = dmtn.getUserObject() instanceof SMatchGUI.BaseCoalesceTreeModel.Coalesce;
                    }
                }

                if (result) {
                    SMatchGUI.uncoalesceNode(tree);
                }
            }
        }
    };

    /**
     * Tab text format: token \t pos \t\t token \t pos \t\t ...
     *
     * @param label label instance
     * @return tab text
     */
    private String toTabText(ILabel label) {
        StringBuilder result = new StringBuilder();
        if (0 < label.getTokens().size()) {
            for (IToken token : label.getTokens()) {
                result.append(token.getText()).append("\t").append(token.getPOSTag()).append("\t\t");
            }
        }
        return result.substring(0, result.length() - 2);
    }

    private void fromTabText(ILabel label, String tabText) {
        String[] tokens = tabText.split("\t\t");
        label.setTokens(new ArrayList<IToken>(tokens.length));
        for (String token : tokens) {
            String[] tt = token.split("\t");
            IToken t = new Token(tt[0]);
            t.setPOSTag(tt[1]);
            label.getTokens().add(t);
        }
    }

    class NextNNPAction extends NextAction {
        public NextNNPAction(String text, String desc, Integer mnemonic) {
            super(text, desc, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerforming(e);

            //set NNP
            ILabel curLabel = data.get(curIndex).getNodeData().getLabel();
            if (null != curLabel) {
                for (IToken token : curLabel.getTokens()) {
                    token.setPOSTag(NLPToolsConstants.PROPER_NOUN_SING);
                }
                modify();
                loadLabelAndPath();
            }

            super.actionPerformed(e);
        }
    }

    private void modify() {
        dataModified = true;
        updateActions();
    }

    class PrevTagsAction extends AbstractAction {
        public PrevTagsAction(String text, String desc, Integer mnemonic) {
            super(text);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            //set prev tags
            copyTags(data.get(curIndex - 1).getNodeData().getLabel(), data.get(curIndex).getNodeData().getLabel());
            loadLabelAndPath();
            loadPanel();
        }

        public void actionPerforming(ActionEvent e) {
        }
    }

    private class CoalesceTreeCellRenderer extends DefaultTreeCellRenderer {
        public CoalesceTreeCellRenderer() {
            super();
        }

        public Component getTreeCellRendererComponent(final JTree tree, final Object value,
                                                      final boolean sel,
                                                      final boolean expanded,
                                                      final boolean leaf, final int row,
                                                      final boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setToolTipText("");
            if (value instanceof INLPNode) {
                INLPNode node = (INLPNode) value;
                ILabel label = node.getNodeData().getLabel();
                if (null != label) {
                    String posPattern = getPOSPattern(label);
                    setToolTipText(posPattern);
                    //String newText = node.getNodeData().getName() + " (" + posPattern + ")";
                    //setText(newText);
                }
            } else
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
                if (dmtn.getUserObject() instanceof SMatchGUI.BaseCoalesceTreeModel.Coalesce) {
                    SMatchGUI.BaseCoalesceTreeModel.Coalesce c = (SMatchGUI.BaseCoalesceTreeModel.Coalesce) dmtn.getUserObject();
                    setText(SMatchGUI.ELLIPSIS);
                    setIcon(iconUncoalesceSmall);
                    StringBuilder tip = new StringBuilder();
                    for (int i = c.range.x; i <= c.range.y; i++) {
                        if (i < c.parent.getChildCount()) {
                            tip.append(c.parent.getChildAt(i).getNodeData().getName());
                            if (i < c.range.y) {
                                tip.append(", ");
                            }
                        }
                    }
                    if (100 < tip.length()) {
                        tip.delete(50, tip.length() - 50);
                        tip.insert(50, "...");
                    }
                    setToolTipText(tip.toString());
                }
            }

            return this;
        }

    }

    private String getPOSPattern(ILabel label) {
        StringBuilder result = new StringBuilder();
        if (0 < label.getTokens().size()) {
            for (IToken token : label.getTokens()) {
                result.append(token.getPOSTag()).append(" ");
            }
            return result.substring(0, result.length() - 1);
        }
        return result.toString();
    }

    private final CoalesceTreeCellRenderer coalesceTreeCellRenderer = new CoalesceTreeCellRenderer();


    private void copyTags(ILabel source, ILabel target) {
        if (null != source && null != target) {
            if (source.getTokens().size() == target.getTokens().size()) {
                for (int i = 0; i < source.getTokens().size(); i++) {
                    target.getTokens().get(i).setPOSTag(source.getTokens().get(i).getPOSTag());
                }
            }
        }
    }

    private void loadPanel() {
        curPhrasePanel = null;
        //get new one if there is one
        if ((-1 < curIndex) && (curIndex < phrasePanels.size())) {
            curPhrasePanel = phrasePanels.get(curIndex);
        }

        //or create new one
        if (null == curPhrasePanel) {
            createCurrentPhrasePanel();
        }

        //update
        if (null != curPhrasePanel) {
            //update token pos
            updateTokenPOS(curPhrasePanel, data.get(curIndex).getNodeData().getLabel());

            tokensScroll.setViewportView(curPhrasePanel);
            tokensScroll.repaint();
        }
    }

    private void updateTokenPOS(JPanel curPhrasePanel, ILabel phrase) {
        java.util.List<IToken> tokens;
        tokens = phrase.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            IToken token = tokens.get(i);
            Object obj = curPhrasePanel.getClientProperty(MAGIC_TOKEN_POS_LIST + Integer.toString(i));
            if (null != obj && obj instanceof JList) {
                JList list = (JList) obj;
                //select current POS tag
                list.setSelectedValue(token.getPOSTag(), true);
                updatePOSListToolTip(list, token);
            }
        }
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading configuration...");
        }
        Properties oldProperties = new Properties();
        oldProperties.putAll(properties);
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(LOOK_AND_FEEL_KEY)) {
                lookAndFeel = newProperties.getProperty(LOOK_AND_FEEL_KEY);
            }

            if (newProperties.containsKey(TAG_ORDER)) {
                String order = newProperties.getProperty(TAG_ORDER);

                String[] tags = order.split("\t");
                tagOrder = new ArrayList<String>(Arrays.asList(tags));
                for (String t : NLPToolsConstants.ARR_POS_ALL) {
                    if (!tagOrder.contains(t)) {
                        tagOrder.add(t);
                    }
                }
            }

            contextLoader = (INLPContextLoader) configureComponent(contextLoader, oldProperties, newProperties, "context loader", CONTEXT_LOADER_KEY, INLPContextLoader.class);
            contextRenderer = (INLPContextRenderer) configureComponent(contextRenderer, oldProperties, newProperties, "context renderer", CONTEXT_RENDERER_KEY, INLPContextRenderer.class);
            tokenizer = (ILabelPipelineComponent) configureComponent(tokenizer, oldProperties, newProperties, "tokenizer", TOKENIZER_KEY, ILabelPipelineComponent.class);
            postagger = (ILabelPipelineComponent) configureComponent(postagger, oldProperties, newProperties, "POS tagger", POS_TAGGER_KEY, ILabelPipelineComponent.class);
        }
        return result;
    }

    private void createCurrentPhrasePanel() {
        try {
            curPhrasePanel = buildPhrasePanel(data.get(curIndex).getNodeData().getLabel());
        } catch (NLPToolsException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("NLPToolsException: ", e);
            }
            curPhrasePanel = null;
        } catch (RemoteException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("RemoteException: ", e);
            }
            curPhrasePanel = null;
        }
        if ((-1 < curIndex) && (curIndex < phrasePanels.size())) {
            phrasePanels.set(curIndex, curPhrasePanel);
        } else {
            phrasePanels.add(curIndex, curPhrasePanel);
        }

        //update
        tokensScroll.setViewportView(curPhrasePanel);
        tokensScroll.repaint();
    }

    private JPanel buildPhrasePanel(ILabel curPhrase) throws NLPToolsException, RemoteException {
        JPanel result = null;
        //http://java.sun.com/docs/books/tutorial/uiswing/misc/keybinding.html
        //TODO: hotkey for each token: 1,2,3,4...9,0  ?A,B,C?
        String layoutRows = "default, 4dlu, fill:min:grow";//row for tokens, row for POS tags
        String layoutColumns = "";

        //create a column for each token
        java.util.List<IToken> tokens = curPhrase.getTokens();
        if (0 < tokens.size()) {
            for (IToken token : tokens) {
                layoutColumns = layoutColumns + "fill:default, 4dlu, ";
            }
            layoutColumns = layoutColumns.substring(0, layoutColumns.length() - 2);

            FormLayout layout = new FormLayout(layoutColumns, layoutRows);
            //PanelBuilder builder = new PanelBuilder(layout, new FormDebugPanel());
            PanelBuilder builder = new PanelBuilder(layout);
            CellConstraints cc = new CellConstraints();

            //later bind components via putClientProperty
            for (int i = 0; i < tokens.size(); i++) {
                builder.add(buildTokenTextField(curPhrase, tokens.get(i)), cc.xy(1 + 2 * i, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

                JScrollPane listScroll = new JScrollPane();
                listScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                JList list = buildTokenPOSList(curPhrase, tokens.get(i));
                builder.getPanel().putClientProperty(MAGIC_TOKEN_POS_LIST + Integer.toString(i), list);
                listScroll.setViewportView(list);
                builder.add(listScroll, cc.xy(1 + 2 * i, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
            }

            //FormDebugUtils.dumpAll(builder.getPanel());
            result = builder.getPanel();
        }
        return result;
    }

    private JTextField buildTokenTextField(ILabel phrase, IToken token) {
        JTextField textField = new JTextField();
        textField.setHorizontalAlignment(JTextField.TRAILING);
        textField.setText(token.getText());
        //for listeners
        //split on space and on dblclick
        textField.putClientProperty(MAGIC_TOKEN, token);
        textField.putClientProperty(MAGIC_LABEL, phrase);
        //splits token on space typed
        textField.addKeyListener(tokenTextFieldKeyTyped);
        //updates token on enter typed
        textField.addKeyListener(tokenTextFieldKeyReleased);
        //splits token on dbl click
        //does not work - work selectAll
        //textField.addMouseListener(tokenTextFieldMouseClick);
        return textField;
    }

    private final KeyAdapter tokenTextFieldKeyTyped = new KeyAdapter() {
        public void keyTyped(KeyEvent e) {
            if (' ' == e.getKeyChar()) {
                super.keyTyped(e);
                processTokenSplittingKeyEvent(e);
            } else {
                if ('+' == e.getKeyChar()) {
                    super.keyTyped(e);
                    processTokenJoinKeyEvent(e);
                }
                //now we allow token editing...
                //e.consume();
                super.keyTyped(e);
            }
        }
    };

    private final KeyAdapter tokenTextFieldKeyReleased = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                super.keyTyped(e);
                processTokenUpdateKeyEvent(e);
            } else {
                //now we allow token editing...
                //e.consume();
                super.keyTyped(e);
            }
        }
    };

    private void processTokenUpdateKeyEvent(KeyEvent e) {
        Object mayBeTextField = e.getSource();
        if (mayBeTextField instanceof JTextField) {
            JTextField textField = (JTextField) mayBeTextField;
            Object mayBeNLToken = textField.getClientProperty(MAGIC_TOKEN);
            if (mayBeNLToken instanceof IToken) {
                Object mayBeNLPhrase = textField.getClientProperty(MAGIC_LABEL);
                if (mayBeNLPhrase instanceof ILabel) {
                    updateToken((ILabel) mayBeNLPhrase, (IToken) mayBeNLToken, textField.getText());
                }
            }
        }
    }

    private void processTokenSplittingKeyEvent(KeyEvent e) {
        Object mayBeTextField = e.getSource();
        if (mayBeTextField instanceof JTextField) {
            JTextField textField = (JTextField) mayBeTextField;
            Object mayBeNLToken = textField.getClientProperty(MAGIC_TOKEN);
            if (mayBeNLToken instanceof IToken) {
                Object mayBeNLPhrase = textField.getClientProperty(MAGIC_LABEL);
                if (mayBeNLPhrase instanceof ILabel) {
                    splitToken((ILabel) mayBeNLPhrase, (IToken) mayBeNLToken, textField.getCaretPosition());
                }
            }
        }
    }

    private void processTokenJoinKeyEvent(KeyEvent e) {
        Object mayBeTextField = e.getSource();
        if (mayBeTextField instanceof JTextField) {
            JTextField textField = (JTextField) mayBeTextField;
            Object mayBeNLToken = textField.getClientProperty(MAGIC_TOKEN);
            if (mayBeNLToken instanceof IToken) {
                Object mayBeNLPhrase = textField.getClientProperty(MAGIC_LABEL);
                if (mayBeNLPhrase instanceof ILabel) {
                    joinToken((ILabel) mayBeNLPhrase, (IToken) mayBeNLToken, textField.getText());
                }
            }
        }
    }

    private void splitToken(ILabel label, IToken token, int position) {
        String stringToken = token.getText();
        if (0 < position && position < stringToken.length()) {
            //will insert new tokens here
            int tokenIndex = label.getTokens().indexOf(token);

            IToken firstToken = new Token(stringToken.substring(0, position).trim());
            IToken secondToken = new Token(stringToken.substring(position, stringToken.length()).trim());

            //bye bye
            label.getTokens().remove(token);

            label.getTokens().add(tokenIndex, secondToken);
            label.getTokens().add(tokenIndex, firstToken);

            try {
                postagger.process(label);
            } catch (PipelineComponentException e) {
                log.error(e.getMessage(), e);
            }

            //updates current panel to make controls for new tokens
            createCurrentPhrasePanel();
            loadLabelAndPath();

            modify();
        }
    }

    private void joinToken(ILabel label, IToken token, String updatedTokenText) {
        int tokenIndex = label.getTokens().indexOf(token);
        java.util.List<IToken> tokens = label.getTokens();
        if (tokenIndex < tokens.size() - 1) {
            String newTokenText = updatedTokenText + tokens.get(tokenIndex + 1).getText();

            //current one
            label.getTokens().remove(token);
            //next one
            label.getTokens().remove(tokens.get(tokenIndex));

            //add new one in place of original
            label.getTokens().add(tokenIndex, new Token(newTokenText));

            try {
                postagger.process(label);
            } catch (PipelineComponentException e) {
                log.error(e.getMessage(), e);
            }

            //updates current panel to make controls for new tokens
            createCurrentPhrasePanel();
            loadLabelAndPath();

            modify();
        }
    }

    private void updateToken(ILabel label, IToken token, String updatedTokenText) {
        int tokenIndex = label.getTokens().indexOf(token);
        //current one
        label.getTokens().remove(token);

        //add new one in place of original
        label.getTokens().add(tokenIndex, new Token(updatedTokenText));

        try {
            postagger.process(label);
        } catch (PipelineComponentException e) {
            log.error(e.getMessage(), e);
        }

        //updates current panel to make controls for new tokens
        createCurrentPhrasePanel();
        loadLabelAndPath();

        modify();
    }

    private JList buildTokenPOSList(ILabel label, IToken token) {
        JList posList = new JList();
        posList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultListModel model = new DefaultListModel();
        for (String pos : tagOrder) {
            model.addElement(pos);
        }
        //for empty tags. should be encountered rarely
        model.addElement("");

        posList.setModel(model);

        //select current POS tag
        int idx = model.indexOf(token.getPOSTag());
        if (-1 != idx) {
            posList.setSelectedIndex(idx);
            posList.ensureIndexIsVisible(idx);
        } else {
            if (log.isEnabledFor(Level.WARN)) {
                log.warn("buildTokenPOSList(ILabel label, IToken token): POS tag not found. Token: " + token.getText() + ". POS: " + token.getPOSTag());
            }
        }
        updatePOSListToolTip(posList, token);

        //token and label for future reference in listener
        posList.putClientProperty(MAGIC_TOKEN, token);
        posList.putClientProperty(MAGIC_LABEL, label);

        //hook listener
        posList.addListSelectionListener(tokenPOSListSelectionListener);
        posList.addMouseListener(tokenPOSListMouseListener);
        posList.addMouseWheelListener(tokenPOSListMouseWheelListener);
        return posList;
    }

    private static void updatePOSListToolTip(JList posCombo, IToken nlToken) {
        String hint = NLPToolsConstants.posDescriptions.get(nlToken.getPOSTag());
        posCombo.setToolTipText(hint);
    }

    //listener for token pos combobox
    private ListSelectionListener tokenPOSListSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() instanceof JList) {
                JList source = (JList) e.getSource();
                IToken token = (IToken) source.getClientProperty(MAGIC_TOKEN);
                if (source.getSelectedValue() instanceof String) {
                    String tag = (String) source.getSelectedValue();
                    token.setPOSTag(tag);
                    updatePOSListToolTip(source, token);

                    //update cache, useful on correction
                    ILabel label = (ILabel) source.getClientProperty(MAGIC_LABEL);
                    taggedPOS.remove(label.getText());
                    taggedPOS.put(label.getText(), toTabText(label));

                    modify();
                }
            }
        }
    };

    private static final MouseListener tokenPOSListMouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (2 == e.getClickCount()) {
                btNext.doClick();
            }
        }
    };

    private static final MouseWheelListener tokenPOSListMouseWheelListener = new MouseWheelListener() {
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getSource() instanceof JList) {
                JList source = (JList) e.getSource();
                int idx = source.getSelectedIndex();
                idx = idx + e.getWheelRotation();
                if (-1 < idx && idx < source.getModel().getSize()) {
                    source.setSelectedIndex(idx);
                    source.ensureIndexIsVisible(idx);
                }
            }
        }
    };

    private void updateProgressBar() {
        if (-1 < curIndex) {
            progressBar.setValue(curIndex);
            progressBar.setToolTipText("Item " + Integer.toString(curIndex) + " of " + datasetSizeString);
        }
    }

    public POSAnnotationTool(String inputFile) {
        inputFileName = inputFile;
    }

    private void loadLabelAndPath() {
        String label = "";
        String path = "";
        if (-1 != curIndex) {
            //label = curDataItem.getLabel();
            path = getPathToRoot(data.get(curIndex));
            label = data.get(curIndex).getNodeData().getLabel().getText();
        }

        lbLabel.setText(label);
        lbLabel.setToolTipText(label);
        lbPath.setText(path);
        lbPath.setToolTipText(path);
    }

    private String getPathToRoot(INLPNode node) {
        StringBuilder result = new StringBuilder();
        java.util.List<INLPNode> ancestors = node.getAncestorsList();
        for (int i = ancestors.size() - 1; i >= 0; i--) {
            result.append(ancestors.get(i).getNodeData().getLabel().getText()).append("/");
        }
        return result.toString();
    }

    private void updateActions() {
        prevAction.setEnabled(0 < curIndex);
        prevTagsAction.setEnabled(0 < curIndex);
        nextAction.setEnabled(curIndex < data.size() - 1);
        nextNNPAction.setEnabled(curIndex < data.size() - 1);
    }

    private void clearUserObjects(INLPNode root) {
        root.getNodeData().setUserObject(null);
        for (INLPNode node : root.getDescendantsList()) {
            node.getNodeData().setUserObject(null);
        }
    }

    /**
     * Creates the tree from a context and a mapping.
     *
     * @param context context
     * @param jTree   a JTree
     */
    private void createTree(final INLPContext context, final JTree jTree) {
        TreeModel treeModel;
        clearUserObjects(context.getRoot());
        treeModel = new SMatchGUI.BaseCoalesceTreeModel(context.getRoot());
        jTree.setModel(treeModel);

        //expand first level
        TreePath p = new TreePath(context.getRoot());
        jTree.expandPath(p);
        jTree.setEditable(false);
    }

    @SuppressWarnings("unchecked")
    public void focusNodeInATree(JTree tTarget, JScrollPane spTarget, IBaseNode target) {
        if (tTarget.getModel() instanceof SMatchGUI.BaseCoalesceTreeModel) {
            SMatchGUI.BaseCoalesceTreeModel mtm = (SMatchGUI.BaseCoalesceTreeModel) tTarget.getModel();
            mtm.uncoalesceParents(target);

            // construct path to root
            TreePath pp = SMatchGUI.createPathToRoot(target);
            List<Object> ppList = Arrays.asList(pp.getPath());

            tTarget.makeVisible(pp);
            tTarget.setSelectionPath(pp);
            tTarget.scrollPathToVisible(pp);

            // check whether root is visible
            if (!SMatchGUI.isRootVisible(tTarget)) {
                // first try collapsing all the nodes above the target one
                IBaseNode<IBaseNode, IBaseNodeData> curNode = target.getParent();
                while (null != curNode) {
                    for (IBaseNode child : curNode.getChildrenList()) {
                        if (!ppList.contains(child) && !mtm.isCoalesced(child)) {
                            tTarget.collapsePath(SMatchGUI.createPathToRoot(child));
                        }
                    }
                    curNode = curNode.getParent();
                }

                if (!SMatchGUI.isRootVisible(tTarget)) {
                    // second try coalescing nodes, starting from those in the top
                    for (int i = 0; i < ppList.size() - 1; i++) {
                        if (ppList.get(i) instanceof IBaseNode && ppList.get(i + 1) instanceof IBaseNode) {
                            IBaseNode node = (IBaseNode) ppList.get(i);
                            IBaseNode child = (IBaseNode) ppList.get(i + 1);
                            int idx = node.getChildIndex(child);
                            mtm.coalesce(node, 0, idx - 1);

                            if (SMatchGUI.isRootVisible(tTarget)) {
                                break;
                            }
                        }
                    }
                }
            }

            spTarget.repaint();
        }
    }


    private void prepareData() throws ContextLoaderException {
        context = contextLoader.loadContext(inputFileName);
        data = context.getNodesList();

        progressBar.setMaximum(data.size());
        datasetSizeString = Integer.toString(data.size());

        curIndex = -1;
        while (null != data.get(curIndex + 1).getNodeData().getLabel() && (curIndex < (data.size() - 2))) {
            curIndex++;
        }

        //in case we load partially marked-up dataset
        while (phrasePanels.size() <= curIndex) {
            phrasePanels.add(null);
        }

        int oldIndex = curIndex;
        while (-1 < curIndex) {
            //put all already tagged labels for future reuse
            ILabel label = data.get(curIndex).getNodeData().getLabel();
            taggedPOS.put(label.getText(), toTabText(label));
            curIndex--;
        }

        //restore index
        curIndex = oldIndex;

        createTree(context, tSource);
    }

    private void loadAlso(String loadAlsoFileNames) throws ContextLoaderException {
        String[] files = loadAlsoFileNames.split(";");
        for (String file : files) {
            log.info("Loading: " + file);
            int oldSize = taggedPOS.size();

            HashSet<String> conflicts = new HashSet<String>();
            INLPContext c = contextLoader.loadContext(file);
            for (Iterator<INLPNode> i = c.getNodes(); i.hasNext(); ) {
                ILabel label = i.next().getNodeData().getLabel();
                String annotation = toTabText(label);

                if (!conflicts.contains(label.getText())) {
                    if (taggedPOS.containsKey(label.getText())) {
                        if (!taggedPOS.get(label.getText()).equals(annotation)) {
                            log.warn("Conflict in " + file + ": " + label.getText());
                            taggedPOS.remove(label.getText());
                            conflicts.add(label.getText());
                        }
                    } else {
                        //put all already tagged labels for future reuse
                        taggedPOS.put(label.getText(), annotation);
                    }
                }
            }
            log.info("Loaded " + (taggedPOS.size() - oldSize) + " items from " + file);
        }
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

    private void buildStaticGUI() {
        String layoutColumns = "right:pref, 4dlu, fill:max(p;400px):grow";
        String layoutRows = "center:default, 4dlu, center:default, 4dlu, center:default:grow, 4dlu, center:default";

        FormLayout layout = new FormLayout(layoutColumns, layoutRows);
        //PanelBuilder builder = new PanelBuilder(layout, new FormDebugPanel());
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        builder.addLabel("Path:", cc.xy(1, 1));

        lbPath = new JTextField("/Some/Example/Path");
        lbPath.setEditable(false);
        builder.add(lbPath, cc.xy(3, 1));

        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        JButton btPrev = new JButton(prevAction);
        toolBar1.add(btPrev);
        btNext = new JButton(nextAction);
        toolBar1.add(btNext);
        btNextNNP = new JButton(nextNNPAction);
        toolBar1.add(btNextNNP);
        btPrevTags = new JButton(prevTagsAction);
        toolBar1.add(btPrevTags);
        builder.add(toolBar1, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.DEFAULT));

        lbLabel = new JTextField("Loading data...");
        //big font for label
        lbLabel.setFont(new Font(lbLabel.getFont().getName(), lbLabel.getFont().getStyle(), 22));
        lbLabel.setEditable(false);
        builder.add(lbLabel, cc.xy(3, 3));

        tokensScroll = new JScrollPane();
        tokensScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        tokensScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Tokens"));
        builder.add(tokensScroll, cc.xyw(1, 5, 3, CellConstraints.FILL, CellConstraints.FILL));

        progressBar = new JProgressBar(0, 100);
        builder.add(progressBar, cc.xyw(1, 7, 3, CellConstraints.FILL, CellConstraints.BOTTOM));


        //tree and tags
        JPanel pnTreeTags = new JPanel();
        pnTreeTags.setLayout(new FormLayout("fill:d:grow", "fill:d:grow"));
        JSplitPane spnTreeTags = new JSplitPane();
        spnTreeTags.setContinuousLayout(true);
        spnTreeTags.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        spnTreeTags.setOneTouchExpandable(true);

        pnTreeTags.add(spnTreeTags, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

        //tags
        spnTreeTags.setRightComponent(builder.getPanel());

        //tree
        spSource = new JScrollPane();
        tSource = new JTree(new DefaultMutableTreeNode("Top"));
        tSource.setCellRenderer(coalesceTreeCellRenderer);
        tSource.addMouseListener(treeMouseListener);
        ToolTipManager.sharedInstance().registerComponent(tSource);
        tSource.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        spSource.setViewportView(tSource);
        spnTreeTags.setLeftComponent(spSource);

        mainPanel = pnTreeTags;
    }


    public void startup() throws ContextLoaderException {
        showLFIs();
        applyLookAndFeel();
        buildStaticGUI();
        prepareData();
        updateProgressBar();

        File inputFile = new File(inputFileName);
        JFrame frame = new JFrame(inputFile.getName() + " - POS tag annotation tool");
        frame.setMinimumSize(new Dimension(1000, 800));
        frame.setLocation(200, 200);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(windowListener);

        updateActions();
        //initiate, load first item
        btNext.doClick();
    }

    private final WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            if (dataModified) {
                lbLabel.setText("Saving data...");
                try {
                    contextRenderer.render(context, inputFileName);
                } catch (ContextRendererException exc) {
                    log.error(exc.getMessage(), exc);
                }
            }
            e.getWindow().dispose();
        }
    };

    public static void main(String[] args) throws ConfigurableException, ClassNotFoundException, IOException {
        //analyze args
        if (0 > args.length) {
            System.out.println("Usage: POSAnnotationTool datasetFileName [" + loadAlsoCmdLineToken + "file1;file2;...;fileN]");
        } else {
            POSAnnotationTool tool = new POSAnnotationTool(args[0]);
            tool.setProperties(CONF_FILE);
            if (2 == args.length && args[1].startsWith(loadAlsoCmdLineToken)) {
                String loadAlsoFileNames = args[1].substring(loadAlsoCmdLineToken.length(), args[1].length());
                tool.loadAlso(loadAlsoFileNames);
            }
            tool.startup();
        }
    }
}