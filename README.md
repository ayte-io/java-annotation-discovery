# Language Element Discovery

[![Bintray](https://img.shields.io/bintray/v/ayte/maven/discovery.svg?style=flat-square)](https://bintray.com/ayte/maven/discovery)
[![CircleCI/master](https://img.shields.io/circleci/project/github/ayte-io/java-discovery/master.svg?style=flat-square)](https://circleci.com/gh/ayte-io/workflows/java-discovery/tree/master)
[![Scrutinizer/master](https://img.shields.io/scrutinizer/g/ayte-io/java-discovery/master.svg?style=flat-square)](https://scrutinizer-ci.com/g/ayte-io/java-discovery/?branch=master)
[![Code Climate/issues](https://img.shields.io/codeclimate/issues/ayte-io/java-discovery.svg?style=flat-square)](https://codeclimate.com/github/ayte-io/java-discovery)

## Motivation and problem solving

Every once in a while end developer needs to find specific classes (or 
even non-class symbols, like methods and type parameters) in his 
classpath. Usually this is done via implementing annotations and 
classpath scanning, then it turns out that classpath scanning has to
account for several annotations, contracts change, application startup 
time increases, and nobody gets happy.

This library provides means for easy compile-time symbol discovery
(and, probably, one day runtime as well) through annotation processing.
The API exposes just several elements needed for work. First, it's
`@AutoDiscovery` annotation, which is used for two things

- Marking symbols to be discovered, amking a huge collection
- Marking other annotations as `@AutoDiscovery(transitive = true)`, 
which will turn other annotations as discovery roots themselves.

The second entrypoint is `Repository` interface - it allows to get all 
discovered symbols of specific type, either as a giant set or filtered 
by specific annotation. `Repository.getInstance()` will return 
compile-time populated repository instance, which will hold all marked
symbols.

Currently supported symbols are:

- Packages
- Classes
- Interfaces
- Annotations
- Enums
- Type parameters
- Class fields
- Enum constants
- Class methods (limited support)
- Method parameters

## Usage

The usage is very simple. First, you need to pass compile-time 
processor to compiler - this is done by adding `provided` scope 
dependency on `io.ayte.utility.discovery:compile-time-processor`:

```xml
<dependency>
  <groupId>io.ayte.utility.discovery</groupId>
  <artifactId>compile-time-processor</artifactId>
  <version>0.1.0</version>
  <scope>provided</scope>
</dependency>
```

Also you will need standalone API dependency to query results in 
runtime:

```xml
<dependency>
  <groupId>io.ayte.utility.discovery</groupId>
  <artifactId>api</artifactId>
  <version>0.1.0</version>
</dependency>
```

Then you start with annotating elements with `@AutoDiscovery`:

```java
import AutoDiscovery;

@AutoDiscovery
class OneOfManyImplementations implements Contract {}
```

The annotation processor will pick up and store this class during
compilation:

```java 
import Repository; 

public class Main {
    public static void main(String[] args) {
        Repository.getInstance().getClasses().forEach(System.out::println);
    }
}
```

This is very simple, but in real world you will need to differentiate 
found classes. Some of those would be services, other would be data 
objects, etc., etc. To do so, you can introduce your own discovery 
annotations:

```java
import AutoDiscovery;

public class Main {
    @AutoDiscovery(transitive = true)
    public @interface ServiceDiscovery {}

    @AutoDiscovery(transitive = true)
    public @interface DataObjectDiscovery {}
    
    public static void main(String[] args) {
        System.out.println("Found services:");
        Repository.getInstance().getClasses(ServiceDiscovery.class).forEach(System.out::println);
        System.out.println("Found data objects:");
        Repository.getInstance().getClasses(DataObjectDiscovery.class).forEach(System.out::println);
    }
}
```

And that's just it.

There are just two more things to mention. First, repository doesn't 
return language symbols directly - it returns name / type wrappers, 
which can be converted to symbols with `.toSymbol()` method. This is 
done because runtime classpath is often different from compile-time 
classpath, and there are no guarantees that corresponding symbols are
really present and loadable. The last thing is that compile-time 
processor generates resource files for everything it finds:

- `META-INF/io/ayte/utility/discovery/<annotation-name>.yml` file for 
every annotation, which will contain references to all symbols found 
by that annotation
- `META-INF/io/ayte/utility/discovery/$combined.yml` file with all 
found symbols for all annotations

And this pretty much sums up the whole functionality.

## Development process

## Limitations

- Currently Java 8 is used for building. That means no module discovery
as well as no module packaging. Hopefully this will be addressed in 
future.
- No runtime scanner is implemented at this moment. Of course it is
pleasant to catch everything compile-time, but sometimes one just needs
to do this in runtime.
- Methods are stored as shit. Annotation processing doesn't let me to 
capture parameter types, so if you have two identically-named methods
with identically-named parameters, well, then everything is pretty much
borked and you have to find correct method among those yourself. Same 
applies for method parameters, since their parent can be quite 
ambiguous.

## Dev branch state

[![CircleCI/dev](https://img.shields.io/circleci/project/github/ayte-io/java-discovery/dev.svg?style=flat-square)](https://circleci.com/gh/ayte-io/workflows/java-discovery/tree/dev)
[![Scrutinizer/dev](https://img.shields.io/scrutinizer/g/ayte-io/java-discovery/dev.svg?style=flat-square)](https://scrutinizer-ci.com/g/ayte-io/java-discovery/?branch=dev)

## Contributing

Please send pull requests for **dev** branch.

## Licensing

MIT / Ayte Labs / 2018
