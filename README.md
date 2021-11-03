##### Table of Contents

1. [Overview](#overview)
1. [Content](#content)
   1. [Sources](#sources)
   1. [Queries](#queries)   
   1. [Sinks](#sinks)
   1. [Roots](#roots)
1. [Ledgers](#ledgers)

## Overview

`JBuildSled` is a library for implementing [key-value
stores](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) that
supports specific features for building compilers.  Content is stored
in a binary form, and is read into corresponding in-memory structures
(e.g. an [Abstract Syntax
Tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree)).  For
example, content might be stored on disk or in an archive file and
might also be _versioned_ (or not).  Roughly speaking a content
_source_ provides a way to read content, whilst a _sink_ provides a
way of writing content.  Furthermore, content can be stored in
journal-like structures called _ledgers_.

### Content

All managed content implements the `Content` interface, and is
associated with an instance of `Content.Type`.  In particular, given
an `InputStream` and a `Content.Type` one can attempt to instantiate
an instance of `Content` (though this might fail in the `InputStream`
is corrupted, etc).  The following illustrates a minimal example:

```Java
interface Shape extends Content {}

class Point implements Shape {
  public static Content.Type<Point> ContentType = new Content.Type<Point>() {

    @Override
    public Point read(InputStream input) throws IOException {
      try (ObjectInputStream ois = new ObjectInputStream(input)) {
        int x = ois.readInt();
        int y = ois.readInt();
        return new Point(x, y);
      }
    }

    @Override
    public void write(OutputStream output, Point value) throws IOException {
      try (ObjectOutputStream ois = new ObjectOutputStream(output)) {
        ois.writeInt(value.x);
        ois.writeInt(value.y);
      }
    }

  };

  public final int x;
  public final int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public Type<?> getContentType() {
    return ContentType;
  }
}
```

This defines a class of structured content, `Point`, associated with
`Point.ContentType`.  Through `Point.ContentType` we can serialise /
deserialise our structured content (e.g. read it from disk into an
instance of `Point`, or write an instance of `Point` back into its
binary form).

#### Sources

A content source is an instance of `Content.Source`, and provides an
API for reading structured content out of a store.  Content sources
(and sinks) are hierarchically structured into a tree-like
organisation (roughly similar to that of a file system).  The
following illustrates a simple method for reading a `Point` out of an
arbitrary source:

```Java
Point read(Content.Source<String,Shape> source) throws IOException {
    return source.get(Point.ContentType, "test");
}
```

This reads an instance of `Point` from the key `test` in
the given source.

#### Queries

Sources can be _queried_ arbitrarily using instances of
`Content.Filter` as follows:

```Java
List<Point> select(Content.Source source, Content.Filter<Point> query) throws IOException {
    return source.getAll(query);
}
```

This method returns a list of all matching `Point` instances in the
given source.

#### Sinks

A content sink is an instance of `Content.Sink`, and provides an API
for writing structured content into the store.  The following
illustrates writing a piece of structured content into the store:

```Java
void write(Content.Sink<String,Shape> sink, Point pt) throws IOException {
    sink.put("test", pt);
}
```

Again, this writes our `Point` instance into the store.  Observe that
if `Point` were mutable, then any _changes to point after the write
would not be visible in the store_.  Generally speaking, we encourage
the use of immutable classes for implementing structure content.

#### Roots

A content root is an instance of `Content.Root` which means it is both
a `Content.Source` and `Content.Sink`.  In other words, its an
end-point for our structured content (such as a filesystem or
database).  Perhaps the simplest example of a root is `DirectoryRoot`
which allows one to treat a file system directory as a store for
structured content.

Another interesting example of a root is a `ZipFile` which is both an
item of structure content, and a root for the structured content it
contains.

## Ledgers

A content _ledger_ is an instance of `Content.Ledger`.  Unlike a
`Content.Source` a ledger provides a versioned history of our content
(roughly similar to the idea of a [journaling
filesystem](https://en.wikipedia.org/wiki/Journaling_file_system)).
This means, for example, we can compare content between versions to
see what has changed.  This is useful, for example, in the context of
[incremental
compilers](https://en.wikipedia.org/wiki/Incremental_compiler) where
it provides a key piece of the underlying functionality.
