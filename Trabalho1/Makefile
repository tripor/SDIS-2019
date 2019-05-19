SERVER_FILE=./src/*.java
SERVER_BIN=./bin/
SERVER_CLASS_FILE=$(SERVER_BIN)*.class
arguments=error

all: $(SERVER_CLASS_FILE)
	java -classpath $(SERVER_BIN) Server $(arguments)

$(SERVER_CLASS_FILE): $(SERVER_FILE)
	rm -f $(SERVER_CLASS_FILE)
	if [ ! -d "./bin" ]; then \
		mkdir ./bin; \
	fi
	if [ ! -d "$(SERVER_BIN)" ]; then \
		mkdir $(SERVER_BIN); \
	fi
	javac -d $(SERVER_BIN) -cp $(SERVER_BIN) -s ./src $(SERVER_FILE)

compile:$(SERVER_FILE)
	rm -f $(SERVER_CLASS_FILE)
	if [ ! -d "./bin" ]; then \
		mkdir ./bin; \
	fi
	if [ ! -d "$(SERVER_BIN)" ]; then \
		mkdir $(SERVER_BIN); \
	fi
	javac -d $(SERVER_BIN) -cp $(SERVER_BIN) -s ./src $(SERVER_FILE)


clean:
	rm -f $(SERVER_CLASS_FILE)
	rm -f $(CLIENT_CLASS_FILE)