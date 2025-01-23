# NullCheck
This is a work in progress!

A lightweight library for tracking how often Kotlin class fields are null.

When converting Java classes to Kotlin in larger projects, it can be challenging to determine which fields can be null and which fields are guaranteed to be non-null. As a result, fields in the new Kotlin classes are often declared as nullable by default.

This library provides insights into how often fields are actually null. It collects and stores metadata for all instantiations into two database tables.

After some time has passed, you can analyze the data and confidently declare fields as non-null if they are never null in practice.

## How to use
The library has been tested with PostgreSQL. To use it, first create the following database tables:
```
create table nullcheck_class(
    class_name text primary key,
    number_of_instantiations bigint not null
);

create table nullcheck_field(
  class_name text not null,
  field_name text not null,
  number_of_times_null bigint not null default 0,
  CONSTRAINT class_name_field_name_unique_constraint UNIQUE (class_name, field_name),
  FOREIGN KEY (class_name) references nullcheck_class(class_name)
);
```
Set up the `NullCheck` class at application startup as follows:

```
NullCheck.setUp(dataSource)
```
After setting it up, trigger the null check in an init block within the classes you want to monitor:
```
init { 
    checkNulls(this) 
}
```