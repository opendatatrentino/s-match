package it.unitn.disi.smatch.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;



import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.HashMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.SPSMTreeMatcher;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.TreeEditDistance;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.ITreeAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.data.impl.CTXMLTreeAccessor;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.InvalidElementException;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.MatchedTreeNodeComparator;
import it.unitn.disi.smatch.matchers.structure.tree.spsm.ted.utils.impl.WorstCaseDistanceConversion;

/**
 * Class used for reordering the siblings in a tree based on the
 * relations returned by the matching component. It is also used for 
 * filtering the relations in order to
 * return 1 to 1 correspondences for the data translation. this is done in the same class for the 
 * purpose of efficiency
 *  
 * @author Juan Pane pane@disi.unitn.it
 */
public class SPSMMappingFilter extends BaseFilter implements IMappingFilter {

	private IContext sourceContext;
	private IContext targetContext;
	
    //used for reordering of siblings
	private Vector<Integer> sourceIndex; 
	private Vector<Integer> targetIndex;
		

//	private Vector<IMappingElement> mappings;
	/**
	 * the original mappings
	 */
	private int numberofOriginalMappings;
//	private IContextMapping<INode> mappings;
	
	//Class used to verify the relations between nodes, and build the QA mechanism
//	private SPSMMappingFilter spsmFilter;
	
	
	
	/////////////////////////////////////////////////
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
	private IContextMapping<INode> spsmMappings;
	
//	private Hashtable<String,Integer> orderOfSiblingsInSource;
//	private Hashtable<String,Integer> orderOfSiblingsInTarget;
	
//	IContextMapping<INode> mappings2;
	
	private static final Logger log = Logger.getLogger(SPSMMappingFilter.class);

	/** Contains the original mappings from which it will be computed the 
	 * similarity score and then the filtering will take place **/
	private IContextMapping<INode> originalMappings;
	
	/**
	 * Constructor of the class for 
	 * @param source Source Context
	 * @param target Target Context
	 * @param mapp	Mapping or semantic relations among the nodes in source and target  
	 */
	@SuppressWarnings("unchecked")
	private void init(IContextMapping<INode> mappings){
		sourceContext = mappings.getSourceContext();
		targetContext = mappings.getTargetContext();
		sourceIndex = new Vector<Integer>();
		targetIndex = new Vector<Integer>();
//		mappings = (Vector<IMappingElement>) mapp.getMapping().clone();
//		this.mappings = mapp;
//		spsmFilter = new SPSMMappingFilter( sourceContext,targetContext,mappings );
		
		sourceVector = mappings.getSourceContext().getNodesList();
		targetVector = mappings.getTargetContext().getNodesList();
		
		sourceSize = sourceVector.size();
		targetSize = targetVector.size();

		cNodeMatrix = new MappingElement[sourceSize][targetSize];//new NodesMatrixMapping(new MatchMatrix(), source, target);
//		filteredMapppings = new MappingElement[sourceSize];
		spsmMappings = new HashMapping<INode>(mappings.getSourceContext(),
												mappings.getTargetContext());
		

		Iterator<IMappingElement<INode>> mappingIt = mappings.iterator();
		while(mappingIt.hasNext()){
			IMappingElement<INode> mapping = mappingIt.next();
			INode s = mapping.getSource();
			INode t = mapping.getTarget();
			int sourceIndex = sourceVector.indexOf(s);
			int targetIndex = targetVector.indexOf(t);
			cNodeMatrix[sourceIndex][targetIndex] = mapping;	
		}
		
		numberofOriginalMappings = mappings.size();
		originalMappings = mappings;
		
	}
	
