package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.INode;

import java.util.StringTokenizer;

/**
 * Holds an element of the mapping.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MappingElement implements IMappingElement, Comparable {

    private String sourceEntity = null;
    private String targetEntity = null;
    private char relation = ' ';
    private double eq = 0;
    private double lg = 0;
    private double mg = 0;
    private String sID = "";
    private String tID = "";


    public double getEq() {
        return eq;
    }

    public double getLg() {
        return lg;
    }

    public double getMg() {
        return mg;
    }


    public static IMappingElement getInstance(String sourceEntity, String targetEntity, char relation) {
        return new MappingElement(sourceEntity, targetEntity, relation);
    }

    public MappingElement(String sourceEntity, String targetEntity, char relation, String sID, String tID) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.relation = relation;
        this.sID = sID;
        this.tID = tID;
        if (relation == MatchManager.SYNOMYM) {
            eq = 1;
        }
        if (relation == MatchManager.LESS_GENERAL_THAN) {
            lg = 1;
        }
        if (relation == MatchManager.MORE_GENERAL_THAN) {
            mg = 1;
        }
    }

    public MappingElement(String sourceEntity, String targetEntity, char relation) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.relation = relation;
        if (relation == MatchManager.SYNOMYM) {
            eq = 1;
        }
        if (relation == MatchManager.LESS_GENERAL_THAN) {
            lg = 1;
        }
        if (relation == MatchManager.MORE_GENERAL_THAN) {
            mg = 1;
        }
    }


    public MappingElement(double eq, double mg, double lg) {
        this.eq = eq;
        this.mg = mg;
        this.lg = lg;
        if (eq > 0.5) {
            this.relation = MatchManager.SYNOMYM;
        }
        if (lg > 0.5) {
            this.relation = MatchManager.LESS_GENERAL_THAN;
        }
        if (mg > 0.5) {
            this.relation = MatchManager.MORE_GENERAL_THAN;
        }
        if ((eq == 0) && (lg == 0) && (mg == 0)) {
            this.relation = MatchManager.IDK_RELATION;
        }

    }

    //TODO specify which is the format this function loads, certainly is not (source\trel\ttarge)
    //
    public MappingElement(String fileLine) {
        if (-1 == fileLine.indexOf("\t")) {
            StringTokenizer st1 = new StringTokenizer(fileLine);
            if (st1.hasMoreTokens()) {
                String sTok = st1.nextToken();
                if ((sTok != null) && (sTok.length() > 1)) {
                    try {
                        this.sID = sTok;
                        sTok = st1.nextToken();
                        this.tID = sTok;
                        sTok = st1.nextToken();
                        this.sourceEntity = sTok;
                        sTok = st1.nextToken();
                        while (!sTok.startsWith("/")) {
                            this.sourceEntity = this.sourceEntity + sTok;
                            sTok = st1.nextToken();
                        }

                        this.targetEntity = sTok;
                        sTok = st1.nextToken();
                        while ((!sTok.startsWith("1")) && (!sTok.startsWith("0"))) {
                            this.targetEntity = this.targetEntity + sTok;
                            sTok = st1.nextToken();
                        }

                        this.eq = Double.parseDouble(sTok);
                        sTok = st1.nextToken();
                        this.lg = Double.parseDouble(sTok);
                        sTok = st1.nextToken();
                        this.mg = Double.parseDouble(sTok);
                    } catch (Exception e) {
                        System.out.println(fileLine);
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            }
            if (eq > 0.5) {
                this.relation = MatchManager.SYNOMYM;
            }
            if (lg > 0.5) {
                this.relation = MatchManager.LESS_GENERAL_THAN;
            }
            if (mg > 0.5) {
                this.relation = MatchManager.MORE_GENERAL_THAN;
            }
            if ((eq == 0) && (lg == 0) && (mg == 0)) {
                this.relation = MatchManager.IDK_RELATION;
            }
        } else {
            String[] pieces = fileLine.split("\t");
            try {
                this.relation = pieces[1].charAt(0);
                this.sourceEntity = pieces[0].trim();
                this.targetEntity = pieces[2].trim();
            } catch (Exception e) {
                System.out.printf(fileLine);
                e.printStackTrace();
            }
        }
    }

    public boolean isWithOr() {
        if (sourceEntity.indexOf('&') > -1) {
            return true;
        }
//		if (sourceEntity.indexOf("and")>-1) return true;
        if (targetEntity.indexOf('&') > -1) {
            return true;
        }
//		if (targetEntity.indexOf("and")>-1) return true;
//		if (targetEntity.indexOf("or")>-1) return true;
//		if (sourceEntity.indexOf("or")>-1) return true;
        return false;
    }

    public String getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(String sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public char getRelation() {
        return relation;
    }

    public void setRelation(char relation) {
        this.relation = relation;
        if (relation == MatchManager.SYNOMYM) {
            eq = 1;
        }
        if (relation == MatchManager.LESS_GENERAL_THAN) {
            lg = 1;
        }
        if (relation == MatchManager.MORE_GENERAL_THAN) {
            mg = 1;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MappingElement)) {
            return false;
        }
        final MappingElement that = (MappingElement) o;
        if (relation != that.relation) {
            return false;
        }
        if (!sourceEntity.equals(that.sourceEntity)) {
            return false;
        }
        if (!targetEntity.equals(that.targetEntity)) {
            return false;
        }
        return true;
    }

    public boolean equalsForMappingComparison(MappingElement m) {
        if (!sID.equals(m.sID)) {
            return false;
        }
        if (!tID.equals(m.tID)) {
            return false;
        }
        return true;
    }

    public boolean isMeaningful() {
        if ((eq == 0) && (lg == 0) && (mg == 0)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean weakEquals(Object o) {
        try {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MappingElement)) {
                return false;
            }
            final MappingElement mappingElement = (MappingElement) o;
            if (!sourceEntity.equals(mappingElement.sourceEntity)) {
                return false;
            }
            if (!targetEntity.equals(mappingElement.targetEntity)) {
                return false;
            }
            if (mappingElement.relation == MatchManager.IDK_RELATION) {
                return false;
            }
            if (relation == MatchManager.IDK_RELATION) {
                return false;
            }
        } catch (Exception e) {
            System.out.println(sourceEntity);
            System.out.println(targetEntity);
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        return true;
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public String toString() {
        return sourceEntity + "\t" + relation + "\t" + targetEntity;
    }

    public int compareTo(Object o) {
        if (o != null) {
            MappingElement me = (MappingElement) o;
            if (sourceEntity.equals(me.sourceEntity)) {
                return (targetEntity.compareTo(me.targetEntity));
            } else {
                return (sourceEntity.compareTo(me.sourceEntity));
            }
        }
        return 1;
    }

    public INode getSourceNode() {
        return null;
    }

    public INode getTargetNode() {
        return null;
    }
}
