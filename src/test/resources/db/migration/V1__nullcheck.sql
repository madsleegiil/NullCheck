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