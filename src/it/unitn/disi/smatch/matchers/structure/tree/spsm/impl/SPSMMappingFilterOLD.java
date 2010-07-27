package it.unitn.disi.smatch.matchers.structure.tree.spsm.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

//import org.apache.log4j.Logger;

import it.unitn.disi.smatch.data.mappings.HashMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IMappingElement;




/**
 *  
 * @author Juan Pane pane@disi.unitn.it
 */
public class SPSMMappingFilterOLD {

	//CNode matrix (matrix of relations between nodes of the ontologies) rebuild from the mappings vector
	private IMappingElement<INode>[][] cNodeMatrix;
	
	//vector with all the source nodes, used for fast searching of the index in the matrix
	private List<INode> sourceVector;
	
	//vector with all the target nodes, used for fast searching of the index in the matrix
	private List<INode> targetVector;
	
	//number of rows
	private int sourceSize; 
	
	//number of columns
	private int targetSize;
	
	//Data translation vector
//	private IMappingElement<INode> filteredMapppings[];
	private IContextMapping<INode> spsmMapppings;
	
//	private Hashtable<String,Integer> orderOfSiblingsInSource;
//	private Hashtable<String,Integer> orderOfSiblingsInTarget;
	
	IContextMapping<INode> mappings2;
	
	private static final Logger log = Logger.getLogger(SPSMMappingFilterOLD.class);
	

	
	/**
	 * Fills the CNode Matrix from the mappings vector returned from the matcher
	 * 
	 * @param sourceList vector with the nodes of the LCC constraints
	 * @param targetVec vector with the nodes of the OKC function
	 * @param mappings mappings returned by the Matcher
	 */
	public SPSMMappingFilterOLD(IContext source, IContext target, 
			IContextMapping<INode> mappings){

		sourceVector = source.getNodesList();
		targetVector = target.getNodesList();
		
		sourceSize = sourceVector.size();
		targetSize = targetVector.size();

		cNodeMatrix = new MappingElement[sourceSize][targetSize];//new NodesMatrixMapping(new MatchMatrix(), source, target);
//		filteredMapppings = new MappingElement[sourceSize];
		spsmMapppings = new HashMapping<INode>(source,target);
		

		Iterator<IMappingElement<INode>> mappingIt = mappings.iterator();
		while(mappingIt.hasNext()){
			IMappingElement<INode> mapping = mappingIt.next();
			INode s = mapping.getSource();
			INode t = mapping.getTarget();
			int sourceIndex = sourceVector.indexOf(s);
			int targetIndex = targetVector.indexOf(t);
			cNodeMatrix[sourceIndex][targetIndex] = mapping;	
		}
		this.mappings2 = mappings;
		
//		orderOfSiblingsInSource = new Hashtable<String,Integer>();
//		orderOfSiblingsInTarget = new Hashtable<String,Integer>();
//		fillOrderedSiblingsList();
	}
	
//	private IMappingElement<INode> getMappingForCNodeMatrix(INode source, INode target, 
//			IMappingElement<INode> imap){
//		IMappingElement<INode> me = new MappingElement<INode>(
//				source, target, imap.getRelation()
//		);
//
//		return me;
//	}
	
	/**
	 * Checks if the given source and target elements are related considering the
	 * defined relation and the cNodeMatrix, is the relation is held, then it is set
	 * as the strongest relation for the source and target
	 * @param source	Source element
	 * @param target	Target element
	 * @param relation	Defined relation SEMANTIC_RELATION
	 * @return	true if the relation holds between source and target, false otherwise
	 */
	protected boolean isRelated(INode source, INode target,char semantic_relation){
		
		boolean related = false;
		IMappingElement<INode> mapping = findMappingElement(source, target, semantic_relation);
		if ( mapping != null ){
				related = true;
		}
		return related;
	}
	
	
	/**
	 * Finds the mapping element in the CNodeMatrix for the given source, target and semantic relation
	 * 
	 * @param source source node
	 * @param target target node
	 * @param semantic_relation relation holding between the source and target
	 * @return Mapping between source and target for the given relation, null is there is none
	 */
	public IMappingElement<INode> findMappingElement(INode source, INode target, char semantic_relation){
		IMappingElement<INode> me = null;
		
		int sourceIndex = -1;
		int targetIndex = -1;
		sourceIndex = sourceVector.indexOf(source);
		targetIndex = targetVector.indexOf(target);
		
		if ( sourceIndex >= 0 && targetIndex >= 0 ){

			if ( cNodeMatrix[sourceIndex][targetIndex].getRelation() == semantic_relation){

				me = cNodeMatrix[sourceIndex][targetIndex];
			}
		}
		
		return me;
	}
	
