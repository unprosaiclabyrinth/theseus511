#!/bin/bash

# Function to display usage
usage() {
    echo "usage: $0 [-n <num_trials>] [-o <output_file>] [-h]"
    echo "Options:"
    echo "  -n <num_trials>  Number of times to run the simulation."
    echo "  -o <output_file> Output file to save the scores."
    echo "  -h               Display this help message and exit."
    exit 1
}

# Initialize variables
number=10
output_file="wumpus_eval.txt"

# Parse command-line options
while getopts ":n:o:h" opt; do
    case $opt in
        n)
            number=$OPTARG
            ;;
        o)
            output_file=$OPTARG
            ;;
        h)
            usage
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            usage
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2
            usage
            ;;
    esac
done

echo "Running wumpus simulation $number times and compiling scores..."

for((i=0;i<number;++i)); do
    scala run -cp target --main-class WorldApplication -- -n false -a false -s 200 > /dev/null;
    tail -n2 wumpus_out.txt | head -n1 | cut -d ':' -f 2 | awk '{print $1}';
done > "$output_file"

echo "Scores written to \"$output_file\""

avg=$(echo "$(tr '\n' '+' < "$output_file" | sed 's/+$/\n/' | bc)/$number" | bc)
echo "Average score over $number trials was $avg"
