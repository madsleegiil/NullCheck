# NullCheck
This is a work in progress!

A lightweight library for tracking how often Kotlin class fields are null.

When converting Java classes to Kotlin in larger projects, it can be challenging to determine which fields can be null and which fields are guaranteed to be non-null. As a result, fields in the new Kotlin classes are often declared as nullable by default.

This library provides insights into how often fields are actually null. It collects and stores metadata for all instantiations into two database tables.

After some time has passed, you can analyze the data and confidently declare fields as non-null if they are never null in practice.