	/**
	 * Set the strongest mapping element for the data translation
	 * for the given source and target 
	 * @param source source node 
	 * @param target target node
	 */
	public void setStrongestMapping(INode source, INode target){
	
		int sourceIndex = sourceVector.indexOf(source);
		int targetIndex = targetVector.indexOf(target);
		
		if (sourceIndex >= 0 && targetIndex >= 0){
			setStrongestMapping(sourceIndex,targetIndex);
		}
	}
	
	/**
	 * Set the given column and row as the strongest mapping element for the given 
	 * source and target indexes of the cNodeMatrix, setting all the other realtions
	 * for the same row (source) as IDK if the relations are weaker
	 * @param row
	 * @param col
	 */
	public void setStrongestMapping(int row, int col){
	
		//if it's structure preserving
		if (isSameStructure(sourceVector.get(row),targetVector.get(col))){
//			spsmMapppings[row] = cNodeMatrix[row][col];
			spsmMapppings.add( cNodeMatrix[row][col]);
			
			//TODO check whether all the other relations from the matrix should be discarded
			
			
			//deletes all the less precedent relations in the row
			//i.e., for the same source node
			for(int j = 0; j < targetSize; j++){
				//if its not the target of the mapping elements and the relation is weaker
				if(j != col && morePrecedent(cNodeMatrix[row][col],cNodeMatrix[row][j])){
					cNodeMatrix[row][j] = convertToIDK(cNodeMatrix[row][j]);
				}
			}
			
			//deletes all the relations in the column
			for(int i = 0; i < sourceSize; i++){
				if(i != row ){
					cNodeMatrix[i][col] = convertToIDK(cNodeMatrix[i][col]);
				}
			}
		} else {
			//the elements are not in the same structure, look for the correct relation
			computeStrongestMappingForSource(sourceVector.get(row));
		}
			
	}
	
	
	/**
	 * Takes the existing IMappingElement and copies the source and target into a
	 * new instance of IMappingElement with the IDK relation
	 * @param mapping
	 * @return
	 */
	private IMappingElement<INode> convertToIDK(IMappingElement<INode> mapping){
		
		IMappingElement<INode> newMapping = new MappingElement<INode>(
				mapping.getSource(),
				mapping.getTarget(),
				IMappingElement.IDK
				);
		
		return newMapping;
	}
	
	
	/**
	 * Looks for the strongest relation for the given source and sets to
	 * IDK all the other mappings existing for the same source if they are less
	 * precedent
	 *  
	 * @param source INode to look for the strongest relation 
	 */
	public void computeStrongestMappingForSource(INode source){

		int sourceIndex = sourceVector.indexOf(source);
		int strongetsRelationInTarget = -1;
		if (sourceIndex >= 0){
			
			List<IMappingElement<INode>> strongest = new ArrayList<IMappingElement<INode>>();
			
			//look for the strongest relation, and deletes all the not structure 
			//preserving relations
			for(int j = 0; j < targetSize; j++){
				if (isSameStructure(source,targetVector.get(j))){
					if (strongest.isEmpty()){
						strongetsRelationInTarget = j;
						strongest.add(cNodeMatrix[sourceIndex][j]);
					} else {
						int precedence = comparePrecedence(strongest.get(0).getRelation()
								,cNodeMatrix[sourceIndex][j].getRelation());
						if ( precedence == -1){
							strongetsRelationInTarget = j;
							strongest.set(0,cNodeMatrix[sourceIndex][j]);
						} 
					}
				} else {
					//they are not the same structure, function to function, variable to variable
					//delete the relation
					cNodeMatrix[sourceIndex][j] = convertToIDK(cNodeMatrix[sourceIndex][j]);
				}
			}
			
			//if there is a strongest element, and it is different from IDK
			if ( !strongest.isEmpty() && strongest.get(0).getRelation() != IMappingElement.IDK){// SEMANTIC_RELATION.NOT_RELATED){
				//erase all the weaker relations
				for(int j = 0; j < targetSize; j++){
					if (j != strongetsRelationInTarget ){
						int precedence = comparePrecedence(strongest.get(0).getRelation(),cNodeMatrix[sourceIndex][j].getRelation());
						if( precedence == 1){
							cNodeMatrix[sourceIndex][j] = convertToIDK(cNodeMatrix[sourceIndex][j]);//.setSemanticRelation( SEMANTIC_RELATION.NOT_RELATED);
						} else if ( precedence == 0){
							if (isSameStructure(source,targetVector.get(j))){
								strongest.add(cNodeMatrix[sourceIndex][j]);
							}	
						}
					}
				}
				
				//if there is more than one strongest relation
				if (strongest.size() > 1){
					resolveMappingConflicts(sourceIndex, strongest);
				} else {
					
					//deletes all the relations in the column
					for(int i = 0; i < sourceSize; i++){
						if(i != sourceIndex ){
							cNodeMatrix[i][strongetsRelationInTarget] = convertToIDK(cNodeMatrix[i][strongetsRelationInTarget]);//.setSemanticRelation( SEMANTIC_RELATION.NOT_RELATED);
						}
					}
					
//					spsmMapppings[sourceIndex] = strongest.get(0);
					spsmMapppings.add(strongest.get(0));
				}
			} 
//			else {
//				spsmMapppings[sourceIndex] = convertToIDK(cNodeMatrix[sourceIndex][0]);
//			}
			

		}
	}
	
