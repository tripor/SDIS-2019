SERVER_FILE=./src/server/Server.java
SERVER_BIN=./bin/server/
SERVER_CLASS_FILE=$(SERVER_BIN)*.class
CLIENT_FILE=./src/client/Client.java
CLIENT_BIN=./bin/client/
CLIENT_CLASS_FILE=$(CLIENT_BIN)*.class
arguments=error


server: $(SERVER_CLASS_FILE)
	java -classpath $(SERVER_BIN) src.server.Server $(arguments)

$(SERVER_CLASS_FILE): $(SERVER_FILE)
	javac -d $(SERVER_BIN) -cp $(SERVER_BIN) -sourcepath ./src/ $(SERVER_FILE)


client: $(CLIENT_CLASS_FILE)
	java -classpath $(CLIENT_BIN) src.client.Client $(arguments)

$(CLIENT_CLASS_FILE): $(CLIENT_FILE)
	javac -d $(CLIENT_BIN) -cp $(CLIENT_BIN) -sourcepath ./src/ $(CLIENT_FILE)

clean:
	rm -f $(SERVER_CLASS_FILE)
	rm -f $(CLIENT_CLASS_FILE)