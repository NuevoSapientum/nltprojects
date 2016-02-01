# Project 1 Ling 570 - Korean POS Tagger #
From: http://courses.washington.edu/ling570/will_fall08/570-project1.htm

## Description ##
For this project, you will build a Markov Tagger for Korean.  Your tagger will be trained on a subset of the data from the Morphologically Annotated Korean Text, LDC2004T03, and be tested and evaluated against another subset of the corpus.   All required files can be found in dropbox/08-09/570/project1.

## The Data ##
The Morphologically Annotated Korean Text is a corpus of Korean texts that have been annotated using a modified Treebank tagset.  The corpus file was split into two files, one called korean-training.txt (the first 1000 records) and the other called korean-testing.txt (the final 574 records). Both are romanized versions of the original corpus (to make it easier for you to use!).   See the attached for the list of tags used in the corpus.  One of the entries from this corpus is shown here:

```
kunun               ku/NPN+ngun/PAU
lunoka              luno/NPR+ngi/PCA
3                  3/NNU
ngwelmalkkaci      ngwel/NNX+mal/NNX+kkaci/PAU
nginswuceynguy      nginswu/NNC+ceynguy/NNC
sihanngul           sihan/NNC+ngul/PCA
kacko               kac/VV+ko/ECS
ngisstako           ngiss/VX+ta/EFN+ko/PAD
tespwuthngyessta   tespwuthngi/VV+ngess/EPF+ta/EFN
.                  ./SFN
^EOS
```

The corpus consists of two columns, the second annotated and the first not.  For purposes of this project, you can ignore the first column.  Each line consists of one word, with each sentence ending in ^EOS.  Because Korean is typologically distinct from English—Korean is agglutinating and English is quasi-analytic—tagging Korean presents challenges over English.  Note, for example, that the tags are often marked on morphemes within words (although for monomorphemic forms, the tag can apply to the word).  Also note that the morphemes are separated by plusses (“+”).

## The Project ##
There are three parts to this project:  (1) analysis and write-up, (2) generating the transition and emission probabilities central to the tagger (training), and (3) running the model you have built against a subset of the data (testing).  For (3), you will use (and possibly modify) a Viterbi decoder supplied to you.

### Analysis and Write-up ###
English-centric taggers rely heavily on the word to word transitions for their success.  For instance, the transition from DT to JJ or NN is major predictor for these tags.  Will a similar approach work with Korean?  How might the methodology be adapted?  Since Korean tags apply to morphemes, how might relying on the morphological structure of Korean improve the performance of a tagger?  As always, unknown words (and morphemes) present challenges to Markov taggers.  How can the unknown word/morpheme problem be addressed for Korean?

Write-up your analysis of the Korean corpus, with an eye on what you intend to do in the development of your own tagger.  Feel free to think outside the box, but recognize that your tagger must rely on n-gram transition probabilities (bigram will probably be easiest to implement).  Note:  it may prove worthwhile to write tools to do preliminary analyses of the entire corpus to help guide your write-up.

### Training ###
The training phase of your tagger will involve generating transition and emission probabilities calculated over the corpus.  Although you will have access to the entire corpus, training should only be against the training set, which consists of the first 1000 sentences (there are 1574 sentences total).

### Testing ###
For testing, adapt the Viterbi decoder supplied to you, viterbi.pl.  This implementation of viterbi runs as a standalone application, accepting as input a tag vocabulary, and transition and emission probabilities.  It then takes as input on the command line a quoted string that it evaluates, outputting the best tag sequence for that input.  Try the application with the supplied matrices for English to see how it performs.

You’ll want to adapt this decoder for your purposes, mostly to integrate it with your code and also to figure out how to present the Korean data to it.  You may need to change the code to support log probabilities since underflows may be possible with a larger input set.  Don’t forget that this decoder will not handle unknown words/morphemes well, an issue you will need to address.

Test against the remaining 474 sentences in the corpus, which have been separated into the testing set.  Evaluate your tagger by computing accuracy, and give a count of the number of morphemes evaluated.  Submit in your project1-output.txt the following (in this order):  a. the tagged output assigned to the final 474 sentences (the test set), which should be formatted similar to the input corpus, noting mismatches with asterisks (e.g., NNX), b. the total number of morphemes evaluated, and c. the accuracy figure.  Include in comments.txt an evaluation of your tagger’s successes and failures.

