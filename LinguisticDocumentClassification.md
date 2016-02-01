# Project 2 - Ling570 Linguistic Document Classification #
# Description #
From: http://courses.washington.edu/ling570/will_fall08/570-project2.htm

For this project, you will build a vector space model over a set of documents.  For the first part of the project, your model should separate linguistic from non-linguistic documents.  For the second part, your model should facilitate search over the set of linguistic documents, and return those documents most relevant to a query.  All required files can be found in ~/dropbox/08-09/570/project2.

## The Documents ##

The data consists of a set of approximately 710 text documents, all of which were converted from PDF documents downloaded from the Web.  The set’s a mix of linguistic and non-linguistic documents, where the linguistic documents are mostly of scholarly documents discussing language data (and sometimes contain analyzed language data).  The vocabulary used in the linguistic documents tends to be distinctive for particular sub-domains of linguistics, such phonology, syntax, morphology, etc.  For example, documents that analyze a language’s morphology may use terms such as morpheme, prefix, suffix, clitic, inflection, derivation, etc.  More general linguistic terms are also likely to be used.  (Note:  (1) There is a little noise in some of the documents, due to the PDF conversion.  Your methods will likely ignore this noise since the noise will be low frequency across the set of documents.  (2) Although most documents are in English, some are not.  You may treat any of the non-English documents as non-linguistic.)

## Vectors and Distance Measures ##

There are multiple ways to measure the distance between vectors in a multi-dimensioned space, but the easiest to implement is cosine.  You are free, however, to try other methods.  Because of the very large number of words that the set of documents will contain, you will need to filter out irrelevant words using a stop list.  Even this may not be adequate, and you are free to implement other strategies to reduce dimensionality (e.g., stemming), to adjust weights, or to classify the documents.

## Task 1 ##
For this task, you’ll want to build a vector space model that will separate the documents into two sets, those that are linguistic and those that are not.  Your model will contain vectors that will represent each document, mapping key terms, phrases, or “features” in the vectors.  The elements of the vectors can be binary (a simple 1 or 0 indicating the presence or absence of a term), but may prove more useful if more varied values are used.  For instance, integer or real values reflecting different or repeated usage across the document may prove useful, as might weighted values dependent on the relevance of a particular term.  Because some terms will be inflected, stemming algorithms, such as the Porter Stemmer, may prove useful.

You’ll train and test your model on the set of documents contained in ~/dropbox/08-09/570/project2/files1.  A file, labeled ~/dropbox/08-09/570/project2/files1-gold-standard.txt, gives the breakdown between linguistic and non-linguistic documents.  You may use this list to help determine the vocabulary relevant to building your model (such as to build a prototype vector containing the most relevant vocabulary).  The Friday before the assignment is due, a second set of documents will be provided in ~/dropbox/08-09/570/project2/files2.  Test your model against this second set to see how it categorizes these documents.  To test, you’ll build a vector space for the new set of documents, as you did for the first set.  However, neither the dimensions nor the weights of the model should be changed from the first to the second.  In other words, don’t add new vocabulary or features (dimensions) to accommodate the new set of documents.  With this second set, there will be another gold standard file, ~/dropbox/08-09/570/files2-gold-standard.txt, that you can use for calculating your precision and recall numbers.  (Please note:  the file names between files1 and files2 are not unique.  In other words, the file name alone cannot be used as a unique identifier.)

## Task 2 ##

For the second task, take input provided by the user (at the prompt’s fine) and return the documents that most closely match the terms that are given.  You’ll take the user input, and structure it as a vector, which you will then compare against the document vectors you have created.  The output should consist of a list of the documents that most closely match the query, where proximity is measured by some threshold value you have set.  Only documents identified as linguistic in Task 1 should be output.  A week before this Task is due, you will be provided with a set of test queries.

# Task 1 - Solution #
### Notes ###
No stop word list was used, the vocabulary selection process naturally removed common words. Stemming was attemed, but since it did little to improve the accuracy of the model, it was not used.

Given more time, I would have tried EM algorithms, which I believe are an improvement over K-means. It would be interesting to verify my assumption.

### Step 1. Vocabulary selection ###
Class: project2.PrintVocabulary {path of documents} {gold standard file}