	/**
	 * Sorts the siblings in the source and target tree defined in the constructor using 
	 * the given mapping 
	 */
	@Override
	public IContextMapping<INode> filter(IContextMapping<INode> mapping)
			throws MappingFilterException {
		//initialize the local variables
		init(mapping);
		computeSimilarity();
		
		IContextMapping<INode> res = null;

		//add the first mapping element for the root to the mappings result
		if (numberofOriginalMappings > 0){
			
			if (	isRelated(sourceContext.getRoot(), targetContext.getRoot(), IMappingElement.EQUIVALENCE) || 
					isRelated(sourceContext.getRoot(), targetContext.getRoot(), IMappingElement.LESS_GENERAL) ||
					isRelated(sourceContext.getRoot(), targetContext.getRoot(), IMappingElement.MORE_GENERAL)){
			
				setStrongestMapping(sourceContext.getRoot(), targetContext.getRoot());
				filterMappingsOfChildren(sourceContext.getRoot(), targetContext.getRoot());

			}
			res = spsmMappings; 
		}
		else{
			log.info("No remaining mappings after appliying the SPSM mapping filter. " +
					"Initially there were "+numberofOriginalMappings+ " mappings.");
		}
		return res;
	}
	
	
	/**
	 * Computes the similarity score according to the definition provided in 
	 * http://eprints.biblio.unitn.it/archive/00001459/
	 */
	private void computeSimilarity() {

		TreeEditDistance tde = null;
		try {
    		ITreeAccessor source = new CTXMLTreeAccessor(sourceContext);
    		ITreeAccessor target = new CTXMLTreeAccessor(targetContext);
    		MatchedTreeNodeComparator mntc = new MatchedTreeNodeComparator(originalMappings);
    		
			tde = new TreeEditDistance(source, target, mntc, new WorstCaseDistanceConversion());

		} catch (InvalidElementException e) {
        	log.info("Problems in the Tree edit distance computation:" +e.getMessage());
        	if (log.getEffectiveLevel() == Level.TRACE)
        		log.trace(SPSMTreeMatcher.class.getName(), e);
		}

		tde.calculate();
		double ed = tde.getTreeEditDistance();
		double sim=1-(ed/Math.max(sourceSize,targetSize));

		spsmMappings.setSimilarity(sim);
	}

	/**
	 * Sorts the children of the given nodes. 
	 * @param sourceParent Source node
	 * @param targetParent Target node
	 */
	private void filterMappingsOfChildren(INode sourceParent, INode targetParent){
		
		List<INode> source = convertToModifiableList(sourceParent.getChildrenList());
		List<INode> target = convertToModifiableList(targetParent.getChildrenList());

		sourceIndex.add(getNodeDepth(sourceParent), 0);
		targetIndex.add(getNodeDepth(targetParent), 0);


		if (source.size() > 1 && target.size() > 1){
			//sorts the siblings first with the strongest relation, and then with the others
			filterMappingsOfSibligsbyRelation(source,target,IMappingElement.EQUIVALENCE);
			filterMappingsOfSibligsbyRelation(source,target,IMappingElement.MORE_GENERAL);
			filterMappingsOfSibligsbyRelation(source,target,IMappingElement.LESS_GENERAL);
		}

		sourceIndex.remove(getNodeDepth(sourceParent));
		targetIndex.remove(getNodeDepth(targetParent));
	}
	
	
	/**
	 * converts the unmodifiable list of children to a modifiable list of children 
	 * needed when reordering the children
	 * @param unmodifiable
	 * @return
	 */
	private List<INode> convertToModifiableList(List<INode> unmodifiable){
		List<INode> modifiable = new ArrayList<INode>();
		
		for(int i =0; i<unmodifiable.size(); i++){
			modifiable.add(unmodifiable.get(i)); 
		}
		
		return modifiable;
	}

