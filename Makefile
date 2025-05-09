# Default target
all:
	@echo "Specify an agent target. Available agent targets: run, sra, mra, uba, rla, lba"

# Simple reflex agent
sra: src/scala/SimpleReflexAgent.scala
	@sed -i '.orig' 's|.*// specify agent|\t\treturn SimpleReflexAgent.process(tp); // specify agent|' src/java/AgentFunction.java
	@make run
	@if [[ -f src/java/AgentFunction.java.orig ]]; then mv src/java/AgentFunction.java.orig src/java/AgentFunction.java; fi

# Model-based reflex agent
mra: src/scala/ModelBasedReflexAgent.scala
	@sed -i '.orig' 's|.*// specify agent|\t\treturn ModelBasedReflexAgent.process(tp); // specify agent|' src/java/AgentFunction.java
	@make run
	@if [[ -f src/java/AgentFunction.java.orig ]]; then mv src/java/AgentFunction.java.orig src/java/AgentFunction.java; fi

# Utility-based agent
uba: src/scala/UtilityBasedAgent.scala
	@sed -i '.orig' 's|.*// specify agent|\t\treturn UtilityBasedAgent.process(tp); // specify agent|' src/java/AgentFunction.java
	@make run
	@if [[ -f src/java/AgentFunction.java.orig ]]; then mv src/java/AgentFunction.java.orig src/java/AgentFunction.java; fi

# Reactive learning agent
# Change the forward probability in the run recipe
rla: src/scala/ReactiveLearningAgent.scala
	@sed -i '.orig' 's|.*// specify agent|\t\treturn ReactiveLearningAgent.process(tp); // specify agent|' src/java/AgentFunction.java
	@make run
	@if [[ -f src/java/AgentFunction.java.orig ]]; then mv src/java/AgentFunction.java.orig src/java/AgentFunction.java; fi

# LLM-based agent
# Requires the GOOGLE_API_KEY env var to be set
lba: src/scala/LLMBasedAgent.scala
	@sed -i '.orig' 's|.*// specify agent|\t\treturn LLMBasedAgent.process(tp); // specify agent|' src/java/AgentFunction.java
	@make run
	@if [[ -f src/java/AgentFunction.java.orig ]]; then mv src/java/AgentFunction.java.orig src/java/AgentFunction.java; fi

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
	@printf "Building the agent"
	@mkdir -p target & pid=$$!; \
		while kill -0 $$pid 2> /dev/null; do printf "."; sleep 0.5; done; \
		wait $$pid
	@scalac -classpath "lib/*" -d target src/java/*.java src/scala/*.scala & pid=$$!; \
		while kill -0 $$pid 2> /dev/null; do printf "."; sleep 0.5; done; \
		wait $$pid
	@javac -d target -cp target src/java/*.java & pid=$$!; \
		while kill -0 $$pid 2> /dev/null; do sleep 0.25; printf "."; sleep 0.25; done; \
		wait $$pid
	@echo

# Run the project
run: build
	@scala run -cp "target:lib/*" --main-class WorldApplication -- -n 1.00 -a false

tenk: build
	@echo "Running the agent 10,000 times..."
	@scala run -cp "target:lib/*" --main-class WorldApplication -- -n 1.00 -a false -t 10000 > /dev/null 
	@tail -n1 wumpus_out.txt
	@echo "Complete results in wumpus_out.txt"

la-tenk: build
	@echo "Running the agent 10,000 times with different forward probabilities..."

	@scala run -cp "target:lib/*" --main-class WorldApplication -- -n 1.0 -a false -t 3334 -f deterministic_out.txt > /dev/null
	@echo "Done deterministic trials, results written to deterministic_out.txt"

	@scala run -cp "target:lib/*" --main-class WorldApplication -- -n 0.8 -a false -t 3333 -f stochastic_out.txt > /dev/null
	@echo "Done stochastic trials, results written to stochastic_out.txt"

	@scala run -cp "target:lib/*" --main-class WorldApplication -- -n 0.3334 -a false -t 3333 -f random_out.txt > /dev/null
	@echo "Done random trials, results written to random_out.txt"

	@cat deterministic_out.txt stochastic_out.txt random_out.txt > wumpus_out.txt
	@rm -f deterministic_out.txt stochastic_out.txt random_out.txt
	@cat wumpus_out.txt | grep 'Total Score:'
	@cat wumpus_out.txt | grep 'Average Score:'
	@echo "Complete results in wumpus_out.txt"

# Clean the project and junk backup files
# If junk backups exist before build, then they are indeed junk
clean:
	@rm -rf target

# Phony targets
.PHONY: sra mra uba rla lba check build run tenk clean