	/**
	 * Used to resolve conflicts in case there are more than one element with
	 * the strongest relation for a given source node
	 */
	private void resolveMappingConflicts(int sourceNodeIndex, List<IMappingElement<INode>> strongest){
		//copy the relations to a string to log it
		int strongestIndex = -1;
		String sourceString = sourceVector.get(sourceNodeIndex).getNodeData().getName().trim();
		String strongRelations = "";
		for(int i = 0; i < strongest.size() ; i++){
			strongRelations += strongest.get(i).getTarget().toString()+"|";
		}
		log.info("more tha one strongest relation for "+
				sourceString +
				": |"+strongRelations);

       	
       	//looks the first related node that is equal to the source node 
       	for(int i = 0; i < strongest.size() ; i++){
       		String strongString = strongest.get(i).getTarget().toString().trim();
       		if (sourceString.equalsIgnoreCase(strongString)){
       			strongestIndex = i;
       			break;
       		}
       	}
       	
       	//if there was no equal string, then set it to the first one
       	if(strongestIndex == -1)
       		strongestIndex = 0;
       	
//		spsmMapppings[sourceNodeIndex] = strongest.get(strongestIndex);
		spsmMapppings.add(strongest.get(strongestIndex));
		
		//TODO Remove the relations from the same column and row
	}
	
	/**
	 * Checks if source and target are structural preserving 
	 * this is, function to function and argument to argument match
	 * @param source
	 * @param target
	 * @return true if they are the same structure, false otherwise
	 */
	private boolean isSameStructure(INode source, INode target){
		boolean result = false;
		if(source != null && target != null){
			if(source.getChildrenList() != null && target.getChildrenList() != null){
				int sourceChildren = source.getChildrenList().size();
				int targetChildren = target.getChildrenList().size();
				if (sourceChildren == 0 && targetChildren == 0){
					result = true;
				}
				else if (sourceChildren > 0 && targetChildren > 0){
					result = true;
				}
			} else if (source.getChildrenList() == null && target.getChildrenList() == null){
				result = true;
			}
		}
		else if (source == null && target == null){
			result = true;
		}
		return result;
	}
	
	
	/**
	 * Checks if the semantic relation of the source is more important, in the order of precedence
	 * than the one in the target, the order of precedence is, = > < ?
	 * @param source source Mapping element
	 * @param target target Mapping element
	 * @return
	 */
	private boolean morePrecedent(IMappingElement<INode> source, IMappingElement<INode> target){
		
		return  comparePrecedence(source.getRelation(), target.getRelation()) == 1?true: false;

	}
	
	
	/**
	 * Finds all mappings related to the source node represented by the string
	 * @param sourceStr String representation of the node
	 * @return a vector with all the mappings corresponding to specified node
	 */
	public List<IMappingElement<INode>> findRelatedMappings(INode source ){
		List<IMappingElement<INode>> relatedMappings = new ArrayList<IMappingElement<INode>>();

		int sourceIndex = -1;
		sourceIndex = sourceVector.indexOf(source);

		
		//first find all mappings related to the source node 
		for (int j = 0; j < targetSize ; j++){
				relatedMappings.add(cNodeMatrix[sourceIndex][j]);
		}
		
		return relatedMappings;
		
	}
	
