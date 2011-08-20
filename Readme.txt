There are two projects that are managed by ant/maven. There are six demo projects that demo the server and client. There are two flex clients included as well.

Here are some basic instructions to build and compile or run junit tests.

With Ant (default target is 'compile'):

ant clean:
- deletes the entire target folder for both server and client projects

ant compile:
- compiles all classes to the target folders for the server and client projects

ant jar:
- creates a server and client library jar of the framework