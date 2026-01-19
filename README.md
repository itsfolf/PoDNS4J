# PoDNS4J

A Java library for the [Pronouns over DNS](https://github.com/CutieZone/pronouns-over-dns) specification, allowing retrieval and parsing of pronouns from DNS TXT records.

## Features

- Full implementation of the Pronouns over DNS specification
- Parser with normalization and validation
- Support for wildcards, none records, and comments
- Automatic selection of preferred pronouns
- Zero dependencies

## Installation

### Gradle (via JitPack)

Add JitPack repository:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add dependency:

```gradle
dependencies {
    implementation 'com.github.itsfolf:podns4j:0.1.0-SNAPSHOT'
}
```

### Maven (via JitPack)

Add JitPack repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add dependency:

```xml
<dependency>
    <groupId>com.github.itsfolf</groupId>
    <artifactId>podns4j</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage

### Basic Lookup

```java
import me.folf.podns4j.PoDNS4J;
import me.folf.podns4j.model.*;

PronounResult result = PoDNS4J.lookup("example.com");
if (result != null && result.preferred() != null) {
    PronounSet pronouns = result.preferred();
    System.out.println(pronouns.subject() + "/" + pronouns.object());
    // Output: she/her
}
```

### Parsing Records

```java
// Parse a single record
PronounRecord record = PoDNS4J.parse("they/them;preferred");
System.out.println(record.pronounSet().subject()); // they
System.out.println(record.pronounSet().isPreferred()); // true

// Parse and select from multiple records
PronounResult result = PoDNS4J.parseAndSelect(List.of(
    "she/her",
    "they/them;preferred"
));
System.out.println(result.preferred().subject()); // they
```

### Checking Record Types

```java
PronounResult result = PoDNS4J.lookup("example.com");

if (result.prefersName()) {
    System.out.println("Refer to this person by name");
} else if (result.acceptsAny()) {
    System.out.println("Accepts any pronouns (preferred: " + result.preferred() + ")");
} else {
    System.out.println("Pronouns: " + result.preferred());
}
```

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```