PrintVocabulary assigns a average text frequency weight to words in the set of linguistic & non-linguistic documents. It also finds the global IDF score from all documents.

For all words that occured, a weight was calculated as follows:

> Weight = absolute( (AverageTextFrequency\_Linguistic **LinguisticDocumentFrequencyNormalized - AverageTextFrequencyNonLinguistic** NonLinguisticDocumentFrequencyNormalized) **GlobalIDF)**


Words were sorted by this weight and the top 10000 were printed. This weighting scheme highlights words with significant differences between Linguistic / Non-linguistic categories. The multiplication factor of GlobalIDF acts as a stop-list filter, preventing common words from showing up.

### Step 2. Create document vectors ###
Class: project2.PrintVectors {Vocabulary file} {path of documents}

PrintVectors uses the generated vocabulary as a vector space and map each document into it.

Note: next steps only use only a subset of the 10000-dimension (size of vocabulary) vector space.


### Step 3. Find best parameters for K-means clustering algorithm ###
Class: project2.PrintClustersHillClimb {Gold standard file} {Vocabulary file} {path of documents}

PrintClustersHillClimb is a double-loop iterating over a continuom of vocabulary and cluster sizes to use by K-means clustering. For each configuration, it runs K-means 15 times. Each time, the generated clusters are validated against training data, and the error rate is printed.

K-means implementation works as follows:
1. Create K initial cluster centroids. K / 2 are centered around a random selection of linguistic documents. The rest are centered around a random selection of non-linguistic documents.
2. Cluster each document to a centroid
3. Calculate new centroids
4. Repeat steps 2-3 until centroids are stable

### Step 4. Print cluster centroids found by K-means clustering algorithm ###
Class: project2.PrintClusters {Gold standard file} {Vocabulary file} {path of documents}

The best parameters are hard coded in the file, which outputs the cluster centroids. Each cluster is labelled Linguistic / Non-linguistic voted on the majority of it constituents.

From hill climbing, a cluster size of 3 and vocabulary of 2800 yielded a 99.71% F-measure over training data. Larger cluster sizes & vocabulary sizes yielded very similar results, but smaller is better so this configuration was chosen.

### Step 5. Test results for Task 1 ###
Class: project2.PrintTask1 {Vocabulary file} {Clusters file - step 3} {Gold standard file} {Path of documents}

This is the final step of Task 1. This program classifies any set of documents and compares it's classification accuracy against a gold standard.

Files are classified as


**Files2 results - test data**
Precision	%99.39
Recall:		%95.91
F-measure:	%97.62

**Files1 results - training data**
Precision	%99.71
Recall:		%99.71
F-measure:	%99.71

# Task 2 Solution #
Search was implemented in a straightforward way. A query is converted into a vector of words. Each word is weighted by IDF. A set of documents with lowest cosine similarity to the query is returned. The top 3 results are always returned, the cutoff is reached when the document's similarity score drops below 1/3 of the top score, 1/2 of the average. This threshold was chosen by hand.

However, the difficulty in this task lied in the difficulty of quantitatively measure my results. Given more time, I would have focused more efforts on that process which would allow me to test improvements to the model.


## Notes ##
An attempt was made at query expansion through reverse stemming. Note that the vector space' vocabulary is not stemmed (design decision in Task 1).

In this method, each query word & vocabulary are stemmed. A matching is made between the stemmed query words and stemmed vocabulary words. Once matched, the vocabulary words are expanded into their original forms.
This effectively expanded a query to all possible forms (from same stem) in the vocabulary.

However, this method didn't seem to increase relevancy that much (probably because it expanded the weight of the de-stemmed words), so it was abandoned.


## Search any set of files ##
Class: project2.Search {Vocabulary file} {Clusters file - from Task 1, step 3} {Path of documents}

Search takes a few minutes to load (depending on the number of documents specified in path.

In this program, each document is mapped into a vector space, and classified via the process as Task 1,


## Search pre-generated vectors ##
Class: project2.SearchVectors {Vocabulary file} {Clusters file - from Task 1, step 3} {Path of documents}

SearchVectors is a optimized version of SearchVectors, it reads a pre-generated document vectors (linguistic only). Otherwise, it is the same as Search.