package it.unitn.disi.smatch.data;

import java.util.HashSet;

/**
 * Holds data parts of the context.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IContextData {

    void setSchemaLocation(String schemaLocation);

    void setCtxId(String ctxId);

    void setLanguage(String language);

    void setNamespace(String namespace);

    void setDescription(String description);

    void setLabel(String label);

    void setStatus(String status);

    //Header stuff
    void setOwner(String owner);

    void setGroup(String group);

    void setSecurityAccessRights(String securityAccessRights);

    void setSecurityEncription(String securityEncription);

    String getLanguage();

    String getNamespace();

    String getDescription();

    String getCtxId();

    String getLabel();

    String getGroup();

    String getStatus();

    String getOwner();

    String getSecurityAccessRights();

    String getSecurityEncription();

    HashSet<String> getMg();

    void setMg(HashSet<String> mg);

    HashSet<String> getLg();

    void setLg(HashSet<String> lg);

    HashSet<String> getSynonyms();

    void setSynonyms(HashSet<String> synonyms);

    HashSet<String> getOpp();

    void setOpp(HashSet<String> opp);

    boolean isNormalized();

    void setNormalized(boolean normalized);

    void sort();

    public void updateNodeIds();
}
