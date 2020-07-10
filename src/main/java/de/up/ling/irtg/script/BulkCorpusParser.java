package de.up.ling.irtg.script;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.codec.CodecParseException;
import de.up.ling.irtg.corpus.Corpus;
import de.up.ling.irtg.corpus.CorpusReadingException;
import de.up.ling.irtg.corpus.CorpusWriter;
import de.up.ling.irtg.corpus.InterpretationPrintingPolicy;
import de.up.ling.irtg.gui.JTreeAutomaton;
import de.up.ling.irtg.util.ProgressListener;

import java.io.*;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * This reads an (annotated or unannotated) corpus, parses all inputs,
 * and saves the inputs and outputs to a file, exhibiting the same
 * behavior found in the GUI-based bulk parser. <p>
 *
 * See {@link JTreeAutomaton#miBulkParseActionPerformed} for the original GUI code.)<p>
 * 
 * Usage: java BulkCorpusParser &lt;IRTG&gt; &lt;corpus&gt; &lt;output file&gt;
 * 
 * @author David M. Howcroft
 */

@Command(description = "Parses an Alto corpus file with the given grammar and saves the output",
        name = "BulkCorpusParser", mixinStandardHelpOptions = true)
public class BulkCorpusParser implements Callable<Void> {
    @Parameters(index="0") static private String grammarFilepath;
    @Parameters(index="1") static private String inputCorpusFilepath;
    @Parameters(index="2") static private String outputCorpusFilepath;
    @Option(names="-n") static private int numParsesToInclude = 10;

    public static void main(String[] args) {
        CommandLine.call(new BulkCorpusParser(), args);
    }

    @Override
    public Void call() throws IOException, CorpusReadingException, CodecParseException {
        // Prepare input and output streams
        InterpretedTreeAutomaton irtg = InterpretedTreeAutomaton.read(new FileInputStream(grammarFilepath));
        FileWriter outputWriter = new FileWriter(outputCorpusFilepath);

        String parsingMetadata = "Parsed from " + inputCorpusFilepath + "\nat " + new Date().toString();
        // TODO change this so it uses whatever comment format is used in the original corpus
        String commentPrefix = "# ";

        InterpretationPrintingPolicy printingPolicy = InterpretationPrintingPolicy.fromIrtg(irtg);
        final CorpusWriter corpusWriter = new CorpusWriter(irtg, parsingMetadata, commentPrefix, printingPolicy, outputWriter);
        corpusWriter.setPrintSeparatorLines(true);
        corpusWriter.setAnnotated(true);

        ProgressListener listener = (currentValue, maxValue, string) -> {};

        Corpus corpus = Corpus.readCorpus(new FileReader(inputCorpusFilepath), irtg);
//        irtg.bulkParse(corpus, corpusWriter, listener);
        irtg.bulkParseNbest(corpus, corpusWriter, listener, numParsesToInclude);
        outputWriter.flush();
        outputWriter.close();

        return null;
    }
}
