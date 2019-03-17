SERVER_FILE=$(shell find ./server/ -name '*.java')
SERVER_BIN=./bin/server/
SERVER_CLASS_FILE=$(SERVER_BIN)*.class
CLIENT_FILE=$(shell find ./client/ -name '*.java')
CLIENT_BIN=./bin/client/
CLIENT_CLASS_FILE=$(CLIENT_BIN)*.class


server: $(SERVER_CLASS_FILE)
	java -classpath $(SERVER_BIN) Server

$(SERVER_CLASS_FILE): $(SERVER_FILE)
	javac -d $(SERVER_BIN) $(SERVER_FILE)


client: $(CLIENT_CLASS_FILE)
	java -classpath $(CLIENT_BIN) Client

$(CLIENT_CLASS_FILE): $(CLIENT_FILE)
	javac -d $(CLIENT_BIN) $(CLIENT_FILE)

clean:
	rm -f $(SERVER_CLASS_FILE)
	rm -f $(CLIENT_CLASS_FILE)