package edu.stanford.nlp.parser.tools;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.international.Languages;
import edu.stanford.nlp.international.Languages.Language;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.TreebankLangParserParams;
import edu.stanford.nlp.trees.DiskTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.StringUtils;

/**
 * Counts the number of trees in a (PTB format) treebank file. Also provides
 * flags for printing (after processing by the various language packs)
 * and flattening the trees.
 * 
 * @author Spence Green
 *
 */
public class CountTrees {

  private static final int minArgs = 1;
  private static final String usage;
  static {
    StringBuilder sb = new StringBuilder();
    String nl = System.getProperty("line.separator");
    sb.append(String.format("Usage: java %s [OPTS] tree_file%s%s",CountTrees.class.getName(),nl,nl));
    sb.append("Options:\n");
    sb.append("  -l lang    : Select language settings from " + Languages.listOfLanguages()).append(nl);
    sb.append("  -e enc     : Encoding.").append(nl);
    sb.append("  -y len     : Only print trees with yields <= len.").append(nl);
    sb.append("  -a         : Only print the pre-terminal yields, one per line.").append(nl);
    sb.append("  -p         : Print the trees to stdout.").append(nl);
    sb.append("  -f         : Flatten the trees and print to stdout.").append(nl);
    sb.append("  -t         : Print TnT style output.").append(nl);
    usage = sb.toString();
  }
  
  public static final Map<String,Integer> optionArgDefinitions = new HashMap<String,Integer>();
  static {
    optionArgDefinitions.put("l", 1);
    optionArgDefinitions.put("e", 1);
    optionArgDefinitions.put("y", 1);
    optionArgDefinitions.put("a", 0);
    optionArgDefinitions.put("p", 0);
    optionArgDefinitions.put("f", 0);
    optionArgDefinitions.put("t", 0);
  }

  public static void main(String[] args) {
    if(args.length < minArgs) {
      System.out.println(usage);
      System.exit(-1);
    }
    
    // Process command-line options
    Properties options = StringUtils.argsToProperties(args, optionArgDefinitions);
    String fileName = options.getProperty("");
    if (fileName == null || fileName.equals("")) {
      System.out.println(usage);
      System.exit(-1);
    }
    int maxLen = PropertiesUtils.getInt(options, "y", Integer.MAX_VALUE);
    boolean printTrees = PropertiesUtils.getBool(options, "p", false);
    boolean flattenTrees = PropertiesUtils.getBool(options, "f", false);
    boolean printPOS = PropertiesUtils.getBool(options, "a", false);
    boolean printTnT = PropertiesUtils.getBool(options, "t", false);
    Language language = PropertiesUtils.get(options, "l", Language.English, Language.class);
    TreebankLangParserParams tlpp = Languages.getLanguageParams(language);
    String encoding = options.getProperty("e", "UTF-8");
    tlpp.setInputEncoding(encoding);
    tlpp.setOutputEncoding(encoding);
    
    DiskTreebank tb = tlpp.diskTreebank();
    tb.loadPath(fileName);
    
    // Read the treebank
    PrintWriter pw = tlpp.pw();
    int numTrees = 0;
    for (Tree tree : tb) {
      if(tree.yield().size() > maxLen) continue;
      ++numTrees;
      if (printTrees) {
        pw.println(tree.toString());
        
      } else if (flattenTrees) {
        pw.println(Sentence.listToString(tree.yield()));
        
      } else if (printPOS) {
        pw.println(Sentence.listToString(tree.preTerminalYield()));
      
      } else if (printTnT) {
        List<CoreLabel> yield = tree.taggedLabeledYield();
        for (CoreLabel label : yield) {
          pw.printf("%s\t%s%n", label.word(), label.tag());
        }
        pw.println();
      }      
    }
    System.err.printf("Read %d trees.%n", numTrees);
  }
}