	/**
	 * Filters the mappings of two siblings node list for which the parents are also supposed to
	 * be related. Checks whether in the two given node list there is a pair of nodes related 
	 * by the given relation and if so, if deletes all the other relations for the given 2 
	 * nodes setting the current one as strongest.
	 *
	 * @param source	Source vector of siblings
	 * @param target	Target vector of siblings
	 * @param semantic_relation	Defined relation in edu.unitn.dit.smatch.MatchManager
	 */
	private void filterMappingsOfSibligsbyRelation(List<INode> source, List<INode> target, char semantic_relation){
		
		int sourceDepth = (getNodeDepth(source.get(0)) - 1);
		int targetDepth = (getNodeDepth(target.get(0)) - 1);

		
		int sourceSize=source.size();
		int targetSize=target.size();

		
		while(sourceIndex.get(sourceDepth) < sourceSize && targetIndex.get(targetDepth) < targetSize)
		{
			if( isRelated(source.get(sourceIndex.get(sourceDepth)),
									target.get(targetIndex.get(targetDepth)),
									semantic_relation) ){
				
				//sort the children of the matched node
				setStrongestMapping(source.get(sourceIndex.get(sourceDepth)), target.get(targetIndex.get(targetDepth)));
				filterMappingsOfChildren(source.get(sourceIndex.get(sourceDepth)), target.get(targetIndex.get(targetDepth)) );
				
				//increment the index
				inc(sourceIndex,sourceDepth);
				inc(targetIndex,targetDepth);

			}
			else
			{
				//look for the next related node in the target
				int relatedIndex = getRelatedIndex(source, target, semantic_relation);
				if (relatedIndex > sourceIndex.get(sourceDepth)) {
					//there is a related node, but further between the siblings
					//they should be swapped	
					swapINodes(target,targetIndex.get(targetDepth),relatedIndex);

					//filter the mappings of the children of the matched node
					filterMappingsOfChildren(source.get(sourceIndex.get(sourceDepth)), target.get(targetIndex.get(targetDepth)) );
					
					//increment the index
					inc(sourceIndex,sourceDepth);
					inc(targetIndex,targetDepth);
					
				}
				else {
					//there is not related item among the remaining siblings
					//swap this element of source with the last, and decrement the sourceSize
					swapINodes(source,sourceIndex.get(sourceDepth),(sourceSize-1));

					sourceSize--;
				}
			}

		}
	

	}

	
	/**
	 * Swaps the INodes in vect in the positions source and target
	 * @param listOfNodes		List of INodes of which the elements should be swapped
	 * @param source	index of the source element to be swapped
	 * @param target	index of the target element to be swapped
	 */
	private void swapINodes(List<INode> listOfNodes, int source,  int target){
		INode aux = null;

		aux = listOfNodes.get(source);
		listOfNodes.set(source,listOfNodes.get(target));
		listOfNodes.set(target, aux);

	}

	
	/**
	 * Looks for the related index for the source vector at the position sourceIndex 
	 * in the target vector beginning at the targetIndex position for the defined relation
	 * @param source	source vector of siblings
	 * @param target	target vector of siblings
	 * @param semantic_relation	Defined relation in edu.unitn.dit.smatch.MatchManager
	 * @return	the index of the related element in target, or -1 if there is no relate element
	 */
	private int getRelatedIndex(List<INode> source, List<INode> target, char semantic_relation){
		
		int srcIndex = sourceIndex.get(getNodeDepth(source.get(0)) - 1);
		int tgtIndex = targetIndex.get(getNodeDepth(target.get(0)) - 1);
		
		int returnIndex = -1;


		INode sourceNode = source.get(srcIndex);
		

		//find the first one who is related in the same level
		for (int i = tgtIndex+1; i < target.size(); i++){
			INode targetNode = target.get(i);
			if ( isRelated(sourceNode, targetNode, semantic_relation) ){
				setStrongestMapping(sourceNode, targetNode);
				return i;
			}
		}

		//there was no correspondence between siblings in source and target vectors
		//try to clean the mapping elements
		
		computeStrongestMappingForSource(source.get(srcIndex));
		
		
		return returnIndex;
	}



	


	/**
	 * Returns the depth of a node in a given tree, the root have a depth of 0
	 * @param node node from which we want to compute the depth
	 * @return	depth of the given node in the tree
	 */
	private int getNodeDepth(INode node){
		return node.getAncestorsList().size();
	}

