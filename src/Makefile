
all: MNB_classification.class

clean:
	- rm -rf *.class

#Search.class: Classifier.java StopWords.class Tokenizer.class MNB_classification.class
#	javac -Xlint Classifier.java

MNB_classification.class: MNB_classification.java Tokenizer.class Pair.class
	javac -Xlint MNB_classification.java

Tokenizer.class: Tokenizer.java StopWords.class
	javac -Xlint Tokenizer.java

StopWords.class: StopWords.java
	javac -Xlint StopWords.java

Pair.class: Pair.java
	javac -Xlint Pair.java

ReverseComparator.class: ReverseComparator.java
	javac -Xlint ReverseComparator.java

PorterStemmer.class: PorterStemmer.java
	javac -Xlint PorterStemmer.java