# Solution #
## Preprocessing ##
As a preliminary step the first column of the training and testing data sets will be eliminated. Skipping this step would not cause irreparable harm as the presence of the first column certainly could be accounted for in the program code that will process the files. However, quasi standardizing the program code to process one-column data is a good idea because it increases the generalizability of the code and its potential future value. We will use UNIX tools (i.e.: “cut” and “cat”) to split the first column off from each line.

## Tagging Challenges ##
Measuring and calculating statistics based on word to word transitions will not be successful with our working corpus. Since Korean is an agglutinating language, it has a vocabulary orders of magnitude larger than English. Given the lack of training data orders of magnitude larger than English, relying on word-level statistics will likely present a sparsity problem. Another problem is that Korean words are often composed of morphemes of different tag classes.

The code we write will need to process the input on the level of morphemes. Fortunately for this project, we do not have to worry about designing a morpheme-breaker, which would have been a project onto its own. The training & test data are both anglicized and have specified morpheme boundaries marked with the “+” character.

We have a definitive list of tags used in the training corpus. Identifying the tags will be easy using regular expressions (i.e.: forward slash followed by uppercase tag markers followed by white space or new line or “+” character). A n-gram hidden Markov Model of Korean can have state transitions at a morpheme-level. Within this model, word-transition can be modeled as its own state.

One way to experiment with the code (to test differences in accuracy) is to train and test the code on bi-grams, tri-grams and potentially four-grams. However, while designing our n-gram model, we must keep in mind the limited size of our training corpus. A bigram model may be both simple and sufficient.

The other consequence of the limited-size training corpus is that unknown morphemes are likely to exist within the test corpus. To avoid zero probability, all tag states will accept unknown words with a non-zero probability. One potential solution is to generate a distribution of tags in the training corpus and apply known morpheme tags in the test data with similar statistical distribution. We could also use secondary training to measure the effectiveness of our approximation.

A significant amount of tweaking and re-design is expected with any language model, especially one for a foreign language.  If there are non-obvious model parameters, a search for optimal values will be automated. Given this, we must be careful not to optimize for a local maximum. To avoid that, we can separate our test data into two sets. However, we should be aware that any division of the data sets either for secondary training or testing purposes shrinks the sample size further and could introduce bias on its own.

## Analysis ##
The main program builds a bigram language model based on morphological tag-transitions and emissions using training-data. The bigram model is used by a Viterbi decoder to predict tags on a set of morpehemes from input. The Viterbi decoder operates on morpheme tokens in a sentence unit at a time. Word boundaries are ignored. We used naive smoothing for both Emissions and Transition probability.

To evaluate the effectiveness of our model we will need to compare the output of our program against a “gold standard”. Testing will consist of verifying the model against the tagged training data. The Viterbi algorithm will be used to search for most-likely tag sets for a sentence unit.  A semi-automated test harness will be built to support this endeavor.

Our very simple approach gave us a 93% tag-prediction accuracy on the 23042 morphemes found in the test data.

## Utility programs ##
The following Java programs were developed to aid development and testing

### korean-tagger.sh ###
This script prints Project 1 output using LDC2004T03 training data.

It has no parameters and takes Standard Input. Input is expected to be tagged text formatted similarly to LDC2004T03.

Since training occurs at run-time, the program around 30 seconds to run to completion on the provided test data with approximatly 23000 morphemes.


```
// Main program used by korean-tagger.sh
java edu.nlt.ling570.project1.Evaluate <training-file> < StdIn

// Prints emissions table
java edu.nlt.ling570.project1.PrintEmissions < StdIn

// Prints transitions table
java edu.nlt.ling570.project1.PrintTransitions < StdIn

// Trains using training-file and tags input using Viterbi
java edu.nlt.ling570.project1.PrintViterbi <training-file> <input-file>

// Removes tags from input
java edu.nlt.ling570.project1.PrintTagsRemoved < StdIn
 
// Used to measure the accuracy of PrintViterbi
java edu.nlt.ling570.project1.PrintAccuracy <Output of PrintViterbi>  <Tagged file>
```

## Important classes ##
```
edu.nlt.parser.LDC2004T03Parser - Parser used for training and test data
edu.nlt.formatter.LDC2004T03Formatter - Use to ouput in training / test data format
edu.nlt.algorithm.ViterbiAlgorithm - Modified Java Viterbi decoder based on supplied Perl Viterbi decoder
edu.nlt.shallow.data.EmissionTableBuilder - Build emissions table
edu.nlt.shallow.data.TransitionTableBuilder - Build transitions table
```