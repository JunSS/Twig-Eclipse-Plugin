package org.eclipse.twig.core.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.eclipse.twig.core.parser.ITwigNodeVisitor;
import org.eclipse.twig.core.parser.TwigCommonTree;
import org.eclipse.twig.core.parser.TwigCommonTreeAdaptor;
import org.eclipse.twig.core.parser.TwigLexer;
import org.eclipse.twig.core.parser.TwigNode;
import org.eclipse.twig.core.parser.TwigParser;
import org.eclipse.twig.core.parser.TwigSourceParser;

public class NodeFinder {

	private int offset;
	private int currentPosition;
	public TwigNode node = null;

	public TwigNode find(String source, int offset) {

		try {

			this.offset = offset;			
			BufferedReader br = new BufferedReader(new StringReader(source));

			String line;

			currentPosition = 0;
			int lineNumber = 0;

			while( (line = br.readLine()) != null) {

				lineNumber++;


				if (line.contains(TwigSourceParser.TWIG_OPEN)) {
					parsePrint(line, TwigSourceParser.TWIG_OPEN, TwigSourceParser.TWIG_CLOSE, lineNumber, currentPosition);		
										
				} else if (line.contains(TwigSourceParser.STMT_OPEN)) {
					parseStatement(line, TwigSourceParser.STMT_OPEN, TwigSourceParser.STMT_CLOSE, lineNumber, currentPosition);								
				}

				// +1 for the line break which apparently doesn't count in .length();
				currentPosition += line.length() + 1;

			}

		} catch (IOException e) { 
			e.printStackTrace();
		}

		if (node != null)
			System.err.println("found node " + node);
		return node;
	}

	private void parsePrint(String line, String twigOpen, String twigClose,
			int lineNumber, int current) {


	}

	private void parseStatement(String line, String open, String close,
			int lineNumber, int current) {

		int start = 0;
		int end = 0;

		while( (start = line.indexOf(open)) >= 0) {

			end = line.indexOf(close);

			if (end == -1) {
				//TODO: report error
				break;
			}


			String twig = line.substring(start, end+2);
			parseTwig(twig, (current + start), lineNumber);

			if (line.length() > end +1) {						
				//TODO: report error
				break;
			}

			line = line.substring(end + 2);

		}		


	}

	private void parseTwig(String source, int offset, int line) {

		try {

			CharStream content = new ANTLRStringStream(source);
			TwigLexer lexer = new TwigLexer(content);

			TwigParser parser = new TwigParser(new CommonTokenStream(lexer));

			parser.setTreeAdaptor(new TwigCommonTreeAdaptor());
			TwigParser.twig_source_return root;

			root = parser.twig_source();
			TwigCommonTree tree = (TwigCommonTree) root.getTree();
			NodeVisitor visitor = new NodeVisitor(offset);
			tree.accept(visitor);


		} catch (Exception e) {
			//Logger.logException(e);
		}
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
			int nodeEnd = nodeStart + length;

			if (nodeStart <= offset && nodeEnd >= offset) {

				if (node.getType() == TwigParser.STRING) {
					
					 
					NodeFinder.this.node= new TwigNode(node.getText(), nodeStart, nodeEnd, TwigParser.STRING);
					
					

				} else if (node.getType() == TwigParser.BLOCK) {
					
					NodeFinder.this.node= new TwigNode(node.getText(), nodeStart, nodeEnd, TwigParser.BLOCK);					

				}
			}
		}

		@Override
		public void endVisit(TwigCommonTree node) {


		}		
	}	

}