	/**
	 * Compares the semantic relation of the source and target in the order of precedence
	 * = > < ! ?. Returning -1 if source_relation is less precedent than target_relation,
	 * 			0 if source_relation is equally precedent than target_relation,
	 * 		 	1 if source_relation  is more precedent than target_relation
	 * @param source_relation source relation from IMappingElement
	 * @param target_relation target relation from IMappingElement
	 * @return 	-1 if source_relation is less precedent than target_relation,
	 * 			0 if source_relation is equally precedent than target_relation,
	 * 		 	1 if source_relation  is more precedent than target_relation
	 */
	private int comparePrecedence(char source_relation,  char target_relation){ 
		int result = -1;
		
		int sourcePrecedence = getPrecedenceNumber(source_relation);
		int targetPrecedence = getPrecedenceNumber(target_relation);
		if (sourcePrecedence < targetPrecedence)
		{
			result = 1;
		} else if (sourcePrecedence == targetPrecedence){
			result = 0;
		} else {
			result = -1;
		}
		
		return result;
	}

	
//	/**
//	 * Compares the semantic relation of the source and target in the order of precedence
//	 * = > < ! ?
//	 * @param source source SEMANTIC_RELATION
//	 * @param target target SEMANTIC_RELATION
//	 * @return -1 if is less precedent, 0 if equally precedent, 1 if it is more precedent
//	 */
//	private int comparePrecedence( IMappingElement<INode> source,  IMappingElement<INode> target){
//		
//		return comparePrecedence(source.getRelation(),target.getRelation());
//	}
	
