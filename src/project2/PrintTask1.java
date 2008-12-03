package project2;

import java.io.File;
import java.util.Collection;

import project2.data.LinguisticCluster;
import project2.data.Vocabulary;
import project2.processor.GoldStandard;
import edu.nlt.shallow.data.vector.DocumentVector;
import edu.nlt.util.FileProcessor;
import edu.nlt.util.Globals;
import edu.nlt.util.InputUtil;
import edu.nlt.util.Formatters;

public class PrintTask1 {
	/**
	 * Classifies standard input as Linguistic / Non-linguistic
	 * 
	 * @param args
	 * 
	 * 
	 *            args[0] - Vocabulary file
	 * 
	 *            args[1] - Cluster centroids file (generated by PrintClusters
	 *            program)
	 * 
	 *            args[2] - Tags - gold standard
	 * 
	 *            args[3] - Path to untagged documents
	 */
	public static void main(String[] args) {

		final Vocabulary vocabulary = Util.getVocabulary(new File(args[0]), -1);
		final Collection<LinguisticCluster> clusters = Util.getClusters(new File(args[1]),
				vocabulary);
		final GoldStandard goldStandard = Util.getGoldStandard(new File(args[2]));

		TestProcessor processor = new TestProcessor(vocabulary, clusters, goldStandard);
		InputUtil.processFiles(args[3], processor);
		processor.printPrescisionRecall();
	}
}

class TestProcessor implements FileProcessor {

	private Collection<LinguisticCluster> clusters;

	private GoldStandard goldStandard;

	private Vocabulary vocabulary;

	private int truePositives;
	private int relevantDocuments;
	private int documentsRetrieved;

	public TestProcessor(Vocabulary vocabulary, Collection<LinguisticCluster> clusters,
			GoldStandard goldStandard) {
		super();

		this.vocabulary = vocabulary;
		this.clusters = clusters;
		this.goldStandard = goldStandard;
	}

	@Override
	public void processFile(File file) {
		DocumentVector document = Util.getDocumentVector(file, vocabulary);

		if (Globals.IsDebugEnabled || goldStandard.isCategorized(file.getName())) {
			boolean isLinguistic = Classify.isLinguistic(clusters, document);

			System.out.println(file.getName().split("\\.")[0] + (isLinguistic ? "\tX" : ""));

			boolean isLinguisticGold = goldStandard.isLinguistic(file.getName());

			if (isLinguisticGold) {
				relevantDocuments++;
			}

			if (isLinguistic) {
				documentsRetrieved++;
				if (isLinguisticGold) {
					truePositives++;

				}
			}

			if (Globals.IsDebugEnabled) {
				if (isLinguistic && !isLinguisticGold) {
					System.err.println("False positive:\t" + file.getName());
				}

				else if (!isLinguistic && isLinguisticGold) {
					System.err.println("False negative :\t" + file.getName());
				}
			}
		}
	}

	public void printPrescisionRecall() {
		System.out.println();
		double precision = (double) truePositives / (double) documentsRetrieved;
		double recall = (double) truePositives / (double) relevantDocuments;

		System.out.println("Precision\t" + Formatters.PercentageFormatter.format(precision));
		System.out.println("Recall:\t\t" + Formatters.PercentageFormatter.format(recall));

		System.out.println("F-measure:\t"
				+ Formatters.PercentageFormatter.format((2 * recall * precision)
						/ (recall + precision)));
	}
}