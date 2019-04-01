SERVER_FILE=./src/server/*.java
SERVER_BIN=./bin/server/
SERVER_CLASS_FILE=$(SERVER_BIN)*.class
CLIENT_FILE=./src/client/*.java
CLIENT_BIN=./bin/client/
CLIENT_CLASS_FILE=$(CLIENT_BIN)*.class
arguments=error


server: $(SERVER_CLASS_FILE)
	java -classpath $(SERVER_BIN) Server $(arguments)

$(SERVER_CLASS_FILE): $(SERVER_FILE)
	rm -f $(SERVER_CLASS_FILE)
	javac -d $(SERVER_BIN) -cp $(SERVER_BIN) -s ./src/server $(SERVER_FILE)


client: $(CLIENT_CLASS_FILE)
	java -classpath $(CLIENT_BIN) Client $(arguments)

$(CLIENT_CLASS_FILE): $(CLIENT_FILE)
	rm -f $(CLIENT_CLASS_FILE)
	javac -d $(CLIENT_BIN) -cp $(CLIENT_BIN) -s ./src/client $(CLIENT_FILE)

clean:
	rm -f $(SERVER_CLASS_FILE)
	rm -f $(CLIENT_CLASS_FILE)