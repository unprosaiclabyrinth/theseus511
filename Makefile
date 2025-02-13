# Default target
all: run

# Check if required tools are installed
check:
	@echo "Checking for required tools..."

	@command -v java >/dev/null 2>&1 || { echo >&2 "java is required but not installed. Aborting :("; exit 1; }
	@java --version

	@command -v scala >/dev/null 2>&1 || { echo >&2 "scala is required but not installed. Aborting :("; exit 1; }
	@scala --version

	@echo "All required tools are installed :)"

# Build the project
build: clean
	@mkdir -p target
	@scalac -d target src/java/*.java src/scala/*.scala
	@javac -d target -cp target src/java/*.java

# Run the project
run: build
	@scala run -cp target --main-class WorldApplication -- -n false -a false

# Clean the project
clean:
	@rm -rf target

# Phony targets
.PHONY: build run clean