	/**
	 * Increments by 1 the Integer of the given vector of integers at the index position
	 * @param vec	vector of integers
	 * @param index	index of the element to be incremented
	 */
	private void inc(Vector<Integer> vec, int index){
		vec.set(index,vec.get(index)+1);
	}
	
	/**
	 * Checks if the given source and target elements are related considering the
	 * defined relation and the cNodeMatrix, is the relation is held, then it is set
	 * as the strongest relation for the source and target
	 * @param source	Source element
	 * @param target	Target element
	 * @param relation	Defined relation SEMANTIC_RELATION
	 * @return	true if the relation holds between source and target, false otherwise
	 */
	private boolean isRelated(INode source, INode target,char semantic_relation){
		
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
	private IMappingElement<INode> findMappingElement(INode source, INode target, char semantic_relation){
		IMappingElement<INode> me = null;
		
		int sourceIndex = -1;
		int targetIndex = -1;
		sourceIndex = sourceVector.indexOf(source);
		targetIndex = targetVector.indexOf(target);
		
		if ( sourceIndex >= 0 && targetIndex >= 0 ){

			if ( cNodeMatrix[sourceIndex][targetIndex] != null 
					&& cNodeMatrix[sourceIndex][targetIndex].getRelation() == semantic_relation){

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
	private void setStrongestMapping(INode source, INode target){
	
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
	private void setStrongestMapping(int row, int col){
	
		//if it's structure preserving
		if (isSameStructure(sourceVector.get(row),targetVector.get(col))){
//			spsmMapppings[row] = cNodeMatrix[row][col];
			spsmMappings.add( cNodeMatrix[row][col]);
			
			//TODO check whether all the other relations from the matrix should be discarded
			
			
			//deletes all the less precedent relations in the row
			//i.e., for the same source node
			for(int j = 0; j < targetSize; j++){
				//if its not the target of the mapping elements and the relation is weaker
				if(j != col && cNodeMatrix[row][j] != null 
						&& morePrecedent(cNodeMatrix[row][col],cNodeMatrix[row][j])){
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
		IMappingElement<INode> newMapping = null;
		if(mapping != null){
			newMapping = new MappingElement<INode>(
					mapping.getSource(),
					mapping.getTarget(),
					IMappingElement.IDK
					);
		}
		return newMapping;
	}
	
	
	/**
	 * Looks for the strongest relation for the given source and sets to
	 * IDK all the other mappings existing for the same source if they are less
	 * precedent
	 *  
	 * @param source INode to look for the strongest relation 
	 */
	private void computeStrongestMappingForSource(INode source){

		int sourceIndex = sourceVector.indexOf(source);
		int strongetsRelationInTarget = -1;
		if (sourceIndex >= 0){
			
			List<IMappingElement<INode>> strongest = new ArrayList<IMappingElement<INode>>();
			
			//look for the strongest relation, and deletes all the non structure 
			//preserving relations
			for(int j = 0; j < targetSize; j++){
				if (isSameStructure(source,targetVector.get(j))){
					if (strongest.isEmpty() && cNodeMatrix[sourceIndex][j] != null){
						strongetsRelationInTarget = j;
						strongest.add(cNodeMatrix[sourceIndex][j]);
					} else if (cNodeMatrix[sourceIndex][j] != null) {
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
				//erase all the weaker relations in the row
				for(int j = 0; j < targetSize; j++){
					if (j != strongetsRelationInTarget && cNodeMatrix[sourceIndex][j] != null){
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
					resolveStrongestMappingConflicts(sourceIndex, strongest);
				} else {
					
					//deletes all the relations in the column
					for(int i = 0; i < sourceSize; i++){
						if(i != sourceIndex ){
							cNodeMatrix[i][strongetsRelationInTarget] = convertToIDK(cNodeMatrix[i][strongetsRelationInTarget]);//.setSemanticRelation( SEMANTIC_RELATION.NOT_RELATED);
						}
					}

			       	if(strongest.get(0).getRelation() != IMappingElement.IDK){
						spsmMappings.add(strongest.get(0));
						
						// Remove the relations from the same column and row
						deleteRemainingRelationsFromMatrix(strongest.get(0));
			       	}
				}
			} 
			

		}
	}
	
	/**
	 * Used to resolve conflicts in case there are more than one element with
	 * the strongest relation for a given source node
	 */
	private void resolveStrongestMappingConflicts(int sourceNodeIndex, 
			List<IMappingElement<INode>> strongest){
		//copy the relations to a string to log it
		int strongestIndex = -1;
		String sourceString = sourceVector.get(sourceNodeIndex).getNodeData().getName().trim();
		String strongRelations = "";
		for(int i = 0; i < strongest.size() ; i++){
			strongRelations += strongest.get(i).getTarget().toString()+"|";
		}
		log.info("more tha one strongest relation for "+
				sourceString +": |"+strongRelations);

       	
       	//looks the first related node that is equal to the source node 
       	for(int i = 0; i < strongest.size() ; i++){
       		String strongString = strongest.get(i).getTarget().getNodeData().getName().trim();
       		if (sourceString.equalsIgnoreCase(strongString)){
       			strongestIndex = i;
       			break;
       		}
       	}
       	
       	//if there was no equal string, then set it to the first one
       	if(strongestIndex == -1)
       		strongestIndex = 0;
       	
//		spsmMapppings[sourceNodeIndex] = strongest.get(strongestIndex);
       	if(strongest.get(strongestIndex).getRelation() != IMappingElement.IDK){
			spsmMappings.add(strongest.get(strongestIndex));
			
			// Remove the relations from the same column and row
			deleteRemainingRelationsFromMatrix(strongest.get(strongestIndex));
       	}
	}
	
	
	/**
	 * When a given mapping element has been chosen as the strongest, 
	 * then delete all the other mappings from the cNodeMatrix by
	 * setting the relation to IDK
	 * @param mapping
	 */
	private void deleteRemainingRelationsFromMatrix(IMappingElement<INode> mapping){
		int sourceIndex = sourceVector.indexOf(mapping.getSource());
		int targetIndex = targetVector.indexOf(mapping.getTarget());

		//deletes all the relations in the column
		for(int i = 0; i < sourceSize; i++){
			if(i != sourceIndex ){
				cNodeMatrix[i][targetIndex] = convertToIDK(cNodeMatrix[i][targetIndex]);
			}
		}
		
		//deletes all the relations in the row
		for(int j = 0; j < targetIndex; j++){
			if(j != targetIndex ){
				cNodeMatrix[sourceIndex][j] = convertToIDK(cNodeMatrix[sourceIndex][j]);
			}
		}
		
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
		
//		if(source != null && target == null){
//			return true;
//		}
		return  comparePrecedence(source.getRelation(), target.getRelation()) == 1?true: false;

	}
	
	
//	/**
//	 * Finds all mappings related to the source node represented by the string
//	 * @param sourceStr String representation of the node
//	 * @return a vector with all the mappings corresponding to specified node
//	 */
//	private List<IMappingElement<INode>> findRelatedMappings(INode source ){
//		List<IMappingElement<INode>> relatedMappings = new ArrayList<IMappingElement<INode>>();
//
//		int sourceIndex = -1;
//		sourceIndex = sourceVector.indexOf(source);
//
//		
//		//first find all mappings related to the source node 
//		for (int j = 0; j < targetSize ; j++){
//				relatedMappings.add(cNodeMatrix[sourceIndex][j]);
//		}
//		
//		return relatedMappings;
//		
//	}
	
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
	 * IDK = 5
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
		} else if (relation == IMappingElement.IDK){
			precedence = 5;
		}
		
		return precedence;
	}




	


}
