
all: Project3.class

clean:
	- rm -rf *.class

#Search.class: Classifier.java StopWords.class Tokenizer.class MNB_classification.class
#	javac -Xlint Classifier.java


Project3.class: MNB_classification.class MNB_evaluation.class Tokenizer.class Project3.java
	javac -Xlint Project3.java

MNB_evaluation.class: MNB_classification.class MNB_evaluation.java
	javac -Xlint MNB_evaluation.java

MNB_classification.class: MNB_classification.java Pair.class Utilities.class ReverseComparator.class MNB_probability.class
	javac -Xlint MNB_classification.java

Tokenizer.class: Tokenizer.java StopWords.class
	javac -Xlint Tokenizer.java

StopWords.class: StopWords.java
	javac -Xlint StopWords.java

Utilities.class: Utilities.java
	javac -Xlint Utilities.java

MNB_probability.class: MNB_probability.java
	javac -Xlint MNB_probability.java

ReverseComparator.class: ReverseComparator.java
	javac -Xlint ReverseComparator.java

Pair.class: Pair.java
	javac -Xlint Pair.java