	/**
	 * Gives the precedence order for the given SEMANTIC_RELATION. 
	 * EQUIVALENT_TO = 1
	 * MORE_GENERAL = 2
	 * LESS_GENERAL = 3
	 * DISJOINT_FROM = 4
	 * NOT_RELATED = 5
	 * 
	 * @param source source SEMANTIC_RELATION
	 * @return the order of precedence for the given relation, Integer.MAX_VALUE if the relation
	 * 			is not recognized
	 */
	private int getPrecedenceNumber( char relation){
		
		//initializes the procedence number to the least precedent 
		int precedence = Integer.MAX_VALUE;
		
		if (relation == IMappingElement.EQUIVALENCE){
			precedence = 1;
		} else if (relation == IMappingElement.MORE_GENERAL){
			precedence = 2;
		} else if (relation == IMappingElement.LESS_GENERAL){
			precedence = 3;
		} else if (relation == IMappingElement.DISJOINT){
			precedence = 4;
		} 
//		else if (relation == SEMANTIC_RELATION.NOT_RELATED){
//			precedence = 5;
//		}
		
		return precedence;
	}
	
	
//	/**
//	 * prepares the data translation vector to return the mapping elements.
//	 * if there are missing elements of the target in the data translation
//	 * vector, then it adds them with an empty source, in this way the result
//	 * is simetrical, this is, the set of elements that the matcher return
//	 * is the same for match(a,b) and match(b,a)
//	 * @return
//	 */
//	public MappingElement<INode>[] getDataTranslationVector() {
//		
//		HashSet<String> usedTargetElements = new HashSet<String>();
//		List<IMappingElement<INode>> temDTVector = new ArrayList<IMappingElement<INode>>();
//		
//		//set the objects in the source and target objects from INode to string
//		for (int i = 0 ; i < sourceSize; i++){
//			if (dataTranslationVector[i] == null){
//				setStrongestMapping(sourceVector.get(i));
//			}
//			transformSourceTargetMapping(dataTranslationVector[i]);
//			temDTVector.add(dataTranslationVector[i]);
//			
//			//marks the elements in the target structure that were already used
//			if(dataTranslationVector[i].getTarget() != null &&
//					((String)dataTranslationVector[i].getTarget().getNodeData().getName()).length() > 0){
//				usedTargetElements.add(getPathToRoot(dataTranslationVector[i].getTarget(), "target"));
//			}
//			
//		}
//		
//		//checks that all the nodes in the target vector are
//		//in the data translation array, if not, add the missing
//		//elements with null in the source mapping element, 
//		//not related as relation and similarity = 0
//		for(INode targetElem:targetVector){
//			String targetPath = getPathToRoot( targetElem,"target");
//			if(!usedTargetElements.contains(targetPath)){
//				//create a new MappingElement
//				
//				MappingElement missingTarget = new MappingElementImpl();
//
//				missingTarget.setSemanticRelation(SEMANTIC_RELATION.NOT_RELATED);
//				missingTarget.setSimilarity(0);
//				missingTarget.setSourceStructureConcept("");
//				missingTarget.setTargetStructureConcept(targetPath);
//				temDTVector.add(missingTarget);
//			}
//		}
//		
//		//if there are more elements in the temporary vector, we should copy this elements
//		//into the data translation vector
//		if(temDTVector.size() > dataTranslationVector.length){
//			MappingElement[] auxDTVector = new MappingElement[temDTVector.size()];
//			for(int i = 0; i < temDTVector.size(); i++){
//				auxDTVector[i] = temDTVector.get(i);
//			}
//			dataTranslationVector = auxDTVector;
//		}
//		
//		
//		return dataTranslationVector;
//	}
	
//	/**
//	 * Prepares the source and target objects to be used in the format needed by the QA
//	 * @param me Mapping element to be transformed
//	 */
//	private void transformSourceTargetMapping(IMappingElement<INode> me){
//
//		me.setSourceStructureConcept( getPathToRoot((INode) me.getSourceStructureConcept(),"source")  );
//		me.setTargetStructureConcept( getPathToRoot((INode) me.getTargetStructureConcept(),"target")   );
//		
//		
//
//	}
	
//	private String getPathToRoot(Object node, String origin){
//		StringBuffer path = new StringBuffer();
//		if (node != null){
//			path.append("/");
//			if (node instanceof INode ) {	
//				
//				INode concept = (INode)node;
//
//				Vector<INode> reversePath = concept.getAncestors();
//				if (concept.isRoot())
//					path.append("/");
//				for (int i = reversePath.size() - 1; i >= 0; i--) {
//					INode c = (reversePath.elementAt(i));
//					path.append(getNodeName(c, origin)).append("/");
//				}
//				path.append(getNodeName(concept,origin));
//				
//			}
//			else{
//				path.append(node.toString());
//			}
//		} else {
//			path.append( new String("") );
//		}
//        return path.toString().trim();
//		
//	}
	
//	private String getNodeName(INode node, String origin){
//		String name = "";
//		String order ="";
//		Integer ord = null;
//		if (origin.equals("source")) {
//			ord = sourceOrgerInSibligs.get(((Node)node).getId());
//		} else {
//			ord = targetOrgerInSibligs.get(((Node)node).getId());
//		}
//		
//		if (ord != null){
//			order = "[" + ord +  "]";
//		}
//			
//		name = node.getNodeData().getName().trim() + order.trim(); 
//		
//		return name;
//	}
	
	
	
//	private void fillOrderedSiblingsList(){
//		for(int i=0; i < sourceVector.size();i++){
//			setNodeOrderInSiblings( sourceVector.get(i).getChildrenList(), "source");
//		}
//		
//		for(int i=0; i < targetVector.size();i++){
//			setNodeOrderInSiblings( targetVector.get(i).getChildrenList(), "target");
//		}
//		
//	}
	
	
//	/**
//	 * fills the hash that contains the index of the order of each
//	 * node according to its position between its siblings
//	 * @param vect
//	 * @param origin
//	 */
//	private void setNodeOrderInSiblings(List<INode> vect, String origin){
//
//		if (origin.equals("source")){
//			for(int i= 0; i<  vect.size();i++ ){
//				if( !orderOfSiblingsInSource.containsKey( ((Node)vect.get(i)).getId()) ){
//					orderOfSiblingsInSource.put( ((Node)vect.get(i)).getId(), i);
//
//
//				} else {
//					System.out.println("CLAVE DUPLICADA IN SOURCE!!!!!!!!!!!!!!");
//				}
//			}
//		} else {
//
//			for(int i= 0; i<  vect.size();i++ ){
//				if( !orderOfSiblingsInTarget.containsKey( ((Node)vect.get(i)).getId())){
//					orderOfSiblingsInTarget.put( ((Node)vect.get(i)).getId(), i);
//				} else {
//					System.out.println("CLAVE DUPLICADA IN TARGET!!!!!!!!!!!!!!");
//				}
//			}
//		}
//	}
	


	

    
	/**
	 * 
	 */
	public IContextMapping<INode> getFilteredMappings() {
		return spsmMapppings;
	}	
	
}
