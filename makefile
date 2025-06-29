# Makefile para compilar y ejecutar Main.java
SRC_DIR=src
BIN_DIR=bytecode
MAIN_CLASS=Main
ARGS=''

all: compile

compile: $(BIN_DIR)
	cd $(SRC_DIR) && javac -d ../$(BIN_DIR) $(MAIN_CLASS).java
	
$(BIN_DIR):
	mkdir $(BIN_DIR)

run: all
	java -cp $(BIN_DIR) $(MAIN_CLASS) $(ARGS)

clean:
	rm -rf $(BIN_DIR)