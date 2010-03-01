package it.unitn.disi.smatch.gui;


import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.loaders.PlainMappingLoader;
import it.unitn.disi.smatch.loaders.TABLoader;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Provides basic S-Match GUI.
 *
 * @author Juan Pane (pane@disi.unitn.it)
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatchingBasicGUI extends JPanel
        implements ActionListener, ComponentListener, AdjustmentListener, TreeExpansionListener {

    private static final long serialVersionUID = 1L;

    private static final String OPEN_SOURCE_COMMAND = "open source";
    private static final String OPEN_TARGET_COMMAND = "open target";
    private static final String OPEN_MAPPING_COMMAND = "open mapping";
    private static final String RUN_MATCHER_COMMAND = "run matcher";

    private static final String MAIN_ICON_FILE = ".." + File.separator + "res" + File.separator + "s-match.ico";

    private JTree sourceTree;
    private JTree targetTree;
    private JSplitPane splitPane;
    private JFileChooser fc;

    //source and target contexts
    private IContext sourceContext;
    private IContext targetContext;

    //matcher source and target files
//    private String sourceFileName;
//    private String targetFileName;

    JTextField sourceFileTxt;
    JTextField targetFileTxt;
    JTextField mappingFileTxt;

    //hashes for paths with the index of the tree element
    HashMap<String, Integer> sourceRowForPath;
    HashMap<String, Integer> targetRowForPath;

    //offsets to draw the lines when things mode around
    private Point leftOffset = new Point(); //for source tree
    private Point rightOffset = new Point(); //for target tree

    ArrayList<MappingElement> mappings = null;


    public MatchingBasicGUI() {
        super(new GridBagLayout());

        //Create a file chooser
        fc = new JFileChooser();

        sourceTree = new JTree(new DefaultMutableTreeNode("Load source"));
        targetTree = new JTree(new DefaultMutableTreeNode("Load target"));

        sourceRowForPath = new HashMap<String, Integer>();
        targetRowForPath = new HashMap<String, Integer>();


        //Create the scroll pane and add the tree to it.
        JScrollPane leftTreeView = new JScrollPane(sourceTree);
        leftTreeView.getHorizontalScrollBar().addAdjustmentListener(this);
        leftTreeView.getVerticalScrollBar().addAdjustmentListener(this);


        //Create the scroll pane and add the tree to it.

        JScrollPane rightTreeView = new JScrollPane(targetTree);
        rightTreeView.getHorizontalScrollBar().addAdjustmentListener(this);
        rightTreeView.getVerticalScrollBar().addAdjustmentListener(this);


        //Add the scroll panes to a split pane.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(leftTreeView);
        splitPane.setRightComponent(rightTreeView);


        Dimension minimumSize = new Dimension(100, 100);
        leftTreeView.setMinimumSize(minimumSize);
        rightTreeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(400);
        splitPane.setPreferredSize(new Dimension(800, 500));


        //constraints for the split pane
        GridBagConstraints constraintSplit = new GridBagConstraints();

        constraintSplit.fill = GridBagConstraints.BOTH;
        constraintSplit.gridx = 0;
        constraintSplit.gridy = 0;
        constraintSplit.weighty = 1.0;
        constraintSplit.weightx = 1.0;
        constraintSplit.gridwidth = 2;

        //Add the split pane to this panel.
        add(splitPane, constraintSplit);


        splitPane.addComponentListener(this);

        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                repaint();
            }
        });


        //constraints for the selectors
        GridBagConstraints constraintFileSelector = new GridBagConstraints();
        constraintFileSelector.fill = GridBagConstraints.BOTH;
        constraintFileSelector.gridx = 0;
        constraintFileSelector.gridy = 1;
        constraintFileSelector.weighty = 0.0;
        constraintFileSelector.weightx = 1.0;


        //add the file selectors
        add(createFileSelectors(), constraintFileSelector);

        //constraints for the legends
        GridBagConstraints constraintLegend = new GridBagConstraints();
        constraintLegend.fill = GridBagConstraints.HORIZONTAL;
        constraintLegend.gridx = 1;
        constraintLegend.gridy = 1;
        constraintLegend.weighty = 0.0;
        constraintLegend.weightx = 0.0;

        //add the legend
        add(createLegend(), constraintLegend);


    }


    /**
     * Converts the IMatchMatrix into a list of mapping elements between
     * sourcePath and targetPath
     *
     * @param sourceContext2
     * @param targetContext2
     * @return
     * @throws IOException
     */
    private ArrayList<MappingElement> loadMappingsFromFile(
            IContext sourceContext2, IContext targetContext2,
            String mappingFile) throws IOException {

        mappingFileTxt.setText(mappingFile);

        PlainMappingLoader mappingLoader = new PlainMappingLoader();
        IMatchMatrix matchmatrix = mappingLoader.loadMapping(sourceContext2, targetContext2, mappingFile);


        ArrayList<MappingElement> mappingsElems = new ArrayList<MappingElement>();
        Vector<INode> sourceNodes = sourceContext2.getAllNodes();
        Vector<INode> targetNodes = targetContext2.getAllNodes();

        //TODO refactor the loaders, change get path to INode
        char relation = ' ';
        String sourcePath = "";
        String targetPath = "";

        for (int sourceIndex = 0; sourceIndex < matchmatrix.getX(); sourceIndex++) {
            for (int targetIndex = 0; targetIndex < matchmatrix.getY(); targetIndex++) {
                relation = matchmatrix.getElement(sourceIndex, targetIndex);
                if (relation == MatchManager.SYNOMYM ||
                        relation == MatchManager.LESS_GENERAL_THAN ||
                        relation == MatchManager.MORE_GENERAL_THAN) {
                    sourcePath = mappingLoader.getNodePath(sourceNodes.get(sourceIndex));
                    targetPath = mappingLoader.getNodePath(targetNodes.get(targetIndex));
                    MappingElement mappingElem = new MappingElement(sourcePath, targetPath, relation);
                    mappingsElems.add(mappingElem);
                }

            }
        }

        repaint();
        return mappingsElems;
    }


    /**
     * Creates the tree given a file in a tab indented format
     *
     * @param fileName File name in a tab indented format
     * @return the JTree representing the content of the tab indented file
     */
    private IContext createTree(String fileName, JTree jTree, HashMap<String, Integer> rowForPathHash) {
        //Create the nodes.

        TABLoader loader = new TABLoader();
        IContext context = loader.loadContext(fileName);
        TreeNode rootNode = context.getRoot();

        //Create a tree that allows one selection at a time.
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        jTree.setModel(treeModel);

        jTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree.addTreeExpansionListener(this);

        //expand all the nodes initially
        for (int i = 0; i < jTree.getRowCount(); i++) {
            jTree.expandRow(i);
            TreePath rowPath = jTree.getPathForRow(i);
            rowForPathHash.put(getPathForTreePath(rowPath), i);
        }

        return context;
    }


    /**
     * Returns the full path to the root using \\ as separator
     *
     * @param treePath
     * @return
     */
    private String getPathForTreePath(TreePath treePath) {
        String result = "";
        Object[] path = treePath.getPath();
        for (int i = path.length - 1; i >= 0; i--) {
            result = "\\" + path[i] + result;
        }
        return result;
    }


    /**
     * Paints the windows accordingly to the Swing JPanels, then paints the mappings
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    public void paintChildren(Graphics g) {
        super.paintChildren(g);

        Graphics2D g2 = (Graphics2D) g;
        paintMappings(g2);

        //       g2.draw(createArrow());
    }


    /**
     * Paints the lines considering the mappings loaded
     *
     * @param g2
     */
    private void paintMappings(Graphics2D g2) {
        if (mappings != null) {
            BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.RED);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.4));
            g2.setStroke(stroke);
            Rectangle splitBound = splitPane.getBounds();
            g2.setClip(0, 0, splitBound.width, splitBound.height - 5);
            computeOffset();
            for (MappingElement mapping : mappings) {
                int source = sourceRowForPath.get(mapping.getSourceEntity());
                int target = targetRowForPath.get(mapping.getTargetEntity());

                if (source >= 0 && target >= 0) {
                    Line2D line2 = drawBoundingLine(sourceTree.getRowBounds(source), targetTree.getRowBounds(target));
                    changeColorOfRelation(g2, mapping);
                    g2.draw(line2);
                }
            }
        }
    }


    /**
     * Computes he offset of the trees in order to draw the lines correctly
     */
    private void computeOffset() {

        int correction = 2;
        int dividerSize = 9;

        int leftX = sourceTree.getLocationOnScreen().x - splitPane.getLocationOnScreen().x - correction;
        int leftY = sourceTree.getLocationOnScreen().y - splitPane.getLocationOnScreen().y - correction;

        int rightX = targetTree.getLocationOnScreen().x - splitPane.getLocationOnScreen().x - correction;
        int rightY = targetTree.getLocationOnScreen().y - splitPane.getLocationOnScreen().y - correction;

        int dividerLocation = dividerSize + splitPane.getDividerLocation();
        if (rightX < dividerLocation) {
            rightX = dividerLocation;
        }

        leftOffset.setLocation(leftX, leftY);
        rightOffset.setLocation(rightX, rightY);


    }


    /**
     * changes the color of the lne according to the type of relation
     *
     * @param g2
     * @param mapping
     */
    private void changeColorOfRelation(Graphics2D g2, MappingElement mapping) {
        char rel = mapping.getRelation();
        switch (rel) {
            case MatchManager.LESS_GENERAL_THAN: {
                g2.setColor(Color.ORANGE);
                break;
            }
            case MatchManager.MORE_GENERAL_THAN: {
                g2.setColor(Color.BLUE);
                break;
            }
            case MatchManager.SYNOMYM: {
                g2.setColor(Color.GREEN);
                break;
            }
            case MatchManager.OPPOSITE_MEANING: {
                g2.setColor(Color.RED);
                break;
            }
            default:
                break;
        }

    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {

        //Create and set up the window.
        JFrame frame = new JFrame("S-Match GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //try to set an icon
        try {
            nl.ikarus.nxt.priv.imageio.icoreader.lib.ICOReaderSpi.registerIcoReader();
            System.setProperty("nl.ikarus.nxt.priv.imageio.icoreader.autoselect.icon", "true");
            ImageInputStream in = ImageIO.createImageInputStream(new FileInputStream(MAIN_ICON_FILE));
            ArrayList<Image> icons = new ArrayList<Image>();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader r = readers.next();
                r.setInput(in);
                int nr = r.getNumImages(true);
                for (int i = 0; i < nr; i++) {
                    icons.add(r.read(i));
                }
                frame.setIconImages(icons);
            }
        } catch (IOException e) {
            //silently fail
        }


        //Add content to the window.
        frame.add(new MatchingBasicGUI());

        //Display the window.
        frame.pack();
        frame.setVisible(true);

    }


    /**
     * used to print in the console the boundaries of the nodes of the JTree
     *
     * @param jTree
     */
    protected void printNodesBounds(JTree jTree) {

        for (int i = 0; i < jTree.getRowCount(); i++) {
            System.out.println(jTree.getPathForRow(i));
            Rectangle bound = jTree.getRowBounds(i);
            printBound(bound);
        }
    }


    /**
     * prints to the console the boundaries of a given rectangle
     *
     * @param bound
     */
    private void printBound(Rectangle bound) {
        System.out.println("X:" + bound.getX() +
                ",\tY:" + bound.getY() +
                ",\theight:" + bound.height +
                ",\twidth:" + bound.width +
                ",\tlocation:" + bound.getLocation() +
                ",\tgetMinX:" + bound.getMinX() +
                ",\tgetMaxX:" + bound.getMaxX() +
                ",\tgetMinY:" + bound.getMinY() +
                ",\tgetMaxY:" + bound.getMaxY() +
                ",\tgetCenterX:" + bound.getCenterX()
        );
    }


    /**
     * Draws a line given the boundaries of 2 elements of
     * the 2 trees in the GUI
     *
     * @param leftBound  a node from the let tree
     * @param rightBound a node from the right tree
     * @return a line
     */
    private Line2D drawBoundingLine(Rectangle leftBound, Rectangle rightBound) {//, Line2D lowerBound){

        Line2D line = new Line2D.Double(
                leftBound.getMaxX() + leftOffset.x,
                leftBound.getCenterY() + leftOffset.y,
                rightBound.getMinX() + rightOffset.x,
                rightBound.getCenterY() + rightOffset.y);

        return line;
    }


    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


    /**
     * Creates a JPanel with the legend for the mappings to be drawn
     *
     * @return
     */
    public JPanel createLegend() {
        Dimension lsize = new Dimension(130, 18);
        Font plaintext = new Font("plain", Font.TRUETYPE_FONT, 12);

        JPanel legend = new JPanel();
        legend.setLayout(new GridLayout(3, 1));

        JLabel equal = new JLabel("- equals");
        equal.setFont(plaintext);
        equal.setForeground(Color.GREEN);
        equal.setPreferredSize(lsize);
        legend.add(equal);

        JLabel lessGeneral = new JLabel("- less general");
        lessGeneral.setFont(plaintext);
        lessGeneral.setPreferredSize(lsize);
        lessGeneral.setForeground(Color.ORANGE);
        legend.add(lessGeneral);

        JLabel moreGeneral = new JLabel("- more general");
        moreGeneral.setFont(plaintext);
        moreGeneral.setPreferredSize(lsize);
        moreGeneral.setForeground(Color.BLUE);
        legend.add(moreGeneral);

        legend.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Reference"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return legend;
    }


    /**
     * Creates a JPanel with the file selectors to load the files
     *
     * @return
     */
    public JScrollPane createFileSelectors() {


        JPanel fileSelectorPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
//         layout.
        fileSelectorPanel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();

        JLabel title = new JLabel("Source and Target files should be in tab indented format:");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        fileSelectorPanel.add(title, c);
        c.gridwidth = 1; //reset grid width

        //create labels

        JLabel sourceFileLbl = new JLabel("Source file: ");
        JLabel targetFileLbl = new JLabel("Target file: ");
        JLabel mappingFileLbl = new JLabel("Mapping file: ");


        JLabel labels[] = {sourceFileLbl, targetFileLbl, mappingFileLbl};

        //create text areas
        sourceFileTxt = new JTextField(40);
        targetFileTxt = new JTextField(40);
        mappingFileTxt = new JTextField(40);

        JTextField textFields[] = {sourceFileTxt, targetFileTxt, mappingFileTxt};
        //create buttons

        JButton sourceButton = new JButton("Open Source");
        sourceButton.setActionCommand(OPEN_SOURCE_COMMAND);
        sourceButton.addActionListener(this);

        JButton targetButton = new JButton("Open Target");
        targetButton.setActionCommand(OPEN_TARGET_COMMAND);
        targetButton.addActionListener(this);

        JButton mappingButton = new JButton("Open Mapping");
        mappingButton.setActionCommand(OPEN_MAPPING_COMMAND);
        mappingButton.addActionListener(this);

        JButton buttons[] = {sourceButton, targetButton, mappingButton};

        addLabelTextRows(labels, textFields, buttons, fileSelectorPanel);

        JButton runButton = new JButton("Run matcher");
        runButton.setActionCommand(RUN_MATCHER_COMMAND);
        runButton.addActionListener(this);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        fileSelectorPanel.add(runButton, c);


        fileSelectorPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Choose the files to be processed"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JScrollPane scrollPane = new JScrollPane(fileSelectorPanel);
        scrollPane.setMinimumSize(new Dimension(100, 150));

        return scrollPane;
    }

    private void addLabelTextRows(JLabel[] labels,
                                  JTextField[] textFields,
                                  JButton[] buttons,
                                  Container container) {

        Dimension buttonsize = new Dimension(130, 20);
        Font plaintext = new Font("plain", Font.TRUETYPE_FONT, 12);
        GridBagConstraints c = new GridBagConstraints();
        int numLabels = labels.length;

        for (int i = 1; i <= numLabels; i++) {
            //position labels
            c.gridx = 0;
            c.gridy = i;
            c.anchor = GridBagConstraints.WEST;
            container.add(labels[i - 1], c);
            labels[i - 1].setFont(plaintext);

            //position textfields
            c.gridx = 1;
            c.gridy = i;
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.HORIZONTAL;
            container.add(textFields[i - 1], c);
            textFields[i - 1].setEnabled(false);

            c.fill = GridBagConstraints.NONE;        //reset

            //position textfields
            c.gridx = 2;
            c.gridy = i;
            c.anchor = GridBagConstraints.EAST;
            container.add(buttons[i - 1], c);
            buttons[i - 1].setPreferredSize(buttonsize);
        }


    }


    /**
     * matches the source and target files and returns the file with the mapping
     *
     * @return
     * @throws SMatchException
     */
    private String runMatcher() throws SMatchException {

        String sourceFileName = sourceFileTxt.getText();
        String targetFileName = targetFileTxt.getText();

        String defaultConfigFile = ".." + File.separator + "conf" + File.separator + "SMatchDefault.properties";
        String minimalConfigFile = ".." + File.separator + "conf" + File.separator + "SMatchDefaultMinimal.properties";
        String outputFolder = sourceFileName.substring(0, sourceFileName.lastIndexOf(File.separator) + 1);


        //TODO remove static variables from MatchManager
        MatchManager defaultMatcher = new MatchManager(defaultConfigFile);


        // directories of files to online
        MatchManager.ctxsSourceFile = sourceFileName + ".xml";
        MatchManager.ctxsTargetFile = targetFileName + ".xml";

        //linguistic pre-processing
        defaultMatcher.offline(sourceContext, MatchManager.ctxsSourceFile);
        defaultMatcher.offline(targetContext, MatchManager.ctxsTargetFile);

        //match
        MatchManager.setOutputFile(outputFolder + "resultDefault.txt");
        defaultMatcher.online(sourceContext, targetContext);


        MatchManager minimalMatcher = new MatchManager(minimalConfigFile);
        //match
        MatchManager.setOutputFile(outputFolder + "resultMinimal.txt");
        minimalMatcher.online(sourceContext, targetContext);

        return outputFolder + "resultDefault.txt";
    }

    /**
     * EVENT LISTENERS
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (OPEN_SOURCE_COMMAND.equals(command)) {
            int returnVal = fc.showOpenDialog(MatchingBasicGUI.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                sourceFileTxt.setText(file.getAbsolutePath());
                System.out.println("Opening source: " + file.getAbsolutePath() + "");

                sourceContext = createTree(file.getAbsolutePath(), sourceTree, sourceRowForPath);
            }
        } else if (OPEN_TARGET_COMMAND.equals(command)) {
            int returnVal = fc.showOpenDialog(MatchingBasicGUI.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                targetFileTxt.setText(file.getAbsolutePath());
                System.out.println("Opening target: " + file.getAbsolutePath() + "");

                targetContext = createTree(file.getAbsolutePath(), targetTree, targetRowForPath);
            }
        } else if (OPEN_MAPPING_COMMAND.equals(command)) {
            int returnVal = fc.showOpenDialog(MatchingBasicGUI.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                try {
                    mappings = loadMappingsFromFile(sourceContext, targetContext, file.getAbsolutePath());

                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

        } else if (RUN_MATCHER_COMMAND.equals(command)) {

            try {
                String mappingFile = runMatcher();
                mappings = loadMappingsFromFile(sourceContext, targetContext, mappingFile);

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    public void componentHidden(ComponentEvent arg0) {

    }

    public void componentMoved(ComponentEvent arg0) {
        repaint();
    }

    public void componentResized(ComponentEvent arg0) {
        repaint();
    }

    public void componentShown(ComponentEvent arg0) {
        repaint();
    }

    public void adjustmentValueChanged(AdjustmentEvent arg0) {
        repaint();
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        repaint();
    }

    public void treeExpanded(TreeExpansionEvent event) {
        repaint();
    }
}

