# Taxonomic Relation Classifier 

## Run tests

There are two tests embedded in `AllTest.java`

- `simpleClassifierTest()` reproduces the result of LOCAL TACREC Search

- `testWithConstraints()` reproduces the result of INFERENCE TACREC Search

In order to run the tests, 

`mvn lbjava:generate` to generate the LBJ Classifiers.

Modify `PMI_FILE` and `CONSTRAINT_WEIGHTS` variable in `Constraints.java` to correct local paths of those two files.

Then modify the data path variables (Marked as "TODO") within the two functions in `AllTest.java` to correct local paths of the data files.

Then modify the main function of `AllTest.java` to run each of the tests. Don't run both at the same time, since there are a lot of debug outputs.

## Understanding the results

Note that the two functions will produce both "Accuracy" and "F1"

The paper used "Accuracy", which is computed by the correct numbers of instances (including no relation) divided by the total number of instances (including no relation)

F1 is measure by taking out the no relation instances. (Not useful if you are comparing results with the paper).
