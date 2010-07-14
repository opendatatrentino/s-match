package it.unitn.disi.smatch.loaders.context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;


import it.unitn.disi.smatch.matchers.structure.tree.spsm.ITreeConverter;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.trees.Context;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;


/**
 * Implements ITreeConverter to convert a function like string to a tree structure.
 * The string to be converted in the form: fn(arg,arg,..). where arg can be fn(arg,..)
 * 
 * @author Juan Pane pane@disi.unitn.it
 *
 */
public class FunctionContextLoader extends TabContextLoader implements IContextLoader {

	private static final Logger log = Logger.getLogger(FunctionContextLoader.class);

	private final char COMA = ',';
	private final char OPEN_PARENTHESIS = '(';
	private final char CLOSE_PARENTHESIS = ')';

	IContext ctx = null;


	/**
	 * Function to convert the string representing a function, to a tree represented in IContext 
	 * to be used in the Matcher
	 * @param in 	the BufferedTReader containing a single line with 
	 * 				the string to be converted in the form: fn(arg,arg,..). 
	 * 				where arg can be fn(arg,..)
	 * @return IContext with the tree representation
	 */
	@Override //TODO change the visibility of same method in FunctionContextLoader
	protected IContext process(BufferedReader input) throws IOException {
		ctx = new Context();
        final String fnLine = input.readLine();
		parse(fnLine,null);
		return ctx;
    }
	
	/**
	 * Adds a node to the tree
	 * @param parent 	parent node, if this is null then we are creating the root of the tree
	 * 					and the child parameter represents the name of the root
	 * @param child 	child to be added
	 * @return the ID of the inserted node
	 */
	private INode addChild(INode parent, String child){

		INode newChild = null;
		if(parent == null){ //if this is true, then it is the root of the tree
			newChild = ctx.createRoot(child);
		} else {
			newChild = parent.createChild(child);
		}

		return newChild;
	}

	/**
	 * Parses the string to get the children of the given node
	 * @param inString string to be parsed/tokenized
	 * @param parent the parent node of the parsed tokens
	 */ 
	private void parse(String inString, INode parent){

		Vector<String> tokens = getComaTokens(inString);
		for (int i = 0; i<tokens.size();i++){
			int isFunction = tokens.elementAt(i).indexOf(OPEN_PARENTHESIS);
			if(isFunction >= 0){

				String func_name = tokens.elementAt(i).substring(0,isFunction);
				INode newParent = addChild( parent,  func_name);
				String arguments = tokens.elementAt(i).substring(isFunction+1,tokens.elementAt(i).length()-1);
				parse(arguments, newParent);
			} else {
				addChild( parent,  tokens.elementAt(i));
			}
		}
	}

	/**
	 * Tokenizes the string into siblings
	 * @param inString string to be parsed, in the form arg,arg,.. where arg can be fn(arg,arg,..)
	 * @return vector of siblings
	 */
	private Vector<String> getComaTokens(String inString){
		//
		String input = inString.trim();
		Vector<String> tokens = new Vector<String>();
		String token = "";
		while(input.length() > 0){
			if (input.charAt(0) == COMA){
				input = input.substring(1);
			}
			token = getNextComaToken(input);

			input = input.substring(token.length());
			tokens.add(token.trim());
		}


		return tokens;

	}

	/**
	 * Computes the first argument from a list of arguments
	 * @param inString arg,arg,... arg can also be fn(arg,..)
	 * @return the first argument of the given string
	 */
	private String getNextComaToken(String inString){
		String input = inString;
		String token = "";

		//number of open parenthesis
		int parenthesis = 0;

		for(int i= 0 ; i < input.length(); i++){

			if ( OPEN_PARENTHESIS == input.charAt(i)){
				parenthesis++;
				token += input.charAt(i);

			} else if (CLOSE_PARENTHESIS == input.charAt(i)){
				parenthesis--;
				token += input.charAt(i);
			} else if (COMA == input.charAt(i)){
				//if there is no open parenthesis finish
				if (parenthesis == 0){
					break;
				} else{
					token += input.charAt(i);
				}
			} else {
				token += input.charAt(i);
			}

		}

		return token;

	}



	//TODO remove after changes of aliaksandr
//    public IContext loadContext(String fileName) throws ContextLoaderException {
//        IContext result = null;
//        try {
//            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
//            try {
//                result = process(input);
//                log.info("Parsed nodes: " + nodesParsed);
//                createIds(result);
//            } finally {
//                input.close();
//            }
//        } catch (IOException e) {
//            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
//            log.error(errMessage, e);
//            throw new ContextLoaderException(errMessage, e);
//        }
//        return result;
//    }





}
