SRC_FILES=./src/*.java
SRC_BIN=./bin/
SRC_CLASS=$(SRC_BIN)*.class
arguments=


all: compile
	java -ea -classpath $(SRC_BIN) src.Server $(arguments)

client: compile
	java -ea -classpath $(SRC_BIN) src.Client $(arguments)

compile:
	if [ ! -d "$(SRC_BIN)" ]; then \
		mkdir $(SRC_BIN); \
	fi
	javac -Xlint:deprecation -d $(SRC_BIN) -cp $(SRC_BIN) -s ./src $(SRC_FILES)

$(SRC_FILES):
	compile



clean:
	rm -r ./bin
	rm -r ./files