package org.eclipse.twig.core.search;

import org.eclipse.twig.core.parser.ITwigNodeVisitor;
import org.eclipse.twig.core.parser.TwigCommonTree;
import org.eclipse.twig.core.parser.TwigParser;


/**
 * 
 * 
 * 
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public class LocalVariableOccurrencesFinder extends AbstractOccurrencesFinder {

	@Override
	public String getElementName() {
		
		if (twigNode != null)
			return twigNode.getName();
		
		
		return null;
	}


	@Override
	protected ITwigNodeVisitor getVisitor(int offset) {

		return new NodeVisitor(offset);
		
	}
	
	private class NodeVisitor implements ITwigNodeVisitor {

		private final int curOffset;

		public NodeVisitor(int offset) {

			this.curOffset = offset;
		}


		@Override
		public void beginVisit(TwigCommonTree node) {

			int nodeStart = node.getCharPositionInLine() + curOffset /*+ 1*/;
			int length = node.getText() != null ? node.getText().length() : 0;
			
			// simply match all occurrences for strings
			// needs to be refined
			if (node.getType() == twigNode.getType() && node.getType() == TwigParser.STRING) {
				
				String text  = node.getText();
				
				if (twigNode.getName().equals(text)) {
					OccurrenceLocation location = new OccurrenceLocation(nodeStart, length, F_READ_OCCURRENCE, text);
					locations.add(location);
				}
			}
		}

		@Override
		public void endVisit(TwigCommonTree node) {


		}		
	}
}