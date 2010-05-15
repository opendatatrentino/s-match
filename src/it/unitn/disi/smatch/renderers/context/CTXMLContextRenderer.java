package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.*;
import it.unitn.disi.smatch.loaders.context.CTXML;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Renders a context into a CTXML file.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class CTXMLContextRenderer extends Configurable implements IContextRenderer {

    private static final Logger log = Logger.getLogger(CTXMLContextRenderer.class);

    //TODO rewrite with proper exceptions
    //TODO rewrite for generation via interfaces

    public void render(IContext context, String fileName) throws ContextRendererException {
        context.getContextData().sort();
        saveToXml(context, fileName);
    }

    /**
     * Saves the context into a xml file.
     *
     * @param c           the interface of the context
     * @param xmlFileName The name of the file where the contexts has to be saved
     * @throws ContextRendererException ContextRendererException
     */
    private void saveToXml(IContext c, String xmlFileName) throws ContextRendererException {
        try {
            IContextData cd = c.getContextData();
            if (null == c.getRoot()) {
                throw new ContextRendererException("No root defined for the context");
            }
            BufferedWriter ctxmlFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFileName), "UTF-8"));
            // write the head of the xml document
            ctxmlFile.write("<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n");
            ctxmlFile.write("<datastructures xmlns:xsi = \"" + Context.INSTANCE_NAMESPACE_URI + "\"");
            ctxmlFile.write(" xsi:noNamespaceSchemaLocation = \"" + Context.getSCHEMA_LOCATION() + "\">\n");

            // write the header of the context
            ctxmlFile.write("<ctxHeader ctxId = \"" + cd.getCtxId() + "\"");
            if (cd.getLabel() == null || (cd.getLabel()).equals("")) {
                INode root = c.getRoot();
                cd.setLabel("Context-" + root.getNodeName());
            }
            ctxmlFile.write(" label = \"" + cd.getLabel() + "\"");
            ctxmlFile.write(" status =\"" + cd.getStatus() + "\"");
            ctxmlFile.write(" normalized =\"" + cd.isNormalized() + "\">\n");
            ctxmlFile.write("<owner>\n");
            ctxmlFile.write("<agentId>" + cd.getOwner() + "</agentId>\n");
            ctxmlFile.write("</owner>\n");
            ctxmlFile.write("<group>\n");
            ctxmlFile.write("<groupId>" + cd.getGroup() + "</groupId>\n");
            ctxmlFile.write("</group>\n");
            ctxmlFile.write("<security>\n");
            ctxmlFile.write("<accessRights>" + cd.getSecurityAccessRights() + "</accessRights>\n");
            ctxmlFile.write("<encription>" + cd.getSecurityEncryption() + "</encription>\n");
            ctxmlFile.write("</security>\n");
            String description = cd.getDescription();
            description = CTXML.xmlTagEncode(description);
            ctxmlFile.write("<description>" + description + "</description>\n");
            ctxmlFile.write("</ctxHeader>\n");

            // write the header of the schema of the concept hierarchy
            ctxmlFile.write("<ctxContent language = \"" + cd.getLanguage() + "\">\n");
            ctxmlFile.write("<CHContent>\n");
            ctxmlFile.write("<schema reservedNameSpaceTag=\"" + Context.NAMESPACE_URI + "\"");
            ctxmlFile.write(" targetNamespace=\"" + cd.getNamespace() + "\"");
            ctxmlFile.write(" reservedNameSpaceTag--ctxTag=\"" + cd.getNamespace() + "\">\n");

            /// write the content of the concept hierarchy
            writeBaseNode(ctxmlFile);
            writeConceptHierarchyToFile(ctxmlFile, c.getRoot());

            ctxmlFile.write("</schema>\n");
            ctxmlFile.write("</CHContent>\n");
            ctxmlFile.write("</ctxContent>\n");
            ctxmlFile.write("</datastructures>\n");
            ctxmlFile.close();
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextRendererException(errMessage, e);
        }
    }

    //write root concept to string

    private static void writeBaseNode(BufferedWriter ctxmlFile) throws IOException {
        ctxmlFile.write("<complexType-Concept name = \"" + Context.BASE_NODE + "\">");
        ctxmlFile.write("<annotation>\n");
        ctxmlFile.write("<appinfo>\n");
        ctxmlFile.write("<synonyms></synonyms>\n");
        ctxmlFile.write("<alternativeLabels></alternativeLabels>\n");
        ctxmlFile.write("<weight></weight>\n");
        ctxmlFile.write("</appinfo>\n");
        ctxmlFile.write("</annotation>\n");
        ctxmlFile.write("</complexType-Concept>");
        ctxmlFile.newLine();
    }

    //write context data to file

    private void writeConceptHierarchyToFile(BufferedWriter xmlFile, INode root) throws IOException {
        toCtxml(xmlFile, root);
    }

    //write to ctxml

    public void toCtxml(BufferedWriter ctxmlFile, INode node) throws IOException {
        INodeData parent = null;
        if (node.getParent() != null)
            parent = node.getParent().getNodeData();
        INodeData nodeData = node.getNodeData();
        String nodeUniqueName = nodeData.getNodeUniqueName();
        List<INode> children = node.getChildren();
        boolean root = false;
        boolean defineBaseNode = false;
        if (parent == null)
            root = true;
        String conceptUniqueNameRevised;
        conceptUniqueNameRevised = CTXML.xmlTagEncode(nodeUniqueName);
        String fatherName = null;
        if (parent != null)
            fatherName = parent.getNodeUniqueName();
        String fatherNameRevised = "";
        if (fatherName != null) {
            fatherNameRevised = CTXML.xmlTagEncode(fatherName);
        }
        if (!root) {
            ctxmlFile.write("<complexType-Concept name = \"" + conceptUniqueNameRevised + "\">\n");
            ctxmlFile.write(writeAttributesAndSetOfSenses(nodeData));
            ctxmlFile.write("<complexContent>\n");
            ctxmlFile.write("<extension base = \"" + fatherNameRevised + "\"/>\n");
            ctxmlFile.write("</complexContent>\n");
            ctxmlFile.write("</complexType-Concept>\n");
        } else {
            ctxmlFile.write("<complexType-Concept name = \"" + conceptUniqueNameRevised + "\">\n");
            ctxmlFile.write(writeAttributesAndSetOfSenses(nodeData));
            ctxmlFile.write("</complexType-Concept>\n");
        }
        for (int i = 0; i < children.size(); i++) {
            INode concept = (INode) (children.get(i));
            toCtxml(ctxmlFile, concept);
        }
    }

    //get string for saving in ctxml

    private String writeAttributesAndSetOfSenses(INodeData node) {
        List<String> synonyms = node.getSynonyms();
        List<String> alternativeLabels = node.getAlternativeLabels();
        double weight = node.getWeight();
        String cNodeFormula = node.getCNodeFormula();
        String cLabFormula = node.getcLabFormula();
        List<IAtomicConceptOfLabel> setOfSenses = node.getACoLs();
        StringBuffer stringAttributes = new StringBuffer();
        stringAttributes.append("<annotation>\n");
        stringAttributes.append("<appinfo>\n");
        /// Concept attributes
        stringAttributes.append("<synonyms>");
        if (synonyms.size() > 0) {
            for (int i = 0; i < synonyms.size(); i++) {
                String syn = (synonyms.get(i));
                if (i < synonyms.size() - 1)
                    stringAttributes.append(syn).append(" ");
                else
                    stringAttributes.append(syn);
            }
        }
        stringAttributes.append("</synonyms>\n");
        stringAttributes.append("<alternativeLabels>");
        if (alternativeLabels.size() > 0) {
            for (int i = 0; i < alternativeLabels.size(); i++) {
                String lab = (alternativeLabels.get(i));
                if (i < alternativeLabels.size() - 1) {
                    stringAttributes.append(lab).append(" ");
                } else
                    stringAttributes.append(lab);
            }
        }
        stringAttributes.append("</alternativeLabels>\n");
        stringAttributes.append("<weight>");
        if (weight != -1) {
            stringAttributes.append(Double.toString(weight));
        }
        stringAttributes.append("</weight>\n");
        // Concept at label
        if (cLabFormula != null && !cLabFormula.equals("")) {
            stringAttributes.append("<cLabFormula> ");
            String formula = CTXML.xmlTagEncode(cLabFormula);
            stringAttributes.append(formula);
            stringAttributes.append("</cLabFormula>\n");
        }
        // Concept logical Formula representation
        if (cNodeFormula != null && !cNodeFormula.equals("")) {
            stringAttributes.append("<logicalFormulaRepresentation> ");
            String formula = CTXML.xmlTagEncode(cNodeFormula);
            stringAttributes.append(formula);
            stringAttributes.append("</logicalFormulaRepresentation>\n");
        }
        /// Concept Table of Senses
        if (setOfSenses.size() > 0) {
            stringAttributes.append("<setOfSenses>\n");
            for (IAtomicConceptOfLabel sense : setOfSenses) {
                stringAttributes.append(getXmlRepresentation(sense));
            }
            stringAttributes.append("</setOfSenses>\n");
        }
        stringAttributes.append("</appinfo>\n");
        stringAttributes.append("</annotation>\n");
        return stringAttributes.toString();
    }

    public String getXmlRepresentation(IAtomicConceptOfLabel acol) {
        ISensesSet wSenses = acol.getSenses();
        StringBuffer stringXml = new StringBuffer();
        stringXml.append("<sense>\n");
        stringXml.append("<idToken>").append(acol.getIdToken()).append("</idToken>\n");
        stringXml.append("<token>").append(CTXML.xmlTagEncode(acol.getToken().trim())).append("</token>\n");
        stringXml.append("<lemma>").append(CTXML.xmlTagEncode(acol.getLemma().trim())).append("</lemma>\n");
        stringXml.append("<PoS>").append(acol.getPos().trim()).append("</PoS>\n");
        stringXml.append("<wSenses>");
        List<String> senseList = wSenses.getSenseList();
        for (String senseId : senseList) {
            stringXml.append(CTXML.xmlTagEncode(senseId.trim())).append(" ");
        }
        stringXml.append("</wSenses>\n");
        stringXml.append("</sense>\n");
        return stringXml.toString();
    }
}